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
@objc public class CantataLog: NSObject {
    
    static let kLogKey = "Cantata"
    
    fileprivate static var log: SwiftyBeaver.Type = {
        let log = AgoraEntLog.getSceneLogger(with: kLogKey)
        return log
    }()
    
    @objc public static func error(text: String,
                                   tag: String? = nil) {
        AgoraEntLog.getSceneLogger(with: kLogKey).error(text, context: tag)
    }
    
    @objc public static func info(text: String,
                                  tag: String? = nil) {
        AgoraEntLog.getSceneLogger(with: kLogKey).info(text, context: tag)
    }
    
    @objc public static func info(text: String) {
        AgoraEntLog.getSceneLogger(with: kLogKey).info(text, context: kLogKey)
    }
    
    @objc public static func debug(text: String,
                                   tag: String? = nil) {
        AgoraEntLog.getSceneLogger(with: kLogKey).debug(text, context: tag)
    }
    
    @objc public static func warning(text: String,
                                     tag: String? = nil) {
        AgoraEntLog.getSceneLogger(with: kLogKey).warning(text, context: tag)
    }
}
