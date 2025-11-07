package com.github.ttftcuts.gigatools.main.tagging

import com.intellij.openapi.project.Project
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

open class TaggedDefinition(val def: ParadoxScriptDefinitionElement) {
    val tags: Map<String,DefinitionTag> by lazy { DefinitionTag.getTags(def)?.associate { tag -> tag.name to tag } ?: mapOf() }

    // does this definition have EVERY listed tag
    fun hasTags(vararg tagsToCheck : String) : Boolean {
        return tags.keys.containsAll(tagsToCheck.toList())
    }

    // does this definition have ANY listed tag
    fun hasAnyTags(vararg tagsToCheck : String) : Boolean {
        for(tag in tagsToCheck) {
            if (tags.containsKey(tag)) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "(${this.javaClass.canonicalName}: ${def.name})"
    }

    companion object {
        fun resolve(project: Project, type: String, id: String) : TaggedDefinition? {
            val found = ParadoxDefinitionSearch.search(id,type, selector(project, project.projectFile).definition().distinctByName()).find() ?: return null
            return TaggedDefinition(found)
        }
    }
}