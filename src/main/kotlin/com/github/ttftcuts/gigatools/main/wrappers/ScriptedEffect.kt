package com.github.ttftcuts.gigatools.main.wrappers

import com.github.ttftcuts.gigatools.main.definitions.Definition
import com.github.ttftcuts.gigatools.main.definitions.properties.ITaggedListGeneratorProperty
import com.github.ttftcuts.gigatools.main.definitions.properties.TaggedListGeneratorProperty
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class ScriptedEffect(override val def: ParadoxScriptDefinitionElement): Definition(def), ITaggedListGeneratorProperty<ScriptedEffect> by TaggedListGeneratorProperty(def,
    Companion
) {

    companion object: WrapperCompanion<ScriptedEffect>("scripted_effect", ::ScriptedEffect)
}