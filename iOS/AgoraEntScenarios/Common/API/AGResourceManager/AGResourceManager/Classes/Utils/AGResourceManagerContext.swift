//
//  AGResourceManagerContext.swift
//  AGResourceManager
//
//  Created by wushengtao on 2024/3/15.
//

import Foundation

public class AGResourceManagerContext: NSObject {
    public static let shared = AGResourceManagerContext()
    public var displayLogClosure: ((String) -> Void)?
}
