//
//  VLPhotoView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/18.
//

import UIKit
import HXPhotoPicker

class VLPhotoView: UIView {
    private lazy var flowLayout: UICollectionViewFlowLayout = {
        let flowLayout = UICollectionViewFlowLayout()
        return flowLayout
    }()
    private lazy var collectionView: UICollectionView = {
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.register(VLPhotoViewResultCell.self, forCellWithReuseIdentifier: "ResultViewCellID")
        collectionView.register(VLPhotoViewAddCell.self, forCellWithReuseIdentifier: "ResultAddViewCellID")
        collectionView.dragDelegate = self
        collectionView.dropDelegate = self
        collectionView.backgroundColor = .clear
        collectionView.dragInteractionEnabled = true
        return collectionView
    }()
    private var addCell: VLPhotoViewAddCell {
        let cell = collectionView.dequeueReusableCell(
            withReuseIdentifier: "ResultAddViewCellID",
            for: IndexPath(item: selectedAssets.count, section: 0)
        ) as! VLPhotoViewAddCell
        return cell
    }
    private var canSetAddCell: Bool {
        if selectedAssets.count == config.maximumSelectedCount &&
            config.maximumSelectedCount > 0 {
            return false
        }
        return true
    }
    private var collectionViewHeightConstraint: NSLayoutConstraint?
    private let row_Count: Int = UIDevice.current.userInterfaceIdiom == .pad ? 5 : 3
    /// 相关配置
    private var config: PickerConfiguration = PhotoTools.getWXPickerConfig(isMoment: true)
    private var localAssetArray: [PhotoAsset] = []
    private var beforeRowCount: Int = 0
    var maxCount: Int = 3 {
        didSet {
            config.maximumSelectedCount = maxCount
        }
    }
    /// 当前已选资源
    var selectedAssets: [PhotoAsset] = []
    /// 是否选中的原图
    var isOriginal: Bool = false
    /// 相机拍摄的本地资源
    var localCameraAssetArray: [PhotoAsset] = []
    
    private var localCachePath: String {
        var cachePath = FileManager.cachesPath
        cachePath.append(contentsOf: "/com.silence.WeChat_Moment")
        return cachePath
    }
    private var localURL: URL {
        var cachePath = localCachePath
        cachePath.append(contentsOf: "/PhotoAssets")
        return URL.init(fileURLWithPath: cachePath)
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        config.allowSelectedTogether = false
        config.selectOptions = [.photo]
        config.maximumSelectedCount = maxCount
        
        collectionViewHeightConstraint = heightAnchor.constraint(equalToConstant: 80)
        collectionViewHeightConstraint?.isActive = true
        
        addSubview(collectionView)
        collectionView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        collectionView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        collectionView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        collectionView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        let flowLayout: UICollectionViewFlowLayout = collectionView.collectionViewLayout as! UICollectionViewFlowLayout
        let itemWidth = Int((width - 24 - CGFloat(row_Count - 1))) / row_Count
        flowLayout.itemSize = CGSize(width: itemWidth, height: itemWidth)
        flowLayout.minimumInteritemSpacing = 1
        flowLayout.minimumLineSpacing = 1
        configCollectionViewHeight()
    }
    
