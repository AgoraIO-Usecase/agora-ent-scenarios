//
//  VLSelectTagView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/18.
//

import UIKit

@objc protocol VLSelectTagViewDelegate : NSObjectProtocol {
    
    
    /// 传递所有选中的值
    ///
    /// - Parameters:
    ///   - selArr: 所有选中的值
    ///   - groupArr: gtoupIdArr
    
    @objc optional func confimrReturnAllSelValueWithDelegate(selArr : Array<Any>, groupArr : Array<Any>)
    
    /// 当前选择的值
    ///
    /// - Parameters:
    ///   - valueStr: Value
    ///   - index: 当前下标
    ///   - groupId: groupId
    @objc optional func currentSelValueWithDelegate(valueStr : String, index : Int, groupId : Int)
}

class VLSelectTagView: UIView {
    //MARK:----publish
    /// 组高度
    public var titleLabHeight = 30
    /// 组标题字体
    public var titleTextFont : UIFont = .boldSystemFont(ofSize: 14)
    /// 组标题 字体颜色
    public var titleTextColor : UIColor = .black
    /// 显示按钮的高度
    public var content_height = 30
    /// 上下按钮之间的间距
    public var content_y = 10
    /// 左右按钮之间的间距
    public var content_x = 10
    /// title默认颜色
    public var content_norTitleColor : UIColor = .white
    /// title选中颜色
    public var content_selTitleColor : UIColor = .white
    /// 背景默认原色
    public var content_backNorColor : UIColor = .gray
    /// 背景选中颜色
    public var content_backSelColor : UIColor = .orange
    /// 字体大小
    public var content_titleFont : UIFont = .systemFont(ofSize: 12)
    /// 圆角
    public var content_radius : Int = 8
    /// 是否单选，默认 true 是单选
    public var isSingle : Bool = true
    /// 是否设置默认选中 默认 true 默认选中
    public var isDefaultChoice : Bool = true
    /// isDefaultChoice 为true时 改属性有效，默认为 0
    public var defaultSelIndex : Int = 0
    /// isDefaultChoice 为true时 改属性有效 defaultSelIndex 属性无效，为每各组设置单选选项
    public var defaultSelSingleIndeArr : Array = Array<Any>()
    /// 为每个组设置单选或多选，设置该属性时 isSingle 参数无效, 0 = 多选， 1 = 单选
    public var defaultGroupSingleArr = Array<Int>(){
        didSet{
            for value in defaultGroupSingleArr {
                if !(value == 0 || value == 1){
                    assert((value == 0 || value == 1), "defaultGroupSingleArr的值只能是 0 和 1")
                }
            }
        }
    }
    /// isDefaultChoice 为true时 该属性有效，设置每组默认选择项，可传数组
    public var defaultSelIndexArr = Array<Any>() {
        didSet{
            for (index,value) in defaultSelIndexArr.enumerated() {
                if value is Array<Any>{
                    if !defaultGroupSingleArr.isEmpty{
                        defaultGroupSingleArr[index] = 0
                    }
                    isSingle = false
                }
            }
        }
    }
    
    public var confirmReturnValueClosure : ((Array<Any>, Array<Any>) -> Void)?
    public var currentSelValueClosure : ((String, Int, Int) -> Void)?
    public weak var delegate : VLSelectTagViewDelegate?
    
