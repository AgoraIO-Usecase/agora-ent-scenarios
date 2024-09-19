//
//  VoiceChatAutoDismissView.swift
//  AIChat
//
//  Created by wushengtao on 2024/9/19.
//

import UIKit

class VoiceChatAutoDismissView: UIImageView {
    private var timer: Timer?
    override var isHidden: Bool {
        didSet {
            if isHidden == true {
                stopAutoDismiss()
                return
            }
            
            startAutoDismiss()
        }
    }
    
    func startAutoDismiss() {
        stopAutoDismiss()
        timer = Timer.scheduledTimer(withTimeInterval: 10, repeats: false, block: {[weak self] _ in
            self?.isHidden = true
        })
    }
    
    func stopAutoDismiss() {
        timer?.invalidate()
        timer = nil
    }
}
