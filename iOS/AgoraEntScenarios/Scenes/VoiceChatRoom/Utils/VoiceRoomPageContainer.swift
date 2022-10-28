//
//  VoiceRoomPageContainer.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/9.
//

import UIKit

public class VoiceRoomPageContainer: UIView, UIPageViewControllerDataSource, UIPageViewControllerDelegate {
    var scrollClosure: ((Int) -> Void)?

    var controllers: [UIViewController]?

    var nextViewController: UIViewController?

    var index = 0 {
        didSet {
            DispatchQueue.main.async {
                if let vc = self.controllers?[self.index] {
                    self.pageController.setViewControllers([vc], direction: .forward, animated: false)
                }
            }
        }
    }

    lazy var pageController: UIPageViewController = {
        let page = UIPageViewController(transitionStyle: .scroll, navigationOrientation: .horizontal, options: nil)
        page.view.backgroundColor = .clear
        page.dataSource = self
        page.delegate = self
        return page
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    convenience init(frame: CGRect, viewControllers: [UIViewController]) {
        self.init(frame: frame)
        controllers = viewControllers
        pageController.setViewControllers([viewControllers[0]], direction: .forward, animated: false)
        addSubview(pageController.view)
        pageController.view.translatesAutoresizingMaskIntoConstraints = false
        pageController.view.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        pageController.view.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        pageController.view.rightAnchor.constraint(equalTo: rightAnchor).isActive = true
        pageController.view.topAnchor.constraint(equalTo: topAnchor).isActive = true
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension VoiceRoomPageContainer {
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        controllers?[safe: index - 1]
    }

    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        controllers?[safe: index + 1]
    }

    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if finished, controllers?.count ?? 0 > 0 {
            for (idx, vc) in controllers!.enumerated() {
                if vc == nextViewController {
                    index = idx
                    break
                }
            }
            if scrollClosure != nil {
                scrollClosure!(index)
            }
        } else {
            nextViewController = previousViewControllers.first
        }
    }

    func pageViewController(_ pageViewController: UIPageViewController, willTransitionTo pendingViewControllers: [UIViewController]) {
        nextViewController = pendingViewControllers.first
    }
}
