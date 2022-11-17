//
//  ShowSettingSegmentCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/14.
//

import UIKit

fileprivate class ShowSettingSegmentView: UIView {
    
    var didSelectedIndex: ((_ index: Int)->())?
    
    private var buttonArray = [UIButton]()
  
    var titles: [String]?{
        didSet {
            guard let titles = titles else { return }
            let buttonWidth: CGFloat = 30
            let spacing: CGFloat = 6
            for i in 0 ..< titles.count {
                var button: UIButton
                if buttonArray.count > i {
                    button = buttonArray[i]
                }else{
                    button = UIButton(type: .custom)
                    buttonArray.append(button)
                    addSubview(button)
                }
                button.isHidden = false
                button.titleLabel?.font = .show_R_11
                button.setTitle(titles[i], for: .normal)
                button.setTitleColor(.show_segment_title_nor, for: .normal)
                button.setTitleColor(.show_zi03, for: .selected)
                button.setImage(UIImage(color: .show_segment_bg), for: .normal)
                button.setImage(UIImage(color: .show_main_text), for: .selected)
                button.layer.borderWidth = 1
                button.layer.cornerRadius = 4
                button.layer.borderColor = UIColor.show_segment_border.cgColor
                button.layer.masksToBounds = true
                button.addTarget(self, action: #selector(didClickButton(_:)), for: .touchUpInside)
                button.snp.remakeConstraints { make in
                    make.right.equalTo(-(buttonWidth + spacing) * CGFloat(titles.count - 1 - i))
                    make.top.bottom.equalToSuperview()
                    make.width.equalTo(buttonWidth)
                    make.height.equalTo(28)
                    if i == 0 {
                        make.left.equalToSuperview()
                    }
                }
            }
            
            // 多余的button隐藏
            if buttonArray.count > titles.count {
                for i in titles.count ..< buttonArray.count {
                    let button = buttonArray[i]
                    button.isHidden = true
                }
            }
        }
    }
    
    var defaultSelectedIndex = 0 {
        didSet {
            let button = buttonArray[defaultSelectedIndex]
            didClickButton(button)
        }
    }
  
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
        
    }
    
    @objc private func didClickButton(_ button: UIButton) {
        for btn in buttonArray {
            btn.isSelected = btn.isEqual(button)
            btn.layer.borderColor = btn.isSelected ? UIColor.show_zi03.cgColor : UIColor.show_segment_border.cgColor
        }
        if let index = buttonArray.firstIndex(of: button) {
            didSelectedIndex?(index)
        }
    }
}

class ShowSettingSegmentCell: ShowSettingBaseCell {

    private let segmentView: ShowSettingSegmentView = ShowSettingSegmentView()
    override func createSubviews(){
        super.createSubviews()
        contentView.addSubview(segmentView)
        segmentView.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.centerY.equalToSuperview()
        }
    }

    func setTitle(_ title: String, items: [String], defaultSelectIndex: Int, didSelectedIndex: ((_ index: Int)->())?) {
        titleLabel.text = title
        segmentView.titles = items
        segmentView.defaultSelectedIndex = defaultSelectIndex
        segmentView.didSelectedIndex = didSelectedIndex
    }
}
