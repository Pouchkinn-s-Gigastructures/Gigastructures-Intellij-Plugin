package com.github.ttftcuts.gigatools.actions

import com.github.ttftcuts.gigatools.main.wrappers.EconomicCategory
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.ParadoxLocaleManager

internal class RegenJobEcoLoc : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val selector = selector(project, project.projectFile).definition().distinctByName()
        val locale = ParadoxLocaleManager.getPreferredLocaleConfig()

        EconomicCategory.clearCache()

        val planet_jobs = EconomicCategory.get("planet_jobs") ?: return
        println(planet_jobs.parent)

        val resources = ParadoxDefinitionSearch.search(null, "resource", selector).findAll()

        println(resources)
        //var found = ParadoxLocalisationSearch.search(selector(project).localisation().withConstraint(ParadoxIndexConstraint.Localisation.Modifier)).findAll()

        //println(found)
    }

    companion object {

    }
}