    private let scrollView : UIScrollView = {
        let scrollview = UIScrollView()
        scrollview.showsVerticalScrollIndicator = false
        scrollview.showsHorizontalScrollIndicator = false
        scrollview.translatesAutoresizingMaskIntoConstraints = false
        scrollview.isScrollEnabled = false
        return scrollview
    }()
    private var tempContentArr : Array = Array<Any>()
    private var tempTitleArr : Array = Array<Any>()
    private var frameRect : CGRect = .zero
    private var dataSourceArr : Array = Array<Any>()
    private var saveSelButValueArr : Array = Array<Any>()
    private var saveSelGroupIndexeArr : Array = Array<Any>()
    private let itemTag: Int = 100
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(scrollView)
        scrollView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        scrollView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        scrollView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        scrollView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    func setDataSource(contetnArr : Array<Any>, titleArr : Array<String>){
        saveSelButValueArr.removeAll()
        saveSelGroupIndexeArr.removeAll()
        
        if defaultGroupSingleArr.count != titleArr.count && !defaultGroupSingleArr.isEmpty{
            assert(defaultGroupSingleArr.count == titleArr.count, "默认选择的defaultGroupSingleArr.count 要 与titleArr.count一至")
            return
        }
        
        tempContentArr = contetnArr.count > 0 ? contetnArr : tempContentArr
        tempTitleArr = titleArr.count > 0 ? titleArr : tempTitleArr
        
        frameRect = .zero
        dataSourceArr.removeAll()
        dataSourceArr.append(contentsOf: tempContentArr)
        
        for (index,title) in titleArr.enumerated() {
            saveSelButValueArr.append("")
            saveSelGroupIndexeArr.append("")
            frameRect = setupGroupAndStream(content: contetnArr[index] as! Array<Any>, titleStr: title, currFrame: frameRect, groupId: index)
        }
        scrollView.contentSize = CGSize(width: frame.width, height: frameRect.size.height + frameRect.origin.y + 20)
    }
    
