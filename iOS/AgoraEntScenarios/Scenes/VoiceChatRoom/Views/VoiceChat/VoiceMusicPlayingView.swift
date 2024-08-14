//
//  VoiceMusicPlayingView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/5/23.
//

import UIKit

let kLabelCount = 2
let kDefaultFadeLength: CGFloat = 7
// pixel buffer space between scrolling label
let kDefaultLabelBufferSpace: CGFloat = 20
let kDefaultPixelsPerSecond = 30.0
let kDefaultPauseTime = 1.5

enum AutoScrollDirection {
    case Right
    case Left
}

class AutoScrollLabel: UIView {
    
    var scrollDirection = AutoScrollDirection.Right {
        didSet {
            scrollLabelIfNeeded()
        }
    }
    var scrollSpeed = 30.0 {// pixels per second, defaults to 30
        didSet {
            scrollLabelIfNeeded()
        }
    }
    var pauseInterval = 1.5 // defaults to 1.5
    var labelSpacing: CGFloat = 20 // pixels, defaults to 20
    
    var animationOptions: UIView.AnimationOptions = .curveEaseInOut
    
    /**
    * Returns YES, if it is actively scrolling, NO if it has paused or if text is within bounds (disables scrolling).
    */
    var scrolling = false
    var fadeLength: CGFloat = 7 { // defaults to 7
        didSet {
            if oldValue != fadeLength {
                refreshLabels()
                applyGradientMaskForFadeLength(fadeLength: fadeLength, enableFade: false)
            }
        }
    }
    
    // UILabel properties
    var text: String? {
        get {
            return mainLabel.text
        }
        set {
            setText(text: newValue, refresh: true)
        }
    }
    
    func setText(text: String?, refresh: Bool) {
        // ignore identical text changes
        
        if text == self.text {
            return
        }
        
        for l in labels {
            l.text = text
        }
        
        if refresh {
            refreshLabels()
        }
    }
    
    var attributedText: NSAttributedString? {
        get {
            return mainLabel.attributedText
        }
        set {
            setAttributedText(text: newValue, refresh: true)
        }
    }
    
    func setAttributedText(text: NSAttributedString?, refresh: Bool) {
        if text == self.attributedText {
            return
        }
        
        for l in labels {
            l.attributedText = text
        }
        
        if refresh {
            refreshLabels()
        }
    }
    
    var textColor: UIColor? {
        get {
            return self.mainLabel.textColor
        }
        set {
            for lab in labels {
                lab.textColor = newValue
            }
        }
    }
    
    var font: UIFont? {
        get {
            return mainLabel.font
        }
        set {
            mainLabel.font = newValue
            refreshLabels()
            invalidateIntrinsicContentSize()
        }
    }
    
    var shadowColor: UIColor? {
        get {
            return self.mainLabel.shadowColor
        }
        set {
            for lab in labels {
                lab.shadowColor = newValue
            }
        }
    }
    
    var shadowOffset: CGSize! {
        get {
            return self.mainLabel.shadowOffset
        }
        set {
            for lab in labels {
                lab.shadowOffset = newValue
            }
        }
    }
    
    var textAlignment: NSTextAlignment = .left // only applies when not auto-scrolling
    
    // views
    private var labels = [UILabel]()
    private var mainLabel: UILabel! {
        if labels.count > 0 {
            return labels[0]
        }
        return nil
    }
    
