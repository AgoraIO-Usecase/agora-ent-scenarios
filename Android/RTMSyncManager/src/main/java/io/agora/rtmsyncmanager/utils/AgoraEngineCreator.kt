package io.agora.rtmsyncmanager.utils

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig

object AgoraEngineCreator {

    fun createRtmClient(
        context: Context,
        appId: String,
        userId: String
    ): RtmClient {
        val rtmConfig = RtmConfig.Builder(appId, userId)
            .build()
        return RtmClient.create(rtmConfig)
    }

    private fun isMainProcess(context: Context): Boolean {
        val processName: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessNameByApplication()
        } else {
            getProcessNameByReflection()
        }
        return context.applicationInfo.packageName == processName
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private fun getProcessNameByApplication(): String {
        return Application.getProcessName()
    }

    private fun getProcessNameByReflection(): String {
        var processName: String = ""
        try {
            val declaredMethod = Class.forName(
                "android.app.ActivityThread", false,
                Application::class.java.classLoader
            )
                .getDeclaredMethod("currentProcessName", *arrayOfNulls<Class<*>?>(0))
            declaredMethod.isAccessible = true
            val invoke = declaredMethod.invoke(null, *arrayOfNulls(0))
            if (invoke is String) {
                processName = invoke
            }
        } catch (e: Throwable) {
        }
        return processName
    }
}