//
//  IAUICollection.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/4.
//

import Foundation

public typealias AUICollectionGetClosure = (NSError?, Any?)-> Void


//(publisher uid, valueCmd, new value of item) -> value[new value of edit item]
public typealias AUICollectionValueWillChangeClosure = (String, String?, [String: Any]) -> [String: Any]?

//(publisher uid, valueCmd, new value)
public typealias AUICollectionAddClosure = (String, String?, [String: Any]) -> NSError?

//(publisher uid, valueCmd, new value, old value of item)
public typealias AUICollectionUpdateClosure = (String, String?, [String: Any], [String: Any]) -> NSError?

//(publisher uid, valueCmd, oldValue)
public typealias AUICollectionRemoveClosure = (String, String?, [String: Any]) -> NSError?

//(publisher uid, valueCmd, old value of item, keys, update value, min, max)
public typealias AUICollectionCalculateClosure = (String, String?, [String: Any], [String], Int, Int, Int) -> NSError?

//(channelName, key, valueCmd, value[will set metadata])->value[can set metadata]
public typealias AUICollectionAttributesWillSetClosure = (String, String, String?, AUIAttributesModel) -> AUIAttributesModel

//(channelName, key, value)
public typealias AUICollectionAttributesDidChangedClosure = (String, String, AUIAttributesModel) -> Void

@objc public class AUIAttributesModel: NSObject {
    private var attributes: Any?
    public required init(list: [[String: Any]]) {
        self.attributes = list
        super.init()
    }
    public required init(map: [String: Any]) {
        self.attributes = map
        super.init()
    }
    
    public func getMap() -> [String: Any]? {
        return attributes as? [String: Any]
    }
    
    public func getList() -> [[String: Any]]? {
        return attributes as? [[String: Any]]
    }
}

@objc public protocol IAUICollection: NSObjectProtocol {
    
    init(channelName: String, observeKey: String, rtmManager: AUIRtmManager) 
    
    /// 对应的节点对象将要被更新，询问是否需要本地增删(例如更新一个节点，需要再次更新最新时间)
    /// - Parameter callback: <#callback description#>
    @objc optional func subsceibeValueWillChange(callback: AUICollectionValueWillChangeClosure?)
    
    /// 订阅即将添加新的节点的事件
    /// - Parameter callback: <#callback description#>
    @objc optional func subscribeWillAdd(callback: AUICollectionAddClosure?)
    
    /// 订阅即将替换某个节点的事件
    /// - Parameter callback: <#callback description#>
    @objc optional func subscribeWillUpdate(callback: AUICollectionUpdateClosure?)
    
    /// 订阅即将合并某个节点的事件回调
    /// - Parameter callback: <#callback description#>
    @objc optional func subscribeWillMerge(callback: AUICollectionUpdateClosure?)
    
    /// 订阅即将删除某个节点的事件回调
    /// - Parameter callback: <#callback description#>
    @objc optional func subscribeWillRemove(callback: AUICollectionRemoveClosure?)
    
    /// 订阅即将计算某个节点的事件回调
    /// - Parameter callback: <#callback description#>
    @objc optional func subscribeWillCalculate(callback: AUICollectionCalculateClosure?)
    
    /// 即将写入meta data，上层是否需要修改
    /// - Parameter callback: <#callback description#>
    func subscribeAttributesWillSet(callback: AUICollectionAttributesWillSetClosure?)
    
    /// 收到的meta data变化
    /// - Parameter callback: <#callback description#>
    func subscribeAttributesDidChanged(callback: AUICollectionAttributesDidChangedClosure?)
    
    /// 查询当前scene节点所有内容
    /// - Parameter callback: <#callback description#>
    func getMetaData(callback: AUICollectionGetClosure?)
    
    /// 获取本地metadata，仲裁者为本地缓存数据（可能比远端数据更新），观众则为真实远端数据
    /// - Parameter attributes: <#attributes description#>
    func getLocalMetaData() -> AUIAttributesModel?
    
    
    /// 更新本地到远端
    func syncLocalMetaData()
}


@objc public protocol IAUIMapCollection: IAUICollection {
    
