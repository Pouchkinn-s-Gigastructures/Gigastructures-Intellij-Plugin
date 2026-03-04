package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.data.EcoModifierDomain
import com.github.ttftcuts.gigatools.main.data.EcoModifierType
import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.DefinitionHolder
import com.github.ttftcuts.gigatools.main.util.PsiUtils.findProperty
import com.github.ttftcuts.gigatools.main.util.PsiUtils.findPropertyAndInline

class EconomicCategory(inDef: DefinitionHolder) : Definition(inDef) {
    val parent: EconomicCategory? by lazy { val prop = def.findProperty("parent") ?: return@lazy null
        if (prop.value == null) return@lazy null
        return@lazy resolve(def.project, prop.value!!)
    }

    val children: Set<EconomicCategory> by lazy {
        resolveAll(def.project)
        cache.values.filter { e -> (e != this) && e.parent == this }.toSet()
    }

    val generatesAnyModifiers: Boolean by lazy {
        return@lazy generatesProducesMult || generatesProducesAdd || generatesUpkeepMult || generatesUpkeepAdd || generatesCostMult || generatesCostAdd
    }

    val generatesCostAdd: Boolean by lazy {
        return@lazy generatesModifiers(EcoModifierDomain.COST, EcoModifierType.ADD)
    }
    val generatesCostMult: Boolean by lazy {
        return@lazy generatesModifiers(EcoModifierDomain.COST, EcoModifierType.MULT)
    }
    val generatesProducesAdd: Boolean by lazy {
        return@lazy generatesModifiers(EcoModifierDomain.PRODUCES, EcoModifierType.ADD)
    }
    val generatesProducesMult: Boolean by lazy {
        return@lazy generatesModifiers(EcoModifierDomain.PRODUCES, EcoModifierType.MULT)
    }
    val generatesUpkeepAdd: Boolean by lazy {
        return@lazy generatesModifiers(EcoModifierDomain.UPKEEP, EcoModifierType.ADD)
    }
    val generatesUpkeepMult: Boolean by lazy {
        return@lazy generatesModifiers(EcoModifierDomain.UPKEEP, EcoModifierType.MULT)
    }

    fun generatesModifiers(domain: EcoModifierDomain, type: EcoModifierType): Boolean {
        // find the modifier block
        val property = def.findProperty("generate_${type.name}_modifiers") ?: return false
        // if it's null somehow, false
        val block = property.block ?: return false
        // find the modifier type inside it, if missing false
        block.findProperty(domain.name) ?: return false
        // if we got this far it means we found it
        return true
    }

    fun isDescendantOf(other: EconomicCategory) : Boolean {
        if (this == other) { return false }
        if (parent == null) { return false }
        if (parent == other) { return true }
        return parent!!.isDescendantOf(other)
    }

    companion object : WrapperCompanion<EconomicCategory>("economic_category", ::EconomicCategory)
}