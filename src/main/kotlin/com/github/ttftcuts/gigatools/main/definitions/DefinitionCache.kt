package com.github.ttftcuts.gigatools.main.definitions

import com.intellij.openapi.project.Project
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.distinctByName
import icu.windea.pls.lang.search.selector.selector

class DefinitionCache<T : Definition>(val typeExpression: String, val factory: (DefinitionHolder) -> T ) {
    private var resolvedAll = false
    val cache : MutableMap<String, T?> = mutableMapOf()

    val values get() = cache.values.filterNotNull()

    fun clear() {
        cache.clear()
        resolvedAll = false
    }

    fun get(id: String) : T? {
        if (cache.containsKey(id)) { return cache[id] }
        return null
    }

    fun resolve(project: Project, id: String) : T? {
        if (cache.containsKey(id)) { return cache[id] }
        val found = ParadoxDefinitionSearch.search(id, typeExpression, selector(project, project.projectFile).definition().distinctByName()).find()?.element
        val obj = if (found != null) factory(DefinitionHolder(found)) else null
        cache[id] = obj
        return obj
    }

    fun resolveAll(project: Project) : DefinitionCache<T> {
        if (resolvedAll) { return this }
        resolvedAll = true
        val found = ParadoxDefinitionSearch.search(null, typeExpression, selector(project, project.projectFile).definition().distinctByName()).findAll()
        cache.putAll(found.filter { e -> !cache.keys.contains(e.name) && e.element!=null }.associate { e -> e.name to factory(DefinitionHolder(e.element!!)) })

        return this
    }
}