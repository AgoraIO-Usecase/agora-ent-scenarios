//
//  ShowRoomChatCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/8.
//

import UIKit

class ShowRoomChatCell: UITableViewCell {
    
    private lazy var bgView: UIView = {
        let bgView = UIView()
        bgView.backgroundColor = .show_room_info_cover
        bgView.layer.cornerRadius = 13
        bgView.layer.masksToBounds = true
        return bgView
    }()
    
    private lazy var msgLabel: UILabel = {
        let label = UILabel()
        label.lineBreakMode = .byWordWrapping
        label.numberOfLines = 0
        return label
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        contentView.backgroundColor = .clear
        backgroundColor = .clear
        transform = CGAffineTransform(rotationAngle: Double.pi)
        contentView.addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.top.equalTo(5)
            make.bottom.equalTo(-5)
            make.left.equalToSuperview()
            make.right.lessThanOrEqualTo(0)
        }
        
        bgView.addSubview(msgLabel)
        msgLabel.snp.makeConstraints { make in
            make.left.equalTo(10)
            make.top.equalTo(8)
            make.right.equalTo(-10)
            make.bottom.equalTo(-10)
        }
        self.selectedBackgroundView = UIView()
    }
    
    func setUserName(_ userName: String, msg: String) {
        
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineHeightMultiple = 1.2
        let nameAttributes = [NSAttributedString.Key.paragraphStyle: paragraphStyle,
                              NSAttributedString.Key.font: UIFont.show_chat_user_name,
                              NSAttributedString.Key.foregroundColor: UIColor.show_chat_user_name]
        let msgAttributes = [NSAttributedString.Key.font: UIFont.show_chat_msg, NSAttributedString.Key.foregroundColor: UIColor.show_main_text]
        let attributedText = NSMutableAttributedString(string: userName, attributes: nameAttributes as [NSAttributedString.Key : Any])
        let attributedMsg = NSAttributedString(string: ": " + msg, attributes: msgAttributes as [NSAttributedString.Key : Any])
        attributedText.append(attributedMsg)
        msgLabel.attributedText = attributedText
    }
}
