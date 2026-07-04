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
            TagLangTypes.TAG -> TAG_KEYS
            TagLangTypes.AND_OP -> OPERATOR_KEYS
            TagLangTypes.OR_OP -> OPERATOR_KEYS
            TagLangTypes.NOT_OP -> OPERATOR_KEYS
            TagLangTypes.L_PAREN -> PARENTHESIS_KEYS
            TagLangTypes.R_PAREN -> PARENTHESIS_KEYS
            TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
            else -> EMPTY_KEYS
        }
    }

    companion object {
        // attribute keys
        val OPERATOR = createTextAttributesKey("TAGLANG_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val TAG = createTextAttributesKey("TAGLANG_TAG", DefaultLanguageHighlighterColors.STRING)
        val PARENTHESIS = createTextAttributesKey("TAGLANG_PARENTHESIS", DefaultLanguageHighlighterColors.PARENTHESES)
        val BAD_CHARACTER = createTextAttributesKey("TAGLANG_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        // attribute arrays
        val OPERATOR_KEYS = arrayOf(OPERATOR)
        val TAG_KEYS = arrayOf(TAG)
        val PARENTHESIS_KEYS = arrayOf(PARENTHESIS)
        val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
        val EMPTY_KEYS = arrayOf<TextAttributesKey>()
    }
}