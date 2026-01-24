package com.github.ttftcuts.gigatools.main.definitions.modules

import com.github.ttftcuts.gigatools.main.definitions.DefinitionTag
import com.github.ttftcuts.gigatools.main.definitions.DefinitionHolder
import com.github.ttftcuts.gigatools.main.definitions.ModuleCompanion
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class TagModule(override val def: ParadoxScriptDefinitionElement) : DefinitionHolder, ITagModule {
    override val tags: Map<String,DefinitionTag> by lazy { DefinitionTag.getTags(def)?.associate { tag -> tag.name to tag }?.toMap() ?: mapOf() }
    override val derivedTags: MutableMap<String,DefinitionTag> = mutableMapOf()

    override fun hasTags(vararg tagsToCheck : String, includeDerived: Boolean) : Boolean {
        if (includeDerived && derivedTags.keys.containsAll((tagsToCheck.toList()))) {
            return true
        }
        return tags.keys.containsAll(tagsToCheck.toList())
    }

    // does this definition have ANY listed tag
    override fun hasAnyTags(vararg tagsToCheck : String, includeDerived: Boolean) : Boolean {
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

    override fun addDerivedTag(tag: DefinitionTag) {
        if (derivedTags.containsKey(tag.name)) { return }
        derivedTags[tag.name] = tag
    }

    companion object: ModuleCompanion
}

interface ITagModule: DefinitionHolder {
    val tags: Map<String,DefinitionTag>
    val derivedTags: MutableMap<String,DefinitionTag>

    fun hasTags(vararg tagsToCheck : String, includeDerived: Boolean = true) : Boolean
    fun hasAnyTags(vararg tagsToCheck : String, includeDerived: Boolean = true) : Boolean
    fun addDerivedTag(tag: DefinitionTag)
}