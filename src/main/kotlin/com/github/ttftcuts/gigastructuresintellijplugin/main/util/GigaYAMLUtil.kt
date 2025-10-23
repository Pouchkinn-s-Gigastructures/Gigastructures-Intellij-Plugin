package com.github.ttftcuts.gigastructuresintellijplugin.main.util

import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLPsiElement
import org.jetbrains.yaml.psi.YAMLScalar

object GigaYAMLUtil {
    inline fun <reified T> YAMLKeyValue.getValueAndCast() : T {
        if (value !is T) { error("Type Mismatch: value of $key is not a ${T::class}: $value (${value?.javaClass})") }
        return value as T
    }

    inline fun <reified T> YAMLMapping.getValueAndCast(key: String) : T {
        val pair = this.getKeyValueByKey(key)
        val value = pair?.value
        if (value !is T) { error("Type Mismatch: value of $key is not a ${T::class}: $value (${value?.javaClass})") }
        return value
    }

    fun YAMLPsiElement.asText() : String {
        //println("asText: $this, ${this.javaClass}")
        if (this is YAMLKeyValue) { return this.value?.asText() ?: error("Pair has no value") }
        if (this is YAMLScalar) { return this.textValue }
        return this.name ?: error("Bad conversion to string")
    }
}