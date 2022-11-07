//
//  ShowLiveViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import UIKit
import AgoraRtcKit

class ShowLiveViewController: UIViewController {

    var agoraKit: AgoraRtcEngineKit!
    override func viewDidLoad() {
        super.viewDidLoad()
        if agoraKit == nil {
            fatalError("agoraKit can not be nil")
            return
        }
    }
}
