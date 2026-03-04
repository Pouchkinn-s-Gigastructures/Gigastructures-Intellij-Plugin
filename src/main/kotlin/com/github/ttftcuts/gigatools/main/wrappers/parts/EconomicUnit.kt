package com.github.ttftcuts.gigatools.main.wrappers.parts

import com.github.ttftcuts.gigatools.main.definitions.DefinitionHolder
import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.github.ttftcuts.gigatools.main.util.PsiUtils.findPropertyAndInline
import com.github.ttftcuts.gigatools.main.wrappers.EconomicCategory
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.script.psi.ParadoxScriptBlockElement

interface EconomicUnit {
    val def: DefinitionHolder

    companion object {
        val EconomicUnit.economicCategory : EconomicCategory?
            get() {
                val data: PsiUtils.ResolvedElement = def.base.findPropertyAndInline("resources") ?: return null
                val block = data.element.propertyValue
                if (block !is ParadoxScriptBlockElement) { return null }

                val category = block.properties().find{p->p.name=="category"} ?: return null // .findProperty("category") ?: return null
                val categoryValue = category.propertyValue?.value ?: return null

                return EconomicCategory.resolve(def.base.project, data.resolver(categoryValue) )
            }
    }
}

