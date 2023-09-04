//
//  CantataMainViewController.swift
//  Cantata
//
//  Created by CP on 2023/8/29.
//

import Foundation
import AgoraCommon

@objcMembers
public class CantataMainViewController: BaseViewController {
    
    @objc public var roomModel: VLRoomListModel?
    @objc public var seatsArray: [VLRoomSeatModel]?
    private var chorusMicView: ChorusMicView!
    private var topView: VLKTVTopView!
    private var botView: VLBottomView!
    private var lrcControlView: DHCLRCControl!
    public override func viewDidLoad() {
        super.viewDidLoad()
        layoutUI()
    }

    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

    }

    public override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

    }

    public override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

    }

    public override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)

    }

}

extension CantataMainViewController {
    private func layoutUI() {
        /**
         1.头部布局
         2.歌词组件
         3.麦位
         4.底部点歌和麦克风
         */
        
        view.backgroundColor = .white
        
        let bgView = UIImageView(frame: self.view.bounds)
        bgView.image = UIImage.sceneImage(name: "dhc_main_bg", bundleName: "DHCResource")
        view.addSubview(bgView)
        
        //头部布局
        topView = VLKTVTopView(frame: CGRect(x: 0, y: ZStatusBarHeight, width: ScreenWidth, height: 60), withDelegate: self)
        view.addSubview(topView)

        let mainBgView = UIImageView(frame: CGRect(x: 0, y: topView.frame.maxY, width: ScreenWidth, height: 520))
        mainBgView.contentMode = .scaleAspectFill
        mainBgView.image = UIImage.sceneImage(name: "dhc_bg", bundleName: "DHCResource")
        view.addSubview(mainBgView)
        
        lrcControlView = DHCLRCControl(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 520))
        lrcControlView.delegate = self
        lrcControlView.backgroundColor = .clear
        mainBgView.addSubview(lrcControlView)
        
        //麦位
        let chorusMicView = ChorusMicView(frame: CGRect(x: 0, y: 140, width: ScreenWidth, height: 320), topMicCount: 8)
        mainBgView.addSubview(chorusMicView)
        chorusMicView.backgroundColor = .clear
        
        botView = VLBottomView(frame: CGRect(x: 0, y: ScreenHeight - CGFloat(ZBottombarHeight) - 50, width: ScreenWidth, height: 50))
        botView.delegate = self
        view.addSubview(botView)
    }
}

