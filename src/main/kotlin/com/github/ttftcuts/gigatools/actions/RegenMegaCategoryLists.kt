package com.github.ttftcuts.gigatools.actions

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.util.EditorUtils.showMessage
import com.github.ttftcuts.gigatools.main.wrappers.EconomicCategory
import com.github.ttftcuts.gigatools.main.wrappers.Megastructure
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAwareAction

class RegenMegaCategoryLists : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = (project != null) && ToolData.isLoaded
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        //val results = GigaPsiUtils.findCommentsWithPrefix(project, PREFIX)
        //println(results)

        //val test = Megastructure.resolve(project, "matrioshka_brain_2_g_star")!!
        //val results = test.def.findPropertyAndInline("upgrade_from", inline = true)
        //println("RESULT:")
        //println(results)
        //println(test.upgradeFrom)

        //val results = test.def.findPropertyTest("upgrade_from", inline = true)

        WriteCommandAction.writeCommandAction(project).withName(NAME).run<Throwable> {

            // reset cache
            EconomicCategory.clearCache()
            Megastructure.regenerateCache(project)
            Megastructure.deriveAllTags(project)

//            val nexus = Megastructure.get("think_tank_0")
//            println(nexus?.name)
//            println(nexus?.economicCategory)
//            println(nexus?.tags)
//            println(nexus?.derivedTags)

            println(Megastructure.allFamilies.keys.map { mega -> "${mega.name} -> ${mega.getFamilyName()}" })
            println(Megastructure.allFamilies)
            val familyMap: MutableMap<String, Megastructure> = mutableMapOf()
            
            for (mega in Megastructure.allFamilies.keys) {
                val name = mega.getFamilyName() ?: continue
                if (familyMap.contains(name)) { println("Warning: ${mega.name} maps to family name $name already occupied by ${familyMap[name]}") }
                familyMap[name] = mega
            }


//            val trigger = TaggedDefinition.resolve(project, "scripted_trigger", "another_test_trigger")
//
//            val builder = StringBuilder()
//            val firstStages = Megastructure.cache.values.filterNotNull().filter { e ->
//                e.upgradeFrom.isEmpty() && // must be a first stage
//                        e.upgradeTo.isNotEmpty() && // which upgrades to something else (misses the inlined megas, pending potential fix?)
//                        !e.hasAnyTags("technical", "ruined") // ruins don't count, technical aren't proper megas
//                //true
//            }
//
//            for(mega in firstStages) {
//                builder.appendLine("# ${PsiUtils.getElementName(mega.def)}")
//                builder.appendLine("or = {")
//                //builder.appendLine("is_megastructure_type = ${mega.def.name} # ${GigaPsiUtils.getElementName(mega.def)}")
//                //builder.appendLine("# Tags: ${mega.tags.keys}")
//                //builder.appendLine("# Upgrades from: ${mega.upgradeFrom.size}")
//                //builder.appendLine("# Upgrades to: ${mega.upgradeTo.size}")
//
//                val toWriteSet = mutableSetOf(mega)
//                val written : MutableSet<Megastructure> = mutableSetOf()
//                while (toWriteSet.isNotEmpty()) {
//                    val toWrite = toWriteSet.first()
//                    toWriteSet.remove(toWrite)
//
//                    // catch loops
//                    if (written.contains(toWrite)) { continue }
//                    written.add(toWrite)
//
//                    builder.appendLine("is_megastructure_type = ${toWrite.def.name} # ${PsiUtils.getElementName(toWrite.def)}")
//
//                    if (!toWrite.hasTags("force_final")) {
//                        toWriteSet.addAll(toWrite.upgradeTo.filter { e -> !e.hasAnyTags("technical") })
//                    }
//                }
//
//                builder.appendLine("}")
//                builder.appendLine()
//            }
//
//            ListBuilders.replaceBlockContents(project, trigger!!.def.block!!, builder.toString())

//            // test triggers for now
//            ListBuilders.buildMegaCategoryList(project, "plugin_test_kilos_trigger") { def ->
//                GigaListConditions.hasEcoCategoryByName(def, "giga_kilostructures")
//                        || GigaListConditions.hasDefinitionTags(def, "force_kilo")
//            }
//            ListBuilders.buildMegaCategoryList(project, "plugin_test_gigas_trigger") { def ->
//                GigaListConditions.hasEcoCategoryByName(def, "giga_gigastructures")
//                        || GigaListConditions.hasDefinitionTags(def, "force_giga")
//            }
//
//            ListBuilders.buildMegaCategoryList(project, "plugin_test_ruined_trigger") { def -> GigaListConditions.hasDefinitionTags(def,"ruined") }
//            ListBuilders.buildMegaCategoryList(project, "plugin_test_restored_trigger") { def -> GigaListConditions.hasDefinitionTags(def,"restored") }
//            ListBuilders.buildMegaCategoryList(project, "plugin_test_technical_trigger") { def -> GigaListConditions.hasDefinitionTags(def,"technical") }
//            ListBuilders.buildMegaCategoryList(project, "plugin_test_megaproject_trigger") { def -> GigaListConditions.hasDefinitionTags(def,"megaproject") }
        }

        showMessage("Trigger Rebuild Complete")
    }

    companion object {
        const val PREFIX = "## Auto List"

        const val NAME = "Rebuild Kilo/Giga Category Triggers"
    }
}