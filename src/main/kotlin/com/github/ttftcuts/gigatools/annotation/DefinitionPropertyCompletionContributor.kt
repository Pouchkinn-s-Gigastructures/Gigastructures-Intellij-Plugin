package com.github.ttftcuts.gigatools.annotation

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiComment
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class DefinitionPropertyCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement(PsiComment::class.java),
            object: CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet) {
                    // get the comment element
                    val comment = parameters.position

                    // check that we're a property comment and get data
                    val data = Definition.getPropertyData(comment) ?: return

                    data.type.addCompletions(data, parameters, context, resultSet)

//                    // find the next element and check that it's a definition
//                    val nextElement = PsiUtils.nextNonWhiteSpaceSibling(comment)
//                    if (nextElement !is ParadoxScriptDefinitionElement) { return }
//                    // get the valid tags for the definition's type
//                    val elementType = nextElement.definitionInfo?.typeConfig?.name ?: "unknown"
//                    val validTags = ToolData.definitionTags[elementType] ?: return
//
//                    // add all valid tags to the list, along with their descriptions
//                    resultSet.addAllElements(validTags.keys.map { s -> LookupElementBuilder.create(s).withTypeText( validTags[s]?.shortDesc ) })
                }
            })
    }
}