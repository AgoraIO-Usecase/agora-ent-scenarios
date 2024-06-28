///
///  ProtobufDeserializer.swift
///  AgoraSuttileView
///
///  Created by zhuyuping on 2024/6/17

import UIKit

class ProtobufDeserializer: NSObject {
    private let logTag = "ProtobufDeserializer"
    typealias DataStreamMessage = SttText
    
    /// deserialize
    /// - Parameter data: pb data
    /// - Returns: model
    func deserialize(data: Data) -> DataStreamMessage? {
        do {
            let msg = try DataStreamMessage.parse(from: data)
            
            guard msg.uid != 0 else {
                Log.errorText(text: "deserialize uid invalid.", tag: logTag)
                return nil
            }
            
            guard let _ = MessageType(string: msg.dataType) else {
                Log.errorText(text: "deserialize data type unknown.", tag: logTag)
                return nil
            }
            
            return msg
        } catch let error as NSError {
            Log.errorText(text: "deserialize data failed, err:\(error.localizedDescription)", tag: logTag)
            return nil
        }
    }
}

