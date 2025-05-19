//
//  JoyNavigationBar.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit

struct ShowBarButtonItem {
    var title: String?
    var image: UIImage?
    weak var target: AnyObject?
    var action: Selector
}

enum JoyNaviBarType {
    case light
    case dark
}

class JoyNavigationBar: UIView {
    var navibarType: JoyNaviBarType = .dark {
        didSet {
            let imageName = navibarType == .dark ? "navi_back" : "icon_close"
            titleLabel.textColor = navibarType == .dark ? .black : .white
            setLeftButtonTarget(self,
                                action: #selector(didClickLeftButtonAction),
                                image: UIImage.sceneImage(name: imageName))
        }
    }
    var title: String? {
        didSet {
            titleLabel.text = title
        }
    }
    
    var rightItems: [ShowBarButtonItem]? {
        didSet {
            addRightBarButtonItems(rightItems)
        }
    }
    
    var leftItems: [ShowBarButtonItem]? {
        didSet {
            addLeftBarButtonItems(leftItems)
        }
    }
    
    private var leftButtons = [UIButton]()
    private var rightButtons = [UIButton]()
    
    lazy var titleLabel: UILabel = {
        let titleLabel = UILabel()
        addSubview(titleLabel)
        titleLabel.text = "user_list_title".joyLocalization()
        
        titleLabel.font = .joy_navi_title
        return titleLabel
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.centerX.equalToSuperview()
        }
        self.navibarType = .dark
    }
    
    func setLeftButtonTarget(_ target: AnyObject, 
                             action: Selector,
                             image: UIImage?,
                             title: String? = nil) {
        self.leftItems = nil
        let item = ShowBarButtonItem(title: title, image: image ,target: target, action: action)
        self.leftItems = [item]
    }
}

extension JoyNavigationBar {
    
    @objc private func didClickLeftButtonAction(){
        currentNavigationController()?.popViewController(animated: true)
    }
    
    private func currentNavigationController() -> UINavigationController? {
        var nextResponder = next
        while (nextResponder is UINavigationController || nextResponder == nil) == false {
            nextResponder = nextResponder?.next
        }
        return nextResponder as? UINavigationController
    }
    
    // 添加右边按钮 从右往左排列
    private func addRightBarButtonItems(_ items: [ShowBarButtonItem]?) {
        if items == nil {
            for button in rightButtons {
                button.removeFromSuperview()
            }
            return
        }
        var firstButton: UIButton?
        for item in items! {
            let button = createBarButton(item: item)
            addSubview(button)
            rightButtons.append(button)
            button.snp.makeConstraints { make in
                if firstButton == nil {
                    firstButton = button
                    make.right.equalTo(-20)
                }else{
                    make.right.equalTo(firstButton!.snp.left).offset(-25)
                }
                make.centerY.equalTo(titleLabel)
            }
        }
    }
    
    // 添加左边按钮 从左向右排列
    private func addLeftBarButtonItems(_ items: [ShowBarButtonItem]?) {
        if items == nil {
            for button in leftButtons {
                button.removeFromSuperview()
            }
            return
        }
        var firstButton: UIButton?
        for item in items! {
            let button = createBarButton(item: item)
            addSubview(button)
            leftButtons.append(button)
            button.snp.makeConstraints { make in
                if firstButton == nil {
                    firstButton = button
                    make.left.equalTo(20)
                }else{
                    make.left.equalTo(firstButton!.right).offset(25)
                }
                make.centerY.equalTo(titleLabel)
            }
        }
    }
    
    private func createBarButton(item: ShowBarButtonItem) -> UIButton {
        let button = UIButton(type: .custom)
        button.setTitle(item.title, for: .normal)
        button.setImage(item.image, for: .normal)
        button.setTitleColor(.joy_zi03, for: .normal)
        button.titleLabel?.font = .joy_R_14
        button.addTarget(item.target, action: item.action, for: .touchUpInside)
        return button
    }
}
