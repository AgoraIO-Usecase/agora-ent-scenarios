//
//  CreateIntelligentsGroupViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/9.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage
import AgoraCommon
import AgoraChat

class CreateIntelligentGroupViewController: UIViewController {
    
    private var items = [AIChatGroupUserProfileProtocol]()
    
    lazy var background: UIImageView = {
        UIImageView(frame: self.view.bounds).image(UIImage(named: "roomList", in: .chatAIBundle, with: nil)!).contentMode(.scaleAspectFill)
    }()
        
    lazy var navigation: AIChatNavigation = {
        AIChatNavigation(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: NavigationHeight), textAlignment: .center, rightTitle: nil).backgroundColor(.clear)
    }()
    
    lazy var nameTextField: UITextField = {
        UITextField(frame: CGRect(x: 20, y: self.navigation.frame.maxY+16, width: self.view.frame.width-40, height: 48)).delegate(self).backgroundColor(.white).placeholder(
            "aichat_create_group_name_placeholder".toSceneLocalization() as String).font(.systemFont(ofSize: 16)).clearButtonMode(.whileEditing)
    }()
    
    lazy var leftContainer: UIView = {
        UIView {
            UIView(frame: CGRect(x: 0, y: 0, width: 117, height: 40)).backgroundColor(.clear)
            UIButton(type: .custom).frame(CGRect(x: 15, y: 0, width: 77, height: 40)).title("aichat_create_group_name".toSceneLocalization() as String, .normal).textColor(UIColor(0x303553), .normal).isUserInteractionEnabled(false).font(.systemFont(ofSize: 16,weight: .medium))
            UIView(frame: CGRect(x: 100, y: 10, width: 1, height: 20)).backgroundColor(UIColor(0x979CBB))
        }
    }()
    
    lazy var rightContainer: UIView = {
        UIView {
            UIView(frame: CGRect(x: 0, y: 0, width: 50, height: 48)).backgroundColor(.clear)
            self.limitLabel
        }
    }()
    
    lazy var limitLabel: UILabel = {
        UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 48)).text("0/32").textColor(UIColor(0x979cbb)).font(.systemFont(ofSize: 16)).backgroundColor(.clear)
    }()
    
    lazy var alertLabel: UILabel = {
        UILabel(frame: CGRect(x: 28, y: self.nameTextField.frame.maxY+32, width: self.view.frame.width-56, height: 14)).attributedText(NSAttributedString {
            AttributedText("aichat_create_members".toSceneLocalization() as String).font(.systemFont(ofSize: 12, weight: .medium)).foregroundColor(Color(0x303553))
            AttributedText("aichat_create_members_limited".toSceneLocalization() as String).font(.systemFont(ofSize: 12)).foregroundColor(Color(0x979CBB))
        })
    }()
    
    lazy var collectionView: DetectTapCollection = {
        let layout = UICollectionViewFlowLayout()
        let itemWidth = floor((self.view.frame.width - 40) / 4.0)
        layout.itemSize = CGSize(width: itemWidth, height: 92) // 头像加文字的总大小
        layout.minimumInteritemSpacing = 0.1 // 项目之间的间距
        layout.minimumLineSpacing = 10 // 行之间的间距
        layout.sectionInset = .zero
        
        let collectionView = DetectTapCollection(frame: CGRect(x: 20, y: self.alertLabel.frame.maxY+20, width: self.view.frame.width-40, height: 220), collectionViewLayout: layout).backgroundColor(.clear)
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.register(IntelligenceCell.self, forCellWithReuseIdentifier: "IntelligenceCell")
        
        return collectionView
    }()
    
    lazy var create: UIButton = {
        let createButton = UIButton(type: .custom).cornerRadius(23)
        createButton.frame = CGRect(x: 20, y: self.view.frame.maxY-BottomBarHeight-20-46, width: self.view.frame.width-40, height: 46)
        createButton.setBackgroundImage(UIImage(named: "create_group_large", in: .chatAIBundle, with: nil), for: .normal)
        createButton.backgroundColor = .clear
        createButton.contentMode = .scaleAspectFill
        createButton.addTarget(self, action: #selector(createGroup), for: .touchUpInside)
        return createButton
    }()
    
    lazy var createShadow: UIImageView = {
        UIImageView(frame: CGRect(x: 20, y: self.view.frame.maxY-BottomBarHeight-20-38, width: self.view.frame.width-40, height: 56)).image(UIImage(named: "create_bot_large_shadow", in: .chatAIBundle, with: nil)!).contentMode(.scaleAspectFill)
    }()
    
    private let botService = AIChatBotImplement()

    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addSubViews([self.background, self.navigation,self.nameTextField,self.alertLabel,self.collectionView,self.createShadow,self.create])
        self.nameTextField.attributedPlaceholder = NSAttributedString(string: "aichat_create_group_name_placeholder".toSceneLocalization() as String, attributes: [NSAttributedString.Key.foregroundColor: UIColor(0x979CBB)])
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
        self.navigation.title = "aichat_create_group_title".toSceneLocalization() as String
        self.navigation.separateLine.isHidden = true
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
            AIChatGroupUserProfile(id: AppContext.shared.getAIChatUid(), name: VLUserCenter.user.name, avatar: VLUserCenter.user.headUrl, type: .normal),
            AIChatGroupUserProfile(id: "6", name: "添加智能体".toSceneLocalization() as String, avatar: "", type: .add)
        ])
        self.collectionView.reloadData()
    }
    
    @objc private func createGroup() {
        let name = self.nameTextField.text ?? ""
        if name.isEmpty {
            ToastView.show(text: "群组名称不能为空")
            return
        }
        if name.count > 32 {
            ToastView.show(text: "群组名称不能超过32个字符")
            return
        }
        if self.items.filter({ $0.type == .normal }).count < 2 {
            ToastView.show(text: "群组成员至少需要2个")
            return
        }
        
        var bots = [AIChatBotProfileProtocol]()
        for item in self.items {
            if item.type == .normal {
                let bot = AIChatBotProfile()
                bot.botId = item.id
                bot.botName = item.name
                bot.botIcon = item.avatar
                bots.append(bot)
            }
        }
        
        // 创建群组
        self.create.isEnabled = false
        self.botService.createGroupChatBot(groupName: name, bots: bots) { [weak self] error, groupId in
            if error == nil {
                DispatchQueue.main.async {
                    self?.createConversation(userId: groupId)
                }
            } else {
                aichatPrint("创建群聊失败 error:\(error?.localizedDescription ?? "")")
                ToastView.show(text: "创建群组失败:\(error?.localizedDescription ?? "")", postion: .center)
            }
            DispatchQueue.main.async {
                self?.create.isEnabled = true
            }
        }
        
    }
    
    private func createConversation(userId: String) {
        let conversation = AgoraChatClient.shared().chatManager?.getConversation(userId, type: .chat, createIfNotExist: false)
        let timeMessage = AgoraChatMessage(conversationID: userId, body: AgoraChatCustomMessageBody(event: "AIChat_alert_message", customExt: nil), ext: ["something":"\(UInt64(Date().timeIntervalSince1970))"])
        conversation?.insert(timeMessage, error: nil)
        let alertMessage = AgoraChatMessage(conversationID: userId, body: AgoraChatCustomMessageBody(event: "AIChat_alert_message", customExt: nil), ext: ["something":"aichat_create_group_successful".toSceneLocalization() as String])
        conversation?.insert(alertMessage, error: nil)
        let bot = AIChatBotProfile()
        bot.botId = userId
        bot.botName = self.nameTextField.text ?? ""
        if let botIcon = self.items.filter{$0.type == .normal && AppContext.shared.getAIChatUid() != $0.id}.first?.avatar {
            bot.botIcon = VLUserCenter.user.headUrl+","+botIcon
        }
        DispatchQueue.main.async {
            self.navigationController?.popViewController(animated: false)
            DispatchQueue.main.asyncAfter(wallDeadline: .now()+0.2) {
                let chatVC = AIChatViewController(bot: bot, type: .group)
                UIViewController.currentController?.navigationController?.pushViewController(chatVC, animated: true)
            }
        }
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.view.endEditing(true)
    }
}

