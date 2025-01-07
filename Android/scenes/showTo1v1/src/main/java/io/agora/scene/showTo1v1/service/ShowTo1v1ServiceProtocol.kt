package io.agora.scene.showTo1v1.service

enum class ShowTo1v1ServiceNetworkStatus {
    Connecting, // Connecting
    Open,       // Opened
    Fail,       // Failed
    Closed,     // Closed
}

interface ShowTo1v1ServiceListenerProtocol {

    // Network status change callback
    fun onNetworkStatusChanged(status: ShowTo1v1ServiceNetworkStatus)

    // User list change
    fun onUserListDidChanged(userNum: Int)

    // Room destroyed
    fun onRoomDidDestroy(roomId: String)

    // Room time expired
    fun onRoomTimeUp()
}

interface ShowTo1v1ServiceProtocol {

    // Create room
    fun createRoom(roomName: String, completion: (error: Exception?, roomInfo: ShowTo1v1RoomInfo?) -> Unit)

    // Join room
    fun joinRoom(roomInfo: ShowTo1v1RoomInfo, completion: (error: Exception?) -> Unit)

    // Leave room
    fun leaveRoom(roomInfo: ShowTo1v1RoomInfo, completion: (error: Exception?) -> Unit)

    // Get room list
    fun getRoomList(completion: (error: Exception?, roomList: List<ShowTo1v1RoomInfo>) -> Unit)

    // Subscribe to listener
    fun subscribeListener(listener: ShowTo1v1ServiceListenerProtocol)

    // Release resources
    fun reset()
}