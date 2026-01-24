package com.github.ttftcuts.gigatools.main.definitions

import com.github.ttftcuts.gigatools.main.definitions.modules.ITagModule
import com.github.ttftcuts.gigatools.main.definitions.modules.TagModule
import com.github.ttftcuts.gigatools.main.util.PsiUtils.isVanilla
import com.intellij.openapi.project.Project
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

open class ModularDefinition(override val def: ParadoxScriptDefinitionElement) : DefinitionHolder, ITagModule by TagModule(def) {
    override fun toString(): String {
        return "(${this.javaClass.simpleName}: ${def.name})"
    }

    companion object {
        fun resolve(project: Project, type: String, id: String) : ModularDefinition? {
            val found = ParadoxDefinitionSearch.search(id,type, selector(project, project.projectFile).definition().distinctByName()).find() ?: return null
            return ModularDefinition(found)
        }
    }
}