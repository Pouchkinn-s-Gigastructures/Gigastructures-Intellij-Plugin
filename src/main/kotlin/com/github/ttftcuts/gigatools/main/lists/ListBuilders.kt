package com.github.ttftcuts.gigatools.main.lists

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.util.EditorUtils.showMessage
import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.intellij.openapi.project.Project
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.filterBy
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptElementFactory

object ListBuilders {
    // rewrite the body of a specified scripted trigger with a list of megas which have the matching economic category or a child thereof
    fun buildMegaCategoryList(project: Project, triggerName: String, predicate: (ParadoxDefinitionIndexInfo) -> Boolean ) {
        // find the trigger that we're going to rewrite
        val trigger = ParadoxDefinitionSearch.search(triggerName,"scripted_trigger", ParadoxDefinitionSearch.selector(project, project.projectFile).distinct()).find()?.element
        if (trigger == null) {
            showMessage("Failed to find scripted trigger: $triggerName")
            return
        }

        // get a list of matching megas
        val megas: Iterable<ParadoxDefinitionElement> = ParadoxDefinitionSearch.search(null, "megastructure", ParadoxDefinitionSearch.selector(project, project.projectFile).distinct().filterBy(predicate)).findAll().mapNotNull { d -> d.element }.sortedBy { mega -> mega.name }

        val content = buildListTextWithFormat(megas, ToolData.listFormats["scripted_trigger"]!!, mapOf("trigger" to "\$CONDITION\$"))

        replaceBlockContents(project, trigger.block!!, content)
    }

    fun replaceBlockContents(project: Project, block: ParadoxScriptBlockElement, contents: String) {
        val newBlock = ParadoxScriptElementFactory.createBlockFromText(project, "{\n# ${ToolData.textGeneratedBlock}\n$contents\n}")
        block.replace(newBlock)
    }

    fun buildListTextWithFormat(items: Iterable<ParadoxDefinitionElement>, format: ListFormat, parameters: Map<String,String> = mapOf()) : String {
        val builder = StringBuilder()

        if (format.prefix != null) {
            builder.appendLine(if(parameters.isEmpty()) { format.prefix } else { parameterPattern.replace(format.prefix) { match -> parameters[match.groups[1]!!.value] ?: ""} })
        }

        for(item in items) {
            builder.appendLine(listEntryWithFormat(item, format, parameters))
        }

        if (format.suffix != null) {
            builder.appendLine(if(parameters.isEmpty()) { format.suffix } else { parameterPattern.replace(format.suffix) { match -> parameters[match.groups[1]!!.value] ?: ""} })
        }
        return builder.toString()
    }

    private val namePattern = Regex("£name")
    private val locNamePattern = Regex("£locName")
    private val parameterPattern = Regex("\\$(\\w+)")
    fun listEntryWithFormat(item: ParadoxDefinitionElement, format: ListFormat, parameters: Map<String,String>) : String {
        var string = format.entry

        string = namePattern.replace(string, item.name)

        val locName = lazy { PsiUtils.getElementName(item) }
        string = locNamePattern.replace(string) { _ -> locName.value }

        if (parameters.isNotEmpty()) {
            string = parameterPattern.replace(string) { match -> parameters[match.groups[1]!!.value] ?: ""}
        }

        return string
    }
}