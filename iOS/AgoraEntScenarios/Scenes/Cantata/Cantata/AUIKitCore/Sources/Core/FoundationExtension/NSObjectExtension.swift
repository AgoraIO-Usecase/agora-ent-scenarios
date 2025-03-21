//
//  NSObjectExtension.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/22.
//

import Foundation

public extension NSObject {
    
    var a: AUIKitSwiftLib<NSObject> {
        AUIKitSwiftLib.init(self)
    }
    
}
    

public extension AUIKitSwiftLib where Base == NSObject {
    
    var swiftClassName: String? {
        let className = type(of: base).description().components(separatedBy: ".").last
        return  className
    }
}

@propertyWrapper public struct AUserDefault<T> {
    let key: String
    let defaultValue: T

    public init(_ key: String, defaultValue: T) {
        self.key = key
        self.defaultValue = defaultValue
    }
    ///  wrappedValue是@propertyWrapper必须要实现的属性
    /// 当操作我们要包裹的属性时  其具体set get方法实际上走的都是wrappedValue 的set get 方法。
    public var wrappedValue: T {
        get {
            return UserDefaults.standard.object(forKey: key) as? T ?? defaultValue
        }
        set {
            UserDefaults.standard.set(newValue, forKey: key)
        }
    }
}
