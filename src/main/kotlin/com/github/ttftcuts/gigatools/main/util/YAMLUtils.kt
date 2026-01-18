package com.github.ttftcuts.gigatools.main.util

import org.jetbrains.yaml.psi.YAMLAlias
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLPsiElement
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequenceItem

object YAMLUtils {
    inline fun <reified T> YAMLKeyValue.getValueAndCast() : T {
        if (value !is T) { error("Type Mismatch: value of $key is not a ${T::class}: $value (${value?.javaClass})") }
        return value as T
    }

    inline fun <reified T> YAMLSequenceItem.getValueAndCast() : T {
        if (value !is T) { error("Type Mismatch: value is not a ${T::class}: $value (${value?.javaClass})") }
        return value as T
    }

    inline fun <reified T> YAMLMapping.getValueAndCast(key: String) : T {
        val pair = this.getKeyValueByKey(key)
        val value = pair?.value
        if (value !is T) { error("Type Mismatch: value of $key is not a ${T::class}: $value (${value?.javaClass})") }
        return value
    }

    fun YAMLMapping.resolveKeyValues() : Iterable<YAMLKeyValue> {
        return Iterable {
            // generator function yay
            iterator {
                for (keyValue in keyValues) {
                    val keyText = keyValue.keyText
                    val value = keyValue.value

                    if (value is YAMLAlias) {
                        // the value is an alias, so we need to deal with resolving that
                        val alias: YAMLAlias = value
                        val anchor = alias.reference?.resolve()
                        val marked = anchor?.markedValue

                        if (keyText == "<<") {
                            // if it's an inclusion alias, check type and yield map values
                            if (marked is YAMLMapping) {
                                for (markedKeyVal in marked.keyValues) {
                                    yield(markedKeyVal)
                                }
                            } else {
                                error("Alias with << targets non-map value!")
                            }
                        } else {
                            // if it's a regular alias, I guess we'll have to make a new pair with the marked value
                            error("Still need to implement direct YAML references")
                        }
                    } else {
                        // not an alias, just yield normally
                        yield(keyValue)
                    }
                }
            }
        }
    }

    fun YAMLPsiElement.asText() : String {
        //println("asText: $this, ${this.javaClass}")
        if (this is YAMLKeyValue) { return this.value?.asText() ?: error("Pair has no value") }
        if (this is YAMLScalar) { return this.textValue }
        return this.name ?: error("Bad conversion to string")
    }
}