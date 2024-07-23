//
//  ActionSheetManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/2/7.
//

import UIKit

enum ActionSheetCellType {
    case text
    case sw
    case slider
    case segment
    case tips
    case custom
    case tf
}

public enum AINS_CUSTOM_SET_TYPE {
    case ns
    case aed
    case aspt
}

class ActionSheetModel: NSObject {
    var title: String?
    var titleColor: UIColor?
    var decs: String?
    var isOn: Bool = false
    var iconName: String?
    var value: Double = 0
    var items: [String]?
    var segmentSelectedIndex: Int = 0
    var isShowArrow: Bool = false
    var cellType: ActionSheetCellType = .text
    var isEnable: Bool = true
    var cutsomView: UIView?
    var customViewHeight: CGFloat = 0
    var isHiddenLine: Bool = true
    var accessibilityIdentifier: String?
    
    var cellIdentifier: String {
        switch cellType {
        case .text: return ActionSheetTextCell.description()
        case .sw: return ActionSheetSwitchCell.description()
        case .slider: return ActionSheetSliderCell.description()
        case .segment: return ActionSheetSegmentCell.description()
        case .tips: return ActionSheetTipsCell.description()
        case .custom: return ActionSheetCustomCell.description()
        case .tf: return ActionSheetTFCell.description()
        }
    }
}

struct ActionSheetSectionModel {
    var title: String?
    var decs: String?
    var iconName: String?
}

