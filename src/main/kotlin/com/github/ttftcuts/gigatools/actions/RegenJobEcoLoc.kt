package com.github.ttftcuts.gigatools.actions

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.util.EditorUtils
import com.github.ttftcuts.gigatools.main.util.PsiUtils.isVanilla
import com.github.ttftcuts.gigatools.main.util.TextUtils.appendGeneratedLocFileWarning
import com.github.ttftcuts.gigatools.main.wrappers.EconomicCategory
import com.github.ttftcuts.gigatools.main.wrappers.StrategicResource
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findChild
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.locale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.model.ParadoxLocalisationType

class RegenJobEcoLoc : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = (project != null) && ToolData.isLoaded
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // get the file
        val file = ParadoxFilePathSearch.search(FILE_PATH, null, ParadoxFilePathSearch.selector(project, project.projectFile)).findFirst() ?: return
        val psiFile = file.toPsiFile(project) ?: return
        val locFile = psiFile.castOrNull<ParadoxLocalisationFile>() ?: return

        //println(locFile.firstChild)
        //val list = locFile.firstChild.castOrNull<ParadoxLocalisationPropertyList>() ?: return

        // reload object caches
        EconomicCategory.regenerateCache(project)
        StrategicResource.regenerateCache(project)

        // planet_jobs category
        val planetJobs = EconomicCategory.resolve(project, "planet_jobs") ?: return

        WriteCommandAction.writeCommandAction(project).withName(NAME).run<Throwable> {

            // all categories which are descendants of planet_jobs, plus inclusions from data, minus exclusions from data
            // also exclude any categories which don't generate modifiers
            val categories = EconomicCategory.cache.values.filter { cat ->
                !ToolData.EcoCategoryLoc.excludeCategories.contains(cat.name)
                && (cat == planetJobs || cat.isDescendantOf(planetJobs) || ToolData.EcoCategoryLoc.includeCategories.contains(cat.name))
                && cat.generatesAnyModifiers
            }.toSet()

            // all resources, minus exclusions from data
            val resources = StrategicResource.cache.values
                .filter { res -> !ToolData.EcoCategoryLoc.excludeResources.contains(res.name) }.toSet()

            // get locale, standardised for gigas
            val locale = ParadoxLocaleManager.getLocaleConfig("l_english")

            // stringbuilder for the body of the loc
            val builder = StringBuilder()

            builder.appendLine("l_english:")
            builder.appendGeneratedLocFileWarning()

            fun writeLoc(key: String, value: String, test: Boolean = true) {
                if (!test) {
                    return
                }
                val element = ParadoxLocalisationSearch.search(key, ParadoxLocalisationType.Normal, ParadoxLocalisationSearch.selector(project, project.projectFile)
                    .locale(locale)).find()

                if (element == null || !element.isVanilla()) {
                    builder.appendLine(" $key:0 \"$value\"")
                }
            }

            builder.appendLine("# Resources")
            for (resource in resources) {
                // resource icon, with or without name, depends on whether paradox made a mistake or not
                // turns out they did not :(
                val nameAndIcon = "£${resource.name}£ " // no resource name
                //val nameAndIcon = "£${resource.name}£ §Y$${resource.name}$§! " // with resource name

                // additive lines
                writeLoc(
                    "${resource.name}_produced_per_pop_group_unit",
                    "$nameAndIcon\$produced_per_pop_group_unit$"
                )
                writeLoc(
                    "${resource.name}_upkeep_per_pop_group_unit",
                    "$nameAndIcon\$upkeep_per_pop_group_unit$"
                )

                // mult lines
                writeLoc(
                    "${resource.name}_produced_from",
                    "$nameAndIcon\$produced_from$")
                writeLoc(
                    "${resource.name}_upkeep_for",
                    "$nameAndIcon\$upkeep_for$"
                )
            }

            builder.appendLine().appendLine("# Modifiers")
            for (category in categories) {
                builder.appendLine().appendLine("# ${category.name}")
                val jobloc = "\$giga_${category.name}_mod_suffix$"

                // everything mults
                writeLoc(
                    "mod_${category.name}_produces_mult",
                    "\$giga_resources_produced_from$ $jobloc",
                    category.generatesProducesMult
                )
                writeLoc(
                    "mod_${category.name}_upkeep_mult",
                    "\$giga_upkeep_for$ $jobloc",
                    category.generatesUpkeepMult
                )
                //builder.appendLine()

                for (resource in resources) {
                    // resource add
                    writeLoc(
                        "mod_${category.name}_${resource.name}_produces_add",
                        "\$${resource.name}_produced_per_pop_group_unit$ $jobloc",
                        category.generatesProducesAdd
                    )
                    writeLoc(
                        "mod_${category.name}_${resource.name}_upkeep_add",
                        "\$${resource.name}_upkeep_per_pop_group_unit$ $jobloc",
                        category.generatesUpkeepAdd
                    )

                    // resource mult
                    writeLoc(
                        "mod_${category.name}_${resource.name}_produces_mult",
                        "\$${resource.name}_produced_from$ $jobloc",
                        category.generatesProducesMult
                    )
                    writeLoc(
                        "mod_${category.name}_${resource.name}_upkeep_mult",
                        "\$${resource.name}_upkeep_for$ $jobloc",
                        category.generatesUpkeepMult
                    )
                }
            }

            val replacement = ParadoxLocalisationElementFactory.createFileFromText(project, builder.toString()).findChild<ParadoxLocalisationPropertyList>()
            if (replacement != null) {
                locFile.deleteChildRange(locFile.children.first(), locFile.children.last())
                locFile.add(replacement)
                EditorUtils.showMessage("Eco Modifier Loc Rebuild Complete")
            } else {
                EditorUtils.showMessage("Eco Modifier Loc Rebuild FAILED: unable to build a valid list")
            }
        }
    }

    companion object {
        const val NAME = "Rebuild Economic Modifier Localisation"
        const val FILE_PATH = "localisation/english/giga_economic_modifiers_auto_l_english.yml"
    }
}