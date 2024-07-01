package io.agora.imkitmanager.utils

import android.content.Context
import android.util.Log

class AUIChatLogger constructor(private val config: Config) {

    companion object {

        private val logAdapters = mutableListOf<LogAdapter>()
        private var logger: AUIChatLogger? = null

        private fun addLogAdapterSafe(logAdapter: LogAdapter) {
            if (!logAdapters.contains(logAdapter)) {
                logAdapters.add(logAdapter)
            }
        }

        private fun removeLogAdapterSafe(logAdapter: LogAdapter) {
            if (logAdapters.contains(logAdapter)) {
                logAdapters.remove(logAdapter)
            }
        }

        fun initLogger(config: Config) {
            logger = AUIChatLogger(config)
        }

        fun logger(): AUIChatLogger {
            return logger
                ?: throw RuntimeException("Before calling AUILogger.logger(), the AUILogger.initLogger(Config) method must be called firstly!")
        }
    }

    private val wrapTag = config.rootTag

    data class Config constructor(
        val context: Context,
        val rootTag: String,
        val debug: Boolean,
        val logCallback: AUILogCallback? = null
    )

    private val callbackLogAdapter by lazy {
        object : LogAdapter {
            var lastMessage = ""


            override fun log(priority: Int, tag: String?, message: String) {
                if (lastMessage == message) {
                    return
                }
                // In case of the same message, only log once
                lastMessage = message

                when (priority) {
                    Log.DEBUG -> {
                        config.logCallback?.onLogDebug(tag ?: "", message)
                        if (config.debug) {
                            Log.d(tag ?: "", message)
                        }
                    }

                    Log.INFO -> {
                        config.logCallback?.onLogInfo(tag ?: "", message)
                        if (config.debug) {
                            Log.i(tag ?: "", message)
                        }
                    }

                    Log.WARN -> {
                        config.logCallback?.onLogWarning(tag ?: "", message)
                        if (config.debug) {
                            Log.w(tag ?: "", message)
                        }
                    }

                    Log.ERROR -> {
                        config.logCallback?.onLogError(tag ?: "", message)
                        if (config.debug) {
                            Log.e(tag ?: "", message)
                        }
                    }
                }
            }
        }
    }


    init {
        addLogAdapterSafe(callbackLogAdapter)
    }

    fun i(tag: String, message: String) {
        logAdapters.forEach {
            it.log(Log.INFO, "${wrapTag}_$tag", message)
        }
    }

    fun w(tag: String, message: String) {
        logAdapters.forEach {
            it.log(Log.WARN, "${wrapTag}_$tag", message)
        }
    }

    fun d(tag: String, message: String) {
        logAdapters.forEach {
            it.log(Log.DEBUG, "${wrapTag}_$tag", message)
        }
    }

    fun e(tag: String, message: String) {
        logAdapters.forEach {
            it.log(Log.ERROR, "${wrapTag}_$tag", message)
        }
    }

    interface AUILogCallback {
        fun onLogDebug(tag: String, message: String)
        fun onLogInfo(tag: String, message: String)
        fun onLogWarning(tag: String, message: String)
        fun onLogError(tag: String, message: String)
    }

    interface LogAdapter {
        fun log(priority: Int, tag: String?, message: String)
    }
}

