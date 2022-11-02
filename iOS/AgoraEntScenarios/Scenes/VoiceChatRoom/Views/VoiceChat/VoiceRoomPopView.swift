//
//  VoiceRoomPopView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/8.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomPopView: UIView, UITableViewDelegate, UITableViewDataSource {
    var countClosure: ((String) -> Void)?

    let datas = [VoiceRoomGiftCount(number: 999, selected: false), VoiceRoomGiftCount(number: 599, selected: false), VoiceRoomGiftCount(number: 199, selected: false), VoiceRoomGiftCount(number: 99, selected: false), VoiceRoomGiftCount(number: 9, selected: false), VoiceRoomGiftCount(number: 1, selected: true)]

    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        datas.count
    }

    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "VoiceRoomPopViewCell") as? VoiceRoomPopViewCell
        if cell == nil {
            cell = VoiceRoomPopViewCell(style: .default, reuseIdentifier: "VoiceRoomPopViewCell")
        }
        cell?.selectionStyle = .none
        let item = datas[indexPath.row]
        cell?.content.text = "\(item.number)"
        cell?.content.backgroundColor = (item.selected == true ? UIColor(0xF1DDFF) : UIColor.white)
        cell?.content.textColor = (item.selected == true ? UIColor(0x009FFF) : .black)
        return cell!
    }

    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        datas.forEach { $0.selected = false }
        datas[safe: indexPath.row]?.selected = true
        if countClosure != nil {
            countClosure!("\(datas[indexPath.row].number)")
        }
        popList.reloadData()
    }

    lazy var popList: UITableView = .init(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height), style: .plain).delegate(self).dataSource(self).tableFooterView(UIView()).separatorStyle(.none).registerCell(VoiceRoomPopViewCell.self, forCellReuseIdentifier: "VoiceRoomPopViewCell").rowHeight(29)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(popList)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public class VoiceRoomPopViewCell: UITableViewCell {
    lazy var content: UILabel = .init(frame: CGRect(x: 0, y: 0, width: self.contentView.frame.width, height: self.contentView.frame.height)).cornerRadius(6).font(.systemFont(ofSize: 14, weight: .regular)).textColor(.darkText).textAlignment(.center)

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        contentView.addSubview(content)
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        content.frame = CGRect(x: 0, y: 0, width: contentView.frame.width, height: contentView.frame.height)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
