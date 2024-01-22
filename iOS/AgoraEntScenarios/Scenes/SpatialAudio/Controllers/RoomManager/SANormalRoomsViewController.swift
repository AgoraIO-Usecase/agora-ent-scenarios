//
//  VRNormalRoomsViewController.swift
//  AgoraScene_iOS
//
//  Created by 朱继超 on 2022/9/14.
//

import UIKit
import ZSwiftBaseLib
import AgoraCommon
public class SANormalRoomsViewController: UIViewController {
    public var didSelected: ((SARoomEntity) -> Void)?
    
    public var totalCountClosure: ((Int) -> Void)?
    
    lazy var empty: SARoomListEmptyView = .init(frame: CGRect(x: 0, y: 120, width: ScreenWidth, height: self.view.frame.height - 10 - CGFloat(ZBottombarHeight) - 30), title: "spatial_voice_no_chat_room_yet".spatial_localized(), image: nil)
    
    lazy var roomList: SRRoomHomeListView = .init(frame: CGRect(x: 0, y: 10, width: ScreenWidth, height: self.view.frame.height - 10 - CGFloat(ZBottombarHeight) - 30))
    
    deinit {
        saLogger.info("deinit----- SANormalRoomsViewController")
        AppContext.unloadSaServiceImp()
    }
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        view.addSubViews([empty, roomList])
        // Do any additional setup after loading the view.
        refresh()
        
        roomList.refreshBlock = {[weak self] _ in
            guard let self = self else {return}
            self.refresh()
        }
        
        roomList.clickBlock = {[weak self] index in
            guard let self = self, let room = self.roomList.roomList?[index] else {return}
            if self.didSelected != nil { self.didSelected!(room) }
            self.entryRoom(room: room)
        }

    }
    
    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.refresh()
    }
    
}

extension SANormalRoomsViewController {
    @objc func refresh() {
        roomList.roomList = nil
        fetchRooms(cursor: "")
    }

    @objc private func fetchRooms(cursor: String) {
        AppContext.saServiceImp().fetchRoomList(page: 0) {[weak self] error, rooms in
            guard let self = self else {return}
            self.roomList.refreshControl.endRefreshing()
            if error == nil {
                guard let rooms = rooms?.filter({ $0.room_id != nil }) else {return}
                let roomsEntity: SARoomsEntity = SARoomsEntity()
                roomsEntity.rooms = rooms
                roomsEntity.total = rooms.count
                self.roomList.roomList = rooms
                self.empty.isHidden = (rooms.count > 0)
            } else {
                self.empty.isHidden = true
                self.view.makeToast("\(error?.localizedDescription ?? "")")
            }
        }
    }
    
    private func entryRoom(room: SARoomEntity) {
        let info: SARoomInfo = SARoomInfo()
        info.room = room
        info.mic_info = nil
        let vc = SARoomViewController(info: info)
        self.navigationController?.pushViewController(vc, animated: true)
    }
}
