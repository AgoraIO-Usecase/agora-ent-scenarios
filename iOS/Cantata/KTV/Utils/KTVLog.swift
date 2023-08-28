//
//  LogProvider.swift
//  AgoraSyncManager
//
//  Created by ZYP on 2021/5/28.
//

import Foundation
import UIKit
import SwiftyBeaver
import AgoraCommon
@objc public class KTVLog: NSObject {
    fileprivate static let log: SwiftyBeaver.Type = {
        let config = AgoraEntLogConfig(sceneName: "KTV")
        let log = AgoraEntLog.createLog(config: config)
        return log
    }()

    @objc public static func error(text: String,
                            tag: String? = nil) {
        log.error(text, context: tag)
    }

    @objc public static func info(text: String,
                           tag: String? = nil) {
        log.info(text, context: tag)
    }

    @objc public static func debug(text: String,
                            tag: String? = nil) {
        log.debug(text, context: tag)
    }

    @objc public static func warning(text: String,
                              tag: String? = nil) {
        log.warning(text, context: tag)
    }
}
