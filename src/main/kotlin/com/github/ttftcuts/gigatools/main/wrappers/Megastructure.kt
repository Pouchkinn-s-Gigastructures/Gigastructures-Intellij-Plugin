package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.util.PsiUtils.findPropertyAndInline
import com.github.ttftcuts.gigatools.main.wrappers.parts.EconomicUnit
import com.github.ttftcuts.gigatools.main.wrappers.parts.EconomicUnit.Companion.economicCategory
import com.intellij.openapi.project.Project
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class Megastructure(def: ParadoxScriptDefinitionElement) : Definition(def), EconomicUnit {
    // what mega family (build chain) does this mega belong to?
    // e.g. "is this a dyson sphere?"
    var megaFamily: String? = null

    val upgradeFrom : Set<Megastructure> by lazy {
        val upgradeData = def.findPropertyAndInline("upgrade_from") ?: return@lazy setOf()
        val resolver = upgradeData.second ?: { e: String -> e }
        val upgradeElement = upgradeData.first ?: return@lazy setOf()
        val upgradeBlock = upgradeElement.propertyValue
        if (upgradeBlock !is ParadoxScriptBlockElement) { return@lazy setOf() }

        upgradeBlock.valueList.mapNotNull {
            v ->
            //println("in ${def.name}: $v, ${v.javaClass}");
            resolve(def.project, resolver(v.value))
        }.toSet()
    }

    val upgradeTo : Set<Megastructure> by lazy {
        resolveAll(def.project)
        cache.values.filterNotNull().filter { e -> (e != this) && e.upgradeFrom.contains(this) }.toSet()
    }

    companion object: WrapperCompanion<Megastructure>("megastructure", ::Megastructure) {
        val allFamilies: MutableMap<String, MutableSet<Megastructure>> = mutableMapOf()

        fun familyName(mega: Megastructure): String {
            val familyNameRaw = mega.name

            if (ToolData.MegaFamilies.nameOverrides.contains(familyNameRaw)) {
                return ToolData.MegaFamilies.nameOverrides[familyNameRaw]!!
            }
            val regex = Regex("(\\w+?)(?:_(?:ruined|\\d)\\w*)?")
            val result = regex.matchEntire(familyNameRaw)
            //println(result?.groups)

            val familyName = result?.groups[1]?.value

            return familyName ?: error("Unable to generate proper family name for mega $familyNameRaw")
        }

        fun deriveAllTags(project: Project) {
            val allMegaTags = ToolData.getTagsForType("megastructure")
            val firstStageTag = allMegaTags["first_stage"] ?: error("first_stage megastructure tag missing!")
            val finalStageTag = allMegaTags["final_stage"] ?: error("final_stage megastructure tag missing!")

            // get every mega
            resolveAll(project)

            // base sweep, for stuff all megas should check
            for(mega in cache.values.filterNotNull()) {

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
            val firstStages = cache.values.filterNotNull().filter { mega ->
                mega.upgradeFrom.isEmpty() &&
                mega.upgradeTo.isNotEmpty() &&
                !mega.hasAnyTags("technical", "dummy_first_stage", "placeholder")
            }
            for (firstStage in firstStages) {
                firstStage.addDerivedTag(firstStageTag)

                // get any ancestor override or start with this mega
                val ancestorOverride = ToolData.MegaFamilies.ancestorOverrides[firstStage.name]
                val ancestor = if (ancestorOverride != null) resolve(project, ancestorOverride) ?: firstStage else firstStage
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

                    val nonTechnicalUpgradeTo = mega.upgradeTo.filter { e -> !e.hasAnyTags("technical") }

                    // apply last
                    if (mega.hasTags("force_final_stage") || nonTechnicalUpgradeTo.isEmpty()) {
                        mega.addDerivedTag(finalStageTag)
                    }

                    if (!mega.hasTags("force_final_stage")) {
                        toProcess.addAll(nonTechnicalUpgradeTo)
                    }
                }
            }
        }

        // also clear families map on cache clear
        override fun clearCache() {
            cache.clear()
            allFamilies.clear()
        }
    }
}