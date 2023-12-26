//
//  VLMineAccountModel.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/16.
//

import UIKit

enum VLMineAccountClickType {
    case sign_out  // 注销
    
}

struct VLMineAccountModel {
    var title: String?
    var desc: String?
    var type: VLMineAccountClickType = .sign_out
    
    static func createData() -> [VLMineAccountModel] {
        var dataArray = [VLMineAccountModel]()
        
        var model = VLMineAccountModel()
        model.title = NSLocalizedString("app_logoff_account", comment: "")
        model.desc = NSLocalizedString("app_logooff_account_desc", comment: "")
        model.type = .sign_out
        dataArray.append(model)

        return dataArray
    }
}
