//
//  VLFeedbackViewController.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/18.
//

import UIKit
//import HXPhotoPicker

@objc
class VLFeedbackViewController: VLBaseViewController {
    private lazy var selectTagView: VLSelectTagView = {
        let view = VLSelectTagView()
        view.translatesAutoresizingMaskIntoConstraints = false
        view.content_titleFont = .systemFont(ofSize: 14)
        view.content_norTitleColor = UIColor(hex: "#303553", alpha: 1.0)!
        view.content_selTitleColor = UIColor(hex: "#FFFFFF", alpha: 1.0)!
        view.content_backNorColor = UIColor(hex: "#E9ECF5", alpha: 1.0)!
        view.content_backSelColor = UIColor(hex: "#303553", alpha: 1.0)!
        view.isDefaultChoice = true
        view.defaultSelIndex = 4
        view.titleLabHeight = 0
        view.isSingle = false
        view.delegate = self
        return view
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("feedback_desc", comment: "")
        label.textColor = UIColor(hex: "#303553", alpha: 1.0)
        label.font = .systemFont(ofSize: 14)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var textCountLabel: UILabel = {
        let label = UILabel()
        label.text = "0/200"
        label.textColor = UIColor(hex: "#979CBB", alpha: 1.0)
        label.font = .systemFont(ofSize: 14)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var containerView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#FFFFFF", alpha: 1.0)
        view.cornerRadius(12)
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    private lazy var textView: UITextView = {
        let textView = UITextView()
        textView.font = .systemFont(ofSize: 14)
        textView.setPlaceholder(text: NSLocalizedString("feedback_textView_placeholder", comment: ""))
        textView.delegate = self
        textView.translatesAutoresizingMaskIntoConstraints = false
        return textView
    }()
    private lazy var uploadTipsButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("feedback_upload_log_tips", comment: ""), for: .normal)
        button.setTitleColor(UIColor(hex: "#979CBB", alpha: 1.0), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 15)
        button.setImage(UIImage(named: "icon_privacy_noselect_button"), for: .normal)
        button.setImage(UIImage(named: "icon_privacy_select_button"), for: .selected)
        button.spacingBetweenImageAndTitle = 5
        button.addTarget(self, action: #selector(onClickUploadTipsButton(sender:)), for: .touchUpInside)
        button.translatesAutoresizingMaskIntoConstraints = false
        return button
    }()
    private lazy var submitButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("feedback_submit", comment: ""), for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        button.backgroundColor = UIColor(hex: "#2E6CF6", alpha: 1.0)
        button.cornerRadius(12)
        button.addTarget(self, action: #selector(onClickSubmitButton), for: .touchUpInside)
        button.translatesAutoresizingMaskIntoConstraints = false
        return button
    }()
    private lazy var photoView: VLPhotoView = {
        let view = VLPhotoView()
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    private var selectedTags: [String]?
    private var imageUrls: [String]?
    private var logUrl: String?
    private let maxLength: Int = 200
    
    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
    }
    
    private func setupUI() {
        hiddenBackgroundImage()
        setNaviTitleName(NSLocalizedString("app_submit_feedback", comment: ""))
        setBackBtn()
        
        view.addSubview(selectTagView)
        selectTagView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 11).isActive = true
        selectTagView.topAnchor.constraint(equalTo: view.topAnchor, constant: Screen.kNavHeight + 20).isActive = true
        selectTagView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10).isActive = true
        selectTagView.heightAnchor.constraint(equalToConstant: 84).isActive = true
        
        selectTagView.setDataSource(contetnArr: [[NSLocalizedString("feedback_tags_flashback", comment: ""),
                                                  NSLocalizedString("feedback_tags_audioStuck", comment: ""),
                                                  NSLocalizedString("feedback_tags_videoStuck", comment: ""),
                                                  NSLocalizedString("feedback_tags_abnormal_communication", comment: ""),
                                                  NSLocalizedString("feedback_tags_functional_recommendations", comment: ""),
                                                  NSLocalizedString("feedback_tags_other", comment: "")]], titleArr: [""])
        
