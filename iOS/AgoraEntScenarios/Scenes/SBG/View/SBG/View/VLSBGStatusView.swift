//
//  VLSBGStatusView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/5/11.
//

import UIKit

@objc enum SBGClickAction: Int {
    case chooseSong
    case randomSelectSong
    case sbg
    case nextSong
    case effect
    case origin
    case again
    case aac
}

@objc protocol VLSBGStatusViewDelegate: NSObjectProtocol {
    @objc func didSbgActionChanged(_ action: SBGClickAction)
}

@objc public enum SBGState: Int {
    /**
     0 房主点歌
     1 观众等待
     2 准备开始
     3 倒计时
     4 抢唱中
     5 抢唱成功
     6 无人抢唱
     7 演唱中
     8 挑战失败
     9 挑战成功
     10 下一首
     11 结算
     */
    case ownerOrderMusic
    case audienceWating
    case ready
    case timeDownAudience
    case timeDownBroadcaster
    case sbgingOnSeat
    case sbgingOffSeat
    case sbgSuccess
    case sbgNobody
    case singingBroadcaster
    case singingAudience
    case singFailed
    case singSuccess
    case next
    case resultOwner
    case resultAudience

}

class VLSBGStatusView: UIView {
    
    @objc weak var delegate: VLSBGStatusViewDelegate?
    
    private var basicScoreModels  = {
        var models = [SubRankModel]()
        var model1 = SubRankModel()
        model1.index = 1
        
        var model2 = SubRankModel()
        model2.index = 2
        
        var model3 = SubRankModel()
        model3.index = 3
        
        models.append(model1)
        models.append(model2)
        models.append(model3)
        return models
    }()
    
    @objc public var dataSource: [SubRankModel]? {
        didSet {
            if dataSource == nil {
                dataSource = basicScoreModels
            } else {
                if let data = dataSource, data.count < 3 {
                    for i in data.count..<3 {
                        if let scoreModel = basicScoreModels[safe: i] {
                            dataSource!.append(scoreModel)
                        }
                    }
                }
            }
            tableView.reloadData()
        }
    }

