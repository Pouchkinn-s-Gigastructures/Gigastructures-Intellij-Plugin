package com.github.ttftcuts.gigatools.main.wrappers.parts

import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.github.ttftcuts.gigatools.main.util.PsiUtils.findPropertyAndInline
import com.github.ttftcuts.gigatools.main.wrappers.EconomicCategory
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.findProperty

interface EconomicUnit {
    val def: ParadoxScriptDefinitionElement

    companion object {
        val EconomicUnit.economicCategory : EconomicCategory?
            get() {
                val data: PsiUtils.ResolvedElement = def.findPropertyAndInline("resources") ?: return null
                val block = data.element.propertyValue
                if (block !is ParadoxScriptBlockElement) { return null }

                val category = block.findProperty("category") ?: return null
                val categoryValue = category.propertyValue?.value ?: return null

                return EconomicCategory.resolve(def.project, data.resolver(categoryValue) )
            }
    }
}

