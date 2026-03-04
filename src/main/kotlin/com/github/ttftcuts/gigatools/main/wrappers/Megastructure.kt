package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.DefinitionHolder
import com.github.ttftcuts.gigatools.main.definitions.properties.IMegaFamilyProperty
import com.github.ttftcuts.gigatools.main.definitions.properties.MegaFamilyProperty
import com.github.ttftcuts.gigatools.main.util.PsiUtils.findPropertyAndInline
import com.github.ttftcuts.gigatools.main.wrappers.parts.EconomicUnit
import com.github.ttftcuts.gigatools.main.wrappers.parts.EconomicUnit.Companion.economicCategory
import com.intellij.openapi.project.Project
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.values
import icu.windea.pls.script.psi.ParadoxScriptBlockElement

class Megastructure(override val def: DefinitionHolder) : Definition(def), EconomicUnit, IMegaFamilyProperty by MegaFamilyProperty(def) {

    val upgradeFrom : Set<Megastructure> by lazy {
        val upgradeData = def.base.findPropertyAndInline("upgrade_from") ?: return@lazy setOf()
        val upgradeBlock = upgradeData.element.propertyValue
        if (upgradeBlock !is ParadoxScriptBlockElement) { return@lazy setOf() }

        upgradeBlock.values().mapNotNull {
            v ->
            //println("in ${def.name}: $v, ${v.javaClass}");
            resolve(def.base.project, upgradeData.resolver(v.value))
        }.toSet()
    }

    val upgradeTo : Set<Megastructure> by lazy {
        resolveAll(def.base.project)
        cache.values.filter { e -> (e != this) && e.upgradeFrom.contains(this) }.toSet()
    }

    companion object: WrapperCompanion<Megastructure>("megastructure", ::Megastructure) {
        val allFamilies: MutableMap<String, MutableSet<Megastructure>> = mutableMapOf()

        fun familyName(mega: Megastructure): String {
            val override = MegaFamilyProperty.getFamilyOverride(mega.def.base)
            if (override != null) { return override }

            val regex = Regex("(\\w+?)(?:_(?:permanently_ruined|ruined|\\d)\\w*)?")
            val result = regex.matchEntire(mega.name)
            //println(result?.groups)

            val familyName = result?.groups[1]?.value

            return familyName ?: error("Unable to generate proper family name for mega ${mega.name}")
        }

        fun deriveAllTags(project: Project) {
            val allMegaTags = ToolData.getTagsForType("megastructure")
            val firstStageTag = allMegaTags["first_stage"] ?: error("first_stage megastructure tag missing!")
            val finalStageTag = allMegaTags["final_stage"] ?: error("final_stage megastructure tag missing!")
            val buildableTag = allMegaTags["buildable"] ?: error("buildable megastructure tag missing!")

            // get every mega
            resolveAll(project)

            // base sweep, for stuff all megas should check
            for(mega in cache.values) {

                // inherit tags from economic category
                if (mega.economicCategory != null) {
                    ecoTags@ for (ecoTag in mega.economicCategory!!.tags.values) {
                        if (mega.hasTags(ecoTag.name, includeDerived = false)) {
                            continue
                        }
                        if (ecoTag.incompatibleList == null) {
                            continue
                        }
                        if (mega.hasAnyTags(*ecoTag.incompatibleList.toTypedArray(), includeDerived = false) ) {
                            continue@ecoTags
                        }
                        val matching = allMegaTags[ecoTag.name] ?: continue@ecoTags
                        mega.derivedTags[ecoTag.name] = matching
                    }
                }
            }

            // find all first stage megas and propagate forward family membership
            val firstStages = cache.values.filter { mega ->
                mega.upgradeFrom.isEmpty() &&
                //mega.upgradeTo.isNotEmpty() &&
                !mega.hasAnyTags("technical", "dummy")
            }
            for (firstStage in firstStages) {
                firstStage.addDerivedTag(firstStageTag)

                // get any ancestor override or start with this mega
                //val ancestorOverride = ToolData.MegaFamilies.ancestorOverrides[firstStage.name]
                //val ancestor = if (ancestorOverride != null) resolve(project, ancestorOverride) ?: firstStage else firstStage
                val ancestor = firstStage
                val ancestorFamilyName = familyName(ancestor)

                // set up family object if it's not done already
                if (!allFamilies.contains(ancestorFamilyName)) {
                    allFamilies[ancestorFamilyName] = mutableSetOf()
                }
                val family = allFamilies[ancestorFamilyName]!!

                // process descendants
                val toProcess = mutableSetOf(firstStage)
                val processed: MutableSet<Megastructure> = mutableSetOf()

                while(toProcess.isNotEmpty()) {
                    val mega = toProcess.first()
                    toProcess.remove(mega)
                    if (processed.contains(mega)) { continue }
                    processed.add(mega)

                    if (mega.megaFamily == null) {
                        mega.megaFamily = ancestorFamilyName
                        family.add(mega)
                    } else if (mega.megaFamily != ancestorFamilyName) {
                        // this shouldn't happen if stuff is arranged properly
                        println("Warning: Attempted to add mega [${mega.name}] to family [$ancestorFamilyName], but it already belongs to ${mega.megaFamily}")
                    }

                    val nonTechnicalUpgradeTo = mega.upgradeTo.filter { e -> !e.hasAnyTags("technical", "dummy") }

                    // apply last
                    if (mega.hasTags("force_final_stage") || nonTechnicalUpgradeTo.isEmpty()) {
                        mega.addDerivedTag(finalStageTag)
                    }

                    if (!mega.hasTags("force_final_stage")) {
                        val nonDummyUpgradeTo = mega.upgradeTo.filter { e -> !e.hasAnyTags("dummy") }
                        toProcess.addAll(nonDummyUpgradeTo)
                    }
                }
            }

            // determine buildability, all first stages which don't have always = no as a potential
            fun tagBuildable(mega: Megastructure) {
                mega.addDerivedTag(buildableTag)
            }
            for (firstStage in firstStages) {
                val potential = firstStage.def.base.findPropertyAndInline("potential")
                // if no potential block, buildable
                if (potential == null) { tagBuildable(firstStage); continue }

                // if potential but it's busted, not buildable
                val block = potential.element.propertyValue as? ParadoxScriptBlockElement ?: continue

                val blockProperties = block.properties(inline = true).toList()
                // if the potential is empty, buildable
                if (blockProperties.isEmpty()) {
                    tagBuildable(firstStage); continue
                }
                // if the potential is always = no, not buildable
                else if (blockProperties.count() == 1) {
                    val first = blockProperties.first()
                    if (first.name.lowercase() == "always" && first.value?.lowercase() == "no") {
                        continue
                    }
                }

                // if we got this far, buildable
                tagBuildable(firstStage)
            }
        }

        // also clear families map on cache clear
        override fun clearCache() {
            cache.clear()
            allFamilies.clear()
        }
    }
}