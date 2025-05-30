//
//  AUIMusicModel.swift
//  AUIKitCore
//
//  Created by FanPengpeng on 2023/8/1.
//

import UIKit

public typealias AUIMusicListCompletion = (Error?, [AUIMusicModel]?)->()
public typealias AUIChooseSongListCompletion = (Error?, [AUIChooseMusicModel]?)->()
public typealias AUILoadSongCompletion = (Error?, String?, String?)->()

@objc public enum AUIPlayStatus: Int {
    case idle = 0      //待播放
    case playing       //播放中
}

@objcMembers
open class AUIMusicModel: NSObject {
    public var songCode: String = ""     //歌曲id，mcc则对应songCode
    public var name: String = ""         //歌曲名称
    public var singer: String = ""       //演唱者
    public var poster: String = ""       //歌曲封面海报
//    public var releaseTime: String = ""  //发布时间
    public var duration: Int = 0         //歌曲长度，单位秒
    public var musicUrl: String = ""     //歌曲url，mcc则为空
    public var lrcUrl: String = ""       //歌词url，mcc则为空
}

@objcMembers
open class AUIChooseMusicModel: AUIMusicModel {
    public var owner: AUIUserThumbnailInfo?          //点歌用户
    public var pinAt: Int64 = 0                      //置顶歌曲时间，与19700101的时间差，单位ms，为0则无置顶操作
    public var createAt: Int64 = 0                   //点歌时间，与19700101的时间差，单位ms
    public var playStatus: AUIPlayStatus {    //播放状态
        AUIPlayStatus(rawValue: status) ?? .idle
    }
    
    @objc public var status: Int = 0
    
    class func modelContainerPropertyGenericClass() -> NSDictionary {
        return [
            "owner": AUIUserThumbnailInfo.self
        ]
    }
    
    //做歌曲变化比较用
    public static func == (lhs: AUIChooseMusicModel, rhs: AUIChooseMusicModel) -> Bool {
        aui_info("\(lhs.name)-\(rhs.name)   \(lhs.pinAt)-\(rhs.pinAt)", tag: "AUIChooseMusicModel")
        if lhs.songCode != rhs.songCode {
            return false
        }
            
        if lhs.musicUrl != rhs.musicUrl {
            return false
        }
            
        if lhs.lrcUrl != rhs.lrcUrl {
            return false
        }
            
        if lhs.owner?.userId ?? "" != rhs.owner?.userId ?? "" {
            return false
        }
            
        if lhs.pinAt != rhs.pinAt {
            return false
        }
            
        if lhs.createAt != rhs.createAt {
            return false
        }
            
        if lhs.playStatus.rawValue != rhs.playStatus.rawValue {
            return false
        }
        
        return true
    }
}


@objc public enum AUIMicSeatViewLayoutType: UInt {
    case one = 1
    case six
    case eight
    case nine
}
