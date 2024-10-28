//
//  Impact+AIChat.swift
//  AIChat
//
//  Created by wushengtao on 2024/10/18.
//

import Foundation

extension UIImpactFeedbackGenerator {
    static func feedback(with style: UIImpactFeedbackGenerator.FeedbackStyle) {            
        let feedbackGenerator = UIImpactFeedbackGenerator(style: style)
        feedbackGenerator.prepare()
        feedbackGenerator.impactOccurred()
    }
}
