//
//  ShowLiveView.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/8.
//

import UIKit

protocol ShowRoomLiveViewDelegate: ShowRoomBottomBarDelegate {
    func onClickSendMsgButton()
    func onClickCloseButton()
}

class ShowRoomLiveView: UIView {
    
    weak var delegate: ShowRoomLiveViewDelegate? {
        didSet{
            bottomBar.delegate = delegate
        }
    }
    var canvasView: UIView = UIView()

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
        let view = ShowRoomBottomBar()
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
        return tableView
    }()
    
    private lazy var chatTextField: UITextField = {
        let textField = UITextField()
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
    
    override init(frame: CGRect) {
        super.init(frame: frame)
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
            make.right.equalTo(15)
            make.height.equalTo(40)
            make.top.equalTo(tableView.snp.bottom).offset(20)
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
        chatTextField.becomeFirstResponder()
    }
    
    @objc private func didClickCloseButton() {
        delegate?.onClickCloseButton()
    }

}


extension ShowRoomLiveView: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 10
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cellID = "ShowRoomChatCell"
        var cell = tableView.dequeueReusableCell(withIdentifier: cellID) as? ShowRoomChatCell
        if cell == nil {
            cell = ShowRoomChatCell(style: .default, reuseIdentifier: cellID)
        }
        cell?.setUserName("撒量较大", msg: "hello everyone")
        return cell!
    }
}
