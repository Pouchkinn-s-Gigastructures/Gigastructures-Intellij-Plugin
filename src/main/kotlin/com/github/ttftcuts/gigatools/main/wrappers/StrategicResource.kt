package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.definitions.Definition
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class StrategicResource(def: ParadoxScriptDefinitionElement) : Definition(def) {

    companion object : WrapperCompanion<StrategicResource>("resource", ::StrategicResource)
}