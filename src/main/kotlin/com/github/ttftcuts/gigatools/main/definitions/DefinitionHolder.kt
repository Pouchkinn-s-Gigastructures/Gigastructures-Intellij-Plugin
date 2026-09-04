package com.github.ttftcuts.gigatools.main.definitions

import com.github.ttftcuts.gigatools.main.util.PsiUtils.isVanilla
import icu.windea.pls.core.collections.process
import icu.windea.pls.ep.resolve.ParadoxInlineScriptInlineSupport
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathExpressionEvaluator
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptMember
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

        fun doReplacement(input: String) : String {
            if (parameterStack.isEmpty()) {
                return input
            }
            val parameters = parameterStack.last()
            //println(parameterStack)
            //println(parameters)

            var replaced = input
            for (key in parameters.keys) {
                for (value in parameters[key]!!) {
                    replaced = replaced.replace("$$key$", value)
                }
            }

            // try to resolve inline math, if it fails, that's fine, we can try again later, that just means it's being passed forward
            try {
                replaced = replaced.replace(MATH_PATTERN) {
                    val mathToDo = ParadoxScriptElementFactory.createInlineMathFromText(project, it.groupValues[0])


                    ParadoxInlineMathExpressionEvaluator().evaluate(mathToDo).value.toString()
                }
            } catch (e: Exception) {}

            return replaced
        }

        fun processor(it: ParadoxScriptMember) : Boolean {
            //println("PROCESSING: ${it.name}")

            // if the current file isn't the parameter file, pop the stack
            if (it.fileInfo != null && it.fileInfo!!.path != parameterFile) {
                //println("POP $parameterFile!")
                parameterFile = it.fileInfo!!.path
                parameterStack.removeLast()
            }
            // if the member is a property, check it
            if (it is ParadoxScriptProperty){
                if (it.fileInfo != null) {
                    // if the element appears to be an inline script
                    if (it.name.equals("inline_script", true)) {
                        // get the element for it to get the file name
                        val inlineElement = ParadoxInlineScriptInlineSupport().getInlinedElement(it)

                        if (inlineElement != null && inlineElement.containingFile.fileInfo != null) {
                            val inlineFilePath = inlineElement.containingFile.fileInfo!!.path
                            // holder for the data
                            val data = mutableMapOf<String, MutableList<String>>()

                            // if it's a block, fill out the data
                            if (it.propertyValue is ParadoxScriptBlock) {
                                // get the block
                                val inlineBlock: ParadoxScriptBlock =
                                    it.propertyValue as ParadoxScriptBlock
                                //println(inlineBlock.propertyList)

                                //val replaceParameters = parameterStack.last()
                                for (property in inlineBlock.properties()) {
                                    // read in parameters
                                    if (property.name != "script") {
                                        val name = doReplacement(property.name)
                                        val value = doReplacement(property.propertyValue?.text?.removeSurrounding("\"") ?: "")

                                        data.putIfAbsent(name, mutableListOf())
                                        data[name]!!.add(value)
                                    }
                                }
                            }
                            // new inline, push onto the stack
                            parameterStack.add(data)
                            parameterFile = inlineFilePath
                            //println("PUSH $parameterFile! data = $data")
                        }
                    }
                    // if it isn't an inline script, better check it for being a block
                    else {
                        //println("type: ${it.propertyValue?.javaClass?.simpleName}")
                        // if it's a block, process its members
                        if (it.propertyValue is ParadoxScriptBlock) {
                            builder.appendLine("${it.name} = {")

                            it.members(inline = true).process(::processor)

                            builder.appendLine("}")
                        }
                        // if it's not a block, just pass it through replacement
                        else {
                            builder.appendLine(doReplacement(it.text))
                        }
                    }
                }
            }
            // if the member isn't a property, just run it through the property replacement
            else {
                builder.appendLine(doReplacement(it.text))
            }
            // continue walking the tree
            return true
        }

        //println("start process")
        builder.appendLine("${base.name} = {")

        base.properties(inline = true).process(::processor)

        builder.appendLine("}")
        //println("end process")

        // build resolved definition
        val resolvedText = doReplacement(builder.toString())
        //println(resolvedText)
        val resolvedDef = ParadoxScriptElementFactory.createFileFromText(base.project, resolvedText).firstChild.firstChild as ParadoxDefinitionElement

        // aaaand return both values
        ::doReplacement to resolvedDef
    }

    val inlineResolver: (String) -> String get() = resolve.first
    val inlined: ParadoxDefinitionElement get() = resolve.second

    fun findProperty(property: String, fromBase: Boolean = false): ParadoxScriptProperty? {
        return (if(fromBase) { base.properties(inline = true) } else { inlined.properties() }).find { p -> p.name == property }
    }

    fun getAttachedProperties() = Definition.getAttachedProperties(base)

    companion object {
        private val MATH_PATTERN : Regex by lazy { Regex("@\\[([^]]+)]") }
    }
}