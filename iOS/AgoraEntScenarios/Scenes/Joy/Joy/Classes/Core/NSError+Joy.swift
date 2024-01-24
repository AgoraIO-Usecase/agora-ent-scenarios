//
//  NSError+Joy.swift
//  Joy
//
//  Created by wushengtao on 2023/12/26.
//

import Foundation

extension NSError {
    func joyErrorString() -> String? {
        switch code {
        case 2002:
            return "game_start_no_available_cloud_host".joyLocalization()
        default:
            return nil
        }
    }
}
