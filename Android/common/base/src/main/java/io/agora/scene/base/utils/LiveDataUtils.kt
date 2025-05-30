package io.agora.scene.base.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * Utility class for LiveData operations
 */
object LiveDataUtils {
    /**
     * Observe LiveData once and automatically remove observer after first change
     */
    @JvmStatic
    fun <T> observeOnce(
        owner: LifecycleOwner,
        liveData: LiveData<T>,
        observer: Observer<T>
    ) {
        liveData.observe(owner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer.onChanged(value)
                liveData.removeObserver(this)
            }
        })
    }
}

/**
 * Extension function to observe LiveData once and automatically remove observer
 */
fun <T> LiveData<T>.observeOnce(
    owner: LifecycleOwner,
    observer: Observer<T>
) {
    LiveDataUtils.observeOnce(owner, this, observer)
}

/**
 * Extension function to observe LiveData once with lambda
 */
fun <T> LiveData<T>.observeOnce(
    owner: LifecycleOwner,
    onChanged: (T) -> Unit
) {
    observeOnce(owner, Observer { onChanged(it) })
}

/**
 * Set value only if it's different from current value
 */
fun <T> MutableLiveData<T>.setValueIfNew(newValue: T) {
    if (this.value != newValue) {
        this.value = newValue
    }
}

/**
 * Post value asynchronously only if it's different from current value
 */
fun <T> MutableLiveData<T>.postValueIfNew(newValue: T) {
    if (this.value != newValue) {
        this.postValue(newValue)
    }
}

/**
 * Combine two LiveData instances and transform their values
 */
fun <T1, T2, R> LiveData<T1>.combine(
    other: LiveData<T2>,
    owner: LifecycleOwner,
    transform: (T1?, T2?) -> R
): LiveData<R> {
    val result = MutableLiveData<R>()
    
    // Create separate observers for each LiveData
    val observer1 = Observer<T1> { _ ->
        result.value = transform(this.value, other.value)
    }
    
    val observer2 = Observer<T2> { _ ->
        result.value = transform(this.value, other.value)
    }
    
    this.observe(owner, observer1)
    other.observe(owner, observer2)
    
    return result
}

/**
 * Map non-null values of LiveData
 */
fun <T, R> LiveData<T>.mapNonNull(
    owner: LifecycleOwner,
    transform: (T) -> R
): LiveData<R> {
    val result = MutableLiveData<R>()
    this.observe(owner) { value ->
        value?.let { result.value = transform(it) }
    }
    return result
}

/**
 * Filter LiveData values based on predicate
 */
fun <T> LiveData<T>.filter(
    owner: LifecycleOwner,
    predicate: (T?) -> Boolean
): LiveData<T> {
    val result = MutableLiveData<T>()
    this.observe(owner) { value ->
        if (predicate(value)) {
            result.value = value
        }
    }
    return result
} 