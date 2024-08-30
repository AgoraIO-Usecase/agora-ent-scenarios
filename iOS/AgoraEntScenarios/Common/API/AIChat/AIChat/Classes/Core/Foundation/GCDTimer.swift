
import Foundation


public protocol GCDTimer {
    // 启动
    func resume()
    // 暂停
    func suspend()
    // 取消
    func cancel()
}

public class GCDTimerMaker {
    
    static func exec(_ task: (() -> ())?, interval: Int, repeats: Bool = true, async: Bool = true) -> GCDTimer? {
        
        guard let _ = task else {
            return nil
        }
        
        return TimerMaker(task,
                          deadline: .now(),
                          repeating: repeats ? .seconds(interval):.never,
                          async: async)
        
    }
}

private class TimerMaker: GCDTimer {
    
    /// 当前Timer 运行状态
    enum TimerState {
        case running
        case stoped
    }
    
    public private(set) var state = TimerState.stoped
    
    private var timer: DispatchSourceTimer?
    
    convenience init(_ exce: (() -> ())?, deadline: DispatchTime, repeating interval: DispatchTimeInterval = .never, leeway: DispatchTimeInterval = .seconds(0), async: Bool = true) {
        self.init()

        let queue = async ? DispatchQueue.global():DispatchQueue.main
        
        timer = DispatchSource.makeTimerSource(queue: queue)
        
        timer?.schedule(deadline: deadline,
                        repeating: interval,
                        leeway: leeway)
        
        timer?.setEventHandler(handler: {
            exce?()
        })
    }
    
    
    func resume() {
        guard state != .running else { return }
        state = .running
        timer?.resume()
    }
    
    func suspend() {
        guard state != .stoped else { return }
        state = .stoped
        timer?.suspend()
    }
    
    func cancel() {
        state = .stoped
        timer?.cancel()
    }
    
    
}

@objc protocol TimerListener {
    @objc optional func timerDidChanged(key: String, duration: Int)
}

final class GlobalTimer {
    
    static let shared = GlobalTimer()
    
    private let lock = NSRecursiveLock()
    
    private lazy var timer: GCDTimer? = {
        GCDTimerMaker.exec({
            self.timerFire()
        }, interval: 1, repeats: true)
    }()
    
    private var timers: NSMapTable<NSString, TimerListener> = NSMapTable<NSString, TimerListener>(keyOptions: .copyIn, valueOptions: .weakMemory)
        
    private var startTimeMap: [String: Int] = [:]
    
    private var removeTimerKey = ""
    
    private init() {
        self.timer?.resume()
    }
    
    func timerFire() {
        for key in self.timers.keyEnumerator() {
            if let objectKey = key as? NSString, let timerHandler = self.timers.object(forKey: objectKey) {
                if let timeKey = objectKey as? String,let start = self.startTimeMap[timeKey] {
                    if self.removeTimerKey.isEmpty {
                        let duration = Int(Date().timeIntervalSince1970*1000) - start
                        let seconds = Int(CGFloat(duration)/CGFloat(1000))
                        DispatchQueue.main.async {
                            timerHandler.timerDidChanged?(key: timeKey, duration: seconds)
                        }
                    } else {
                        self.lock.lock()
                        let duration = Int(Date().timeIntervalSince1970*1000) - start
                        let seconds = Int(CGFloat(duration)/CGFloat(1000))
                        DispatchQueue.main.async {
                            timerHandler.timerDidChanged?(key: timeKey, duration: seconds)
                        }
                        self.lock.unlock()
                    }
                }
            }
        }
        
    }
    
    func addTimer(_ key: String, timerHandler: TimerListener) {
        if self.timers.object(forKey: key as NSString) != nil {
            return
        }
        self.timers.setObject(timerHandler, forKey: key as NSString)
        let currentTimeStamp = Int(Date().timeIntervalSince1970*1000)
        self.startTimeMap[key] = currentTimeStamp
    }
    
    func removeTimer(_ key: String) {
        self.lock.lock()
        self.removeTimerKey = key
        self.timers.removeObject(forKey: key as NSString)
        self.startTimeMap.removeValue(forKey: key)
        self.lock.unlock()
        self.removeTimerKey = ""
    }
    
    func removeAll() {
        self.lock.lock()
        self.timers.removeAllObjects()
        self.startTimeMap.removeAll()
        self.lock.unlock()
        self.timer?.cancel()
    }
    
    deinit {
        removeAll()
    }
}
