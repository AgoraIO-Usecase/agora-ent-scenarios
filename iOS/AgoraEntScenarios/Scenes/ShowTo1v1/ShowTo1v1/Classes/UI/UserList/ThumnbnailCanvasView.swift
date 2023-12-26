//
//  ThumnbnailCanvasView.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/8/9.
//

import UIKit

//房间列表主播展示画布
class ThumnbnailCanvasView: UIView {
    lazy var canvasView = UIView()
    private lazy var leaveIconView = UIImageView(image: UIImage.sceneImage(name: "icon_user_leave")!)
    private lazy var leaveTipsLabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 13)
        label.numberOfLines = 0
        label.lineBreakMode = .byWordWrapping
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.alignment = .center
        paragraphStyle.lineHeightMultiple = 1.3
        label.attributedText = NSMutableAttributedString(string: "user_list_user_leave".showTo1v1Localization(),
                                                        attributes: [NSAttributedString.Key.paragraphStyle: paragraphStyle])

        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubViews() {
        addSubview(leaveIconView)
        addSubview(leaveTipsLabel)
        addSubview(canvasView)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        leaveIconView.aui_centerX = aui_width / 2
        leaveIconView.aui_bottom = aui_height / 2
        leaveTipsLabel.aui_width = aui_width - 20
        leaveTipsLabel.sizeToFit()
        leaveTipsLabel.aui_centerX = leaveIconView.aui_centerX
        leaveTipsLabel.aui_top = leaveIconView.aui_bottom + 10
        canvasView.frame = bounds
    }
}
