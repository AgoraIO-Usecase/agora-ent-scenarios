//
//  RoomNoDataView.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/28.
//

import UIKit


class RoomNoDataView: UIView {
    private lazy var bgImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.image = UIImage.sceneImage(name: "roomList")
        return imgView
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
        addSubview(bgImgView)
        addSubview(noDataImageView)
        addSubview(noDataDialogView)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let padding = 52.0
        let top = UIDevice.current.aui_SafeDistanceTop + 200
        let width = self.aui_width - padding * 2
        let imageSize = noDataImageView.image!
        let height = width * imageSize.size.height / imageSize.size.width
        noDataImageView.frame = CGRect(x: padding, y: top, width: width, height: height)
        noDataDialogView.frame = CGRect(x:0 , y: top + height + 30, width: self.aui_width , height: 50)
        bgImgView.frame = bounds
    }
}
