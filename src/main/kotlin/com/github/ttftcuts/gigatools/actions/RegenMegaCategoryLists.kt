package com.github.ttftcuts.gigatools.actions

import com.github.ttftcuts.gigatools.main.data.Consts
import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.properties.DefinitionTag
import com.github.ttftcuts.gigatools.main.util.EditorUtils.showMessage
import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.github.ttftcuts.gigatools.main.util.PsiUtils.replaceContents
import com.github.ttftcuts.gigatools.main.util.TextUtils.appendEOFComment
import com.github.ttftcuts.gigatools.main.util.TextUtils.appendGeneratedFileWarning
import com.github.ttftcuts.gigatools.main.util.TextUtils.appendSectionBreak
import com.github.ttftcuts.gigatools.main.wrappers.EconomicCategory
import com.github.ttftcuts.gigatools.main.wrappers.Megastructure
import com.github.ttftcuts.gigatools.main.wrappers.ScriptedEffect
import com.github.ttftcuts.gigatools.main.wrappers.ScriptedTrigger
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.script.psi.ParadoxScriptFile

class RegenMegaCategoryLists : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = (project != null) && ToolData.isLoaded
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return



        //val triggerFile = psiFile.castOrNull<ParadoxScriptF>()

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

            ScriptedTrigger.regenerateCache(project)
            ScriptedEffect.regenerateCache(project)

            println("Scripted triggers with tagged list data")
            println(ScriptedTrigger.cache.values.filter { t -> t.taggedListInfo != null })

//            // families
//            run {
//                val file = PsiUtils.resolveFile<ParadoxScriptFile>(project, FAMILY_FILE_PATH)
//
//                val builder = StringBuilder()
//                builder.appendGeneratedFileWarning()
//
//                for (keyVal in Megastructure.allFamilies) {
//                    val familyName = keyVal.key
//                    val family = keyVal.value
//                    builder.appendLine("# $familyName")
//                    builder.appendLine("giga_mega_is_$familyName = {")
//
//                    builder.appendLine("if = {")
//                    builder.appendLine("limit = {")
//                    builder.appendLine("has_megastructure_flag = @giga_mega_classified")
//                    builder.appendLine("}")
//                    builder.appendLine("has_megastructure_flag = $FAMILY_PREFIX$familyName")
//                    builder.appendLine("}")
//
//                    builder.appendLine("else = {")
//                    builder.appendLine("or = {")
//                    for (mega in family) {
//                        builder.appendLine("is_megastructure_type = ${mega.name} # ${mega.locName}")
//                    }
//                    builder.appendLine("}")
//                    builder.appendLine("}")
//
//                    builder.appendLine("}")
//                    builder.appendLine()
//                }
//                builder.appendEOFComment()
//                file.replaceContents(builder.toString())
//            }
//
//            // categorisation
//            run {
//                val file = PsiUtils.resolveFile<ParadoxScriptFile>(project, CLASSIFIER_FILE_PATH)
//                val vanillaDefs = mutableListOf<Megastructure>()
//                val classifierFlags = mutableSetOf<String>()
//                val constructionFlags = mutableSetOf<String>()
//
//                val builder = StringBuilder()
//                builder.appendGeneratedFileWarning()
//
//                // megas
//                builder.appendLine("giga_classify_mega_switch = {")
//                builder.appendLine("switch = {")
//                builder.appendLine("trigger = is_megastructure_type")
//
//                for (mega in Megastructure.cache.values.filterNotNull()) {
//                    val tags = mega.getAllTags().filter { tag -> tag.classify }
//                    if (mega.megaFamily == null && tags.isEmpty()) { continue }
//
//                    builder.appendLine("# ${mega.locName}")
//                    builder.appendLine("${mega.name} = {")
//
//                    if (mega.megaFamily != null) {
//                        builder.appendLine("set_megastructure_flag = ${familyString(mega)}")
//                        classifierFlags.add(familyString(mega))
//                    }
//
//                    for (tag in tags) {
//                        builder.appendLine("set_megastructure_flag = ${tagString(tag)}")
//                        classifierFlags.add(tagString(tag))
//                    }
//
//                    builder.appendLine("}")
//
//                    if (mega.isVanilla()) { vanillaDefs.add(mega) }
//                }
//
//                builder.appendLine("default = {")
//                builder.appendLine("giga_classify_mega_switch_default = yes")
//                builder.appendLine("}")
//                builder.appendLine("}")
//                builder.appendLine("}")
//
//                // clear tags
//                builder.appendLine()
//                builder.appendLine("giga_classify_mega_clear = {")
//                for (flag in classifierFlags) {
//                    builder.appendLine("remove_megastructure_flag = $flag")
//                }
//                builder.appendLine("}")
//
//                // spacer
//                builder.appendSectionBreak()
//
//                // construction
//                builder.appendLine("giga_classify_mega_construction_switch = {")
//                builder.appendLine("switch = {")
//                builder.appendLine("trigger = is_constructing")
//
//                for (mega in Megastructure.cache.values.filterNotNull()) {
//                    val tags = mega.getAllTags().filter { tag -> tag.classify }
//                    if (mega.megaFamily == null && tags.isEmpty()) { continue }
//
//                    // first stages which are buildable only
//                    if (!(mega.hasTags("buildable", includeDerived = true))) { continue }
//
//                    builder.appendLine("# ${mega.locName}")
//                    builder.appendLine("${mega.name} = {")
//
//                    if (mega.megaFamily != null) {
//                        builder.appendLine("set_fleet_flag = ${familyString(mega)}")
//                        constructionFlags.add(familyString(mega))
//                    }
//
//                    for (tag in tags) {
//                        builder.appendLine("set_fleet_flag = ${tagString(tag)}")
//                        constructionFlags.add(tagString(tag))
//                    }
//
//                    builder.appendLine("}")
//                }
//
//                builder.appendLine("default = {")
//                builder.appendLine("giga_classify_mega_construction_switch_default = yes")
//                builder.appendLine("}")
//                builder.appendLine("}")
//                builder.appendLine("}")
//
//                // clear construction tags
//                builder.appendLine()
//                builder.appendLine("giga_classify_mega_construction_clear = {")
//                for (flag in constructionFlags) {
//                    builder.appendLine("remove_fleet_flag = $flag")
//                }
//                builder.appendLine("}")
//
//                // vanilla defs readout
//                if (vanillaDefs.isNotEmpty()) {
//                    builder.appendLine()
//                    builder.appendLine("# Vanilla megas not redefined by gigas")
//                    builder.appendLine()
//                    for (mega in vanillaDefs) {
//                        builder.appendLine("# ${mega.name} - ${mega.locName}")
//                    }
//                }
//
//                // eof
//                builder.appendLine()
//                builder.appendEOFComment()
//                file.replaceContents(builder.toString())
//            }



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

    fun familyString(mega: Megastructure): String {
        return "$FAMILY_PREFIX${mega.megaFamily}"
    }

    fun tagString(tag: DefinitionTag): String {
        return "$TAG_PREFIX${tag.name}"
    }

    companion object {
        const val FAMILY_PREFIX = "giga_family_"
        const val TAG_PREFIX = "giga_tag_"

        const val FAMILY_FILE_PATH = "common/scripted_triggers/giga_mega_families_auto.txt"
        const val CLASSIFIER_FILE_PATH = "common/scripted_effects/giga_mega_classifier_auto.txt"
        const val NAME = "Rebuild Kilo/Giga Category Triggers"
    }
}