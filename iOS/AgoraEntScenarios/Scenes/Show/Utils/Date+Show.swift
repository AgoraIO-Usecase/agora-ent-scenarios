//
//  Date+Show.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/17.
//

import Foundation

extension Date {
    func millionsecondSince1970() ->Int64 {
        return Int64(Date().timeIntervalSince1970 * 1000)
    }
}
