package com.github.ttftcuts.gigatools.annotation

import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.PropertyData
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class DefinitionPropertyAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // get property data for this element, if null then it's not something to annotate anyway
        val data = Definition.getPropertyData(element) ?: return

        // if we aren't allowed here, complain
        if (!data.type.validForDefinitionType(data.definitionType)) {
            holder
                .newAnnotation(HighlightSeverity.WARNING, "Property \"${data.type}\" is not valid for definition type ${data.definitionType}")
                .range(element.textRange)
                .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                .create()
            return
        }

        // check that this isn't a duplicate property line
        if (Definition.isPropertyDuplicate(data.element)) {
            holder
                .newAnnotation(HighlightSeverity.WARNING, "Can't have multiple properties of the same type")
                .range(element.textRange)
                .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                .create()
            return
        }

        // highlight the prefix to say we're ok
        val prefixRange = TextRange.from(element.textRange.startOffset, data.propertyTextOffset)
        if (data.wholeFile) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(prefixRange)
                .tooltip("${data.type.description}<br/><br/>Applied all entries in this file")
                .textAttributes(GigaToolsAttributesKeys.PROPERTY_LINE_KEY_WHOLE_FILE).create()
        } else {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(prefixRange)
                .tooltip(data.type.description)
                .textAttributes(GigaToolsAttributesKeys.PROPERTY_LINE_KEY).create()
        }

        // hand over processing to the property itself
        data.type.annotate(holder, data)
    }
}