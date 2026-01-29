package com.github.ttftcuts.gigatools.main.data

import com.github.ttftcuts.gigatools.main.definitions.PropertyCompanion
import com.github.ttftcuts.gigatools.main.definitions.properties.MegaFamilyProperty
import com.github.ttftcuts.gigatools.main.definitions.properties.TagProperty

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
    val DefinitionPropertyTypes: Set<PropertyCompanion> = setOf(
        TagProperty,
        MegaFamilyProperty,
    )

    object Property {
        const val PREFIX: String = "## "
        const val COLON: String = ": "
    }
}