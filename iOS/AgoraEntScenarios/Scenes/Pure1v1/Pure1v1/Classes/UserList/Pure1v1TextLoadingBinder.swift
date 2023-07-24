//
//  Pure1v1TextLoadingBinder.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/24.
//

import Foundation

protocol Pure1v1TextLoadingBinderDelegate: NSObject {
    var stateTitle: String? {get}
    var renderStateTitle: String? {get set}
}

private let loadingExtText = ["", ".", "..", "..."]
class Pure1v1TextLoadingBinder: NSObject {
    private weak var delegate: Pure1v1TextLoadingBinderDelegate?
    private var timer:Timer!
    private var loadingRange: Int = 0 {
        didSet {
            let text = delegate?.stateTitle ?? ""
            delegate?.renderStateTitle = "\(text)\(loadingExtText[loadingRange])"
        }
    }
    deinit {
        timer.invalidate()
    }
    init(delegate: Pure1v1TextLoadingBinderDelegate?) {
        super.init()
        self.delegate = delegate
        self.timer =
        Timer.scheduledTimer(withTimeInterval: 0.3, block: {[weak self] timer in
            guard let self = self else{return}
            let range = (self.loadingRange + 1) % loadingExtText.count
            self.loadingRange = range
            print("loading text: \(range)")
        }, repeats: true)
        self.timer?.fire()
    }
}
