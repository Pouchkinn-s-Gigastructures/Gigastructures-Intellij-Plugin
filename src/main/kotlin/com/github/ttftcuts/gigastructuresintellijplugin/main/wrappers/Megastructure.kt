package com.github.ttftcuts.gigastructuresintellijplugin.main.wrappers

import com.github.ttftcuts.gigastructuresintellijplugin.main.tagging.TaggedDefinition
import com.github.ttftcuts.gigastructuresintellijplugin.main.util.GigaPsiUtils.findPropertyAndInline
import com.intellij.openapi.project.Project
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class Megastructure(def: ParadoxScriptDefinitionElement) : TaggedDefinition(def) {
    val upgradeFrom : Set<Megastructure> by lazy {
        val upgradeData = def.findPropertyAndInline("upgrade_from") ?: return@lazy setOf()
        //val upgradeElement = def.findProperty("upgrade_from", inline = true) ?: return@lazy setOf()
        val resolver = upgradeData.second ?: { e: String -> e }
        val upgradeElement = upgradeData.first ?: return@lazy setOf()
        val upgradeBlock = upgradeElement.propertyValue
        if (upgradeBlock !is ParadoxScriptBlockElement) { return@lazy setOf() }

        upgradeBlock.valueList.mapNotNull { v -> println("in ${def.name}: $v, ${v.javaClass}"); resolve(def.project, resolver(v.value)) }.toSet()
    }

    val upgradeTo : Set<Megastructure> by lazy {
        resolveAll(def.project)
        cache.values.filterNotNull().filter { e -> (e != this) && e.upgradeFrom.contains(this) }.toSet()
    }

    companion object {
        private var resolvedAll = false
        val cache : MutableMap<String,Megastructure?> = mutableMapOf()
        fun clearCache() {
            cache.clear()
            resolvedAll = false
        }

        fun resolve(project: Project, id: String) : Megastructure? {
            if (cache.containsKey(id)) { return cache[id] }
            val found = ParadoxDefinitionSearch.search(id, "megastructure", selector(project, project.projectFile).definition().distinctByName()).find()
            val mega = if (found != null) Megastructure(found) else null
            cache[id] = mega
            return mega
        }

        fun resolveAll(project: Project) {
            if (resolvedAll) { return }
            resolvedAll = true
            val found = ParadoxDefinitionSearch.search(null, "megastructure", selector(project, project.projectFile).definition().distinctByName()).findAll()
            cache.putAll(found.filter { e -> !cache.keys.contains(e.name) }.associate { e -> e.name to Megastructure(e) })
        }
    }
}