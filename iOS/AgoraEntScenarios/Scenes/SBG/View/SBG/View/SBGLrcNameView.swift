//
//  SBGLrcNameView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/6/5.
//

import Foundation

class SBGLrcNameView: UIView {
    var contentView: UIView!
    var icon: UIImageView!
    var endLabel: UILabel!
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        contentView = UIView(frame: .zero)
        addSubview(contentView)

        icon = UIImageView(frame: .zero)
        contentView.addSubview(icon)
        
        endLabel = UILabel(frame: .zero)
        endLabel.textColor = .white
        endLabel.font = UIFont.systemFont(ofSize: 12)
        contentView.addSubview(endLabel)
    }
    
    public func setName(with name: String, isCenter: Bool) {
        icon.image = UIImage.sceneImage(name: "ktv_bigMusic_icon")
        endLabel.text = name
        let endWidth = textAutoWidth(text: endLabel.text ?? "", height: 20, fontSize: 12)
        let totalWidth = endWidth + 28
        contentView.frame = CGRect(x: isCenter ? self.bounds.width / 2.0 - totalWidth / 2.0 : 0, y: self.bounds.height / 2.0 - 14, width: totalWidth, height: 28)
        icon.frame = CGRect(x: 0, y: 1, width: 28, height: 28)
        endLabel.frame = CGRect(x: 33, y: 4, width: endWidth, height: 20)
    }
    
    func textAutoWidth(text: String, height:CGFloat, fontSize:CGFloat) ->CGFloat{
        let origin = NSStringDrawingOptions.usesLineFragmentOrigin
        let lead = NSStringDrawingOptions.usesFontLeading
        let rect = text.boundingRect(with:CGSize(width:0, height: height), options: [origin,lead], attributes: [NSAttributedString.Key.font:UIFont.systemFont(ofSize: fontSize)], context:nil)
        return rect.width
    }
}
