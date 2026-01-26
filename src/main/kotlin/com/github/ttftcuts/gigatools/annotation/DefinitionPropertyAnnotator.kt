package com.github.ttftcuts.gigatools.annotation

import com.github.ttftcuts.gigatools.main.data.Consts
import com.github.ttftcuts.gigatools.main.definitions.PropertyCompanion
import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import kotlinx.html.COL

class DefinitionPropertyAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // only comments
        if (element !is PsiComment) { return }
        // only top level elements
        if (element.parent !is ParadoxScriptRootBlock) { return }

        // value of the comment
        var text = element.text
        // only specially annotated lines
        if (!text.startsWith(PREFIX)) { return }

        // only if there are script definitions in the file
        if (element.parent.children.isEmpty() || element.parent.children.first() !is ParadoxScriptDefinitionElement) { return }

        // use the first child to determine what kind of definitions this file "should" have
        val fileDefType = (element.parent.children.first() as ParadoxScriptDefinitionElement).definitionInfo?.typeConfig?.name ?: "unknown"

        // snip down the text range
        var textOffset = PREFIX.length
        text = text.substring(textOffset)

        // find whether this comment is attached to a definition
        val definition = PsiUtils.findAssociatedDefinition(element)
        // is this a "whole file" property?
        val wholeFile = (definition == null) && (element.textRange.endOffset < element.parent.children.first().textRange.startOffset)
        // if we're not attached and not whole file, bail
        if (definition == null && !wholeFile) { return }

        // determine property type
        var propertyType: PropertyCompanion? = null
        for (pType in Consts.DefinitionPropertyTypes) {
            if (text.startsWith(pType.prefix)) {
                propertyType = pType
                break
            }
        }
        if (propertyType == null) { return }
        textOffset += propertyType.prefix.length
        text = text.substring(propertyType.prefix.length)

        // check for the colon lol
        if (!text.startsWith(COLON)) { return }
        textOffset += COLON.length
        text = text.substring(COLON.length)


        // next, if we have a definition check types
        if (definition != null) {
            val elementType = definition.definitionInfo?.typeConfig?.name ?: "unknown"

            // if we aren't allowed here, complain
            if (!propertyType.validForDefinitionType(elementType)) {
                holder
                    .newAnnotation(HighlightSeverity.WARNING, "Property \"$propertyType\" is not valid for definition type $elementType")
                    .range(element.textRange)
                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    .create()
                return
            }
        }

        // highlight the prefix to say we're ok
        val prefixRange = TextRange.from(element.textRange.startOffset, textOffset)
        if (wholeFile) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(prefixRange)
                .textAttributes(GigaToolsAttributesKeys.PROPERTY_LINE_KEY_WHOLE_FILE).create()
        } else {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(prefixRange)
                .textAttributes(GigaToolsAttributesKeys.PROPERTY_LINE_KEY).create()
        }

        // hand over processing to the property itself
        propertyType.annotate(element, holder, definition, fileDefType, text, textOffset)

//        // get next non-whitespace element or bail
//        val nextElement: PsiElement = PsiUtils.nextNonWhiteSpaceSibling(element) ?: return
//        // only definition lines
//        if (nextElement !is ParadoxScriptDefinitionElement) { return }
//        // work out what type of thing we're looking at for getting valid tags
//        val elementType = nextElement.definitionInfo?.typeConfig?.name ?: "unknown"
//        // get the valid tags or abort if there aren't any
//        val validTags = ToolData.definitionTags[elementType] ?: return
//
//        // mark the prefix
//        val prefixRange = TextRange.from(element.textRange.startOffset, DefinitionTag.PREFIX.length)
//        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(prefixRange).textAttributes(GigaToolsAttributesKeys.PROPERTY_LINE_KEY).create()
//
//        // find all properties via pattern
//        val propertyMatches = DefinitionTag.pattern.findAll(text, DefinitionTag.PREFIX.length)
//        for(match in propertyMatches) {
//            // the @ at the start
//            val markerRange = TextRange.from(element.textRange.startOffset + match.range.first, 1)
//            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(markerRange).textAttributes(GigaToolsAttributesKeys.PROPERTY_KEY).create()
//
//            // won't be empty or null otherwise it wouldn't match the pattern
//            val property = match.groups[1]!!
//            val propertyRange = TextRange.from(element.textRange.startOffset + property.range.first, property.range.last - property.range.first + 1)
//            val propertyName = property.value
//
//            if (validTags.containsKey(propertyName)) {
//                holder
//                    .newAnnotation(HighlightSeverity.INFORMATION, validTags[propertyName]?.fullDesc ?: "")
//                    .range(propertyRange)
//                    .textAttributes(GigaToolsAttributesKeys.PROPERTY_NAME_KEY)
//                    .create()
//            } else {
//                holder
//                    .newAnnotation(HighlightSeverity.WARNING, "Unknown tag \"$propertyName\" for type $elementType")
//                    .range(propertyRange)
//                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
//                    .create()
//            }
//        }

        //println("ANNOTATED: $text")
    }

    companion object {
        const val PREFIX: String = "## "
        const val FILE: String = "File "
        const val COLON: String = ": "
    }

//    fun register(disposable: Disposable) {
//        LanguageAnnotators.INSTANCE.addExplicitExtension(ParadoxScriptLanguage, this)
//        disposable.whenDisposed {
//            LanguageAnnotators.INSTANCE.removeExplicitExtension(ParadoxScriptLanguage, this)
//        }
//    }
}