//
//  CreateIntelligentsGroupViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/9.
//

import UIKit
import ZSwiftBaseLib

class CreateIntelligentGroupViewController: UIViewController {
    
    lazy var background: UIImageView = {
        UIImageView(frame: self.view.bounds).image(UIImage(named: "roomList", in: .chatAIBundle, with: nil)!).contentMode(.scaleAspectFill)
    }()
        
    lazy var navigation: AIChatNavigation = {
        AIChatNavigation(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: NavigationHeight), textAlignment: .center, rightTitle: nil).backgroundColor(.clear)
    }()
    
    lazy var leftContainer: UIView = {
        UIView {
            UIView(frame: CGRect(x: 0, y: 0, width: 87, height: 40)).backgroundColor(.clear)
            UIButton(type: .custom).frame(CGRect(x: 0, y: 0, width: 77, height: 40)).title("名称", .normal).textColor(UIColor(0x303553), .normal).isUserInteractionEnabled(false).font(.systemFont(ofSize: 16))
            UIView(frame: CGRect(x: 74, y: 10, width: 1, height: 20)).backgroundColor(UIColor(0x979CBB))
        }
    }()
    
    lazy var rightContainer: UILabel = {
        UILabel(frame: CGRect(x: 0, y: 0, width: 60, height: 40)).text("0/32").textColor(UIColor(0x979cbb)).font(.systemFont(ofSize: 16)).backgroundColor(.clear)
    }()
    
    private var nameTextField: UITextField!


    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addSubViews([self.background, self.navigation])
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
        self.navigation.contentMode = .scaleAspectFill
        self.navigation.backgroundColor = UIColor(patternImage: UIImage(named: "headerbg", in: .chatAIBundle, with: nil)!)
        self.navigation.leftItem.setImage(UIImage(systemName: "chevron.backward")?.withTintColor(.black, renderingMode: .alwaysOriginal), for: .normal)
        self.navigation.title = "创建群聊"

        // 名称输入框
        self.nameTextField = UITextField()
        self.nameTextField.placeholder = "请输入群组名称"
        self.nameTextField.clearButtonMode = .whileEditing
        self.nameTextField.font = UIFont.systemFont(ofSize: 16)
        self.nameTextField.translatesAutoresizingMaskIntoConstraints = false
//        self.nameTextField.delegate = self
        self.nameTextField.backgroundColor = .white
        self.nameTextField.leftView = self.leftContainer
        self.nameTextField.leftViewMode = .always
        self.nameTextField.rightView = self.rightContainer
        self.nameTextField.rightViewMode = .always
        self.nameTextField.cornerRadius(16)
        self.view.addSubview(self.nameTextField)
        
        NSLayoutConstraint.activate([
            // 名称输入框布局
            self.nameTextField.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            self.nameTextField.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            self.nameTextField.topAnchor.constraint(equalTo: self.navigation.bottomAnchor, constant: 16),
            self.nameTextField.heightAnchor.constraint(equalToConstant: 40),

        ])
    }

}