class ActionSheetManager: UIView {
    @objc var didCellItemClosure: ((IndexPath) -> Void)?
    @objc var didSliderValueChangeClosure: ((IndexPath, Double) -> Void)?
    @objc var didSwitchValueChangeClosure: ((IndexPath, Bool) -> Void)?
    @objc var didSegmentValueChangeClosure: ((IndexPath, String, Int) -> Void)?
    var didCustomModeSetClosure:((AINS_CUSTOM_SET_TYPE) -> Void)?
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView(frame: .zero, style: .grouped)
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = 53
        tableView.estimatedSectionFooterHeight = 0
        tableView.estimatedSectionHeaderHeight = 0
        tableView.estimatedRowHeight = 0
        tableView.backgroundColor = .clear
        tableView.showsVerticalScrollIndicator = false
        tableView.separatorStyle = .none
        tableView.registerCell(ActionSheetTextCell.self,
                               forCellReuseIdentifier: ActionSheetTextCell.description())
        tableView.registerCell(ActionSheetSwitchCell.self,
                               forCellReuseIdentifier: ActionSheetSwitchCell.description())
        tableView.registerCell(ActionSheetSliderCell.self,
                               forCellReuseIdentifier: ActionSheetSliderCell.description())
        tableView.register(ActionSheetSegmentCell.self,
                           forCellReuseIdentifier: ActionSheetSegmentCell.description())
        tableView.register(ActionSheetTipsCell.self,
                           forCellReuseIdentifier: ActionSheetTipsCell.description())
        tableView.register(ActionSheetCustomCell.self,
                           forCellReuseIdentifier: ActionSheetCustomCell.description())
        tableView.register(ActionSheetTFCell.self, forCellReuseIdentifier:ActionSheetTFCell.description())
        return tableView
    }()
    private lazy var backButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "back", bundleName: "SpatialAudioResource"), for: .normal)
        button.isHidden = true
        button.addTargetFor(self, action: #selector(clickBackButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var lineView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "tchead", bundleName: "SpatialAudioResource"))
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#040925", alpha: 1.0)
        label.font = UIFont.boldSystemFont(ofSize: 16)
        return label
    }()
    
    private var rows: [Int] = []
    private var dataArray: [ActionSheetModel] = []
    private var sectionArray = [[ActionSheetModel]]()
    private var sectionHeaderArray: [ActionSheetSectionModel] = []
    private var tableViewHCons: NSLayoutConstraint?
    private var isVoice: Bool = false
    
    var backButtonAccessibilityIdentifier: String? {
        didSet{
            backButton.accessibilityIdentifier = backButtonAccessibilityIdentifier
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .white
        translatesAutoresizingMaskIntoConstraints = false
        widthAnchor.constraint(equalToConstant: Screen.width).isActive = true
        layer.cornerRadius = 10
        layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        layer.masksToBounds = true
        
        addSubview(lineView)
        lineView.addSubview(titleLabel)
        addSubview(backButton)
        
        backButton.translatesAutoresizingMaskIntoConstraints = false
        backButton.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        backButton.centerYAnchor.constraint(equalTo: titleLabel.centerYAnchor).isActive = true
        
        lineView.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        
        lineView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        lineView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 59).isActive = true
        lineView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        
        titleLabel.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        titleLabel.bottomAnchor.constraint(equalTo: lineView.bottomAnchor, constant: -10).isActive = true
        
        addSubview(tableView)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        tableView.topAnchor.constraint(equalTo: lineView.bottomAnchor).isActive = true
        tableView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        tableViewHCons = tableView.heightAnchor.constraint(equalToConstant: 300)
        tableViewHCons?.isActive = true
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func title(title: String) -> ActionSheetManager {
        titleLabel.text = title
        return self
    }
    @objc func sectionHeader(iconName: String? = nil,
                       title: String?,
                       desc: String?) -> ActionSheetManager {
        if rows.isEmpty && !dataArray.isEmpty {
            rows.append(dataArray.count)
        } else if !dataArray.isEmpty {
            let preCount = rows.last ?? 0
            let count = dataArray.count - preCount
            rows.append(count)
        }
        let model = ActionSheetSectionModel(title: title, decs: desc, iconName: iconName)
        sectionHeaderArray.append(model)
        return self
    }
    func textCell(iconName: String? = nil,
                  title: String?,
                  desc: String?,
                  isShowArrow: Bool = false,
                  isEnable: Bool = true) -> ActionSheetManager {
        let model = ActionSheetModel()
        model.title = title
        model.decs = desc
        model.iconName = iconName
        model.isShowArrow = isShowArrow
        model.cellType = .text
        dataArray.append(model)
        insertEmptyHeaderData()
        return self
    }
    
    @objc func tipsCell(iconName: String? = nil,
                        title: String?,
                        titleColor: UIColor? = nil) -> ActionSheetManager {
        let model = ActionSheetModel()
        model.title = title
        model.titleColor = titleColor
        model.iconName = iconName
        model.cellType = .tips
        dataArray.append(model)
        insertEmptyHeaderData()
        return self
    }
    @objc func switchCell(iconName: String? = nil,
                    title: String?,
                    isOn: Bool = false,
                    isEnabel: Bool = true,
                    accessibilityIdentifier: String? = nil) -> ActionSheetManager {
        let model = ActionSheetModel()
        model.title = title
        model.isOn = isOn
        model.iconName = iconName
        model.cellType = .sw
        model.isEnable = isEnabel
        model.accessibilityIdentifier = accessibilityIdentifier
        dataArray.append(model)
        insertEmptyHeaderData()
        return self
    }
    
    @objc func tfCell(
        title: String,
        value: Double,
        desc: String) -> ActionSheetManager {
        let model = ActionSheetModel()
        model.title = title
        model.decs = desc
        model.value = value
        model.cellType = .tf
        dataArray.append(model)
        insertEmptyHeaderData()
        return self
    }
    
    @objc func sliderCell(iconName: String? = nil,
                    title: String?,
                    value: Double,
                    isEnable: Bool = true,
                    accessibilityIdentifier: String? = nil) -> ActionSheetManager {
        let model = ActionSheetModel()
        model.title = title
        model.value = value
        model.iconName = iconName
        model.cellType = .slider
        model.isEnable = isEnable
        model.accessibilityIdentifier = accessibilityIdentifier
        dataArray.append(model)
        insertEmptyHeaderData()
        return self
    }
    func segmentCell(iconName: String? = nil,
                     title: String?,
                     items: [String]?,
                     selectedIndex: Int = 0,
                     desc: String? = nil,
                     isEnable: Bool = true) -> ActionSheetManager {
        let model = ActionSheetModel()
        model.title = title
        model.decs = desc
        model.items = items
        model.iconName = iconName
        model.cellType = .segment
        model.segmentSelectedIndex = selectedIndex
        model.isEnable = isEnable
        dataArray.append(model)
        insertEmptyHeaderData()
        return self
    }
    func customCell(customView: UIView?, viewHeight: CGFloat) -> ActionSheetManager {
        let model = ActionSheetModel()
        model.cutsomView = customView
        model.cellType = .custom
        model.customViewHeight = viewHeight
        dataArray.append(model)
        insertEmptyHeaderData()
        return self
    }
    func updateTextCellDesc(indexPath: IndexPath, desc: String?) {
        let section = indexPath.section >= sectionArray.count ? indexPath.section - 1 : indexPath.section
        let model = sectionArray.count > 0 ? sectionArray[section][indexPath.row] : dataArray[indexPath.row]
        model.decs = desc
        tableView.reloadData()
    }
    
    func updateTFCellDesc(indexPath: IndexPath, value: Double) {
        let section = indexPath.section >= sectionArray.count ? indexPath.section - 1 : indexPath.section
        let model = sectionArray.count > 0 ? sectionArray[section][indexPath.row] : dataArray[indexPath.row]
        model.value = value
        tableView.reloadData()
    }
    
    func updateTipsCellTitle(indexPath: IndexPath, title: String?, titleColor: UIColor?) {
        let section = indexPath.section >= sectionArray.count ? indexPath.section - 1 : indexPath.section
        let model = sectionArray.count > 0 ? sectionArray[section][indexPath.row] : dataArray[indexPath.row]
        model.title = title
        model.titleColor = titleColor
        tableView.reloadData()
    }
    func updateSliderValue(indexPath: IndexPath, value: Double, isEnable: Bool = true) {
        let section = indexPath.section >= sectionArray.count ? indexPath.section - 1 : indexPath.section
        let model = sectionArray.count > 0 ? sectionArray[section][indexPath.row] : dataArray[indexPath.row]
        model.value = value
        model.isEnable = isEnable
        tableView.reloadData()
    }
    func updateSwitchStatus(indexPath: IndexPath, isOn: Bool, isEnable: Bool = true) {
        let section = indexPath.section >= sectionArray.count ? indexPath.section - 1 : indexPath.section
        let model = sectionArray.count > 0 ? sectionArray[section][indexPath.row] : dataArray[indexPath.row]
        model.isOn = isOn
        model.isEnable = isEnable
        tableView.reloadData()
    }
    func updateSegmentStatus(indexPath: IndexPath, selectedIndex: Int, isEnable: Bool = true) {
        let section = indexPath.section >= sectionArray.count ? indexPath.section - 1 : indexPath.section
        let model = sectionArray.count > 0 ? sectionArray[section][indexPath.row] : dataArray[indexPath.row]
        model.segmentSelectedIndex = selectedIndex
        model.isEnable = isEnable
        tableView.reloadData()
    }
    
    @objc func config() {
        let preCount = rows.last ?? 0
        let count = dataArray.count - preCount
        rows.append(count)
        guard rows.count > 1 else {
            tableView.reloadData()
            return
        }
        var i: Int = 0
        var itemCount: Int = 0
        var tempArray = [ActionSheetModel]()
        dataArray.forEach({ item in
            let count = self.rows[i]
            tempArray.append(item)
            itemCount += 1
            if itemCount == count {
                sectionArray.append(tempArray)
                itemCount = 0
                if i >= self.rows.count - 1 { return }
                i += 1
                tempArray = [ActionSheetModel]()
            }
        })
        tableView.reloadData()
        layoutIfNeeded()
    }
    func show() {
        guard let vc = UIViewController.cl_topViewController() else { return }
        let vcs = vc.navigationController?.viewControllers ?? []
        let isContainer = vcs.contains(where: { $0 is SAAlertViewController })
        let maxH = Screen.height * 0.75
        let tableViewH = tableView.contentSize.height >= maxH ? maxH : tableView.contentSize.height < 300 ? 300 : tableView.contentSize.height
        tableViewHCons?.constant = tableViewH
        tableViewHCons?.isActive = true
        var height = tableViewH + 59 + Screen.safeAreaBottomHeight()
        var component = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth,
                                                                     height: height))
        backButton.isHidden = !isContainer
        if isContainer || vcs.isEmpty {
            height = vc.view.height - 59 - Screen.safeAreaBottomHeight()
            component = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth,
                                                                         height: height))
            tableViewHCons?.constant = height
            tableViewHCons?.isActive = true
            let controller = SAAlertViewController(compent: component,
                                                         custom: self,
                                                         isLayout: true)
            vc.navigationController?.pushViewController(controller, animated: true)
            
        } else {
            let controller = SAAlertViewController(compent: component,
                                                   custom: self,
                                                   isLayout: true)
            let nav = SAAlertNavigationController(rootViewController: controller)
            if let savc = vc as? SABaseViewController {
                savc.sa_navigationViewController(nav)
            }else{
                vc.present(nav, animated: true)
            }
        }
    }
    
    @objc func show_voice() {
        isVoice = true
        guard let vc = UIViewController.cl_topViewController() else { return }
        let vcs = vc.navigationController?.viewControllers ?? []
        let isContainer = vcs.contains(where: { $0 is SAAlertViewController })
        let maxH = Screen.height * 0.75
        let tableViewH = tableView.contentSize.height >= maxH ? maxH : tableView.contentSize.height < 300 ? 300 : tableView.contentSize.height
        tableViewHCons?.constant = tableViewH
        tableViewHCons?.isActive = true
        var height = tableViewH + 59 + Screen.safeAreaBottomHeight()
        var component = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth,
                                                                     height: height))
        backButton.isHidden = false
        if isContainer || vcs.isEmpty {
            height = vc.view.height - 59 - Screen.safeAreaBottomHeight()
            component = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth,
                                                                         height: height))
            tableViewHCons?.constant = height
            tableViewHCons?.isActive = true
            let controller = SAAlertViewController(compent: component,
                                                         custom: self,
                                                         isLayout: true)
            vc.navigationController?.pushViewController(controller, animated: true)
            
        } else {
            let controller = SAAlertViewController(compent: component,
                                                   custom: self,
                                                   isLayout: true)
            VoiceRoomPresentView.shared.push(with: controller, frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 454), maxHeight: height)
        }
    }
    
    private func insertEmptyHeaderData() {
        if sectionHeaderArray.isEmpty {
            let model = ActionSheetSectionModel(title: nil, decs: nil, iconName: nil)
            sectionHeaderArray.append(model)
        }
    }
    
    @objc
    private func clickBackButton(sender: UIButton) {
        if isVoice {
            VoiceRoomPresentView.shared.pop()
            return
        }
        let vc = UIViewController.cl_topViewController()
        vc?.navigationController?.popViewController(animated: true)
        let vcs = vc?.navigationController?.viewControllers ?? []
        sender.isHidden = vcs.isEmpty
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        self.endEditing(true)
    }
}

