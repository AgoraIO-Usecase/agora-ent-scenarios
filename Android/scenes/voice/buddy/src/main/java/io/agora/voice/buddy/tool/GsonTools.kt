package io.agora.voice.buddy.tool

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.lang.Exception
import java.lang.reflect.Type

object GsonTools {
    private val gson =
        GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create()

    @JvmStatic
    fun beanToString(obj: Any?): String? {
        return try {
            gson.toJson(obj)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun <T> toBean(jsonString: String?, clazz: Class<T>): T? {
        return try {
            gson.fromJson(jsonString, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun <T> toBean(jsonString: String?, type: Type): T? {
        return try {
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun <T> toBean(element: JsonElement?, cls: Class<T>?): T? {
        return try {
            gson.fromJson(element, cls)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun <T> toMaps(jsonString: String?): Map<String, T>? {
        return try {
            gson.fromJson(jsonString, object : TypeToken<Map<String, T>>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun <T> toList(gsonString: String?): List<T>? {
        return try {
            gson.fromJson(gsonString, object : TypeToken<List<T>>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun getBoolean(jsonObject: JSONObject?, valueKey: String?, defaultValue: Boolean): Boolean {
        if (jsonObject == null || valueKey == null) return defaultValue
        var value = defaultValue
        if (jsonObject.has(valueKey)) try {
            value = jsonObject.getBoolean(valueKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return value
    }

    @JvmStatic
    fun getInt(jsonObject: JSONObject?, valueKey: String?): Int {
        if (jsonObject == null || valueKey == null) return 0
        var value = 0
        if (jsonObject.has(valueKey)) try {
            value = jsonObject.getInt(valueKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return value
    }

    @JvmStatic
    fun getLong(jsonObject: JSONObject?, valueKey: String?): Long {
        if (jsonObject == null || valueKey == null) return 0L
        var value = 0L
        if (jsonObject.has(valueKey)) try {
            value = jsonObject.getLong(valueKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return value
    }

    @JvmStatic
    fun getString(jsonObject: JSONObject?, valueKey: String?): String {
        if (jsonObject == null || valueKey == null) return ""
        var value = ""
        if (jsonObject.has(valueKey)) try {
            value = jsonObject.getString(valueKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return value
    }
}