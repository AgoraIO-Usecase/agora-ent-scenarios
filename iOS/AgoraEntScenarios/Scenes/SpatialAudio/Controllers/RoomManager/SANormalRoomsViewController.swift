//
//  VRNormalRoomsViewController.swift
//  AgoraScene_iOS
//
//  Created by 朱继超 on 2022/9/14.
//

import UIKit
import ZSwiftBaseLib

public class SANormalRoomsViewController: UIViewController {
    public var didSelected: ((SARoomEntity) -> Void)?
    
    public var totalCountClosure: ((Int) -> Void)?
    
    lazy var empty: SAEmptyView = .init(frame: CGRect(x: 0, y: 10, width: ScreenWidth, height: self.view.frame.height - 10 - CGFloat(ZBottombarHeight) - 30), title: "spatial_voice_no_chat_room_yet".spatial_localized(), image: nil)
    
    lazy var roomList: SARoomListView = .init(frame: CGRect(x: 0, y: 10, width: ScreenWidth, height: self.view.frame.height - 10 - CGFloat(ZBottombarHeight) - 30), style: .plain)
    
    deinit {
        saLogger.info("deinit----- SANormalRoomsViewController")
        AppContext.unloadSaServiceImp()
    }
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        view.addSubViews([empty, roomList])
        // Do any additional setup after loading the view.
        roomListEvent()
        refresh()
        roomList.refreshControl?.addTarget(self, action: #selector(refresh), for: .valueChanged)
        NotificationCenter.default.addObserver(self, selector: #selector(refresh), name: NSNotification.Name("refreshList"), object: nil)
    }
    
    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.refresh()
    }
    
}

extension SANormalRoomsViewController {
    @objc func refresh() {
        roomList.rooms = nil
        fetchRooms(cursor: "")
    }

    @objc private func fetchRooms(cursor: String) {
        AppContext.saServiceImp().fetchRoomList(page: 0) {[weak self] error, rooms in
            guard let self = self else {return}
            self.roomList.refreshControl?.endRefreshing()
            if error == nil {
                guard let rooms = rooms else {return}
                let roomsEntity: SARoomsEntity = SARoomsEntity()
                roomsEntity.rooms = rooms
                roomsEntity.total = rooms.count
                self.fillDataSource(rooms: roomsEntity)
                self.roomList.reloadData()
                self.empty.isHidden = (rooms.count > 0)
            } else {
                self.view.makeToast("\(error?.localizedDescription ?? "")")
            }
        }
    }

    private func fillDataSource(rooms: SARoomsEntity?) {
        if roomList.rooms == nil {
            roomList.rooms = rooms
        } else {
            roomList.rooms?.total = rooms?.total
            roomList.rooms?.cursor = rooms?.cursor
            roomList.rooms?.rooms?.append(contentsOf: rooms?.rooms ?? [])
        }
    }

    private func roomListEvent() {
        roomList.didSelected = { [weak self] in
            guard let self = self else { return }
            if self.didSelected != nil { self.didSelected!($0) }
        }
        roomList.loadMore = { [weak self] in
            if self?.roomList.rooms?.total ?? 0 > self?.roomList.rooms?.rooms?.count ?? 0 {
                self?.fetchRooms(cursor: self?.roomList.rooms?.cursor ?? "")
            }
        }
    }
}
