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
}

class ShowRoomLiveView: UIView {
    
    var room: ShowRoomListModel? {
        didSet{
            roomInfoView.setRoomInfo(avatar: room?.ownerAvater, name: room?.roomName, id: room?.roomNo, time: "")
            countView.count = room?.roomUserCount ?? 1
        }
    }
    
    weak var delegate: ShowRoomLiveViewDelegate? {
        didSet{
            bottomBar.delegate = delegate
        }
    }
    var canvasView: UIView = UIView()
    
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
    
    private lazy var bottomBar: ShowRoomBottomBar = {
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
        tableView.transform = CGAffineTransformMakeRotation(Double.pi)
        return tableView
    }()
    
    private lazy var chatTextField: UITextField = {
        let textField = UITextField()
        textField.delegate = self
        textField.isHidden = true
        textField.font = .show_R_14
        textField.textColor = .black
        textField.returnKeyType = .send
        textField.backgroundColor = .show_main_text
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
        
        addSubview(chatTextField)
        chatTextField.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.right.equalTo(-15)
            make.height.equalTo(40)
            make.top.equalTo(tableView.snp.bottom).offset(15)
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
    }
    
    @objc private func didClickChatButton() {
        chatButton.isHidden = true
        chatTextField.isHidden = false
        chatTextField.becomeFirstResponder()
    }
    
    @objc private func didClickCloseButton() {
        delegate?.onClickCloseButton()
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

extension ShowRoomLiveView: UITextFieldDelegate {
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        textField.isHidden = true
        chatButton.isHidden = false
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if let text = textField.text?.trimmingCharacters(in: .whitespacesAndNewlines), text.count > 0 {
            delegate?.onClickSendMsgButton(text: text)
        }
        textField.text = nil
        textField.resignFirstResponder()
        return true
    }
}
