//
//  UITextView+Extension.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/12.
//

import Foundation

extension UITextView {
    func setPlaceholder(text: String) {
        let placeholderLabel = UILabel()
        placeholderLabel.numberOfLines = 0
        placeholderLabel.font = UIFont.systemFont(ofSize: 14, weight: .regular)
        placeholderLabel.textColor = UIColor(red: 0.593, green: 0.612, blue: 0.732, alpha: 1)
        placeholderLabel.text = text
        placeholderLabel.sizeToFit()
        let pointSize = font?.pointSize ?? 14
        placeholderLabel.frame.origin = CGPoint(x: 5, y: pointSize * 0.5)
        addSubview(placeholderLabel)
        setValue(placeholderLabel, forKeyPath: "_placeholderLabel")
    }
}
