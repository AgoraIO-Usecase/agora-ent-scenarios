//
//  VRSoundEffectsList.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/26.
//

import QuartzCore
import UIKit
import ZSwiftBaseLib

public class VRSoundEffectsList: UITableView, UITableViewDelegate, UITableViewDataSource {
    var type = "Social Chat"

    static var heightMap = [String: CGFloat]()

    private var datas = VRSoundEffectsCell.items()

    override public init(frame: CGRect, style: UITableView.Style) {
        super.init(frame: frame, style: style)
        for item in datas {
            VRSoundEffectsList.heightMap[item.title] = item.detail.z.sizeWithText(font: .systemFont(ofSize: 13, weight: .regular), size: CGSize(width: ScreenWidth - 80, height: 999)).height
        }
        delegate(self).dataSource(self).registerCell(VRSoundEffectsCell.self, forCellReuseIdentifier: "VRSoundEffectsCell")
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    public func numberOfSections(in tableView: UITableView) -> Int {
        1
    }

    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        datas.count
    }

    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let item = datas[indexPath.row]
        return VRSoundEffectsList.heightMap[item.title]! + 20 + 102.5
    }

    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "VRSoundEffectsCell", for: indexPath) as? VRSoundEffectsCell
        cell?.refresh(item: datas[safe: indexPath.row]!)
        cell?.selectionStyle = .none
        return cell ?? VRSoundEffectsCell()
    }

    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: false)
        for item in datas { item.selected = false }
        datas[safe: indexPath.row]?.selected = true
        type = datas[safe: indexPath.row]?.soundType ?? ""
        reloadData()
    }
}

public extension CATransaction {
    class func disableAnimations(_ completion: () -> Void) {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        completion()
        CATransaction.commit()
    }
}
