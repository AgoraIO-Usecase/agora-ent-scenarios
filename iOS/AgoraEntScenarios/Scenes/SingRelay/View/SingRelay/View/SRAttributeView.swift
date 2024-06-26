//
//  SBGAttributeView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/5/30.
//

import UIKit

class SRAttributeView: UIView {
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
        
        preLabel = UILabel(frame: .zero)
        preLabel.text = getLocalizeString(with: "sr_next_tbd")
        preLabel.textColor = .white
        contentView.addSubview(preLabel)
        
        icon = UIImageView(frame: .zero)
        icon.layer.cornerRadius = 12
        icon.layer.masksToBounds = true
        icon.contentMode = .scaleAspectFill
        contentView.addSubview(icon)
        
        endLabel = UILabel(frame: .zero)
        endLabel.text = getLocalizeString(with: "sr_next_sing")
        endLabel.textColor = .white
        contentView.addSubview(endLabel)
    }
    
    public func setMicOwner(with owner: String, url: String) {
        let imageUrl = URL(string: url)
        guard let imageData = try? Data(contentsOf: imageUrl!), let image = UIImage(data: imageData) else {
            return
        }
        icon.image = image
        endLabel.text = " \(owner)\(getLocalizeString(with: "sr_next_sing"))"
        let endWidth = textAutoWidth(text: endLabel.text ?? "", height: 25, fontSize: 18)
        let preWidth = textAutoWidth(text: preLabel.text ?? "", height: 25, fontSize: 18)
        let totalWidth = endWidth + preWidth + 25
        contentView.frame = CGRect(x: self.bounds.width / 2.0 - totalWidth / 2.0, y: self.bounds.height / 2.0 - 12.5, width: totalWidth, height: 25)
        preLabel.frame = CGRect(x: 0, y: 0, width: preWidth, height: 25)
        icon.frame = CGRect(x: preWidth, y: 0, width: 25, height: 25)
        endLabel.frame = CGRect(x: preWidth + 25, y: 0, width: endWidth, height: 25)
    }
    
    func textAutoWidth(text: String, height:CGFloat, fontSize:CGFloat) ->CGFloat{
        let origin = NSStringDrawingOptions.usesLineFragmentOrigin
        let lead = NSStringDrawingOptions.usesFontLeading
        let rect = text.boundingRect(with:CGSize(width:0, height: height), options: [origin,lead], attributes: [NSAttributedString.Key.font:UIFont.systemFont(ofSize: fontSize)], context:nil)
        return rect.width
    }
    
    private func getLocalizeString(with key: String) -> String {
        return Bundle.localizedString(key, bundleName: "SRResource")
    }
}


