package com.github.ttftcuts.gigatools.language

import com.github.ttftcuts.gigatools.language.psi.TagLangTypes
import com.intellij.psi.tree.TokenSet

object TagLangTokenSets {
    val IDENTIFIERS = TokenSet.create(TagLangTypes.KEY)
    val COMMENTS = TokenSet.create(TagLangTypes.COMMENT)
}