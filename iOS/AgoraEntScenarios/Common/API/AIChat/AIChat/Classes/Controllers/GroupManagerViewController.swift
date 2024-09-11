//
//  GroupManagerViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/11.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage
import AgoraCommon
import AgoraChat

class GroupManagerViewController: UIViewController {

    private var items = [AIChatGroupUserProfileProtocol]()
    
    lazy var background: UIImageView = {
        UIImageView(frame: self.view.bounds).image(UIImage(named: "roomList", in: .chatAIBundle, with: nil)!).contentMode(.scaleAspectFill)
    }()
        
    lazy var navigation: AIChatNavigation = {
        AIChatNavigation(showLeftItem: true,textAlignment: .left,rightImages: [UIImage(named: "more", in: .chatAIBundle, with: nil)!]).backgroundColor(.clear)
    }()
    
    lazy var nameTextField: UITextField = {
        UITextField(frame: CGRect(x: 20, y: self.navigation.frame.maxY+16, width: self.view.frame.width-40, height: 48)).delegate(self).backgroundColor(.white).placeholder("请输入群组名称").font(.systemFont(ofSize: 16)).clearButtonMode(.whileEditing).isEnabled(false)
    }()
    
    lazy var leftContainer: UIView = {
        UIView {
            UIView(frame: CGRect(x: 0, y: 0, width: 96, height: 48)).backgroundColor(.clear)
            UIButton(type: .custom).frame(CGRect(x: 20, y: 0, width: 68, height: 48)).title("群组名称", .normal).textColor(UIColor(0x303553), .normal).isUserInteractionEnabled(false).font(.systemFont(ofSize: 16))
            UIView(frame: CGRect(x: 91, y: 13.5, width: 1, height: 20)).backgroundColor(UIColor(0x979CBB))
        }
    }()
    
    lazy var rightContainer: UIView = {
        UIView {
            UIView(frame: CGRect(x: 0, y: 0, width: 50, height: 48)).backgroundColor(.clear)
            self.editButton
        }
    }()
    
    lazy var editButton: UIButton = {
        UIButton(type: .custom).frame( CGRect(x: 0, y: 0, width: 40, height: 48)).title("编辑", .normal).textColor(UIColor(0x009dff), .normal).font(.systemFont(ofSize: 16)).backgroundColor(.clear).addTargetFor(self, action: #selector(editAction), for: .touchUpInside)
    }()
    
    lazy var alertLabel: UILabel = {
        UILabel(frame: CGRect(x: 28, y: self.nameTextField.frame.maxY+32, width: self.view.frame.width-56, height: 14)).attributedText(NSAttributedString {
            AttributedText("群聊伙伴  ").font(.systemFont(ofSize: 12, weight: .medium)).foregroundColor(Color(0x303553))
            AttributedText("最多可以添加5个智能体").font(.systemFont(ofSize: 12)).foregroundColor(Color(0x979CBB))
        })
    }()
    
    lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        let itemWidth = floor((self.view.frame.width - 40) / 4.0)
        layout.itemSize = CGSize(width: itemWidth, height: 92) // 头像加文字的总大小
        layout.minimumInteritemSpacing = 0.1 // 项目之间的间距
        layout.minimumLineSpacing = 10 // 行之间的间距
        layout.sectionInset = .zero
        
        let collectionView = UICollectionView(frame: CGRect(x: 20, y: self.alertLabel.frame.maxY+20, width: self.view.frame.width-40, height: 388), collectionViewLayout: layout).backgroundColor(.clear)
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.register(IntelligenceCell.self, forCellWithReuseIdentifier: "IntelligenceCell")
        
        return collectionView
    }()
    
    private var groupId = ""
    
    private var service = AIChatBotImplement()
    
    public required init(groupId: String) {
        self.groupId = groupId
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addSubViews([self.background, self.navigation,self.nameTextField,self.alertLabel,self.collectionView])
        self.setupUI()
        
        // Do any additional setup after loading the view.
        self.navigation.clickClosure = { [weak self] type,_ in
            if type == .back {
                self?.pop()
            }
        }
    }
    
    private func pop() {
        if self.navigationController != nil {
            self.navigationController?.popViewController(animated: true)
        } else {
            self.dismiss(animated: true)
        }
    }
    
    func setupUI() {
        self.navigation.leftItem.setImage(UIImage(systemName: "chevron.backward")?.withTintColor(.black, renderingMode: .alwaysOriginal), for: .normal)
        self.navigation.title = "群管理"
       
        self.nameTextField.leftView = self.leftContainer
        self.nameTextField.leftViewMode = .always
        self.nameTextField.rightView = self.rightContainer
        self.nameTextField.rightViewMode = .always
        self.nameTextField.cornerRadius(16)
        self.fillItems()
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.view.endEditing(true)
    }
    
