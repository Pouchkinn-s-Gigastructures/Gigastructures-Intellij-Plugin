package com.github.ttftcuts.gigatools.language

import com.github.ttftcuts.gigatools.language.psi.TagLangTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType


class TagLangSyntaxHighlighter(): SyntaxHighlighterBase() {



    override fun getHighlightingLexer(): Lexer = TagLangLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey?> {
        if (tokenType == null) { return EMPTY_KEYS }
        return when(tokenType) {
            TagLangTypes.SEPARATOR -> SEPARATOR_KEYS
            TagLangTypes.KEY -> KEY_KEYS
            TagLangTypes.VALUE -> VALUE_KEYS
            TagLangTypes.COMMENT -> COMMENT_KEYS
            TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
            else -> EMPTY_KEYS
        }
    }

    companion object {
        // attribute keys
        val SEPARATOR = createTextAttributesKey("SIMPLE_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val KEY = createTextAttributesKey("SIMPLE_KEY", DefaultLanguageHighlighterColors.KEYWORD)
        val VALUE = createTextAttributesKey("SIMPLE_VALUE", DefaultLanguageHighlighterColors.STRING)
        val COMMENT = createTextAttributesKey("SIMPLE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BAD_CHARACTER = createTextAttributesKey("SIMPLE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        // attribute arrays
        val BAD_CHAR_KEYS = arrayOf<TextAttributesKey>(BAD_CHARACTER)
        val SEPARATOR_KEYS = arrayOf<TextAttributesKey>(SEPARATOR)
        val KEY_KEYS = arrayOf<TextAttributesKey>(KEY)
        val VALUE_KEYS = arrayOf<TextAttributesKey>(VALUE)
        val COMMENT_KEYS = arrayOf<TextAttributesKey>(COMMENT)
        val EMPTY_KEYS = arrayOf<TextAttributesKey>()
    }
}