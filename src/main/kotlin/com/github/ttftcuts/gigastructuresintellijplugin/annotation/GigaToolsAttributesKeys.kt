package com.github.ttftcuts.gigastructuresintellijplugin.annotation

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

object GigaToolsAttributesKeys {
    val PROPERTY_LINE_KEY = createTextAttributesKey("GIGA_TOOLS.PROPERTY_LINE", DefaultLanguageHighlighterColors.STRING)
    val PROPERTY_KEY = createTextAttributesKey("GIGA_TOOLS.PROPERTY", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    val PROPERTY_NAME_KEY = createTextAttributesKey("GIGA_TOOLS.PROPERTY_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
}