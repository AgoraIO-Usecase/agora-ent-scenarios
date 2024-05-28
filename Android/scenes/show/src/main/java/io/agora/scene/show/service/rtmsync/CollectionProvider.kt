package io.agora.scene.show.service.rtmsync

import io.agora.rtmsyncmanager.Scene
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.service.collection.AUIAttributesModel
import io.agora.rtmsyncmanager.service.collection.AUIListCollection
import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper

internal class CollectionProvider(
    val syncManager: SyncManager
) {

    private val interactionEventObserver =
        mutableMapOf<String, ObservableHelper<(observeKey: String, value: AUIAttributesModel) -> Unit>>()

    private val delegate: (channelName: String, observeKey: String, value: AUIAttributesModel) -> Unit =
        { channelName: String, observeKey: String, value: AUIAttributesModel ->
            interactionEventObserver[channelName]?.notifyEventHandlers {
                it.invoke(observeKey, value)
            }
        }

    private val mapCollectionCreator =
        { channelName: String, sceneKey: String, rtmManager: AUIRtmManager ->
            AUIMapCollection(channelName, sceneKey, rtmManager)
        }

    private val listCollectionCreator =
        { channelName: String, sceneKey: String, rtmManager: AUIRtmManager ->
            AUIListCollection(channelName, sceneKey, rtmManager)
        }

    fun Scene.getListCollection(key: String): AUIListCollection {
        return getCollection(key, listCollectionCreator)
    }

    fun Scene.getMapCollection(key: String): AUIMapCollection {
        return getCollection(key, mapCollectionCreator)
    }

    fun <T> subscribeMultiAttributesDidChanged(
        collection: AUIMapCollection,
        channelName: String,
        clazz: Class<T>,
        onAttributesDidChanged: (T?) -> Unit
    ) {
        collection.subscribeAttributesDidChanged(delegate)
        interactionEventObserver.getOrPut(channelName) { ObservableHelper() }
            .subscribeEvent { key, value->
                if(collection.observeKey == key){
                    onAttributesDidChanged.invoke(
                        value.getMap()?.let {
                            if(it.isNotEmpty()){
                                return@let GsonTools.toBean(GsonTools.beanToString(it), clazz)
                            }
                            return@let null
                        }
                    )
                }
            }
    }

    fun <T> subscribeMultiAttributesDidChanged(
        collection: AUIListCollection,
        channelName: String,
        clazz: Class<T>,
        onAttributesDidChanged: (List<T>) -> Unit
    ) {
        collection.subscribeAttributesDidChanged(delegate)
        interactionEventObserver.getOrPut(channelName) { ObservableHelper() }
            .subscribeEvent { key, value ->
                if(collection.observeKey == key){
                    onAttributesDidChanged.invoke(
                        value.getList()?.map {
                            GsonTools.toBean(GsonTools.beanToString(it), clazz)!!
                        } ?: emptyList()
                    )
                }
            }
    }

    fun clean(channelName: String) {
        interactionEventObserver[channelName]?.unSubscribeAll()
        interactionEventObserver.remove(channelName)
    }

    fun destroy(){
        interactionEventObserver.clear()
    }
}