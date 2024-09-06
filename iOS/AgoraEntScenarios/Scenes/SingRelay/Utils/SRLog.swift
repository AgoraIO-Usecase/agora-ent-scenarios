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
@objc class SRLog: NSObject {
    @objc static let kLogKey = "singRelay"

    @objc static func error(text: String,
                            tag: String? = nil) {
        agoraDoMainThreadTask {
            let log = AgoraEntLog.getSceneLogger(with: kLogKey)
            log.error(text, context: tag)
        }
    }

    @objc static func info(text: String,
                           tag: String? = nil) {
        agoraDoMainThreadTask {
            let log = AgoraEntLog.getSceneLogger(with: kLogKey)
            log.info(text, context: tag)
        }
    }

    @objc static func debug(text: String,
                            tag: String? = nil) {
        agoraDoMainThreadTask {
            let log = AgoraEntLog.getSceneLogger(with: kLogKey)
            log.debug(text, context: tag)
        }
    }

    @objc static func warning(text: String,
                              tag: String? = nil) {
        agoraDoMainThreadTask {
            let log = AgoraEntLog.getSceneLogger(with: kLogKey)
            log.warning(text, context: tag)
        }
    }
}
