//
//  ShowTo1v1UserNoDataView.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/28.
//

import UIKit


class Pure1v1UserNoDataView: UIView {
    private lazy var gradientLayer: CAGradientLayer = {
        let layer = CAGradientLayer()
        layer.colors = [
            UIColor(hexString: "#FFF6ED")!.cgColor,
            UIColor(hexString: "#F6F3DB")!.cgColor,
            UIColor(hexString: "#F7D3B1")!.cgColor,
            UIColor(hexString: "#EEA3A1")!.cgColor,
            UIColor(hexString: "#EFC75C")!.cgColor,
        ]
        
        return layer
    }()
    
    private lazy var noDataImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage.sceneImage(name: "user_empty")
        return imageView
    }()
    
    lazy var noDataDialogView: ShowTo1v1NoDataDialog = {
        let view = ShowTo1v1NoDataDialog()
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubView()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubView() {
        layer.addSublayer(gradientLayer)
        gradientLayer.frame = bounds
        addSubview(noDataImageView)
        addSubview(noDataDialogView)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let padding = 0.0
        let top = UIDevice.current.aui_SafeDistanceTop + 50
        let width = self.aui_width - padding * 2
        let imageSize = noDataImageView.image!
        let height = width * imageSize.size.height / imageSize.size.width
        noDataImageView.frame = CGRect(x: padding, y: top, width: width, height: height)
        noDataDialogView.frame = bounds
    }
}
