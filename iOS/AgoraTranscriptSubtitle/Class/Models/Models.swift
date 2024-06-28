//
//  Models.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/17.
//

import Foundation

@objc public enum MessageType: UInt8 {
    case transcribe = 0
    case translate = 1
    
    public init?(string: String) {
        switch string {
        case "transcribe":
            self.init(rawValue: 0)
        case "translate":
            self.init(rawValue: 1)
        default:
            return nil
        }
    }
}