    private var sv: UIScrollView!
    private var scrollView: UIScrollView! {
        get {
            if sv == nil {
                sv = UIScrollView(frame: self.bounds)
                sv.autoresizingMask = [UIView.AutoresizingMask.flexibleWidth, UIView.AutoresizingMask.flexibleHeight]
                sv.backgroundColor = .clear
            }
            return sv
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        commonInit()
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        commonInit()
    }
    
   private func commonInit() {
        addSubview(scrollView)
        // create the labels
        for _ in 0 ..< kLabelCount {
            let label = UILabel()
            label.backgroundColor = .clear
            label.autoresizingMask = autoresizingMask
            
            // store labels
            scrollView.addSubview(label)
            labels.append(label)
        }
        
        // default values
        scrollDirection = AutoScrollDirection.Left
        scrollSpeed = kDefaultPixelsPerSecond
        pauseInterval = kDefaultPauseTime
        labelSpacing = kDefaultLabelBufferSpace
        textAlignment = .left
        animationOptions = .curveLinear
        scrollView.showsVerticalScrollIndicator = false
        scrollView.showsHorizontalScrollIndicator = false
        scrollView.isScrollEnabled = false
        isUserInteractionEnabled = true
        backgroundColor = .clear
        clipsToBounds = true
        fadeLength = kDefaultFadeLength
    }
    
    override var frame: CGRect {
        get {
            return super.frame
        }
        set {
            super.frame = newValue
            didChangeFrame()
        }
    }
    
    override var bounds: CGRect {
        get {
            return super.bounds
        }
        set {
            super.bounds = newValue
            didChangeFrame()
        }
    }
    
   private func didChangeFrame() {
        refreshLabels()
        applyGradientMaskForFadeLength(fadeLength: fadeLength, enableFade: scrolling)
    }
    
    override var intrinsicContentSize: CGSize {
        CGSize(width: 0, height: mainLabel.intrinsicContentSize.height)
    }
    
   private func observeApplicationNotifications() {
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(scrollLabelIfNeeded),
                                               name: UIApplication.willEnterForegroundNotification,
                                               object: nil)
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(scrollLabelIfNeeded),
                                               name: UIApplication.didBecomeActiveNotification,
                                               object: nil)
    }
    
    @objc
    private func enableShadow() {
        scrolling = true
        applyGradientMaskForFadeLength(fadeLength: self.fadeLength, enableFade: true)
    }
    
    @objc
    private func scrollLabelIfNeeded() {
        if text == nil || text!.count == 0 {
            return
        }
        
        let labelWidth = CGRectGetWidth(mainLabel.bounds)
        if labelWidth <= CGRectGetWidth(bounds) {
            return
        }
        
        NSObject.cancelPreviousPerformRequests(withTarget: self,
                                               selector: #selector(scrollLabelIfNeeded),
                                               object: nil)
        NSObject.cancelPreviousPerformRequests(withTarget: self,
                                               selector: #selector(enableShadow),
                                               object: nil)
        
        scrollView.layer.removeAllAnimations()
        
        let doScrollLeft = scrollDirection == AutoScrollDirection.Left
        scrollView.contentOffset = doScrollLeft ? CGPointZero : CGPointMake(labelWidth + labelSpacing, 0)
        
        perform(#selector(enableShadow), with: nil, afterDelay: pauseInterval)
        
        // animate the scrolling
        let duration = Double(labelWidth) / scrollSpeed
        
        UIView.animate(withDuration: duration,
                       delay: pauseInterval,
                       options: [animationOptions, .allowUserInteraction],
            animations: { () -> Void in
                // adjust offset
                self.scrollView.contentOffset = doScrollLeft ? CGPointMake(labelWidth + self.labelSpacing, 0) : CGPointZero
                
        }, completion: { [self] finished in
            self.scrolling = false
            
            // remove the left shadow
            self.applyGradientMaskForFadeLength(fadeLength: self.fadeLength, enableFade: false)
            
            // setup pause delay/loop
            if finished {
                self.perform(#selector(scrollLabelIfNeeded), with: nil, afterDelay: 0)
            }
        })
    }
    
    @objc
    private func refreshLabels() {
        var offset: CGFloat = 0
        guard mainLabel != nil else { return }
        for lab in labels {
            lab.sizeToFit()
            
            lab.font = font
            var frame = lab.frame
            frame.origin = CGPoint(x: offset, y: 0)
            frame.size.height = bounds.height
            lab.frame = frame
            
            lab.center = CGPoint(x: lab.center.x, y: round(center.y - CGRectGetMinY(self.frame)))
            
            offset += CGRectGetWidth(lab.bounds) + labelSpacing
            
            lab.isHidden = false
        }
        
        scrollView.contentOffset = CGPointZero
        scrollView.layer.removeAllAnimations()
        
        // if the label is bigger than the space allocated, then it should scroll
        if CGRectGetWidth(mainLabel.bounds) > CGRectGetWidth(bounds) {
            var size = CGSize(width: 0, height: 0)
            size.width = CGRectGetWidth(mainLabel.bounds) + CGRectGetWidth(bounds) + labelSpacing
            size.height = CGRectGetHeight(bounds)
            scrollView.contentSize = size
            
            applyGradientMaskForFadeLength(fadeLength: fadeLength, enableFade: scrolling)
            
            scrollLabelIfNeeded()
        } else {
            for lab in labels {
                lab.isHidden = lab != mainLabel
            }
            
            // adjust the scroll view and main label
            scrollView.contentSize = bounds.size
            mainLabel.frame = bounds
            mainLabel.isHidden = false
            mainLabel.textAlignment = textAlignment
            
            // cleanup animation
            scrollView.layer.removeAllAnimations()
            
            applyGradientMaskForFadeLength(fadeLength: 0, enableFade: false)
        }
        
    }
    
    private func applyGradientMaskForFadeLength(fadeLength: CGFloat, enableFade fade: Bool) {
        if mainLabel == nil {
            return
        }
        let labelWidth = CGRectGetWidth(mainLabel.bounds)
        if labelWidth <= CGRectGetWidth(bounds) {
            self.fadeLength = 0
        }
        
        if fadeLength != 0 {
            // Recreate gradient mask with new fade length
            let gradientMask = CAGradientLayer()
            
            gradientMask.bounds = layer.bounds
            gradientMask.position = CGPointMake(CGRectGetMidX(bounds), CGRectGetMidY(bounds))
            
            gradientMask.shouldRasterize = true
            gradientMask.rasterizationScale = UIScreen.main.scale
            
            gradientMask.startPoint = CGPointMake(0, CGRectGetMidY(frame))
            gradientMask.endPoint = CGPointMake(1, CGRectGetMidY(frame))
            
            // setup fade mask colors and location
            let transparent = UIColor.clear.cgColor
            
            let opaque = UIColor.black.cgColor
            gradientMask.colors = [transparent, opaque, opaque, transparent]
            
            // calcluate fade
            let fadePoint = fadeLength / CGRectGetWidth(bounds)
            var leftFadePoint = fadePoint
            var rightFadePoint = 1 - fadePoint
            if !fade {
                switch (scrollDirection) {
                case .Left:
                    leftFadePoint = 0
                    
                case .Right:
                    leftFadePoint = 0
                    rightFadePoint = 1
                }
            }
            
            // apply calculations to mask
            gradientMask.locations = [NSNumber(value: 0),
                                      NSNumber(value: leftFadePoint),
                                      NSNumber(value: rightFadePoint),
                                      NSNumber(value: 1)]
            
            // don't animate the mask change
            CATransaction.begin()
            CATransaction.setDisableActions(true)
            self.layer.mask = gradientMask
            CATransaction.commit()
        } else {
            layer.mask = nil
        }
    }
    
    private func onUIApplicationDidChangeStatusBarOrientationNotification(notification: NSNotification) {
        // delay to have it re-calculate on next runloop
        perform(#selector(refreshLabels), with: nil, afterDelay: 0.1)
        perform(#selector(scrollLabelIfNeeded), with: nil, afterDelay: 0.1)
    }
}

class VoiceMusicPlayingView: UIView {
    var onClickAccompanyButtonClosure: ((Bool) -> Void)?
    var onClickBGMClosure: ((VoiceMusicModel?) -> Void)?
    var onUpdateBGMClosure: ((VoiceMusicModel?) -> Void)?
    private lazy var titleLabel: AutoScrollLabel = {
        let label = AutoScrollLabel()
        label.text = ""
        label.textColor = UIColor(hex: "#FFFFFF", alpha: 1.0)
        label.font = .systemFont(ofSize: 12)
        label.textAlignment = .right
        label.labelSpacing = 10
        label.pauseInterval = 0
        label.scrollSpeed = 30
        label.fadeLength = 12
        label.scrollDirection = .Left
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var accompanyButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "voice_accompany_room_on"), for: .normal)
        button.setImage(UIImage.sceneImage(name: "voice_accompany_room_off"), for: .selected)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(onClickAccompanyButton(sender:)), for: .touchUpInside)
        button.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        button.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return button
    }()
    private var voiceModel: VoiceMusicModel?
    private var isOwner: Bool = false
    
    init(isOwner: Bool) {
        super.init(frame: .zero)
        self.isOwner = isOwner
        setupUI()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func updateOriginButtonStatus(isOrigin: Bool) {
        accompanyButton.isSelected = isOrigin
        ChatRoomServiceImp.getSharedInstance().updateRoomBGM(songName: voiceModel?.name, singerName: voiceModel?.singer, isOrigin: isOrigin)
    }
    
    func setupMusic(model: VoiceMusicModel, isOrigin: Bool) {
        voiceModel = model
        if model.name?.isEmpty == false {
            titleLabel.text = "\(model.name ?? "")-\(model.singer ?? "")"
        }
        accompanyButton.isSelected = isOrigin
        ChatRoomServiceImp.getSharedInstance().updateRoomBGM(songName: model.name, singerName: model.singer, isOrigin: isOrigin)
    }
    
    func eventHandler(roomId: String?) {
        ChatRoomServiceImp.getSharedInstance().subscribeRoomBGMChange(roomId: roomId) { songName, singerName, isOrigin in
            self.isHidden = songName == nil
            self.titleLabel.text = "\(songName ?? "")-\(singerName ?? "")"
            self.accompanyButton.isSelected = !isOrigin
            let model = VoiceMusicModel()
            model.name = songName
            model.singer = singerName
            model.status = .playing
            self.onUpdateBGMClosure?(model)
        }
    }
    
    private func setupUI() {
        addSubview(accompanyButton)
        addSubview(titleLabel)
        
        widthAnchor.constraint(equalToConstant: 180).isActive = true
        heightAnchor.constraint(equalToConstant: 20).isActive = true
        
        accompanyButton.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -16).isActive = true
        accompanyButton.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
        
        titleLabel.trailingAnchor.constraint(equalTo: accompanyButton.leadingAnchor, constant: -5).isActive = true
        titleLabel.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
        titleLabel.heightAnchor.constraint(equalTo: heightAnchor).isActive = true
        titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        
        let tap = UITapGestureRecognizer(target: self, action: #selector(onClickBGMHandler))
        titleLabel.addGestureRecognizer(tap)
    }
    
    @objc
    private func onClickAccompanyButton(sender: UIButton) {
        guard isOwner else { return }
        sender.isSelected = !sender.isSelected
        onClickAccompanyButtonClosure?(sender.isSelected)
    }
    
    @objc
    private func onClickBGMHandler() {
        onClickBGMClosure?(voiceModel)
    }
}
