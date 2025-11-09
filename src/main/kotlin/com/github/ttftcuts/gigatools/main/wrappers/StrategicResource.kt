package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.tagging.TaggedDefinition
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class StrategicResource(def: ParadoxScriptDefinitionElement) : TaggedDefinition(def) {

    companion object : WrapperCompanion<StrategicResource>("resource", ::StrategicResource)
}