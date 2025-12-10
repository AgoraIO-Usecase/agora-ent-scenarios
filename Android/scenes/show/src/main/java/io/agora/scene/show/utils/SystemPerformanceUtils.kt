package io.agora.scene.show.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.Process
import android.text.TextUtils
import io.agora.scene.show.ShowLogger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.RandomAccessFile

/**
 * System performance monitoring utility class
 * Provides methods to get CPU and memory usage (DoKit implementation)
 */
object SystemPerformanceUtils {
    private const val TAG = "SystemPerformanceUtils"

    // CPU usage calculation related variables (DoKit implementation)
    private var mProcStatFile: RandomAccessFile? = null
    private var mAppStatFile: RandomAccessFile? = null
    private var mLastCpuTime: Long? = null
    private var mLastAppCpuTime: Long? = null
    private val mAboveAndroidO = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    /**
     * Get current app memory usage in MB (DoKit implementation)
     * @param context Context to get ActivityManager
     * @return Memory usage in MB
     */
    fun getAppMemoryUsage(context: Context?): Float {
        var mem = 0.0f
        try {
            var memInfo: Debug.MemoryInfo? = null
            // 28 is Android P
            if (Build.VERSION.SDK_INT > 28) {
                // Get process memory info using totalPss
                memInfo = Debug.MemoryInfo()
                Debug.getMemoryInfo(memInfo)
            } else {
                // As of Android Q, for regular apps this method will only return information
                // about the memory info for the processes running as the caller's uid;
                // no other process memory info is available and will be zero.
                // Also of Android Q the sample rate allowed by this API is significantly limited,
                // if called faster the limit you will receive the same data as the previous call.
                val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                val memInfos = activityManager?.getProcessMemoryInfo(intArrayOf(Process.myPid()))
                if (memInfos != null && memInfos.isNotEmpty()) {
                    memInfo = memInfos[0]
                }
            }

            var totalPss = 0
            if (memInfo != null) {
                totalPss = memInfo.totalPss
            }
            if (totalPss >= 0) {
                // Memory in MB
                mem = totalPss / 1024.0f
            }
        } catch (e: Exception) {
            ShowLogger.e(TAG, e, "Failed to get memory usage")
        }
        return mem
    }

    /**
     * Get CPU usage percentage
     * Uses DoKit's proven implementation
     * Note: No frequency limit needed as onRtcStats is called every 2 seconds
     * @return CPU usage percentage (0-100%)
     */
    fun getAppCpuUsage(): Double {
        return try {
            if (mAboveAndroidO) {
                // Android 8.0+: Use top command
                getCpuDataForO().toDouble()
            } else {
                // Android 7 and below: Use /proc files
                getCpuData().toDouble()
            }
        } catch (e: Exception) {
            ShowLogger.e(TAG, e, "Failed to get CPU usage")
            -1.0
        }
    }

    /**
     * Get CPU usage for Android 8.0+ using top command (DoKit implementation)
     * @return CPU usage percentage (0-100%)
     */
    private fun getCpuDataForO(): Float {
        var process: java.lang.Process? = null
        try {
            process = Runtime.getRuntime().exec("top -n 1")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            var cpuIndex = -1

            while (reader.readLine().also { line = it } != null) {
                line = line?.trim()
                if (TextUtils.isEmpty(line)) {
                    continue
                }

                val tempIndex = getCpuIndex(line!!)
                if (tempIndex != -1) {
                    cpuIndex = tempIndex
                    continue
                }

                if (line!!.startsWith(Process.myPid().toString())) {
                    if (cpuIndex == -1) {
                        continue
                    }
                    val param = line!!.split("\\s+".toRegex())
                    if (param.size <= cpuIndex) {
                        continue
                    }
                    var cpu = param[cpuIndex]
                    if (cpu.endsWith("%")) {
                        cpu = cpu.substring(0, cpu.lastIndexOf("%"))
                    }
                    // val rate = cpu.toFloat() / Runtime.getRuntime().availableProcessors()
                    val rate = cpu.toFloat() / Runtime.getRuntime().availableProcessors()
                    ShowLogger.d(TAG, "top command CPU: ${"%.2f".format(rate)} percent")
                    return rate
                }
            }
        } catch (e: Exception) {
            ShowLogger.e(TAG, e, "Failed to get CPU from top command")
        } finally {
            process?.destroy()
        }
        return 0f
    }

    /**
     * Get CPU index from top command header
     */
    private fun getCpuIndex(line: String): Int {
        if (line.contains("CPU")) {
            val titles = line.split("\\s+".toRegex())
            for (i in titles.indices) {
                if (titles[i].contains("CPU")) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * Get CPU usage for Android 7 and below using /proc files (DoKit implementation)
     * @return CPU usage percentage (0-100%)
     */
    private fun getCpuData(): Float {
        var cpuTime: Long
        var appTime: Long
        var value = 0.0f
        try {
            if (mProcStatFile == null || mAppStatFile == null) {
                mProcStatFile = RandomAccessFile("/proc/stat", "r")
                mAppStatFile = RandomAccessFile("/proc/${Process.myPid()}/stat", "r")
            } else {
                mProcStatFile?.seek(0L)
                mAppStatFile?.seek(0L)
            }

            val procStatString = mProcStatFile?.readLine()
            val appStatString = mAppStatFile?.readLine()
            val procStats = procStatString?.split(" ")
            val appStats = appStatString?.split(" ")

            if (procStats != null && procStats.size > 8 && appStats != null && appStats.size > 14) {
                cpuTime = (procStats[2].toLongOrNull() ?: 0L) +
                        (procStats[3].toLongOrNull() ?: 0L) +
                        (procStats[4].toLongOrNull() ?: 0L) +
                        (procStats[5].toLongOrNull() ?: 0L) +
                        (procStats[6].toLongOrNull() ?: 0L) +
                        (procStats[7].toLongOrNull() ?: 0L) +
                        (procStats[8].toLongOrNull() ?: 0L)
                appTime = (appStats[13].toLongOrNull() ?: 0L) + (appStats[14].toLongOrNull() ?: 0L)

                if (mLastCpuTime != null && mLastAppCpuTime != null) {
                    value = ((appTime - mLastAppCpuTime!!).toFloat() /
                            (cpuTime - mLastCpuTime!!).toFloat()) * 100f
                }
                mLastCpuTime = cpuTime
                mLastAppCpuTime = appTime

                ShowLogger.d(TAG, "/proc/stat CPU: ${"%.2f".format(value)} percent")
            }
        } catch (e: Exception) {
            ShowLogger.e(TAG, e, "Failed to get CPU from /proc files")
        }
        return value
    }
}

