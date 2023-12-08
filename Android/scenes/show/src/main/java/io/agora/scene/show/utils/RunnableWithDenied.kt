package io.agora.scene.show.utils

/**
 * @author create by zhangwei03
 */
abstract class RunnableWithDenied : Runnable{
    abstract fun onDenied()
}