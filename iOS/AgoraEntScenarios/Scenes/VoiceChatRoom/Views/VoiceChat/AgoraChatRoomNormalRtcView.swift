//
//  AgoraChatRoomRtcView.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/29.
//

import SnapKit
import UIKit

class AgoraChatRoomNormalRtcView: UIView {
    private var collectionView: UICollectionView!
    private let nIdentifier = "normal"
    private let aIdentifier = "alien"

    var clickBlock: ((AgoraChatRoomBaseUserCellType, Int) -> Void)?

    var isOwner: Bool = false

    var micInfos: [VRRoomMic]? {
        didSet {
            guard let _ = collectionView else {
                return
            }
            collectionView.reloadData()
        }
    }

    override func draw(_ rect: CGRect) {
        // Drawing code
        if collectionView == nil {
            layoutUI()
        }
    }

    public func updateVolume(with index: Int, vol: Int) {
        let indexPath = IndexPath(item: index, section: 0)
        guard let cell: AgoraChatRoomBaseUserCollectionViewCell = collectionView.cellForItem(at: indexPath) as? AgoraChatRoomBaseUserCollectionViewCell else { return }
        DispatchQueue.main.async {
            cell.refreshVolume(vol: vol)
        }
    }

    public func updateUser(_ mic: VRRoomMic) {
        let indexPath = IndexPath(item: mic.mic_index, section: 0)
        guard let cell: AgoraChatRoomBaseUserCollectionViewCell = collectionView.cellForItem(at: indexPath) as? AgoraChatRoomBaseUserCollectionViewCell else { return }
        DispatchQueue.main.async {
            cell.refreshUser(with: mic)
        }
    }

    public func updateAlien(_ status: Int) {
        let indexPath = IndexPath(item: 6, section: 0)
        guard let cell: AgoraChatRoomBaseAlienCollectionViewCell = collectionView.cellForItem(at: indexPath) as? AgoraChatRoomBaseAlienCollectionViewCell else { return }
        DispatchQueue.main.async {
            cell.refreshAlien(with: status)
        }
    }

    public func updateAlienMic(_ type: ALIEN_TYPE) {
        let indexPath = IndexPath(item: 6, section: 0)
        guard let cell: AgoraChatRoomBaseAlienCollectionViewCell = collectionView.cellForItem(at: indexPath) as? AgoraChatRoomBaseAlienCollectionViewCell else { return }
        DispatchQueue.main.async {
            cell.updateAlienMic(with: type)
        }
    }

    private func layoutUI() {
        let flowLayout = UICollectionViewFlowLayout()
        flowLayout.itemSize = CGSize(width: bounds.size.width / 4.0, height: 120)
        flowLayout.minimumLineSpacing = 0
        flowLayout.minimumInteritemSpacing = 0
        flowLayout.scrollDirection = .vertical

        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
        collectionView.register(AgoraChatRoomBaseUserCollectionViewCell.self, forCellWithReuseIdentifier: nIdentifier)
        collectionView.register(AgoraChatRoomBaseAlienCollectionViewCell.self, forCellWithReuseIdentifier: aIdentifier)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = .clear

        self.collectionView = collectionView
        addSubview(collectionView)
        self.collectionView.snp.makeConstraints { make in
            make.left.top.bottom.right.equalTo(self)
        }
    }
}

extension AgoraChatRoomNormalRtcView: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return 7
    }

    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        if indexPath.item < 6 {
            return CGSize(width: bounds.size.width / 4.0, height: 120)
        } else {
            return CGSize(width: bounds.size.width / 2.0, height: 120)
        }
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        if indexPath.item < 6 {
            let cell: AgoraChatRoomBaseUserCollectionViewCell = collectionView.dequeueReusableCell(withReuseIdentifier: nIdentifier, for: indexPath) as! AgoraChatRoomBaseUserCollectionViewCell
            cell.tag = indexPath.item + 200
            cell.clickBlock = { [weak self] tag in
                print("------\(tag)-----\(cell.tag))")
                guard let block = self?.clickBlock else { return }
                block(cell.cellType, tag)
            }
            /*
             0: 正常 1: 闭麦 2: 禁言 3: 锁麦 4: 锁麦和禁言 -1: 空闲
             */
            if let mic_info = micInfos?[safe:indexPath.row] {
                DispatchQueue.main.async {
                    cell.refreshUser(with: mic_info)
                }
            } else {
                cell.cellType = .AgoraChatRoomBaseUserCellTypeAdd
            }
            return cell
        } else {
            let cell: AgoraChatRoomBaseAlienCollectionViewCell = collectionView.dequeueReusableCell(withReuseIdentifier: aIdentifier, for: indexPath) as! AgoraChatRoomBaseAlienCollectionViewCell
            if let mic_info = micInfos?[safe:indexPath.row] {
                cell.cellType = mic_info.status == 5 ? .AgoraChatRoomBaseUserCellTypeActived : .AgoraChatRoomBaseUserCellTypeNonActived
            }

            cell.clickVBlock = {
                guard let clickBlock = self.clickBlock else { return }
                clickBlock(.AgoraChatRoomBaseUserCellTypeAlienActive, 206)
            }
            return cell
        }
    }
}
