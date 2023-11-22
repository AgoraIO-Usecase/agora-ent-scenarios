//
//  SoundCardSettingView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/12.
//

import Foundation

@objc class SoundCardSettingView: UIView {
    var headIconView: UIView!
    var headTitleLabel: UILabel!
    var noSoundCardView: UIView!
    var warNingLabel: UILabel!
    var tipsView: UIView!
    var tableView: UITableView!
    var headLabel: UILabel!
    var exLabel: UILabel!
    var coverView: UIView!
    @objc var soundOpen:Bool = false
    @objc var gainValue: Float = 0
    @objc var effectType: Int = 0
    @objc var typeValue: Int = 2
    
    @objc var clicKBlock:((Int) -> Void)?
    @objc var gainBlock:((Float) -> Void)?
    @objc var typeBlock:((Int) -> Void)?
    @objc var soundCardBlock:((Bool) -> Void)?
    @objc func setUseSoundCard(enable: Bool) {
        self.noSoundCardView.isHidden = enable
        self.tableView.isHidden = !enable
        self.tableView.reloadData()
    }
    
    @objc override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.backgroundColor = .white
        
        headIconView = UIView()
        headIconView.backgroundColor = UIColor(red: 212/255.0, green: 207/255.0, blue: 229/255.0, alpha: 1)
        headIconView.layer.cornerRadius = 2
        headIconView.layer.masksToBounds = true
        self.addSubview(headIconView)
        
        headTitleLabel = UILabel()
        headTitleLabel.text = "虚拟声卡"
        headTitleLabel.textAlignment = .center
        headTitleLabel.font = UIFont.systemFont(ofSize: 16, weight: .bold)
        self.addSubview(headTitleLabel)
        
        tableView = UITableView()
        tableView.dataSource = self
        tableView.delegate = self
        tableView.separatorColor = UIColor(hexString: "#F2F2F6")
        tableView.registerCell(UITableViewCell.self, forCellReuseIdentifier: "cell")
        tableView.registerCell(SoundCardMicCell.self, forCellReuseIdentifier: "mic")
        tableView.registerCell(SoundCardSwitchCell.self, forCellReuseIdentifier: "switch")
        self.addSubview(tableView)
        
        coverView = UIView()
        coverView.backgroundColor = .white
        coverView.alpha = 0.7
        self.addSubview(coverView)
        
        noSoundCardView = UIView()
        self.addSubview(noSoundCardView)
        
        // 创建图片 attachment
        let imageAttachment = NSTextAttachment()
        imageAttachment.image = UIImage.sceneImage(name: "candel")

        // 设置图片的大小和位置
        let imageSize = CGSize(width: 20, height: 20)
        imageAttachment.bounds = CGRect(origin: .zero, size: imageSize)

        // 创建带有图片的富文本
        let attributedString = NSMutableAttributedString()
        let imageAttString = NSAttributedString(attachment: imageAttachment)
        attributedString.append(imageAttString)

        // 添加文字部分
        let text = " 当前无法使用虚拟声卡，请连接优先输入设备！"
        let textAttributes: [NSAttributedString.Key: Any] = [
            .font: UIFont.systemFont(ofSize: 12),
            .foregroundColor: UIColor.red,
            .baselineOffset: (imageSize.height - UIFont.systemFont(ofSize: 12).capHeight) / 2  // 调整图片位置以实现垂直居中
        ]
        let textAttString = NSAttributedString(string: text, attributes: textAttributes)
        attributedString.append(textAttString)

        warNingLabel = UILabel()
        warNingLabel.attributedText = attributedString
        warNingLabel.textColor = .red
        warNingLabel.font = UIFont.systemFont(ofSize: 12)
        noSoundCardView.addSubview(warNingLabel)
        
        tipsView = UIView()
        tipsView.backgroundColor = UIColor(red: 1, green: 251/255.0, blue: 252/255.0, alpha: 1)
        noSoundCardView.addSubview(tipsView)
        tipsView.layer.cornerRadius = 5
        tipsView.layer.masksToBounds = true
        
        headLabel = UILabel()
        headLabel.text = "目前支持以下设备:"
        headLabel.font = UIFont.systemFont(ofSize: 13, weight: .bold)
        tipsView.addSubview(headLabel)
        
        exLabel = UILabel()
        exLabel.text = "1.有线耳机 \n2.有线麦克风"
        exLabel.numberOfLines = 0
        exLabel.font = UIFont.systemFont(ofSize: 12)
        exLabel.textColor = UIColor(red: 60/255.0, green: 66/255.0, blue: 103/255.0, alpha: 1)
        tableView.tableFooterView = UIView()
        tipsView.addSubview(exLabel)
        
        noSoundCardView.isHidden = true
        
        let flag = KTVHeadSetUtil.hasSoundCard()
        self.noSoundCardView.isHidden = true
        self.tableView.isHidden = !true

