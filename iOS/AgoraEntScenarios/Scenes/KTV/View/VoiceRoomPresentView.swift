//
//  VoiceRoomPresentView.swift
//  VoiceRoomPresentView
//
//  Created by CP on 2023/1/30.
//

import UIKit

class VoiceRoomPresentView: UIView, UIGestureRecognizerDelegate {
    
    fileprivate let screenSize: CGSize = UIScreen.main.bounds.size
    
    @objc static let shared = VoiceRoomPresentView()
    
    fileprivate var nav: UINavigationController!
    
    fileprivate var bgView: UIView = UIView()
    fileprivate var mainView: UIView = UIView()
    
    fileprivate let animationDuration: Double = 0.2
    
    fileprivate let keyWindow: UIWindow? = {
        return UIApplication.shared.connectedScenes
            .filter { $0.activationState == .foregroundActive }
            .first(where: { $0 is UIWindowScene })
            .flatMap({ $0 as? UIWindowScene })?.windows
            .first(where: \.isKeyWindow)
    }()
    
    fileprivate var frames: [CGRect] = [.zero] //保存所有的frame 保证你回来的各个高度都是你之前设定的
    
    @objc func showView(with frame: CGRect, vc: UIViewController) {
        
        frames.append(frame)
        self.frame = UIScreen.main.bounds
        self.bgView.backgroundColor = .clear
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(tap))
        tap.delegate = self
        self.bgView.addGestureRecognizer(tap)
        self.bgView.isUserInteractionEnabled = true
        self.bgView.frame = UIScreen.main.bounds
        self.addSubview(self.bgView)
        
        self.mainView.frame = CGRect(x: 0, y: screenSize.height, width: frame.width, height: frame.height)
        self.addSubview(self.mainView)
        
        nav = UINavigationController(rootViewController: vc)
        nav.navigationBar.setBackgroundImage(UIImage(), for: .default)
        nav.navigationBar.shadowImage = UIImage()
        nav.navigationBar.isTranslucent = true
        nav.navigationBar.tintColor = .white

        self.mainView.addSubview(nav.view ?? UIView());
        
        
        keyWindow?.addSubview(self)
        UIView.animate(withDuration: animationDuration) {[weak self] in
            self?.mainView.frame = CGRect(x: 0, y: (self?.screenSize.height ?? 0) - frame.height, width: frame.width, height: frame.height)
        }
    }
    
    @objc fileprivate  func tap() {
        dismiss()
    }
    
    @objc func push(with vc: UIViewController, frame: CGRect) {
        frames.append(frame)
        UIView.animate(withDuration: animationDuration) {[weak self] in
            self?.mainView.frame = CGRect(x: 0, y: (self?.screenSize.height ?? 0) - frame.height, width: frame.width, height: frame.height)
            self?.nav.pushViewController(vc, animated: true)
        }
    }
    
    //返回上一级
    @objc func pop() {
        if frames.count < 2 {return}
        let lastFrame: CGRect = frames[frames.count - 2];
        UIView.animate(withDuration: animationDuration) {[weak self] in
            self?.mainView.frame = CGRect(x: 0, y: (self?.screenSize.height ?? 0) - lastFrame.height, width: lastFrame.width, height: lastFrame.height)
            self?.nav.popViewController(animated: true)
            self?.frames.removeLast()
        }
    }
    
    @objc func dismiss() {
        UIView.animate(withDuration: animationDuration) {[weak self] in
            self?.mainView.frame = CGRect(x: 0, y: self?.screenSize.height ?? 0, width: self?.screenSize.width ?? 0, height: self?.screenSize.height ?? 0)
            self?.removeFromSuperview()
        }
    }
    
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
        let point = touch.location(in: self)
        return point.y < screenSize.height - self.mainView.bounds.height
    }
    
}