extension ActionSheetManager: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        sectionHeaderArray.count
    }
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        sectionArray.count > 0 ? sectionArray[section >= sectionArray.count ? section - 1 : section].count : dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let section = indexPath.section > sectionArray.count ? indexPath.section - 1 : indexPath.section
        let model = sectionArray.count > 0 ? sectionArray[section][indexPath.row] : dataArray[indexPath.row]
        model.isHiddenLine = indexPath.row == (sectionArray.count > 0 ? sectionArray[section].count - 1 : dataArray.count - 1)
        switch model.cellType {
        case .text:
            let cell = tableView.dequeueReusableCell(withIdentifier: model.cellIdentifier, for: indexPath) as! ActionSheetTextCell
            cell.setupModel(model: model)
            return cell
        case .tf:
            let cell = tableView.dequeueReusableCell(withIdentifier: model.cellIdentifier, for: indexPath) as! ActionSheetTFCell
            cell.setupModel(model: model)
            cell.tag = indexPath.row + 100
            cell.sendBlock = {[weak self](key, value) in
                //设置指令
                let newModel: ActionSheetModel = model
                newModel.value = Double(value)
                self?.dataArray[indexPath.row] = newModel
                 UserDefaults.standard.setValue(value, forKey: key)
                UserDefaults.standard.synchronize()
                if indexPath.row > 0 && indexPath.row < 11 {
                    guard let flag: Bool = UserDefaults.standard.object(forKey: "AINSCUSTOM") as? Bool, let self = self, let block = didCustomModeSetClosure else {return}
                    if flag == true {
                        block(.ns)
                    }
                } else if indexPath.row >= 11 && indexPath.row < 15 {
                    guard let flag: Bool = UserDefaults.standard.object(forKey: "AEDCUSTOM") as? Bool, let self = self, let block = didCustomModeSetClosure else {return}
                    if flag == true {
                        block(.aed)
                    }
                    
                } else if indexPath.row == 15 {
                    guard let flag: Bool = UserDefaults.standard.object(forKey: "ASPTCUSTOM") as? Bool, let self = self, let block = didCustomModeSetClosure else {return}
                    if flag == true {
                        block(.aspt)
                    }
                    
                }
                
            }
            return cell
        case .tips:
            let cell = tableView.dequeueReusableCell(withIdentifier: model.cellIdentifier, for: indexPath) as! ActionSheetTipsCell
            model.isHiddenLine = true
            cell.setupModel(model: model)
            cell.selectionStyle = .none
            return cell
            
        case .sw:
            let cell = tableView.dequeueReusableCell(withIdentifier: model.cellIdentifier, for: indexPath) as! ActionSheetSwitchCell
            cell.setupModel(model: model)
            cell.didSwitchValueChangeClosure = { [weak self] isOn in
                self?.didSwitchValueChangeClosure?(indexPath, isOn)
            }
            return cell
            
        case .slider:
            let cell = tableView.dequeueReusableCell(withIdentifier: model.cellIdentifier, for: indexPath) as! ActionSheetSliderCell
            cell.setupModel(model: model)
            cell.didSliderValueChangeClosure = { [weak self] value in
                self?.didSliderValueChangeClosure?(indexPath, value)
            }
            return cell
            
        case .segment:
            let cell = tableView.dequeueReusableCell(withIdentifier: model.cellIdentifier, for: indexPath) as! ActionSheetSegmentCell
            cell.setupModel(model: model)
            cell.didSegmentValueChangeClosure = { [weak self] index in
                let item = model.items?[index] ?? ""
                self?.didSegmentValueChangeClosure?(indexPath, item, index)
            }
            return cell
            
        case .custom:
            let cell = tableView.dequeueReusableCell(withIdentifier: model.cellIdentifier, for: indexPath) as! ActionSheetCustomCell
            cell.setupModel(model: model)
            return cell
        }
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        didCellItemClosure?(indexPath)
    }
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        sectionHeaderArray[section].title == nil ? 0.1 : 32.0
    }
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        0.1
    }
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let model = sectionArray.count > 0 ? sectionArray[indexPath.section][indexPath.row] : dataArray[indexPath.row]
        return model.cellType == .tips ? 40 : model.cellType == .custom ? model.customViewHeight : 53
    }
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if sectionHeaderArray.isEmpty || section >= sectionHeaderArray.count  {
            return UIView()
        }
        let view = ActionSheetHeaderView()
        let model = sectionHeaderArray[section]
        view.setupModel(model: model)
        return view
    }
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        UIView()
    }
}

