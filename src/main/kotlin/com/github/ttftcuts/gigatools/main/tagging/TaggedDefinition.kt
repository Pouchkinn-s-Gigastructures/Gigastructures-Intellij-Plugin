package com.github.ttftcuts.gigatools.main.tagging

import com.github.ttftcuts.gigatools.main.util.PsiUtils.isVanilla
import com.intellij.openapi.project.Project
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

open class TaggedDefinition(val def: ParadoxScriptDefinitionElement) {
    open val tags: Map<String,DefinitionTag> by lazy { DefinitionTag.getTags(def)?.associate { tag -> tag.name to tag }?.toMap() ?: mapOf() }
    val derivedTags: MutableMap<String,DefinitionTag> = mutableMapOf()

    val name get() = def.name

    // does this definition have EVERY listed tag
    fun hasTags(vararg tagsToCheck : String, includeDerived: Boolean = true) : Boolean {
        if (includeDerived && derivedTags.keys.containsAll((tagsToCheck.toList()))) {
            return true
        }
        return tags.keys.containsAll(tagsToCheck.toList())
    }

    // does this definition have ANY listed tag
    fun hasAnyTags(vararg tagsToCheck : String, includeDerived: Boolean = true) : Boolean {
        for(tag in tagsToCheck) {
            if (includeDerived && derivedTags.containsKey(tag)) {
                return true
            }
            if (tags.containsKey(tag)) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "(${this.javaClass.simpleName}: ${def.name})"
    }

    fun addDerivedTag(tag: DefinitionTag) {
        if (derivedTags.containsKey(tag.name)) { return }
        derivedTags[tag.name] = tag
    }

    fun isVanilla(): Boolean { return def.isVanilla() }

    companion object {
        fun resolve(project: Project, type: String, id: String) : TaggedDefinition? {
            val found = ParadoxDefinitionSearch.search(id,type, selector(project, project.projectFile).definition().distinctByName()).find() ?: return null
            return TaggedDefinition(found)
        }
    }
}