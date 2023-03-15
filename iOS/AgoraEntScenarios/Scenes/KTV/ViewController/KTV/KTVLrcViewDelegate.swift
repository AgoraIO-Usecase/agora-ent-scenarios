//
//  KTVLrcViewDelegate.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation

@objc public protocol KTVLrcViewDelegate: NSObjectProtocol {
    func onUpdatePitch(pitch: Float)
    func onUpdateProgress(progress: Int)
    func onDownloadLrcData(url: String)
}