class ActionSheetHeaderView: UIView {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "标题"
        label.font = .systemFont(ofSize: 13)
        label.textColor = UIColor(hex: "#6C7192", alpha: 1.0)
        label.textAlignment = .left
        label.setContentHuggingPriority(.defaultLow, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = "描述"
        label.font = .systemFont(ofSize: 11)
        label.textColor = UIColor(hex: "#979CBB", alpha: 1.0)
        label.textAlignment = .right
        label.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: ""))
        return imageView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: ActionSheetSectionModel) {
        iconImageView.isHidden = model.iconName == nil
        descLabel.isHidden = model.decs == nil
        iconImageView.image = UIImage.sceneImage(name: model.iconName ?? "")
        titleLabel.text = model.title
        descLabel.text = model.decs
    }
    
    private func setupUI() {
        backgroundColor = UIColor(hexString: "#F7F8FB")
        addSubview(titleLabel)
        addSubview(iconImageView)
        addSubview(descLabel)
        
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        iconImageView.translatesAutoresizingMaskIntoConstraints = false
        descLabel.translatesAutoresizingMaskIntoConstraints = false
        
        titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 15).isActive = true
        titleLabel.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
        
        iconImageView.leadingAnchor.constraint(equalTo: titleLabel.trailingAnchor, constant: 4).isActive = true
        iconImageView.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
        
        descLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -16).isActive = true
        descLabel.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
    }
}

