//
//  VRSoundTypeCard.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/26.
//

import UIKit
import ZSwiftBaseLib

public class VRSoundTypeCard: HorizontalCardView {
    
    private var content = ""
    
    lazy var imageView: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height)).contentMode(.scaleToFill)
    }()
    
    lazy var noteLabel: UILabel = {
        UILabel().numberOfLines(4).font(.systemFont(ofSize: 12, weight: .regular)).textColor(.white)
    }()
    
    lazy var titleLabel: UILabel = {
        UILabel().font(.systemFont(ofSize: 16, weight: .semibold)).textColor(.white)
    }()
    
    convenience init(frame: CGRect,title: String,note: String,background: UIImage) {
        self.init(frame: frame)
        self.backgroundColor = .clear
        self.content = note
        self.addSubViews([self.imageView,self.noteLabel,self.titleLabel])
        self.imageView.image = background
        let height = note.z.sizeWithText(font: .systemFont(ofSize: 12, weight: .regular), size: CGSize(width: self.frame.width-32, height: 9999)).height
        self.noteLabel.frame = CGRect(x: 16, y: self.frame.height - height - 18, width: self.frame.width - 32, height: height+5)
        self.titleLabel.frame = CGRect(x: 16, y: self.frame.height - height - 18  - 22 - 10, width: self.frame.width - 32, height: 22)
        self.noteLabel.text = note
        self.titleLabel.text = title
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        let height = self.content.z.sizeWithText(font: .systemFont(ofSize: 12, weight: .regular), size: CGSize(width: self.frame.width-32, height: 9999)).height
        self.noteLabel.frame = CGRect(x: 16, y: self.frame.height - height - 18, width: self.frame.width - 32, height: height+5)
        self.titleLabel.frame = CGRect(x: 16, y: self.frame.height - height - 18  - 22 - 10, width: self.frame.width - 32, height: 22)
    }

    public override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}
