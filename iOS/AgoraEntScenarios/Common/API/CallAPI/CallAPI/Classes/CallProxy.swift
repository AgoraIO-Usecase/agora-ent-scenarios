//
//  CallProxy.swift
//  CallAPI
//
//  Created by wushengtao on 2023/7/13.
//

import Foundation
import AgoraRtcKit

//TODO: fix retain cycle
class CallProxy: NSObject {
    weak var delegate: NSObjectProtocol?
    
    override func responds(to aSelector: Selector!) -> Bool {
        return delegate?.responds(to: aSelector) ?? false
    }
    
    override func method(for aSelector: Selector!) -> IMP! {
        guard let obj = self.delegate as? NSObject else {
            return super.method(for: aSelector)
        }
        
        return obj.method(for: aSelector)
    }
    
    override func forwardingTarget(for aSelector: Selector!) -> Any? {
        if delegate?.responds(to: aSelector) ?? false {
            return delegate
        }
        
        return super.forwardingTarget(for: aSelector)
    }
}

class CallAgoraExProxy: CallProxy, AgoraRtcEngineDelegate {
    init(delegate: AgoraRtcEngineDelegate?) {
        super.init()
        self.delegate = delegate
    }
}
