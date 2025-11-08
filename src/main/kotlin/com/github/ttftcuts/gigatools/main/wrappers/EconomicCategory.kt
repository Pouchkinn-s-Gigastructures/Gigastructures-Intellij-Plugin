package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.tagging.TaggedDefinition
import com.github.ttftcuts.gigatools.main.util.PsiUtils.findPropertyAndInline
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class EconomicCategory(def: ParadoxScriptDefinitionElement) : TaggedDefinition(def) {
    val parent: EconomicCategory? by lazy {
        var parentData = def.findPropertyAndInline("parent") ?: return@lazy null
        val resolver = parentData.second ?: { e: String -> e }
        if (parentData.first?.value == null) return@lazy null
        return@lazy resolve(def.project, resolver(parentData.first!!.value!!))
    }

    companion object : WrapperCompanion<EconomicCategory>("economic_category", ::EconomicCategory)
}