//
//  VLLoginGudieModel.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/13.
//

import UIKit

struct VLLoginGudieModel {
    var imageName: String?
    var title: String?
    var desc: String?

    static func createGudieData() -> [VLLoginGudieModel] {
        var dataArray = [VLLoginGudieModel]()
        var model = VLLoginGudieModel()
        model.imageName = "login_gudie_1"
        model.title = NSLocalizedString("app_login_guide_title1", comment: "")
        model.desc = NSLocalizedString("app_login_guide_desc1", comment: "")
        dataArray.append(model)
        
        model = VLLoginGudieModel()
        model.imageName = "login_gudie_2"
        model.title = NSLocalizedString("app_login_guide_title2", comment: "")
        model.desc = NSLocalizedString("app_login_guide_desc2", comment: "")
        dataArray.append(model)
        
        model = VLLoginGudieModel()
        model.imageName = "login_gudie_3"
        model.title = NSLocalizedString("app_login_guide_title3", comment: "")
        model.desc = NSLocalizedString("app_login_guide_desc3", comment: "")
        dataArray.append(model)
        
        return dataArray
    }
}
