package io.agora.scene.joy.utils

import android.content.res.Resources
import android.util.TypedValue
import java.util.Random

val Number.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

val Int.getRandomString
    get() = run {
        val str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val sb = StringBuffer()
        for (i in 0 until this) {
            val number = random.nextInt(62)
            sb.append(str[number])
        }
        sb.toString()
    }