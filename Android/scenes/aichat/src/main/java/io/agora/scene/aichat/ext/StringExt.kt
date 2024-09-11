package io.agora.scene.aichat.ext

import android.text.TextUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.StringReader
import java.io.StringWriter
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

fun String.hasUrlEncoded(): Boolean {
    var encode = false
    for (i in this.indices) {
        val c = this[i]
        if (c == '%' && i + 2 < this.length) {
            // 判断是否符合urlEncode规范
            val c1 = this[i + 1]
            val c2 = this[i + 2]
            if (c1.isValidHexChar() && c2.isValidHexChar()) {
                encode = true
                break
            } else {
                break
            }
        }
    }
    return encode
}

fun Char.isValidHexChar(): Boolean {
    return this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
}

fun String.jsonFormat(): String {
    var json = this
    if (TextUtils.isEmpty(json)) {
        return "Empty/Null json content"
    }
    var message: String
    try {
        json = json.trim { it <= ' ' }
        message = if (json.startsWith("{")) {
            val jsonObject = JSONObject(json)
            jsonObject.toString(4)
        } else if (json.startsWith("[")) {
            val jsonArray = JSONArray(json)
            jsonArray.toString(4)
        } else {
            json
        }
    } catch (e: JSONException) {
        message = json
    } catch (error: OutOfMemoryError) {
        message = "Output omitted because of Object size"
    }
    return message
}

fun String.xmlFormat(): String? {
    val xml = this
    if (TextUtils.isEmpty(xml)) {
        return "Empty/Null xml content"
    }
    val message: String?
    message = try {
        val xmlInput: Source =
            StreamSource(StringReader(xml))
        val xmlOutput =
            StreamResult(StringWriter())
        val transformer =
            TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        transformer.transform(xmlInput, xmlOutput)
        xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">\n")
    } catch (e: TransformerException) {
        xml
    }
    return message
}

fun String.MD5(): String {
    if (this.isEmpty()) {
        return ""
    }
    var hexStr = ""
    try {
        val hash = MessageDigest.getInstance("MD5").digest(toByteArray(charset("utf-8")))
        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            if (b.toInt() and 0xFF < 0x10) {
                hex.append("0")
            }
            hex.append(Integer.toHexString(b.toInt() and 0xFF))
        }
        hexStr = hex.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    }

    return hexStr
}