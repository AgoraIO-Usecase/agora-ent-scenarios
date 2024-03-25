package io.agora.scene.base.api.apiutils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object GsonUtils {
    class StringConverter : JsonSerializer<String>, JsonDeserializer<String> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): String {
            return json.asJsonPrimitive.asString
        }

        override fun serialize(
            src: String?,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement =
            if (src == null || src == "null") JsonPrimitive("") else JsonPrimitive(src.toString())
    }

    @JvmStatic
    val gson: Gson = Gson()

    fun covertToString(obj: Any): String {
        return gson.toJson(obj)
    }

    fun covertToString(obj: Any, typeOfSrc: Type): JsonElement {
        return gson.toJsonTree(obj, typeOfSrc)
    }

    fun covertToMap(obj: Any): Map<String, String> {
        return gson.fromJson(gson.toJson(obj), object : TypeToken<HashMap<String, String>>() {}.type)
    }
}
