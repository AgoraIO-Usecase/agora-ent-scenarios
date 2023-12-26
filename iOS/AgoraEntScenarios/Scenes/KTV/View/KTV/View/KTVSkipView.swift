//
//  KTVSkipView1.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/12.
//

import Foundation

enum SkipActionType: Int {
    case down = 0
    case cancel = 1
}

enum SkipType: Int {
    case prelude = 0
    case epilogue = 1
}

typealias OnSkipCallback = (SkipActionType) -> Void

class KTVSkipView: UIView {
    
    private var bgView: UIView!
    private var skipBtn: UIButton!
    private var canCelBtn: UIButton!
    private var completion: OnSkipCallback?
    
    init(frame: CGRect, completion: OnSkipCallback?) {
        super.init(frame: frame)
        self.completion = completion
        setupUI()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        bgView = UIView()
        bgView.backgroundColor = UIColor(red: 63/255.0, green: 64/255.0, blue: 93/255.0, alpha: 1)
        bgView.layer.borderColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.6).cgColor
        addSubview(bgView)
        
        skipBtn = UIButton()
        skipBtn.titleLabel?.font = UIFont.systemFont(ofSize: 15)
        skipBtn.setTitle("  跳过前奏", for: .normal)
        skipBtn.setTitleColor(.white, for: .normal)
        skipBtn.addTarget(self, action: #selector(skip(_:)), for: .touchUpInside)
        skipBtn.tag = 200
        bgView.addSubview(skipBtn)
        
        canCelBtn = UIButton()
        canCelBtn.titleLabel?.font = UIFont.systemFont(ofSize: 13)
        canCelBtn.setImage(UIImage.sceneImage(name: "x"), for: .normal)
        canCelBtn.tag = 201
        canCelBtn.addTarget(self, action: #selector(skip(_:)), for: .touchUpInside)
        canCelBtn.setTitleColor(.white, for: .normal)
        bgView.addSubview(canCelBtn)
    }
        
    func setSkipType(_ type: SkipType) {
        let title = type == .prelude ? Bundle.localizedString("ktv_skip_pre", bundleName: "KtvResource") : Bundle.localizedString("ktv_skip_end", bundleName: "KtvResource")
        skipBtn.setTitle(title, for: .normal)
    }
    
    @objc private func skip(_ btn: UIButton) {
        let actionType: SkipActionType = (btn.tag == 200) ? .down : .cancel
        completion?(actionType)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        self.bgView.frame = self.bounds
        self.bgView.layer.cornerRadius = self.bounds.size.height / 2.0
        self.bgView.layer.masksToBounds = true
        self.bgView.layer.borderWidth = 1
        self.skipBtn.frame = CGRect(x: 10, y: 0, width: self.bounds.size.width / 3.0 * 2 , height: self.bounds.size.height)
        self.canCelBtn.frame = CGRect(x: self.bounds.size.width / 3.0 * 2, y: 0, width: self.bounds.size.width / 3.0 , height: self.bounds.size.height)
    }
}
