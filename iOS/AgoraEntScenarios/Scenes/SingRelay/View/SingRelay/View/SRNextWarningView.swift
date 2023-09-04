//
//  SRNextWarningView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/8/2.
//

import Foundation
class SRNextWarningView: UIView {
    var imgView: UIImageView!
    var contentView: UIView!
    var preLabel: UILabel!
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
        
        imgView = UIImageView()
        imgView.image = UIImage.sceneImage(name: "micwithbackground")
        contentView.addSubview(imgView)
        
        preLabel = UILabel(frame: .zero)
        preLabel.text = "下段由 "
        preLabel.textColor = .white
        preLabel.font = UIFont.systemFont(ofSize: 12)
        contentView.addSubview(preLabel)
        
        icon = UIImageView(frame: .zero)
        contentView.addSubview(icon)
        
        endLabel = UILabel(frame: .zero)
        endLabel.text = " 演唱"
        endLabel.font = UIFont.systemFont(ofSize: 12)
        endLabel.textColor = .white
        contentView.addSubview(endLabel)
    }
    
    public func setMicOwner(with owner: String, url: String) {
        let imageUrl = URL(string: url)
        guard let imageData = try? Data(contentsOf: imageUrl!), let image = UIImage(data: imageData) else {
            return
        }
        icon.image = image
        endLabel.text = " \(owner) 演唱"
        imgView.frame = CGRect(x: 2, y: 5, width: 32, height: 16)
        let endWidth = textAutoWidth(text: endLabel.text ?? "", height: 20, fontSize: 12)
        let preWidth = textAutoWidth(text: preLabel.text ?? "", height: 20, fontSize: 12)
        let totalWidth = endWidth + preWidth + 40 + 16
        contentView.frame = CGRect(x: self.bounds.width / 2.0 - totalWidth / 2.0, y: self.bounds.height / 2.0 - 12.5, width: totalWidth, height: 20)
        preLabel.frame = CGRect(x: 40, y: 6, width: preWidth, height: 12)
        icon.frame = CGRect(x: preWidth + 40, y: 5, width: 16, height: 16)
        endLabel.frame = CGRect(x: preWidth + 40 + 16, y: 6, width: endWidth, height: 12)
        self.layer.cornerRadius = 10
        self.layer.masksToBounds = true
        self.layer.borderColor = UIColor.white.cgColor
        self.layer.borderWidth = 1
        self.backgroundColor = .systemBlue
    }
    
    func textAutoWidth(text: String, height:CGFloat, fontSize:CGFloat) ->CGFloat{
        let origin = NSStringDrawingOptions.usesLineFragmentOrigin
        let lead = NSStringDrawingOptions.usesFontLeading
        let rect = text.boundingRect(with:CGSize(width:0, height: height), options: [origin,lead], attributes: [NSAttributedString.Key.font:UIFont.systemFont(ofSize: fontSize)], context:nil)
        return rect.width
    }
}
