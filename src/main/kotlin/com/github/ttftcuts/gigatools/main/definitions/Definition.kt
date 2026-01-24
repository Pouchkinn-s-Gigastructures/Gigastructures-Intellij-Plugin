package com.github.ttftcuts.gigatools.main.definitions

import com.github.ttftcuts.gigatools.main.definitions.properties.ITagProperty
import com.github.ttftcuts.gigatools.main.definitions.properties.TagProperty
import com.intellij.openapi.project.Project
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

open class Definition(override val def: ParadoxScriptDefinitionElement) : DefinitionHolder, ITagProperty by TagProperty(def) {
    override fun toString(): String {
        return "(${this.javaClass.simpleName}: ${def.name})"
    }

    companion object {
        fun resolve(project: Project, type: String, id: String) : Definition? {
            val found = ParadoxDefinitionSearch.search(id,type, selector(project, project.projectFile).definition().distinctByName()).find() ?: return null
            return Definition(found)
        }
    }
}