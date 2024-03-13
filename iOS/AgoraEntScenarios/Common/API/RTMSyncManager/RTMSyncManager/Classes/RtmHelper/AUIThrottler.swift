//
//  AUIThrottler.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/14.
//

import Foundation

class AUIThrottler {
    private var workItem: DispatchWorkItem?
    private var firstTriggerDate: NSDate?
    private var triggerTimerout: Int64
    
    required init(triggerTimerout: Int64 = 100) {
        self.triggerTimerout = triggerTimerout
    }
    
    func triggerLastEvent(after delay: TimeInterval, execute: @escaping () -> Void) {
        workItem?.cancel()
        
        if self.firstTriggerDate == nil {
            self.firstTriggerDate = NSDate()
        }
        
        let newWorkItem = DispatchWorkItem { execute() }
        workItem = newWorkItem
        
        DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
            self.triggerNow()
        }
        
        if let date = firstTriggerDate, -Int64(date.timeIntervalSinceNow * 1000) > triggerTimerout {
            triggerNow()
        }
    }
    
    func triggerNow() {
        if workItem?.isCancelled ?? false == false {
            self.firstTriggerDate = nil
            workItem?.perform()
            workItem?.cancel()
        }
    }
}
