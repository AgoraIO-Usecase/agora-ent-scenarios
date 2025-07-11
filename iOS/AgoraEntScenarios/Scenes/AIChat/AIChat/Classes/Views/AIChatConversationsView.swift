//
//  AIChatConversationsView.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/9.
//

import UIKit
import ZSwiftBaseLib

@objc public protocol IAIChatConversationsViewDriver: NSObjectProtocol {
    
    func addDelegate(_ delegate: AIChatConversationsViewDelegate)
    
    func removeDelegate(_ delegate: AIChatConversationsViewDelegate)
    
    func refresh(conversations: [AIChatConversationInfo])
    
    func delete(conversation: AIChatConversationInfo)
}

@objc public protocol AIChatConversationsViewDelegate: NSObjectProtocol {
        
    func onDelete(conversation: AIChatConversationInfo)

}

public class AIChatConversationsView: UIView {
    
    private var handlers: NSHashTable<AIChatConversationsViewDelegate> = NSHashTable.weakObjects()
    
    var chatClosure: ((AIChatBotProfileProtocol) -> Void)?
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView(frame: .zero).delegate(self).dataSource(self).backgroundColor(.clear).rowHeight(104).separatorStyle(.none)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        return tableView
    }()
    
    private lazy var empty: EmptyStateView = {
        EmptyStateView(frame: self.bounds, emptyImage: UIImage(named: "empty", in: .chatAIBundle, with: nil)) {
            
        }.backgroundColor(.clear)
    }()
    
    private var conversations = [AIChatConversationInfo]() {
        didSet {
            DispatchQueue.main.async {
                if self.conversations.isEmpty {
                    self.empty.isHidden = false
                    self.tableView.isHidden = true
                } else {
                    self.empty.isHidden = true
                    self.tableView.isHidden = false
                }
            }
        }
    }
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.setupViews()
        self.setupConstraints()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupViews() {
        self.addSubview(self.empty)
        self.addSubview(self.tableView)
    }
    
    private func setupConstraints() {
        self.empty.retryButton.setTitle("暂无会话", for: .normal)
        self.tableView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            self.tableView.topAnchor.constraint(equalTo: self.topAnchor),
            self.tableView.leadingAnchor.constraint(equalTo: self.leadingAnchor),
            self.tableView.trailingAnchor.constraint(equalTo: self.trailingAnchor),
            self.tableView.bottomAnchor.constraint(equalTo: self.bottomAnchor)
        ])
    }
    
    
}

extension AIChatConversationsView: UITableViewDelegate,UITableViewDataSource {
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.conversations.count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "AIChatConversationCell") as? AIConversationCell
        if cell == nil {
            cell = AIConversationCell(style: .default, reuseIdentifier: "AIChatConversationCell")
        }
        cell?.selectionStyle = .none
        if let info = self.conversations[safe: indexPath.row] {
            cell?.refresh(with: info)
        }
        return cell ?? AIConversationCell()
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if let conversation = self.conversations[safe: indexPath.row],let bot = conversation.bot {
            self.chatClosure?(bot)
        }
    }
    
    public func tableView(_ tableView: UITableView, trailingSwipeActionsConfigurationForRowAt indexPath: IndexPath) -> UISwipeActionsConfiguration? {
        let action = UIContextualAction(style: .normal, title: nil, handler: { [weak self] (action, view, completion) in
            guard let `self` = self else { return }
            for handler in self.handlers.allObjects {
                handler.onDelete(conversation: self.conversations[indexPath.row])
            }
            completion(true)
        })
        action.image = UIImage(named: "delete", in: .chatAIBundle, with: nil)
        action.backgroundColor = UIColor(white: 1, alpha: 0)
        let configuration = UISwipeActionsConfiguration(actions: [action])
        configuration.performsFirstActionWithFullSwipe = false
        return configuration
    }
}

extension AIChatConversationsView: IAIChatConversationsViewDriver {
    public func addDelegate(_ delegate: any AIChatConversationsViewDelegate) {
        if self.handlers.contains(delegate) {
            return
        }
        self.handlers.add(delegate)
    }
    
    public func removeDelegate(_ delegate: any AIChatConversationsViewDelegate) {
        self.handlers.remove(delegate)
    }

    public func refresh(conversations: [AIChatConversationInfo]) {
        self.conversations.removeAll()
        self.conversations = conversations
        self.tableView.reloadData()
    }
    
    public func delete(conversation: AIChatConversationInfo) {
        self.conversations.removeAll { $0.id == conversation.id }
        self.tableView.reloadData()
    }
}
