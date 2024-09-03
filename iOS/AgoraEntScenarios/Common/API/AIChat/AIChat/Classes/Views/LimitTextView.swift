
import UIKit
import ZSwiftBaseLib

public enum LimitTextViewState {
    case normal
    case editing
}

open class LimitTextView: UIView, UITextViewDelegate {
    private let titleLabel = UILabel()
    private let descriptionTextView = PlaceHolderTextView()
    private let charCountLabel = UILabel().textAlignment(.right)
    
    var finalText = ""

    
    private var introduce = ""
    
    private var placeHolder = ""
    
    private var maxCharCount = 1024
    
    private var keyboardHeight: CGFloat = 0
    
    private var needListenKeyboard: Bool = false
    
    var state = LimitTextViewState.normal
    
    var editStateChanged: ((LimitTextViewState,LimitTextView,CGFloat) -> Void)?
    
    init(introduce: String, placeHolder: String, limitCount: Int, needListenKeyboard: Bool = false) {
        self.introduce = introduce
        self.placeHolder = placeHolder
        self.maxCharCount = limitCount
        self.needListenKeyboard = needListenKeyboard
        super.init(frame: .zero)
        self.setupUI() 
        // 注册监听键盘出现和隐藏的通知
        if needListenKeyboard {
            NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
            NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
        }
        
    }
    
    deinit {
//        // 移除通知监听
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    
    @objc func keyboardWillShow(_ notification: Notification) {
        // 获取键盘的高度
        if let keyboardFrame = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            self.keyboardHeight = keyboardFrame.cgRectValue.height
            if self.needListenKeyboard {
                self.editStateChanged?(.editing,self,self.keyboardHeight)
            }
        }
    }
    
    @objc func keyboardWillHide(_ notification: Notification) {

    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setupUI()
    }
    
    private func setupUI() {
        // Set up the view's appearance
        self.backgroundColor = UIColor(white: 0.95, alpha: 1)
        self.layer.cornerRadius = 12
        
        // Title Label
        self.titleLabel.text = self.introduce
        self.titleLabel.font = UIFont.systemFont(ofSize: 16, weight: .medium)
        self.titleLabel.textColor = UIColor(0x303553)
        self.titleLabel.translatesAutoresizingMaskIntoConstraints = false
        
        // Description TextView
        self.descriptionTextView.font = UIFont.systemFont(ofSize: 16)
        self.descriptionTextView.placeHolder = self.placeHolder
        self.descriptionTextView.placeHolderColor = UIColor(0x979CBB)
        self.descriptionTextView.textColor = .black
        self.descriptionTextView.delegate = self
        self.descriptionTextView.translatesAutoresizingMaskIntoConstraints = false
        
        // Character Count Label
        self.charCountLabel.text = "0/\(maxCharCount)"
        self.charCountLabel.font = UIFont.systemFont(ofSize: 12)
        self.charCountLabel.textColor = UIColor(0x979CBB)
        self.charCountLabel.translatesAutoresizingMaskIntoConstraints = false
        
        // Add subviews
        self.addSubViews([self.titleLabel, self.descriptionTextView, self.charCountLabel])
        
        // Layout constraints
        NSLayoutConstraint.activate([
            self.titleLabel.topAnchor.constraint(equalTo: self.topAnchor, constant: 12),
            self.titleLabel.leadingAnchor.constraint(equalTo: self.leadingAnchor, constant: 16),
            self.titleLabel.trailingAnchor.constraint(equalTo: self.trailingAnchor, constant: -16),
            
            self.descriptionTextView.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 8),
            self.descriptionTextView.leadingAnchor.constraint(equalTo: self.leadingAnchor, constant: 5.5),
            self.descriptionTextView.trailingAnchor.constraint(equalTo: self.trailingAnchor, constant: -5.5),
            self.descriptionTextView.bottomAnchor.constraint(equalTo: self.bottomAnchor, constant: -44),
            
            self.charCountLabel.heightAnchor.constraint(equalToConstant: 22),
            self.charCountLabel.trailingAnchor.constraint(equalTo: self.trailingAnchor, constant: -16),
            self.charCountLabel.leadingAnchor.constraint(equalTo: self.leadingAnchor, constant: 16),
            self.charCountLabel.bottomAnchor.constraint(equalTo: self.bottomAnchor, constant: -12)
        ])
        
        // Initial character count
        self.updateCharCount()
    }
    
    public func textViewShouldBeginEditing(_ textView: UITextView) -> Bool {
        self.state = .editing
        return true
    }
     
    public func textViewDidEndEditing(_ textView: UITextView) {
        self.finalText = textView.text
        self.state = .normal
        self.editStateChanged?(.normal,self, self.keyboardHeight)
    }
    
    public func textViewDidChange(_ textView: UITextView) {
        self.updateCharCount()
    }
    
    private func updateCharCount() {
        let currentCharCount = descriptionTextView.text?.count ?? 0
        self.charCountLabel.text = "\(currentCharCount)/\(maxCharCount)"
        self.charCountLabel.textColor = currentCharCount > maxCharCount ? .red : UIColor(0x979CBB)
    }
    
    open override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        self.endEditing(true)
    }
}
