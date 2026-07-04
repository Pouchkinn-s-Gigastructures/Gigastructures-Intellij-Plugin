package com.github.ttftcuts.gigatools.language

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

object TagLangFileType: LanguageFileType(TagLanguage) {
    override fun getName(): @NonNls String {
        return "GigaTools Tag"
    }

    override fun getDescription(): @NlsContexts.Label String {
       return "GigaTools tag syntax file"
    }

    override fun getDefaultExtension(): @NlsSafe String {
        return "gigatag"
    }

    override fun getIcon(): Icon {
        return AllIcons.FileTypes.UiForm
    }
}