class ActionSheetTextCell: UITableViewCell {
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 15
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fill
        return stackView
    }()
    private lazy var iconContainerView: UIView = {
        let view = UIView()
        view.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        view.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return view
    }()
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: ""))
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "标题"
        label.font = .systemFont(ofSize: 13)
        label.textColor = .black
        label.textAlignment = .left
        label.setContentHuggingPriority(.defaultLow, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = "描述"
        label.font = .systemFont(ofSize: 13)
        label.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        label.textAlignment = .right
        label.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var arrowContainerView: UIView = {
        let view = UIView()
        view.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        view.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return view
    }()
    private lazy var arrowImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "arrow_right"))
        return imageView
    }()
    private lazy var lineView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#F8F5FA")
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: ActionSheetModel) {
        iconContainerView.isHidden = model.iconName == nil
        titleLabel.text = model.title
        descLabel.text = model.decs
        iconImageView.image = UIImage.sceneImage(name: model.iconName ?? "")
        arrowContainerView.isHidden = !model.isShowArrow
        lineView.isHidden = model.isHiddenLine
    }
    
    private func setupUI() {
        selectionStyle = .none
        contentView.addSubview(stackView)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.addArrangedSubview(iconContainerView)
        stackView.addArrangedSubview(titleLabel)
        stackView.addArrangedSubview(descLabel)
        stackView.addArrangedSubview(arrowContainerView)
        
        stackView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 15).isActive = true
        stackView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -15).isActive = true
        stackView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        iconContainerView.translatesAutoresizingMaskIntoConstraints = false
        iconContainerView.widthAnchor.constraint(equalToConstant: 20).isActive = true
        
        iconContainerView.addSubview(iconImageView)
        iconImageView.translatesAutoresizingMaskIntoConstraints = false
        iconImageView.centerYAnchor.constraint(equalTo: iconContainerView.centerYAnchor).isActive = true
        iconImageView.centerYAnchor.constraint(equalTo: iconContainerView.centerYAnchor).isActive = true
        
        arrowContainerView.widthAnchor.constraint(equalToConstant: 16).isActive = true
        
        arrowContainerView.addSubview(arrowImageView)
        arrowImageView.translatesAutoresizingMaskIntoConstraints = false
        arrowImageView.trailingAnchor.constraint(equalTo: arrowContainerView.trailingAnchor).isActive = true
        arrowImageView.centerYAnchor.constraint(equalTo: arrowContainerView.centerYAnchor).isActive = true
        
        contentView.addSubview(lineView)
        lineView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        lineView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        lineView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
}