   @objc var state: SBGState = .ownerOrderMusic {
        didSet {
            if state == .ownerOrderMusic {
                numLabel.isHidden = true
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "暂无演唱歌单，开始点歌吧！"
                lrcView.isHidden = true
                randomBtn.isHidden = false
                orderBtn.isHidden = false
                nextBtn.isHidden = true
            } else if state == .audienceWating {
                numLabel.isHidden = true
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "房主点歌中，游戏即将开始"
                lrcView.isHidden = true
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .ready {
                numLabel.isHidden = true
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "嗨唱开始"
                lrcView.isHidden = true
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .timeDownAudience {
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                lrcView.isHidden = false
                lrcView.state = .timeDownAudience
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .timeDownBroadcaster {
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                lrcView.isHidden = false
                lrcView.state = .timeDownBroadcaster
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .sbgingOnSeat {
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                lrcView.isHidden = false
                lrcView.state = .sbgingOnSeat
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            }else if state == .sbgingOffSeat {
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                lrcView.isHidden = false
                lrcView.state = .sbgingOffSeat
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .sbgSuccess {
                numLabel.isHidden = true
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = ""
                lrcView.isHidden = true
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .sbgNobody {
                numLabel.isHidden = true
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "本轮无人演唱\n即将播放下一首"
                contentTextLabel.numberOfLines = 0
                lrcView.isHidden = true
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .singingBroadcaster {
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                lrcView.isHidden = false
                lrcView.state = .singingBroadcaster
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .singingAudience {
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                lrcView.isHidden = false
                lrcView.state = .singingAudience
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .singFailed {
                numLabel.isHidden = true
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
               // contentTextLabel.text = "挑战失败"
                lrcView.isHidden = true
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .singSuccess {
                numLabel.isHidden = true
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
               // contentTextLabel.text = "挑战成功"
                lrcView.isHidden = true
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .next {
                numLabel.isHidden = true
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "下一首"
                lrcView.isHidden = true
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            } else if state == .resultOwner {
                numLabel.isHidden = true
                contentImgView.isHidden = true
                tableView.isHidden = false
                resultTitleLabel.isHidden = false
                lrcView.isHidden = true
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = false
            } else if state == .resultAudience {
                numLabel.isHidden = true
                contentImgView.isHidden = true
                tableView.isHidden = false
                resultTitleLabel.isHidden = false
                lrcView.isHidden = true
                randomBtn.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
            }
            
            if state == .singFailed {
                contentImgView.image = UIImage.sceneImage(name: "sbg-bg-fail")
                fightImage.isHidden = false
                fightResult.isHidden = false
                contentTextLabel.isHidden = true
            } else if state == .singSuccess {
                contentImgView.image = UIImage.sceneImage(name: "sbg-bg-success")
                fightResult.isHidden = false
                fightImage.isHidden = false
                contentTextLabel.isHidden = true
            } else {
                contentImgView.image = UIImage.sceneImage(name: "sbg-bg-text")
                fightImage.isHidden = true
                fightResult.isHidden = true
                contentTextLabel.isHidden = false
            }
            
            attributeView.isHidden = state != .sbgSuccess
        }
    }
    
    private lazy var bgImgView: UIImageView =  {//背景图
        let imgView = UIImageView()
        imgView.image = UIImage.sceneImage(name: "sbg-bg-main")
        return imgView
    }()
    
    private lazy var orderBtn: UIButton = { //点歌按钮
        let btn = UIButton()
        btn.setBackgroundImage(UIImage.sceneImage(name: "sbg-btn-diange"), for: .normal)
        btn.addTargetFor(self, action: #selector(choose), for: .touchUpInside)
        return btn
    }()
    
    var attributeView: SBGAttributeView = {
        let attrView = SBGAttributeView()
        return attrView
    }()
    
    private lazy var randomBtn: UIButton = { //随机选歌按钮
        let btn = UIButton()
        btn.setTitle("随机选歌开始", for: .normal)
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 15)
        btn.layer.cornerRadius = 17
        btn.layer.masksToBounds = true
        btn.layer.borderColor = UIColor.lightGray.cgColor
        btn.layer.borderWidth = 1
        btn.setTitleColor(.white, for: .normal)
        btn.addTargetFor(self, action: #selector(randomChoose), for: .touchUpInside)
        return btn
    }()
    
    private lazy var numLabel: UILabel = {//左上角显示播放歌曲index的lable
        let label = UILabel()
        label.backgroundColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.15)
        label.textColor = .white
        label.layer.cornerRadius = 10
        label.layer.masksToBounds = true
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 12)
        return label
    }()
    
    private lazy var contentView: UIView = { //中间的view
        let view = UIView()
        return view
    }()
    
    private lazy var contentTextLabel: UILabel = { //富文本
        let label = UILabel()
        label.textColor = .white
        label.textAlignment = .center
       // label.numberOfLines = 0
        label.font = UIFont.systemFont(ofSize: 18, weight: .bold)
        return label
    }()
    
    private lazy var contentImgView: UIImageView = { //挑战结果
        let imgView = UIImageView()
        imgView.image = UIImage.sceneImage(name: "sbg-bg-text")
        return imgView
    }()
    
    private lazy var scoreLabel: UILabel = { //分数label
        let label = UILabel()
        return label
    }()
    
    private lazy var resultTitleLabel: UILabel = { //本轮评分
        let label = UILabel()
        label.text = "本轮评分"
        label.textAlignment = .center
        label.textColor = .white
        return label
    }()
    
    private lazy var tableView: UITableView = { //分数的tableView
        let tableView = UITableView()
        tableView.registerCell(SBGScoreTitleCell.self, forCellReuseIdentifier: "title")
        tableView.registerCell(SBGScoreCell.self, forCellReuseIdentifier: "score")
        tableView.dataSource = self
        tableView.delegate = self
        tableView.backgroundColor = .clear
        return tableView
    }()
    
    private lazy var fightImage: UIImageView = {
        let imgView = UIImageView()
        imgView.contentMode = .scaleAspectFit
        return UIImageView()
    }()
    
    private lazy var fightResult: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 18)
        return label
    }()
    
    private lazy var nextBtn: UIButton = { //下一首按钮
        let btn = UIButton()
        btn.setBackgroundImage(UIImage.sceneImage(name: "sbg-btn-again"), for: .normal)
        btn.addTargetFor(self, action: #selector(again), for: .touchUpInside)
        return btn
    }()
    
    @objc public lazy var lrcView: VLSBGLyricView = {
        let lrcView: VLSBGLyricView = VLSBGLyricView()
        return lrcView
    }()
    
    @objc public var countTime: Int = 0{
        didSet {
            print("countTime:\(countTime)")
            lrcView.setCountTime(time: countTime)
        }
    }
    
    @objc public var micOwner: String = "" {
        didSet {
            
        }
    }
    
    @objc func setMicOwner(with owner: String, url: String) {
        attributeView.setMicOwner(with: owner, url: url)
    }
    
    @objc public var contentStr: String = "" {
        didSet {
            contentTextLabel.text = contentStr
        }
    }
    
    @objc func setFight(_ isSuccess: Bool, score: Int) {
        contentImgView.image = UIImage.sceneImage(name: isSuccess ? "sbg-bg-success" : "sbg-bg-fail")
        fightImage.image = UIImage.sceneImage(name: isSuccess ? "sbg-挑战成功" : "sbg-挑战失败")
        fightResult.text = "\(score)"
    }
    
    @objc public var numStr: String = "" {
        didSet {
            numLabel.text = numStr
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        addSubview(bgImgView)
        
        addSubview(numLabel)
        addSubview(contentImgView)
        contentImgView.addSubview(contentTextLabel)
        contentImgView.addSubview(fightImage)
        contentImgView.addSubview(fightResult)
        contentImgView.addSubview(attributeView)
        attributeView.isHidden = true
        
        addSubview(lrcView)
        
        addSubview(orderBtn)
        
        addSubview(randomBtn)
        
        addSubview(resultTitleLabel)
        
        addSubview(tableView)
        
        addSubview(nextBtn)

    }
    
    @objc private func choose() {//点歌
        guard let delegate = delegate else {return}
        delegate.didSbgActionChanged(.chooseSong)
    }
    
    @objc private func randomChoose() {//随机点歌
        guard let delegate = delegate else {return}
        delegate.didSbgActionChanged(.randomSelectSong)
    }
    
    @objc private func again() {//再来一轮
        guard let delegate = delegate else {return}
        delegate.didSbgActionChanged(.again)
    }
    
    @objc private func sbg() {//抢唱
        guard let delegate = delegate else {return}
        delegate.didSbgActionChanged(.sbg)
    }
    
    @objc private func nextSong() {//切歌
        guard let delegate = delegate else {return}
        delegate.didSbgActionChanged(.nextSong)
    }
    
    @objc private func effect() {//调音
        guard let delegate = delegate else {return}
        delegate.didSbgActionChanged(.effect)
    }
    
    @objc private func trackChange() {//原唱
        guard let delegate = delegate else {return}
        delegate.didSbgActionChanged(.origin)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bgImgView.frame = self.bounds
        
        numLabel.frame = CGRect(x: 10, y: 10, width: 50, height: 20)
        
        contentImgView.frame = CGRect(x: 0, y: self.bounds.height / 2.0 - 32, width: self.bounds.width, height: 64)
        
        contentTextLabel.frame = CGRect(x: 0, y: 0, width: self.bounds.width, height: 64)
        
        fightImage.frame = CGRect(x: self.bounds.width / 2.0 - 45, y: 10, width: 90, height: 24)
        fightResult.frame = CGRect(x: self.bounds.width / 2.0 - 45, y: 40, width: 90, height: 24)
        attributeView.frame = CGRect(x: 0, y: 0, width: self.bounds.width, height: 64)
        
        orderBtn.frame = CGRect(x: self.bounds.width / 2.0 - 114, y: self.bounds.height - 68, width: 84, height: 34)
        
        randomBtn.frame = CGRect(x: self.bounds.width / 2.0 - 114 + 108, y: self.bounds.height - 68, width: 120, height: 34)
        
        resultTitleLabel.frame = CGRect(x: self.bounds.width / 2.0 - 50, y: 8, width: 100, height: 22)
        
        tableView.frame = CGRect(x: 0, y: resultTitleLabel.frame.maxY + 8, width: self.bounds.width, height: self.bounds.height - resultTitleLabel.frame.maxY - 8)
        
        nextBtn.frame = CGRect(x: self.bounds.width / 2.0 - 55, y: self.bounds.height - 18 - 34, width: 110, height: 34)
        
        lrcView.frame = CGRect(x: 12, y: 36, width: self.bounds.width - 24, height: self.bounds.height - 40)
        
    }
}

extension VLSBGStatusView: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return (dataSource?.count ?? 3) + 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.row == 0 {
            let cell : SBGScoreTitleCell = tableView.dequeueReusableCell(withIdentifier: "title") as! SBGScoreTitleCell
            return cell
        } else {
            let cell : SBGScoreCell = tableView.dequeueReusableCell(withIdentifier: "score") as! SBGScoreCell
            if let model: SubRankModel = dataSource?[indexPath.row - 1] {
                cell.score = model
            }
            return cell
        }
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return indexPath.row == 0 ? 28 : 38
    }
}
