package com.github.ttftcuts.gigastructuresintellijplugin.main.tagging

import com.github.ttftcuts.gigastructuresintellijplugin.main.data.ToolData
import com.github.ttftcuts.gigastructuresintellijplugin.main.util.GigaPsiUtils
import com.github.ttftcuts.gigastructuresintellijplugin.main.util.GigaYAMLUtil.asText
import com.github.ttftcuts.gigastructuresintellijplugin.main.util.GigaYAMLUtil.getValueAndCast
import com.intellij.psi.PsiComment
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import io.ktor.http.escapeIfNeeded
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import kotlin.text.get

class DefinitionTag(val name: String, val shortDesc: String, val fullDesc: String) {

    fun entry(): Pair<String, DefinitionTag> {
        return Pair(name, this)
    }

    override fun toString(): String {
        return "DefinitionTag( \"${name.escapeIfNeeded()}\" | \"${shortDesc.escapeIfNeeded()}\" | \"${fullDesc.escapeIfNeeded()}\" )"
    }

    companion object {
        const val PREFIX = "## Tags:"
        val pattern by lazy { Regex("(?<=\\s)@(\\S+)") }

        fun fromYAMLKeyValue(tagPair: YAMLKeyValue) : DefinitionTag {
            val name = tagPair.keyText
            val def: YAMLMapping = tagPair.getValueAndCast()
            val desc: String = def.getKeyValueByKey("desc")?.asText() ?: ""
            val fullDesc: String = def.getKeyValueByKey("fullDesc")?.asText() ?: ""

            return DefinitionTag(name, desc, fullDesc)
        }

        fun getTags(definition: ParadoxScriptDefinitionElement) : Set<DefinitionTag>? {
            // get the previous comment
            val prevElement = GigaPsiUtils.prevNonWhiteSpaceSibling(definition)
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