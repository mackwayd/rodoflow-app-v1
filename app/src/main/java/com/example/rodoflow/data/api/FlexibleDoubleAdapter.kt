package com.example.rodoflow.data.api

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/** API envia decimais como string; Gson padrão falha sem este adapter. */
class FlexibleDoubleAdapter : TypeAdapter<Double>() {
    override fun write(out: JsonWriter, value: Double?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): Double {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                0.0
            }
            JsonToken.STRING -> reader.nextString().replace(',', '.').toDoubleOrNull() ?: 0.0
            JsonToken.NUMBER -> reader.nextDouble()
            else -> {
                reader.skipValue()
                0.0
            }
        }
    }
}
