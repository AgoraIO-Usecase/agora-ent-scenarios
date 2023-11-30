package io.agora.scene.joy.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.agora.scene.base.BuildConfig
import io.agora.scene.joy.network.JoyApiManager
import io.agora.scene.joy.network.JoyApiResult
import io.agora.scene.joy.network.JoyApiService
import io.agora.scene.joy.network.JoyGameEntity
import io.agora.scene.joy.network.JoyGameResult
import io.agora.scene.joy.network.SingleLiveEvent
import kotlinx.coroutines.launch

class JoyViewModel : ViewModel() {

    private val TAG = "JoyViewModel"

    private val mJoyApiService: JoyApiService by lazy {
        JoyApiManager.create(JoyApiService::class.java)
    }

    private val mAppId: String
        get() = BuildConfig.AGORA_APP_ID

    val mGameEntityList: MutableLiveData<List<JoyGameEntity>> = SingleLiveEvent()

    fun getGames() {
        viewModelScope.launch {
            try {
                val res: JoyApiResult<JoyGameResult> = mJoyApiService.getGames(mAppId)

                if (res.isSucceed) {
                    mGameEntityList.value = res.data?.list ?: emptyList()
                } else {

                }
            } catch (e: Exception) {
                Log.e(TAG, "getGames Exception ${e.message}")
            }
        }
    }
}