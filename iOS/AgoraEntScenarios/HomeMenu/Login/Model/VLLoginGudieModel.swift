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
        model.title = "声动语聊"
        model.desc = "搭载声网最佳音效实践、最新音频技术的语聊房 带来更沉浸、更易用、更有趣的语聊体验"
        dataArray.append(model)
        
        model = VLLoginGudieModel()
        model.imageName = "login_gudie_2"
        model.title = "在线K歌房"
        model.desc = "一站式接入 20W+ 正版 K 歌热门曲库，灵活接入各类娱乐社交场景，激活多样 K 歌互动玩法"
        dataArray.append(model)
        
        model = VLLoginGudieModel()
        model.imageName = "login_gudie_3"
        model.title = "直播超级画质"
        model.desc = "视频『高清』升级，人更美、物更真，体验更流畅，为直播变现增添更多 buff"
        dataArray.append(model)
        
        return dataArray
    }
}
