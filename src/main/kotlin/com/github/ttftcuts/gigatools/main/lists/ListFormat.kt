package com.github.ttftcuts.gigatools.main.lists

import com.github.ttftcuts.gigatools.main.util.YAMLUtils.asText
import com.github.ttftcuts.gigatools.main.util.YAMLUtils.getValueAndCast
import io.ktor.http.escapeIfNeeded
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

class ListFormat(val name: String, val entry: String, val prefix: String?, val suffix: String?) {

    override fun toString(): String {
        return "ListFormat( \"${name.escapeIfNeeded()}\" | \"${entry.escapeIfNeeded()}\" | \"${prefix?.escapeIfNeeded()}\" | \"${suffix?.escapeIfNeeded()}\" )"
    }

    companion object {
        fun fromYAMLKeyValue(tagPair: YAMLKeyValue) : ListFormat {
            val name = tagPair.keyText
            val def: YAMLMapping = tagPair.getValueAndCast()

            val entry: String = def.getKeyValueByKey("entry")?.asText() ?: error("ListFormat $name missing entry field")
            val prefix: String? = def.getKeyValueByKey("prefix")?.asText()
            val suffix: String? = def.getKeyValueByKey("suffix")?.asText()

            return ListFormat(name, entry, prefix, suffix)
        }
    }
}