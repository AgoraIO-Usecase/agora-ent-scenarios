//
//  VRSoundEffectsCell.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/26.
//

import UIKit
import ZSwiftBaseLib

public class VRSoundEffectsCell: UITableViewCell,UICollectionViewDelegate,UICollectionViewDataSource {
    
    var entity: VRRoomMenuBarEntity?
    
    private var images = [["wangyi","momo","pipi","yinyu"],["wangyi","jiamian","yinyu","paipaivoice","wanba","qingtian","skr","soul"],["yalla-ludo","jiamian"],["qingmang","cowLive","yuwan","weibo"]]
    
    lazy var background: UIView = {
        UIView(frame: CGRect(x: 20, y: 15, width: ScreenWidth-40, height: self.frame.height - 15)).backgroundColor(.white).cornerRadius(20)
    }()
    
    lazy var shaodw: UIView = {
        UIView(frame: CGRect(x: 32, y: 17, width: ScreenWidth-64, height: self.frame.height - 15)).backgroundColor(.white)
    }()
    
    lazy var effectName: UILabel = {
        UILabel(frame: CGRect(x: 20, y: 17.5, width: self.background.frame.width - 40, height: 20)).textColor(UIColor(0x156EF3)).font(.systemFont(ofSize: 16, weight: .semibold))
    }()
    
    lazy var effectDesc: UILabel = {
        UILabel(frame: CGRect(x: 20, y: self.effectName.frame.maxY+4, width: self.effectName.frame.width, height: 60)).font(.systemFont(ofSize: 13, weight: .regular)).textColor(UIColor(0x3C4267)).numberOfLines(0)
    }()
    
    lazy var line: UIView = {
        UIView(frame: CGRect(x: 20, y: self.effectDesc.frame.maxY+6, width: self.effectDesc.frame.width, height: 1)).backgroundColor(UIColor(0xF6F6F6))
    }()
    
    lazy var customUsage: UILabel = {
        UILabel(frame: CGRect(x: 20, y: self.effectDesc.frame.maxY+10, width: 200, height: 15)).font(.systemFont(ofSize: 11, weight: .regular)).textColor(UIColor(0xD8D8D8)).text(LanguageManager.localValue(key: "Current Customer Usage"))
    }()
    
    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.minimumInteritemSpacing = 10
        layout.itemSize = CGSize(width: 20, height: 20)
        return layout
    }()
    
    lazy var iconList: UICollectionView = {
        UICollectionView(frame: CGRect(x: 20, y: self.customUsage.frame.maxY+5, width: self.effectName.frame.width, height: 20), collectionViewLayout: self.flowLayout).delegate(self).dataSource(self).registerCell(VRIconCell.self, forCellReuseIdentifier: "VRIconCell").showsVerticalScrollIndicator(false).showsHorizontalScrollIndicator(false).isUserInteractionEnabled(false).backgroundColor(.white)
    }()
    
    lazy var chooseSymbol: UIImageView = {
        UIImageView(frame: CGRect(x: self.background.frame.width-32, y: self.frame.height-31, width: 32, height: 31)).image(UIImage("check")!).contentMode(.scaleAspectFit)
    }()

    public override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.contentView.backgroundColor = .clear
        self.backgroundColor = .clear
        self.contentView.addSubview(self.shaodw)
        self.contentView.addSubview(self.background)
        self.shaodw.layer.shadowRadius = 8
        self.shaodw.layer.shadowOffset = CGSize(width: 0, height: 2)
        self.shaodw.layer.shadowColor = UIColor(red: 0.04, green: 0.1, blue: 0.16, alpha: 0.12).cgColor
        self.shaodw.layer.shadowOpacity = 1
        self.background.addSubViews([self.effectName,self.effectDesc,self.line,self.customUsage,self.iconList,self.chooseSymbol])
        self.iconList.isScrollEnabled = false
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

extension VRSoundEffectsCell {
    
