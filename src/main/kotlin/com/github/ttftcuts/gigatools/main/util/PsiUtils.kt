package com.github.ttftcuts.gigatools.main.util

import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.search.PsiSearchHelper
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.findChild
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.ep.resolve.ParadoxInlineSupport
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.dataFlow.*
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.*

object PsiUtils {

    fun nextNonWhiteSpaceSibling(element: PsiElement): PsiElement? {
        var nextElement: PsiElement? = element.nextSibling ?: element.parent.nextSibling

        while (nextElement != null) {
            if (nextElement is ParadoxScriptRootBlock) {
                nextElement = nextElement.firstChild
            } else if (nextElement !is PsiWhiteSpace) {
                return nextElement
            } else {
                nextElement = nextElement.nextSibling
            }
        }
        return null
    }

    fun prevNonWhiteSpaceSibling(element: PsiElement): PsiElement? {
        var prevElement: PsiElement? = element.prevSibling ?: element.parent.prevSibling

        while (prevElement != null) {
            if (prevElement !is PsiWhiteSpace) {
                return prevElement
            } else {
                prevElement = prevElement.prevSibling
            }
        }
        return null
    }

    fun nextNonWhiteSpaceSiblingLine(element: PsiElement): PsiElement? {
        var nextElement: PsiElement? = element.nextSibling ?: element.parent.nextSibling

        while (nextElement != null) {
            if (nextElement is ParadoxScriptRootBlock) {
                nextElement = nextElement.firstChild
            } else if (nextElement !is PsiWhiteSpace) {
                return nextElement
            } else if (nextElement.text.count { c -> c == '\n' } == 1){
                nextElement = nextElement.nextSibling
            } else {
                return null
            }
        }
        return null
    }

    fun prevNonWhiteSpaceSiblingLine(element: PsiElement): PsiElement? {
        var prevElement: PsiElement? = element.prevSibling ?: element.parent.prevSibling

        while (prevElement != null) {
            if (prevElement !is PsiWhiteSpace) {
                return prevElement
            } else if (prevElement.text.count { c -> c == '\n' } == 1) {
                prevElement = prevElement.prevSibling
            } else {
                return null
            }
        }
        return null
    }

    fun findAssociatedDefinition(element: PsiElement): ParadoxScriptDefinitionElement? {
        var nextElement: PsiElement? = nextNonWhiteSpaceSiblingLine(element)

        while (nextElement != null) {
            if (Definition.isPropertyComment(nextElement)) {
                nextElement = nextNonWhiteSpaceSiblingLine(nextElement)
            } else if (nextElement is ParadoxScriptDefinitionElement) {
                return nextElement
            } else {
                return null
            }
        }
        return null
    }

    fun getElementName(element: ParadoxScriptDefinitionElement) : String {
        val locale = ParadoxLocaleManager.getLocaleConfig("l_english") // english for standardisation
        val selector = selector(element.project, element).localisation().contextSensitive().preferLocale(locale)
        val loc = ParadoxLocalisationSearch.search(element.name, selector).find() ?: return element.name
        val rendered = ParadoxLocalisationTextRenderer().render(loc).replace("\u200B", "")
        return rendered.ifEmpty { loc.value ?: element.name }
    }

    fun findCommentsWithPrefix(project: Project, prefix: String) : List<PsiComment> {
        // get all comments containing the prefix (slow, so read action)
        val rawResults = runReadAction {
            PsiSearchHelper.getInstance(project).findCommentsContainingIdentifier(prefix, ProjectScope.getProjectScope(project))
        }
        // check that they actually start with it, and cast
        return rawResults.map { e -> if(e !is PsiComment) { error("Not a comment?!") }; e }.filter { e -> e.text.startsWith(prefix) }
    }

    //val parameterSupport by lazy { ParadoxDefinitionParameterSupport() }

    val DEFAULT_RESOLVER = { e: String -> e }
    class ResolvedElement(val element: ParadoxScriptProperty, val resolver: ((String) -> String))

