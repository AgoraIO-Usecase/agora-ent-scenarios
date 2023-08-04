//
//  VLSBGStatusView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/5/11.
//

import UIKit

@objc enum SRClickAction: Int {
    case startGame
    case sbg
    case nextSong
    case effect
    case origin
    case again
    case aac
}

@objc protocol VLSRStatusViewDelegate: NSObjectProtocol {
    @objc func didSrActionChanged(_ action: SRClickAction)
}

@objc public enum SBGState: Int {
    /**
     0 房主点歌
     1 玩家等待
     2 观众等待
     3 观众播放过程中
     4 玩家们即将抢唱
     5 玩家倒计时
     6 玩家抢唱中
     7 某个玩家抢唱到
     8 抢唱开始
     9 房主抢到
     10 房主未抢到
     11 玩家抢到
     12 玩家未抢到
     13 房主结算
     14 观众结算
     */
    case ownerOrderMusic
    case playerWating
    case audienceWating
    case audiencePlaying
    case timedownPlayersred //房主和合唱着的倒计时界面
    case timedownPlayerunsred //房主和合唱着的倒计时界面
    case timedownOwnersred //房主和合唱着的倒计时界面
    case timedownOwnerunsred //房主和合唱着的倒计时界面
    case ownerPrepare
    case audiencePrepare
    case ready
    case timeDown
    case srging
    case playerSred
    case readyToStart
    case ownerSredAndPlaying
    case ownerUnSredAndPlaying
    case playerSredAndPlaying
    case playerUnsredAndPlaying
    case resultOwner
    case resultAudience

}

class VLSRStatusView: UIView {
    
