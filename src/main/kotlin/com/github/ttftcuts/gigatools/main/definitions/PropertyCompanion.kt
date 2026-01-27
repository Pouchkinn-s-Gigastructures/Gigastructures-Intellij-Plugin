package com.github.ttftcuts.gigatools.main.definitions

import com.github.ttftcuts.gigatools.annotation.GigaToolsAttributesKeys
import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.properties.DefinitionTag
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

open class PropertyCompanion(val prefix: String, val description: String) {
    open fun validForDefinitionType(type: String): Boolean { return true }

    open fun annotate(holder: AnnotationHolder, data: PropertyData) {

    }

    open fun addCompletions(data: PropertyData, parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet) {

    }
}