//
//  ShowDebug1TFModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/2/11.
//

import UIKit

class ShowDebug1TFModel: NSObject {
    var title: String?
    var tfText: String?
    var unitText: String?
    
    init(title: String? = nil, tfText: String? = nil, unitText: String? = nil) {
        self.title = title
        self.tfText = tfText
        self.unitText = unitText
    }
}
