package io.agora.scene.base.api.apiutils

import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object SchedulersUtil {

    fun <T> applySchedulersIO(): ObservableTransformer<T, T> {
        return ObservableTransformer { observable ->
            observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
        }
    }

    fun <T> applyApiSchedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer { observable ->
            observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }
}