    fun PsiElement.findPropertyAndInline(
        propertyName: String? = null,
        ignoreCase: Boolean = true,
        conditional: Boolean = false,
    ): ResolvedElement? {
        if (language != ParadoxScriptLanguage) return null
        if (propertyName != null && propertyName.isEmpty()) return ResolvedElement(this as ParadoxScriptProperty, DEFAULT_RESOLVER)
        val block = when {
            this is ParadoxScriptDefinitionElement -> this.block
            this is ParadoxScriptBlock -> this
            else -> null
        }
        val parameterStack = mutableListOf<MutableMap<String,MutableList<String>>>()
        var parameterFile = this.containingFile.fileInfo?.path
        var result: ParadoxScriptProperty? = null

        val doReplacement = e@{ input: String ->
            if (parameterStack.isEmpty()) {
                return@e input
            }
            val parameters = parameterStack.last()

            var replaced = input
            for (key in parameters.keys) {
                for (value in parameters[key]!!) {
                    replaced = replaced.replace("$$key$", value)
                }
            }
            return@e replaced
        }

        //println("start process")

        //block?.processProperty(conditional, true) {
        block?.properties()?.options(conditional = conditional, inline = true)?.process {
            //println("visited: ${it.name}, ${it.javaClass}")
            // if the current file isn't the parameter file, pop the stack
            //println("file: ${it.fileInfo?.path} - parameterFile: $parameterFile - name: ${it.name}")
            if (it.fileInfo != null && it.fileInfo!!.path != parameterFile) {
                //println(it.fileInfo!!.path)
                //println(parameterFile)
                parameterFile = it.fileInfo!!.path
                parameterStack.removeLast()
                //println("POP!")
            }
            // if the element appears to be an inline script
            if (it.name.equals("inline_script", true) && it.fileInfo != null) {
                // get the element for it to get the file name
                val inlineElement = ParadoxInlineSupport.getInlinedElement(it)

                if (inlineElement != null && inlineElement.containingFile.fileInfo != null) {
                    val inlineFilePath = inlineElement.containingFile.fileInfo!!.path
                    // holder for the data
                    val data = mutableMapOf<String,MutableList<String>>()

                    // if it's a block, fill out the data
                    if (it.propertyValue is ParadoxScriptBlockElement) {
                        // get the block
                        val inlineBlock: ParadoxScriptBlockElement = it.propertyValue as ParadoxScriptBlockElement
                        //println(inlineBlock.propertyList)

                        //val replaceParameters = parameterStack.last()
                        for (property in inlineBlock.propertyList) {
                            // read in parameters
                            if (property.name != "script") {
                                val name = doReplacement(property.name)
                                val value = doReplacement(property.propertyValue?.text ?: "")

                                data.putIfAbsent(name, mutableListOf())
                                data[name]!!.add(value)
                            }
                        }
                    }
                    // new inline, push onto the stack
                    parameterStack.add(data)
                    parameterFile = inlineFilePath
                    //println("PUSH $parameterFile!")
                }
            }
            if (propertyName == null || propertyName.equals(it.name, ignoreCase)) {
                result = it
                false
            } else {
                true
            }
        }
        if (result == null) { return null }
        if (parameterStack.isEmpty()) { return ResolvedElement(result, DEFAULT_RESOLVER) }
        return ResolvedElement(result, doReplacement)
    }

    fun PsiElement.isVanilla(): Boolean {
        if (this.fileInfo == null) { return false }
        if (this.fileInfo!!.rootInfo is ParadoxRootInfo.Game) { return true }
        return false
    }

    fun ParadoxScriptFile.replaceContents(newContents: String) {
        val replacement = ParadoxScriptElementFactory.createDummyFile(project, newContents).findChild<ParadoxScriptRootBlock>()
        if (replacement != null) {
            this.deleteChildRange(this.children.first(), this.children.last())
            this.add(replacement)
        } else {
            error("Script file contents replace failed")
        }
    }

    fun ParadoxLocalisationFile.replaceContents(newContents: String) {
        val replacement = ParadoxLocalisationElementFactory.createDummyFile(project, newContents).findChild<ParadoxLocalisationPropertyList>()
        if (replacement != null) {
            this.deleteChildRange(this.children.first(), this.children.last())
            this.add(replacement)
        } else {
            error("Loc file contents replace failed")
        }
    }

    inline fun <reified T: PsiFile>resolveFile(project: Project, path: String): T {
        val file = ParadoxFilePathSearch.search(path, null, selector(project, project.projectFile).file()).findFirst() ?: error("PsiUtils.ResolveFile: Unable to find file: $path")
        val psiFile = file.toPsiFile(project) ?: error("PsiUtils.ResolveFile: Unable to find PsiFile: $path")
        val typedFile = psiFile.castOrNull<T>() ?: error("PsiUtils.ResolveFile: File $path is not a the expected type")
        return typedFile
    }
}
