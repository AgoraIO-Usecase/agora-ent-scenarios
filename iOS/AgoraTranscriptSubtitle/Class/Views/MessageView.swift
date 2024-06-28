//
//  MessageView.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/17.
//

import UIKit

class MessageView: UIView {
    typealias RenderInfo = TranscriptSubtitleMachine.RenderInfo
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
    
    func addItem(renderInfo: RenderInfo) {
        items.append(renderInfo)
        let indexPath = IndexPath(row: items.count - 1, section: 0)
        
        
        UIView.performWithoutAnimation {
            tableView.insertRows(at: [indexPath], with: .fade)
        }
        tableView.scrollToRow(at: indexPath, at: .bottom, animated: true)
    }
    
    func updateLast(renderInfo: RenderInfo) {
        guard let (offset, _) = items.enumerated().map({ ($0.offset, $0.element) }).last(where: { $0.1.identifier == renderInfo.identifier }) else {
            Log.debug(text: "can not find last: \(renderInfo.identifier)", tag: logTag)
            return
        }
        let indexPath = IndexPath(row: offset, section: 0)
        items[offset] = renderInfo
        tableView.reloadRows(at: [indexPath], with: .fade)
    }
}

extension MessageView: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return items.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! MessageCell
        let item = items[indexPath.row]
        
        let transcriptText = NSMutableAttributedString(string: item.transcriptText)
        for range in item.transcriptRanges {
            let color: UIColor = range.isFinal ? finalTextColor : nonFinalTextColor
            transcriptText.addAttribute(.foregroundColor, value: color, range: range.range)
        }
        
        let translateText = NSMutableAttributedString(string: item.translateText)
        for range in item.translateRanges {
            let color: UIColor = range.isFinal ? finalTextColor : nonFinalTextColor
            translateText.addAttribute(.foregroundColor, value: color, range: range.range)
        }
        cell.transcriptLabel.font = textFont
        cell.translateLabel.font = textFont
        cell.transcriptLabel.attributedText = transcriptText
        cell.translateLabel.attributedText = translateText
        cell.bgView.backgroundColor = textAreaBackgroundColor
        return cell
    }
}