        KTVHeadSetUtil.addSoundCardObserver {[weak self] flag in
            self?.noSoundCardView.isHidden = flag
            self?.tableView.isHidden = !flag
            if flag == false {
                self?.soundOpen = false
                guard let soundCardBlock = self?.soundCardBlock else {return}
                soundCardBlock(false)
            }
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        headIconView.frame = CGRect(x: (self.bounds.width - 38)/2.0, y: 8, width: 38, height: 4)
        headTitleLabel.frame = CGRect(x: (self.bounds.width - 80)/2.0, y: 30, width: 80, height: 22)
        tableView.frame = CGRect(x: 0, y: headTitleLabel.frame.maxY + 10, width: self.bounds.width, height: self.bounds.height - headTitleLabel.frame.maxY - 10)
        
        coverView.frame = CGRect(x: 0, y: headTitleLabel.frame.maxY + 10 + 104, width: self.bounds.width, height: 156)
        
        noSoundCardView.frame = CGRect(x: 0, y: headTitleLabel.frame.maxY + 10, width: self.bounds.width, height: 200)
        warNingLabel.frame = CGRect(x: 20, y: 10, width: self.bounds.width, height: 20)
        tipsView.frame = CGRect(x: 20, y: 40, width: self.bounds.width - 40, height: 100)
        
        headLabel.frame = CGRect(x: 10, y: 10, width: 200, height: 20)
        exLabel.frame = CGRect(x: 20, y: headLabel.frame.maxY + 3, width: 80, height: 40)
        
    }
    
    private func getEffectDesc(with type: Int) -> String {
        switch type {
            case 0:
                return "大叔音(高混响 | KTV)"
            case 1:
                return "妈音(高混响 | KTV)"
            case 2:
                return "青叔音(明亮 | 磁性)"
            case 3:
                return "御妈音(明亮 |磁性)"
            case 4:
                return "青年音(低沉 | 温暖)"
            case 5:
                return "少御音(醇厚 | 饱满)"
            default:
                break
        }
        return ""
    }
    
    @objc func soundChange(swich: UISwitch) {
        if swich.isOn {
            self.gainValue = 1.0;
            self.effectType = 0;
            self.typeValue = 4;
        }
        self.soundOpen = swich.isOn
        coverView.isHidden = swich.isOn
        guard let soundCardBlock = soundCardBlock else {return}
        soundCardBlock(swich.isOn)
    }
    
}

extension SoundCardSettingView: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 5
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 52
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        if indexPath.row == 0 || indexPath.row == 2 {
            let cell: UITableViewCell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath)
            let rightLabel = UILabel()
            rightLabel.font = UIFont.systemFont(ofSize: 12)
            rightLabel.textColor = .gray
            rightLabel.translatesAutoresizingMaskIntoConstraints = false
            cell.contentView.addSubview(rightLabel)

            rightLabel.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -20).isActive = true
            rightLabel.centerYAnchor.constraint(equalTo: cell.contentView.centerYAnchor).isActive = true
            
            cell.textLabel?.font = UIFont.systemFont(ofSize: 13)
            if indexPath.row == 2 {
                cell.accessoryType = .disclosureIndicator
                cell.textLabel?.text = "预设音效"
                rightLabel.text = getEffectDesc(with: self.effectType)
            } else {
                cell.textLabel?.text = "麦克风类型"
                rightLabel.text = "default 外置麦克风"
            }
            cell.selectionStyle = .none
            return cell
        } else if indexPath.row == 1 {
            let cell: UITableViewCell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath)
            cell.textLabel?.text = "开启虚拟声卡"
            cell.textLabel?.font = UIFont.systemFont(ofSize: 13)
            let swich = UISwitch()
            swich.translatesAutoresizingMaskIntoConstraints = false
            swich.isOn = self.soundOpen
            swich.addTarget(self, action: #selector(soundChange), for: .valueChanged)
            cell.contentView.addSubview(swich)

            swich.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -20).isActive = true
            swich.centerYAnchor.constraint(equalTo: cell.contentView.centerYAnchor).isActive = true
            cell.selectionStyle = .none
            return cell
        } else if indexPath.row == 3 {
            let cell: SoundCardSwitchCell = tableView.dequeueReusableCell(withIdentifier: "switch", for: indexPath) as! SoundCardSwitchCell
            cell.selectionStyle = .none
            cell.slider.value = Float(1/4.0 * gainValue)
            cell.numLable.text = String(format: "%.1f",gainValue)
            cell.valueBlock = {[weak self] gain in
                guard let self = self, let gainBlock = self.gainBlock else {return}
                self.gainValue = Float(gain)
                gainBlock(self.gainValue)
            }
            return cell
        } else {
            let cell: SoundCardMicCell = tableView.dequeueReusableCell(withIdentifier: "mic", for: indexPath) as! SoundCardMicCell
            cell.setupValue(self.typeValue)
            cell.valueBlock = {[weak self] type in
                guard let self = self, let typeBlock = self.typeBlock else {return}
                self.typeValue = type
                typeBlock(self.typeValue)
            }
            coverView.isHidden = self.soundOpen
            return cell
        }
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        guard let block = clicKBlock else {return}
        if indexPath.row == 2 {
            //弹出音效选择
            block(2)
        } else if indexPath.row == 4 {
//            //弹出麦克风类型
//            block(4)
        }
    }
}
