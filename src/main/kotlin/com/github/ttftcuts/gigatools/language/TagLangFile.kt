package com.github.ttftcuts.gigatools.language

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class TagLangFile(viewProvider: FileViewProvider): PsiFileBase(viewProvider, TagLanguage) {
    override fun getFileType(): FileType = TagLangFileType

    override fun toString(): String = "GigaTools Tag File"
}