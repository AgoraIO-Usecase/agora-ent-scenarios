//
//  CantataMainViewController.swift
//  Cantata
//
//  Created by CP on 2023/8/29.
//

import Foundation
import AgoraCommon

@objcMembers
class CantataMainViewController: BaseViewController {
    
    @objc public var roomModel: VLRoomListModel?
    @objc public var seatsArray: [VLRoomSeatModel]?

    private var topView: VLKTVTopView!
    override func viewDidLoad() {
        super.viewDidLoad()
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        
    }
    
}

//订阅消息模块
extension CantataMainViewController {
    func subscribeServiceEvent() {
        let weakSelf = self
        AppContext.ktvServiceImp()?.unsubscribeAll()
        
        AppContext.ktvServiceImp()?.subscribeUserListCountChanged {[weak self] count in
            // TODO
            self?.setRoomUsersCount(count)
        }
        
        AppContext.ktvServiceImp()?.subscribeSeatListChanged { status, seatModel in
            AgoraEntAuthorizedManager.checkMediaAuthorized(parent: weakSelf) { granted in
                guard granted else { return }
                guard let model = weakSelf.getUserSeatInfo(withIndex: seatModel.seatIndex) else {
                    assertionFailure("model == nil")
                    return
                }
                
                if status == .created || status == .updated {
                    // 上麦消息 / 是否打开视频 / 是否静音
                    model.reset(with: seatModel)
                    weakSelf.setSeatsArray(weakSelf.seatsArray)
                } else if status == .deleted {
                    // 下麦消息
                    
                    // 下麦重置占位模型
                    model.reset(with: nil)
                    weakSelf.setSeatsArray(weakSelf.seatsArray)
                }
                
                let song = weakSelf.selSongsArray.first
                weakSelf.MVView.updateUI(withSong: song, role: weakSelf.singRole)
                weakSelf.roomPersonView.reloadSeatIndex(model.seatIndex)
                
                weakSelf.onSeatFull()
            }
        }
        
        AppContext.ktvServiceImp.subscribeRoomStatusChanged { status, roomInfo in
            if status == .updated {
                // 切换背景
                
                // mv bg / room member count did changed
                let selBgModel = VLKTVSelBgModel()
                selBgModel.imageName = "ktv_mvbg\(roomInfo.bgOption)"
                selBgModel.isSelect = true
                weakSelf.choosedBgModel = selBgModel
            } else if status == .deleted {
                // 房主关闭房间
                if roomInfo.creatorNo == VLUserCenter.user.id {
                    let mes = "连接超时，房间已解散"
                    VLKTVAlert.shared.showKTVToast(withFrame: UIScreen.main.bounds, image: UIImage.sceneImage(withName: "empty"), message: mes, buttonTitle: KTVLocalizedString("ktv_confirm")) { flag, text in
                        VLKTVAlert.shared.dismiss()
                        weakSelf.leaveRoom()
                    }
                    return
                }
                
                weakSelf.popForceLeaveRoom()
            }
        }
        
        // callback if choose song list did changed
        AppContext.ktvServiceImp.subscribeChooseSongChanged { status, songInfo, songArray in
            // update in-ear monitoring
            weakSelf._checkInEarMonitoring()
            
            if status == .deleted {
                let success = weakSelf.removeSelSong(withSongNo: Int(songInfo.songNo)!, sync: false)
                if !success {
                    weakSelf.selSongsArray = songArray
                    KTVLogInfo("removeSelSongWithSongNo fail, reload it")
                }
                // 清除合唱者总分
                weakSelf.coSingerDegree = 0
            } else {
                let song = weakSelf.selSong(withSongNo: songInfo.songNo)
                // add new song
                KTVLogInfo("song did updated: \(song.name) status: \(songInfo.status)")
                weakSelf.selSongsArray = NSMutableArray(array: songArray)
            }
        }
        
        AppContext.ktvServiceImp()?.subscribeNetworkStatusChanged { status in
            if status != .open {
                // [VLToast toast:[NSString stringWithFormat:@"network changed: %ld", status]]
                return
            }
            weakSelf.subscribeServiceEvent()
            weakSelf._fetchServiceAllData()
        }
        
        AppContext.ktvServiceImp.subscribeRoomWillExpire {
            let isOwner = weakSelf.roomModel.creatorNo == VLUserCenter.user.id
            let mes = isOwner ? "您已体验超过20分钟，当前房间已过期，请退出重新创建房间" : "当前房间已过期,请退出"
            VLKTVAlert.shared.showKTVToast(withFrame: UIScreen.main.bounds, image: UIImage.sceneImage(withName: "empty"), message: mes, buttonTitle: KTVLocalizedString("ktv_confirm")) { flag, text in
                VLKTVAlert.shared.dismiss()
                weakSelf.leaveRoom()
            }
        }
    }
    
    func setRoomUsersCount(_ userCount: UInt) {
        if let roomModel = self.roomModel {
            self.roomModel!.roomPeopleNum = String(userCount)
        }
        self.topView.listModel = roomModel
    }

}

//加载RTC模块
extension CantataMainViewController {
    
}
