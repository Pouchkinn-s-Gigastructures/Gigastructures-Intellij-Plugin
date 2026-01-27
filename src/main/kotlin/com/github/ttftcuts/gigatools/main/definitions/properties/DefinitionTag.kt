package com.github.ttftcuts.gigatools.main.definitions.properties

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.github.ttftcuts.gigatools.main.util.YAMLUtils.asText
import com.github.ttftcuts.gigatools.main.util.YAMLUtils.getItemsAndCast
import com.github.ttftcuts.gigatools.main.util.YAMLUtils.getValueAndCast
import com.intellij.psi.PsiComment
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import io.ktor.http.escapeIfNeeded
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequence

class DefinitionTag(val name: String, val shortDesc: String, val fullDesc: String, val incompatibleList: List<String>?) {

    fun entry(): Pair<String, DefinitionTag> {
        return Pair(name, this)
    }

    override fun toString(): String {
        //return "DefinitionTag( \"${name.escapeIfNeeded()}\" | \"${shortDesc.escapeIfNeeded()}\" | \"${fullDesc.escapeIfNeeded()}\" )"
        return "DefinitionTag( \"${name.escapeIfNeeded()}\" )"
    }

    companion object {
        val pattern by lazy { Regex("@(\\S+)\\b") }

        fun fromYAMLKeyValue(tagPair: YAMLKeyValue) : DefinitionTag {
            val name = tagPair.keyText
            val def: YAMLMapping = tagPair.getValueAndCast()
            val desc: String = def.getKeyValueByKey("desc")?.asText() ?: ""
            val fullDesc: String = def.getKeyValueByKey("fullDesc")?.asText() ?: ""
            val incompatibleList: List<String>? = def.getKeyValueByKey("incompatible")?.getValueAndCast<YAMLSequence>()?.getItemsAndCast<YAMLScalar>()?.map { item -> item.asText() }

            return DefinitionTag(name, desc, fullDesc, incompatibleList)
        }

        fun getTags(definition: ParadoxScriptDefinitionElement) : Set<DefinitionTag>? {
            // get both property sets
            val data = Definition.getAttachedProperties(definition).firstOrNull { p -> p.type == TagProperty }
            val fileData = Definition.getWholeFileProperties(definition).firstOrNull { p -> p.type == TagProperty }

            // get valid tags for the definition's type
            val definitionType = Definition.getDefinitionType(definition)
            val validTags = ToolData.definitionTags[definitionType] ?: return null

            // fill set of found tags
            val tags : MutableSet<DefinitionTag> = mutableSetOf()
            if (data != null) {
                tags.addAll(getTagsFromString(data.propertyText, validTags))
            }
            if (fileData != null) {
                tags.addAll(getTagsFromString(fileData.propertyText, validTags))
            }

            return tags
        }

        fun getTagsFromString(input: String, validTags: Map<String, DefinitionTag>): Set<DefinitionTag> {
            // array for found tags
            val tags : MutableSet<DefinitionTag> = mutableSetOf()

            // check each match against the tags
            val propertyMatches = pattern.findAll(input)
            for(match in propertyMatches) {
                // won't be null, or it wouldn't match the pattern
                val tag = match.groups[1]!!.value

                // insert valid tags
                if (validTags.containsKey(tag)) {
                    tags.add(validTags[tag]!!)
                }
            }
            return tags
        }

        fun getTagNames(definition: ParadoxScriptDefinitionElement) : Set<String>? { return getTags(definition)?.map { tag -> tag.name }?.toSet() }
    }
}