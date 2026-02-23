package com.github.ttftcuts.gigatools.language

import com.github.ttftcuts.gigatools.main.definitions.properties.TaggedListInfo
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.jetbrains.annotations.Unmodifiable

class TagLangInjector: MultiHostInjector {
    override fun getLanguagesToInject( registrar: MultiHostRegistrar, context: PsiElement ) {
        val text = context.text

        if(context is PsiComment && text.startsWith(PREFIX)) {

            val matches = TaggedListInfo.partsPattern.findAll(text.substring(PREFIX.length)).filter { m -> m.groups[1]?.value == "tags" }.toList()

            if (matches.isEmpty()) { return }

            registrar.startInjecting(TagLanguage)
            for (match in matches) {
                val tagMatch = match.groups[2] ?: error("Injector: Tag match null for some reason")

                registrar.addPlace(
                    null,
                    null,
                    context as PsiLanguageInjectionHost,
                    TextRange(tagMatch.range.first, tagMatch.range.last + 1).shiftRight(PREFIX.length)
                )
            }
            registrar.doneInjecting()
        }
    }

    override fun elementsToInjectIn(): @Unmodifiable List<Class<out PsiElement?>?> {
        return listOf(PsiComment::class.java)
    }

    companion object {
        const val PREFIX = "## TaggedList:"
    }
}