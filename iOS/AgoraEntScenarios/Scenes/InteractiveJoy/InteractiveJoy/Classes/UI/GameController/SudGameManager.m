//
//  SudGameManager.m
//  QuickStart
//
//  Created by kaniel on 2024/1/12.
//  Copyright © 2024 Sud.Tech (https://sud.tech). All rights reserved.
//

#import "SudGameManager.h"
#import <SudMGP/SudInitSDKParamModel.h>
#import <SudMGP/SudLoadMGParamModel.h>

@interface SudGameManager()
/// 游戏事件处理对象
/// Game event handling object
@property(nonatomic, weak)SudGameBaseEventHandler *sudGameEventHandler;
@end

@implementation SudGameManager

- (void)dealloc {
    NSLog(@"SudGameManager dealloc");
}

#pragma mark --- public

- (void)registerGameEventHandler:(SudGameBaseEventHandler *)eventHandler {
    self.sudGameEventHandler = eventHandler;
    [self.sudGameEventHandler.sudFSMMGDecorator setEventListener:eventHandler];
}

- (void)loadGame:(nonnull SudGameLoadConfigModel *)configModel {
    NSAssert(self.sudGameEventHandler, @"Must registerGameEventHandler before!");
    if (self.sudGameEventHandler) {
        __weak typeof(self) weakSelf = self;
        [self.sudGameEventHandler onGetCode:configModel.userId result:^(NSString * _Nonnull code) {
            NSLog(@"on getCode success");
            [weakSelf initSudMGPSDK:configModel code:code];
        }];
    }
}

- (void)destroyGame {
    NSAssert(self.sudGameEventHandler, @"Must registerGameEventHandler before!");
    [self.sudGameEventHandler.sudFSMMGDecorator clearAllStates];
    [self.sudGameEventHandler.sudFSTAPPDecorator destroyMG];
}

#pragma mark --- private

/// 初始化游戏SudMDP SDK
- (void)initSudMGPSDK:(SudGameLoadConfigModel *)configModel code:(NSString *)code {

    __weak typeof(self) weakSelf = self;
    if (configModel.gameId <= 0) {
        NSLog(@"Game id is empty can not load the game:%@, currentRoomID:%@", @(configModel.gameId), configModel.roomId);
        return;
    }
//    [[SudMGP getCfg]addEmbeddedMGPkg:1763401430010871809 mgPath:@"GreedyStar_1.0.0.1.sp"];
    
    // 2. 初始化SudMGP SDK<SudMGP initSDK>
    // 2. Initialize the SudMGP SDK <SudMGP initSDK>
    SudInitSDKParamModel *paramModel = SudInitSDKParamModel.new;
    paramModel.appId = configModel.appId;
    paramModel.appKey = configModel.appKey;
    paramModel.isTestEnv = configModel.isTestEnv;
    [SudMGP initSDK:paramModel listener:^(int retCode, const NSString * _Nonnull retMsg) {
        
        if (retCode != 0) {
            NSLog(@"ISudFSMMG:initGameSDKWithAppID init sdk failed :%@(%@)", retMsg, @(retCode));
            return;
        }
        NSLog(@"ISudFSMMG:initGameSDKWithAppID: init sdk successfully");
        // 加载游戏
        // Load the game
        [weakSelf loadMG:configModel code:code];
    }];
}

/// 加载游戏MG
/// Initialize the SudMDP SDK for the game
/// @param configModel 配置model
/// @param configModel cofnig model
- (void)loadMG:(SudGameLoadConfigModel *)configModel code:(NSString *)code {
    NSAssert(self.sudGameEventHandler, @"Must registerGameEventHandler before!");
    [self.sudGameEventHandler setupLoadConfigModel:configModel];
    // 确保初始化前不存在已加载的游戏 保证SudMGP initSDK前，销毁SudMGP
    // Ensure that there are no loaded games before initialization. Ensure SudMGP is destroyed before initSDK
    [self destroyGame];
    NSLog(@"loadMG:userId:%@, gameRoomId:%@, gameId:%@", configModel.userId, configModel.roomId, @(configModel.gameId));
    if (configModel.userId.length == 0 ||
            configModel.roomId.length == 0 ||
            code.length == 0 ||
            configModel.language.length == 0 ||
            configModel.gameId <= 0) {

        NSLog(@"loadGame: param has some one empty");
        return;
    }
    // 必须配置当前登录用户
    // The current login user must be configured
    [self.sudGameEventHandler.sudFSMMGDecorator setCurrentUserId:configModel.userId];
    // 3. 加载SudMGP SDK<SudMGP loadMG>，注：客户端必须持有iSudFSTAPP实例
    // 3. Load SudMGP SDK<SudMGP loadMG>. Note: The client must hold the iSudFSTAPP instance
    SudLoadMGParamModel *paramModel = SudLoadMGParamModel.new;
    paramModel.userId = configModel.userId;
    paramModel.roomId = configModel.roomId;
    paramModel.code = code;
    paramModel.mgId = configModel.gameId;
    paramModel.language = configModel.language;
    paramModel.gameViewContainer = configModel.gameView;
    paramModel.authorizationSecret = configModel.authorizationSecret;
    id <ISudFSTAPP> iSudFSTAPP = [SudMGP loadMG:paramModel fsmMG:self.sudGameEventHandler.sudFSMMGDecorator];
    [self.sudGameEventHandler.sudFSTAPPDecorator setISudFSTAPP:iSudFSTAPP];
}


@end
