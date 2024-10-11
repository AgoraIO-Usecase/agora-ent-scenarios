//
//  GroupNameEditViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/11.
//

import UIKit
import ZSwiftBaseLib
import AgoraCommon

class GroupNameEditViewController: UIViewController {
    
    var groupName: String = "" {
        didSet {
            DispatchQueue.main.async {
                self.nameTextField.text = self.groupName
            }
        }
    }
    
    lazy var navigation: AIChatNavigation = {
        AIChatNavigation(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 44),textAlignment: .left,rightTitle: "保存")
    }()
    
    lazy var nameTextField: UITextField = {
        UITextField(frame: CGRect(x: 20, y: self.navigation.frame.maxY+16, width: self.view.frame.width-40, height: 48)).delegate(self).backgroundColor(.white).placeholder("请输入群组名称").font(.systemFont(ofSize: 16)).clearButtonMode(.whileEditing)
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
            self.limitLabel
        }
    }()
    
    lazy var limitLabel: UILabel = {
        UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 48)).text("0/32").textColor(UIColor(0x979cbb)).font(.systemFont(ofSize: 16)).backgroundColor(.clear)
    }()
    
    private var confirmClosure: ((String) -> Void)
    
    public required init(confirmClosure: @escaping ((String) -> Void)) {
        self.confirmClosure = confirmClosure
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .white
        self.view.cornerRadius(16, [.topLeft,.topRight], .clear, 0)
        if let image = UIImage(named: "edit_bg", in: .chatAIBundle, with: nil) {
            self.navigation.backgroundColor = UIColor(patternImage: image)
        }
        self.navigation.title = "编辑群组名称"
        self.navigation.leftItem.isHidden = true
        self.navigation.separateLine.isHidden = true
        self.view.addSubViews([self.navigation,self.nameTextField])
        // Do any additional setup after loading the view.
        self.navigation.clickClosure = { [weak self] type,_ in
            if type == .rightTitle {
                if (self?.nameTextField.text ?? "").count > 32 {
                    ToastView.show(text: "群组名称不能超过32个字符")
                    return
                }
                self?.confirmClosure(self?.nameTextField.text ?? "")
                self?.dismiss(animated: true, completion: nil)
            }
        }
        // Do any additional setup after loading the view.
    }
    
    
}

extension GroupNameEditViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let text = textField.text ?? ""
        let length = text.count + string.count - range.length
        self.limitLabel.text = "\(length)/32"
        self.navigation.rightItem.isEnabled = length > 0
        return length <= 32
    }
}
