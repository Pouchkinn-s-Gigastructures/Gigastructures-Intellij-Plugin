package com.github.ttftcuts.gigastructuresintellijplugin.main.util

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.search.PsiSearchHelper
import icu.windea.pls.core.collections.process
import icu.windea.pls.ep.resolve.ParadoxInlineSupport
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.dataFlow.*
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextRenderer
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.*

object GigaPsiUtils {

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

    fun PsiElement.findPropertyAndInline(
        propertyName: String? = null,
        ignoreCase: Boolean = true,
        conditional: Boolean = false,
    ): Pair<ParadoxScriptProperty?,((String) ->String)?>? {
        if (language != ParadoxScriptLanguage) return null
        if (propertyName != null && propertyName.isEmpty()) return this as? ParadoxScriptProperty to null
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

        println("start process")

        //block?.processProperty(conditional, true) {
        block?.properties()?.options(conditional = conditional, inline = true)?.process {
            //println("visited: ${it.name}, ${it.javaClass}")
            // if the current file isn't the parameter file, pop the stack
            println("file: ${it.fileInfo?.path} - parameterFile: $parameterFile - name: ${it.name}")
            if (it.fileInfo != null && it.fileInfo!!.path != parameterFile) {
                println(it.fileInfo!!.path)
                println(parameterFile)
                parameterFile = it.fileInfo!!.path
                parameterStack.removeLast()
                println("POP!")
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
                    println("PUSH $parameterFile!")
                }
            }
            if (propertyName == null || propertyName.equals(it.name, ignoreCase)) {
                result = it
                false
            } else {
                true
            }
        }
        if (parameterStack.isEmpty()) { return result to null }
        return result to doReplacement
    }

//    fun PsiElement.findPropertyTest(
//        propertyName: String? = null,
//        ignoreCase: Boolean = true,
//        conditional: Boolean = false,
//        inline: Boolean = false
//    ): ParadoxScriptProperty? {
//        if (language != ParadoxScriptLanguage) return null
//        if (propertyName != null && propertyName.isEmpty()) return this as? ParadoxScriptProperty
//        val block = when {
//            this is ParadoxScriptDefinitionElement -> this.block
//            this is ParadoxScriptBlock -> this
//            else -> null
//        }
//        var result: ParadoxScriptProperty? = null
//        block?.processProperty(conditional, inline) {
//            if (it.name.equals("inline_script", true)) {
//                val from = ParadoxParameterContextReferenceInfo.From.ContextReference
//                val contextConfig = ParadoxExpressionManager.getConfigs(it).firstOrNull()
//                if (contextConfig != null) {
//                    val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(it, from, contextConfig)
//                    println(contextReferenceInfo)
//
//                    println(contextReferenceInfo?.arguments?.map { e -> "${e.argumentName} = ${e.argumentValueElement?.text}" })
//
//                    //print(contextReferenceInfo.)
//                }
//            }
//
//            if (propertyName == null || propertyName.equals(it.name, ignoreCase)) {
//                result = it
//                false
//            } else {
//                true
//            }
//        }
//        return result
//    }
}
