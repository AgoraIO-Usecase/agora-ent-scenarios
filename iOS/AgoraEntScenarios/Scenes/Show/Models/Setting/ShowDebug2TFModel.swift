//
//  ShowDebug2TFModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/2/11.
//

import UIKit

class ShowDebug2TFModel: NSObject {
    var title: String?
    var tf1Text: String?
    var tf2Text: String?
    var separatorText: String?
    
    init(title: String? = nil, tf1Text: String? = nil, tf2Text: String? = nil, separatorText: String? = nil) {
        self.title = title
        self.tf1Text = tf1Text
        self.tf2Text = tf2Text
        self.separatorText = separatorText
        super.init()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
