package com.github.ttftcuts.gigatools.main.definitions.properties

import com.github.ttftcuts.gigatools.annotation.GigaToolsAttributesKeys
import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.DefinitionHolder
import com.github.ttftcuts.gigatools.main.definitions.HasDefinitionElement
import com.github.ttftcuts.gigatools.main.definitions.PropertyCompanion
import com.github.ttftcuts.gigatools.main.definitions.PropertyData
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.util.ProcessingContext
import icu.windea.pls.script.psi.ParadoxDefinitionElement

class TagProperty(override val def: DefinitionHolder) : HasDefinitionElement, ITagProperty {
    override val tags: Map<String,DefinitionTag> by lazy { DefinitionTag.getTags(def.base)?.associate { tag -> tag.name to tag }?.toMap() ?: mapOf() }
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

    override fun getAllTags(): Iterable<DefinitionTag> {
        return Iterable {
            iterator {
                yieldAll(tags.values)
                yieldAll(derivedTags.values)
            }
        }
    }

    companion object: PropertyCompanion("Tags", "Tags for reference in other GigaTools functions, used to categorise where Stellaris does not.") {

        override fun annotate( holder: AnnotationHolder, data: PropertyData ) {
            //println("p: $prefix@$textOffset: $text")
            val validTags = ToolData.definitionTags[data.definitionType] ?: mapOf()

            val startOffset = data.element.textRange.startOffset + data.propertyTextOffset
            val propertyMatches = DefinitionTag.pattern.findAll(data.propertyText)
            for(match in propertyMatches) {
                //println("match ${match.value}")
                // the @ at the start
                val markerRange = TextRange.from(startOffset + match.range.first, 1)
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(markerRange).textAttributes(GigaToolsAttributesKeys.PROPERTY_KEY).create()

                // won't be empty or null otherwise it wouldn't match the pattern
                val property = match.groups[1]!!
                val propertyRange = TextRange.from(startOffset + property.range.first, property.range.last - property.range.first + 1)
                val propertyName = property.value

                if (validTags.containsKey(propertyName)) {
                    holder
                        .newAnnotation(HighlightSeverity.INFORMATION, validTags[propertyName]?.fullDesc ?: "")
                        .range(propertyRange)
                        .textAttributes(GigaToolsAttributesKeys.PROPERTY_NAME_KEY)
                        .create()
                } else {
                    holder
                        .newAnnotation(HighlightSeverity.WARNING, "Unknown tag \"$propertyName\" for type ${data.definitionType}")
                        .range(propertyRange)
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .create()
                }
            }
        }

        override fun addCompletions( data: PropertyData, parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet ) {
            // get the valid tags for the definition's type
            val validTags = ToolData.definitionTags[data.definitionType] ?: return
            // add all valid tags to the list, along with their descriptions
            resultSet.addAllElements(validTags.keys.map { s -> LookupElementBuilder.create(s).withTypeText( validTags[s]?.shortDesc ) })
        }
    }
}

interface ITagProperty: HasDefinitionElement {
    val tags: Map<String,DefinitionTag>
    val derivedTags: MutableMap<String,DefinitionTag>

    fun hasTags(vararg tagsToCheck : String, includeDerived: Boolean = true) : Boolean
    fun hasAnyTags(vararg tagsToCheck : String, includeDerived: Boolean = true) : Boolean
    fun addDerivedTag(tag: DefinitionTag)
    fun getAllTags(): Iterable<DefinitionTag>
}