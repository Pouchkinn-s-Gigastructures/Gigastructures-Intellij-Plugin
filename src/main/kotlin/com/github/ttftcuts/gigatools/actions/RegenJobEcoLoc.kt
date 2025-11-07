package com.github.ttftcuts.gigatools.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.psi.stubs.StubIndexKey
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constraints.ParadoxIndexConstraint

internal class RegenJobEcoLoc : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val locale = ParadoxLocaleManager.getLocaleConfig("l_english")

        var found = ParadoxLocalisationSearch.search(selector(project).localisation().withConstraint(
            ParadoxIndexConstraint.Localisation.Modifier)).findAll()

        println(found)
    }

    companion object {

    }
}