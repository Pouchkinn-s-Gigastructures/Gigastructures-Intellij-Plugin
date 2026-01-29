package com.github.ttftcuts.gigatools.main.definitions.properties

import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.DefinitionHolder
import com.github.ttftcuts.gigatools.main.definitions.PropertyCompanion
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class MegaFamilyProperty(override val def: ParadoxScriptDefinitionElement) : DefinitionHolder, IMegaFamilyProperty {
    override var megaFamily: String? = null

    companion object: PropertyCompanion("Family", "Override for megastructure family names. Only needed if the mega is not connected via upgrades and the name does not reduce to the same family name.") {
        override fun validForDefinitionType(type: String): Boolean {
            return type == "megastructure"
        }

        fun getFamilyOverride(def: ParadoxScriptDefinitionElement): String? {
            val data = Definition.getAttachedProperties(def).firstOrNull{ p -> p.type == MegaFamilyProperty }
                ?: Definition.getWholeFileProperties(def).firstOrNull{ p -> p.type == MegaFamilyProperty } ?: return null

            return data.propertyText.splitByBlank().firstOrNull()
        }
    }
}

interface IMegaFamilyProperty : DefinitionHolder {
    var megaFamily: String?
}