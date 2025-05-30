//
//  SudGameManager.h
//  QuickStart
//
//  Created by kaniel on 2024/1/12.
//  Copyright © 2024 Sud.Tech (https://sud.tech). All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SudMGPWrapper.h"
#import "SudGameBaseEventHandler.h"
NS_ASSUME_NONNULL_BEGIN

/// SUD 游戏管理模块
@interface SudGameManager : NSObject

/// 一：注册游戏事件监听
/// - Parameter eventHandler: eventHandler 游戏事件监听对象，所有游戏与app交互事件从这里回调给listener，备注：内部不强制引用，外部必须强持有该handler
/// Register a game event handler to listen for game events.
/// @param eventHandler The event handler object that will receive callbacks for game events. Note: References are not mandatory internally, and this handler must be held externally
- (void)registerGameEventHandler:(SudGameBaseEventHandler *)eventHandler;

/// 二：加载游戏
/// 接入方客户端 调用 接入方服务端 getCode: 获取 短期令牌code
/// 参考文档时序图：sud-mgp-doc(https://github.com/SudTechnology/sud-mgp-doc)
/// 执行步骤：
/// 1. 请求业务服务接口获取游戏初始化SDK需要的code码<getCode>
/// 2. 初始化SudMGP SDK<SudMGP initSDK>
/// 3. 加载SudMGP SDK<SudMGP loadMG>
/// Step 2: Game Login
/// The client-side of the integration calls the getCode method on the server-side of the integration to obtain a short-term token code.
/// Please refer to the sequence diagram in the documentation: sud-mgp-doc (https://github.com/SudTechnology/sud-mgp-doc)
/// Execution Steps:
/// 1. Request the business service API to obtain the code required for initializing the game SDK (getCode).
/// 2. Initialize the SudMGP SDK using the initSDK method.
/// 3. Load the SudMGP SDK using the loadMG method.
- (void)loadGame:(SudGameLoadConfigModel *)configModel;

/// 三：销毁游戏 销毁SudMGP SDK
/// Step 3: Game Logout
/// This step is responsible for exiting the game and destroying the SudMGP SDK.
- (void)destroyGame;

@end

NS_ASSUME_NONNULL_END
