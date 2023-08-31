//
//  VLSongItemModel.swift
//  Cantata
//
//  Created by CP on 2023/8/31.
//

import Foundation
import AgoraCommon

class VLSongItmModel: VLBaseModel {
    var singer: String?
    var songName: String?
    var songNo: String?
    var imageUrl: String?
    var lyric: String?

    //是否被点过
    var ifChoosed: Bool = false
    var ifChorus: Bool = false
}
