//
//  DHCPresentView.swift
//  DHCPresentView
//
//  Created by CP on 2023/09/20.
//

import UIKit

class DHCPresentView: UIView, UIGestureRecognizerDelegate {
    
    fileprivate let screenSize: CGSize = UIScreen.main.bounds.size
    
    @objc static var shared: DHCPresentView = DHCPresentView()
    
    fileprivate var nav: UINavigationController!
    
    fileprivate var bgView: UIView = UIView()
    fileprivate var mainView: UIView = UIView()
    
    fileprivate let animationDuration: Double = 0.2
    fileprivate var curTableview: UITableView?
    
    fileprivate let keyWindow: UIWindow? = {
        if #available(iOS 13.0, *) {
            return UIApplication.shared.connectedScenes
                .filter { $0.activationState == .foregroundActive }
                .first(where: { $0 is UIWindowScene })
                .flatMap({ $0 as? UIWindowScene })?.windows
                .first(where: \.isKeyWindow)
        } else {
            // Fallback on earlier versions
            return nil
        }
    }()
    
    fileprivate var frames: [CGRect] = [] //保存所有的frame 保证你回来的各个高度都是你之前设定的
    fileprivate var maxHeights: [CGFloat] = [] //保存每个视图的最大高度
    fileprivate var minHeights: [CGFloat] = [] //保存每个视图的最小高度
    fileprivate var isTableViewScrollable: Bool = false
    
    @objc func showView(with frame: CGRect, vc: UIViewController, maxHeight: CGFloat) {
        
        frames.append(frame)
        maxHeights.append(maxHeight)
        minHeights.append(frame.height)
        
        self.frame = UIScreen.main.bounds
        self.bgView.backgroundColor = .clear
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(tap))
        tap.delegate = self
        self.bgView.addGestureRecognizer(tap)
        self.bgView.isUserInteractionEnabled = true
        self.bgView.frame = UIScreen.main.bounds
        self.addSubview(self.bgView)
        
        self.mainView.frame = CGRect(x: 0, y: screenSize.height, width: frame.width, height: frame.height)
        let pan: UIPanGestureRecognizer = UIPanGestureRecognizer(target: self, action: #selector(pan))
        pan.delegate = self
        self.mainView.addGestureRecognizer(pan)
        self.mainView.isUserInteractionEnabled = true
        self.addSubview(self.mainView)
        
        nav = UINavigationController(rootViewController: vc)
        nav.navigationBar.setBackgroundImage(UIImage(), for: .default)
        nav.navigationBar.shadowImage = UIImage()
        nav.navigationBar.isTranslucent = true
        nav.navigationBar.tintColor = .white

        self.mainView.addSubview(nav.view ?? UIView());

        UIView.animate(withDuration: animationDuration) {[weak self] in
            self?.mainView.frame = CGRect(x: 0, y: (self?.screenSize.height ?? 0) - frame.height, width: frame.width, height: frame.height)
        }
    }
    
    @objc fileprivate  func tap(tap: UITapGestureRecognizer) {
        dismiss()
    }
    
    @objc fileprivate func pan(pan: UIPanGestureRecognizer) {
        let translation = pan.translation(in: self)
        let height: CGFloat = self.mainView.frame.height - translation.y
        if height > maxHeights.last ?? 0 {
            return
        }
        if height < minHeights.last ?? 0 {
            return
        }
        
        curTableview?.isScrollEnabled = height > screenSize.height
        if height > screenSize.height {
            return
        }

        let rect = CGRect(x: 0, y: self.mainView.frame.minY + translation.y, width: self.mainView.frame.width, height: height)
        self.mainView.frame = rect
        self.frames[frames.count - 1] = rect
        pan.setTranslation(.zero, in: self)
        print(rect)
    }
    
    @objc public func update(_ height: CGFloat) {
        UIView.animate(withDuration: 0.5) {
            let rect = CGRect(x: 0, y: UIScreen.main.bounds.height - height, width: self.mainView.frame.width, height: height)
            self.mainView.frame = rect
        }
    }
    
    @objc func push(with vc: UIViewController, frame: CGRect, maxHeight: CGFloat) {
        frames.append(frame)
        maxHeights.append(maxHeight)
        minHeights.append(frame.height)
        curTableview = getTableView(with: vc)
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
        } completion: { [weak self] _ in
            self?.frames.removeLast()
            self?.maxHeights.removeLast()
            self?.minHeights.removeLast()
            guard let VC: UIViewController = self?.nav.children.last else {return}
            guard let tableview: UITableView = self?.getTableView(with: VC) else {return}
            self?.curTableview = tableview
        }
    }
    
    @objc func dismiss() {
        UIView.animate(withDuration: animationDuration) {[weak self] in
            self?.mainView.frame = CGRect(x: 0, y: self?.screenSize.height ?? 0, width: self?.screenSize.width ?? 0, height: self?.screenSize.height ?? 0)
        } completion: {[weak self] _ in
            self?.frames.removeAll()
            self?.maxHeights.removeAll()
            self?.minHeights.removeAll()
            self?.removeFromSuperview()
        }
    }
    
    fileprivate func getTableView(with VC: UIViewController) -> UITableView? {
        var tableView: UITableView? = nil
        for view in VC.view.subviews {
            if view.isKind(of: UITableView.self) {
                tableView = view as? UITableView
            }
        }
        return tableView
    }
    
}
