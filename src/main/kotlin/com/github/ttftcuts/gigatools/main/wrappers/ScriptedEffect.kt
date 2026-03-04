package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.DefinitionHolder
import com.github.ttftcuts.gigatools.main.definitions.properties.ITaggedListGeneratorProperty
import com.github.ttftcuts.gigatools.main.definitions.properties.TaggedListGeneratorProperty

class ScriptedEffect(override val def: DefinitionHolder): Definition(def), ITaggedListGeneratorProperty by TaggedListGeneratorProperty(def) {

    companion object: WrapperCompanion<ScriptedEffect>("scripted_effect", ::ScriptedEffect)
}