    private func getCollectionViewrowCount() -> Int {
        let assetCount = canSetAddCell ? selectedAssets.count + 1 : selectedAssets.count
        var rowCount = assetCount / row_Count + 1
        if assetCount % row_Count == 0 {
            rowCount -= 1
        }
        return rowCount
    }
    private func configCollectionViewHeight() {
        let rowCount = getCollectionViewrowCount()
        beforeRowCount = rowCount
        let itemWidth = Int((width - 24 - CGFloat(row_Count - 1))) / row_Count
        let heightConstraint = CGFloat(rowCount * itemWidth + rowCount)
        collectionViewHeightConstraint?.constant = heightConstraint
        collectionViewHeightConstraint?.isActive = true
    }
    private func updateCollectionViewHeight() {
        let rowCount = getCollectionViewrowCount()
        if beforeRowCount == rowCount {
            return
        }
        UIView.animate(withDuration: 0.25) {
            self.configCollectionViewHeight()
            self.layoutIfNeeded()
        }
    }
    private func presentPickerController() {
        let pickerController = PhotoPickerController(picker: config)
        pickerController.pickerDelegate = self
        pickerController.selectedAssetArray = selectedAssets
        pickerController.localCameraAssetArray = localCameraAssetArray
        pickerController.isOriginal = isOriginal
        pickerController.localAssetArray = localAssetArray
        pickerController.autoDismiss = false
        pickerController.modalPresentationStyle = .fullScreen
        UIViewController.cl_topViewController()?.present(pickerController, animated: true)
    }
    
