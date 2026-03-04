package com.github.ttftcuts.gigatools.actions

import com.github.ttftcuts.gigatools.language.TagLangHelpers.evaluate
import com.github.ttftcuts.gigatools.main.data.Consts
import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.properties.DefinitionTag
import com.github.ttftcuts.gigatools.main.definitions.properties.ITaggedListGeneratorProperty
import com.github.ttftcuts.gigatools.main.lists.ListBuilders
import com.github.ttftcuts.gigatools.main.util.EditorUtils
import com.github.ttftcuts.gigatools.main.util.EditorUtils.showMessage
import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.github.ttftcuts.gigatools.main.util.PsiUtils.findPropertyAndInline
import com.github.ttftcuts.gigatools.main.util.PsiUtils.replaceContents
import com.github.ttftcuts.gigatools.main.util.TextUtils.appendEOFComment
import com.github.ttftcuts.gigatools.main.util.TextUtils.appendGeneratedFileWarning
import com.github.ttftcuts.gigatools.main.util.TextUtils.appendSectionBreak
import com.github.ttftcuts.gigatools.main.wrappers.EconomicCategory
import com.github.ttftcuts.gigatools.main.wrappers.Megastructure
import com.github.ttftcuts.gigatools.main.wrappers.ScriptedEffect
import com.github.ttftcuts.gigatools.main.wrappers.ScriptedTrigger
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import icu.windea.pls.lang.psi.properties
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

            //val testMega = Megastructure.get("dyson_sphere_2_a_star")!!
            //println(testMega.def.findProperty("upgrade_from", fromBase = true)?.text)
            //println(testMega.def.findProperty("upgrade_from")?.text)

            regenMegaFamilies(project)
            regenCategories(project)
            regenTaggedLists(project)
        }

        showMessage("Trigger Rebuild Complete")
    }

    fun regenMegaFamilies(project: Project) {
        val file = PsiUtils.resolveFile<ParadoxScriptFile>(project, FAMILY_FILE_PATH)

        val builder = StringBuilder()
        builder.appendGeneratedFileWarning()

        for (keyVal in Megastructure.allFamilies) {
            val familyName = keyVal.key
            val family = keyVal.value
            builder.appendLine("# $familyName")
            builder.appendLine("giga_mega_is_$familyName = {")

            builder.appendLine("if = {")
            builder.appendLine("limit = {")
            builder.appendLine("has_megastructure_flag = @giga_mega_classified")
            builder.appendLine("}")
            builder.appendLine("has_megastructure_flag = $FAMILY_PREFIX$familyName")
            builder.appendLine("}")

            builder.appendLine("else = {")
            builder.appendLine("or = {")
            for (mega in family) {
                builder.appendLine("is_megastructure_type = ${mega.name} # ${mega.locName}")
            }
            builder.appendLine("}")
            builder.appendLine("}")

            builder.appendLine("}")
            builder.appendLine()
        }
        builder.appendEOFComment()
        file.replaceContents(builder.toString())
    }

    fun regenCategories(project: Project) {
        val file = PsiUtils.resolveFile<ParadoxScriptFile>(project, CLASSIFIER_FILE_PATH)
        val vanillaDefs = mutableListOf<Megastructure>()
        val classifierFlags = mutableSetOf<String>()
        val constructionFlags = mutableSetOf<String>()

        val builder = StringBuilder()
        builder.appendGeneratedFileWarning()

        // megas
        builder.appendLine("giga_classify_mega_switch = {")
        builder.appendLine("switch = {")
        builder.appendLine("trigger = is_megastructure_type")

        for (mega in Megastructure.cache.values.filterNotNull()) {
            val tags = mega.getAllTags().filter { tag -> tag.classify }
            if (mega.megaFamily == null && tags.isEmpty()) { continue }

            builder.appendLine("# ${mega.locName}")
            builder.appendLine("${mega.name} = {")

            if (mega.megaFamily != null) {
                builder.appendLine("set_megastructure_flag = ${familyString(mega)}")
                classifierFlags.add(familyString(mega))
            }

            for (tag in tags) {
                builder.appendLine("set_megastructure_flag = ${tagString(tag)}")
                classifierFlags.add(tagString(tag))
            }

            builder.appendLine("}")

            if (mega.isVanilla()) { vanillaDefs.add(mega) }
        }

        builder.appendLine("default = {")
        builder.appendLine("giga_classify_mega_switch_default = yes")
        builder.appendLine("}")
        builder.appendLine("}")
        builder.appendLine("}")

        // clear tags
        builder.appendLine()
        builder.appendLine("giga_classify_mega_clear = {")
        for (flag in classifierFlags) {
            builder.appendLine("remove_megastructure_flag = $flag")
        }
        builder.appendLine("}")

        // spacer
        builder.appendSectionBreak()

        // construction
        builder.appendLine("giga_classify_mega_construction_switch = {")
        builder.appendLine("switch = {")
        builder.appendLine("trigger = is_constructing")

        for (mega in Megastructure.cache.values.filterNotNull()) {
            val tags = mega.getAllTags().filter { tag -> tag.classify }
            if (mega.megaFamily == null && tags.isEmpty()) { continue }

            // first stages which are buildable only
            if (!(mega.hasTags("buildable", includeDerived = true))) { continue }

            builder.appendLine("# ${mega.locName}")
            builder.appendLine("${mega.name} = {")

            if (mega.megaFamily != null) {
                builder.appendLine("set_fleet_flag = ${familyString(mega)}")
                constructionFlags.add(familyString(mega))
            }

            for (tag in tags) {
                builder.appendLine("set_fleet_flag = ${tagString(tag)}")
                constructionFlags.add(tagString(tag))
            }

            builder.appendLine("}")
        }

        builder.appendLine("default = {")
        builder.appendLine("giga_classify_mega_construction_switch_default = yes")
        builder.appendLine("}")
        builder.appendLine("}")
        builder.appendLine("}")

        // clear construction tags
        builder.appendLine()
        builder.appendLine("giga_classify_mega_construction_clear = {")
        for (flag in constructionFlags) {
            builder.appendLine("remove_fleet_flag = $flag")
        }
        builder.appendLine("}")

        // vanilla defs readout
        if (vanillaDefs.isNotEmpty()) {
            builder.appendLine()
            builder.appendLine("# Vanilla megas not redefined by gigas")
            builder.appendLine()
            for (mega in vanillaDefs) {
                builder.appendLine("# ${mega.name} - ${mega.locName}")
            }
        }

        // eof
        builder.appendLine()
        builder.appendEOFComment()
        file.replaceContents(builder.toString())
    }

    fun regenTaggedLists(project: Project) {
        // get all the triggers and effects with list data
        val tagged = mutableListOf<ITaggedListGeneratorProperty>()
        try {
            tagged.addAll(ScriptedTrigger.cache.values.filter { t -> t.taggedListInfo != null })
            tagged.addAll(ScriptedEffect.cache.values.filter { t -> t.taggedListInfo != null })
        } catch (e: IllegalStateException) {
            showMessage("Tagged List generation failed:\n${e.message}", title = NAME, notificationType = NotificationType.ERROR)
            return
        }

        // go through each one
        for (tList in tagged) {
            // skip malformed blocks
            if (tList.def.inlined.block == null) { continue }
            // we know the list info is non-null due to the filtered lists above
            val info = tList.taggedListInfo!!
            // make sure the list's definition type is available
            info.type.resolveAll(project)
            val allEntries = info.type.cache.values
            // get all entries of the matching type which have matching tags and generate a list with the given template
            val generated = ListBuilders.buildListTextWithFormat(allEntries
                .filter { def -> info.tagEvaluator.evaluate(def) }
                .map { def -> def.def.inlined },
                info.template, info.parameters)
            // apply new block contents
            ListBuilders.replaceBlockContents(project, tList.def.base.block!!, generated)
        }
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