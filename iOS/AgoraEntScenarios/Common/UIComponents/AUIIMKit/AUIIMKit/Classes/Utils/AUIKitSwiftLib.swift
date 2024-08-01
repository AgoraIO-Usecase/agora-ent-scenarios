//
//  AUIKitSwiftLib.swift
//  AUIKitCore
//
//  Created by FanPengpeng on 2023/8/4.
//

//project wrapper
public struct AUIKitSwiftLib<Base> {
    public var base: Base
    init(_ base: Base) {
        self.base = base
    }
}
