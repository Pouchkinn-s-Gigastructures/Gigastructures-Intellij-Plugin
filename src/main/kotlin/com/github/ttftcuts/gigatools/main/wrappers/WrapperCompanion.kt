package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.tagging.DefinitionCache
import com.github.ttftcuts.gigatools.main.tagging.TaggedDefinition
import com.intellij.openapi.project.Project
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

open class WrapperCompanion<T: TaggedDefinition>(val typeExpression: String, val factory: (ParadoxScriptDefinitionElement) -> T) {
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