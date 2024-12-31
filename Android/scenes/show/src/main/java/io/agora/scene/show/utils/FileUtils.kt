/*
 * MIT License
 *
 * Copyright (c) 2023 Agora Community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.agora.scene.show.utils

import android.content.Context
import android.util.Log
import java.io.*

object FileUtils {
    val TAG = "FileUtils"

    @JvmStatic
    fun getAssetsString(context: Context, path: String): String {
        val sb = StringBuilder()
        var isr: InputStreamReader? = null
        var br: BufferedReader? = null
        // Read license file content
        try {
            isr = InputStreamReader(context.resources.assets.open(path))
            br = BufferedReader(isr)
            var line: String? = null
            while (br.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
        } catch (e: IOException) {
            Log.e(TAG, "getAssetsString error: $e")
        } finally {
            if (isr != null) {
                try {
                    isr.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return sb.toString()
    }

    @JvmStatic
    fun copyAssets(context: Context, assetsPath: String, targetPath: String) {
        // Get all files and folders under the assets directory assetDir
        val fileNames = context.resources.assets.list(assetsPath)
        // If it's a folder (directory), continue recursive traversal
        if (fileNames?.isNotEmpty() == true) {
            val targetFile = File(targetPath)
            if (!targetFile.exists() && !targetFile.mkdirs()) {
                return
            }
            for (fileName in fileNames) {
                copyAssets(
                    context,
                    "$assetsPath/$fileName",
                    "$targetPath/$fileName"
                )
            }
        } else {
            copyAssetsFile(context, assetsPath, targetPath)
        }
    }

    private fun copyAssetsFile(context: Context, assetsFile: String, targetPath: String) {
        val dest = File(targetPath)
        dest.parentFile?.mkdirs()
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            input = BufferedInputStream(context.assets.open(assetsFile))
            output = BufferedOutputStream(FileOutputStream(dest))
            val buffer = ByteArray(1024)
            var length = 0
            while (input.read(buffer).also { length = it } != -1) {
                output.write(buffer, 0, length)
            }
        } catch (e: Exception) {
            Log.e(TAG, "copyAssetsFile", e)
        } finally {
            output?.close()
            input?.close()
        }
    }
}