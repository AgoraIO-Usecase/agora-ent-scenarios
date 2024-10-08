//
//  ShowToolMenuView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/9.
//

import UIKit

enum ShowToolMenuType: CaseIterable {
    case camera
    case mic
    case real_time_data
    
    var imageName: String {
        switch self {
        case .camera: return "show_camera"
        case .mic: return "show_mic"
        case .real_time_data: return "show_realtime"
        }
    }
    
    var selectedImageName: String? {
        switch self {
        case .camera: return "show_camera_off"
        case .mic: return "show_mic_off"
        default: return nil
        }
    }
    
    var title: String {
        switch self {
        case .camera: return "setting_video_on".showTo1v1Localization()
        case .mic: return "setting_mic_on".showTo1v1Localization()
        case .real_time_data: return "setting_statistic".showTo1v1Localization()
        }
    }
    var selectedTitle: String? {
        switch self {
        case .camera: return "setting_video_off".showTo1v1Localization()
        case .mic: return "setting_mic_off".showTo1v1Localization()
        default: return title
        }
    }
}

class ShowToolMenuModel {
    var imageName: String = ""
    var selectedImageName: String = ""
    var title: String = ""
    var type: ShowToolMenuType = .real_time_data
    var isSelected: Bool = false
}

class ShowToolMenuView: UIView {
    private var dataArray:[ShowToolMenuModel] = []
    private var menuTypes: [ShowToolMenuType]
    var title: String? {
        didSet {
            collectionView.reloadData()
        }
    }
    var onTapItemClosure: ((ShowToolMenuType, Bool) -> Void)?
    var selectedMap: [ShowToolMenuType: Bool]? {
        didSet {
            updateToolType(type: menuTypes)
            collectionView.reloadData()
        }
    }
    
    public lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.minimumLineSpacing = 33
        layout.minimumInteritemSpacing = 0
        layout.sectionInset = .zero
        layout.itemSize = CGSize(width: UIScreen.main.bounds.width / 4, height: 47)
        layout.scrollDirection = .vertical
        let view = UICollectionView(frame: bounds, collectionViewLayout: layout)
        view.showsHorizontalScrollIndicator = false
        view.delegate = self
        view.dataSource = self
        view.register(LiveToolViewCell.self,
                      forCellWithReuseIdentifier: LiveToolViewCell.description())
        view.translatesAutoresizingMaskIntoConstraints = false
        view.backgroundColor = .clear
        return view
    }()
    
    required init(menuTypes: [ShowToolMenuType]) {
        self.menuTypes = menuTypes
        super.init(frame: .zero)
        setupUI()
        updateToolType(type: menuTypes)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func updateToolType(type: [ShowToolMenuType]) {
        var datas = [ShowToolMenuModel]()
        type.forEach({
            let model = ShowToolMenuModel()
            model.imageName = $0.imageName
            model.selectedImageName = $0.selectedImageName ?? ""
            model.title = $0.title
            model.type = $0
            model.isSelected = selectedMap?[$0] ?? false
            datas.append(model)
        })
        self.dataArray = datas
    }
    
    func updateStatus(type: ShowToolMenuType, isSelected: Bool) {
        let index = dataArray.compactMap({ $0 as? ShowToolMenuModel }).firstIndex(where: { $0.type == type }) ?? 0
        var datas = dataArray
        if let model = datas[index] as? ShowToolMenuModel {
            model.isSelected = isSelected
            datas[index] = model
        }
        self.dataArray = datas
    }
    
    private func setupUI() {
        backgroundColor = UIColor(hexString: "#151325")?.withAlphaComponent(0.85)
        translatesAutoresizingMaskIntoConstraints = false
        layer.cornerRadius = 10
        layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        layer.masksToBounds = true
        
        addSubview(collectionView)
        
        widthAnchor.constraint(equalToConstant: aui_width).isActive = true
        
        collectionView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        collectionView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        collectionView.topAnchor.constraint(equalTo: topAnchor, constant: 28).isActive = true
        collectionView.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -UIDevice.current.aui_SafeDistanceBottom).isActive = true
    }
}
extension ShowToolMenuView: UICollectionViewDelegate, UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: LiveToolViewCell.description(), for: indexPath) as! LiveToolViewCell
        cell.setToolData(item: self.dataArray[indexPath.item])
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard let cell = collectionView.cellForItem(at: indexPath) as? LiveToolViewCell else { return }
        let model = self.dataArray[indexPath.item]
        let isSelected = cell.updateButtonState()
        onTapItemClosure?(model.type, isSelected)
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, referenceSizeForHeaderInSection section: Int) -> CGSize {
        CGSize(width: collectionView.aui_width, height: 50)
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
        label.textColor = UIColor(hexString: "#C6C4DD")
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
        iconButton.setImage(UIImage.sceneImage(name: model.imageName), for: .normal)
        iconButton.setImage(UIImage.sceneImage(name: model.selectedImageName), for: .selected)
        iconButton.isSelected = model.isSelected
        titleLabel.text = model.isSelected ? model.type.selectedTitle : model.type.title
    }
    
    @discardableResult
    func updateButtonState() -> Bool {
        iconButton.isSelected = !iconButton.isSelected
        model?.isSelected = iconButton.isSelected
        titleLabel.text = iconButton.isSelected ? model?.type.title : model?.type.title
        return iconButton.isSelected
    }
}
