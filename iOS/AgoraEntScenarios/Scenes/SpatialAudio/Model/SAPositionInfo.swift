//
//  SAPositionInfo.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/2/7.
//

import UIKit

class SAPositionInfo: Codable {
    var uid: Int = 0
    var position: [Double] = []
    var forward: [Double] = []
    var x: CGFloat = 0
    var y: CGFloat = 0
    var angle: CGFloat = 0
}
