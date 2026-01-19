package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.tagging.DefinitionTag
import com.github.ttftcuts.gigatools.main.tagging.TaggedDefinition
import com.github.ttftcuts.gigatools.main.util.PsiUtils.findPropertyAndInline
import com.github.ttftcuts.gigatools.main.wrappers.parts.EconomicUnit
import com.github.ttftcuts.gigatools.main.wrappers.parts.EconomicUnit.Companion.economicCategory
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class Megastructure(def: ParadoxScriptDefinitionElement) : TaggedDefinition(def), EconomicUnit {
    // include valid tags from eco category
    override val tags: MutableMap<String,DefinitionTag> by lazy {
        val tags = super.tags

        // for each tag in the eco category's tags, make sure it's compatible then add the equivalent mega tag
        if (economicCategory != null) {
            ecoTags@ for (ecoTag in economicCategory!!.tags.values) {
                if (tags.contains(ecoTag.name)) {
                    continue
                }
                if (ecoTag.incompatibleList == null) {
                    continue
                }
                for (incompatible in ecoTag.incompatibleList) {
                    if (tags.keys.contains(incompatible)) {
                        continue@ecoTags
                    }
                }
                val matching = ToolData.definitionTags["megastructure"]?.get(ecoTag.name) ?: continue@ecoTags
                tags[ecoTag.name] = matching
            }
        }

        // TODO: do first stage and last stage tag stuff here

        tags
    }

    val upgradeFrom : Set<Megastructure> by lazy {
        val upgradeData = def.findPropertyAndInline("upgrade_from") ?: return@lazy setOf()
        val resolver = upgradeData.second ?: { e: String -> e }
        val upgradeElement = upgradeData.first ?: return@lazy setOf()
        val upgradeBlock = upgradeElement.propertyValue
        if (upgradeBlock !is ParadoxScriptBlockElement) { return@lazy setOf() }

        upgradeBlock.valueList.mapNotNull { v -> println("in ${def.name}: $v, ${v.javaClass}"); resolve(def.project, resolver(v.value)) }.toSet()
    }

    val upgradeTo : Set<Megastructure> by lazy {
        resolveAll(def.project)
        cache.values.filterNotNull().filter { e -> (e != this) && e.upgradeFrom.contains(this) }.toSet()
    }

    companion object: WrapperCompanion<Megastructure>("megastructure", ::Megastructure)
}