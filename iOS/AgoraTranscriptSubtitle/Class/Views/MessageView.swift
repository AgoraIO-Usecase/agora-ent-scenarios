//
//  MessageView.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/17.
//

import UIKit

class MessageView: UIView {
    private let logTag = "MessageView"
    var finalTextColor: UIColor = .black
    var nonFinalTextColor: UIColor = .gray
    var textAreaBackgroundColor: UIColor = UIColor.black.withAlphaComponent(0.25)
    var textFont: UIFont = .systemFont(ofSize: 16)
    private let tableView = UITableView(frame: .zero, style: .grouped)
    private var items = [RenderInfo]()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        commonInit()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        Log.debug(text: "deinit", tag: logTag)
    }
    
    private func setupUI() {
        backgroundColor = .clear
        tableView.backgroundColor = .clear
        tableView.separatorStyle = .none
        tableView.allowsSelection = false
        tableView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(tableView)
        tableView.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        tableView.rightAnchor.constraint(equalTo: rightAnchor).isActive = true
        tableView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        tableView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }
    
    private func commonInit() {
        tableView.register(MessageCell.self, forCellReuseIdentifier: "cell")
        tableView.delegate = self
        tableView.dataSource = self
    }
    
    func addOrUpdate(renderInfo: RenderInfo) {
        if items.contains(where: { $0.identifier == renderInfo.identifier }) {
            for index in 0..<items.count {
                if items[index].identifier == renderInfo.identifier {
                    items[index] = renderInfo
                    let indexPath = IndexPath(row: index, section: 0)
                    UIView.performWithoutAnimation {
                        tableView.reloadRows(at: [indexPath], with: .fade)
                    }
                    if indexPath.row == items.count - 1 { /** only scroll to last **/
                        tableView.scrollToRow(at: indexPath, at: .bottom, animated: true)
                    }
                    return
                }
            }
        }
        
        items.append(renderInfo)
        let indexPath = IndexPath(row: items.count - 1, section: 0)
        UIView.performWithoutAnimation {
            tableView.insertRows(at: [indexPath], with: .fade)
        }
        tableView.scrollToRow(at: indexPath, at: .bottom, animated: true)
    }
    
    func clear() {
        items = []
        tableView.reloadData()
    }
    
    private func makeAttributedText(item: RenderInfo) -> NSAttributedString {
        let transcriptText = NSMutableAttributedString(string: item.transcriptText)
        for range in item.transcriptRanges {
            let color: UIColor = range.isFinal ? finalTextColor : nonFinalTextColor
            transcriptText.addAttribute(.foregroundColor, value: color, range: range.range)
        }
        
        let translateText = NSMutableAttributedString()
        for (index, translateRenderInfo) in item.translateRenderInfos.enumerated() {
            let attributedString = NSMutableAttributedString(string: translateRenderInfo.text)
            for range in translateRenderInfo.ranges {
                let color: UIColor = range.isFinal ? finalTextColor : nonFinalTextColor
                attributedString.addAttribute(.foregroundColor, value: color, range: range.range)
            }
            if index != item.translateRenderInfos.count - 1 {
                attributedString.append(NSAttributedString(string: "\n"))
            }
            translateText.append(attributedString)
        }
        
        let allText = NSMutableAttributedString(string: "")
        if !item.transcriptText.isEmpty {
            allText.append(transcriptText)
        }
        
        if !translateText.string.isEmpty, !item.transcriptText.isEmpty {
            allText.append(NSAttributedString(string: "\n"))
        }
        
        if !translateText.string.isEmpty {
            allText.append(translateText)
        }
        
        if allText.string.isEmpty { /** set a placeholder text if allText is empty **/
            allText.append(NSAttributedString(string: "      "))
        }
        
        return allText
    }
}

extension MessageView: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return items.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! MessageCell
        let item = items[indexPath.row]
        let attributedText = makeAttributedText(item: item)
        cell.label.font = textFont
        cell.label.attributedText = attributedText
        cell.bgView.backgroundColor = textAreaBackgroundColor
        return cell
    }
}
