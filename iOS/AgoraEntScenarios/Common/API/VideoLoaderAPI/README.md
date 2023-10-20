# 秒切/秒开场景化API

本文档主要介绍如何快速集成秒切/秒开场景化API

## 1.环境准备
- Xcode 13.0及以上版本
- 最低支持系统：iOS 10.0
- 请确保您的项目已设置有效的开发者签名

## 2.最佳实践
### 秒开
- 房间列表页面preloadChannel
- 触碰到房间Item 加入频道但不订阅音视频
- 加入频道后尽早的设置远端渲染画面的view
- 松手订阅音视频
- 触碰到房间但未完成点击 退出频道
### 秒切
- 加入房间后上下房间join rtc但是不订阅音视频流
- 加入频道后尽早的设置远端渲染画面的view
- 滑动后订阅可视区域房间的视频流,
- 停止滑动后订阅可视区域的房间音视频流，取消不在重用cell列表里的房间音视频流并leave rtc
- 万能Token 节省几百ms的耗时(可选: 取决于客户对于业务安全性的诉求)

## 3.快速接入

- 把示例代码的目录VideoLoaderAPI拷贝至自己的工程里，例如与Podfile文件同级
- 在Podfile文件里加入
  ```
  pod 'VideoLoaderAPI', :path => './VideoLoaderAPI'
  ```
- 打开终端，执行`pod install`命令，秒切/秒开API即可集成进项目里
- 设置config
  ```swift
  let engine = AgoraRtcEngineConfig()
  ...

  let api = VideoLoaderApiImpl.shared
  let config = VideoLoaderConfig()
  config.rtcEngine = _createRtcEngine()
  api.setup(config: config)
  ```
- 秒开设置
  - 1.通过按钮点击+ScrollView方式集成
    - 设置preload
      ```swift
      let preloadRoomList = [...]
      VideoLoaderApiImpl.shared.preloadRoom(preloadRoomList: preloadRoomList)
      ```
    - 对按钮设置预加载点击处理
      ```swift
      let button = UIButton()
      ...

      //设置秒开事件处理
      button.ag_addPreloadTap(localUid: kCurrentUid, roomInfo: info)
      ```
  - 2.通过UICollectionView方式集成
    - 继承AGCollectionLoadingDelegateHandler处理点击
        ```swift
        class TestCollectionLoadingDelegateHandler: AGCollectionLoadingDelegateHandler {
            var selectClosure: ((IndexPath)->())?
            open override func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
                super.collectionView(collectionView, didSelectItemAt: indexPath)
                selectClosure?(indexPath)
            }
        }
        ```
    - 把handler绑定对应的collectionView
        ```swift
        let collectionView = UICollectionView()
        ...

        //创建handler实例
        let delegateHandler = TestCollectionLoadingDelegateHandler()
        //设置当前用户的uid
        handler.localUid = 123
        //设置房间列表
        delegateHandler.roomList = roomList
        //绑定handler
        collectionView.delegate = self.delegateHandler
        ```
  - 3.通过UITableView方式集成
    - 继承AGTableLoadingDelegateHandler
        ```swift
        class TestTableLoadingDelegateHandler: AGTableLoadingDelegateHandler {
            var selectClosure: ((IndexPath)->())?

            //处理点击
            open override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
                super.tableView(tableView, didSelectRowAt: indexPath)
                selectClosure?(indexPath)
            }
        }
        ```


    - 把handler绑定对应的collectionView
        ```swift
        let tableView = UITableView()
        ...

        //创建handler实例
        let delegateHandler = TestTableLoadingDelegateHandler()
        //设置当前用户的uid
        handler.localUid = 123
        //设置房间列表
        delegateHandler.roomList = roomList
        //绑定handler
        tableView.delegate = self.delegateHandler
        ```
- 秒切
  - 1.通过UICollectionView方式集成
    - 继承AGTableLoadingDelegateHandler处理自定义cell
        ```swift
        class TestCollectionViewDelegateHandler: AGCollectionSlicingDelegateHandler {

            //设置自定义cell
            override func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
                let cell = super.collectionView(collectionView, cellForItemAt: indexPath) as! TestRoomCollectionViewCell
                let idx = realCellIndex(with: indexPath.row)
                let room = self.roomList![idx]
                cell.titleLabel.text = "roomId:\(room.roomId)\n index:\(idx)"
                return cell
            }


            //设置渲染画布
            override func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
                super.collectionView(collectionView, willDisplay: cell, forItemAt: indexPath)
        
                let idx = indexPath.row
                guard let cell = cell as? ThumbRoomCollectionViewCell,
                      let room = self.roomList?[idx].getRoomInfo() else {return}
                let container = VideoCanvasContainer()
                container.uid = room.uid
                container.container = cell.canvasView
                VideoLoaderApiImpl.shared.renderVideo(roomInfo: room, container: container)
            }
        }
        ```
    - 把handler绑定对应的collectionView
        ```swift
        let collectionView = UICollectionView()
        ...

        //创建handler实例
        let delegateHandler = TestCollectionViewDelegateHandler()

        //设置uid
        delegateHandler.localUid = kCurrentUid
        //设置房间列表
        delegateHandler.roomList = self.roomList
        //设置初始展示的index，用于预加载处理
        delegateHandler.initIndex = focusIndex

        //绑定handler
        collectionView.delegate = delegateHandler
        collectionView.dataSource = delegateHandler

        ```
  - 2.通过UITableView方式集成
    - 继承AGTableLoadingDelegateHandler处理自定义cell
        ```swift
        class TestTableViewDelegateHandler: AGTableSlicingDelegateHandler {

            //设置自定义cell
            open override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
                var cell = tableView.dequeueReusableCell(withIdentifier: kUIListViewCellIdentifier) as? TestRoomTableViewCell
                if cell == nil {
                    cell = TestRoomTableViewCell(style: .default, reuseIdentifier: kUIListViewCellIdentifier)
                }
                let idx = realCellIndex(with: indexPath.row)
                let room = self.roomList![idx]
                cell?.titleLabel.text = "roomId:\(room.roomId) index: \(indexPath.row)\n index\(idx)"
                return cell!
            }


            //设置渲染画布
            override func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
                super.tableView(tableView, willDisplay: cell, forRowAt: indexPath)
        
                let idx = indexPath.row
                guard let cell = cell as? ThumbRoomTableViewCell,
                      let room = self.roomList?[idx].getRoomInfo() else {return}
                let container = VideoCanvasContainer()
                container.uid = room.uid
                container.container = cell.canvasView
                VideoLoaderApiImpl.shared.renderVideo(roomInfo: room, container: container)
            }
        }
        ```
    - 把handler绑定对应的tableView
        ```swift
        let tableView = UITableView()
        ...

        //创建handler实例
        let delegateHandler = TestTableViewDelegateHandler()

        //设置uid
        delegateHandler.localUid = kCurrentUid
        //设置房间列表
        delegateHandler.roomList = self.roomList
        //设置初始展示的index，用于预加载处理
        delegateHandler.initIndex = focusIndex

        //绑定handler
        tableView.delegate = delegateHandler
        tableView.dataSource = delegateHandler

        ```
- 离开秒切房间后清理缓存
    ```swift
    VideoLoaderApiImpl.shared.cleanCache()
    ```
## 4.FAQ

### 如何获取声网APPID

> - 声网APPID申请：[https://www.agora.io/cn/](https://www.agora.io/cn/)
> 
### 集成遇到困难，该如何联系声网获取协助

> 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
> 
> 方案2：发送邮件给[support@agora.io](mailto:support@agora.io)咨询