package io.agora.voice.buddy.basic

import io.agora.voice.buddy.event.ActionEvent

abstract class BasicInvoker(commandMap: MutableMap<String, BasicCommand>? = null) {

    private val commandMap: MutableMap<String, BasicCommand> = commandMap ?: mutableMapOf()

    abstract fun addReceiver(receiver: BasicReceiver)

    fun addCommand(name: String, command: BasicCommand) {
        if (!commandMap.containsKey(name)) {
            commandMap[name] = command
        } else {
            commandMap[name]?.addAllReceiver(command.getReceiver())
        }
    }

    protected open fun action(event: ActionEvent) {
        event.command.let {
            commandMap[it]?.execute(event)
        }
    }

}