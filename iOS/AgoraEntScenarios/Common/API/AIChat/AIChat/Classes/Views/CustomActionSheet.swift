//
//  CustomActionSheet.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/25.
//

import UIKit
import ZSwiftBaseLib

class GroupActionSheet: UIView {
        
    lazy var background: UIView = {
        UIView(frame: self.bounds).backgroundColor(UIColor(red: 0, green: 0, blue: 0, alpha: 0.3))
    }()
    
    lazy var sheetMenu: CustomActionSheet = {
        CustomActionSheet(frame: CGRect(x: 0, y: ScreenHeight-143, width: ScreenWidth, height: 143))
    }()
    
    required init(action: @escaping ()->()) {
        super.init(frame: UIScreen.main.bounds)
        self.addSubViews([self.background,self.sheetMenu])
        self.background.isUserInteractionEnabled = true
        let tap = UITapGestureRecognizer(target: self, action: #selector(dismissSelf))
        self.background.addGestureRecognizer(tap)
        self.sheetMenu.actionClosure = { [weak self] in
            action()
            self?.dismissSelf()
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: - Show and Dismiss Methods
    func show(in viewController: UIViewController) {
        viewController.view.addSubview(self)
        
        self.frame = UIScreen.main.bounds
        self.sheetMenu.frame = CGRect(x: 0, y: ScreenHeight-143, width: ScreenWidth, height: 143)
        // Animate the action sheet sliding up
        UIView.animate(withDuration: 0.3) {
            self.frame = UIScreen.main.bounds
        }
    }
    
    @objc func dismissSelf() {
        guard let window = UIApplication.shared.chat.keyWindow else { return }
        self.background.isHidden = true
        // Animate the action sheet sliding down and remove from view
        UIView.animate(withDuration: 0.3, animations: {
            self.sheetMenu.frame = CGRect(x: 0, y: ScreenHeight, width: self.sheetMenu.frame.width, height: self.sheetMenu.frame.height)
        }) { _ in
            self.removeFromSuperview()
        }
    }
}


class CustomActionSheet: UIView {
    
    var actionClosure: (()->())?
    
    // MARK: - Properties
    private let actionButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("删除群聊", for: .normal)
        button.setTitleColor(UIColor.theme.errorColor5, for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.addTarget(self, action: #selector(deleteGroup), for: .touchUpInside)
        return button
    }()
    
    lazy var gradient: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: 56)).image(UIImage(named: "edit_bg", in: .chatAIBundle, with: nil)!)
    }()
    
    // MARK: - Initializer
    override init(frame: CGRect) {
        super.init(frame: UIScreen.main.bounds)
        self.setupView()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc private func deleteGroup() {
        self.actionClosure?()
    }
    
    // MARK: - Setup View
    private func setupView() {
        self.backgroundColor = .white
        self.cornerRadius(16, [.topLeft, .topRight], .clear, 0)
        self.addSubview(self.gradient)
        // Add button to the view
        addSubview(actionButton)
        actionButton.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            actionButton.centerXAnchor.constraint(equalTo: centerXAnchor),
            actionButton.topAnchor.constraint(equalTo: self.topAnchor,constant: 46),
            actionButton.widthAnchor.constraint(equalTo: widthAnchor),
            actionButton.heightAnchor.constraint(equalToConstant: 22)
        ])
    }
    
    
}

