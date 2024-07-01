//
//  AUILogger.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation
import SwiftyBeaver

public class CommonLogger: NSObject {
    
    static let kLogKey = "Common"
    
    public static func info(_ text: String, tag: String = "AgoraEntCommon") {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).info(text, context: tag)
        }
    }

    public static func warn(_ text: String, tag: String = "AgoraEntCommon") {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).warning(text, context: tag)
        }
    }

    public static func error(_ text: String, tag: String = "AgoraEntCommon") {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).error(text, context: tag)
        }
    }

    public static func default_info(_ text: String, tag: String = "AgoraEntCommon") {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).info(text, context: tag)
        }
    }
}
