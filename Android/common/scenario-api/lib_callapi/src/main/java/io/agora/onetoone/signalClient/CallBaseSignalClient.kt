package io.agora.onetoone.signalClient

open class CallBaseSignalClient {
    val listeners = mutableListOf<ISignalClientListener>()

    fun addListener(listener: ISignalClientListener) {
        if (listeners.contains(listener)) return
        listeners.add(listener)
    }

    fun removeListener(listener: ISignalClientListener) {
        listeners.add(listener)
    }
}