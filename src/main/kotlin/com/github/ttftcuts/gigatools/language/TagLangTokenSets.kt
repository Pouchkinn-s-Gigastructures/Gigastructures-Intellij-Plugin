package com.github.ttftcuts.gigatools.language

import com.github.ttftcuts.gigatools.language.psi.TagLangTypes
import com.intellij.psi.tree.TokenSet

object TagLangTokenSets {
    val IDENTIFIERS = TokenSet.create(TagLangTypes.TAG)
    val COMMENTS = TokenSet.create()
}