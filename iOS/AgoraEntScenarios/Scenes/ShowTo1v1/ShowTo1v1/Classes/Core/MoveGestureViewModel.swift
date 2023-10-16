//
//  MoveGestureViewModel.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/25.
//

import Foundation

class MoveGestureViewModel: NSObject {
    var touchArea: CGRect?
    lazy var gesture:UIPanGestureRecognizer = UIPanGestureRecognizer(target: self, action: #selector(moveAction(_:)))
    
    @objc private func moveAction(_ gesture: UIPanGestureRecognizer) {
        guard let touchView = gesture.view else {return}
        let touchValue = gesture.translation(in: touchView)
        switch gesture.state {
        case .changed, .cancelled, .ended:
            touchView.aui_tl = CGPoint(x: touchView.aui_tl.x + touchValue.x, y: touchView.aui_tl.y + touchValue.y)
            if let touchArea = touchArea {
                if touchArea.origin.x > touchView.aui_left {
                    touchView.aui_left = touchArea.origin.x
                } else if touchArea.origin.x + touchArea.size.width < touchView.aui_right {
                    touchView.aui_right = touchArea.origin.x + touchArea.size.width
                }
                
                if touchArea.origin.y > touchView.aui_top {
                    touchView.aui_top = touchArea.origin.y
                } else if touchArea.origin.y + touchArea.size.height < touchView.aui_bottom {
                    touchView.aui_bottom = touchArea.origin.y + touchArea.size.height
                }
            }
            gesture.setTranslation(.zero, in: touchView)
        default:
            break
        }
    }
    
}
