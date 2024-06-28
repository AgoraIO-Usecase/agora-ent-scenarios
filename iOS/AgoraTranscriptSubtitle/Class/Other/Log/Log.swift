//
//  LogProvider.swift
//  
//
//  Created by ZYP on 2021/5/28.
//

import Foundation
import UIKit

class Log {
    static let provider = LogManager.share
    
    static func errorText(text: String,
                          tag: String? = nil) {
        provider.errorText(text: text, tag: tag)
    }
    
    static func error(error: CustomStringConvertible,
                      tag: String? = nil) {
        provider.errorText(text: error.description, tag: tag)
    }
    
    static func info(text: String,
                     tag: String? = nil) {
        provider.info(text: text, tag: tag)
    }
    
    static func debug(text: String,
                      tag: String? = nil) {
        provider.debug(text: text, tag: tag)
    }
    
    static func warning(text: String,
                        tag: String? = nil) {
        provider.warning(text: text, tag: tag)
    }
    
    static func setLoggers(loggers: [ILogger]) {
        provider.loggers = loggers
    }
}

class LogManager {
    static let share = LogManager()
    var loggers = [ILogger]()
    private let queue = DispatchQueue(label: "LogManager")
    let dateFormatter: DateFormatter
    
    init() {
        dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "dd/MM/YY HH:mm:ss:SSS"
    }
    
    fileprivate func error(error: Error?,
                           tag: String?,
                           domainName: String) {
        guard let e = error else {
            return
        }
        var text = "<can not get error info>"
        if e.localizedDescription.count > 1 {
            text = e.localizedDescription
        }
        
        let err = e as CustomStringConvertible
        if err.description.count > 1 {
            text = err.description
        }
        
        errorText(text: text,
                  tag: tag)
    }
    
    fileprivate func errorText(text: String,
                               tag: String?) {
        log(type: .error,
            text: text,
            tag: tag)
    }
    
    fileprivate func info(text: String,
                          tag: String?) {
        log(type: .info,
            text: text,
            tag: tag)
    }
    
    fileprivate func warning(text: String,
                             tag: String?) {
        log(type: .warning,
            text: text,
            tag: tag)
    }
    
    fileprivate func debug(text: String,
                           tag: String?) {
        log(type: .debug,
            text: text,
            tag: tag)
    }
    
    fileprivate func log(type: LoggerLevel,
                         text: String,
                         tag: String?) {
        queue.async { [weak self] in
            guard let self = self, !self.loggers.isEmpty else { return }
            let time = self.dateFormatter.string(from: .init())
            self.log(content: text, tag: tag, time: time, level: type)
        }
    }
    
    func log(content: String, tag: String?, time: String, level: LoggerLevel) {
        for logger in loggers {
            logger.onLog(content: content, tag: tag, time: time, level: level)
        }
    }
}
