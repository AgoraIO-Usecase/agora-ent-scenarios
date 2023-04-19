//
//  SAPositionInfo.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/2/7.
//

import UIKit

struct SADataStreamInfo: Codable {
    var code: Int = 101
    var message: String?
}

struct SAPositionInfo: Codable {
    var uid: Int = 0
    var forward: [Double] = []
    var x: CGFloat = 0
    var y: CGFloat = 0
    var angle: CGFloat = 0
}
