//
//  ShowToolMenuView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/9.
//

import UIKit
import Agora_Scene_Utils

enum ShowToolMenuType: CaseIterable {
    case switch_camera
    case camera
    case mic
    case real_time_data
    case HD
    case setting
    case mute_mic
    case end_pk
    
    var imageName: String {
        switch self {
        case .switch_camera: return "show_switch_camera"
        case .camera: return "show_camera"
        case .mic, .mute_mic: return "show_mic"
        case .real_time_data: return "show_realtime"
        case .HD: return "show_hd"
        case .setting: return "show_setting"
        case .end_pk: return "show_end_pk"
        }
    }
    
    var selectedImageName: String? {
        switch self {
        case .camera: return "show_camera_off"
        case .mic, .mute_mic: return "show_mic_off"
        default: return nil
        }
    }
    
    var title: String {
        switch self {
        case .switch_camera: return "翻转镜头".show_localized
        case .camera: return "摄像头开".show_localized
        case .mic: return "麦克风开".show_localized
        case .real_time_data: return "实时数据".show_localized
        case .HD: return "画质".show_localized
        case .setting: return "高级设置".show_localized
        case .mute_mic: return "静音".show_localized
        case .end_pk: return "结束连麦".show_localized
        }
    }
    var selectedTitle: String? {
        switch self {
        case .camera: return "摄像头关".show_localized
        case .mic: return "麦克风关".show_localized
        case .mute_mic: return "取消静音".show_localized
        default: return title
        }
    }
}

class ShowToolMenuModel {
    var imageName: String = ""
    var selectedImageName: String = ""
    var title: String = ""
    var type: ShowToolMenuType = .switch_camera
    var isSelected: Bool = false
}

enum ShowMenuType {
    /// 未pk观众
    case idle_audience
    /// 未pk主播
    case idle_broadcaster
    /// PK中
    case pking
    /// 管理麦位
    case managerMic
}

class ShowToolMenuView: UIView {
    var title: String? {
        didSet {
            collectionView.reloadData()
        }
    }
    var onTapItemClosure: ((ShowToolMenuType, Bool) -> Void)?
    var selectedMap: [ShowToolMenuType: Bool]? {
        didSet {
            collectionView.reloadData()
        }
    }
    
    public lazy var collectionView: AGECollectionView = {
        let view = AGECollectionView()
        let w = Screen.width / 4
        view.itemSize = CGSize(width: w, height: 47)
        view.showsHorizontalScrollIndicator = false
        view.minInteritemSpacing = 0
        view.minLineSpacing = 33
        view.scrollDirection = .vertical
        view.delegate = self
        view.register(LiveToolViewCell.self,
                      forCellWithReuseIdentifier: LiveToolViewCell.description())
        view.register(LiveToolHeaderView.self,
                      forSupplementaryViewOfKind: UICollectionView.elementKindSectionHeader,
                      withReuseIdentifier: LiveToolHeaderView.description())
        return view
    }()
    
    var type: ShowMenuType = .idle_audience {
        didSet {
            
            switch type {
            case .idle_broadcaster:
                updateToolType(type: [.switch_camera, .camera, .mic, .real_time_data, .HD, .setting])
            case .pking:
                updateToolType(type: [.switch_camera, .camera, .mute_mic, .end_pk])
            case .managerMic:
                updateToolType(type: [.mute_mic, .end_pk])
            case .idle_audience:
                updateToolType(type: [.real_time_data, .setting])
            }
        }
    }
    