    func fillItems() {
        self.items.removeAll()
        self.items.append(contentsOf: [
            AIChatGroupUserProfile(id: VLUserCenter.user.id, name: VLUserCenter.user.name, avatar: VLUserCenter.user.headUrl, type: .normal),
            AIChatGroupUserProfile(id: "6", name: "添加智能体", avatar: "", type: .add)
        ])
        self.collectionView.reloadData()
    }
    
    
    private func createConversation(userId: String) {
        let conversation = AgoraChatClient.shared().chatManager?.getConversation(userId, type: .chat, createIfNotExist: true)
        let timeMessage = AgoraChatMessage(conversationID: userId, body: AgoraChatCustomMessageBody(event: "AIChat_alert_message", customExt: nil), ext: ["something":"\(UInt64(Date().timeIntervalSince1970*1000))"])
        conversation?.insert(timeMessage, error: nil)
        let alertMessage = AgoraChatMessage(conversationID: userId, body: AgoraChatCustomMessageBody(event: "AIChat_alert_message", customExt: nil), ext: ["something":"群组 \(self.nameTextField.text ?? "") 创建成功"])
        conversation?.insert(alertMessage, error: nil)
        let bot = AIChatBotProfile()
        bot.botId = userId
        bot.botName = self.nameTextField.text ?? ""
        if let botIcon = self.items.filter{$0.type == .normal}.first?.avatar {
            bot.botIcon = VLUserCenter.user.headUrl+","+botIcon
        }
        DispatchQueue.main.async {
            self.dismiss(animated: false) {
                let chatVC = AIChatViewController(bot: bot, type: .group)
                UIViewController.currentController?.navigationController?.pushViewController(chatVC, animated: true)
            }
        }
    }

    @objc private func editAction() {
        let vc = GroupNameEditViewController { [weak self] text in
            self?.updateGroupName(text: text)
        }
        self.present(vc, animated: true)
    }
    
    private func updateGroupName(text: String) {
        self.service.updateGroupName(groupId: self.groupId, groupName: text) { [weak self] error in
            if error != nil {
                ToastView.show(text: "修改群名称失败")
            } else {
                ToastView.show(text: "修改群名称成功")
                DispatchQueue.main.async {
                    self?.nameTextField.text = text
                }
            }
        }
    }
    
    private func updateGroupMembers(userIds: [String]) {
        self.service.updateGroupMembers(groupId: self.groupId, botIds: userIds) { error in
            if error != nil {
                ToastView.show(text: "修改群成员失败")
            } else {
                ToastView.show(text: "修改群成员成功")
            }
        }
    }
}

extension GroupManagerViewController: UITextFieldDelegate {
    
}

extension GroupManagerViewController: UICollectionViewDataSource,UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.items.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "IntelligenceCell", for: indexPath) as! IntelligenceCell
        if let item = self.items[safe: indexPath.row] {
            cell.refresh(item: item)
            cell.removeBadge.isHidden = true
        }
        return cell
    }
    
    // MARK: UICollectionView Delegate
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        if let item = self.items[safe: indexPath.row] {
            switch item.type {
            case .add: self.addUser()
            case .remove: self.removeUser()
            case .normal:
                break
            }
        }
    }
        
    private func addUser() {
        let vc = GroupAddBotViewController(userIds: self.items.map({ $0.id })) { [weak self] items in
            self?.processAddBots(items: items)
        }
        self.present(vc, animated: true)
    }
    
    private func processAddBots(items: [AIChatBotProfileProtocol]) {
        for item in items {
            if item.selected {
                self.items.insert(AIChatGroupUserProfile(id: item.botId, name: item.botName, avatar: item.botIcon, type: .normal), at: 1)
            }
        }
        self.collectionView.reloadData()
        self.updateUsers()
    }
    
    private func removeUser() {
        let vc = GroupRemoveBotViewController(profileIds: self.items.map({ $0.id })) { [weak self] items in
            self?.processRemoveBots(items: items)
        }
        
        self.present(vc, animated: true)
    }
    
    private func processRemoveBots(items: [AIChatBotProfileProtocol]) {
        for item in items {
            if item.selected {
                if let index = self.items.firstIndex(where: { $0.id == item.botId }) {
                    self.items.remove(at: index)
                }
            }
        }
        self.collectionView.reloadData()
        self.updateUsers()
    }
    
    private func updateUsers() {
        var ids = [String]()
        for item in self.items {
            if item.type == .normal {
                ids.append(item.id)
            }
        }
        self.updateGroupMembers(userIds: ids)
    }
}