    func getAssetUrl(completionHandler: @escaping ([URL]) -> Void) {
        selectedAssets.getURLs(
            compression: nil,
            toFile: nil) { result, photoAsset, index in
            print("第" + String(index + 1) + "个")
            switch result {
            case .success(let response):
                if let livePhoto = response.livePhoto {
                    print("LivePhoto里的图片地址：", livePhoto.imageURL)
                    print("LivePhoto里的视频地址：", livePhoto.videoURL)
                    return
                }
                print(response.urlType == .network ?
                        response.mediaType == .photo ?
                            "网络图片地址：" : "网络视频地址：" :
                        response.mediaType == .photo ?
                            "本地图片地址" : "本地视频地址",
                      response.url)
            case .failure(let error):
                print("地址获取失败", error)
            }
        } completionHandler: { urls in
            completionHandler(urls)
        }
    }
}
extension VLPhotoView: UICollectionViewDelegate,
                        UICollectionViewDataSource,
                        ResultViewCellDelegate,
                        UICollectionViewDragDelegate,
                       UICollectionViewDropDelegate {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        canSetAddCell ? selectedAssets.count + 1 : selectedAssets.count
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        if canSetAddCell && indexPath.item == selectedAssets.count {
            return addCell
        }
        let cell = collectionView.dequeueReusableCell(
            withReuseIdentifier: "ResultViewCellID",
            for: indexPath
        ) as! VLPhotoViewResultCell
        cell.resultDelegate = self
        cell.photoAsset = selectedAssets[indexPath.item]
        return cell
    }
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        if canSetAddCell && indexPath.item == selectedAssets.count {
            presentPickerController()
            return
        }
        if selectedAssets.isEmpty {
            return
        }
        var config = HXPhotoPicker.PhotoBrowser.Configuration()
        config.showDelete = true
        let cell = collectionView.cellForItem(at: indexPath) as? VLPhotoViewResultCell
        HXPhotoPicker.PhotoBrowser.show(
            selectedAssets,
            pageIndex: indexPath.item,
            config: config,
            transitionalImage: cell?.photoView.image
        ) { index, _ in
            self.collectionView.cellForItem(
                at: IndexPath(
                    item: index,
                    section: 0
                )
            ) as? VLPhotoViewResultCell
        } deleteAssetHandler: { index, photoAsset, photoBrowser in
            // 点击了删除按钮
            PhotoTools.showAlert(
                viewController: photoBrowser,
                title: "是否删除当前资源",
                leftActionTitle: "确定",
                leftHandler: { (alertAction) in
                    photoBrowser.deleteCurrentPreviewPhotoAsset()
                    self.previewDidDeleteAsset(index: index)
                }, rightActionTitle: "取消") { (alertAction) in }
        } longPressHandler: { index, photoAsset, photoBrowser in
            if photoAsset.mediaSubType == .localLivePhoto ||
               photoAsset.mediaSubType == .livePhoto {
                return
            }
        }
    }
    func collectionView(_ collectionView: UICollectionView, canEditItemAt indexPath: IndexPath) -> Bool {
        if canSetAddCell && indexPath.item == selectedAssets.count {
            return false
        }
        return true
    }
    func collectionView(
        _ collectionView: UICollectionView,
        moveItemAt sourceIndexPath: IndexPath,
        to destinationIndexPath: IndexPath) {
        let sourceAsset = selectedAssets[sourceIndexPath.item]
        selectedAssets.remove(at: sourceIndexPath.item)
        selectedAssets.insert(sourceAsset, at: destinationIndexPath.item)
    }
    func collectionView(
        _ collectionView: UICollectionView,
        itemsForBeginning session: UIDragSession,
        at indexPath: IndexPath) -> [UIDragItem] {
        let itemProvider = NSItemProvider.init()
        let dragItem = UIDragItem.init(itemProvider: itemProvider)
        dragItem.localObject = indexPath
        return [dragItem]
    }
    func collectionView(_ collectionView: UICollectionView, canHandle session: UIDropSession) -> Bool {
        if let sourceIndexPath = session.items.first?.localObject as? IndexPath {
            if canSetAddCell && sourceIndexPath.item == selectedAssets.count {
                return false
            }
        }
        return true
    }
    func collectionView(
        _ collectionView: UICollectionView,
        dropSessionDidUpdate session: UIDropSession,
        withDestinationIndexPath
            destinationIndexPath: IndexPath?) -> UICollectionViewDropProposal {
        if let sourceIndexPath = session.items.first?.localObject as? IndexPath {
            if canSetAddCell && sourceIndexPath.item == selectedAssets.count {
                return UICollectionViewDropProposal.init(operation: .forbidden, intent: .insertAtDestinationIndexPath)
            }
        }
        if destinationIndexPath != nil && canSetAddCell && destinationIndexPath!.item == selectedAssets.count {
            return UICollectionViewDropProposal.init(operation: .forbidden, intent: .insertAtDestinationIndexPath)
        }
        var dropProposal: UICollectionViewDropProposal
        if session.localDragSession != nil {
            dropProposal = UICollectionViewDropProposal.init(operation: .move, intent: .insertAtDestinationIndexPath)
        }else {
            dropProposal = UICollectionViewDropProposal.init(operation: .copy, intent: .insertAtDestinationIndexPath)
        }
        return dropProposal
    }
    func collectionView(
        _ collectionView: UICollectionView,
        performDropWith coordinator: UICollectionViewDropCoordinator) {
        if let destinationIndexPath = coordinator.destinationIndexPath,
           let sourceIndexPath = coordinator.items.first?.sourceIndexPath {
            collectionView.isUserInteractionEnabled = false
            collectionView.performBatchUpdates {
                let sourceAsset = selectedAssets[sourceIndexPath.item]
                selectedAssets.remove(at: sourceIndexPath.item)
                selectedAssets.insert(sourceAsset, at: destinationIndexPath.item)
                collectionView.moveItem(at: sourceIndexPath, to: destinationIndexPath)
            } completion: { (isFinish) in
                collectionView.isUserInteractionEnabled = true
            }
            if let dragItem = coordinator.items.first?.dragItem {
                coordinator.drop(dragItem, toItemAt: destinationIndexPath)
            }
        }
    }
    func cell(didDeleteButton cell: VLPhotoViewResultCell) {
        if let indexPath = collectionView.indexPath(for: cell) {
            let isFull = selectedAssets.count == config.maximumSelectedCount
            selectedAssets.remove(at: indexPath.item)
            if isFull {
                collectionView.reloadData()
            }else {
                collectionView.deleteItems(at: [indexPath])
            }
            updateCollectionViewHeight()
        }
    }
}

extension VLPhotoView: PhotoPickerControllerDelegate {
    func pickerController(_ pickerController: PhotoPickerController, didFinishSelection result: PickerResult) {
        selectedAssets = result.photoAssets
        isOriginal = result.isOriginal
        collectionView.reloadData()
        updateCollectionViewHeight()
        pickerController.dismiss(true)
    }
    
