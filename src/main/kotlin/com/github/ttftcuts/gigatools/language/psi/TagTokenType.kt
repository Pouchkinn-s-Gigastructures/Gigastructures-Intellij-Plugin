package com.github.ttftcuts.gigatools.language.psi

import com.github.ttftcuts.gigatools.language.TagLanguage
import com.intellij.psi.tree.IElementType

class TagTokenType(debugName: String): IElementType(debugName, TagLanguage) {

    override fun toString(): String {
        return "TagTokenType.${super.toString()}"
    }
}