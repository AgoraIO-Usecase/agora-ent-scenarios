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
        case 40001:
            return "game_invalid_params".joyLocalization()
        case 40023:
            return "game_params_missing".joyLocalization()
        case 92000:
            return "game_rec_not_start".joyLocalization()
        case 92001:
            return "game_send_cmd_fail_try_success".joyLocalization()
        case 92002:
            return "game_send_cmd_fail_try_fail".joyLocalization()
        case 92003:
            return "game_cmd_err".joyLocalization()
        case 92004:
            return "game_gift_id_err".joyLocalization()
        case 92005:
            return "game_gift_am_err".joyLocalization()
        case 2001:
            return "game_cloud_start_err".joyLocalization()
        case 10001:
            return "game_server_err".joyLocalization()
        default:
            return "game_unknow_err".joyLocalization() + "code: \(code)"
        }
    }
}
