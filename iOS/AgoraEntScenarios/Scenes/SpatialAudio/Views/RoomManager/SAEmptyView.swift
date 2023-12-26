//
//  VREmptyView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/26.
//

import UIKit
import ZSwiftBaseLib

public class SAEmptyView: UIView {
    var emptyImage = UIImage.sceneImage(name:"sa_img_msg_empty")

    lazy var imageView: UIImageView = .init(frame: .zero).contentMode(.scaleAspectFit).image(self.emptyImage!)

    lazy var textLabel: UILabel = .init(frame: .zero).textAlignment(.center).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x979CBB)).numberOfLines(0)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .clear
    }

    convenience init(frame: CGRect, title: String, image: UIImage?) {
        self.init(frame: frame)
        if image != nil {
            emptyImage = image!
        }
        addSubview(imageView)
        textLabel.text = title.spatial_localized()
        addSubview(textLabel)
        createConstrains()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func createConstrains() {
        imageView.snp.makeConstraints { make in
            make.top.equalToSuperview().priority(999)
            make.center.equalToSuperview()
        }
        textLabel.snp.makeConstraints { make in
            make.top.equalTo(imageView.snp.bottom)
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.height.greaterThanOrEqualTo(20)
            make.bottom.equalToSuperview().priority(999)
        }
    }
}

public class SARoomListEmptyView: UIView {
    var emptyImage = UIImage.sceneImage(name:"empty")

    lazy var imageView: UIImageView = .init(frame: .zero).contentMode(.scaleAspectFit).image(self.emptyImage!)

    lazy var textLabel: UILabel = .init(frame: .zero).textAlignment(.center).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x979CBB)).numberOfLines(0)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .clear
    }

    convenience init(frame: CGRect, title: String, image: UIImage?) {
        self.init(frame: frame)
        if image != nil {
            emptyImage = image!
        }
        addSubview(imageView)
        textLabel.text = title.spatial_localized()
        addSubview(textLabel)
        createConstrains()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func createConstrains() {
        imageView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.centerY.equalToSuperview().multipliedBy(0.3)
        }
        textLabel.snp.makeConstraints { make in
            make.top.equalTo(imageView.snp.bottom)
            make.left.equalTo(20)
            make.right.equalTo(-20)
        }
    }
}
