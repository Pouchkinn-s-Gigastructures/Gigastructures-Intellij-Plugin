package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.properties.ITaggedListGeneratorProperty
import com.github.ttftcuts.gigatools.main.definitions.properties.TaggedListGeneratorProperty
import icu.windea.pls.script.psi.ParadoxDefinitionElement

class ScriptedTrigger(override val def: ParadoxDefinitionElement): Definition(def), ITaggedListGeneratorProperty by TaggedListGeneratorProperty(def) {

    companion object: WrapperCompanion<ScriptedTrigger>("scripted_trigger", ::ScriptedTrigger)
}