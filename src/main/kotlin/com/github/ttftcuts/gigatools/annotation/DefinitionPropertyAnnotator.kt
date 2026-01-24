package com.github.ttftcuts.gigatools.annotation

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.DefinitionTag
import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptRootBlock

class DefinitionPropertyAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        //println(element.text)
        // only comments
        if (element !is PsiComment) { return }
        // only top level elements
        if (element.parent !is ParadoxScriptRootBlock && element.parent !is ParadoxScriptFile) { return }

        val text = element.text
        // only specially annotated lines
        if (!text.startsWith(DefinitionTag.PREFIX)) { return }
        //println("STARTED")

        // get next non-whitespace element or bail
        val nextElement: PsiElement = PsiUtils.nextNonWhiteSpaceSibling(element) ?: return
        // only definition lines
        if (nextElement !is ParadoxScriptDefinitionElement) { return }
        // work out what type of thing we're looking at for getting valid tags
        val elementType = nextElement.definitionInfo?.typeConfig?.name ?: "unknown"
        // get the valid tags or abort if there aren't any
        val validTags = ToolData.definitionTags[elementType] ?: return

        // mark the prefix
        val prefixRange = TextRange.from(element.textRange.startOffset, DefinitionTag.PREFIX.length)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(prefixRange).textAttributes(GigaToolsAttributesKeys.PROPERTY_LINE_KEY).create()

        // find all properties via pattern
        val propertyMatches = DefinitionTag.pattern.findAll(text, DefinitionTag.PREFIX.length)
        for(match in propertyMatches) {
            // the @ at the start
            val markerRange = TextRange.from(element.textRange.startOffset + match.range.first, 1)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(markerRange).textAttributes(GigaToolsAttributesKeys.PROPERTY_KEY).create()

            // won't be empty or null otherwise it wouldn't match the pattern
            val property = match.groups[1]!!
            val propertyRange = TextRange.from(element.textRange.startOffset + property.range.first, property.range.last - property.range.first + 1)
            val propertyName = property.value

            if (validTags.containsKey(propertyName)) {
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

        //println("ANNOTATED: $text")
    }

//    fun register(disposable: Disposable) {
//        LanguageAnnotators.INSTANCE.addExplicitExtension(ParadoxScriptLanguage, this)
//        disposable.whenDisposed {
//            LanguageAnnotators.INSTANCE.removeExplicitExtension(ParadoxScriptLanguage, this)
//        }
//    }
}