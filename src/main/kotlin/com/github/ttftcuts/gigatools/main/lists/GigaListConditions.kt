package com.github.ttftcuts.gigatools.main.lists

import com.github.ttftcuts.gigatools.main.definitions.properties.DefinitionTag
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.*

object GigaListConditions {

    // does this definition have EVERY listed tag
    fun hasDefinitionTags(element : ParadoxDefinitionElement, vararg tagsToCheck : String) : Boolean {
        val tags = DefinitionTag.getTagNames(element)
        return tags?.containsAll(tagsToCheck.toList()) ?: false
    }

    // does this definition have ANY listed tag
    fun hasAnyDefinitionTags(element : ParadoxDefinitionElement, vararg tagsToCheck : String) : Boolean {
        val tags = DefinitionTag.getTagNames(element) ?: return false
        for(tag in tags) {
            if (tagsToCheck.contains(tag)) {
                return true
            }
        }
        return false
    }

    // cache variables for hasEcoCategory
    var cachedEcoCategory : ParadoxDefinitionElement? = null
    var ecoCategoryCheckCache : MutableMap<ParadoxDefinitionElement, Boolean>? = null
    // check if a definition has an eco category or one of its children
    fun hasEcoCategory(element : ParadoxDefinitionElement, categoryToCheck : ParadoxDefinitionElement) : Boolean {
        val resources = element.properties(inline = true).find { p->p.name == "resources" }// .findProperty("resources", inline = true)
        if (resources == null) {
            //builder.appendLine("# ${mega.name}: no resource block")
            return false
        }

        val elementCategoryName = resources.properties(inline = true).find { p->p.name=="category" }?.value //findProperty("category", inline = true)?.value
        if (elementCategoryName == null) {
            //builder.appendLine("# ${mega.name}: no category given")
            return false
        }
        val category = ParadoxDefinitionSearch.search(elementCategoryName, "economic_category", selector(element.project, element.project.projectFile).definition().distinctByName()).find()?.element
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
    fun hasEcoCategoryByName(element : ParadoxDefinitionElement, categoryToCheck: String) : Boolean {
        val wantedCategory = ParadoxDefinitionSearch.search(categoryToCheck,"economic_category", selector(element.project, element.project.projectFile).definition().distinctByName()).find()?.element
        if (wantedCategory == null) {
            //show("Failed to find economic category: $categoryName")
            return false
        }
        return hasEcoCategory(element, wantedCategory)
    }

    // checks if a given economic category is the same as, or a descendant of, another
    fun checkEcoCategoryWithLineage(categoryToCheck: ParadoxDefinitionElement, categoryToMatch: ParadoxDefinitionElement, map: MutableMap<ParadoxDefinitionElement, Boolean>) : Boolean {
        if (map.containsKey(categoryToCheck)) {
            return map[categoryToCheck]!!
        }

        if (categoryToCheck == categoryToMatch) {
            map[categoryToCheck] = true
            return true
        }

        val parent = categoryToCheck.properties().find { p->p.name=="parent" } //.findProperty("parent")
        if (parent == null || parent.value == null) {
            map[categoryToCheck] = false
            return false
        }

        val selector = selector(categoryToCheck.project, categoryToCheck.context).definition().distinctByName()
        val parentCategory = ParadoxDefinitionSearch.search(parent.value!!,"economic_category", selector).find()?.element

        if (parentCategory != null) { return checkEcoCategoryWithLineage(parentCategory, categoryToMatch, map) }

        return false
    }
}