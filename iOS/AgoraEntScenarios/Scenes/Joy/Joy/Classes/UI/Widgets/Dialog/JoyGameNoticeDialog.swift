//
//  JoyGameNoticeDialog.swift
//  Joy
//
//  Created by wushengtao on 2023/12/1.
//

import Foundation

class JoyGameNoticeDialog: JoyBaseDialog {
    var text: String = "" {
        didSet {
            noticeLabel.text = text
            let size = noticeLabel.sizeThatFits(CGSize(width: scrollView.width - 40, height: 100000))
            noticeLabel.frame = CGRect(origin: CGPoint(x: 20, y: 0), size: size)
            scrollView.contentSize = noticeLabel.size
        }
    }
    private lazy var scrollView: UIScrollView = {
        let scrollView = UIScrollView()
        
        return scrollView
    }()
    
    private lazy var noticeLabel: UILabel = {
        let label = UILabel()
        label.numberOfLines = 0
        label.font = .joy_R_13
        label.textColor = .joy_title_text
        return label
    }()
    
    override func contentSize() -> CGSize {
        return CGSize(width: self.width, height: 468)
    }
    
    override func loadCustomContentView(contentView: UIView) {
        contentView.addSubview(scrollView)
        scrollView.snp.makeConstraints { make in
            make.edges.equalTo(contentView)
        }
        
        scrollView.addSubview(noticeLabel)
    }
    
    override func labelTitle() -> String {
        return "dialog_title_gamelist".joyLocalization()
    }
    
    override func buttonTitle() -> String {
        return "gamenotice_selected_confirm".joyLocalization()
    }
    
    override func onClickButton() {
        self.hiddenAnimation()
    }
}
