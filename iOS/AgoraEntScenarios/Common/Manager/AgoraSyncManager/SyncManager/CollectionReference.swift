
//
//  CollectionReference.swift
//  SyncManager
//
//  Created by ZYP on 2021/12/16.
//

import Foundation

public class CollectionReference {
    private let manager: AgoraSyncManager
    public let className: String
    public let parent: SceneReference
    let inertnalClassName: String

    public init(manager: AgoraSyncManager,
                parent: SceneReference,
                className: String)
    {
        self.manager = manager
        self.className = parent.id + className
        self.parent = parent
        inertnalClassName = parent.id + className
    }

    public func document(id: String = "") -> DocumentReference {
        return DocumentReference(manager: manager, parent: self, id: id)
    }

    public func add(data: [String: Any?],
                    success: SuccessBlockObj?,
                    fail: FailBlock?)
    {
        manager.add(reference: self,
                    data: data,
                    success: success,
                    fail: fail)
    }

    /// update a item in collcetion
    /// - Parameters:
    ///   - id: id of item
    public func update(id: String,
                       data: [String: Any?],
                       success: SuccessBlockVoid?,
                       fail: FailBlock?)
    {
        manager.update(reference: self,
                       id: id,
                       data: data,
                       success: success,
                       fail: fail)
    }

    /// delete an item
    public func delete(id: String,
                       success: SuccessBlockObjOptional?,
                       fail: FailBlock?)
    {
        manager.delete(reference: self,
                       id: id,
                       success: success,
                       fail: fail)
    }

    public func get(success: SuccessBlock?,
                    fail: FailBlock?)
    {
        manager.get(collectionRef: self,
                    success: success,
                    fail: fail)
    }

    public func delete(success: SuccessBlock?,
                       fail: FailBlock?)
    {
        manager.delete(collectionRef: self,
                       success: success,
                       fail: fail)
    }
}