    init(type: ShowMenuType) {
        super.init(frame: .zero)
        setupUI()
        defer {
            self.type = type
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func updateToolType(type: [ShowToolMenuType]) {
        var datas = [ShowToolMenuModel]()
        type.forEach({
            let model = ShowToolMenuModel()
            model.imageName = $0.imageName
            model.selectedImageName = $0.selectedImageName ?? $0.imageName
            model.title = $0.title
            model.type = $0
            model.isSelected = selectedMap?[$0] ?? false
            datas.append(model)
        })
        collectionView.dataArray = datas
    }
    
    func updateStatus(type: ShowToolMenuType, isSelected: Bool) {
        let index = collectionView.dataArray?.compactMap({ $0 as? ShowToolMenuModel }).firstIndex(where: { $0.type == type }) ?? 0
        var datas = collectionView.dataArray
        if let model = datas?[index] as? ShowToolMenuModel {
            model.isSelected = isSelected
            datas?[index] = model
        }
        collectionView.dataArray = datas
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        backgroundColor = UIColor(hex: "#151325", alpha: 0.85)
        layer.cornerRadius = 10
        layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        layer.masksToBounds = true
        
        addSubview(collectionView)
        
        widthAnchor.constraint(equalToConstant: Screen.width).isActive = true
        
        collectionView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        collectionView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        collectionView.topAnchor.constraint(equalTo: topAnchor, constant: 28).isActive = true
        collectionView.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -Screen.safeAreaBottomHeight()).isActive = true
    }
}
extension ShowToolMenuView: AGECollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: LiveToolViewCell.description(), for: indexPath) as! LiveToolViewCell
        cell.setToolData(item: self.collectionView.dataArray?[indexPath.item])
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard let cell = collectionView.cellForItem(at: indexPath) as? LiveToolViewCell,
              let model = self.collectionView.dataArray?[indexPath.item] as? ShowToolMenuModel else { return }
        let isSelected = cell.updateButtonState()
        onTapItemClosure?(model.type, isSelected)
    }
    
    func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
        let headerView = collectionView.dequeueReusableSupplementaryView(ofKind: UICollectionView.elementKindSectionHeader,
                                                                         withReuseIdentifier: LiveToolHeaderView.description(),
                                                                         for: indexPath) as! LiveToolHeaderView
        headerView.tipsLabel.text = title
        return headerView
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, referenceSizeForHeaderInSection section: Int) -> CGSize {
        CGSize(width: Screen.width, height: (type == .idle_audience || type == .idle_broadcaster) ? 0 : 50)
    }
}


class LiveToolHeaderView: UICollectionReusableView {
    lazy var tipsLabel: AGELabel = {
        let label = AGELabel(colorStyle: .white, fontStyle: .middle)
//        label.text = "对观众SLKGJAKLGJ"
        return label
    }()
    private lazy var lineView: AGEView = {
        let view = AGEView()
        view.backgroundColor = .gray
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(tipsLabel)
        addSubview(lineView)
        tipsLabel.translatesAutoresizingMaskIntoConstraints = false
        lineView.translatesAutoresizingMaskIntoConstraints = false
        
        tipsLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20).isActive = true
        tipsLabel.topAnchor.constraint(equalTo: topAnchor).isActive = true
        
        lineView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20).isActive = true
        lineView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -20).isActive = true
        lineView.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -20).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
}

class LiveToolViewCell: UICollectionViewCell {
    private lazy var iconButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "icon-rotate"), for: .normal)
        button.isUserInteractionEnabled = false
        return button
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "Switch_Camera"
        label.textColor = UIColor(hex: "#C6C4DD")
        label.font = .systemFont(ofSize: 12)
        return label
    }()
    
    private var model: ShowToolMenuModel?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        iconButton.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        
        contentView.addSubview(iconButton)
        contentView.addSubview(titleLabel)
        
        iconButton.centerXAnchor.constraint(equalTo: contentView.centerXAnchor).isActive = true
        iconButton.bottomAnchor.constraint(equalTo: titleLabel.topAnchor, constant: -5).isActive = true
        
        titleLabel.centerXAnchor.constraint(equalTo: contentView.centerXAnchor).isActive = true
        titleLabel.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
    }
    
    func setToolData(item: Any?) {
        guard let model = item as? ShowToolMenuModel else { return }
        self.model = model
        iconButton.setImage(UIImage.show_sceneImage(name: model.imageName), for: .normal)
        iconButton.setImage(UIImage.show_sceneImage(name: model.selectedImageName), for: .selected)
        iconButton.isSelected = model.isSelected
        titleLabel.text = model.isSelected ? model.type.selectedTitle : model.type.title
    }
    
    @discardableResult
    func updateButtonState() -> Bool {
        iconButton.isSelected = !iconButton.isSelected
        model?.isSelected = iconButton.isSelected
        titleLabel.text = iconButton.isSelected ? model?.type.selectedTitle : model?.type.title
        return iconButton.isSelected
    }
}