    func pickerController(didCancel pickerController: PhotoPickerController) {
        pickerController.dismiss(true)
    }
    
    func pickerController(
        _ pickerController: PhotoPickerController,
        previewDidDeleteAssets photoAssets: [PhotoAsset], at indexs: [Int]) {
        guard let index = indexs.first else {
            return
        }
        previewDidDeleteAsset(index: index)
    }
    private func previewDidDeleteAsset(index: Int) {
        let isFull = selectedAssets.count == config.maximumSelectedCount
        selectedAssets.remove(at: index)
        if isFull {
            collectionView.reloadData()
        }else {
            collectionView.deleteItems(at: [IndexPath.init(item: index, section: 0)])
        }
        updateCollectionViewHeight()
    }
    func pickerController(
        _ pickerController: PhotoPickerController,
        presentPreviewViewForIndexAt index: Int) -> UIView? {
        let cell = collectionView.cellForItem(at: IndexPath(item: index, section: 0))
        return cell
    }
    func pickerController(
        _ pickerController: PhotoPickerController,
        presentPreviewImageForIndexAt index: Int) -> UIImage? {
        let cell = collectionView.cellForItem(at: IndexPath(item: index, section: 0)) as? VLPhotoViewResultCell
        return cell?.photoView.image
    }
    func pickerController(
        _ pickerController: PhotoPickerController,
        dismissPreviewViewForIndexAt index: Int) -> UIView? {
        let cell = collectionView.cellForItem(at: IndexPath(item: index, section: 0))
        return cell
    }
}

class VLPhotoViewAddCell: PhotoPickerBaseViewCell {
    override func initView() {
        super.initView()
        isHidden = false
        contentView.cornerRadius(8)
        photoView.backgroundColor = UIColor(hex: "#08062F", alpha: 0.03)
        photoView.placeholder = UIImage(named: "hx_picker_add_img")
    }
}

@objc
protocol ResultViewCellDelegate: AnyObject {
    @objc optional func cell(didDeleteButton cell: VLPhotoViewResultCell)
}

class VLPhotoViewResultCell: PhotoPickerViewCell {
    weak var resultDelegate: ResultViewCellDelegate?
    lazy var deleteButton: UIButton = {
        let deleteButton = UIButton.init(type: .custom)
        deleteButton.setImage(UIImage.init(named: "hx_compose_delete"), for: .normal)
        deleteButton.size = deleteButton.currentImage?.size ?? .zero
        deleteButton.addTarget(self, action: #selector(didDeleteButtonClick), for: .touchUpInside)
        return deleteButton
    }()
    override var photoAsset: PhotoAsset! {
        didSet {
            if photoAsset.mediaType == .photo {
                // 隐藏被编辑过的标示
                assetEditMarkIcon.isHidden = true
                assetTypeMaskView.isHidden = true
            }
        }
    }
    override func requestThumbnailImage() {
        // 因为这里的cell不会很多，重新设置 targetWidth，使图片更加清晰
        super.requestThumbnailImage(targetWidth: width * UIScreen.main.scale)
    }
    @objc func didDeleteButtonClick() {
        resultDelegate?.cell?(didDeleteButton: self)
    }
    override func initView() {
        super.initView()
        contentView.cornerRadius(8)
        contentView.addSubview(deleteButton)
    }
    
    override func layoutView() {
        super.layoutView()
        deleteButton.frame.origin.x = width - deleteButton.width
    }
}


extension FileManager {
    class var documentPath: String {
        guard let documentPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).last else {
            return ""
        }
        return documentPath
    }
    class var cachesPath: String {
        guard let cachesPath = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true).last else {
            return ""
        }
        return cachesPath
    }
    class var tempPath: String {
        NSTemporaryDirectory()
    }
}
