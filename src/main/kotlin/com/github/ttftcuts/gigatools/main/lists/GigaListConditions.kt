package com.github.ttftcuts.gigatools.main.lists

import com.github.ttftcuts.gigatools.main.definitions.properties.DefinitionTag
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.*

object GigaListConditions {

    // does this definition have EVERY listed tag
    fun hasDefinitionTags(element : ParadoxScriptDefinitionElement, vararg tagsToCheck : String) : Boolean {
        val tags = DefinitionTag.getTagNames(element)
        return tags?.containsAll(tagsToCheck.toList()) ?: false
    }

    // does this definition have ANY listed tag
    fun hasAnyDefinitionTags(element : ParadoxScriptDefinitionElement, vararg tagsToCheck : String) : Boolean {
        val tags = DefinitionTag.getTagNames(element) ?: return false
        for(tag in tags) {
            if (tagsToCheck.contains(tag)) {
                return true
            }
        }
        return false
    }

    // cache variables for hasEcoCategory
    var cachedEcoCategory : ParadoxScriptDefinitionElement? = null
    var ecoCategoryCheckCache : MutableMap<ParadoxScriptDefinitionElement, Boolean>? = null
    // check if a definition has an eco category or one of its children
    fun hasEcoCategory(element : ParadoxScriptDefinitionElement, categoryToCheck : ParadoxScriptDefinitionElement) : Boolean {
        val resources = element.findProperty("resources", inline = true)
        if (resources == null) {
            //builder.appendLine("# ${mega.name}: no resource block")
            return false
        }

        val elementCategoryName = resources.findProperty("category", inline = true)?.value
        if (elementCategoryName == null) {
            //builder.appendLine("# ${mega.name}: no category given")
            return false
        }
        val category = ParadoxDefinitionSearch.search(elementCategoryName, "economic_category", selector(element.project, element.project.projectFile).definition().distinctByName()).find()
        if (category == null) {
            //builder.appendLine("# ${mega.name}: category has no value")
            return false
        }

        if (cachedEcoCategory != categoryToCheck) {
            cachedEcoCategory = categoryToCheck
            ecoCategoryCheckCache = HashMap()
        }

        val matches = checkEcoCategoryWithLineage(category, categoryToCheck, ecoCategoryCheckCache!!)

        return matches
    }
    fun hasEcoCategoryByName(element : ParadoxScriptDefinitionElement, categoryToCheck: String) : Boolean {
        val wantedCategory = ParadoxDefinitionSearch.search(categoryToCheck,"economic_category", selector(element.project, element.project.projectFile).definition().distinctByName()).find()
        if (wantedCategory == null) {
            //show("Failed to find economic category: $categoryName")
            return false
        }
        return hasEcoCategory(element, wantedCategory)
    }

    // checks if a given economic category is the same as, or a descendant of, another
    fun checkEcoCategoryWithLineage(categoryToCheck: ParadoxScriptDefinitionElement, categoryToMatch: ParadoxScriptDefinitionElement, map: MutableMap<ParadoxScriptDefinitionElement, Boolean>) : Boolean {
        if (map.containsKey(categoryToCheck)) {
            return map[categoryToCheck]!!
        }

        if (categoryToCheck == categoryToMatch) {
            map[categoryToCheck] = true
            return true
        }

        val parent = categoryToCheck.findProperty("parent")
        if (parent == null || parent.value == null) {
            map[categoryToCheck] = false
            return false
        }

        val selector = selector(categoryToCheck.project, categoryToCheck.context).definition().distinctByName()
        val parentCategory = ParadoxDefinitionSearch.search(parent.value!!,"economic_category", selector).find()

        if (parentCategory != null) { return checkEcoCategoryWithLineage(parentCategory, categoryToMatch, map) }

        return false
    }
}