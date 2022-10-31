//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import UIKit
import ZSwiftBaseLib
//public enum

public class VoiceRoomAlertViewController: UIViewController,PresentedViewType {
    
    public var presentedViewComponent: PresentedViewComponent?
    
    var customView: UIView?

    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    public convenience init(compent: PresentedViewComponent,custom: UIView) {
        self.init()
        self.presentedViewComponent = compent
        self.customView = custom
    }
    

    public override func viewDidLoad() {
        super.viewDidLoad()
        if self.customView != nil {
            self.view.addSubview(self.customView!)
        }
    }

}