        view.addSubview(descLabel)
        descLabel.leadingAnchor.constraint(equalTo: selectTagView.leadingAnchor, constant: 22).isActive = true
        descLabel.topAnchor.constraint(equalTo: selectTagView.bottomAnchor, constant: 25).isActive = true
        
        view.addSubview(textCountLabel)
        textCountLabel.centerYAnchor.constraint(equalTo: descLabel.centerYAnchor).isActive = true
        textCountLabel.trailingAnchor.constraint(equalTo: selectTagView.trailingAnchor, constant: -10).isActive = true
        
        view.addSubview(containerView)
        containerView.leadingAnchor.constraint(equalTo: selectTagView.leadingAnchor, constant: 10).isActive = true
        containerView.topAnchor.constraint(equalTo: descLabel.bottomAnchor, constant: 5).isActive = true
        containerView.trailingAnchor.constraint(equalTo: selectTagView.trailingAnchor, constant: -10).isActive = true
        containerView.heightAnchor.constraint(equalToConstant: 240).isActive = true
        
        containerView.addSubview(textView)
        textView.leadingAnchor.constraint(equalTo: containerView.leadingAnchor, constant: 12).isActive = true
        textView.topAnchor.constraint(equalTo: containerView.topAnchor, constant: 6).isActive = true
        textView.trailingAnchor.constraint(equalTo: containerView.trailingAnchor, constant: -12).isActive = true
        
        containerView.addSubview(photoView)
        photoView.topAnchor.constraint(equalTo: textView.bottomAnchor, constant: 10).isActive = true
        photoView.leadingAnchor.constraint(equalTo: textView.leadingAnchor).isActive = true
        photoView.bottomAnchor.constraint(equalTo: containerView.bottomAnchor, constant: -16).isActive = true
        photoView.trailingAnchor.constraint(equalTo: textView.trailingAnchor).isActive = true
        
        view.addSubview(uploadTipsButton)
        uploadTipsButton.leadingAnchor.constraint(equalTo: containerView.leadingAnchor).isActive = true
        uploadTipsButton.topAnchor.constraint(equalTo: containerView.bottomAnchor, constant: 20).isActive = true
        
