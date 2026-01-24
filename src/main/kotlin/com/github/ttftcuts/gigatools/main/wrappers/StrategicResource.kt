package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.definitions.ModularDefinition
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class StrategicResource(def: ParadoxScriptDefinitionElement) : ModularDefinition(def) {

    companion object : WrapperCompanion<StrategicResource>("resource", ::StrategicResource)
}