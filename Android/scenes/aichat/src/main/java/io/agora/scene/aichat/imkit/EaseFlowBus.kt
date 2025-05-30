package io.agora.scene.aichat.imkit

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

/**
 * FlowBus
 * See: https://blog.csdn.net/wy313622821/article/details/105047034
 * ```
 *  // Send a event
 *  EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE.name).post(lifecycleScope,
 *      EaseEvent(EaseEvent.EVENT.UPDATE.name, EaseEvent.TYPE.MESSAGE))
 *
 *  // Receive a event
 *  EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE.name).register(this) {
 *      if (it.isMessageChange) {
 *          // update message
 *      }
 *  }
 * ```
 *
 * Also, you can send a stick event, such as:
 * ```
 * // Send a stick event
 * EaseFlowBus.withStick<EaseEvent>(EaseEvent.EVENT.UPDATE.name).post(lifecycleScope,
 *      EaseEvent(EaseEvent.EVENT.UPDATE.name, EaseEvent.TYPE.MESSAGE))
 *
 * // Receive a stick event
 * EaseFlowBus.withStick<EaseEvent>(EaseEvent.EVENT.UPDATE.name).register(this) {
 *     if (it.isMessageChange) {
 *          // update message
 *     }
 * }
 *
 * // Receive a stick event with lifecycle
 * EaseFlowBus.withStick<EaseEvent>(EaseEvent.EVENT.UPDATE.name)
 *      .onLifeCycle(Lifecycle.State.RESUMED).register(this) {
 *    if (it.isMessageChange) {
 *      // update message
 *    }
 * }
 * ```
 */
object EaseFlowBus {
    private const val TAG = "EaseFlowBus"
    private val busMap = mutableMapOf<String, EventBus<*>>()
    private val busStickMap = mutableMapOf<String, StickEventBus<*>>()

    @Synchronized
    fun <T> with(key: String): EventBus<T> {
        var eventBus = busMap[key]
        if (eventBus == null) {
            eventBus = EventBus<T>(key)
            busMap[key] = eventBus
        }
        return eventBus as EventBus<T>
    }

    @Synchronized
    fun <T> withStick(key: String): StickEventBus<T> {
        var eventBus = busStickMap[key]
        if (eventBus == null) {
            eventBus = StickEventBus<T>(key)
            busStickMap[key] = eventBus
        }
        return eventBus as StickEventBus<T>
    }

    open class EventBus<T>(private val key: String) : DefaultLifecycleObserver {

        private val _events: MutableSharedFlow<T> by lazy {
            obtainEvent()
        }
        val events  = _events.asSharedFlow()
        private var operateEvents: Flow<T> = events
        private var isCollectLatest: Boolean = false

        open fun obtainEvent(): MutableSharedFlow<T> =
            MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)


        fun register(lifecycleOwner: LifecycleOwner
                     , action: (t: T) -> Unit) {
            lifecycleOwner.lifecycle.addObserver(this)
            lifecycleOwner.lifecycleScope.launch {
                val flow = if (this@EventBus is StickEventBus) {
                    operateEvents
                        .flowWithLifecycle(lifecycleOwner.lifecycle, this@EventBus.lifeState)
                } else {
                    operateEvents
                }
                if (isCollectLatest) {
                    flow.collectLatest {
                        try {
                            action(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ChatLog.e(TAG, "EaseFlowBus - Error:$e")
                        }
                    }
                } else {
                    flow.collect {
                        try {
                            action(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ChatLog.e(TAG, "EaseFlowBus - Error:$e")
                        }
                    }
                }
            }
        }

        @OptIn(FlowPreview::class)
        fun debounce(duration: Long): EventBus<T> {
            operateEvents = operateEvents
                .debounce(duration)
            return this
        }

        fun collectLatest(isCollectLatest: Boolean = false): EventBus<T> {
            this.isCollectLatest = isCollectLatest
            return this
        }

        fun filter(predicate: suspend (T) -> Boolean): EventBus<T> {
            operateEvents = operateEvents
                .filter(predicate)
            return this
        }

        fun sample(duration: Long): EventBus<T> {
            operateEvents = operateEvents
                .sample(duration)
            return this
        }

        fun conflate(): EventBus<T> {
            operateEvents = operateEvents
                .conflate()
            return this
        }

        fun buffer(): EventBus<T> {
            operateEvents = operateEvents
                .buffer()
            return this
        }

        suspend fun post(event: T) {
            _events.emit(event)
        }

        // Specify the thread to post data
        fun post(scope: CoroutineScope, event: T) {
            scope.launch {
                _events.emit(event)
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            val subscriptCount = _events.subscriptionCount.value
            if (subscriptCount <= 0)
                busMap.remove(key)
        }
    }

    class StickEventBus<T>(key: String) : EventBus<T>(key) {
        private var _lifeState: Lifecycle.State? = null

        internal val lifeState: Lifecycle.State = _lifeState ?: Lifecycle.State.STARTED

        override fun obtainEvent(): MutableSharedFlow<T> =
            MutableSharedFlow(1, 1, BufferOverflow.DROP_OLDEST)

        fun onLifeCycle(lifeState: Lifecycle.State): StickEventBus<T> {
            _lifeState = lifeState
            return this
        }

    }

}
