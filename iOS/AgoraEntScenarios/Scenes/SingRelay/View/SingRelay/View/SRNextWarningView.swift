//
//  SRNextWarningView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/8/2.
//

import Foundation
class SRNextWarningView: UIView {
    var imgView = UIImageView()
    var contentView = UIView(frame: .zero)
    var preLabel = UILabel(frame: .zero)
    var icon = UIImageView(frame: .zero)
    var endLabel = UILabel(frame: .zero)
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        createViews()
        createConstrains()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createViews() {
        self.layer.cornerRadius = 10
        self.layer.masksToBounds = true
        self.layer.borderColor = UIColor.white.cgColor
        self.layer.borderWidth = 1
        self.backgroundColor = .systemBlue
                
        imgView.image = UIImage.sceneImage(name: "micwithbackground")
        self.addSubview(imgView)
        
        preLabel.text = getLocalizeString(with: "sr_next_tbd")
        preLabel.textColor = .white
        preLabel.font = UIFont.systemFont(ofSize: 12)
        self.addSubview(preLabel)
        
        icon.contentMode = .scaleAspectFill
        icon.layer.cornerRadius = 8
        icon.layer.masksToBounds = true
        self.addSubview(icon)
        
        endLabel.text = getLocalizeString(with: "sr_next_sing")
        endLabel.font = UIFont.systemFont(ofSize: 12)
        endLabel.textColor = .white
        self.addSubview(endLabel)
    }
    
    private func createConstrains() {
        imgView.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalTo(5)
            make.width.equalTo(32)
            make.height.equalTo(16)
        }
        preLabel.snp.makeConstraints { make in
            make.left.equalTo(imgView.snp.right).offset(3)
            make.centerY.equalToSuperview()
        }
        icon.snp.makeConstraints { make in
            make.left.equalTo(preLabel.snp.right).offset(3)
            make.centerY.equalToSuperview()
            make.width.equalTo(16)
            make.height.equalTo(16)
        }
        endLabel.snp.makeConstraints { make in
            make.left.equalTo(icon.snp.right).offset(3)
            make.centerY.equalToSuperview()
            make.right.equalToSuperview().offset(-15)
        }
    }
    
    public func setMicOwner(with owner: String, url: String) {
        let imageUrl = URL(string: url)
        guard let imageData = try? Data(contentsOf: imageUrl!), let image = UIImage(data: imageData) else {
            return
        }
        icon.image = image
        endLabel.text = " \(owner)\(getLocalizeString(with: "sr_next_sing"))"
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