extension CreateIntelligentGroupViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let text = textField.text ?? ""
        let length = text.count + string.count - range.length
        self.limitLabel.text = "\(length)/32"
        return length <= 32
    }
}

extension CreateIntelligentGroupViewController: UICollectionViewDataSource,UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.items.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "IntelligenceCell", for: indexPath) as! IntelligenceCell
        if let item = self.items[safe: indexPath.row] {
            cell.refresh(item: item)
        }
        return cell
    }
    
    // MARK: UICollectionView Delegate
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        self.view.endEditing(true)
        if let item = self.items[safe: indexPath.row] {
            switch item.type {
            case .add: self.addUser()
            case .remove: self.removeUser()
            case .normal:
                if AppContext.shared.getAIChatUid() != item.id {
                    self.items.remove(at: indexPath.row)
                    collectionView.reloadData()
                }
            }
        }
    }
        
    private func addUser() {
        var ids = Array(self.items).filter{ $0.type == .normal }.map { $0.id }
        ids.dropFirst()
        let vc = GroupAddBotViewController(userIds: ids) { [weak self] items in
            self?.processAddBots(items: items)
        }
        self.present(vc, animated: true)
    }
    
    private func processAddBots(items: [AIChatBotProfileProtocol]) {
        var normalsCount = Array(self.items).compactMap{ $0 }.filter{ $0.type == .normal }.count
        if items.count + normalsCount > 6 {
            ToastView.show(text: "aichat_create_members_limited".toSceneLocalization() as String)
            return
        }
        for item in items {
            if item.selected {
                self.items.insert(AIChatGroupUserProfile(id: item.botId, name: item.botName, avatar: item.botIcon, type: .normal), at: 1)
            }
        }
        self.collectionView.reloadData()
    }
    
    private func removeUser() {
        var ids = Array(self.items).compactMap{ $0 }.filter{ $0.type == .normal }.map { $0.id }
        ids.dropFirst()
        let vc = GroupRemoveBotViewController(profileIds: ids) { [weak self] items in
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
    }
    
}

