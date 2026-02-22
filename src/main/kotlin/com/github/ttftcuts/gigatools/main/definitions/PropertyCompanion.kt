package com.github.ttftcuts.gigatools.main.definitions

import com.github.ttftcuts.gigatools.main.data.Consts
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.util.ProcessingContext

open class PropertyCompanion(val prefix: String, val description: String) {
    open fun validForDefinitionType(type: String): Boolean { return true }

    open fun annotate(holder: AnnotationHolder, data: PropertyData) {

    }

    open fun addCompletions(data: PropertyData, parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet) {

    }
}