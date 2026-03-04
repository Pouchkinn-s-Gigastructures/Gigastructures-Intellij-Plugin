package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.DefinitionHolder

class StrategicResource(def: DefinitionHolder) : Definition(def) {

    companion object : WrapperCompanion<StrategicResource>("resource", ::StrategicResource)
}