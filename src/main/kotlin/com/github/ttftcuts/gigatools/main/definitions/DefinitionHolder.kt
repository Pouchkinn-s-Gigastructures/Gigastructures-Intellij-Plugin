package com.github.ttftcuts.gigatools.main.definitions

import com.github.ttftcuts.gigatools.main.util.PsiUtils.isVanilla
import icu.windea.pls.core.collections.process
import icu.windea.pls.ep.resolve.ParadoxInlineScriptInlineSupport
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptProperty

interface HasDefinitionElement {
    val def: DefinitionHolder
}

class DefinitionHolder(val base: ParadoxDefinitionElement) {
    val project get() = base.project
    val name get() = base.name
    val isVanilla get() = base.isVanilla()
    val definitionType get() = Definition.getDefinitionType(base)

    private val resolve by lazy {
        val parameterStack = mutableListOf<MutableMap<String,MutableList<String>>>()
        var parameterFile = base.containingFile.fileInfo?.path

        val builder = StringBuilder()
        builder.appendLine("${base.name} = {")

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
        base.properties(inline = true).process {
            // if the current file isn't the parameter file, pop the stack
            if (it.fileInfo != null && it.fileInfo!!.path != parameterFile) {
                parameterFile = it.fileInfo!!.path
                parameterStack.removeLast()
            }
            // if the element appears to be an inline script
            if (it.fileInfo != null) {
                if (it.name.equals("inline_script", true)) {
                    // get the element for it to get the file name
                    val inlineElement = ParadoxInlineScriptInlineSupport().getInlinedElement(it)

                    if (inlineElement != null && inlineElement.containingFile.fileInfo != null) {
                        val inlineFilePath = inlineElement.containingFile.fileInfo!!.path
                        // holder for the data
                        val data = mutableMapOf<String, MutableList<String>>()

                        // if it's a block, fill out the data
                        if (it.propertyValue is ParadoxScriptBlockElement) {
                            // get the block
                            val inlineBlock: ParadoxScriptBlockElement = it.propertyValue as ParadoxScriptBlockElement
                            //println(inlineBlock.propertyList)

                            //val replaceParameters = parameterStack.last()
                            for (property in inlineBlock.properties()) {
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
                } else {
                    builder.appendLine(it.text)
                }
            }
            // continue walking the tree
            true
        }
        builder.appendLine("}")

        // build resolved definition
        val resolvedText = doReplacement(builder.toString())
        val resolvedDef = ParadoxScriptElementFactory.createFileFromText(base.project, resolvedText).firstChild.firstChild as ParadoxDefinitionElement

        // aaaand return both values
        doReplacement to resolvedDef
    }

    val inlineResolver: (String) -> String get() = resolve.first
    val inlined: ParadoxDefinitionElement get() = resolve.second

    fun findProperty(property: String, fromBase: Boolean = false): ParadoxScriptProperty? {
        return (if(fromBase) { base.properties(inline = true) } else { inlined.properties() }).find { p -> p.name == property }
    }

    fun getAttachedProperties() = Definition.getAttachedProperties(base)
}