//
//  VRRoomListView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/24.
//

import UIKit
import ZSwiftBaseLib

public final class VRRoomListView: UITableView,UITableViewDelegate,UITableViewDataSource {
    
    public var didSelected: ((VRRoomEntity) -> ())?
    
    public var loadMore: (()->())?
    
    public var rooms: VRRoomsEntity?

    public override init(frame: CGRect, style: UITableView.Style) {
        super.init(frame: frame, style: style)
        self.delegate(self).dataSource(self).tableFooterView(UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: (150/335.0)*(ScreenWidth-40)+80))).separatorStyle(.none).backgroundColor(.clear).registerCell(VRRoomListCell.self, forCellReuseIdentifier: "VRRoomListCell")
        self.refreshControl = UIRefreshControl()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

extension VRRoomListView {

    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let height = (150/335.0)*(ScreenWidth-40)+20;
        return CGFloat(ceilf(Float(height)))
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "VRRoomListCell") as? VRRoomListCell
        if cell == nil {
            cell = VRRoomListCell(style: .default, reuseIdentifier: "VRRoomListCell")
        }
        cell?.setupViewsAttributes(room: self.rooms?.rooms?[safe: indexPath.row] ?? VRRoomEntity())
        cell?.selectionStyle = .none
        return cell ?? VRRoomListCell()
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if let item = self.rooms?.rooms?[safe: indexPath.row] as? VRRoomEntity {
            if self.didSelected != nil {
                self.didSelected!(item)
            }
        }
    }
    
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.rooms?.rooms?.count ?? 0
    }
    
    public func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        if self.loadMore != nil,(self.rooms?.rooms?.count ?? 0) - 2 == indexPath.row {
            self.loadMore!()
        }
    }
}
