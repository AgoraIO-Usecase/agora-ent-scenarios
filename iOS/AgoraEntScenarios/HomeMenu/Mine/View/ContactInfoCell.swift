//
//  ContactInfoCell.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2023/2/8.
//

import UIKit
import ZSwiftBaseLib

final class ContactInfoCell: UITableViewCell {
    
    lazy var title: UILabel = {
        UILabel(frame: CGRect(x: 20, y: 10, width: ScreenWidth-64, height: 20)).font(.systemFont(ofSize: 15, weight: .medium)).textColor(.black)
    }()
    
    lazy var detail: UITextView = {
        let textView =  UITextView(frame: CGRect(x: 20, y: self.title.frame.maxY+5, width: ScreenWidth-64, height: 25)).isEditable(false)
        textView.isScrollEnabled = false
        textView.showsVerticalScrollIndicator = false
        return textView
    }()
    
    lazy var separaLine: UIView = {
        UIView(frame: CGRect(x: 20, y: self.detail.frame.maxY+7, width: self.title.frame.width, height: 1)).backgroundColor(UIColor(0xF8F5FA))
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.contentView.addSubViews([self.title,self.detail,self.separaLine])
        self.detail.linkTextAttributes = [.foregroundColor:UIColor(0x009FFF)]
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
        guard let link = info["detail"] else { return }
        var url: URL?
        if let first = link.first {
            if first.isNumber {
                url = URL(string: "tel:\(link)")!
            }else if link.hasPrefix("http://") || link.hasPrefix("https://"){
                url = URL(string: link)!
            }
        }
        if let url = url {
            self.detail.attributedText = NSAttributedString {
                Link(link, url: url).font(.systemFont(ofSize: 13, weight: .medium)).foregroundColor(Color(0x009FFF))
            }
        }else {
            self.detail.attributedText = NSAttributedString{
                AttributedText(link).font(.systemFont(ofSize: 13, weight: .medium)).foregroundColor(Color(0x009FFF))
            }
        }
    }

}
