//
//  LongPressButton.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/4.
//

import UIKit

class LongPressButton: UIButton {
    
    // 定义一个枚举来表示移动方向
    enum MoveDirection {
        case left
        case right
        case up
        case down
        case none
    }
    enum State {
        case start
        case end
        case cancel
    }
    
    // 回调闭包，用于通知外部长按状态和移动方向
    var longPressCallback: ((State, MoveDirection) -> Void)?
    
    // 记录长按开始的位置
    private var startLocation: CGPoint?
    
    private var currentState: State = .start
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setupLongPressGesture()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        self.setupLongPressGesture()
    }
    
    private func setupLongPressGesture() {
        let longPressGesture = UILongPressGestureRecognizer(target: self, action: #selector(handleLongPress(_:)))
//        longPressGesture.minimumPressDuration = 0.5 // 设置长按时间为0.5秒
        self.addGestureRecognizer(longPressGesture)
    }
    
    @objc private func handleLongPress(_ gesture: UILongPressGestureRecognizer) {
        switch gesture.state {
        case .began:
            self.currentState = .start
            self.startLocation = gesture.location(in: self)
            self.longPressCallback?(.start, .none)
            UIImpactFeedbackGenerator.feedback(with: .heavy)
        case .changed:
            guard let startLocation = self.startLocation else { return }
            let currentLocation = gesture.location(in: self)
            let direction = self.getDirection(from: startLocation, to: currentLocation)
            if !self.bounds.contains(currentLocation),self.startLocation != currentLocation {
                self.longPressCallback?(.cancel, direction)
                self.currentState = .cancel
            } else {
                if self.currentState != .start {
                    self.startLocation = gesture.location(in: self)
                    self.longPressCallback?(.start, direction)
                }
            }
        case .ended,.cancelled:
            let location = gesture.location(in: self)
            self.startLocation = nil
            self.longPressCallback?(.end, .none)
            self.currentState = .end
        default:
            break
        }
    }
    
    private func getDirection(from start: CGPoint, to end: CGPoint) -> MoveDirection {
        let deltaX = end.x - start.x
        let deltaY = end.y - start.y
        
        // 设置一个阈值，以确定是否认为移动足够明显
        let threshold: CGFloat = 10
        
        if abs(deltaX) > abs(deltaY) {
            // 水平移动更明显
            if deltaX > threshold {
                return .right
            } else if deltaX < -threshold {
                return .left
            }
        } else {
            // 垂直移动更明显
            if deltaY > threshold {
                return .down
            } else if deltaY < -threshold {
                return .up
            }
        }
        
        return .none
    }
}
