//
//  VLPhotoView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/18.
//

import UIKit
import PhotosUI

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
        if selectedAssets.count == maxCount {
            return false
        }
        return true
    }
    
    private var collectionViewHeightConstraint: NSLayoutConstraint?
    private let row_Count: Int = UIDevice.current.userInterfaceIdiom == .pad ? 5 : 3
    private var localAssetArray: [PhotoAsset] = []
    private var beforeRowCount: Int = 0
    var maxCount: Int = 3
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
    
    // 使用系统相册选择器
    private func presentPickerController() {
        if #available(iOS 14.0, *) {
            var configuration = PHPickerConfiguration()
            configuration.filter = .images // 只选择图片
            configuration.selectionLimit = maxCount - selectedAssets.count // 0 表示不限制选择数量
            configuration.preferredAssetRepresentationMode = .automatic
            
            let pickerViewController = PHPickerViewController(configuration: configuration)
            pickerViewController.delegate = self
            pickerViewController.modalPresentationStyle = .fullScreen // 设置全屏弹出
            UIViewController.cl_topViewController()?.present(pickerViewController, animated: true, completion: nil)
        } else {
            // Fallback on earlier versions
            let imagePickerController = UIImagePickerController()
            imagePickerController.sourceType = .photoLibrary
            imagePickerController.delegate = self
            imagePickerController.allowsEditing = true // 打开编辑功能
            imagePickerController.modalPresentationStyle = .fullScreen // 设置全屏弹出
            imagePickerController.modalTransitionStyle = .coverVertical // 设置从底部弹出
            UIViewController.cl_topViewController()?.present(imagePickerController, animated: true, completion: nil)
        }
    }
    
    func getAssertImage(completionHandler: @escaping ([UIImage]) -> Void) {
        let images = selectedAssets.map { photoAsset in
            photoAsset.image
        }
        completionHandler(images)
    }
}

extension VLPhotoView: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    // 处理选中的图片
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        picker.dismiss(animated: true, completion: nil)
        if let image = info[.editedImage] as? UIImage {
            // 使用编辑后的图片
            let photoAsset = PhotoAsset(image: image)
            selectedAssets.append(photoAsset)
        } else if let image = info[.originalImage] as? UIImage {
            // 如果没有编辑，使用原始图片
            let photoAsset = PhotoAsset(image: image)
            selectedAssets.append(photoAsset)
        }
        
        collectionView.reloadData()
        updateCollectionViewHeight()
    }
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
}

@available(iOS 14.0, *)
extension VLPhotoView: PHPickerViewControllerDelegate {
    @available(iOS 14.0, *)
    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true, completion: nil)

        for result in results {
            if result.itemProvider.canLoadObject(ofClass: UIImage.self) {
                result.itemProvider.loadObject(ofClass: UIImage.self) { [weak self] (image, error) in
                    DispatchQueue.main.async {
                        if let image = image as? UIImage {
                            let photoAsset = PhotoAsset(image: image)
                            self?.selectedAssets.append(photoAsset)
                            self?.collectionView.reloadData()
                            self?.updateCollectionViewHeight()
                        }
                    }
                }
            }
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
        
        // 浏览图片
        PhotoPreviewViewController.show(selectedAssets, pageIndex: indexPath.item) { [weak self] index, photoAssert, previewVC in
            guard let self = self else { return }
            
            self.previewDidDeleteAsset(index: index)
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
    
    private func previewDidDeleteAsset(index: Int) {
        let isFull = selectedAssets.count == maxCount
        selectedAssets.remove(at: index)
        if isFull {
            collectionView.reloadData()
        }else {
            collectionView.deleteItems(at: [IndexPath.init(item: index, section: 0)])
        }
        updateCollectionViewHeight()
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
            let isFull = selectedAssets.count == maxCount
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

class VLPhotoViewAddCell: UICollectionViewCell {
    override init(frame: CGRect) {
        super.init(frame: frame)
        initView()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func initView() {
        contentView.layer.cornerRadius = 8
        contentView.layer.masksToBounds = true
        let placeholderImage = UIImage(named: "hx_picker_add_img")
        let imageView = UIImageView(image: placeholderImage)
        imageView.contentMode = .center
        imageView.frame = contentView.bounds
        contentView.addSubview(imageView)
    }
}

@objc
protocol ResultViewCellDelegate: AnyObject {
    @objc optional func cell(didDeleteButton cell: VLPhotoViewResultCell)
}

class VLPhotoViewResultCell: UICollectionViewCell {
    weak var resultDelegate: ResultViewCellDelegate?
    var photoAsset: PhotoAsset! {
        didSet {
            imageView.image = photoAsset.image
        }
    }
    
    private lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.layer.cornerRadius = 8
        imageView.layer.masksToBounds = true
        return imageView
    }()
    
    private lazy var deleteButton: UIButton = {
        let deleteButton = UIButton(type: .custom)
        deleteButton.setImage(UIImage(named: "hx_compose_delete"), for: .normal)
        deleteButton.addTarget(self, action: #selector(didDeleteButtonClick), for: .touchUpInside)
        return deleteButton
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        initView()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func initView() {
        contentView.addSubview(imageView)
        contentView.addSubview(deleteButton)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        imageView.frame = contentView.bounds
        deleteButton.frame = CGRect(x: contentView.bounds.width - 20, y: 0, width: 20, height: 20)
    }
    
    @objc func didDeleteButtonClick() {
        resultDelegate?.cell?(didDeleteButton: self)
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



