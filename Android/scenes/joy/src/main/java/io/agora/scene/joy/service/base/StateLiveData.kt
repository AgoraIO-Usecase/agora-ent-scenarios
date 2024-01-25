package io.agora.scene.joy.service.base

import androidx.lifecycle.MutableLiveData

class StateLiveData<T> : MutableLiveData<JoyApiResult<T>>() {
}