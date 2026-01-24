package com.github.ttftcuts.gigatools.main.definitions

import com.github.ttftcuts.gigatools.main.util.PsiUtils.isVanilla
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

interface DefinitionHolder {
    val def: ParadoxScriptDefinitionElement
    fun isVanilla(): Boolean { return def.isVanilla() }

    val name get() = def.name
}



