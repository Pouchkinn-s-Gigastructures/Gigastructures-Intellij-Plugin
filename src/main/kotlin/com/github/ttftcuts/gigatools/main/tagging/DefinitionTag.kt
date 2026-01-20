package com.github.ttftcuts.gigatools.main.tagging

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.github.ttftcuts.gigatools.main.util.YAMLUtils.asText
import com.github.ttftcuts.gigatools.main.util.YAMLUtils.getItemsAndCast
import com.github.ttftcuts.gigatools.main.util.YAMLUtils.getValueAndCast
import com.intellij.psi.PsiComment
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import io.ktor.http.escapeIfNeeded
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLScalarText
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
        const val PREFIX = "## Tags:"
        val pattern by lazy { Regex("(?<=\\s)@(\\S+)") }

        fun fromYAMLKeyValue(tagPair: YAMLKeyValue) : DefinitionTag {
            val name = tagPair.keyText
            val def: YAMLMapping = tagPair.getValueAndCast()
            val desc: String = def.getKeyValueByKey("desc")?.asText() ?: ""
            val fullDesc: String = def.getKeyValueByKey("fullDesc")?.asText() ?: ""
            val incompatibleList: List<String>? = def.getKeyValueByKey("incompatible")?.getValueAndCast<YAMLSequence>()?.getItemsAndCast<YAMLScalar>()?.map { item -> item.asText() }

            return DefinitionTag(name, desc, fullDesc, incompatibleList)
        }

        fun getTags(definition: ParadoxScriptDefinitionElement) : Set<DefinitionTag>? {
            // get the previous comment
            val prevElement = PsiUtils.prevNonWhiteSpaceSibling(definition)
            if (prevElement !is PsiComment) { return null }

            // check that it starts with the prefix
            val text = prevElement.text
            if (!text.startsWith(PREFIX)) { return null }

            // find valid tags
            val elementType = definition.definitionInfo?.typeConfig?.name ?: "unknown"
            val validTags = ToolData.definitionTags[elementType] ?: return null

            // array for found tags
            val tags : MutableSet<DefinitionTag> = mutableSetOf()

            // check each match against the tags
            val propertyMatches = pattern.findAll(text, PREFIX.length)
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