class ActionSheetTipsCell: UITableViewCell {
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 0
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fill
        return stackView
    }()
    private lazy var iconContainerView: UIView = {
        let view = UIView()
        view.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        view.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return view
    }()
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage())
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.font = .systemFont(ofSize: 12)
        label.textColor = UIColor(hex: "#979CBB")
        label.textAlignment = .left
        label.setContentHuggingPriority(.defaultLow, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var lineView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#F8F5FA")
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: ActionSheetModel) {
        let titleColor = model.titleColor == nil ? titleLabel.textColor : model.titleColor
        titleLabel.textColor = titleColor
        titleLabel.text = model.title
        let image = UIImage.sceneImage(name: model.iconName ?? "")?.withTintColor(titleColor ?? .black,
                                                                        renderingMode: .alwaysOriginal)
        iconContainerView.isHidden = image == nil
        iconImageView.image = image
        lineView.isHidden = model.isHiddenLine
    }
    
    private func setupUI() {
        selectionStyle = .none
        contentView.addSubview(stackView)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.addArrangedSubview(iconContainerView)
        stackView.addArrangedSubview(titleLabel)
        
        stackView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 15).isActive = true
        stackView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -15).isActive = true
        stackView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -5).isActive = true
        
        iconContainerView.translatesAutoresizingMaskIntoConstraints = false
        iconContainerView.widthAnchor.constraint(equalToConstant: 16).isActive = true
        
        iconContainerView.addSubview(iconImageView)
        iconImageView.translatesAutoresizingMaskIntoConstraints = false
        iconImageView.centerYAnchor.constraint(equalTo: iconContainerView.centerYAnchor).isActive = true
        iconImageView.centerYAnchor.constraint(equalTo: iconContainerView.centerYAnchor).isActive = true
        
        contentView.addSubview(lineView)
        lineView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        lineView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        lineView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
}

class ActionSheetSwitchCell: UITableViewCell {
    var didSwitchValueChangeClosure: ((Bool) -> Void)?
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 15
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fill
        return stackView
    }()
    private lazy var iconContainerView: UIView = {
        let view = UIView()
        return view
    }()
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView()
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "标题"
        label.font = .systemFont(ofSize: 13)
        label.textColor = .black
        label.textAlignment = .left
        label.setContentHuggingPriority(.defaultLow, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var switchContainerView: UIView = {
        let view = UIView()
        return view
    }()
    private lazy var sw: UISwitch = {
        let sw = UISwitch()
        sw.onTintColor = UIColor(hex: "#009FFF", alpha: 1.0)
        sw.addTarget(self, action: #selector(onClickSwitch(sender:)), for: .valueChanged)
        return sw
    }()
    private lazy var lineView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#F8F5FA")
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    private var currentModel: ActionSheetModel?
    
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: ActionSheetModel) {
        currentModel = model
        iconContainerView.isHidden = model.iconName == nil
        sw.isEnabled = model.isEnable
        titleLabel.text = model.title
        iconImageView.image = UIImage.sceneImage(name: model.iconName ?? "")
        sw.setOn(model.isOn, animated: true)
        lineView.isHidden = model.isHiddenLine
        sw.accessibilityIdentifier = model.accessibilityIdentifier
    }
    
    private func setupUI() {
        selectionStyle = .none
        contentView.addSubview(stackView)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.addArrangedSubview(iconContainerView)
        stackView.addArrangedSubview(titleLabel)
        stackView.addArrangedSubview(switchContainerView)
        
        stackView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 15).isActive = true
        stackView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -15).isActive = true
        stackView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        iconContainerView.translatesAutoresizingMaskIntoConstraints = false
        iconContainerView.widthAnchor.constraint(equalToConstant: 20).isActive = true
        
        iconContainerView.addSubview(iconImageView)
        iconImageView.translatesAutoresizingMaskIntoConstraints = false
        iconImageView.centerYAnchor.constraint(equalTo: iconContainerView.centerYAnchor).isActive = true
        iconImageView.centerYAnchor.constraint(equalTo: iconContainerView.centerYAnchor).isActive = true
        
        switchContainerView.addSubview(sw)
        sw.translatesAutoresizingMaskIntoConstraints = false
        sw.trailingAnchor.constraint(equalTo: switchContainerView.trailingAnchor).isActive = true
        sw.centerYAnchor.constraint(equalTo: switchContainerView.centerYAnchor).isActive = true
        
        contentView.addSubview(lineView)
        lineView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        lineView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        lineView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
    
    @objc
    private func onClickSwitch(sender: UISwitch) {
        currentModel?.isOn = sender.isOn
        didSwitchValueChangeClosure?(sender.isOn)
    }
}

