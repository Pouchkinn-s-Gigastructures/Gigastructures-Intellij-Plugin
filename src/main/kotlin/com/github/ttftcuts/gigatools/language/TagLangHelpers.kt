package com.github.ttftcuts.gigatools.language

import com.github.ttftcuts.gigatools.language.psi.TagLangAndExpression
import com.github.ttftcuts.gigatools.language.psi.TagLangExpression
import com.github.ttftcuts.gigatools.language.psi.TagLangOrExpression
import com.github.ttftcuts.gigatools.language.psi.TagLangPrimaryExpression
import com.github.ttftcuts.gigatools.language.psi.TagLangTagExpression
import com.github.ttftcuts.gigatools.language.psi.TagLangUnaryExpression
import com.github.ttftcuts.gigatools.main.data.ToolData
import com.github.ttftcuts.gigatools.main.definitions.Definition

object TagLangHelpers {
    fun TagLangExpression.evaluate(tagged: Definition): Boolean = orExpression.evaluate(tagged)
    fun TagLangOrExpression.evaluate(tagged: Definition): Boolean = andExpressionList.any { e -> e.evaluate(tagged) }
    fun TagLangAndExpression.evaluate(tagged: Definition): Boolean = unaryExpressionList.all { e -> e.evaluate(tagged) }

    fun TagLangUnaryExpression.evaluate(tagged: Definition): Boolean {
        val unary = unaryExpression
        val primary = primaryExpression

        if (unary != null) {
            // not
            return !unary.evaluate(tagged)
        } else if(primary != null) {
            // pass through
            return primary.evaluate(tagged)
        }
        error("TagLang UnaryExpression has both values null")
    }

    fun TagLangPrimaryExpression.evaluate(tagged: Definition): Boolean {
        val tag = tagExpression
        val exp = expression

        if (exp != null) {
            // parentheses
            return exp.evaluate(tagged)
        } else if (tag != null) {
            return tag.evaluate(tagged)
        }
        error("TagLang PrimaryExpression has both values null")
    }

    fun TagLangTagExpression.evaluate(tagged: Definition): Boolean {
        val allTags = ToolData.getTagsForType(tagged.type)
        val tagName = text.trim()
        if (!allTags.contains(tagName)) { return false }
        val tag = allTags[tagName]!!

        //println("${tagged.name} -> checking $tagName: ${tagged.getAllTags().toList()} -> ${tagged.getAllTags().contains(tag)}")

        return tagged.getAllTags().contains(tag)
    }
}