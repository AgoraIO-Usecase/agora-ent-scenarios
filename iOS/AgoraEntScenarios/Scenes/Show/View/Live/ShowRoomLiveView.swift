//
//  ShowLiveView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/8.
//

import UIKit

protocol ShowRoomLiveViewDelegate: ShowRoomBottomBarDelegate {
    func onClickSendMsgButton(text: String)
    func onClickCloseButton()
    func onClickRemoteCanvas()
}

class ShowRoomLiveView: UIView {
    var roomUserCount: Int = 1 {
        didSet {
            countView.count = roomUserCount
        }
    }
    
    var room: ShowRoomListModel? {
        didSet{
            roomInfoView.setRoomInfo(avatar: room?.ownerAvater, name: room?.roomName, id: room?.roomId, time: room?.createdAt)
            guard let count = room?.roomUserCount else {
                roomUserCount = 1
                return
            }
            self.roomUserCount = count
        }
    }
    
    weak var delegate: ShowRoomLiveViewDelegate? {
        didSet{
            bottomBar.delegate = delegate
        }
    }
    lazy var canvasView: ShowCanvasView = {
        let view = ShowCanvasView()
        view.onTapRemoteCanvasClosure = { [weak self] in
            self?.delegate?.onClickRemoteCanvas()
        }
        return view
    }()
    
    private var chatArray = [ShowChatModel]()
    
    private lazy var roomInfoView: ShowRoomInfoView = {
        let roomInfoView = ShowRoomInfoView()
        return roomInfoView
    }()
    
    private lazy var countView: ShowRoomMembersCountView = {
        let countView = ShowRoomMembersCountView()
        return countView
    }()
    
    private lazy var closeButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.show_sceneImage(name: "show_live_close"), for: .normal)
        button.addTarget(self, action: #selector(didClickCloseButton), for: .touchUpInside)
        return button
    }()
    
    lazy var bottomBar: ShowRoomBottomBar = {
        let view = ShowRoomBottomBar(isBroadcastor: isBroadcastor)
        return view
    }()
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView()
        tableView.backgroundColor = .clear
        tableView.separatorStyle = .none
        tableView.delegate = self
        tableView.dataSource = self
        tableView.allowsSelection = false
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 46
        tableView.showsVerticalScrollIndicator = false
        tableView.transform = CGAffineTransform(rotationAngle: Double.pi)
        return tableView
    }()
    
    private lazy var chatInputView: ShowChatInputView = {
        let textField = ShowChatInputView()
        textField.isHidden = true
        textField.delegate = self
        return textField
    }()
    
    private lazy var chatButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setBackgroundImage(UIImage.show_sceneImage(name: "show_live_chat"), for: .normal)
        button.setTitle("create_live_chat_title".show_localized, for: .normal)
        button.titleLabel?.font = UIFont.show_R_12
        button.addTarget(self, action: #selector(didClickChatButton), for: .touchUpInside)
        return button
    }()
    
    private var isBroadcastor = false
    
    init(isBroadcastor: Bool = false) {
        super.init(frame: .zero)
        self.isBroadcastor = isBroadcastor
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        
        addSubview(canvasView)
        canvasView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        addSubview(roomInfoView)
        roomInfoView.snp.makeConstraints { make in
            make.top.equalTo(44)
            make.left.equalTo(15)
        }
        
        addSubview(countView)
        countView.snp.makeConstraints { make in
            make.centerY.equalTo(roomInfoView)
            make.right.equalTo(-57)
        }
        
        addSubview(closeButton)
        closeButton.snp.makeConstraints { make in
            make.right.equalTo(-15)
            make.centerY.equalTo(roomInfoView)
        }
        
        addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.bottom.equalTo(-143)
            make.right.equalTo(-70)
            make.height.equalTo(168)
        }
    
        addSubview(chatButton)
        chatButton.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.bottom.equalTo(-38)
        }
        
        addSubview(bottomBar)
        bottomBar.snp.makeConstraints { make in
            make.centerY.equalTo(chatButton)
            make.right.equalTo(-15)
        }
        
        addSubview(chatInputView)
        chatInputView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.height.equalTo(56)
            make.bottom.equalToSuperview()
        }
    }
    
    @objc private func didClickChatButton() {
        chatInputView.isHidden = false
        bottomBar.isHidden = true
        chatButton.isHidden = true
        chatInputView.textField.becomeFirstResponder()
    }
    
    @objc private func didClickCloseButton() {
        delegate?.onClickCloseButton()
    }
    
    private func sendMessage(){
        
    }

}

extension ShowRoomLiveView {
    func addChatModel(_ chatModel: ShowChatModel) {
        chatArray.insert(chatModel, at: 0)
        tableView.reloadData()
        tableView.scrollToTop()
    }
}

extension ShowRoomLiveView: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return chatArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cellID = "ShowRoomChatCell"
        var cell = tableView.dequeueReusableCell(withIdentifier: cellID) as? ShowRoomChatCell
        if cell == nil {
            cell = ShowRoomChatCell(style: .default, reuseIdentifier: cellID)
        }
        let chatModel = chatArray[indexPath.row]
        cell?.setUserName(chatModel.userName, msg: chatModel.text)
        return cell!
    }
}

extension ShowRoomLiveView: ShowChatInputViewDelegate {
    
    func onEndEditing() {
        chatInputView.isHidden = true
        bottomBar.isHidden = false
        chatButton.isHidden = false
    }

    func onClickEmojiButton() {
        
    }
    
    func onClickSendButton(text: String) {
        if text.count > 80 {
            ToastView.show(text: "show_live_chat_text_length_beyond_bounds".show_localized)
            return
        }
        delegate?.onClickSendMsgButton(text: text)
    }
    
}
