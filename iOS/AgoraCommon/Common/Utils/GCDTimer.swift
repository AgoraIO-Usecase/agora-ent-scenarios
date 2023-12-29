//
//  GCDTimer.swift
//  GDGK_iOSExam
//
//  Created by cleven on 20.4.21.
//
import UIKit

public class GCDTimer {
    public typealias ActionBlock = (String, TimeInterval) -> Void
    private var timerContainer = [String: DispatchSourceTimer]()
    private var currentDuration: TimeInterval = 0
    
    public init() {
        
    }

    /// 秒级定时器
    ///
    /// - Parameters:
    ///  - name: 定时器的名字
    ///  - timeInterval: 时间间隔
    ///  - queue: 线程
    ///  - repeats: 是否重复
    ///  - action: 执行的操作
    public func scheduledSecondsTimer(withName name: String?,
                               timeInterval: Int,
                               queue: DispatchQueue,
                               action: @escaping ActionBlock)
    {
        currentDuration = TimeInterval(0)
        let scheduledName = name ?? Date().timeString()
        var timer = timerContainer[scheduledName]
        if timer == nil {
            timer = DispatchSource.makeTimerSource(flags: [], queue: queue)
            timer?.resume()
            timerContainer[scheduledName] = timer
        }
        timer?.schedule(deadline: .now(), repeating: .seconds(timeInterval), leeway: .milliseconds(100))
        timer?.setEventHandler(handler: { [weak self] in
            guard let self = self else { return }
            self.currentDuration += TimeInterval(timeInterval)
            action(scheduledName, self.currentDuration)
        })
    }

    /// 秒级倒计时定时器
    ///
    /// - Parameters:
    ///  - name: 定时器的名字
    ///  - timeInterval: 时间间隔
    ///  - queue: 线程
    ///  - repeats: 是否重复
    ///  - action: 执行的操作
    public func scheduledTimer(withName name: String?,
                        timeInterval: Int,
                        queue: DispatchQueue,
                        action: @escaping ActionBlock)
    {
        let scheduledName = name ?? Date().timeString()
        var timer = timerContainer[scheduledName]
        if timer == nil {
            timer = DispatchSource.makeTimerSource(flags: [], queue: queue)
            timer?.resume()
            timerContainer[scheduledName] = timer
        }
        timer?.schedule(deadline: .now(), repeating: .seconds(timeInterval), leeway: .milliseconds(1000))
        timer?.setEventHandler(handler: {
            action(scheduledName, 1)
        })
    }

    /// 毫秒级倒计时定时器
    /// - Parameters:
    ///   - name: 名称
    ///   - countDown: 倒计时毫秒
    ///   - timeInterval: 多少毫秒回调一次
    ///   - queue: 线程
    ///   - action: 回调
    public func scheduledMillisecondsTimer(withName name: String?,
                                    countDown: TimeInterval,
                                    milliseconds: TimeInterval,
                                    queue: DispatchQueue,
                                    action: @escaping ActionBlock)
    {
        currentDuration = countDown
        let scheduledName = name ?? Date().timeString()
        var timer = timerContainer[scheduledName]
        if timer == nil {
            timer = DispatchSource.makeTimerSource(flags: [], queue: queue)
            timer?.resume()
            timerContainer[scheduledName] = timer
        }
        timer?.schedule(deadline: .now(), repeating: .milliseconds(Int(milliseconds)), leeway: .milliseconds(1))
        timer?.setEventHandler(handler: { [weak self] in
            guard let self = self else { return }
            self.currentDuration -= milliseconds
            action(scheduledName, self.currentDuration)
            if self.currentDuration <= 0 {
                self.destoryTimer(withName: scheduledName)
            }
        })
    }

    /// 销毁名字为name的计时器
    ///
    /// - Parameter name: 计时器的名字
    public func destoryTimer(withName name: String?) {
        guard let name = name else { return }
        let timer = timerContainer[name]
        if timer == nil { return }
        timerContainer.removeValue(forKey: name)
        timer?.cancel()
    }

    /// 销毁所有计时器
    public func destoryAllTimer() {
        timerContainer.forEach {
            destoryTimer(withName: $0.key)
        }
    }

    /// 检测是否已经存在名字为name的计时器
    ///
    /// - Parameter name: 计时器的名字
    /// - Returns: 返回bool值
    public func isExistTimer(withName name: String?) -> Bool {
        guard let name = name else { return false }
        return timerContainer[name] != nil
    }
}

extension Date {
    public func timeString(ofStyle style: DateFormatter.Style = .medium) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.timeStyle = style
        dateFormatter.dateStyle = .none
        return dateFormatter.string(from: self)
    }
}
