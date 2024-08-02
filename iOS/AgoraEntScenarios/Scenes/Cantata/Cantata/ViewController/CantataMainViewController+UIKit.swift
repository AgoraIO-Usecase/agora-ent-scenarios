//
//  CantataMainViewController+UIKit.swift
//  Cantata
//
//  Created by CP on 2023/9/13.
//

import Foundation
import AUIKitCore

private let kChartIds = [3, 4, 2, 6]
let kListPageCount: Int = 10
extension CantataMainViewController: AUIJukeBoxViewDelegate {
    
    //这个用户应该是无感知的
    public func cleanSearchText(view: AUIJukeBoxView) {
        self.searchMusicList = nil
        self.searchKeyWord = nil
    }
    
    public func search(view: AUIJukeBoxView, text: String, completion: @escaping ([AUIJukeBoxItemDataProtocol]?)->()) {
        self.searchKeyWord = text
        self.searchMusic(keyword: text, page: 1, pageSize: kListPageCount, completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUIToast.show(text:err.localizedDescription)
                return
            }
            self.searchMusicList = list ?? []
            completion(list)
        })
    }
    
    public func onSegmentedChanged(view: AUIJukeBoxView, segmentIndex: Int) -> Bool {
        return false
    }
    
    public func onTabsDidChanged(view: AUIJukeBoxView, tabIndex: Int) -> Bool {
        return false
    }
    
    public func onSelectSong(view: AUIJukeBoxView, tabIndex: Int, index: Int) {
        //先判断自己是否在麦位上
//        if getCurrentUserMicSeat() == nil {
//            let count = seatsArray?.count ?? 0
//            for i in 0..<count {
//                let seat = seatsArray?[i]
//                let rtcUid = seat?.rtcUid ?? ""
//                if rtcUid == "" {
//                    self.enterSeat(withIndex: i) {[weak self] error in
//                        guard let self = self else {return}
//                        if let error = error {
//                            return
//                        }
//                        selectSong(index, tabIndex: tabIndex)
//                    }
//
//                    return
//                }
//            }
//        }
        selectSong(index, tabIndex: tabIndex)
    }
    
    public func onRemoveSong(view: AUIJukeBoxView, index: Int) {
        removeSong(index)
    }
    
    public func onNextSong(view: AUIJukeBoxView, index: Int) {
        AUIAlertView.theme_defaultAlert()
            .isShowCloseButton(isShow: false)
            .title(title: aui_localized("switchToNextSong"))
            .rightButton(title: "确认")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                self.onRemoveSong(view: view, index: index)
            })
            .leftButton(title: "取消")
            .show()
    }
    
    public func onPinSong(view: AUIJukeBoxView, index: Int) {
        let song = self.addedMusicList[index]
        self.pinSong(songCode: song.songCode, completion: { error in
            guard let err = error else {return}
            AUIToast.show(text:err.localizedDescription)
        })
    }
    
    public func songIsSelected(view: AUIJukeBoxView, songCode: String) -> Bool {
        return addedMusicSet.contains(songCode)
    }
    
    public func pingEnable(view: AUIJukeBoxView, songCode: String) -> Bool {
        return pinEnableSet.contains(songCode)
    }
    
    public func deleteEnable(view: AUIJukeBoxView, songCode: String) -> Bool {
        return deleteEnableSet.contains(songCode)
    }
    
    public func getSearchMusicList(view: AUIJukeBoxView) -> [AUIJukeBoxItemDataProtocol]? {
        return self.searchMusicList
    }
    
    public func getMusicList(view: AUIJukeBoxView, tabIndex: Int) -> [AUIJukeBoxItemDataProtocol] {
        return self.musicListMap[tabIndex] ?? []
    }
    
    public func getSelectedSongList(view: AUIJukeBoxView) -> [AUIJukeBoxItemSelectedDataProtocol] {
        return self.addedMusicList
    }
    
    public func onRefreshMusicList(view: AUIJukeBoxView, tabIndex: Int, completion: @escaping ([AUIJukeBoxItemDataProtocol]?)->()) {
        let idx = tabIndex
        aui_info("onRefreshMusicList tabIndex: \(idx)", tag: "AUIJukeBoxViewBinder")
        self.getMusicList(chartId: kChartIds[idx],
                                           page: 1,
                                           pageSize: kListPageCount,
                                           completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUIToast.show(text:err.localizedDescription)
                return
            }
            self.musicListMap[idx] = list ?? []
            completion(list)
        })
    }
    
    public func onLoadMoreMusicList(view: AUIJukeBoxView, tabIndex: Int, completion: @escaping ([AUIJukeBoxItemDataProtocol]?)->()) {
        
        //如果searchMusicList为空，表示为id搜索，否则为关键字搜索
        if let searchList = self.searchMusicList, let keyword = self.searchKeyWord {
            let page = 1 + searchList.count / kListPageCount
            self.searchMusic(keyword: keyword, page: page, pageSize: kListPageCount, completion: {[weak self] error, list in
                guard let self = self else {return}
                if let err = error {
                    AUIToast.show(text:err.localizedDescription)
                    return
                }
                self.searchMusicList? += list ?? []
                completion(list)
            })
            return
        }

        let idx = tabIndex
        let musicListCount = musicListMap[idx]?.count ?? 0
        let page = 1 + musicListCount / kListPageCount
        if musicListCount % kListPageCount > 0 {
            //no more data
            completion(nil)
            return
        }
        aui_info("onLoadMoreMusicList tabIndex: \(idx) page: \(page)", tag: "AUIJukeBoxViewBinder")
        self.getMusicList(chartId: kChartIds[idx],
                                           page: page,
                                           pageSize: kListPageCount,
                                           completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUIToast.show(text:err.localizedDescription)
                return
            }
            if let list = list {
                let musicList = self.musicListMap[idx] ?? []
                self.musicListMap[idx] = musicList + list
            }
            completion(list)
        })
    }
    
    public func onRefreshAddedMusicList(view: AUIJukeBoxView, completion: @escaping ([AUIJukeBoxItemSelectedDataProtocol]?) -> ()) {
        aui_info("onRefreshAddedMusicList", tag: "AUIJukeBoxViewBinder")
        //暂时不需要上拉刷新
    }

    public func pinSong(songCode: String, completion: AUICallback?) {
        aui_info("pinSong: \(songCode)", tag: "AUIMusicServiceImpl")
        guard let selSongArray = self.selSongArray else { return }

        var pinModel = KTVMakeSongTopInputModel()
        guard let songIndex = selSongArray.firstIndex(where: { $0.songNo == songCode }) else {return}
        
        pinModel.objectId = selSongArray[songIndex].objectId
        pinModel.songNo = selSongArray[songIndex].songNo
        
        var pinAble = true
        AppContext.dhcServiceImp().pinSong(with: pinModel, completion: { [weak self] err in
            if pinAble == false {return}
            
            pinAble = false
            guard let self = self else { return }
            
            if err == nil {
                self.addedMusicList.swapAt(1, songIndex)
                
                // 更新pinset
                if isRoomOwner {
                    pinEnableSet.add(self.addedMusicList[songIndex].songCode)
                }
                
                self.jukeBoxView.addedMusicTableView.reloadData()
                self.jukeBoxView.allMusicTableView.reloadData()
                completion?(nil)
            }
        })
    }
    
    public func searchMusic(keyword: String,
                            page: Int,
                            pageSize: Int,
                            completion: @escaping AUIMusicListCompletion) {
        aui_info("searchMusic with keyword: \(keyword)", tag: "AUIMusicServiceImpl")
        let jsonOption = "{\"needLyric\":true,\"pitchType\":1}"
        self.ktvApi.searchMusic(keyword: keyword,
                                page: page,
                                pageSize: pageSize,
                                jsonOption: jsonOption) { requestId, status, collection in
            aui_info("searchMusic with keyword: \(keyword) status: \(status.rawValue) count: \(collection.count)", tag: "AUIMusicServiceImpl")
            guard status == .OK else {
                //TODO:
                DispatchQueue.main.async {
                    completion(nil, nil)
                }
                return
            }
            
            var musicList: [AUIMusicModel] = []
            collection.musicList.forEach { music in
                let model = AUIMusicModel()
                model.songCode = "\(music.songCode)"
                model.name = music.name
                model.singer = music.singer
                model.poster = music.poster
//                model.releaseTime = music.releaseTime
                model.duration = music.durationS
                musicList.append(model)
            }
            
            DispatchQueue.main.async {
                completion(nil, musicList)
            }
        }
    }

    public func getMusicList(chartId: Int,
                             page: Int,
                             pageSize: Int,
                             completion: @escaping AUIMusicListCompletion) {
        aui_info("getMusicList with chartId: \(chartId)", tag: "AUIMusicServiceImpl")
        let jsonOption = "{\"needLyric\":true,\"pitchType\":1}"
        self.ktvApi.searchMusic(musicChartId: chartId,
                                page: page,
                                pageSize: pageSize,
                                jsonOption: jsonOption) { requestId, status, collection in
            aui_info("getMusicList with chartId: \(chartId) status: \(status.rawValue) count: \(collection.count)", tag: "AUIMusicServiceImpl")
            guard status == .OK else {
                //TODO:
                DispatchQueue.main.async {
                    completion(nil, nil)
                }
                return
            }
            
            var musicList: [AUIMusicModel] = []
            collection.musicList.forEach { music in
                let model = AUIMusicModel()
                model.songCode = "\(music.songCode)"
                model.name = music.name
                model.singer = music.singer
                model.poster = music.poster
//                model.releaseTime = music.releaseTime
                model.duration = music.durationS
                musicList.append(model)
            }
            
            DispatchQueue.main.async {
                completion(nil, musicList)
            }
        }
    }
    
    //将选择歌曲的模型转换为UIKit的模型 进行数据的传输显示
    public func getAllChooseSongList(completion: AUIChooseSongListCompletion?) {
        aui_info("getAllChooseSongList", tag: "AUIMusicServiceImpl")
        
        var songList = [AUIChooseMusicModel]()
        guard let selSongArray = self.selSongArray else {return}
        for i in selSongArray {
            let model = AUIChooseMusicModel()
            model.songCode = i.songNo ?? ""
            model.createAt = i.createAt
            model.singer = i.singer ?? ""
            model.poster = i.imageUrl ?? ""
            model.name = i.songName ?? ""
            model.createAt = i.createAt
            model.pinAt = i.pinAt
            model.status =  i.status == .idle ? 0 : 1
            let owner = AUIUserThumbnailInfo()
            owner.userId = i.userNo ?? ""
            owner.userName = i.name ?? ""
            model.owner = owner
            songList.append(model)
        }
        
        self.chooseSongList = songList
        completion?(nil, self.chooseSongList)
    }
    
    public func _notifySongDidAdded(song: AUIChooseMusicModel) {
        addedMusicSet.add(song.songCode)
        if isRoomOwner == true {
            deleteEnableSet.add(song.songCode)
            pinEnableSet.add(song.songCode)
        } else if song.owner?.userId == VLUserCenter.user.id {
            deleteEnableSet.add(song.songCode)
        }
    }
    public func _notifySongDidRemove(song: AUIChooseMusicModel) {
        addedMusicSet.remove(song.songCode)
        deleteEnableSet.remove(song.songCode)
        pinEnableSet.remove(song.songCode)
    }
    
    private func selectSong(_ index: Int, tabIndex: Int) {
        guard let model = searchMusicList == nil ? musicListMap[tabIndex]?[index] : searchMusicList?[index] else {return}
        let inputModel = KTVChooseSongInputModel()
        inputModel.songNo = model.songCode
        inputModel.songName = model.name
        inputModel.singer = model.singer
        inputModel.imageUrl = model.poster
        
        if beforeAddSet.contains(model.songCode) {return}
        beforeAddSet.add(model.songCode)
        AppContext.dhcServiceImp().chooseSong(with: inputModel, completion: { err in
            self.beforeAddSet.remove(inputModel.songNo)
            if err == nil {
//                let addModel: AUIChooseMusicModel = AUIChooseMusicModel()
//                addModel.songCode = model.songCode
//                addModel.name = model.name
//                addModel.singer = model.singer
//                addModel.poster = model.poster
//                addModel.duration = model.duration
//
//                let owner = AUIUserThumbnailInfo()
//                owner.userId = VLUserCenter.user.id
//                addModel.owner = owner
//                self.addedMusicList.append(addModel)
//                self._notifySongDidAdded(song: addModel)
//                self.jukeBoxView.addedMusicTableView.reloadData()
//                self.jukeBoxView.allMusicTableView.reloadData()
            }
        })
        
    }
    
    public func removeSong(_ index: Int) {
        print("我要删除歌曲了")
        let song = self.addedMusicList[index]
        let removeModel = KTVRemoveSongInputModel()
        removeModel.songNo = song.songCode
        
        guard let selSongArray = self.selSongArray else {return}
        for i in selSongArray {
            if i.songNo == song.songCode {
                removeModel.objectId = i.objectId
                AppContext.dhcServiceImp().removeSong(with: removeModel, completion: { err in
                    if err == nil {
//                        if self.addedMusicList.count != 0 {
//                            self.addedMusicList.remove(at: index)
//                        }
//                        self._notifySongDidRemove(song: song)
//                        self.jukeBoxView.addedMusicTableView.reloadData()
//                        self.jukeBoxView.allMusicTableView.reloadData()
                    }
                })
                return
            }
        }
    }
}
