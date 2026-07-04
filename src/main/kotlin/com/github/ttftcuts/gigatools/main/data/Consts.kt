package com.github.ttftcuts.gigatools.main.data

import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.PropertyCompanion
import com.github.ttftcuts.gigatools.main.definitions.properties.MegaFamilyProperty
import com.github.ttftcuts.gigatools.main.definitions.properties.TagProperty
import com.github.ttftcuts.gigatools.main.definitions.properties.TaggedListGeneratorProperty
import com.github.ttftcuts.gigatools.main.wrappers.EconomicCategory
import com.github.ttftcuts.gigatools.main.wrappers.Megastructure
import com.github.ttftcuts.gigatools.main.wrappers.ScriptedEffect
import com.github.ttftcuts.gigatools.main.wrappers.ScriptedTrigger
import com.github.ttftcuts.gigatools.main.wrappers.StrategicResource
import com.github.ttftcuts.gigatools.main.wrappers.WrapperCompanion

enum class EcoModifierType(name: String) {
    ADD("add"),
    MULT("mult")
}

enum class EcoModifierDomain(name: String) {
    COST("cost"),
    PRODUCES("produces"),
    UPKEEP("upkeep")
}

object Consts {
    val DefinitionTypes: Map<String, WrapperCompanion<*>> = setOf(
        EconomicCategory,
        Megastructure,
        ScriptedEffect,
        ScriptedTrigger,
        StrategicResource,
    ).associateBy { e -> e.typeExpression }

    val DefinitionPropertyTypes: Set<PropertyCompanion> = setOf(
        TagProperty,
        MegaFamilyProperty,
        TaggedListGeneratorProperty,
    )

    object Property {
        const val PREFIX: String = "## "
        const val COLON: String = ": "
    }
}