//
//  AUIChatFunctionBottomEntity.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation

@objc public enum AUIChatFunctionBottomEntityType: Int {
    case more
    case mic
    case gift
    case like
    case unknown = -1
}

@objcMembers public class AUIChatFunctionBottomEntity: NSObject {
    
    public var showRedDot: Bool = false
    
    public var selected: Bool = false
    
    public var selectedImage: UIImage?
    
    public var normalImage: UIImage?
    
    public var type: AUIChatFunctionBottomEntityType = .more
    
}
