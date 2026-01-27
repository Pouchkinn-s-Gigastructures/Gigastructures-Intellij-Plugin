package com.github.ttftcuts.gigatools.main.definitions

import com.github.ttftcuts.gigatools.main.data.Consts
import com.github.ttftcuts.gigatools.main.data.Consts.Property.COLON
import com.github.ttftcuts.gigatools.main.data.Consts.Property.PREFIX
import com.github.ttftcuts.gigatools.main.definitions.properties.ITagProperty
import com.github.ttftcuts.gigatools.main.definitions.properties.TagProperty
import com.github.ttftcuts.gigatools.main.util.PsiUtils
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.findTopmostParentOfType
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptRootBlock

open class Definition(override val def: ParadoxScriptDefinitionElement) : DefinitionHolder, ITagProperty by TagProperty(def) {
    override fun toString(): String {
        return "(${this.javaClass.simpleName}: ${def.name})"
    }

    companion object {


        fun resolve(project: Project, type: String, id: String) : Definition? {
            val found = ParadoxDefinitionSearch.search(id,type, selector(project, project.projectFile).definition().distinctByName()).find() ?: return null
            return Definition(found)
        }

        fun isPropertyComment(element: PsiElement): Boolean {
            return (element is PsiComment) && (element.text.startsWith(PREFIX))
        }

        // if this element is a property comment, get the information about it
        fun getPropertyData(element: PsiElement): PropertyData? {
            // only comments
            if (element !is PsiComment) { return null }
            // only top level elements
            if (element.parent !is ParadoxScriptRootBlock) { return null }

            // value of the comment
            var text = element.text
            // only specially annotated lines
            if (!text.startsWith(PREFIX)) { return null }

            // only if there are script definitions in the file
            if (element.parent.children.isEmpty() || element.parent.children.first() !is ParadoxScriptDefinitionElement) { return null }

            // use the first child to determine what kind of definitions this file "should" have
            val fileDefType = getDefinitionType(element.parent.children.first() as ParadoxScriptDefinitionElement)

            // snip down the text range
            var textOffset = PREFIX.length
            text = text.substring(textOffset)

            // find whether this comment is attached to a definition
            val definition = PsiUtils.findAssociatedDefinition(element)
            // is this a "whole file" property?
            val wholeFile = (definition == null) && (element.textRange.endOffset < element.parent.children.first().textRange.startOffset)
            // if we're not attached and not whole file, bail
            if (definition == null && !wholeFile) { return null }

            // determine property type
            var propertyType: PropertyCompanion? = null
            for (pType in Consts.DefinitionPropertyTypes) {
                if (text.startsWith(pType.prefix)) {
                    propertyType = pType
                    break
                }
            }
            if (propertyType == null) { return null }
            textOffset += propertyType.prefix.length
            text = text.substring(propertyType.prefix.length)

            // check for the colon lol
            if (!text.startsWith(COLON)) { return null }
            textOffset += COLON.length
            text = text.substring(COLON.length)

            // at this point if def ins null wholefile is true
            if (definition != null) {
                val elementType = getDefinitionType(definition)
                return PropertyData(element, propertyType, definition, elementType, false, text, textOffset)
            }
            return PropertyData(element, propertyType, definition, fileDefType, true, text, textOffset)
        }

        // get property comments attached to this element, and get their data
        fun getAttachedProperties(element: PsiElement): List<PropertyData> {
            val list: MutableList<PropertyData> = mutableListOf()

            // iterate upward by line and append found comments to the top of the list, else break
            var e: PsiElement? = element
            while (e != null) {
                val prev = PsiUtils.prevNonWhiteSpaceSiblingLine(e) ?: break
                if (!isPropertyComment(prev)) { break }
                val data = getPropertyData(prev)
                if (data != null) { list.addFirst(data) }
                e = prev
            }
            return list
        }

        // get whole-file property comments from the top of the file, and get their data
        fun getWholeFileProperties(element: PsiElement): List<PropertyData> {
            val list: MutableList<PropertyData> = mutableListOf()

            // find the file's root block, and make sure there's at least one def here
            val root = element.findTopmostParentOfType<ParadoxScriptRootBlock>() ?: return list
            if (root.children.isEmpty()) { return list }

            // iterate backwards from the first definition probing for properties
            var e: PsiElement? = root.children.first()
            while (e != null) {
                val prev = PsiUtils.prevNonWhiteSpaceSibling(e) ?: return list
                val data = getPropertyData(prev)
                if (data?.wholeFile == true) {
                    list.addFirst(data)
                }
                e = prev
            }

            return list
        }

        fun isPropertyDuplicate(element: PsiElement): Boolean {
            val data = getPropertyData(element) ?: return false

            val propertyList: List<PropertyData> = if (data.wholeFile) {
                getWholeFileProperties(data.element)
            } else {
                getAttachedProperties(data.element)
            }
            for (property in propertyList) {
                if (property.element == element) { break }
                else if (property.type == data.type) {
                    return true
                }
            }

            return false
        }

        fun getDefinitionType(definition: ParadoxScriptDefinitionElement): String {
            return definition.definitionInfo?.typeConfig?.name ?: "unknown"
        }
    }
}