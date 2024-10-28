//
//  PlaceHolderTextView.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/4.
//

import UIKit

class PlaceholderTextEditor: UITextView {
    
    private var placeholderLabel: UILabel?
    
    public var placeholder: String? {
        didSet {
            placeholderLabel?.text = placeholder
            placeholderLabel?.isHidden = false
        }
    }
    
    public var placeholderColor: UIColor = .lightGray {
        didSet {
            placeholderLabel?.textColor = placeholderColor
        }
    }
    
    override var text: String! {
        didSet {
            textDidChange()
        }
    }
    
    override var attributedText: NSAttributedString! {
        didSet {
            textDidChange()
        }
    }
    
    override var textAlignment: NSTextAlignment {
        didSet {
            placeholderLabel?.textAlignment = textAlignment
        }
    }
    
    override var font: UIFont? {
        didSet {
            placeholderLabel?.font = font
        }
    }
    
    override init(frame: CGRect, textContainer: NSTextContainer?) {
        super.init(frame: frame, textContainer: textContainer)
        commonInit()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        commonInit()
    }
    
    private func commonInit() {
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(textDidChange),
                                               name: UITextView.textDidChangeNotification,
                                               object: nil)
        
        let label = UILabel()
        label.numberOfLines = 0
        label.font = font
        label.textColor = placeholderColor
        label.textAlignment = textAlignment
        label.translatesAutoresizingMaskIntoConstraints = false
        addSubview(label)
        
        NSLayoutConstraint.activate([
            label.topAnchor.constraint(equalTo: topAnchor, constant: self.contentInset.top),
            label.leadingAnchor.constraint(equalTo: leadingAnchor, constant: self.contentInset.left+4),
            label.trailingAnchor.constraint(equalTo: trailingAnchor, constant: self.contentInset.right),
        ])
        
        placeholderLabel = label
    }
    
    @objc private func textDidChange() {
        if !self.isFirstResponder {
            return
        }
        placeholderLabel?.isHidden = !text.isEmpty
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
