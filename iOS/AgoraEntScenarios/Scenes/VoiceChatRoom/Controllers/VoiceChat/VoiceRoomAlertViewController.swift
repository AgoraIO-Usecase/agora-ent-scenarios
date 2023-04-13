//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import UIKit
import ZSwiftBaseLib
// public enum

public class VoiceRoomAlertViewController: UIViewController, PresentedViewType {
    public var presentedViewComponent: PresentedViewComponent?

    var customView: UIView?

    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    public convenience init(compent: PresentedViewComponent, custom: UIView) {
        self.init()
        presentedViewComponent = compent
        customView = custom
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        if customView != nil {
            view.addSubview(customView!)
        }
    }
}

extension VoiceRoomAlertViewController {
    @objc private func keyboardWillShow(notification: Notification) {
        guard let keyboardFrame = notification.keyboardEndFrame else { return }
        let duration = notification.keyboardAnimationDuration!
        UIView.animate(withDuration: duration) {
            self.customView?.frame = CGRect(x: 0, y: ScreenHeight-keyboardFrame.height - self.customView!.frame.height, width: self.customView!.frame.width, height: self.customView!.frame.height)
        }
    }
}
