package io.agora.voice.buddy.basic

import io.agora.voice.buddy.event.ActionEvent

abstract class BasicCommand(receiverMap: MutableMap<String, BasicReceiver>? = null) {

    private val receiverMap: MutableMap<String, BasicReceiver> = receiverMap ?: mutableMapOf()

    abstract fun name(): String

    fun addReceiver(receiver: BasicReceiver) {
        if (this.receiverMap.containsKey(receiver.name())) {
            throw IllegalStateException("${name()} addReceiver, Repeat to add, receiver is ${receiver.name()}")
        } else {
            receiverMap[receiver.name()] = receiver
        }
    }

    fun addAllReceiver(receiverMap: MutableMap<String, BasicReceiver>?) {
        receiverMap?.let { map ->
            if (map.isNotEmpty()) {
                map.forEach {
                    addReceiver(it.value)
                }
            }
        }
    }

    fun getReceiver(): MutableMap<String, BasicReceiver> {
        return receiverMap
    }

    fun execute(event: ActionEvent) {
        event.receiver?.let {
            receiverMap[it]?.action(event)
        }
    }
}