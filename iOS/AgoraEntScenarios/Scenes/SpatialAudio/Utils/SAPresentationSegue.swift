//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//
import Foundation
import UIKit

public class SAPresentationSegue: UIStoryboardSegue {
    override public func perform() {
        guard let destination = destination as? SAPresentationViewController else {
            fatalError("destination must comfirm to protocol PresentedViewType")
        }
        source.sa_presentViewController(destination)
    }
}
