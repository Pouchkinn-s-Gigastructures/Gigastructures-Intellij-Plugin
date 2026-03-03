package com.github.ttftcuts.gigatools.main.definitions.properties

import com.github.ttftcuts.gigatools.annotation.GigaToolsAttributesKeys
import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.DefinitionHolder
import com.github.ttftcuts.gigatools.main.definitions.PropertyCompanion
import com.github.ttftcuts.gigatools.main.definitions.PropertyData
import com.github.ttftcuts.gigatools.main.util.TextUtils
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.script.psi.ParadoxDefinitionElement

class MegaFamilyProperty(override val def: ParadoxDefinitionElement) : DefinitionHolder, IMegaFamilyProperty {
    override var megaFamily: String? = null

    companion object: PropertyCompanion("Family", "Override for megastructure family names. Only needed if the mega is not connected via upgrades and the name does not reduce to the same family name.") {
        val pattern = Regex("\\w+")

        override fun validForDefinitionType(type: String): Boolean {
            return type == "megastructure"
        }

        fun getFamilyOverride(def: ParadoxDefinitionElement): String? {
            val data = Definition.getAttachedProperties(def).firstOrNull{ p -> p.type == MegaFamilyProperty }
                ?: Definition.getWholeFileProperties(def).firstOrNull{ p -> p.type == MegaFamilyProperty } ?: return null

            return data.propertyText.splitByBlank().firstOrNull()
        }

        override fun annotate(holder: AnnotationHolder, data: PropertyData) {
            val startOffset = data.element.textRange.startOffset + data.propertyTextOffset
            val parts = pattern.findAll(data.propertyText).toList()
            for (part in parts) {
                if (part == parts.first()) {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(TextUtils.range(part.range).shiftRight(startOffset)).textAttributes(GigaToolsAttributesKeys.PROPERTY_NAME_KEY).create()
                } else {
                    holder.newAnnotation(HighlightSeverity.WARNING, "Max one family name").range(TextUtils.range(part.range).shiftRight(startOffset)).highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL).create()
                }
            }
        }

        override fun addCompletions(data: PropertyData, parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet) {
            super.addCompletions(data, parameters, context, resultSet)
        }
    }
}

interface IMegaFamilyProperty : DefinitionHolder {
    var megaFamily: String?
}