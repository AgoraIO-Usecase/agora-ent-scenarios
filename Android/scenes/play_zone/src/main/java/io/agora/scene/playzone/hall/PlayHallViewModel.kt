package io.agora.scene.playzone.hall

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.playzone.service.api.PlayApiManager
import io.agora.scene.playzone.service.api.PlayZoneGameBanner
import io.agora.scene.playzone.sub.api.SubApiManager
import io.agora.scene.playzone.sub.api.SubGameListModel

class PlayHallViewModel : ViewModel() {

    private val playZoneApiManager by lazy {
        PlayApiManager()
    }

    val mGameConfigLiveData = MutableLiveData<List<PlayZoneGameBanner>>()

    fun gameConfig() {
        playZoneApiManager.getGameBanner { error, list ->
            error?.message?.let {
                ToastUtils.showToast(it)
            }
            mGameConfigLiveData.postValue(list)
        }
    }


    private val subApiManager by lazy {
        SubApiManager()
    }


    val mGameListLiveData = MutableLiveData<List<SubGameListModel>>()

    fun subGameList(vendor: GameVendor) {
        subApiManager.getGameApiInfo { error, gameApi ->
            if (gameApi != null) {
                subApiManager.getSubGameList(gameApi.api.get_mg_list) { gameError, list ->
                    if (gameError == null) {
                    } else {
                        ToastUtils.showToast(gameError.message ?: "获取游戏列表失败")
                    }
                }
            } else if (error != null) {
                ToastUtils.showToast(error.message ?: "未知错误")
            }
        }

        val gameList = when (vendor) {
            GameVendor.Sub -> subApiManager.getSubGameList()
            GameVendor.GroupPlay -> subApiManager.getGroupPlayList()
            GameVendor.YYGame -> subApiManager.getYYGameList()
        }
        mGameListLiveData.postValue(gameList)
    }
}