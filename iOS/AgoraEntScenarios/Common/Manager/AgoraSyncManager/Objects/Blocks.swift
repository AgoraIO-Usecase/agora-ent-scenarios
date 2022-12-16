//
//  Blocks.swift
//  SyncManager
//
//  Created by ZYP on 2021/11/15.
//

import Foundation

public typealias ConnectBlockState = (SocketConnectState) -> Void
public typealias SuccessBlockInt = (Int) -> Void
public typealias SuccessBlock = ([IObject]) -> Void
public typealias SuccessBlockVoid = () -> Void
public typealias SuccessBlockObj = (IObject) -> Void
public typealias SuccessBlockObjOptional = (IObject?) -> Void
public typealias SuccessBlockObjSceneRef = (SceneReference) -> Void
public typealias FailBlock = (SyncError) -> Void

public typealias OnSubscribeBlock = SuccessBlockObj
public typealias OnSubscribeBlockVoid = SuccessBlockVoid
