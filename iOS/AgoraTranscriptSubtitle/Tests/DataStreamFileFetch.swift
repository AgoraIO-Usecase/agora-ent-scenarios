//
//  DataStreamFileFetch.swift
//  AgoraTranscriptSubtitle-Unit-Tests
//
//  Created by ZYP on 2024/6/19.
//

import Foundation
@testable import AgoraTranscriptSubtitle

class DataStreamFileFetch {
    
    static func fetch(fileName: String) -> [Data] {
        let path = Bundle.current.path(forResource: fileName, ofType: nil)!
        let data = try! Data(contentsOf: URL(fileURLWithPath: path))
        let string = String(data: data, encoding: .utf8)!
        
        let datas = string.split(separator: "\n")
            .map({ String($0) })
            .filter({ !$0.isEmpty })
            .map({ Data(base64Encoded: $0)! })
        return datas
    }
    
    static func printJsonString(fileName: String) {
        let datas = DataStreamFileFetch.fetch(fileName: fileName)
        let deserializer = ProtobufDeserializer()
        /// 打印文件中所有数据
        let array = datas.map({ deserializer.deserialize(data: $0)!.dict })
        let data = try! JSONSerialization.data(withJSONObject: array, options: .init(rawValue: 0))
        let js = String(data: data, encoding: .utf8) ?? "nil"
        print(js)
    }
}
