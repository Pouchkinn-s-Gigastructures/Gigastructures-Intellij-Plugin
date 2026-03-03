package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.definitions.Definition
import icu.windea.pls.script.psi.ParadoxDefinitionElement

class StrategicResource(def: ParadoxDefinitionElement) : Definition(def) {

    companion object : WrapperCompanion<StrategicResource>("resource", ::StrategicResource)
}