    // 设置数据源，创建
    func setupGroupAndStream(content : Array<Any>, titleStr : String, currFrame : CGRect, groupId : Int) -> CGRect{
        layoutIfNeeded()
        let groupTitleLab = UILabel.init(frame: CGRect(x: 15, y: currFrame.size.height + currFrame.origin.y + 10, width: 0, height: CGFloat(titleLabHeight)))
        groupTitleLab.text = titleStr
        groupTitleLab.font = titleTextFont
        groupTitleLab.textColor = titleTextColor
        groupTitleLab.frame.size.width = calcuateLabSizeWidth(str: titleStr, font: titleTextFont, maxHeight: CGFloat(titleLabHeight))
        scrollView.addSubview(groupTitleLab)
        let margian_y = 5 + groupTitleLab.frame.origin.y + groupTitleLab.frame.size.height
        var content_totalHeight = CGFloat(margian_y)
        var alineButWidth = CGFloat(0)
        var current_rect = CGRect()
        var margin_x = CGFloat(content_x)
        var tempSaveSelIndexArr = Array<Any>()
        
        for (index,value) in content.enumerated() {
            let sender = UIButton.init(type: .custom)
            scrollView.addSubview(sender)
            sender.setTitle(value as? String, for: .normal)
            sender.tag = index + groupId * itemTag + 1
            sender.titleLabel?.font = content_titleFont
            sender.backgroundColor = content_backNorColor
            sender.setTitleColor(content_norTitleColor, for: .normal)
            sender.setTitleColor(content_selTitleColor, for: .selected)
            sender.layer.cornerRadius = CGFloat(content_radius)
            sender.addTarget(self, action: #selector(senderEvent), for: .touchUpInside)
            var but_width = calcuateLabSizeWidth(str: value as! String, font:content_titleFont, maxHeight: CGFloat(content_height)) + 20
            var but_height = content_height
            margin_x = CGFloat(alineButWidth) + CGFloat(content_x)
            
            if but_width > UIScreen.main.bounds.size.width - CGFloat(2 * content_x) {
                sender.titleLabel?.numberOfLines = 0
                sender.titleEdgeInsets = .init(top: 0, left: 10, bottom: 0, right: 10)
                but_width = UIScreen.main.bounds.size.width - CGFloat(2 * content_x)
                but_height = Int(self.calcuateLabSizeHeight(str: value as! String, font: content_titleFont, maxWidth: but_width))
                
            }else{
                but_height = content_height
            }
            alineButWidth = CGFloat(content_x) + but_width + CGFloat(alineButWidth)
            if alineButWidth >= self.frame.size.width{
                margin_x = CGFloat(content_x)
                alineButWidth = margin_x + but_width
                content_totalHeight = current_rect.size.height + current_rect.origin.y + CGFloat(content_x)
            }
            sender.frame = CGRect(x: margin_x, y: content_totalHeight, width: but_width, height: CGFloat(but_height))
            current_rect = sender.frame
            if isDefaultChoice{
                if defaultSelIndexArr.isEmpty {
                    setDefaultSingleSelect(index: index, groupId: groupId, value: value as! String, sender: sender, content: content)
                } else {
                    let arr =  setDefaultMultipleSelect(index: index, groupId: groupId, value: value as! String, sender: sender, content: content)
                    tempSaveSelIndexArr.append(contentsOf: arr)
                }
            }
            if index == content.count - 1 {
                frameRect = sender.frame
            }
        }
        if !defaultSelIndexArr.isEmpty{
            saveSelButValueArr[groupId] = tempSaveSelIndexArr
        }
        return frameRect
    }
    
    public func comfirm() {
        if (confirmReturnValueClosure != nil) {
            confirmReturnValueClosure!(saveSelButValueArr,saveSelGroupIndexeArr)
        }
        
        delegate?.confimrReturnAllSelValueWithDelegate?(selArr: saveSelButValueArr, groupArr: saveSelGroupIndexeArr)
        
    }

    public func reload() {
        for value in scrollView.subviews {
            value.removeFromSuperview()
        }
        setDataSource(contetnArr: tempContentArr, titleArr: tempTitleArr as! Array<String>)
    }
    
    // 单选
    private func setDefaultSingleSelect(index : Int , groupId : Int ,value : String, sender : UIButton, content : Array<Any>){
        let valueStr = "\(value)"
        if defaultSelSingleIndeArr.isEmpty{
            assert( !(defaultSelIndex  > content.count - 1), "在groupId = \(groupId) 设置默认选中项不能超过\(content.count - 1)")
            if index == defaultSelIndex{
                sender.isSelected = true
                sender.backgroundColor = content_backSelColor
                saveSelButValueArr[groupId] = valueStr
            }
        }else{
            assert(!((defaultSelSingleIndeArr[groupId] as? Int)! > content.count - 1), "在groupId = \(groupId) 设置默认选中项不能超过\(content.count - 1)")
            if index == defaultSelSingleIndeArr[groupId] as? Int{
                sender.isSelected = true
                sender.backgroundColor = content_backSelColor
                saveSelButValueArr[groupId] = valueStr
            }
        }
        saveSelGroupIndexeArr[groupId] = String(groupId)
    }
    // 多选
    private func setDefaultMultipleSelect(index : Int , groupId : Int ,value : String, sender : UIButton, content : Array<Any>) -> Array<Any>{
        let content = defaultSelIndexArr[groupId]
        var tempSaveSelIndexArr = Array<Any>()
        if content is Int{
            if index == content as! Int{
                sender.isSelected = true
                sender.backgroundColor = content_backSelColor
                tempSaveSelIndexArr.append("\(value)")
            }
        }
        if content is Array<Any>{
            for contenIndex in content as! Array<Any>{
                if index == contenIndex as! Int{
                    sender.isSelected = true
                    sender.backgroundColor = content_backSelColor
                    tempSaveSelIndexArr.append("\(value)")
                    continue
                }
            }
        }
        saveSelGroupIndexeArr[groupId] = String(groupId)
        return tempSaveSelIndexArr
    }
    
    @objc private func senderEvent(sender : UIButton){
        sender.isSelected = !sender.isSelected
        if defaultGroupSingleArr.isEmpty{
            isSingle ? singalSelectEvent(sender: sender) : multipleSelectEvent(sender: sender)
            return
        }
        defaultGroupSingleArr[sender.tag / itemTag] == 0 ? multipleSelectEvent(sender: sender) : singalSelectEvent(sender: sender)
    }
    
    // 单选
    private func singalSelectEvent(sender : UIButton){
        var valueStr : String = ""
        let tempDetailArr = dataSourceArr[sender.tag / itemTag] as! Array<Any>
        if sender.isSelected {
            for (index, _) in tempDetailArr.enumerated(){
                if index + 1 == sender.tag % itemTag {
                    sender.isSelected = true
                    sender.backgroundColor = content_backSelColor
                    continue
                }
                let norSender = scrollView.viewWithTag((sender.tag / itemTag) * itemTag + index + 1) as! UIButton
                norSender.isSelected = false
                norSender.backgroundColor = content_backNorColor
            }
            valueStr = "\(tempDetailArr[sender.tag % itemTag - 1])"
            if currentSelValueClosure != nil {
                currentSelValueClosure!(valueStr,sender.tag % itemTag - 1,sender.tag / itemTag)
            }
            delegate?.currentSelValueWithDelegate?(valueStr: valueStr, index: sender.tag % itemTag - 1, groupId: sender.tag / itemTag)
            
        }else{
            sender.backgroundColor = content_backNorColor
        }
        saveSelButValueArr[sender.tag / itemTag] = valueStr
        saveSelButValueArr[sender.tag / itemTag] as! String == "" ? (saveSelGroupIndexeArr[sender.tag / itemTag] = "") : (saveSelGroupIndexeArr[sender.tag / itemTag] = String(sender.tag / itemTag))
    }
    
    // 多选
    private func multipleSelectEvent(sender : UIButton){
        var valueStr = ""
        var tempSaveArr = Array<Any>()
        if ((saveSelButValueArr[sender.tag / itemTag]) is Array<Any>){
            tempSaveArr = saveSelButValueArr[sender.tag / itemTag] as! Array<Any>
        }else{
            tempSaveArr.append(saveSelButValueArr[sender.tag / itemTag])
        }
        
        let tempDetailArr = dataSourceArr[sender.tag / itemTag] as! Array<Any>
        valueStr = "\(tempDetailArr[sender.tag % itemTag - 1])"
        if sender.isSelected {
            sender.backgroundColor = content_backSelColor
            tempSaveArr.append(valueStr)
            if currentSelValueClosure != nil {
                currentSelValueClosure!(valueStr, sender.tag % itemTag - 1, sender.tag / itemTag)
            }
            delegate?.currentSelValueWithDelegate?(valueStr: valueStr, index: sender.tag % itemTag - 1, groupId: sender.tag / itemTag)
            
        }else{
            sender.backgroundColor = content_backNorColor
            let index : Int = tempSaveArr.firstIndex(where: {$0 as! String == valueStr})!
            tempSaveArr.remove(at: index)
        }
        
        saveSelButValueArr[sender.tag / itemTag] = tempSaveArr
        tempSaveArr.isEmpty ? (saveSelGroupIndexeArr[sender.tag / itemTag] = "") : (saveSelGroupIndexeArr[sender.tag / itemTag] = String(sender.tag / itemTag))
        
    }
        
    private func calcuateLabSizeWidth(str : String, font : UIFont, maxHeight : CGFloat) -> CGFloat{
        let attributes = [kCTFontAttributeName: font]
        let norStr = NSString(string: str)
        let size = norStr.boundingRect(with: CGSize(width: CGFloat(MAXFLOAT), height: maxHeight), options: .usesLineFragmentOrigin, attributes: attributes as [NSAttributedString.Key : Any], context: nil)
        return size.width
    }
    
    private func calcuateLabSizeHeight(str : String, font : UIFont, maxWidth : CGFloat) -> CGFloat{
        let attributes = [kCTFontAttributeName: font]
        let norStr = NSString(string: str)
        let size = norStr.boundingRect(with: CGSize(width: maxWidth, height: CGFloat(MAXFLOAT)), options: .usesLineFragmentOrigin, attributes: attributes as [NSAttributedString.Key : Any], context: nil)
        return size.height + 15
    }
}