class ActionSheetSliderCell: UITableViewCell {
    var didSliderValueChangeClosure: ((Double) -> Void)?
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 15
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fill
        return stackView
    }()
    private lazy var iconContainerView: UIView = {
        let view = UIView()
        return view
    }()
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: ""))
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "标题"
        label.font = .systemFont(ofSize: 13)
        label.textColor = .black
        label.textAlignment = .left
        label.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = "描述"
        label.font = .systemFont(ofSize: 13)
        label.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        label.textAlignment = .right
        label.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var slider: UISlider = {
        let slider = UISlider()
        slider.minimumTrackTintColor = UIColor(hex: "#009FFF", alpha: 1.0)
        slider.addTarget(self, action: #selector(onClickSlider(sender:)), for: .valueChanged)
        slider.setContentHuggingPriority(.defaultLow, for: .horizontal)
        slider.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        return slider
    }()
    private lazy var lineView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#F8F5FA")
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    private var currentModel: ActionSheetModel?
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: ActionSheetModel) {
        currentModel = model
        iconContainerView.isHidden = model.iconName == nil
        slider.isEnabled = model.isEnable
        titleLabel.text = model.title
        titleLabel.isEnabled = model.isEnable
        descLabel.isEnabled = model.isEnable
        descLabel.text = "\(Int(model.value * 100))"
        slider.setValue(Float(model.value), animated: true)
        iconImageView.image = UIImage.sceneImage(name: model.iconName ?? "")
        lineView.isHidden = model.isHiddenLine
        slider.accessibilityIdentifier = accessibilityIdentifier
    }
    
    private func setupUI() {
        selectionStyle = .none
        contentView.addSubview(stackView)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.addArrangedSubview(iconContainerView)
        stackView.addArrangedSubview(titleLabel)
        stackView.addArrangedSubview(slider)
        stackView.addArrangedSubview(descLabel)
        
        stackView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 15).isActive = true
        stackView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -15).isActive = true
        stackView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        iconContainerView.translatesAutoresizingMaskIntoConstraints = false
        iconContainerView.widthAnchor.constraint(equalToConstant: 20).isActive = true
        
        iconContainerView.addSubview(iconImageView)
        iconImageView.translatesAutoresizingMaskIntoConstraints = false
        iconImageView.centerYAnchor.constraint(equalTo: iconContainerView.centerYAnchor).isActive = true
        iconImageView.centerYAnchor.constraint(equalTo: iconContainerView.centerYAnchor).isActive = true
        
        contentView.addSubview(lineView)
        lineView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        lineView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        lineView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
    
    @objc
    private func onClickSlider(sender: UISlider) {
        currentModel?.value = Double(sender.value)
        descLabel.text = "\(Int(sender.value * 100))"
        didSliderValueChangeClosure?(Double(sender.value))
    }
}

class ActionSheetSegmentCell: UITableViewCell {
    var didSegmentValueChangeClosure: ((Int) -> Void)?
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 15
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fill
        return stackView
    }()
    private lazy var iconContainerView: UIView = {
        let view = UIView()
        return view
    }()
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage())
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "标题"
        label.font = .systemFont(ofSize: 13)
        label.textColor = .black
        label.textAlignment = .left
        label.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = "描述"
        label.font = .systemFont(ofSize: 13)
        label.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        label.textAlignment = .right
        label.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var segmentContainerView: UIView = {
        let view = UIView()
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    private lazy var segment: UISegmentedControl = {
        let segment = UISegmentedControl(items: [])
        segment.addTarget(self, action: #selector(onClickSegment(sender:)), for: .valueChanged)
        segment.translatesAutoresizingMaskIntoConstraints = false
        return segment
    }()
    private lazy var lineView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#F8F5FA")
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: ActionSheetModel) {
        segment.isEnabled = model.isEnable
        titleLabel.isEnabled = model.isEnable
        descLabel.isEnabled = model.isEnable
        titleLabel.text = model.title
        descLabel.text = model.decs
        descLabel.isHidden = model.decs?.isEmpty ?? true
        let image = UIImage(named: model.iconName ?? "")
        iconContainerView.isHidden = image == nil
        iconImageView.image = image
        lineView.isHidden = model.isHiddenLine
        segment.selectedSegmentIndex = model.segmentSelectedIndex
        guard segment.numberOfSegments <= 0 else { return }
        model.items?.enumerated().forEach({ index, item in
            let rect = NSAttributedString(string: item).boundingRect(with: CGSize(width: 200, height: 30), context: nil)
            segment.insertSegment(withTitle: item, at: index, animated: false)
            segment.setWidth(rect.width + 25, forSegmentAt: index)
        })
    }
    
    private func setupUI() {
        selectionStyle = .none
        contentView.addSubview(stackView)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.addArrangedSubview(iconContainerView)
        stackView.addArrangedSubview(titleLabel)
        stackView.addArrangedSubview(segmentContainerView)
        stackView.addArrangedSubview(descLabel)
        
        segmentContainerView.addSubview(segment)
        
        stackView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 15).isActive = true
        stackView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -15).isActive = true
        stackView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        iconContainerView.translatesAutoresizingMaskIntoConstraints = false
        iconContainerView.widthAnchor.constraint(equalToConstant: 20).isActive = true
        
        iconContainerView.addSubview(iconImageView)
        iconImageView.translatesAutoresizingMaskIntoConstraints = false
        iconImageView.centerYAnchor.constraint(equalTo: iconContainerView.centerYAnchor).isActive = true
        iconImageView.centerYAnchor.constraint(equalTo: iconContainerView.centerYAnchor).isActive = true
        
        segment.topAnchor.constraint(equalTo: segmentContainerView.topAnchor, constant: 13).isActive = true
        segment.trailingAnchor.constraint(equalTo: segmentContainerView.trailingAnchor).isActive = true
        segment.bottomAnchor.constraint(equalTo: segmentContainerView.bottomAnchor, constant: -13).isActive = true
        
        contentView.addSubview(lineView)
        lineView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        lineView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        lineView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
    
    @objc
    private func onClickSegment(sender: UISegmentedControl) {
        didSegmentValueChangeClosure?(sender.selectedSegmentIndex)
    }
}

