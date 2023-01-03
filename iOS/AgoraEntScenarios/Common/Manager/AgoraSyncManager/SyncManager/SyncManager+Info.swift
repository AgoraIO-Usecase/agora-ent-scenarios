//
//  SyncManager+Info.swift
//  SyncManager
//
//  Created by ZYP on 2021/11/15.
//

import Foundation

public protocol ConfigProtocol {}

public extension AgoraSyncManager {
    struct RtmConfig: ConfigProtocol {
        let appId: String
        let channelName: String
        /// init for RtmConfig
        /// - Parameters:
        ///   - appId: appId
        ///   - channelName: channelName
        public init(appId: String,
                    channelName: String)
        {
            self.channelName = channelName
            self.appId = appId
        }
    }

    struct AskConfig: ConfigProtocol {
        let appId: String
        let channelName: String
        /// init for RtmConfig
        /// - Parameters:
        ///   - appId: appId
        ///   - channelName: channelName
        public init(appId: String,
                    channelName: String)
        {
            self.channelName = channelName
            self.appId = appId
        }
    }

    struct RethinkConfig: ConfigProtocol {
        let appId: String
        let channelName: String
        /// init for RtmConfig
        /// - Parameters:
        ///   - appId: appId
        ///   - channelName: channelName
        public init(appId: String,
                    channelName: String)
        {
            self.channelName = channelName
            self.appId = appId
        }
    }
}