        view.addSubview(submitButton)
        submitButton.leadingAnchor.constraint(equalTo: uploadTipsButton.leadingAnchor).isActive = true
        submitButton.topAnchor.constraint(equalTo: uploadTipsButton.bottomAnchor, constant: 20).isActive = true
        submitButton.trailingAnchor.constraint(equalTo: containerView.trailingAnchor).isActive = true
        submitButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        view.endEditing(true)
    }
    
    @objc
    private func onClickUploadTipsButton(sender: UIButton) {
        sender.isSelected = !sender.isSelected
    }
    
    @objc
    private func onClickSubmitButton() {
        selectTagView.comfirm()
        if selectedTags == nil {
            ToastView.show(text: NSLocalizedString("feedback_tags_tips", comment: ""))
            return
        }
        let group = DispatchGroup()
        group.enter()
        if !photoView.selectedAssets.isEmpty {
            photoView.getAssetUrl { [weak self] urls in
                let images = urls.compactMap({ UIImage(contentsOfFile: $0.path ) })
                self?.uploadImagesHandler(images: images, completion: { urls in
                    self?.imageUrls = urls
                    group.leave()
                })
            }
        } else {
            group.leave()
        }
        group.enter()
        if uploadTipsButton.isSelected {
            uploadRTCLogHandler(completion: { url in
                self.logUrl = url
                group.leave()
            })
        } else {
            group.leave()
        }
        group.notify(queue: .main) {
            self.submitFeedbackData(imageUrls: self.imageUrls, logUrl: self.logUrl)
        }
    }
    
    private func submitFeedbackData(imageUrls: [String]?, logUrl: String?) {
        var images: [String: String] = [:]
        imageUrls?.enumerated().forEach({
            images["\($0.offset + 1)"] = $0.element
        })
        let params = ["screenshotURLs": images,
                      "tags": selectedTags ?? [],
                      "description": textView.text ?? "",
                      "logURL": logUrl ?? ""] as [String : Any]
        VLAPIRequest.postURL(VLURLConfig.kURLPathFeedback, parameter: params, showHUD: true) { response in
            if response.code == 0 {
                let resultVC = VLFeedbackResultViewController()
                self.navigationController?.pushViewController(resultVC, animated: true)
            } else {
                ToastView.show(text: response.message)
            }
        } failure: { error, _ in
            ToastView.show(text: error?.localizedDescription ?? "")
        }
    }
    
    private func uploadRTCLogHandler(completion: @escaping (String?) -> Void) {
        let libtraryPath = NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true)[0] as String
        let logPath = libtraryPath + "/Caches/agorasdk.log"
        VLAPIRequest.uploadFileURL(VLURLConfig.kURLPathUploadLog, showHUD: true, appendKey: "file", filePath: logPath) { response in
            guard response.code == 0 else { ToastView.show(text: response.message); return }
            let model = VLUploadImageResModel.yy_model(withJSON: response.data)
            completion(model?.url)
        } failure: { error, _ in
            print(error?.localizedDescription ?? "")
            completion(nil)
        }
    }
    private func uploadImagesHandler(images: [UIImage], completion: @escaping ([String]) -> Void) {
        var urls: [String] = []
        let group = DispatchGroup()
        let semaphore = DispatchSemaphore(value: 1)
        DispatchQueue.global().async {
            for item in images {
                group.enter()
                semaphore.wait()
                Task {
                    guard let url = try? await self.uploadImages(image: item) else { return }
                    urls.append(url)
                    group.leave()
                    semaphore.signal()
                }
            }
            group.notify(queue: .main) {
                if urls.count != images.count {
                    ToastView.show(text: NSLocalizedString("feedback_upload_image_fail", comment: ""))
                    return
                }
                completion(urls)
            }
        }
    }
    
    private func uploadImages(image: UIImage) async throws -> String? {
        try await withUnsafeThrowingContinuation { continuation in
            VLAPIRequest.uploadImageURL(VLURLConfig.kURLPathUploadImage, showHUD: true, appendKey: "file", images: [image]) { response in
                guard response.code == 0 else { ToastView.show(text: response.message); return }
                let model = VLUploadImageResModel.yy_model(withJSON: response.data)
                continuation.resume(returning: model?.url)
            } failure: { error, _ in
                guard let error = error else { return }
                print(error.localizedDescription)
                continuation.resume(throwing: error)
            }
        }
    }
}

extension VLFeedbackViewController: VLSelectTagViewDelegate {
    func currentSelValueWithDelegate(valueStr: String, index: Int, groupId: Int) {
        view.endEditing(true)
    }
    func confimrReturnAllSelValueWithDelegate(selArr: [Any], groupArr: [Any]) {
        let results = selArr.first is String ? ["\((selArr.first as? String) ?? "")"] : (selArr.first as? [String]) ?? []
        selectedTags = results.isEmpty ? nil : results
        view.endEditing(true)
    }
}

extension VLFeedbackViewController: UITextViewDelegate {
    // 限制输入
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        let currentText = textView.text ?? ""
        let updatedText = currentText.replacingCharacters(in: Range(range, in: currentText)!, with: text)
        return updatedText.count <= maxLength
    }
    // 触发检查
    func textViewDidEndEditing(_ textView: UITextView) {
        let currentText = textView.text ?? ""
        // 获取中文和英文字符数
        let totalChars = currentText.count
        let chineseChars = currentText.countOfCharacters(for: .chinese)
        let englishChars = totalChars - chineseChars
        
        if chineseChars > maxLength || englishChars > maxLength {
            let index = currentText.index(currentText.startIndex, offsetBy: maxLength)
            textView.text = String(currentText[..<index])
        }
    }
    func textViewDidChange(_ textView: UITextView) {
        textCountLabel.text = "\(textView.text.count > maxLength ? maxLength : textView.text.count)/\(maxLength)"
    }
}
