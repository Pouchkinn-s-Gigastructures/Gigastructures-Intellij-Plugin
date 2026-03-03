package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.data.EcoModifierDomain
import com.github.ttftcuts.gigatools.main.data.EcoModifierType
import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.util.PsiUtils.findPropertyAndInline
import icu.windea.pls.script.psi.ParadoxDefinitionElement

class EconomicCategory(inDef: ParadoxDefinitionElement) : Definition(inDef) {
    val parent: EconomicCategory? by lazy {
        var parentData = def.findPropertyAndInline("parent") ?: return@lazy null
        if (parentData.element.value == null) return@lazy null
        return@lazy resolve(def.project, parentData.resolver(parentData.element.value!!))
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
        val property = def.findPropertyAndInline("generate_${type.name}_modifiers") ?: return false
        // if it's null somehow, false
        val block = property.element.block ?: return false
        // find the modifier type inside it, if missing false
        block.findPropertyAndInline(domain.name) ?: return false
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