    static func items() -> [VRRoomMenuBarEntity] {
        var items = [VRRoomMenuBarEntity]()
        do {
            for dic in [["title":LanguageManager.localValue(key: "Social Chat"),"detail":LanguageManager.localValue(key: "This sound effect focuses on solving the voice call problem of the Social Chat scene, including noise cancellation and echo suppression of the anchor's voice. It can enable users of different network environments and models to enjoy ultra-low delay and clear and beautiful voice in multi-person chat."),"selected":true,"index":0,"soundType":"Social Chat"],["title":LanguageManager.localValue(key: "Karaoke"),"detail":LanguageManager.localValue(key: "This sound effect focuses on solving all kinds of problems in the Karaoke scene of single-person or multi-person singing, including the balance processing of accompaniment and voice, the beautification of sound melody and voice line, the volume balance and real-time synchronization of multi-person chorus, etc. It can make the scenes of Karaoke more realistic and the singers' songs more beautiful."),"selected":false,"index":1,"soundType":"Karaoke"],["title":LanguageManager.localValue(key: "Gaming Buddy"),"detail":LanguageManager.localValue(key: "This sound effect focuses on solving all kinds of problems in the game scene where the anchor plays with him, including the collaborative reverberation processing of voice and game sound, the melody of sound and the beautification of sound lines. It can make the voice of the accompanying anchor more attractive and ensure the scene feeling of the game voice. "),"selected":false,"index":2,"soundType":"Gaming Buddy"],["title":LanguageManager.localValue(key: "Professional Podcaster"),"detail":LanguageManager.localValue(key: "This sound effect focuses on solving the problems of poor sound quality of mono anchors and compatibility with mainstream external sound cards. The sound network stereo collection and high sound quality technology can greatly improve the sound quality of anchors using sound cards and enhance the attraction of live broadcasting rooms. At present, it has been adapted to mainstream sound cards in the market. "),"selected":false,"index":3,"soundType":"Professional Podcaster"]] {
                let data = try JSONSerialization.data(withJSONObject: dic, options: [])
                let item = try JSONDecoder().decode(VRRoomMenuBarEntity.self, from: data)
                items.append(item)
            }
        } catch {
            assert(false, "\(error.localizedDescription)")
        }
        return items
    }
    
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.images[self.entity?.index ?? 0].count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "VRIconCell", for: indexPath) as? VRIconCell
        cell?.imageView.image = UIImage(self.images[self.entity?.index ?? 0][indexPath.row])
        return cell ?? UICollectionViewCell()
    }
    
    func refresh(item: VRRoomMenuBarEntity) {
        self.entity = item
        self.effectName.text = item.title
        self.effectDesc.text = item.detail
        self.chooseSymbol.isHidden = !item.selected
        if item.selected {
            self.background.layerProperties(UIColor(0x009FFF), 1)
        } else {
            self.background.layerProperties(.clear, 1)
        }
        self.background.frame = CGRect(x: 20, y: 15, width: self.contentView.frame.width-40, height: self.contentView.frame.height - 15)
        self.shaodw.frame = CGRect(x: 35, y: 15, width: self.contentView.frame.width-70, height: self.frame.height - 16)
        self.effectName.frame = CGRect(x: 20, y: 15, width: self.background.frame.width-40, height: 22)
        self.effectDesc.frame = CGRect(x: 20, y: self.effectName.frame.maxY+4, width: self.effectName.frame.width, height: VRSoundEffectsList.heightMap[item.title] ?? 60)
        self.line.frame = CGRect(x: 20, y: self.effectDesc.frame.maxY+6, width: self.effectDesc.frame.width, height: 1)
        self.customUsage.frame = CGRect(x: 20, y: self.effectDesc.frame.maxY+10, width: 200, height: 15)
        self.iconList.frame = CGRect(x: 20, y: Int(self.customUsage.frame.maxY)+5, width: Int(self.background.frame.width) - 40, height: 20)
        self.chooseSymbol.frame = CGRect(x: self.background.frame.width-32, y: self.background.frame.height-31, width: 32, height: 31)
        self.iconList.reloadData()
    }
    
}

public class VRIconCell: UICollectionViewCell {
    
    lazy var imageView: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height)).contentMode(.scaleAspectFill)
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.addSubview(self.imageView)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
//    public override func layoutSubviews() {
//        super.layoutSubviews()
//        self.imageView.frame = CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height)
//    }
    
}