class ActionSheetCustomCell: UITableViewCell {
    private lazy var lineView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#F8F5FA")
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: ActionSheetModel) {
        lineView.isHidden = model.isHiddenLine
        guard let view = model.cutsomView,
              !contentView.subviews.contains(where: { $0 == view }) else { return }
        contentView.addSubview(view)
        view.translatesAutoresizingMaskIntoConstraints = false
        view.leadingAnchor.constraint(equalTo: contentView.leadingAnchor).isActive = true
        view.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        view.trailingAnchor.constraint(equalTo: contentView.trailingAnchor).isActive = true
        view.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
    }
    
    private func setupUI() {
        selectionStyle = .none
        contentView.addSubview(lineView)
        lineView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        lineView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        lineView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
}

class ActionSheetTFCell: UITableViewCell {
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 15
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fill
        return stackView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "标题"
        label.font = .systemFont(ofSize: 13)
        label.textColor = .black
        label.textAlignment = .left
        label.numberOfLines = 0
        label.setContentHuggingPriority(.defaultLow, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return label
    }()
    private lazy var inputTF: UITextField = {
        let view = UITextField()
        view.placeholder = ""
        view.font = .systemFont(ofSize: 13)
        view.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        view.textAlignment = .center
        view.backgroundColor = .lightGray
        view.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        view.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        return view
    }()
    private lazy var subBtn: UIButton = {
        let view = UIButton()
        view.setTitle("设置", for: .normal)
        view.backgroundColor = .blue
        view.setTitleColor(.white, for: .normal)
        view.titleLabel?.font = UIFont.systemFont(ofSize: 13)
        view.layer.cornerRadius = 5
        view.layer.masksToBounds = true
        return view
    }()
    
    private lazy var lineView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#F8F5FA")
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    public var sendBlock:((String,Int)->Void)?
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: ActionSheetModel) {
        titleLabel.text = model.title
        inputTF.text = String(Int(model.value))
        lineView.isHidden = model.isHiddenLine
    }
    
    private func setupUI() {
        selectionStyle = .none
        contentView.addSubview(stackView)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.addArrangedSubview(titleLabel)
        stackView.addArrangedSubview(inputTF)
        stackView.addArrangedSubview(subBtn)
        
        stackView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 15).isActive = true
        stackView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -15).isActive = true
        stackView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.leadingAnchor.constraint(equalTo: stackView.leadingAnchor, constant: 0).isActive = true
        titleLabel.topAnchor.constraint(equalTo: stackView.topAnchor, constant: 10).isActive = true
        titleLabel.widthAnchor.constraint(equalToConstant: 200).isActive = true
        titleLabel.centerYAnchor.constraint(equalTo: stackView.centerYAnchor, constant: 0).isActive = true
        
        inputTF.translatesAutoresizingMaskIntoConstraints = false
        inputTF.leadingAnchor.constraint(equalTo: titleLabel.trailingAnchor, constant: 15).isActive = true
        inputTF.topAnchor.constraint(equalTo: titleLabel.topAnchor, constant: 10).isActive = true
        inputTF.widthAnchor.constraint(equalToConstant: 60).isActive = true
        inputTF.bottomAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: -10).isActive = true

        subBtn.translatesAutoresizingMaskIntoConstraints = false
        subBtn.leadingAnchor.constraint(equalTo: inputTF.trailingAnchor, constant: 15).isActive = true
        subBtn.topAnchor.constraint(equalTo: inputTF.topAnchor, constant: 10).isActive = true
        subBtn.widthAnchor.constraint(equalToConstant: 50).isActive = true
        subBtn.bottomAnchor.constraint(equalTo: inputTF.bottomAnchor, constant: -10).isActive = true
        subBtn.addTarget(self, action: #selector(send), for: .touchUpInside)
        
        contentView.addSubview(lineView)
        lineView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20).isActive = true
        lineView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20).isActive = true
        lineView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
    
    @objc func send(_ btn: UIButton) {
        let text = inputTF.text
        guard let block = sendBlock else {return}
        block(titleLabel.text ?? "", Int(text ?? "0") ?? 0)
    }
}
