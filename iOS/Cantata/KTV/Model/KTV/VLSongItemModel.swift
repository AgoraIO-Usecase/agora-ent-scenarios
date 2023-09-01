//
//  VLSongItemModel.swift
//  Cantata
//
//  Created by CP on 2023/8/31.
//

import Foundation
import AgoraCommon

public class VLSongItmModel: VLBaseModel {
    @objc public var singer: String?
    @objc public var songName: String?
    @objc public var songNo: String?
    @objc public var imageUrl: String?
    @objc public var lyric: String?

    //是否被点过
    @objc public var ifChoosed: Bool = false
    @objc public var ifChorus: Bool = false
}