    @objc weak var delegate: VLSRStatusViewDelegate?
    
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
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.isHidden = true
                lrcView.isHidden = true
                orderBtn.isHidden = false
                nextBtn.isHidden = true
                noticeBtn.isHidden = false
                srBtn.isHidden = true
                notiView.isHidden = false
            } else if state == .playerWating {
                numLabel.isHidden = true
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = false
                contentTextLabel.text = "房主点歌中，游戏即将开始"
                lrcView.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                noticeBtn.isHidden = false
                srBtn.isHidden = true
                notiView.isHidden = false
            }else if state == .audienceWating {
                numLabel.isHidden = true
                contentImgView.isHidden = false
                tableView.isHidden = true
                contentTextLabel.isHidden = false
                contentTextLabel.text = "房主点歌中，游戏即将开始"
                lrcView.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                noticeBtn.isHidden = false
                srBtn.isHidden = true
                notiView.isHidden = false
            } else if state == .audiencePlaying {
                //显示歌曲的序号 同时需要修改布局
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "房主点歌中，游戏即将开始"
                lrcView.isHidden = false
                lrcView.state = .singingAudience
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                noticeBtn.isHidden = false
                srBtn.isHidden = true
                notiView.isHidden = true
            } else if state == .ownerPrepare { //即将抢唱
                numLabel.isHidden = true
                noticeBtn.isHidden = false
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.isHidden = false
                contentTextLabel.text = "嗨唱开始"
                lrcView.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                srBtn.isHidden = true
                notiView.isHidden = true
            } else if state == .audiencePrepare { //即将抢唱
                numLabel.isHidden = true
                noticeBtn.isHidden = false
                contentImgView.isHidden = false
                tableView.isHidden = true
                contentTextLabel.isHidden = false
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "嗨唱开始"
                lrcView.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                srBtn.isHidden = true
                notiView.isHidden = true
            } else if state == .timedownPlayersred {
                numLabel.isHidden = false
                noticeBtn.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                contentTextLabel.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "嗨唱开始"
                lrcView.isHidden = false
                lrcView.state = .playerWithSinging
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                srBtn.isHidden = false
                srBtn.isEnabled = false
                notiView.isHidden = true
            }else if state == .timedownOwnersred {
                numLabel.isHidden = false
                noticeBtn.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                contentTextLabel.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "嗨唱开始"
                lrcView.isHidden = false
                lrcView.state = .broadcasterWithSinging
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                srBtn.isHidden = false
                srBtn.isEnabled = false
                notiView.isHidden = true
            } else if state == .timedownOwnerunsred {
                numLabel.isHidden = false
                noticeBtn.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                contentTextLabel.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "嗨唱开始"
                lrcView.isHidden = false
                lrcView.state = .broadcasterWithoutSinging
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                srBtn.isHidden = false
                srBtn.isEnabled = false
                notiView.isHidden = true
            } else if state == .timedownPlayerunsred {
                numLabel.isHidden = false
                noticeBtn.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                contentTextLabel.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "嗨唱开始"
                lrcView.isHidden = false
                lrcView.state = .playerWithoutSinging
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                srBtn.isHidden = false
                srBtn.isEnabled = false
                notiView.isHidden = true
            } else if state == .ready { //即将抢唱
                numLabel.isHidden = false
                noticeBtn.isHidden = false
                contentImgView.isHidden = false
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "即将抢唱下轮歌曲"
                lrcView.isHidden = false
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                notiView.isHidden = true
            } else if state == .srging {
                numLabel.isHidden = false
                noticeBtn.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.isHidden = true
                contentTextLabel.text = "你看不见我"
                lrcView.isHidden = false
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                
                //显示抢唱
                srBtn.isHidden = false
                srBtn.isEnabled = true
                notiView.isHidden = true
            } else if state == .playerSred {
                numLabel.isHidden = true
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "房主点歌中，游戏即将开始"
                lrcView.isHidden = false
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                noticeBtn.isHidden = true
                notiView.isHidden = true
            }else if state == .readyToStart {
                numLabel.isHidden = true
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = "嗨唱开始"
                lrcView.isHidden = false
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                noticeBtn.isHidden = true
                notiView.isHidden = true
            } else if state == .ownerSredAndPlaying {//房主抢到并播放
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = ""
                lrcView.isHidden = false
                lrcView.state = .broadcasterWithSinging
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                noticeBtn.isHidden = false
                //显示抢唱
                srBtn.isHidden = false
                srBtn.isEnabled = true
                notiView.isHidden = true
            } else if state == .ownerUnSredAndPlaying { //房主未抢到
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = ""
                lrcView.isHidden = false
                lrcView.state = .broadcasterWithoutSinging
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                noticeBtn.isHidden = false
                //显示抢唱
                srBtn.isHidden = false
                srBtn.isEnabled = true
                notiView.isHidden = true
            } else if state == .playerSredAndPlaying{ //玩家抢到
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = ""
                lrcView.isHidden = false
                lrcView.state = .playerWithSinging
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                noticeBtn.isHidden = false
                //显示抢唱
                srBtn.isHidden = false
                srBtn.isEnabled = true
                notiView.isHidden = true
            } else if state == .playerUnsredAndPlaying { //玩家未抢到
                numLabel.isHidden = false
                contentImgView.isHidden = true
                tableView.isHidden = true
                resultTitleLabel.isHidden = true
                contentTextLabel.text = ""
                lrcView.isHidden = false
                lrcView.state = .playerWithoutSinging
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                noticeBtn.isHidden = false
                //显示抢唱
                srBtn.isHidden = false
                srBtn.isEnabled = true
                notiView.isHidden = true
            }else if state == .resultOwner { //房主结算
                numLabel.isHidden = true
                contentImgView.isHidden = true
                tableView.isHidden = false
                resultTitleLabel.isHidden = false
                lrcView.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = false
                noticeBtn.isHidden = true
                srBtn.isHidden = true
                notiView.isHidden = true
            } else if state == .resultAudience { //观众结算
                numLabel.isHidden = true
                contentImgView.isHidden = true
                tableView.isHidden = false
                resultTitleLabel.isHidden = false
                lrcView.isHidden = true
                orderBtn.isHidden = true
                nextBtn.isHidden = true
                noticeBtn.isHidden = true
                srBtn.isHidden = true
                notiView.isHidden = true
            }

            //如果歌曲index按钮显示 那么他的frame取决于公告是否显示
            if numLabel.isHidden == false {
                numLabel.frame = CGRect(x: noticeBtn.isHidden ? 12 : 70 , y: 10, width: 50, height: 20)
            }
            nextAttrView.frame = CGRect(x: numLabel.frame.maxX + 10, y: 10, width: 160, height: 20)
        }
    }
    
    private lazy var bgImgView: UIImageView =  {//背景图
        let imgView = UIImageView()
        imgView.image = UIImage.sceneImage(name: "sr_main_bg")
        return imgView
    }()
    
    private lazy var orderBtn: UIButton = { //点歌按钮
        let btn = UIButton()
        btn.setBackgroundImage(UIImage.sceneImage(name: "start_sr"), for: .normal)
        btn.addTargetFor(self, action: #selector(startSr), for: .touchUpInside)
        return btn
    }()
    
    private lazy var srBtn: UIButton = { //抢唱按钮
        let btn = UIButton()
        btn.setBackgroundImage(UIImage.sceneImage(name: "sr"), for: .normal)
        btn.setBackgroundImage(UIImage.sceneImage(name: "timedown"), for: .disabled)
        btn.addTargetFor(self, action: #selector(sr), for: .touchUpInside)
        return btn
    }()
    
    var attributeView: SBGAttributeView = {
        let attrView = SBGAttributeView()
        return attrView
    }()
   // sr_next_warning
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
    
    private lazy var noticeBtn: UIButton = {
        let btn = UIButton()
        btn.setTitle("公告 >", for: .normal)
        btn.setTitleColor(.white, for: .normal)
        btn.layer.cornerRadius = 10
        btn.layer.masksToBounds = true
        btn.layer.borderColor = UIColor.white.cgColor
        btn.layer.borderWidth = 1
        btn.setImage(UIImage.sceneImage(name: "sr_noti_right"), for: .normal)
        btn.adjustImageTitlePosition(.right)
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 10)
        btn.addTarget(self, action: #selector(showNotice), for: .touchUpInside)
        btn.layoutIfNeeded()
        return btn
    }()
    
    private lazy var contentView: UIView = { //中间的view
        let view = UIView()
        return view
    }()
    
    private lazy var nextAttrView: SRNextWarningView = {
        let attrView = SRNextWarningView()
        return attrView
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
        imgView.image = UIImage.sceneImage(name: "sr_state_bg")
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
        btn.setBackgroundImage(UIImage.sceneImage(name: "sr_again"), for: .normal)
        btn.addTargetFor(self, action: #selector(again), for: .touchUpInside)
        return btn
    }()
    
    private lazy var notiView: SRGameRuleView = {
        let ruleView = SRGameRuleView()
        return ruleView
    }()
    
    @objc public lazy var lrcView: VLSRLyricView = {
        let lrcView: VLSRLyricView = VLSRLyricView()
        return lrcView
    }()
    
    @objc public var countTime: Int = 0{
        didSet {
            print("countTime:\(countTime)")
            srBtn.setTitle("\(countTime)", for: .disabled)
        }
    }
    
    @objc public var micOwner: String = "" {
        didSet {
            
        }
    }
    
    @objc private func showNotice() {
        notiView.isHidden = false
    }
    
    @objc func resetLrcView() {
        lrcView.resetLrc()
    }
    
    @objc func hideSRBtn() {
        //显示抢唱
        srBtn.isHidden = true
        srBtn.isEnabled = true
    }
    
    @objc func setMicOwner(with owner: String, url: String) {
        contentImgView.isHidden = false
        attributeView.isHidden = false
        attributeView.setMicOwner(with: owner, url: url)
    }
    
    @objc func showNextMicOwner(with owner: String, url: String) {
        nextAttrView.isHidden = false
        nextAttrView.setMicOwner(with: owner, url: url)
    }
    
    @objc func hideMicOwner(){
        contentImgView.isHidden = true
        attributeView.isHidden = true
    }
    
    @objc func hideNextMicOwner() {
        nextAttrView.isHidden = true
    }
    
    @objc func showContentView(with text: String) {
        if contentImgView.isHidden == false {return}
        self.contentTextLabel.isHidden = false
        self.contentTextLabel.text = text
        self.contentImgView.isHidden = false
        srBtn.isHidden = true
    }
    
    @objc func hideContentView() {
        self.contentImgView.isHidden = true
    }
    
    @objc public var contentStr: String = "" {
        didSet {
            contentTextLabel.text = contentStr
        }
    }
    
    @objc private func startSr() {//开始游戏
        guard let delegate = delegate else {return}
        delegate.didSrActionChanged(.startGame)
    }
    
    @objc func sr() {
        guard let delegate = delegate else {return}
        delegate.didSrActionChanged(.sbg)
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
        addSubview(nextAttrView)
        
        addSubview(noticeBtn)
        
        addSubview(lrcView)
        
        addSubview(orderBtn)
        
       // addSubview(randomBtn)
        
        addSubview(resultTitleLabel)
        
        addSubview(tableView)
        
        addSubview(nextBtn)
        
        addSubview(contentImgView)
        contentImgView.addSubview(contentTextLabel)
        contentImgView.addSubview(fightImage)
        contentImgView.addSubview(fightResult)
        contentImgView.addSubview(attributeView)
        attributeView.isHidden = true
        
        addSubview(srBtn)

        addSubview(notiView)
        notiView.isHidden = true
    }
    
    @objc private func choose() {//点歌
        guard let delegate = delegate else {return}
        delegate.didSrActionChanged(.startGame)
    }
    
    @objc private func again() {//再来一轮
        guard let delegate = delegate else {return}
        delegate.didSrActionChanged(.again)
    }
    
    @objc private func sbg() {//抢唱
        guard let delegate = delegate else {return}
        delegate.didSrActionChanged(.sbg)
    }
    
    @objc private func nextSong() {//切歌
        guard let delegate = delegate else {return}
        delegate.didSrActionChanged(.nextSong)
    }
    
    @objc private func effect() {//调音
        guard let delegate = delegate else {return}
        delegate.didSrActionChanged(.effect)
    }
    
    @objc private func trackChange() {//原唱
        guard let delegate = delegate else {return}
        delegate.didSrActionChanged(.origin)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bgImgView.frame = self.bounds
        
        noticeBtn.frame = CGRect(x: 12, y: 10, width: 50, height: 20)
        
        //numLabel.frame = CGRect(x: 12, y: 10, width: 50, height: 20)
        
        contentImgView.frame = CGRect(x: 0, y: self.bounds.height - 64, width: self.bounds.width, height: 64)
        
        contentTextLabel.frame = CGRect(x: 0, y: 0, width: self.bounds.width, height: 64)
        
        fightImage.frame = CGRect(x: self.bounds.width / 2.0 - 45, y: 10, width: 90, height: 24)
        fightResult.frame = CGRect(x: self.bounds.width / 2.0 - 45, y: 40, width: 90, height: 24)
        attributeView.frame = CGRect(x: 0, y: 0, width: self.bounds.width, height: 64)
        
       // orderBtn.frame = CGRect(x: self.bounds.width / 2.0 - 114, y: self.bounds.height - 68, width: 84, height: 34)
        orderBtn.frame = CGRect(x: self.bounds.width / 2.0 - 42, y: self.bounds.height - 68, width: 84, height: 34)
        
       // randomBtn.frame = CGRect(x: self.bounds.width / 2.0 - 114 + 108, y: self.bounds.height - 68, width: 120, height: 34)
        
        resultTitleLabel.frame = CGRect(x: self.bounds.width / 2.0 - 50, y: 8, width: 100, height: 22)
        
        tableView.frame = CGRect(x: 0, y: resultTitleLabel.frame.maxY + 8, width: self.bounds.width, height: self.bounds.height - resultTitleLabel.frame.maxY - 8)
        
        nextBtn.frame = CGRect(x: self.bounds.width / 2.0 - 55, y: self.bounds.height - 18 - 34, width: 110, height: 34)
        
        lrcView.frame = CGRect(x: 12, y: 36, width: self.bounds.width - 24, height: self.bounds.height - 40)
        
        notiView.frame = CGRect(x: 12, y: 10, width: self.bounds.width - 24, height: 105)
        
        srBtn.frame = CGRect(x: (self.bounds.width - 88)/2.0, y: self.bounds.height - 58, width: 88, height: 38)
    }
}

extension VLSRStatusView: UITableViewDelegate, UITableViewDataSource {
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
