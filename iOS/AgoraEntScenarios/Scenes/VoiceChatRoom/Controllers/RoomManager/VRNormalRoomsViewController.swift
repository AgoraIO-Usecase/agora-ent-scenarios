//
//  VRNormalRoomsViewController.swift
//  AgoraScene_iOS
//
//  Created by 朱继超 on 2022/9/14.
//

import UIKit
import ZSwiftBaseLib

public class VRNormalRoomsViewController: UIViewController {
    public var didSelected: ((VRRoomEntity) -> Void)?
    
    public var totalCountClosure: ((Int) -> Void)?
    
    lazy var empty: VREmptyView = .init(frame: CGRect(x: 0, y: 200, width: ScreenWidth, height: self.view.frame.height - 10 - 30), title: "voice_room_nobody", image: nil)

    lazy var roomList: VRRoomHomeListView = .init(frame: CGRect(x: 0, y: ZNavgationHeight, width: ScreenWidth, height: self.view.frame.height - 30 - ZNavgationHeight))
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .clear
        view.addSubViews([ empty,roomList])
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
        
        roomList.backBlock = {[weak self] in
            self?.navigationController?.popViewController(animated: true)
        }
    }
    
    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
       // self.navigationController?.navigationBar.isHidden = true
        self.refresh()
    }
    
    public override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
      //  self.navigationController?.navigationBar.isHidden = false
    }
    
}

extension VRNormalRoomsViewController {
    @objc func refresh() {
        roomList.roomList = nil
        fetchRooms(cursor: "")
    }

    @objc private func fetchRooms(cursor: String) {
        ChatRoomServiceImp.getSharedInstance().fetchRoomList(page: 0) { error, rooms in
            self.roomList.refreshControl.endRefreshing()
            if error == nil {
                guard let rooms = rooms else {return}
                let roomsEntity: VRRoomsEntity = VRRoomsEntity()
                roomsEntity.rooms = rooms
                roomsEntity.total = rooms.count
                self.roomList.roomList = rooms
                self.empty.isHidden = (rooms.count > 0)
            } else {
                self.empty.isHidden = true
                // self.view.makeToast("\(error?.localizedDescription ?? "")")
            }
        }
    }
    
    private func entryRoom(room: VRRoomEntity) {
        let info: VRRoomInfo = VRRoomInfo()
        info.room = room
        info.mic_info = nil
        let vc = VoiceRoomViewController(info: info)
        self.navigationController?.pushViewController(vc, animated: true)
  }

}
