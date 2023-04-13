//
//  SceneVersionCell.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2023/2/8.
//

import UIKit
import ZSwiftBaseLib

final class SceneVersionCell: UITableViewCell {
    
    lazy var title: UILabel = {
        UILabel(frame: CGRect(x: 20, y: 20, width: (ScreenWidth-40)/2.0, height: 20)).font(.systemFont(ofSize: 15, weight: .medium)).textColor(UIColor(0x3C4267))
    }()
    
    lazy var detail: UILabel = {
        UILabel(frame: CGRect(x: self.title.frame.maxX, y: self.title.frame.minX, width: (ScreenWidth-40)/2.0, height: 20)).font(.systemFont(ofSize: 13, weight: .regular)).textColor(UIColor(0x979CBB)).textAlignment(.right)
    }()
    
    lazy var separaLine: UIView = {
        UIView(frame: CGRect(x: 20, y: self.title.frame.maxY+18, width: ScreenWidth-40, height: 1)).backgroundColor(UIColor(0xF8F5FA))
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.contentView.addSubViews([self.title,self.detail,self.separaLine])
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        // Configure the view for the selected state
    }
    
    func refreshInfo(info: Dictionary<String,String>) {
        self.title.text = info["title"]
        self.detail.text = info["detail"]
    }

}