    /// 添加节点
    /// - Parameters:
    ///   - valueCmd: <#valueCmd description#>
    ///   - value: <#value description#>
    ///   - callback: <#callback description#>
    func addMetaData(valueCmd: String?,
                     value: [String: Any],
                     callback: ((NSError?)->())?)
    
    /// 更新节点
    /// - Parameters:
    ///   - valueCmd: 命令类型
    ///   - value: <#value description#>
    ///   - callback: <#callback description#>
    func updateMetaData(valueCmd: String?,
                        value: [String: Any],
                        callback: ((NSError?)->())?)
    
    /// 合并节点
    /// - Parameters:
    ///   - valueCmd: <#valueCmd description#>
    ///   - value: <#value description#>
    ///   - callback: <#callback description#>
    func mergeMetaData(valueCmd: String?,
                       value: [String: Any],
                       callback: ((NSError?)->())?)
    
    /// 移除
    /// - Parameters:
    ///   - valueCmd: <#value description#>
    ///   - callback: <#callback description#>
    func removeMetaData(valueCmd: String?,
                        callback: ((NSError?)->())?)
    
    /// 增加/减小节点(节点必须是Int)
    /// - Parameters:
    ///   - valueCmd: <#valueCmd description#>
    ///   - key: <#key description#>
    ///   - value: <#value description#>
    ///   - min: <#min description#>
    ///   - max: <#max description#>
    ///   - callback: <#callback description#>
    func calculateMetaData(valueCmd: String?,
                           key: [String],
                           value: Int,
                           min: Int,
                           max: Int,
                           callback: ((NSError?)->())?)
    
    /// 移除整个collection对应的key
    /// - Parameter callback: <#callback description#>
    func cleanMetaData(callback: ((NSError?)->())?)
}


@objc public protocol IAUIListCollection: IAUICollection {
    
    /// 添加节点
    /// - Parameters:
    ///   - valueCmd: <#valueCmd description#>
    ///   - value: <#value description#>
    ///   - filter: 如果原始数据满足该filter，新增失败，为nil则无条件新增
    ///   - callback: <#callback description#>
    func addMetaData(valueCmd: String?,
                     value: [String: Any],
                     filter: [[String: Any]]?,
                     callback: ((NSError?)->())?)
    
    /// 更新节点
    /// - Parameters:
    ///   - valueCmd: 命令类型
    ///   - value: <#value description#>
    ///   - filter: 如果原始数据满足该filter，才会更新成功，为nil则更新全部
    ///   - callback: <#callback description#>
    func updateMetaData(valueCmd: String?,
                        value: [String: Any],
                        filter: [[String: Any]]?,
                        callback: ((NSError?)->())?)
    
    /// 合并节点
    /// - Parameters:
    ///   - valueCmd: <#valueCmd description#>
    ///   - value: <#value description#>
    ///   - filter: 如果原始数据满足该filter，才会合并成功，为nil则合并全部
    ///   - callback: <#callback description#>
    func mergeMetaData(valueCmd: String?,
                       value: [String: Any],
                       filter: [[String: Any]]?,
                       callback: ((NSError?)->())?)
    
    /// 移除
    /// - Parameters:
    ///   - valueCmd: <#value description#>
    ///   - filter: <#value description#>
    ///   - callback: <#callback description#>
    func removeMetaData(valueCmd: String?,
                        filter: [[String: Any]]?,
                        callback: ((NSError?)->())?)
    
    /// 增加/减小节点(节点必须是Int)
    /// - Parameters:
    ///   - valueCmd: <#valueCmd description#>
    ///   - key: <#key description#>
    ///   - value: <#value description#>
    ///   - min: <#min description#>
    ///   - max: <#max description#>
    ///   - filter: <#filter description#>
    ///   - callback: <#callback description#>
    func calculateMetaData(valueCmd: String?,
                           key: [String],
                           value: Int,
                           min: Int,
                           max: Int,
                           filter: [[String: Any]]?,
                           callback: ((NSError?)->())?)
    
    /// 移除整个collection对应的key
    /// - Parameter callback: <#callback description#>
    func cleanMetaData(callback: ((NSError?)->())?)
}
