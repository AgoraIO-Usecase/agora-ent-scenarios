//
//  RtmSyncManager+Handle.swift
//  SyncManager
//
//  Created by ZYP on 2021/11/15.
//

import AgoraRtmKit
import Foundation

/// 哪些行为会有回调事件？
/// 1. sceneRef.delete() subscribe(key: nil)
/// 3. sceneRef.update(key) subscribe(key: key) 更新房间信息 只有update事件
/// 4. syncRef.collection(className: "member").add(data: ["userName" : "UserName"])
/// 监听syncRef.collection(className: "member").document().subscribe(key: nil) 有齐全的事件

extension RtmSyncManager {
    func notifyObserver(channel: AgoraRtmChannel, attributes: [AgoraRtmChannelAttribute]) {
        Log.info(text: "--- notifyObserver start ---", tag: "RtmSyncManager")

        /// 1. defaultChannel 会有缓存  没有的话走update
        /// 2. collection 会有缓存
        /// 3. scene.id+key、scene.id 没有缓存 都走update

        if channels[channelName] == channel { /** 1. defaultChannel **/
            if let cache = cachedAttrs[channel] { /** cache 存在的情况下，计算需要回调的事件 **/
                Log.info(text: "--- [defaultChannel, has cache]", tag: "RtmSyncManager")
                guard let tempChannel = channels[sceneName] else {
                    Log.errorText(text: "can not find scene channel", tag: "RtmSyncManager")
                    return
                }
                let onCreateBlock = onCreateBlocks[tempChannel]
                let onUpdateBlock = onUpdatedBlocks[tempChannel]
                let onDeleteBlock = onDeletedBlocks[tempChannel]

                invokeEvent(cache: cache,
                            attributes: attributes,
                            onCreateBlock: onCreateBlock,
                            onUpdateBlock: onUpdateBlock,
                            onDeleteBlock: onDeleteBlock,
                            isDefaultChannelhandle: true)
                cachedAttrs[channel] = attributes
                return
            }

            /** cache 不存在的情况下，onOpdate **/
            if sceneName == nil {
                Log.info(text: "sceneName == nil", tag: "RtmSyncManager")
                return
            }
            Log.info(text: "--- [defaultChannel, no cache]", tag: "RtmSyncManager")
            guard let tempChannel = channels[sceneName] else {
                Log.errorText(text: "can not find scene channel", tag: "RtmSyncManager")
                return
            }
            if let onUpdateBlock = onUpdatedBlocks[tempChannel] {
                for arrt in attributes {
                    Log.info(text: "--- invoke onUpdateBlock", tag: "RtmSyncManager")
                    onUpdateBlock(arrt.toAttribute())
                }
            }
            cachedAttrs[channel] = attributes
            return
        }

        if let cache = cachedAttrs[channel] { /** 2. collection **/
            Log.info(text: "--- [collection]", tag: "RtmSyncManager")
            let onCreateBlock = onCreateBlocks[channel]
            let onUpdateBlock = onUpdatedBlocks[channel]
            let onDeleteBlock = onDeletedBlocks[channel]

            invokeEvent(cache: cache,
                        attributes: attributes,
                        onCreateBlock: onCreateBlock,
                        onUpdateBlock: onUpdateBlock,
                        onDeleteBlock: onDeleteBlock)
            cachedAttrs[channel] = attributes
            return
        }

        if let onUpdateBlock = onUpdatedBlocks[channel] { /** 3. scene.id or scene.id + key **/
            Log.info(text: "--- [scene.id or scene.id + key]", tag: "RtmSyncManager")
            for arrt in attributes {
                Log.info(text: "--- invoke onUpdateBlock", tag: "RtmSyncManager")
                onUpdateBlock(arrt.toAttribute())
            }
            return
        }

        Log.info(text: "--- notifyObserver do empty ---", tag: "RtmSyncManager")
    }

    /// 计算应该产生的回调事件，并执行回调事件
    /// - Parameters:
    ///   - cache: 原来的数据
    ///   - attributes: 更新的attributes
    ///   - isDefaultChannelhandle: 是否是默认channel的处理，主要处理删除房间时，本地不产生onDelete回调事件
    fileprivate func invokeEvent(cache: [AgoraRtmChannelAttribute],
                                 attributes: [AgoraRtmChannelAttribute],
                                 onCreateBlock: OnSubscribeBlock?,
                                 onUpdateBlock: OnSubscribeBlock?,
                                 onDeleteBlock: OnSubscribeBlock?,
                                 isDefaultChannelhandle: Bool = false)
    {
        Log.info(text: "--- onCreateBlock is \(onCreateBlock == nil ? "nil" : "not nil")", tag: "RtmSyncManager")
        Log.info(text: "--- onUpdateBlock is \(onUpdateBlock == nil ? "nil" : "not nil")", tag: "RtmSyncManager")
        Log.info(text: "--- onDeleteBlock is \(onDeleteBlock == nil ? "nil" : "not nil")", tag: "RtmSyncManager")

        var onlyA = [IObject]()
        var onlyB = [IObject]()
        var both = [IObject]()
        var temp = [String: AgoraRtmChannelAttribute]()

        for i in cache {
            temp[i.key] = i
        }

        for b in attributes {
            if let i = temp[b.key] {
                if b.value != i.value {
                    both.append(b.toAttribute())
                }
                temp.removeValue(forKey: b.key)
            } else {
                onlyB.append(b.toAttribute())
            }
        }

        for i in temp.values {
            onlyA.append(i.toAttribute())
        }

        if let onUpdateBlockTemp = onUpdateBlock {
            for i in both {
                Log.info(text: "--- invoke onUpdateBlock", tag: "RtmSyncManager")
                onUpdateBlockTemp(i)
            }
        }

        if let onCreateBlockTemp = onCreateBlock {
            for i in onlyB {
                Log.info(text: "--- invoke onCreateBlock", tag: "RtmSyncManager")
                onCreateBlockTemp(i)
            }
        }

        if let onDeleteBlockTemp = onDeleteBlock {
            for i in onlyA {
                if isDefaultChannelhandle { /** 处理默认房间的内容 **/
                    if i.getId() != sceneName { /** 过滤非本房间的删除事件 **/
                        Log.info(text: "--- be ignore, not local scene id", tag: "RtmSyncManager")
                        continue
                    }

                    /// 本房间的删除事件
                    Log.info(text: "--- invoke onDeleteBlock", tag: "RtmSyncManager")
                    onDeleteBlockTemp(i)
                    continue
                } else { /** collection 删除事件 **/
                    Log.info(text: "--- invoke onDeleteBlock", tag: "RtmSyncManager")
                    onDeleteBlockTemp(i)
                }
            }
        }
    }
}
