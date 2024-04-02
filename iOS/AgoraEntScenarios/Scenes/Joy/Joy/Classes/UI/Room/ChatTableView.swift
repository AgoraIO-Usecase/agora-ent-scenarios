//
//  ChatTableView.swift
//  Joy
//
//  Created by wushengtao on 2023/12/4.
//

import Foundation

let kTableViewBottomOffset: CGFloat = UIDevice.current.aui_SafeDistanceBottom + 109
class ChatTableView: UIView {
    private var chatArray = [JoyMessage]()
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView()
        tableView.backgroundColor = .clear
        tableView.separatorStyle = .none
        tableView.delegate = self
        tableView.dataSource = self
        tableView.allowsSelection = false
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 24
        tableView.showsVerticalScrollIndicator = false
        return tableView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func addObserver() {
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillShowNotification, object: nil, queue: nil) { [weak self] notify in
            guard let self = self else {return}
            guard let keyboardRect = (notify.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue else { return }
            guard let duration = notify.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? TimeInterval else { return }
            let keyboradHeight = keyboardRect.size.height
            self.snp.updateConstraints { make in
                make.bottom.equalTo(-keyboradHeight)
            }
            UIView.animate(withDuration: duration) {
                self.layoutIfNeeded()
            }
        }
        
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillHideNotification, object: nil, queue: nil) {[weak self] notify in
            guard let self = self else {return}
            self.snp.updateConstraints { make in
                make.bottom.equalTo(-kTableViewBottomOffset)
            }
        }
    }
    
    func appendMessage(msg: JoyMessage) {
        chatArray.append(msg)
        tableView.reloadData()
        tableView.scrollToRow(at: IndexPath(row: chatArray.count - 1, section: 0), at: .bottom, animated: true)
    }
}

extension ChatTableView: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return chatArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellID = NSStringFromClass(RoomChatCell.self)
        var cell = tableView.dequeueReusableCell(withIdentifier: cellID) as? RoomChatCell
        if cell == nil {
            cell = RoomChatCell(style: .default, reuseIdentifier: cellID)
        }
        let chatModel = chatArray[indexPath.row]
        cell?.setUserName(chatModel.userName ?? "", msg: chatModel.message ?? "")
        return cell!
    }
}
