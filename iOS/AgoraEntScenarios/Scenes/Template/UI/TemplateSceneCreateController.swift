//
//  TemplateSceneCreateController.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation
import UIKit
import AgoraCommon
@objc class TemplateSceneCreateController: UIViewController {
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nil, bundle: nil)
        // 设置资源包名
        AppContext.shared.sceneLocalizeBundleName = "Template"
        AppContext.shared.sceneImageBundleName = "Template"
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        let image = UIImage.sceneImage(name: "BG.png")
        let title1 = sceneLocalized("AgoraSceneTest1")
        let title2 = sceneLocalized("AgoraSceneTest2")
        print("viewDidLoad: \(title1) \(title2) \(image?.size)")
    }
}
