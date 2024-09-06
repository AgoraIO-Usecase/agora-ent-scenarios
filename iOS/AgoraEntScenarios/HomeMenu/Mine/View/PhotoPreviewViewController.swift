//
//  PhotoPreviewViewController.swift
//  AgoraEntScenarios
//
//  Created by qinhui on 2024/8/20.
//

import UIKit

class PhotoAsset: Equatable {
    var image: UIImage
    
    init(image: UIImage) {
        self.image = image
    }
    
    static func == (lhs: PhotoAsset, rhs: PhotoAsset) -> Bool {
        return lhs.image == rhs.image
    }
}

class PhotoPreviewViewController: UIViewController {
    private var previewAssets: [PhotoAsset]
    private var pageIndex: Int
    private var deleteAssetHandler: (Int, PhotoAsset, PhotoPreviewViewController) -> Void
    
    private var pageViewController: UIPageViewController!
    
    init(previewAssets: [PhotoAsset], pageIndex: Int = 0, deleteAssetHandler: @escaping (Int, PhotoAsset, PhotoPreviewViewController) -> Void) {
        self.previewAssets = previewAssets
        self.pageIndex = pageIndex
        self.deleteAssetHandler = deleteAssetHandler
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
        
        setupPageViewController()
        setupTopBar()
    }
    
    private func setupPageViewController() {
        let pageVC = UIPageViewController(transitionStyle: .scroll, navigationOrientation: .horizontal, options: nil)
        pageVC.dataSource = self
        pageVC.delegate = self
        
        let initialVC = PreviewImageViewController(asset: previewAssets[pageIndex])
        pageVC.setViewControllers([initialVC], direction: .forward, animated: true, completion: nil)
        
        pageViewController = pageVC
        addChild(pageViewController)
        view.addSubview(pageViewController.view)
        pageViewController.didMove(toParent: self)
        pageViewController.view.frame = view.bounds
    }
    
    private func setupTopBar() {
        let topBar = UIView()
        topBar.translatesAutoresizingMaskIntoConstraints = false
        topBar.backgroundColor = .clear
        
        let gradientLayer = CAGradientLayer()
        gradientLayer.colors = [UIColor.black.withAlphaComponent(0.5).cgColor, UIColor.clear.cgColor]
        gradientLayer.startPoint = CGPoint(x: 0, y: 0)
        gradientLayer.endPoint = CGPoint(x: 0, y: 1)
        gradientLayer.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: 88) 
        
        topBar.layer.insertSublayer(gradientLayer, at: 0)
        view.addSubview(topBar)
        
        NSLayoutConstraint.activate([
            topBar.topAnchor.constraint(equalTo: view.topAnchor),
            topBar.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            topBar.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            topBar.heightAnchor.constraint(equalToConstant: 88)
        ])
        
        let closeButton = UIButton(type: .system)
        closeButton.setTitle(NSLocalizedString("feedback_preview_photo_close", comment: ""), for: .normal)
        closeButton.setTitleColor(.white, for: .normal)
        closeButton.addTarget(self, action: #selector(closeButtonTapped), for: .touchUpInside)
        topBar.addSubview(closeButton)
        closeButton.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            closeButton.topAnchor.constraint(equalTo: topBar.topAnchor, constant: 44),
            closeButton.leadingAnchor.constraint(equalTo: topBar.leadingAnchor, constant: 16)
        ])
        
        let deleteButton = UIButton(type: .system)
        deleteButton.setTitle(NSLocalizedString("feedback_delete_button", comment: ""), for: .normal)
        deleteButton.setTitleColor(.white, for: .normal)
        deleteButton.addTarget(self, action: #selector(deleteButtonTapped), for: .touchUpInside)
        topBar.addSubview(deleteButton)
        deleteButton.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            deleteButton.topAnchor.constraint(equalTo: topBar.topAnchor, constant: 44),
            deleteButton.trailingAnchor.constraint(equalTo: topBar.trailingAnchor, constant: -16)
        ])
    }
    
    
    @objc private func closeButtonTapped() {
        dismiss(animated: true, completion: nil)
    }
    
    @objc private func deleteButtonTapped() {
        let alert = UIAlertController(title: NSLocalizedString("feedback_delete_image_title", comment: ""), message: nil, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: NSLocalizedString("feedback_delete_image_confirm", comment: ""), style: .destructive, handler: { [weak self] _ in
            self?.deleteCurrentPreviewPhotoAsset()
        }))
        alert.addAction(UIAlertAction(title: NSLocalizedString("feedback_delete_image_cancel", comment: ""), style: .cancel, handler: nil))
        present(alert, animated: true, completion: nil)
    }
    
    func deleteCurrentPreviewPhotoAsset() {
        deleteAssetHandler(pageIndex, previewAssets[pageIndex], self)
        deletePreviewPhotoAsset(index: pageIndex)
    }
    
    func deletePreviewPhotoAsset(index: Int) {
        guard index >= 0 && index < previewAssets.count else { return }
        previewAssets.remove(at: index)
        
        if previewAssets.isEmpty {
            dismiss(animated: true, completion: nil)
        } else {
            let nextIndex = min(index, previewAssets.count - 1)
            pageIndex = nextIndex
            let nextVC = PreviewImageViewController(asset: previewAssets[nextIndex])
            pageViewController.setViewControllers([nextVC], direction: .forward, animated: true, completion: nil)
        }
    }
    
    class func show(_ previewAssets: [PhotoAsset], pageIndex: Int = 0, deleteAssetHandler: @escaping (Int, PhotoAsset, PhotoPreviewViewController) -> Void) -> Void {
        let previewVC = PhotoPreviewViewController(previewAssets: previewAssets, pageIndex: pageIndex, deleteAssetHandler: deleteAssetHandler)
        previewVC.modalPresentationStyle = .fullScreen
        if let fromVC = PhotoPreviewViewController.topViewController {
            fromVC.present(previewVC, animated: true, completion: nil)
        }
    }
}

