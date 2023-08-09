//
//  TextLoadingBinder.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/24.
//

import Foundation

protocol TextLoadingBinderDelegate: NSObject {
    var stateTitle: String? {get}
    var renderStateTitle: String? {get set}
}

private let loadingExtText = ["", ".", "..", "..."]
class TextLoadingBinder: NSObject {
    private weak var delegate: TextLoadingBinderDelegate?
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
    init(delegate: TextLoadingBinderDelegate?) {
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
