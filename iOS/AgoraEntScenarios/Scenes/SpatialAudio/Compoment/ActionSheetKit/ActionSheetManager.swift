//
//  ActionSheetManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/2/7.
//

import UIKit
import AgoraCommon
enum ActionSheetCellType {
    case text
    case sw
    case slider
}

class ActionSheetModel: NSObject {
    var title: String?
    var decs: String?
    var isOn: Bool = false
    var iconName: String?
    var value: Double = 0
    var isShowArrow: Bool = false
    var cellType: ActionSheetCellType = .text
    var isEnable: Bool = true
    
    var cellIdentifier: String {
        switch cellType {
        case .text: return ActionSheetTextCell.description()
        case .sw: return ActionSheetSwitchCell.description()
        case .slider: return ActionSheetSliderCell.description()
        }
    }
}

struct ActionSheetSectionModel {
    var title: String?
    var decs: String?
    var iconName: String?
}

class ActionSheetManager: UIView {
    var didCellItemClosure: ((IndexPath) -> Void)?
    var didSliderValueChangeClosure: ((IndexPath, Double) -> Void)?
    var didSwitchValueChangeClosure: ((IndexPath, Bool) -> Void)?
    
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
        tableView.registerCell(ActionSheetTextCell.self, forCellReuseIdentifier: ActionSheetTextCell.description())
        tableView.registerCell(ActionSheetSwitchCell.self, forCellReuseIdentifier: ActionSheetSwitchCell.description())
        tableView.registerCell(ActionSheetSliderCell.self, forCellReuseIdentifier: ActionSheetSliderCell.description())
        return tableView
    }()
    private lazy var backButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "back"), for: .normal)
        button.isHidden = true
        button.addTargetFor(self, action: #selector(clickBackButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var lineView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "tchead"))
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#040925", alpha: 1.0)
        label.font = UIFont.boldSystemFont(ofSize: 16)
        return label
    }()
    
    private var section: Int = 0
    private var row: Int = 0
    private var rows: [Int] = []
    private var dataArray: [ActionSheetModel] = []
    private var sectionArray = [[ActionSheetModel]]()
    private var sectionHeaderArray: [ActionSheetSectionModel] = []
    private var tableViewHCons: NSLayoutConstraint?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .white
        translatesAutoresizingMaskIntoConstraints = false
        widthAnchor.constraint(equalToConstant: Screen.width).isActive = true
        layer.cornerRadius = 10
        layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        layer.masksToBounds = true
        
        addSubview(backButton)
        backButton.translatesAutoresizingMaskIntoConstraints = false
        backButton.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        backButton.topAnchor.constraint(equalTo: topAnchor, constant: 10).isActive = true
        
        addSubview(lineView)
        lineView.addSubview(titleLabel)
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
    
    func section(section: Int) -> ActionSheetManager {
        self.section = section
        return self
    }
    func row(row: Int) -> ActionSheetManager {
        self.row = row
        return self
    }
    func rows(rows: [Int]) -> ActionSheetManager {
        self.rows = rows
        return self
    }
    func title(title: String) -> ActionSheetManager {
        titleLabel.text = title
        return self
    }
    func sectionHeader(iconName: String? = nil,
                       title: String?,
                       desc: String?) -> ActionSheetManager {
        let model = ActionSheetSectionModel(title: title, decs: desc, iconName: iconName)
        sectionHeaderArray.append(model)
        return self
    }
    func textCell(iconName: String? = nil,
                  title: String?,
                  desc: String?,
                  isShowArrow: Bool = false) -> ActionSheetManager {
        let model = ActionSheetModel()
        model.title = title
        model.decs = desc
        model.iconName = iconName
        model.isShowArrow = isShowArrow
        model.cellType = .text
        dataArray.append(model)
        return self
    }
    func switchCell(iconName: String? = nil,
                    title: String?,
                    isOn: Bool = false,
                    isEnabel: Bool = true) -> ActionSheetManager {
        let model = ActionSheetModel()
        model.title = title
        model.isOn = isOn
        model.iconName = iconName
        model.cellType = .sw
        model.isEnable = isEnabel
        dataArray.append(model)
        return self
    }
    func sliderCell(iconName: String? = nil,
                    title: String?,
                    value: Double,
                    isEnable: Bool = true) -> ActionSheetManager {
        let model = ActionSheetModel()
        model.title = title
        model.value = value
        model.iconName = iconName
        model.cellType = .slider
        model.isEnable = isEnable
        dataArray.append(model)
        return self
    }
    func updateTextCellDesc(indexPath: IndexPath, desc: String?) {
        let section = indexPath.section >= sectionArray.count ? indexPath.section - 1 : indexPath.section
        let model = sectionArray.count > 0 ? sectionArray[section][indexPath.row] : dataArray[indexPath.row]
        model.decs = desc
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
    
    func config() {
        guard rows.count > 1 else { return }
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
            (vc as? SABaseViewController)?.sa_navigationViewController(nav)
        }
    }
    
    @objc
    private func clickBackButton(sender: UIButton) {
        let vc = UIViewController.cl_topViewController()
        vc?.navigationController?.popViewController(animated: true)
        let vcs = vc?.navigationController?.viewControllers ?? []
        sender.isHidden = vcs.isEmpty
    }
}

extension ActionSheetManager: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        section
    }
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        sectionArray.count > 0 ? sectionArray[section >= sectionArray.count ? section - 1 : section].count : dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let section = indexPath.section >= sectionArray.count ? indexPath.section - 1 : indexPath.section
        let model = sectionArray.count > 0 ? sectionArray[section][indexPath.row] : dataArray[indexPath.row]
        switch model.cellType {
        case .text:
            let cell = tableView.dequeueReusableCell(withIdentifier: model.cellIdentifier, for: indexPath) as! ActionSheetTextCell
            cell.setupModel(model: model)
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
        }
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        didCellItemClosure?(indexPath)
    }
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        sectionHeaderArray.isEmpty ? 0 : 32
    }
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        0.1
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
        
        titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20).isActive = true
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
        descLabel.text = "\(Int(model.value * 100))"
        slider.setValue(Float(model.value), animated: true)
        iconImageView.image = UIImage.sceneImage(name: model.iconName ?? "")
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
    }
    
    @objc
    private func onClickSlider(sender: UISlider) {
        currentModel?.value = Double(sender.value)
        descLabel.text = "\(Int(sender.value * 100))"
        didSliderValueChangeClosure?(Double(sender.value))
    }
}