extension PhotoPreviewViewController: UIPageViewControllerDataSource, UIPageViewControllerDelegate {
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        guard let currentVC = viewController as? PreviewImageViewController else { return nil }
        guard let currentIndex = previewAssets.firstIndex(of: currentVC.asset) else { return nil }
        let previousIndex = currentIndex - 1
        guard previousIndex >= 0 else { return nil }
        return PreviewImageViewController(asset: previewAssets[previousIndex])
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        guard let currentVC = viewController as? PreviewImageViewController else { return nil }
        guard let currentIndex = previewAssets.firstIndex(of: currentVC.asset) else { return nil }
        let nextIndex = currentIndex + 1
        guard nextIndex < previewAssets.count else { return nil }
        return PreviewImageViewController(asset: previewAssets[nextIndex])
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if completed, let currentVC = pageViewController.viewControllers?.first as? PreviewImageViewController,
           let currentIndex = previewAssets.firstIndex(of: currentVC.asset) {
            pageIndex = currentIndex
        }
    }
}

class PreviewImageViewController: UIViewController, UIScrollViewDelegate {
    var asset: PhotoAsset
    private var imageView: UIImageView!
    
    init(asset: PhotoAsset) {
        self.asset = asset
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
        
        let scrollView = UIScrollView()
        scrollView.delegate = self
        scrollView.minimumZoomScale = 1.0
        scrollView.maximumZoomScale = 6.0
        view.addSubview(scrollView)
        scrollView.frame = view.bounds
        
        imageView = UIImageView(image: asset.image)
        imageView.contentMode = .scaleAspectFit
        imageView.isUserInteractionEnabled = true
        scrollView.addSubview(imageView)
        imageView.frame = scrollView.bounds
        
        let doubleTapGesture = UITapGestureRecognizer(target: self, action: #selector(handleDoubleTap(_:)))
        doubleTapGesture.numberOfTapsRequired = 2
        imageView.addGestureRecognizer(doubleTapGesture)
    }
    
    @objc private func handleDoubleTap(_ gesture: UITapGestureRecognizer) {
        let scrollView = imageView.superview as! UIScrollView
        if scrollView.zoomScale == 1 {
            scrollView.zoom(to: zoomRectForScale(scale: scrollView.maximumZoomScale, center: gesture.location(in: gesture.view)), animated: true)
        } else {
            scrollView.setZoomScale(1, animated: true)
        }
    }
    
    func viewForZooming(in scrollView: UIScrollView) -> UIView? {
        return imageView
    }
    
    private func zoomRectForScale(scale: CGFloat, center: CGPoint) -> CGRect {
        let scrollView = imageView.superview as! UIScrollView
        var zoomRect = CGRect.zero
        zoomRect.size.height = scrollView.frame.size.height / scale
        zoomRect.size.width = scrollView.frame.size.width / scale
        let newCenter = scrollView.convert(center, from: imageView)
        zoomRect.origin.x = newCenter.x - (zoomRect.size.width / 2.0)
        zoomRect.origin.y = newCenter.y - (zoomRect.size.height / 2.0)
        return zoomRect
    }
}

extension PhotoPreviewViewController {
    class var topViewController: UIViewController? {
        let window = UIApplication.kWindow
        if var topViewController = window?.rootViewController {
            while true {
                if let controller = topViewController.presentedViewController {
                    topViewController = controller
                }else if let navController = topViewController as? UINavigationController,
                         let controller = navController.topViewController {
                    topViewController = controller
                }else if let tabbarController = topViewController as? UITabBarController,
                         let controller = tabbarController.selectedViewController {
                    topViewController = controller
                }else {
                    break
                }
            }
            return topViewController
        }
        return nil
    }
}

