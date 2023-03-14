package io.agora.voice.common.utils

import androidx.annotation.Keep
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.AndroidViewModel
import java.lang.Exception
import java.lang.reflect.ParameterizedType

@Keep
object BaseUiTool {
    @JvmStatic
    fun <T> getGenericClass(clz: Class<*>, index: Int): Class<T>? {
        val type = clz.genericSuperclass ?: return null
        return (type as ParameterizedType).actualTypeArguments[index] as Class<T>
    }

    fun getViewBinding(bindingClass: Class<*>, inflater: LayoutInflater): Any? {
        try {
            val inflateMethod = bindingClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
            return inflateMethod.invoke(null, inflater)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    fun <T> getViewBinding(bindingClass: Class<T>, inflater: LayoutInflater, container: ViewGroup?): T? {
        try {
            val inflateMethod = bindingClass.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            )
            return inflateMethod.invoke(null, inflater, container, false) as T?
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun <T : ViewModel> getViewModel(viewModelClass: Class<T>, owner: ViewModelStoreOwner): T {
        return ViewModelProvider(owner)[viewModelClass]
    }

    fun <T : ViewModel> getViewModel(
        viewModelClass: Class<T>,
        factory: ViewModelProvider.Factory,
        owner: ViewModelStoreOwner
    ): T {
        return ViewModelProvider(owner, factory)[viewModelClass]
    }

    fun <T : AndroidViewModel> getAndroidViewModel(activity: ComponentActivity, viewModelClass: Class<T>): T {
        return ViewModelProvider(activity)[viewModelClass]
    }

    fun <T : AndroidViewModel> getAndroidViewModel(fragment: Fragment, viewModelClass: Class<T>): T {
        return ViewModelProvider(fragment.requireActivity())[viewModelClass]
    }

    fun <T : AndroidViewModel> getAndroidViewModel(
        activity: ComponentActivity,
        factory: ViewModelProvider.Factory,
        viewModelClass: Class<T>
    ): T {
        return ViewModelProvider(activity, factory)[viewModelClass]
    }

    fun <T : AndroidViewModel> getAndroidViewModel(
        fragment: Fragment,
        factory: ViewModelProvider.Factory,
        viewModelClass: Class<T>
    ): T {
        return ViewModelProvider(fragment.requireActivity(), factory)[viewModelClass]
    }
}