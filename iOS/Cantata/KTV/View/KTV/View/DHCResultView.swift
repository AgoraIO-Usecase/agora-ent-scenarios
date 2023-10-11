//
//  DHCResultView.swift
//  Cantata
//
//  Created by CP on 2023/9/4.
//

import UIKit



class DHCResultView: UIView {
    
    let countdownTimer = CountdownTimer()
    
    private lazy var resultTitleLabel: UILabel = { //本轮评分
        let label = UILabel()
        label.text = "本轮总分"
        label.textAlignment = .center
        label.textColor = .white
        return label
     }()
    
    private lazy var totalScoreLabel: UILabel = { //本轮评分
        let label = UILabel()
        label.text = "10000"
        label.textAlignment = .center
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 35)
        return label
     }()
    
    private lazy var tableView: UITableView = { //分数的tableView
            let tableView = UITableView()
            tableView.registerCell(DHCScoreTitleCell.self, forCellReuseIdentifier: "title")
            tableView.registerCell(DHCScoreCell.self, forCellReuseIdentifier: "score")
            tableView.dataSource = self
            tableView.delegate = self
            tableView.backgroundColor = .clear
            return tableView
    }()
    
    private lazy var nextLabel: UILabel = { //下一首歌提示
        let label = UILabel()
        label.text = "下一首歌:七里香"
        label.textAlignment = .center
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 15)
        return label
     }()
    
    private lazy var nextBtn: UIButton = { //下一首歌提示
        let btn = UIButton()
        btn.setTitle("下一首 10S", for: .normal)
        if let image = UIImage.sceneImage(name: "next", bundleName: "DHCResource") {
            let resizedImage = image.resizableImage(withCapInsets: UIEdgeInsets.zero, resizingMode: .stretch)
            btn.setBackgroundImage(resizedImage, for: .normal)
            btn.contentMode = .scaleAspectFill
        }

        btn.addTarget(self, action: #selector(nextSong), for: .touchUpInside)
        return btn
     }()
    
    var nextBlock: (()->Void)?
    
    @objc public var dataSource: [SubRankModel]? {
        didSet {
            tableView.reloadData()
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

        self.addSubview(resultTitleLabel)
        self.addSubview(totalScoreLabel)
        self.addSubview(tableView)
        addSubview(nextBtn)
        addSubview(nextLabel)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        resultTitleLabel.frame = CGRect(x: (ScreenWidth - 100)/2.0, y: 20, width: 100, height: 20)
        totalScoreLabel.frame = CGRect(x: (ScreenWidth - 300)/2.0, y: 60, width: 300, height: 40)
        tableView.frame = CGRect(x: 0, y: 120, width: ScreenWidth, height: 300)
        nextLabel.frame = CGRect(x:(ScreenWidth - 300)/2.0, y: 500, width: 300, height: 20)
        nextBtn.frame = CGRect(x: (ScreenWidth - 122)/2.0, y: 450, width: 122, height: 38)
    }
    
    public func setResultData(with totalScore: Int, models:[SubRankModel], musicStr: String, isRoomOwner: Bool) {
        
        totalScoreLabel.text = "\(totalScore)"
        nextLabel.isHidden = musicStr.count == 0
        if musicStr.count > 0 {
            nextLabel.text = "\(musicStr)"
        }
        nextBtn.isHidden = !isRoomOwner
        
        if isRoomOwner {
            // 开始倒计时
            var count = 10
            countdownTimer.start(totalTime: 9, update: { timeRemaining in
                // 更新UI，显示剩余时间
                count -= 1
                print("Time remaining: \(timeRemaining) seconds")
                DispatchQueue.main.async {
                    self.nextBtn.setTitle("下一首 \(count)S", for: .normal)
                }
            }) {
                // 倒计时结束处理
                print("Countdown finished!")
                self.nextSong()
            }
        }
        
        self.dataSource = models
        tableView.reloadData()
    }
    
    @objc func nextSong() {
        guard let block = nextBlock else {return}
        countdownTimer.stop()
        block()
    }
}

extension DHCResultView: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return (dataSource?.count ?? 0) + 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.row == 0 {
            let cell : DHCScoreTitleCell = tableView.dequeueReusableCell(withIdentifier: "title") as! DHCScoreTitleCell
            return cell
        } else {
            let cell : DHCScoreCell = tableView.dequeueReusableCell(withIdentifier: "score") as! DHCScoreCell
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

class CountdownTimer {
    private var timer: Timer?
    private var totalTime: TimeInterval = 0
    private var timeRemaining: TimeInterval = 0
    private var completion: (() -> Void)?
    private var update: ((_ timeRemaining: TimeInterval) -> Void)?
    
    func start(totalTime: TimeInterval, update: ((_ timeRemaining: TimeInterval) -> Void)? = nil, completion: (() -> Void)? = nil) {
        self.totalTime = totalTime
        self.timeRemaining = totalTime
        self.update = update
        self.completion = completion
        
        timer?.invalidate()
        timer = Timer.scheduledTimer(timeInterval: 1, target: self, selector: #selector(updateTimer), userInfo: nil, repeats: true)
        
        RunLoop.current.add(timer!, forMode: .common)
    }
    
    func stop() {
        timer?.invalidate()
        timer = nil
    }
    
    @objc private func updateTimer() {
        timeRemaining -= 1
        update?(timeRemaining)
        
        if timeRemaining <= 0 {
            stop()
            completion?()
        }
    }
}
