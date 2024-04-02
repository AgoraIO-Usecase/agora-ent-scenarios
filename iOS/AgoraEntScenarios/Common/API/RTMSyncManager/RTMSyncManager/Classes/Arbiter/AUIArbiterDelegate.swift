//
//  AUIArbiterDelegate.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/2/23.
//

import Foundation

@objc public protocol AUIArbiterDelegate: NSObjectProtocol {
    func onArbiterDidChange(channelName: String, arbiterId: String)
    func onError(channelName: String, error: NSError)
}
