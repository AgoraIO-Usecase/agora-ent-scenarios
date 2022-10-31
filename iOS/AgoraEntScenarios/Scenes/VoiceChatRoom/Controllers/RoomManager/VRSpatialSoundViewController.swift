//
//  VRSpatialSoundViewController.swift
//  AgoraScene_iOS
//
//  Created by 朱继超 on 2022/9/14.
//

import UIKit
import ZSwiftBaseLib

public class VRSpatialSoundViewController: UIViewController {
    
    public var didSelected: ((VRRoomEntity) -> ())?
    
    public var totalCountClosure: ((Int) -> ())?
        
    lazy var empty: VREmptyView = {
        VREmptyView(frame: CGRect(x: 0, y: 10, width: ScreenWidth, height: self.view.frame.height - 10 - CGFloat(ZBottombarHeight) - 30), title: "No Chat Room yet", image: nil)
    }()
    
    lazy var roomList: VRRoomListView = {
        VRRoomListView(frame: CGRect(x: 0, y: 10, width: ScreenWidth, height: self.view.frame.height - 10 - CGFloat(ZBottombarHeight) - 30), style: .plain)
    }()

    public override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addSubViews([self.empty,self.roomList])
        // Do any additional setup after loading the view.
        self.roomListEvent()
        self.refresh()
        self.roomList.refreshControl?.addTarget(self, action: #selector(refresh), for: .valueChanged)
        NotificationCenter.default.addObserver(self, selector: #selector(refresh), name: NSNotification.Name("refreshList"), object: nil)
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
}

extension VRSpatialSoundViewController {
    
    @objc func refresh() {
        self.roomList.rooms = nil
        self.fetchRooms(cursor: "")
    }
    
    @objc private func fetchRooms(cursor: String) {
        VoiceRoomBusinessRequest.shared.sendGETRequest(api: .fetchRoomList(cursor: cursor, pageSize: page_size,type: 1), params: [:], classType: VRRoomsEntity.self) { rooms, error in
            self.roomList.refreshControl?.endRefreshing()
            if error == nil {
                guard let total = rooms?.total else { return }
                self.fillDataSource(rooms: rooms)
                self.roomList.reloadData()
                if self.totalCountClosure != nil {
                    self.totalCountClosure!(total)
                }
                self.empty.isHidden = (total > 0)
            } else {
                self.view.makeToast("\(error?.localizedDescription ?? "")")
            }
        }
    }
    
    private func fillDataSource(rooms: VRRoomsEntity?) {
        if self.roomList.rooms == nil {
            self.roomList.rooms = rooms
        } else {
            self.roomList.rooms?.total = rooms?.total
            self.roomList.rooms?.cursor = rooms?.cursor
            self.roomList.rooms?.rooms?.append(contentsOf: rooms?.rooms ?? [])
        }
    }
    
    private func roomListEvent() {
        self.roomList.didSelected = { [weak self] in
            guard let `self` = self else { return }
            if self.didSelected != nil { self.didSelected!($0) }
        }
        self.roomList.loadMore = { [weak self] in
            if self?.roomList.rooms?.total ?? 0 > self?.roomList.rooms?.rooms?.count ?? 0 {
                self?.fetchRooms(cursor: self?.roomList.rooms?.cursor ?? "")
            }
        }
    }
}
