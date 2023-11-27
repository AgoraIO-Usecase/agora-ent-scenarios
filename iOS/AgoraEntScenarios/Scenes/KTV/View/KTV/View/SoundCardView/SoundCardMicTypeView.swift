//
//  SoundCardEffectView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/12.
//

import Foundation

class SoundCardMicTypeView: UIView {
    var headIconView: UIView!
    var headTitleLabel: UILabel!
    var tableView: UITableView!
    var botView: UIView!
    var cancleView: UIButton!
    var sepView: UIView!
    @objc var micType: Int = 0
    @objc var clickBlock: ((Int)->Void)?
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        self.backgroundColor = .white
        
        headIconView = UIView()
        headIconView.backgroundColor = UIColor(red: 212/255.0, green: 207/255.0, blue: 229/255.0, alpha: 1)
        headIconView.layer.cornerRadius = 2
        headIconView.layer.masksToBounds = true
        self.addSubview(headIconView)
        
        headTitleLabel = UILabel()
        headTitleLabel.text = Bundle.localizedString("ktv_mic_type", bundleName: "KtvResource")
        headTitleLabel.textAlignment = .center
        headTitleLabel.font = UIFont.systemFont(ofSize: 16, weight: .bold)
        self.addSubview(headTitleLabel)
        
        tableView = UITableView()
        tableView.dataSource = self
        tableView.delegate = self
        tableView.registerCell(SoundCardMicTypeCell.self, forCellReuseIdentifier: "cell")
        tableView.tableFooterView = UIView()
        self.addSubview(tableView)
        
        sepView = UIView()
        sepView.backgroundColor = UIColor(red: 245/255.0, green: 244/255.0, blue: 246/255.0, alpha: 1)
        self.addSubview(sepView)
        
        cancleView = UIButton()
        cancleView.titleLabel?.font = UIFont.systemFont(ofSize: 15)
        cancleView.setTitleColor(.black, for: .normal)
        cancleView.setTitle("取消", for: .normal)
        cancleView.backgroundColor = .white
        cancleView.addTarget(self, action: #selector(cancel), for: .touchUpInside)
        self.addSubview(cancleView)
        
        KTVHeadSetUtil.addSoundCardObserver {[weak self] flag in
            guard let block = self?.clickBlock else {return}
            block(-2)
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        headIconView.frame = CGRect(x: (self.bounds.width - 38)/2.0, y: 8, width: 38, height: 4)
        headTitleLabel.frame = CGRect(x: (self.bounds.width - 100)/2.0, y: 30, width: 100, height: 22)
        tableView.frame = CGRect(x: 0, y: headTitleLabel.frame.maxY + 10, width: self.bounds.width, height: 420)
        sepView.frame = CGRect(x: 0, y: self.bounds.height - 70, width: self.bounds.width, height: 10)
        cancleView.frame = CGRect(x: 0, y: self.bounds.height - 60, width: self.bounds.width, height: 50)
    }
    
    @objc private func cancel() {
        guard let block = clickBlock else {return}
        block(-1)
    }
}

extension SoundCardMicTypeView: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 5
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 48
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! SoundCardMicTypeCell
        let text = indexPath.row > 0 ? "\(indexPath.row)" : ""
        cell.titleLabel.text =  Bundle.localizedString("ktv_mic_type", bundleName: "KtvResource") + "\(text)"
        cell.setIsSelected(self.micType == indexPath.row)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if self.micType == indexPath.row {
            return
        }
        self.micType = indexPath.row
        tableView.reloadData()
        guard let block = clickBlock else {return}
        block(indexPath.row)
    }
}
