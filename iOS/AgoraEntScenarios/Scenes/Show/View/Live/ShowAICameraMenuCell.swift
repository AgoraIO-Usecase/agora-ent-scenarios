//
//  ShowAICameraMenuCell.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2024/3/7.
//

import UIKit

enum AICameraMenuItemState: Int {
    case normal = 0
    case loading = 1
    case done = 2
}

enum AICameraMenuItemID {
    case rhythm_heart // 律动
    case rhythm_portrait // 律动
    case rhythm_front_back // 效果描述：焦距平缓但不均匀地变大再变小。
    case rhythm_up_down  // 效果描述：镜头首先向上移动，然后再向下移动。
    case rhythm_left_right // 效果描述：镜头首先向左移动，然后再向右移动。
    case rhythm_faceLock_L // 效果描述：脸部被锁定在视频中间。
    case rhythm_faceLock_P// 效果描述：脸部被锁定在固定点位（视频画面中上部 2/5 处）
    case avatar //

}

class AICameraMenuItem: NSObject {
    private var _state: AICameraMenuItemState = .normal
    
    var isSelected = false
    var id: AICameraMenuItemID
    
    var icon: String {
        get{
            switch id {
            case .rhythm_heart:
                "show_aicamera_avatar"
            case .rhythm_portrait:
                "show_aicamera_avatar"
            case .rhythm_front_back:
                "show_aicamera_avatar"
            case .rhythm_up_down:
                "show_aicamera_avatar"
            case .rhythm_left_right:
                "show_aicamera_avatar"
            case .rhythm_faceLock_L:
                "show_aicamera_avatar"
            case .rhythm_faceLock_P:
                "show_aicamera_avatar"
            case .avatar:
                "show_aicamera_avatar"
            }
        }
    }
    
    var state: AICameraMenuItemState {
        get{
            switch id {
            case .rhythm_heart:
                return .done
            case .rhythm_portrait:
                return .done
            case .rhythm_front_back:
                return .done
            case .rhythm_up_down:
                return .done
            case .rhythm_left_right:
                return .done
            case .rhythm_faceLock_L:
                return .done
            case .rhythm_faceLock_P:
                return .done
            case .avatar:
                return _state
            }
        }
    }
    
    func updateState(_ state: AICameraMenuItemState) {
        _state = state
    }
    
    init(id: AICameraMenuItemID) {
        self.id = id
    }
}

class ShowAICameraMenuCell: UICollectionViewCell {
    
    private var downloadState: AICameraMenuItemState = .normal {
        didSet{
            downloadButton.isSelected = downloadState == .loading
            downloadButton.isHidden = downloadState == .done
        }
    }
    
    var menuItem: AICameraMenuItem? {
        didSet{
            guard let menuItem = menuItem else { return }
            iconImgView.image = UIImage.show_sceneImage(name: menuItem.icon)
            downloadState = menuItem.state
            if downloadState == .done {
                indicatorView.isHidden = !menuItem.isSelected
            }else{
                indicatorView.isHidden = true
            }
        }
    }
    
    // 选中标识
    private lazy var indicatorView: UIView = {
        let view = UIView()
        view.layer.cornerRadius = 8
        view.layer.borderColor = UIColor.show_zi03.cgColor
        view.layer.borderWidth = 1
        view.isHidden = true
        return view
    }()
    
    // 图片
    private lazy var iconImgView: UIImageView = {
        let imageView = UIImageView()
        imageView.layer.cornerRadius = 8
        imageView.layer.masksToBounds = true
        return imageView
    }()
    
    // 下载按钮
    private lazy var downloadButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.show_sceneImage(name: "show_aicamera_download"), for: .normal)
        button.setImage(UIImage.show_sceneImage(name: "show_aicamera_loading"), for: .selected)
        button.addTarget(self, action: #selector(didClickDownloadButton), for: .touchUpInside)
        return button
    }()
    
    var onClickDownloadButton: ((_ state: AICameraMenuItemState)->())?
    
//    override var isSelected: Bool {
//        didSet{
//            if downloadState == .done {
//                indicatorView.isHidden = !isSelected
//            }else{
//                indicatorView.isHidden = true
//            }
//        }
//    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(){
       
        // 图片
        contentView.addSubview(iconImgView)
        iconImgView.snp.makeConstraints { make in
            make.left.bottom.equalToSuperview()
            make.width.height.equalTo(48)
        }
        
        // 选中标识
        contentView.addSubview(indicatorView)
        indicatorView.snp.makeConstraints { make in
            make.edges.equalTo(iconImgView)
        }
        
        // 下载
        contentView.addSubview(downloadButton)
        downloadButton.snp.makeConstraints { make in
            make.centerX.equalTo(iconImgView.snp.right)
            make.centerY.equalTo(iconImgView.snp.top)
        }
    }
    
   
    
    @objc func didClickDownloadButton() {
        if downloadState == .loading {return}
        onClickDownloadButton?(self.downloadState)
    }
}
