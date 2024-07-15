//
//  VLLoginGuideViewController.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/13.
//

import UIKit

class VLLoginGuideViewController: VLBaseViewController {
    private lazy var flowLayout: UICollectionViewFlowLayout = {
        let flowLayout = UICollectionViewFlowLayout()
        flowLayout.scrollDirection = .horizontal
        flowLayout.minimumLineSpacing = 0
        flowLayout.minimumInteritemSpacing = 0
        return flowLayout
    }()
    private lazy var collectionView: UICollectionView = {
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.isPagingEnabled = true
        collectionView.backgroundColor = .clear
        collectionView.showsHorizontalScrollIndicator = false
        collectionView.register(VLLoginGuideViewCell.self, forCellWithReuseIdentifier: "guideCell")
        return collectionView
    }()
    private lazy var pageControl: UIPageControl = {
        let control = UIPageControl()
        control.numberOfPages = dataArray.count
        control.currentPageIndicatorTintColor = UIColor(hex: "#979CBB", alpha: 1.0)
        control.pageIndicatorTintColor = UIColor(hex: "#E2E6F1", alpha: 1.0)
        control.translatesAutoresizingMaskIntoConstraints = false
        control.isEnabled = false
        return control
    }()
    private lazy var loginButton: UIButton = {
        let button = UIButton()
        button.setTitle("\(NSLocalizedString("app_mobile_number", comment: ""))\(NSLocalizedString("app_login", comment: ""))", for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 16)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.setBackgroundImage(UIImage(named: "login_button_bg"), for: .normal)
        button.setContentHuggingPriority(.defaultHigh, for: .vertical)
        button.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        button.addTarget(self, action: #selector(onClickLoginButton), for: .touchUpInside)
        button.alpha = 0.5
        return button
    }()
    private lazy var agreeContainerView: UIView = {
        let view = UIView()
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    private lazy var agreeButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("i_agree", comment: ""), for: .normal)
        button.setTitleColor(UIColor(hex: "#979CBB", alpha: 1.0), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 13)
        button.setImage(UIImage(named: "icon_privacy_noselect_button"), for: .normal)
        button.setImage(UIImage(named: "icon_privacy_select_button"), for: .selected)
        button.imageEdgeInsets(UIEdgeInsets(top: 0, left: -15, bottom: 0, right: 0))
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(onClickAgreeButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var textView: AttributedTextView = {
        let userProtocol = "《\(NSLocalizedString("app_user_agreement", comment: ""))》"
        let privacyPolicy = "《\(NSLocalizedString("app_privacy_agreement", comment: ""))》"
        let string = NSLocalizedString("app_login_guide_privacy_tips", comment: "")
        let userIndexs = string.indices(of: userProtocol)
        let range1 = NSRange(location: userIndexs.first ?? 0, length: userProtocol.count)
        let privacyIndexs = string.indices(of: privacyPolicy)
        let range2 = NSRange(location: privacyIndexs.first ?? 0, length: privacyPolicy.count)
        let textView = AttributedTextView(frame: .zero,
                                          text: string,
                                          font: .systemFont(ofSize: 13),
                                          attributedStringS: [userProtocol, privacyPolicy],
                                          ranges: [range1, range2],
                                          textColor: UIColor(hex: "#979CBB", alpha: 1.0)!,
                                          attributeTextColor: UIColor(hex: "#2E6CF6", alpha: 1.0)!)
        textView.textAlignment = .center
        textView.delegate = self
        textView.translatesAutoresizingMaskIntoConstraints = false
        return textView
    }()
    private var timer: Timer?
    
    private let dataArray: [VLLoginGudieModel] = VLLoginGudieModel.createGudieData()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
        setupTimer()
    }
    
    deinit {
        timer?.invalidate()
        timer = nil
    }
    
    private func setupUI() {
        view.addSubview(collectionView)
        view.addSubview(pageControl)
        view.addSubview(loginButton)
        view.addSubview(agreeContainerView)
        agreeContainerView.addSubview(agreeButton)
        agreeContainerView.addSubview(textView)
        
        agreeContainerView.centerXAnchor.constraint(equalTo: view.centerXAnchor, constant: 5).isActive = true
        agreeContainerView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -15.fit).isActive = true
        agreeContainerView.heightAnchor.constraint(equalToConstant: 30.fit).isActive = true
        
        agreeButton.leadingAnchor.constraint(equalTo: agreeContainerView.leadingAnchor).isActive = true
        agreeButton.centerYAnchor.constraint(equalTo: agreeContainerView.centerYAnchor).isActive = true
        textView.leadingAnchor.constraint(equalTo: agreeButton.trailingAnchor, constant: -10).isActive = true
        textView.trailingAnchor.constraint(equalTo: agreeContainerView.trailingAnchor).isActive = true
        textView.topAnchor.constraint(equalTo: agreeContainerView.topAnchor).isActive = true
        textView.bottomAnchor.constraint(equalTo: agreeContainerView.bottomAnchor).isActive = true
        textView.widthAnchor.constraint(equalToConstant: 190.fit).isActive = true
        
        loginButton.bottomAnchor.constraint(equalTo: agreeContainerView.topAnchor, constant: -20.fit).isActive = true
        loginButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20.fit).isActive = true
        loginButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20.fit).isActive = true
        loginButton.heightAnchor.constraint(equalToConstant: 48.fit).isActive = true
        
        pageControl.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        pageControl.bottomAnchor.constraint(equalTo: loginButton.topAnchor, constant: -35.fit).isActive = true
        
        collectionView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        collectionView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        collectionView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        collectionView.bottomAnchor.constraint(equalTo: pageControl.topAnchor, constant: -30.fit).isActive = true
    }
    
    private func setupTimer() {
        timer = Timer(timeInterval: 3, repeats: true, block: { [weak self] _ in
            guard let self = self else { return }
            var index = self.pageControl.currentPage + 1
            index = index >= self.dataArray.count ? 0 : index
            self.collectionView.scrollToItem(at: IndexPath(item: index, section: 0), at: .centeredHorizontally, animated: true)
        })
        RunLoop.main.add(timer!, forMode: .default)
    }
      
    @objc
    private func onClickAgreeButton(sender: UIButton) {
        sender.isSelected = !sender.isSelected
        loginButton.alpha = sender.isSelected ? 1.0 : 0.5
    }
    
    @objc
    private func onClickLoginButton() {
        if agreeButton.isSelected == false {
            ToastView.show(text: NSLocalizedString("app_login_guide_agree_tips", comment: ""), postion: .bottom, duration: 3.0)
            return
        }
        let registerVC = VLRegisterViewController()
        navigationController?.pushViewController(registerVC, animated: true)
    }
    
    private func toWebVC(url: String) {
        let webVC = VLCommonWebViewController();
        webVC.urlString = url
        navigationController?.pushViewController(webVC, animated: true)
    }
}
extension VLLoginGuideViewController: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        dataArray.count
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "guideCell", for: indexPath) as! VLLoginGuideViewCell
        cell.setupModel(model: dataArray[indexPath.item])
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        collectionView.size
    }
    
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let x = scrollView.contentOffset.x / scrollView.frame.width
        pageControl.currentPage = Int(x)
    }
    
    func scrollViewWillBeginDragging(_ scrollView: UIScrollView) {
        timer?.invalidate()
    }
    
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        setupTimer()
    }
}
extension VLLoginGuideViewController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange) -> Bool {
        if URL.absoluteString == "0" {
            toWebVC(url: VLURLConfig.kURLPathH5UserAgreement)
        } else {
            toWebVC(url: VLURLConfig.kURLPathH5Privacy)
        }
        return true
    }
}
