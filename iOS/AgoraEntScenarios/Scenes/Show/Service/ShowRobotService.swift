//
//  ShowRobotService.swift
//  AgoraEntScenarios
//
//  Created by Jonathan on 2023/7/12.
//

import Foundation

// 机器人相关
class ShowRobotService {
    
    static let shared = ShowRobotService()
    
    private let kRobotRoomStartId = 2023000
    private let kRobotUid = 2000000001

    private let robotStreamURL = [
        "https://download.agora.io/sdk/release/agora_test_video_10.mp4",
        "https://download.agora.io/sdk/release/agora_test_video_10.mp4",
        "https://download.agora.io/sdk/release/agora_test_video_10.mp4",
    ]
    private let robotRoomIds = ["1", "2", "3"]
    private let robotRoomOwnerHeaders = [
        "https://download.agora.io/demo/release/bot1.png"
    ]
    // 开启机器人
    public func startCloudPlayers() {
        robotRoomIds.forEach { robotId in
            let roomId = robotRoomId(from: robotId)
            let idx = ((Int(roomId) ?? 1) - kRobotRoomStartId - 1) % robotStreamURL.count
            NetworkManager.shared.startCloudPlayer(channelName: roomId,
                                                   uid: VLUserCenter.user.id,
                                                   robotUid: UInt(kRobotUid),
                                                   streamUrl: robotStreamURL[idx]) { msg in
                guard let _ = msg else {return}
            }
        }
    }
    
    public func generateRobotRoomsAppend(rooms: [ShowRoomListModel]) -> [ShowRoomListModel] {
        var dataArray = rooms
        var robotIds = robotRoomIds
        dataArray.forEach { room in
            let roomId = (Int(room.roomId) ?? 0) - kRobotRoomStartId
            guard roomId > 0, let idx = robotIds.firstIndex(of: "\(roomId)") else{
                return
            }
            robotIds.remove(at: idx)
        }
        //create fake room
        robotIds.forEach { robotId in
            let room = ShowRoomListModel()
            let userId = "\(kRobotUid)"
            room.roomName = "Smooth \(robotId)"
            room.roomId = robotRoomId(from: robotId)
            room.thumbnailId = "1"
            room.ownerId = userId
            room.ownerName = userId
            room.ownerAvatar = robotRoomOwnerHeaders[((Int(robotId) ?? 1) - 1) % robotRoomOwnerHeaders.count]//VLUserCenter.user.headUrl
            room.createdAt = Date().millionsecondSince1970()
            dataArray.append(room)
        }
        return dataArray
    }
    // 生成机器人房间id
    private func robotRoomId(from robotId: String) -> String {
        return "\((Int(robotId) ?? 1) + kRobotRoomStartId)"
    }
    
}
