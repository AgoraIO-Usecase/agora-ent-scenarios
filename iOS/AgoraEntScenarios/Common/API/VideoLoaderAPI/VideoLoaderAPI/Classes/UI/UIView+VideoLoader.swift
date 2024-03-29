//
//  UIView+VideoLoader.swift
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/9/7.
//

import Foundation

private var ag_gestureId: String = ""
 
struct APIRuntimeKey {
    static let handler = UnsafeRawPointer.init(bitPattern: "api_handler".hashValue)!
    static let touchTopLeft = UnsafeRawPointer.init(bitPattern: "api_touchTL".hashValue)!
    static let gesture = UnsafeRawPointer.init(bitPattern: "api_gesture".hashValue)!
}

private func api_getWinwdow() -> UIWindow {
    if #available(iOS 13.0, *) {
        if let scene = UIApplication.shared.connectedScenes.first(where: { $0.activationState == .foregroundActive }) as? UIWindowScene {
            if let keyWindow = scene.windows.first {
                return keyWindow
            }
        }
    }
    return UIApplication.shared.keyWindow!
}

extension UIView {
    private var ag_eventHandler: VideoLoaderViewEventHandler? {
        set {
            objc_setAssociatedObject(self, APIRuntimeKey.handler, newValue, .OBJC_ASSOCIATION_RETAIN)
        }
        get {
            let handler = objc_getAssociatedObject(self, APIRuntimeKey.handler) as? VideoLoaderViewEventHandler
            return handler
        }
    }
    
    private var ag_touchTL: NSValue? {
        set {
            objc_setAssociatedObject(self, APIRuntimeKey.touchTopLeft, newValue, .OBJC_ASSOCIATION_RETAIN)
        }
        get {
            let tl = objc_getAssociatedObject(self, APIRuntimeKey.touchTopLeft) as? NSValue
            return tl
        }
    }
    
    private var ag_gesture: UIGestureRecognizer? {
        set {
            objc_setAssociatedObject(self, APIRuntimeKey.gesture, newValue, .OBJC_ASSOCIATION_RETAIN)
        }
        get {
            let gesture = objc_getAssociatedObject(self, APIRuntimeKey.gesture) as? UIGestureRecognizer
            return gesture
        }
    }
    
    public func ag_addPreloadTap(roomInfo: IVideoLoaderRoomInfo,
                                 localUid: UInt,
                                 enableProcess: @escaping ((UIGestureRecognizer.State)->Bool),
                                 onRequireRenderVideo: ((AnchorInfo, VideoCanvasContainer)->UIView?)?,
                                 completion: @escaping (()->())) {
        let eventHandler = VideoLoaderViewEventHandler()
        eventHandler.roomInfo = roomInfo
        eventHandler.localUid = localUid
        eventHandler.enableProcess = enableProcess
        eventHandler.onRequireRenderVideo = onRequireRenderVideo
        eventHandler.completion = completion
        self.ag_eventHandler = eventHandler
        addGesture()
    }
    
    private func addGesture() {
        if let gesture = ag_gesture {
            gesture.delegate = ag_eventHandler
            return
        }
        let gesture = UILongPressGestureRecognizer(target: self, action: #selector(onGesture(_:)))
        gesture.delegate = ag_eventHandler
        gesture.delaysTouchesBegan = true
        //continue to deliver touches
        gesture.cancelsTouchesInView = false
        gesture.minimumPressDuration = 0.01
        addGestureRecognizer(gesture)
        self.ag_gesture = gesture
    }
    
    @objc func onGesture(_ ges: UIGestureRecognizer) {
        guard let roomInfo = ag_eventHandler?.roomInfo, let localUid = ag_eventHandler?.localUid else {return}
        let unmanaged = Unmanaged.passUnretained(ges)
        let gestureId = "\(unmanaged.toOpaque())"
        
        //只允许一个item被预加载到
        guard ag_gestureId.count == 0 || ag_gestureId == gestureId else { return }
        
        switch ges.state {
        case .began:
            ag_gestureId = gestureId
            self.ag_touchTL = NSValue(cgPoint: convert(CGPoint.zero, to: api_getWinwdow()))
            debugLoaderPrint("[UI]onGesture began")
            guard ag_eventHandler?.enableProcess?(.began) ?? true else {
                debugLoaderPrint("[UI]room[\(roomInfo.channelName())] disable gesture[began]")
                return
            }
            for anchorInfo in roomInfo.anchorInfoList {
                VideoLoaderApiImpl.shared.switchAnchorState(newState: .joinedWithVideo,
                                                            localUid: localUid,
                                                            anchorInfo: anchorInfo,
                                                            tagId: roomInfo.channelName())
                let container = VideoCanvasContainer()
                if let renderView = self.ag_eventHandler?.onRequireRenderVideo?(anchorInfo, container) {
                    container.uid = anchorInfo.uid
                    container.container = renderView
                    VideoLoaderApiImpl.shared.renderVideo(anchorInfo: anchorInfo, container: container)
                }
            }
        case .cancelled, .ended:
            if ag_gestureId.isEmpty {return}
            ag_gestureId = ""
            let point = ges.location(in: self)
            let currentTl = convert(CGPoint.zero, to:api_getWinwdow())
            
            if bounds.contains(point), let origTl = self.ag_touchTL?.cgPointValue, origTl == currentTl {
                debugLoaderPrint("[UI]onGesture ended")
                guard ag_eventHandler?.enableProcess?(.ended) ?? true else {
                    debugLoaderPrint("[UI]room[\(roomInfo.channelName())] disable gesture[ended]")
                    return
                }
                ag_eventHandler?.completion?()
                
                //上报耗时开始
                VideoLoaderApiImpl.shared.startMediaRenderingTracing(anchorId: roomInfo.channelName())
                return
            }
            debugLoaderPrint("[UI]onGesture cancel")
            for anchorInfo in roomInfo.anchorInfoList {
                VideoLoaderApiImpl.shared.switchAnchorState(newState: .idle,
                                                            localUid: localUid,
                                                            anchorInfo: anchorInfo,
                                                            tagId: roomInfo.channelName())
            }
        default:
            break
        }
    }
}

class VideoLoaderViewEventHandler: NSObject, UIGestureRecognizerDelegate {
    var roomInfo: IVideoLoaderRoomInfo?
    var localUid: UInt = 0
    var enableProcess: ((UIGestureRecognizer.State)->Bool)?
    var onRequireRenderVideo: ((AnchorInfo, VideoCanvasContainer)->UIView?)?
    var completion: (()->())?
    
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        return true
    }
}
