package com.github.ttftcuts.gigatools.actions

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.util.PsiUtils.isVanilla
import com.github.ttftcuts.gigatools.main.wrappers.EconomicCategory
import com.github.ttftcuts.gigatools.main.wrappers.StrategicResource
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.locale
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxLocaleManager

internal class RegenJobEcoLoc : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // reload object caches
        EconomicCategory.regenerateCache(project)
        StrategicResource.regenerateCache(project)

        // planet_jobs category
        val planetJobs = EconomicCategory.resolve(project,"planet_jobs") ?: return

        println(ToolData.EcoCategoryLoc.excludeResources)

        // all categories which are descendants of planet_jobs, plus inclusions from data, minus exclusions from data
        // also exclude any categories which don't generate modifiers
        val categories = EconomicCategory.cache.values.filterNotNull().filter { cat ->
            !ToolData.EcoCategoryLoc.excludeCategories.contains(cat.name)
            && (cat == planetJobs || cat.isDescendantOf(planetJobs) || ToolData.EcoCategoryLoc.includeCategories.contains(cat.name))
            && cat.generatesAnyModifiers
        }.toSet()

        // all resources, minus exclusions from data
        val resources = StrategicResource.cache.values.filterNotNull().filter { res -> !ToolData.EcoCategoryLoc.excludeResources.contains(res.name) }.toSet()

        // get locale, standardised for gigas
        val locale = ParadoxLocaleManager.getLocaleConfig("l_english")

        // stringbuilder for the body of the loc
        val builder = StringBuilder()

        fun writeLoc(key: String, value: String, test: Boolean = true ) {
            if (!test) { return }
            val element = ParadoxLocalisationSearch.search(key, selector(project, project.projectFile).localisation().locale(locale)).find()

            if (element == null || !element.isVanilla()) {
                builder.appendLine(" $key:0 \"$value\"")
            }
        }

        builder.appendLine("# Resources")
        for(resource in resources) {
            // additive lines
            writeLoc("${resource.name}_produced_per_pop_group_unit", "£${resource.name}£ \$produced_per_pop_group_unit$")
            writeLoc("${resource.name}_upkeep_per_pop_group_unit", "£${resource.name}£ \$upkeep_per_pop_group_unit$")

            // mult lines
            writeLoc("${resource.name}_produced_from", "£${resource.name}£ \$produced_from$")
            writeLoc("${resource.name}_upkeep_for", "£${resource.name}£ \$upkeep_for$")
        }

        builder.appendLine().appendLine("# Modifiers")
        for (category in categories) {
            builder.appendLine().appendLine("# ${category.name}")
            val jobloc = "\$giga_${category.name}_mod_suffix$"

            // everything mults
            writeLoc("mod_${category.name}_produces_mult", "Resources Produced from $jobloc", category.generatesProducesMult)
            writeLoc("mod_${category.name}_upkeep_mult", "Upkeep for $jobloc", category.generatesUpkeepMult)
            //builder.appendLine()

            for(resource in resources) {
                // resource add
                writeLoc("mod_${category.name}_${resource.name}_produces_add", "\$${resource.name}_produced_per_pop_group_unit$ $jobloc", category.generatesProducesAdd)
                writeLoc("mod_${category.name}_${resource.name}_upkeep_add", "\$${resource.name}_upkeep_per_pop_group_unit$ $jobloc", category.generatesUpkeepAdd)

                // resource mult
                writeLoc("mod_${category.name}_${resource.name}_produces_mult", "\$${resource.name}_produced_from$ $jobloc", category.generatesProducesMult)
                writeLoc("mod_${category.name}_${resource.name}_upkeep_mult", "\$${resource.name}_upkeep_for$ $jobloc", category.generatesUpkeepMult)
            }
        }

        println(builder.toString())
    }

    companion object {

    }
}