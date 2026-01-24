package com.github.ttftcuts.gigatools.main.definitions

import com.github.ttftcuts.gigatools.annotation.GigaToolsAttributesKeys
import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.properties.DefinitionTag
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

open class PropertyCompanion(val prefix: String, val description: String = "") {
    fun validForDefinitionType(type: String): Boolean { return true }

    fun annotate(element: PsiElement, holder: AnnotationHolder, definition: ParadoxScriptDefinitionElement?, text: String, textOffset: Int) {
        //println("p: $prefix@$textOffset: $text")
        val elementType = definition?.definitionInfo?.typeConfig?.name ?: "unknown"
        val validTags = ToolData.definitionTags[elementType] ?: mapOf()

        val startOffset = element.textRange.startOffset + textOffset
        val propertyMatches = DefinitionTag.pattern.findAll(text)
        for(match in propertyMatches) {
            //println("match ${match.value}")
            // the @ at the start
            val markerRange = TextRange.from(startOffset + match.range.first, 1)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(markerRange).textAttributes(GigaToolsAttributesKeys.PROPERTY_KEY).create()

            // won't be empty or null otherwise it wouldn't match the pattern
            val property = match.groups[1]!!
            val propertyRange = TextRange.from(startOffset + property.range.first, property.range.last - property.range.first + 1)
            val propertyName = property.value

            if (definition == null || validTags.containsKey(propertyName)) {
                holder
                    .newAnnotation(HighlightSeverity.INFORMATION, validTags[propertyName]?.fullDesc ?: "")
                    .range(propertyRange)
                    .textAttributes(GigaToolsAttributesKeys.PROPERTY_NAME_KEY)
                    .create()
            } else {
                holder
                    .newAnnotation(HighlightSeverity.WARNING, "Unknown tag \"$propertyName\" for type $elementType")
                    .range(propertyRange)
                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    .create()
            }
        }
    }
}