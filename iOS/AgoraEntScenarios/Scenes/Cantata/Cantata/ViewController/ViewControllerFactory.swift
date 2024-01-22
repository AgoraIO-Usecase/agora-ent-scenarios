//
//  ViewControllerFactory.swift
//  Cantata
//
//  Created by CP on 2023/9/26.
//

import Foundation

import UIKit

@objc public class ViewControllerFactory: NSObject {
   @objc public static func createCustomViewController(withTitle roomModel: VLRoomListModel?, seatsArray: [VLRoomSeatModel]?) -> UIViewController {
        let viewController = CantataMainViewController()
        viewController.roomModel = roomModel
        viewController.seatsArray = seatsArray
        return viewController
    }
}
