//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import UIKit
import ZSwiftBaseLib
// public enum

public class SAAlertViewController: UIViewController, SAPresentedViewType {
    public var presentedViewComponent: SAPresentedViewComponent?

    var customView: UIView?

    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    public convenience init(compent: SAPresentedViewComponent, custom: UIView) {
        self.init()
        presentedViewComponent = compent
        customView = custom
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        view.layer.cornerRadius = 10
        view.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        view.layer.masksToBounds = true
        if customView != nil {
            view.addSubview(customView!)
            customView?.translatesAutoresizingMaskIntoConstraints = false
            customView?.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
            customView?.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
            customView?.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
            customView?.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
        }
    }
}
