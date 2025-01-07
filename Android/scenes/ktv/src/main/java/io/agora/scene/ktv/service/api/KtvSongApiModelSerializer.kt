package io.agora.scene.ktv.service.api

import com.google.gson.*
import java.lang.reflect.Type
import java.net.URLEncoder
import java.net.URLDecoder
import java.io.UnsupportedEncodingException

class KtvSongApiModelSerializer : JsonSerializer<KtvSongApiModel>, JsonDeserializer<KtvSongApiModel> {

    override fun serialize(src: KtvSongApiModel, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("songCode", src.songCode)
        jsonObject.addProperty("name", src.name)
        jsonObject.addProperty("singer", src.singer)
        try {
            val encodedMusic = URLEncoder.encode(src.music, "UTF-8")
            val encodedLyric = URLEncoder.encode(src.lyric, "UTF-8")
            jsonObject.addProperty("music", encodedMusic)
            jsonObject.addProperty("lyric", encodedLyric)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): KtvSongApiModel {
        val jsonObject = json.asJsonObject
        val songCode = jsonObject.get("songCode").asString
        val name = jsonObject.get("name").asString
        val singer = jsonObject.get("singer").asString
        return try {
            val decodedMusic = URLDecoder.decode(jsonObject.get("music").asString, "UTF-8")
            val decodedLyric = URLDecoder.decode(jsonObject.get("lyric").asString, "UTF-8")
            KtvSongApiModel(songCode, name, singer, decodedMusic, decodedLyric)
        } catch (e: UnsupportedEncodingException) {
            throw JsonParseException(e)
        }
    }
}