//订阅消息模块
extension CantataMainViewController {
//    func subscribeServiceEvent() {
//        AppContext.ktvServiceImp()?.unsubscribeAll()
//
//        AppContext.ktvServiceImp()?.subscribeUserListCountChanged {[weak self] count in
//            // TODO
//            self?.setRoomUsersCount(count)
//        }
//
//        AppContext.ktvServiceImp()?.subscribeSeatListChanged {[weak self] status, seatModel in
//            guard let self = self else {return}
//            AgoraEntAuthorizedManager.checkMediaAuthorized(parent: self) { granted in
//                guard granted else { return }
//                guard let model = self.getUserSeatInfo(with: UInt(seatModel.seatIndex)) else {
//                    assertionFailure("model == nil")
//                    return
//                }
//
//                if status == .created || status == .updated {
//                    // 上麦消息 / 是否打开视频 / 是否静音
//                    model.reset(with: seatModel)
//                    self.setSeatsArray(self.seatsArray)
//                } else if status == .deleted {
//                    // 下麦消息
//
//                    // 下麦重置占位模型
//                    model.reset(with: nil)
//                    self.setSeatsArray(self.seatsArray)
//                }
//
//                let song = self.selSongsArray.first
//                self.MVView.updateUI(withSong: song, role: self.singRole)
//                self.roomPersonView.reloadSeatIndex(model.seatIndex)
//
//                self.onSeatFull()
//            }
//        }
//
//        AppContext.ktvServiceImp.subscribeRoomStatusChanged { status, roomInfo in
//            if status == .updated {
//                // 切换背景
//
//                // mv bg / room member count did changed
//                let selBgModel = VLKTVSelBgModel()
//                selBgModel.imageName = "ktv_mvbg\(roomInfo.bgOption)"
//                selBgModel.isSelect = true
//                weakSelf.choosedBgModel = selBgModel
//            } else if status == .deleted {
//                // 房主关闭房间
//                if roomInfo.creatorNo == VLUserCenter.user.id {
//                    let mes = "连接超时，房间已解散"
//                    VLKTVAlert.shared.showKTVToast(withFrame: UIScreen.main.bounds, image: UIImage.sceneImage(withName: "empty"), message: mes, buttonTitle: KTVLocalizedString("ktv_confirm")) { flag, text in
//                        VLKTVAlert.shared.dismiss()
//                        weakSelf.leaveRoom()
//                    }
//                    return
//                }
//
//                weakSelf.popForceLeaveRoom()
//            }
//        }
//
//        // callback if choose song list did changed
//        AppContext.ktvServiceImp.subscribeChooseSongChanged { status, songInfo, songArray in
//            // update in-ear monitoring
//            weakSelf._checkInEarMonitoring()
//
//            if status == .deleted {
//                let success = weakSelf.removeSelSong(withSongNo: Int(songInfo.songNo)!, sync: false)
//                if !success {
//                    weakSelf.selSongsArray = songArray
//                    KTVLogInfo("removeSelSongWithSongNo fail, reload it")
//                }
//                // 清除合唱者总分
//                weakSelf.coSingerDegree = 0
//            } else {
//                let song = weakSelf.selSong(withSongNo: songInfo.songNo)
//                // add new song
//                KTVLogInfo("song did updated: \(song.name) status: \(songInfo.status)")
//                weakSelf.selSongsArray = NSMutableArray(array: songArray)
//            }
//        }
//
//        AppContext.ktvServiceImp()?.subscribeNetworkStatusChanged { status in
//            if status != .open {
//                // [VLToast toast:[NSString stringWithFormat:@"network changed: %ld", status]]
//                return
//            }
//            weakSelf.subscribeServiceEvent()
//            weakSelf._fetchServiceAllData()
//        }
//
//        AppContext.ktvServiceImp.subscribeRoomWillExpire {
//            let isOwner = weakSelf.roomModel.creatorNo == VLUserCenter.user.id
//            let mes = isOwner ? "您已体验超过20分钟，当前房间已过期，请退出重新创建房间" : "当前房间已过期,请退出"
//            VLKTVAlert.shared.showKTVToast(withFrame: UIScreen.main.bounds, image: UIImage.sceneImage(withName: "empty"), message: mes, buttonTitle: KTVLocalizedString("ktv_confirm")) { flag, text in
//                VLKTVAlert.shared.dismiss()
//                weakSelf.leaveRoom()
//            }
//        }
//    }
//
//    func setRoomUsersCount(_ userCount: UInt) {
//        if let model = self.roomModel {
//            self.roomModel!.roomPeopleNum = String(userCount)
//            self.topView.listModel = model
//        }
//    }
//
//    /// 根据麦位索引获取麦位
//    /// - Parameter seatIndex: 麦位索引
//    /// - Returns: 对应的麦位信息模型
//    func getUserSeatInfo(with seatIndex: UInt) -> VLRoomSeatModel? {
//        guard let seatsArray = self.seatsArray else {return nil}
//        for model in seatsArray {
//            if model.seatIndex == seatIndex {
//                return model
//            }
//        }
//        return nil
//    }

}

//加载RTC模块
extension CantataMainViewController {
    
}

extension CantataMainViewController: DHCGameDelegate {
    public func didGameEventChanged(with event: DHCGameEvent) {
        
    }
}

//头部视图代理
extension CantataMainViewController {
    public func onVLKTVTopView(_ view: VLKTVTopView, closeBtnTapped sender: Any) {

    }

    public func onVLKTVTopView(_ view: VLKTVTopView, moreBtnTapped sender: Any) {

    }
}

//底部视图代理
extension CantataMainViewController: VLBottomViewDelegate {
    public func didBottomChooseSong() {
        
    }
    
    public func didBottomViewAudioStateChangeTo(enable: Bool) {
        
    }
}
