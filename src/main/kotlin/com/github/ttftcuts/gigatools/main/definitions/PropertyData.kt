package com.github.ttftcuts.gigatools.main.definitions

import com.intellij.psi.PsiElement
import icu.windea.pls.script.psi.ParadoxDefinitionElement

class PropertyData(val element: PsiElement, val type: PropertyCompanion, val definition: ParadoxDefinitionElement?,
                   val definitionType: String, val wholeFile: Boolean, val propertyText: String, val propertyTextOffset: Int) {
    override fun toString(): String {
        return "$definitionType Property ${type.prefix}: $propertyText F:$wholeFile, O:$propertyTextOffset"
    }
}