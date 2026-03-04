package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.definitions.DefinitionCache
import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.DefinitionHolder
import com.intellij.openapi.project.Project

open class WrapperCompanion<T: Definition>(val typeExpression: String, val factory: (DefinitionHolder) -> T) {
    val cache = DefinitionCache<T>(typeExpression, factory)

    fun regenerateCache(project: Project) {
        clearCache()
        resolveAll(project)
    }

    open fun clearCache() { cache.clear() }
    fun get(id: String): T? { return cache.get(id) }
    fun resolve(project: Project, id: String): T? { return cache.resolve(project, id) }
    fun resolveAll(project: Project) { cache.resolveAll(project) }
}