class AIChatGroupUserProfile: AIChatGroupUserProfileProtocol {
    var id: String
    var name: String
    var avatar: String
    var type: AIChatGroupRole
    
    init(id: String, name: String, avatar: String, type: AIChatGroupRole) {
        self.id = id
        self.name = name
        self.avatar = avatar
        self.type = type
    }
}

enum AIChatGroupRole: UInt8 {
    case normal
    case add
    case remove
}

protocol AIChatGroupUserProfileProtocol {
    var id: String {set get}
    var name: String {set get}
    var avatar: String {set get}
    var type: AIChatGroupRole {set get}
}


// 自定义 UICollectionViewCell 来显示头像和标题
class IntelligenceCell: UICollectionViewCell {
    
    private let imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.layer.cornerRadius = 28
        imageView.clipsToBounds = true
        imageView.backgroundColor = UIColor(0xe8e4f5)
        return imageView
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 14,weight: .medium)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.numberOfLines = 2
        label.lineBreakMode = .byTruncatingTail
        label.textColor = UIColor(0x303553)
        return label
    }()
        
    private lazy var symbol: UIImageView = {
        UIImageView().contentMode(.scaleAspectFill).backgroundColor(.clear)
    }()
    
    lazy var removeBadge: UIImageView = {
        UIImageView().image(UIImage(named: "jian_circle", in: .chatAIBundle, with: nil)!).contentMode(.scaleAspectFill)
    }()
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.backgroundColor = .clear
        self.backgroundColor = .clear
        self.contentView.addSubview(self.imageView)
        self.contentView.addSubview(self.removeBadge)
        self.contentView.addSubview(self.titleLabel)
        self.imageView.addSubview(self.symbol)
        
        self.symbol.translatesAutoresizingMaskIntoConstraints = false
        self.removeBadge.translatesAutoresizingMaskIntoConstraints = false
        
        let cos45 = sqrt(2) / 2
        
        NSLayoutConstraint.activate([
            self.imageView.centerXAnchor.constraint(equalTo: contentView.centerXAnchor),
            self.imageView.topAnchor.constraint(equalTo: contentView.topAnchor),
            self.imageView.widthAnchor.constraint(equalToConstant: 56),
            self.imageView.heightAnchor.constraint(equalToConstant: 56),
            
            self.removeBadge.widthAnchor.constraint(equalToConstant: 20),
            self.removeBadge.heightAnchor.constraint(equalToConstant: 20),
            self.removeBadge.centerXAnchor.constraint(equalTo: self.imageView.centerXAnchor, constant: 28 * cos45),
            self.removeBadge.centerYAnchor.constraint(equalTo: self.imageView.centerYAnchor, constant: 28 * cos45),
            
            self.titleLabel.centerXAnchor.constraint(equalTo: contentView.centerXAnchor),
            self.titleLabel.topAnchor.constraint(equalTo: self.imageView.bottomAnchor, constant: 5),
            self.titleLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
            self.titleLabel.trailingAnchor.constraint(equalTo: contentView.trailingAnchor),
            
            self.symbol.widthAnchor.constraint(equalToConstant: 26),
            self.symbol.heightAnchor.constraint(equalToConstant: 26),
            self.symbol.centerYAnchor.constraint(equalTo: self.imageView.centerYAnchor),
            self.symbol.centerXAnchor.constraint(equalTo: self.imageView.centerXAnchor)
        ])
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func refresh(item: AIChatGroupUserProfileProtocol) {
        self.titleLabel.text = item.name
        if item.type != .normal {
            // 显示“添加智能体”或“删除智能体”按钮
            self.symbol.image = UIImage(named: item.type == .add ? "add" : "jian", in: .chatAIBundle, with: nil)
            self.imageView.image = nil
            self.removeBadge.isHidden = true
        } else {
            self.symbol.image = nil
            
            if item.id == AppContext.shared.getAIChatUid() {
                self.removeBadge.isHidden = true
            } else {
                self.removeBadge.isHidden = false
            }
            self.imageView.sd_setImage(with: URL(string: item.avatar), placeholderImage: UIImage(named: "botavatar", in: .chatAIBundle, with: nil))
        }
    }
}

class DetectTapCollection: UICollectionView {
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        self.superview?.endEditing(true)
        return super.hitTest(point, with: event)
    }
}
