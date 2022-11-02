//
//  KTVServiceImp.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/19.
//

#import "KTVServiceImp.h"
#import "VLAPIRequest.h"
#import "VLURLPathConfig.h"
#import "VLUserCenter.h"
#import "AgoraRtm.h"
#import "VLMacroDefine.h"
#import "VLRoomListModel.h"
#import "AgoraEntScenarios-Swift.h"
#import "VLGlobalHelper.h"
#import "VLKTVSelBgModel.h"

@interface KTVServiceImp()<AgoraRtmDelegate, AgoraRtmChannelDelegate>
@property (nonatomic, strong) NSString* roomNo;
@property (nonatomic, strong) AgoraRtmChannel *rtmChannel;

@property (nonatomic, copy, nullable) void(^userListCountDidChanged)(NSUInteger);
@property (nonatomic, copy, nullable) void(^seatListDidChanged)(KTVSubscribe, VLRoomSeatModel*);
@property (nonatomic, copy, nullable) void(^roomDidChanged)(KTVSubscribe, VLRoomListModel*);
@property (nonatomic, copy, nullable) void(^chooseSongDidChanged)(KTVSubscribe, VLRoomSelSongModel*);
@property (nonatomic, copy, nullable) void(^messageDidChanged)(AgoraRtmChannel*, AgoraRtmMessage*, AgoraRtmMember*);
@end

@implementation KTVServiceImp

#pragma private method

- (void)setRoomNo:(NSString *)roomNo {
    if (roomNo == _roomNo) {
        return;
    }
    _roomNo = roomNo;
    
    if (_rtmChannel) {
        [self leaveChannel];
        _rtmChannel = nil;
    }
    
    if (_roomNo) {
        [self createChannel:_roomNo];
    }
}

/// create rtm instance
/// @param channel <#channel description#>
- (void)createChannel:(NSString *)channel {
    AgoraRtmChannel *rtmChannel = [AgoraRtm.kit createChannelWithId:channel delegate:self];
    
    if (!rtmChannel) {
//        [VLToast toast:NSLocalizedString(@"加入频道失败", nil)];
        NSAssert(false, [@"加入频道失败" toSceneLocalization]);
        return;
    }
    
    [rtmChannel joinWithCompletion:^(AgoraRtmJoinChannelErrorCode errorCode) {
        if (errorCode != AgoraRtmJoinChannelErrorOk) {
            NSString* errorMsg = [NSString stringWithFormat:@"%@:%ld", [@"加入频道失败" toSceneLocalization], errorCode];
            NSAssert(false, errorMsg);
//            [VLToast toast:[NSString stringWithFormat:NSLocalizedString(@"加入频道失败:%ld", nil), errorCode]];
        }
    }];
    
    self.rtmChannel = rtmChannel;
}


/// bind seat array and song array
/// @param seatsArray <#seatsArray description#>
/// @param songArray <#songArray description#>
- (NSArray *)configureSeatsWithArray:(NSArray *)seatsArray songArray:(NSArray *)songArray {
    NSMutableArray *seatMuArray = [NSMutableArray array];
    
    NSArray *modelArray = [VLRoomSeatModel vj_modelArrayWithJson:seatsArray];
    for (int i=0; i<8; i++) {
        BOOL ifFind = NO;
        for (VLRoomSeatModel *model in modelArray) {
            if (model.onSeat == i) { //这个位置已经有人了
                ifFind = YES;
                if(songArray != nil && [songArray count] >= 1) {
                    if([model.userNo isEqualToString:songArray[0][@"userNo"]]) {
                        model.ifSelTheSingSong = YES;
                    }
                    else if([model.userNo isEqualToString:songArray[0][@"chorusNo"]]) {
                        model.ifJoinedChorus = YES;
                    }
                }
                [seatMuArray addObject:model];
            }
        }
        if (!ifFind) {
            VLRoomSeatModel *model = [[VLRoomSeatModel alloc]init];
            model.onSeat = i;
            [seatMuArray addObject:model];
        }
    }
    return seatMuArray.mutableCopy;
}
- (NSString* _Nonnull)getRoomNo {
    NSAssert([self.roomNo length] > 0, @"roomNo is Empty");
    return self.roomNo;
}

- (NSString* _Nonnull)getUserNo {
    NSAssert([VLUserCenter.user.userNo length] > 0, @"userNo is Empty");
    return VLUserCenter.user.userNo;
}


#pragma mark KTVServiceProtocol

- (void)getRoomListWithPage:(NSUInteger)page
                 completion:(void(^)(NSError* _Nullable, NSArray<VLRoomListModel*>* _Nullable))completion {
    NSDictionary *param = @{
        @"size" : @(10),
        @"current": @(page)
    };
    [VLAPIRequest postRequestURL:kURLGetRoolList
                       parameter:param
                         showHUD:NO
                         success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            NSArray *array = [VLRoomListModel vj_modelArrayWithJson:response.data[@"records"]];
            completion(nil, array);
            
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil], nil);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error, nil);
    }];
}

- (void)createRoomWithInput:(KTVCreateRoomInputModel*)inputModel
                 completion:(void (^)(NSError*_Nullable, KTVCreateRoomOutputModel*_Nullable))completion {
    
    NSDictionary *param = @{
        @"belCanto": inputModel.belCanto,
        @"icon": inputModel.icon,
        @"isPrivate": inputModel.isPrivate,
        @"name": inputModel.name,
        @"password": inputModel.password,
        @"soundEffect": inputModel.soundEffect,
        @"userNo": [self getUserNo]//inputModel.userNo
    };
    VL(weakSelf);
    [VLAPIRequest postRequestURL:kURLCreateRoom
                       parameter:param showHUD:YES
                         success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            NSDictionary *param = @{
                @"roomNo" : response.data,
                @"userNo": [self getUserNo],//inputModel.userNo,
                @"password": inputModel.password
            };
            
            [VLAPIRequest getRequestURL:kURLGetInRoom
                              parameter:param
                                showHUD:YES
                                success:^(VLResponseDataModel * _Nonnull response) {
                if (response.code == 0) {
                    [AgoraRtm updateDelegate:weakSelf];
                    if ([response.data[@"creatorNo"] isEqualToString:VLUserCenter.user.userNo]) { //自己是房主
                        VLUserCenter.user.ifMaster = YES;
                    }else{
                        VLUserCenter.user.ifMaster = NO;
                    }
                    
                    VLUserCenter.user.agoraRTCToken = response.data[@"agoraRTCToken"];
                    VLUserCenter.user.agoraRTMToken = response.data[@"agoraRTMToken"];
                    VLUserCenter.user.agoraPlayerRTCToken = response.data[@"agoraPlayerRTCToken"];
                    
                    VLLog(@"Agora - RTCToken: %@, RTMToken: %@, RTCPlayerToken: %@, UID: %@, roomNo: %@",
                          VLUserCenter.user.agoraRTCToken,
                          VLUserCenter.user.agoraRTMToken,
                          VLUserCenter.user.agoraPlayerRTCToken,
                          VLUserCenter.user.id,
                          param[@"roomNo"]);
                    [AgoraRtm.kit loginByToken:VLUserCenter.user.agoraRTMToken
                                          user:VLUserCenter.user.id
                                    completion:^(AgoraRtmLoginErrorCode errorCode) {
                        if (!(errorCode == AgoraRtmLoginErrorOk || errorCode == AgoraRtmLoginErrorAlreadyLogin)) {
                            VLLog(@"Agora - 加入RTM失败, errorCode: %ld", errorCode);
                            return;
                        }
                        [AgoraRtm setStatus:LoginStatusOnline];
                        
                        KTVCreateRoomOutputModel* outputModel = [KTVCreateRoomOutputModel new];
                        outputModel.name = response.data[@"name"];
                        outputModel.roomNo = response.data[@"roomNo"];
                        NSArray *seatsArray = response.data[@"roomUserInfoDTOList"];
                        outputModel.seatsArray = [self configureSeatsWithArray:seatsArray songArray:nil];
                        weakSelf.roomNo = outputModel.roomNo;
                        completion(nil, outputModel);
                    }];
                }else{
//                    [VLToast toast:NSLocalizedString(@"加入房间失败", nil)];
                    completion([NSError errorWithDomain:[@"加入房间失败" toSceneLocalization] code:response.code userInfo:nil], nil);
                }
                
            } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
//                [VLToast toast:NSLocalizedString(@"加入房间失败", nil)];
                completion(error, nil);
            }];
            
        }else{
//            [VLToast toast:response.message];
            completion([NSError errorWithDomain:response.message code:response.code userInfo:nil], nil);
        }
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
//        [VLToast toast:NSLocalizedString(@"创建房间失败", nil)];
        completion(error, nil);
    }];
}

- (void)joinRoomWithInput:(KTVJoinRoomInputModel*)inputModel
               completion:(void (^)(NSError*_Nullable, KTVJoinRoomOutputModel*_Nullable))completion {
    NSDictionary *param = @{
        @"roomNo" : inputModel.roomNo,
        @"userNo": [self getUserNo],//inputModel.userNo,
        @"password": inputModel.password
    };
    VL(weakSelf);
    [VLAPIRequest getRequestURL:kURLGetInRoom
                      parameter:param
                        showHUD:YES
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            KTVJoinRoomOutputModel* outputModel = [KTVJoinRoomOutputModel new];
        
            [AgoraRtm updateDelegate:weakSelf];
            if ([response.data[@"creatorNo"] isEqualToString:VLUserCenter.user.userNo]) { //自己是房主
                VLUserCenter.user.ifMaster = YES;
            }else{
                VLUserCenter.user.ifMaster = NO;
            }
            outputModel.creator = response.data[@"creatorNo"];
            VLUserCenter.user.agoraRTCToken = response.data[@"agoraRTCToken"];
            VLUserCenter.user.agoraRTMToken = response.data[@"agoraRTMToken"];
            VLUserCenter.user.agoraPlayerRTCToken = response.data[@"agoraPlayerRTCToken"];
            
//            [AgoraRtm setCurrent:VLUserCenter.user.name];
            //登录RTM
            [AgoraRtm.kit loginByToken:VLUserCenter.user.agoraRTMToken
                                  user:VLUserCenter.user.id
                            completion:^(AgoraRtmLoginErrorCode errorCode) {
                if (!(errorCode == AgoraRtmLoginErrorOk || errorCode == AgoraRtmLoginErrorAlreadyLogin)) {
//                    VLLog(@"加入RTM失败");
                    completion([NSError errorWithDomain:@"rtm error" code:response.code userInfo:nil], nil);
                    return;
                }
                [AgoraRtm setStatus:LoginStatusOnline];
                weakSelf.roomNo = inputModel.roomNo;
                //处理座位信息
                NSArray *seatsArray = response.data[@"roomUserInfoDTOList"];
                NSArray *songArray = response.data[@"roomSongInfoDTOS"];
                
                outputModel.seatsArray = [weakSelf configureSeatsWithArray:seatsArray
                                                                 songArray:songArray];
                completion(nil, outputModel);
            }];
        } else {
            completion([NSError errorWithDomain:response.message code:response.code userInfo:nil], nil);
        }
        
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error, nil);
    }];
}


- (void)changeMVCoverWithInput:(KTVChangeMVCoverInputModel*)inputModel
                    completion:(void(^)(NSError* _Nullable))completion {
    NSString* roomNo = [self getRoomNo];
    NSDictionary *param = @{
        @"roomNo": roomNo,//inputModel.roomNo,
        @"bgOption": [NSString stringWithFormat:@"%lu", (unsigned long)inputModel.mvIndex],
        @"userNo": [self getUserNo],//inputModel.userNo
    };
    VL(weakSelf);
    [VLAPIRequest postRequestURL:kURLUpdataRoom parameter:param showHUD:YES success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            completion(nil);
            
            //发送切换背景的消息
            NSDictionary *dict = @{
                @"messageType":@(VLSendMessageTypeChangeMVBg),
                @"bgOption":[NSString stringWithFormat:@"%d",(int)inputModel.mvIndex],
                @"platform":@"1",
                @"roomNo": roomNo,//inputModel.roomNo
            };
            NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
            AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
            [weakSelf.rtmChannel sendMessage:roaMessage completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
                if (errorCode == 0) {
                    VLLog(@"发送切换背景消息");
                }
            }];
        }else{
//            [VLToast toast:response.message];
            completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
        }
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
//        [VLToast toast:NSLocalizedString(@"修改背景失败", nil)];
        completion(error);
    }];
}


- (void)onSeatWithInput:(KTVOnSeatInputModel*)inputModel
             completion:(void(^)(NSError* _Nullable))completion {
    NSString* roomNo = [self getRoomNo];
    NSDictionary *param = @{
        @"roomNo": roomNo,//self.roomModel.roomNo,
        @"seat": @(inputModel.seatIndex),
        @"userNo": [self getUserNo]//VLUserCenter.user.userNo
    };
    
    [VLAPIRequest getRequestURL:kURLRoomOnSeat
                      parameter:param
                        showHUD:YES
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            NSDictionary *dict = @{
                @"messageType": @(VLSendMessageTypeOnSeat),
                @"headUrl": VLUserCenter.user.headUrl ? VLUserCenter.user.headUrl:@"",
                @"onSeat": @(inputModel.seatIndex),
                @"name": VLUserCenter.user.name,
                @"userNo": VLUserCenter.user.userNo,
                @"id": VLUserCenter.user.id,
                @"platform": @"1",
                @"roomNo": roomNo
            };
            NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
            AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
            [self.rtmChannel sendMessage:roaMessage completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
                if (errorCode == 0) {
                    VLLog(@"发送上麦消息成功");
                    completion(nil);
                    return;
                }
                completion([NSError errorWithDomain:@"rtm error" code:errorCode userInfo:nil]);
            }];
            return;
        }
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}

- (void)outSeatWithInput:(KTVOutSeatInputModel*)inputModel
             completion:(void(^)(NSError* _Nullable))completion {
    NSString* roomNo = [self getRoomNo];
    NSDictionary *param = @{
        @"roomNo": roomNo,//self.roomModel.roomNo,
        @"userNo": inputModel.userNo
    };
    VL(weakSelf);
    [VLAPIRequest getRequestURL:kURLRoomDropSeat
                      parameter:param
                        showHUD:YES
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            //发送下麦的推送
            NSDictionary *dict = @{
                @"messageType":@(VLSendMessageTypeDropSeat),
                @"headUrl": inputModel.userHeadUrl,
                @"onSeat":@(inputModel.userOnSeat),
                @"name": inputModel.userName,
                @"userNo": inputModel.userNo,
                @"id": inputModel.userId,
                @"platform":@"1",
                @"roomNo":roomNo
            };
            NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
            AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
            [weakSelf.rtmChannel sendMessage:roaMessage
                              completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
                if (errorCode == 0) {
                    VLLog(@"发送下麦消息成功");
                    completion(nil);
                    return;
                }
                
                completion([NSError errorWithDomain:@"rtm error" code:errorCode userInfo:nil]);
            }];
            
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}


- (void)leaveRoomWithCompletion:(void(^)(NSError* _Nullable))completion {
    NSDictionary *param = @{
        @"roomNo": [self getRoomNo],//self.roomModel.roomNo,
        @"userNo": [self getUserNo],//VLUserCenter.user.userNo
    };
    [VLAPIRequest getRequestURL:kURLRoomOut
                      parameter:param
                        showHUD:NO
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            completion(nil);
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}


- (void)removeRoomWithCompletion:(void(^)(NSError* _Nullable))completion {
    NSString* roomNo = [self getRoomNo];
    NSDictionary *param = @{
        @"roomNo": roomNo,//self.roomModel.roomNo,
        @"userNo": [self getUserNo],//VLUserCenter.user.userNo
    };
    [VLAPIRequest getRequestURL:kURLRoomClose
                      parameter:param
                        showHUD:NO
                        success:^(VLResponseDataModel * _Nonnull response) {
        
        if (response.code == 0) {
            completion(nil);
            //发送关闭房间的消息
            NSDictionary *dict = @{
                @"messageType": @(VLSendMessageTypeCloseRoom),
                @"platform": @"1",
                @"roomNo": roomNo
            };
            NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
            AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
            [self.rtmChannel sendMessage:roaMessage
                              completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
                if (errorCode == 0) {
                    completion(nil);
                    return;
                }
                
                completion([NSError errorWithDomain:@"rtm error" code:errorCode userInfo:nil]);
            }];
            return;
        }

        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}


- (void)removeSongWithInput:(KTVRemoveSongInputModel*)inputModel
                 completion:(void(^)(NSError* _Nullable))completion {
    NSDictionary *param = @{
        @"roomNo" : [self getRoomNo], //self.roomModel.roomNo,
        @"songNo": inputModel.songNo,
        @"sort": inputModel.sort
    };
    
    [VLAPIRequest getRequestURL:kURLDeleteSong
                      parameter:param
                        showHUD:NO
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            completion(nil);
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}

- (void)getChoosedSongsListWithCompletion:(void(^)(NSError* _Nullable, NSArray<VLRoomSelSongModel*>* _Nullable))completion {
    
    NSDictionary *param = @{
        @"roomNo" : [self getRoomNo], //self.roomModel.roomNo
    };
    [VLAPIRequest getRequestURL:kURLChoosedSongs
                      parameter:param
                        showHUD:NO
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            NSArray<VLRoomSelSongModel*>* songArray = [VLRoomSelSongModel vj_modelArrayWithJson:response.data];
            completion(nil, songArray);
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil], nil);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error, nil);
    }];
}

- (void)joinChorusWithInput:(KTVJoinChorusInputModel*)inputModel
                 completion:(void(^)(NSError* _Nullable))completion {
    NSDictionary *param = @{
        @"roomNo": [self getRoomNo], //self.roomModel.roomNo,
        @"isChorus": inputModel.isChorus,
        @"userNo": [self getUserNo],//VLUserCenter.user.userNo,
        @"songNo": inputModel.songNo
    };
    
    [VLAPIRequest getRequestURL:kURLRoomJoinChorus
                      parameter:param
                        showHUD:NO
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            completion(nil);
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}

- (void)getSongDetailWithInput:(KTVSongDetailInputModel*)inputModel
                    completion:(void(^)(NSError* _Nullable, KTVSongDetailOutputModel* _Nullable))completion {
    NSDictionary *param = @{
        @"lyricType": @(inputModel.lyricType),
        @"songCode": inputModel.songNo
    };
    
    [VLAPIRequest getRequestURL:kURLSongDetail
                      parameter:param
                        showHUD:NO
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {     //拿到歌曲和歌词
            KTVSongDetailOutputModel* outputModel = [KTVSongDetailOutputModel new];
            outputModel.songNo = inputModel.songNo;
            outputModel.lyric = response.data[@"data"][@"lyric"];
            outputModel.songUrl = response.data[@"data"][@"playUrl"];
            completion(nil, outputModel);
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil], nil);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error, nil);
    }];
}


- (void)markSongDidPlayWithInput: (VLRoomSelSongModel*)inputModel
                      completion:(void(^)(NSError* _Nullable))completion {
    NSDictionary *param = @{
        @"imageUrl": inputModel.imageUrl,
        @"isChorus": @(inputModel.isChorus),
        @"score": @"",
        @"singer": inputModel.singer,
        @"songName": inputModel.songName,
        @"songNo": inputModel.songNo,
        @"songUrl": inputModel.songUrl,
        @"userNo": inputModel.userNo,
        @"roomNo": [self getRoomNo]//self.roomModel.roomNo
    };
    [VLAPIRequest getRequestURL:kURLBeginSinging parameter:param showHUD:NO success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            completion(nil);
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}

- (void)chooseSongWithInput:(KTVChooseSongInputModel*)inputModel
                 completion:(void(^)(NSError* _Nullable))completion {
    NSDictionary *param = @{
        @"isChorus": @(inputModel.isChorus),
        @"roomNo": [self getRoomNo],
        @"songName": inputModel.songName,
        @"songNo": inputModel.songNo,
        @"songUrl": inputModel.songUrl,
        @"userNo":VLUserCenter.user.userNo
    };
    [VLAPIRequest getRequestURL:kURLChooseSong
                      parameter:param
                        showHUD:NO
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            completion(nil);
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}

- (void)makeSongTopWithInput:(KTVMakeSongTopInputModel*)inputModel
                  completion:(void(^)(NSError* _Nullable))completion {
    NSDictionary *param = @{
        @"roomNo" : [self getRoomNo],
        @"songNo": inputModel.songNo,
        @"sort": inputModel.sort
    };
    [VLAPIRequest getRequestURL:kURLRoomMakeSongTop
                      parameter:param
                        showHUD:NO
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            completion(nil);
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}

#pragma mark subscribe

- (void)subscribeUserListCountWithChanged:(void (^)(NSUInteger))changedBlock {
    self.userListCountDidChanged = changedBlock;
}

- (void)subscribeSeatListWithChanged:(void (^)(NSUInteger, VLRoomSeatModel*))changedBlock {
    self.seatListDidChanged = changedBlock;
}

- (void)subscribeRoomStatusWithChanged:(void (^)(NSUInteger, VLRoomListModel*))changedBlock {
    self.roomDidChanged = changedBlock;
}

- (void)subscribeChooseSongWithChanged:(void (^)(NSUInteger, VLRoomSelSongModel*))changedBlock {
    //using publishSongDidChangedEventWithOwnerStatus
    self.chooseSongDidChanged = changedBlock;
}

#pragma mark Deprecated method

- (void)muteWithMuteStatus:(BOOL)mute
                completion:(void(^)(NSError* _Nullable))completion {
    NSString *setStatus = mute ? @"1" : @"0";

    NSDictionary *param = @{
        @"roomNo": [self getRoomNo],
        @"userNo": VLUserCenter.user.userNo,
        @"setStatus": setStatus
    };
    [VLAPIRequest getRequestURL:kURLIfSetMute
                      parameter:param
                        showHUD:NO
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            completion(nil);
            return;
        }
        
        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}

- (void)openVideoStatusWithStatus: (BOOL)openStatus
                       completion:(void(^)(NSError* _Nullable))completion {
    NSString *setStatus = openStatus ? @"1" : @"0";

    NSDictionary *param = @{
        @"roomNo": [self getRoomNo],
        @"userNo": VLUserCenter.user.userNo,
        @"setStatus": setStatus
    };
    [VLAPIRequest getRequestURL:kURLIfOpenVido
                      parameter:param
                        showHUD:NO
                        success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            completion(nil);
            return;
        }

        completion([NSError errorWithDomain:response.message code:response.code userInfo:nil]);
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        completion(error);
    }];
}

- (void)publishChooseSongEvent {
    //发送消息
    NSDictionary *dict = @{
        @"messageType": @(VLSendMessageTypeChooseSong),
        @"platform": @"1",
        @"roomNo": [self getRoomNo]
    };
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
    [self.rtmChannel sendMessage:roaMessage completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
        if (errorCode == 0) {
            VLLog(@"发送点歌消息成功");
        }
    }];
}

- (void)leaveChannel {
    [self.rtmChannel leaveWithCompletion:^(AgoraRtmLeaveChannelErrorCode errorCode) {
        VLLog(@"leave channel error: %ld", (long)errorCode);
    }];
    VLLog(@"Agora - Leave RTM channel");
}

- (void)publishMuteEventWithMuteStatus:(BOOL)muteStatus
                            completion:(void(^)(NSError* _Nullable))completion {
    NSDictionary *dict = @{
        @"messageType": @(VLSendMessageTypeAudioMute),
        @"userNo": VLUserCenter.user.userNo,
        @"id": VLUserCenter.user.id,
        @"isSelfMuted" : @(muteStatus ? 1 : 0),
        @"platform": @"1",
        @"roomNo": [self getRoomNo]
    };
    
   // [self.RTCkit muteLocalAudioStream:ifMute];
    
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
    [self.rtmChannel sendMessage:roaMessage completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
        if (errorCode == 0) {
            completion(nil);
            return;
        }
        
        completion([NSError errorWithDomain:@"rtm error" code:errorCode userInfo:nil]);
    }];
}

- (void)publishVideoOpenEventWithOpenStatus:(BOOL)openStatus
                                 completion:(void(^)(NSError* _Nullable))completion {
    NSDictionary *dict = @{
        @"messageType": @(VLSendMessageTypeVideoIfOpen),
        @"userNo": VLUserCenter.user.userNo,
        @"id": VLUserCenter.user.id,
        @"isVideoMuted": @(openStatus),
        @"platform": @"1",
        @"roomNo": [self getRoomNo]
    };
    
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
    [self.rtmChannel sendMessage:roaMessage completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
        if (errorCode == 0) {
            completion(nil);
            return;
        }
        
        completion([NSError errorWithDomain:@"rtm error" code:errorCode userInfo:nil]);
    }];
}

- (void)publishSongDidChangedEventWithOwnerStatus:(BOOL)isMaster {
    NSString *isMasterInterrupt = nil;
    if(isMaster) {
        isMasterInterrupt = @"1";
    }
    else {
        isMasterInterrupt = @"0";
    }
    NSDictionary *dict = @{
        @"messageType": @(VLSendMessageTypeChangeSong),
        @"platform": @"1",
        @"roomNo": [self roomNo],
        @"isMasterInterrupt": isMasterInterrupt
    };
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
    [self.rtmChannel sendMessage:roaMessage
                      completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
        if (errorCode == 0) {
            VLLog(@"RTM(SEND) - 发送切歌的消息 - from %@", VLUserCenter.user.userNo);
        }
    }];
}

- (void)publishToSoloEvent {
    NSDictionary *dict = @{
        @"messageType":@(VLSendMessageTypeSoloSong),
        @"platform":@"1",
        @"roomNo": [self getRoomNo]
    };
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
    [self.rtmChannel sendMessage:roaMessage completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
        if (errorCode == 0) {
        }
    }];
}

- (void)publishJoinToChorusWithCompletion:(void(^)(NSError* _Nullable))completion {
    NSDictionary *dict = @{
        @"messageType":@(VLSendMessageTypeTellSingerSomeBodyJoin),
        @"uid":VLUserCenter.user.id ? VLUserCenter.user.id : @"1",
//        @"bgUid":[NSString stringWithFormat:@"%u", arc4random() % 9999999],
//        @"bgUid": VLUserCenter.user.id,
        @"bgUid": [NSString stringWithFormat:@"%ld", [VLGlobalHelper getAgoraPlayerUserId:VLUserCenter.user.id]],
//        @"bgUid":[NSString stringWithFormat:@"%ld", (long)streamId],
        @"platform":@"1",
        @"roomNo": [self getRoomNo],
        @"userNo": VLUserCenter.user.userNo
    };
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
    [self.rtmChannel sendMessage:roaMessage completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
        if (errorCode == 0) {
            VLLog(@"发送加入合唱消息成功");
            completion(nil);
            return;
        }
        
        completion([NSError errorWithDomain:@"rtm error" code:errorCode userInfo:nil]);
    }];
}

- (void)publishSongOwnerWithOwnerId:(NSString*)userNo {
    NSDictionary *dict = @{
        @"messageType": @(VLSendMessageTypeTellJoinUID),
        @"userNo": userNo,
        @"name": VLUserCenter.user.name,
        @"bgUid": [NSString stringWithFormat:@"%ld", [VLGlobalHelper getAgoraPlayerUserId:VLUserCenter.user.id]],
        @"platform": @"1",
        @"roomNo": [self getUserNo]
    };
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
    [self.rtmChannel sendMessage:roaMessage completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
        if (errorCode == 0) {
            VLLog(@"发送通知合唱用户信息成功");
        }
    }];
}


- (void)publishSingingScoreWithTotalVolume:(double)totalVolume {
    NSDictionary *dict = @{
        @"messageType": @(VLSendMessageTypeSeeScore),
        @"pitch": @(totalVolume),
        @"platform": @"1",
        @"userNo": VLUserCenter.user.userNo,
        @"roomNo": [self getRoomNo]
    };
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    AgoraRtmRawMessage *roaMessage = [[AgoraRtmRawMessage alloc]initWithRawData:messageData description:@""];
    [self.rtmChannel sendMessage:roaMessage completion:^(AgoraRtmSendChannelMessageErrorCode errorCode) {
        if (errorCode == 0) {

        }
    }];
}

- (void)subscribeRtmMessageWithStatusChanged:(void(^)(AgoraRtmChannel*, AgoraRtmMessage*, AgoraRtmMember*))changedBlock {
    self.messageDidChanged = changedBlock;
}

@end

@interface KTVServiceImp(AgoraRtm)

@end

@implementation KTVServiceImp(AgoraRtm)

#pragma mark AgoraRtmChannelDelegate
- (void)channel:(AgoraRtmChannel *)channel memberJoined:(AgoraRtmMember *)member {
    NSString *user = member.userId;
    NSString *text = [user stringByAppendingString:@" join"];
    VLLog(@"memberJoined::%@",text);
}

- (void)channel:(AgoraRtmChannel *)channel memberLeft:(AgoraRtmMember *)member {
    NSString *user = member.userId;
    NSString *text = [user stringByAppendingString:@" left"];
    VLLog(@"memberLeft::%@",text);
}

- (void)channel:(AgoraRtmChannel * _Nonnull)channel memberCount:(int)count {
    VLLog(@"memberCount::::%d",count);
    if (self.userListCountDidChanged == nil) {
        return;
    }
    self.userListCountDidChanged(count);
}

- (void)channel:(AgoraRtmChannel *)channel
messageReceived:(AgoraRtmMessage *)message
     fromMember:(AgoraRtmMember *)member {
    
    AgoraRtmRawMessage *rowMessage = (AgoraRtmRawMessage *)message;
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:rowMessage.rawData];
//    if([dict[@"messageType"] intValue] != VLSendMessageTypeSeeScore){
    VLLog(@"messageReceived::%@",dict);
//    }
    if (!([dict[@"roomNo"] isEqualToString:[self getRoomNo]])) {
        return;
    }
    
    if (message.type == AgoraRtmMessageTypeRaw) {
        if ([dict[@"messageType"] intValue] == VLSendMessageTypeOnSeat) {  //上麦消息
            VLRoomSeatModel *seatModel = [VLRoomSeatModel vj_modelWithDictionary:dict];
            if (self.seatListDidChanged) {
                self.seatListDidChanged(KTVSubscribeCreated, seatModel);
            }
            return;
        } else if([dict[@"messageType"] intValue] == VLSendMessageTypeDropSeat){  // 下麦消息
            // 下麦模型
            VLRoomSeatModel *seatModel = [VLRoomSeatModel vj_modelWithDictionary:dict];
            if (self.seatListDidChanged) {
                self.seatListDidChanged(KTVSubscribeDeleted, seatModel);
            }
            return;
        } else if ([dict[@"messageType"] intValue] == VLSendMessageTypeCloseRoom) {  //房主关闭房间
            if (self.roomDidChanged) {
                self.roomDidChanged(KTVSubscribeDeleted, nil);
            }
            return;
        } else if ([dict[@"messageType"] intValue] == VLSendMessageTypeChooseSong) {  //收到点歌的消息
//            VLRoomSelSongModel *song = self.selSongsArray.count ? self.selSongsArray.firstObject : nil;
//            if(song == nil && [member.userId isEqualToString:VLUserCenter.user.id] == NO) {
//                [self getChoosedSongsList:false onlyRefreshList:NO];
//            }
//            else {
//                [self getChoosedSongsList:false onlyRefreshList:YES];
//            }
            if (self.chooseSongDidChanged) {
                //TODO(wushengtao): selSongsArray not found in KTVServiceImp
                self.chooseSongDidChanged(KTVSubscribeCreated, nil);
            }
            return;
        } else if([dict[@"messageType"] intValue] == VLSendMessageTypeChangeSong) { //切换歌曲
//            NSLog(@"RTM(RECV) - userID: %@, VLUserNo: %@, my userID: %@", member.userId, VLUserCenter.user.userNo, VLUserCenter.user.id);
////            dispatch_async(dispatch_get_main_queue(), ^{
////                [self playNextSong:[dict[@"isMasterInterrupt"] intValue]];
////            });
//            self.currentPlayingSongNo = nil;
//            [self prepareNextSong];
//            [self getChoosedSongsList:false onlyRefreshList:NO];
            if (self.chooseSongDidChanged) {
                //TODO(wushengtao): can not get selSongsArray in KTVServiceImp
                self.chooseSongDidChanged(KTVSubscribeDeleted, nil);
            }
            return;
        } else if ([dict[@"messageType"] intValue] == VLSendMessageTypeTellSingerSomeBodyJoin) {//有人加入合唱
//            dispatch_async(dispatch_get_main_queue(), ^{
//                [self.MVView setJoinInViewHidden];
//                [self setUserJoinChorus:dict[@"userNo"]];
//                if([self ifMainSinger:VLUserCenter.user.userNo]) {
//                    [self sendApplySendChorusMessage:dict[@"userNo"]];
//                }
//                [self joinChorusConfig:member.userId];
//            });
            if (self.chooseSongDidChanged) {
                VLRoomSelSongModel* songModel = [VLRoomSelSongModel new];
                songModel.isChorus = YES;
                songModel.chorusNo = dict[@"userNo"];
                self.chooseSongDidChanged(KTVSubscribeUpdated, songModel);
            }
            return;
        }else if([dict[@"messageType"] intValue] == VLSendMessageTypeSoloSong){ //独唱
//            dispatch_async(dispatch_get_main_queue(), ^{
//                [self startSinging];
//                [self.MVView setJoinInViewHidden];
//            });
        }else if([dict[@"messageType"] intValue] == VLSendMessageTypeChangeMVBg){ //切换背景
            VLRoomListModel* roomInfo = [VLRoomListModel new];
            roomInfo.bgOption = [dict[@"bgOption"] integerValue];
            if (self.roomDidChanged) {
                self.roomDidChanged(KTVSubscribeUpdated, roomInfo);
            }
            return;
        }else if([dict[@"messageType"] intValue] == VLSendMessageTypeAudioMute){ //是否静音
//            VLRoomSeatModel *model = [VLRoomSeatModel new];
//            model.userNo = dict[@"userNo"];
//            model.id = dict[@"id"];
//            model.isSelfMuted = [dict[@"isSelfMuted"] intValue];
//            for (VLRoomSeatModel *seatModel in self.seatsArray) {
//                if ([seatModel.userNo isEqualToString:model.userNo]) {
//                    seatModel.isSelfMuted = model.isSelfMuted;
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                        [self.roomPersonView updateSeatsByModel:seatModel];
//                    });
//                    break;
//                }
//            }
        } else if([dict[@"messageType"] intValue] == VLSendMessageTypeVideoIfOpen) { //是否打开视频
//            VLRoomSeatModel *model = [VLRoomSeatModel new];
//            model.userNo = dict[@"userNo"];
//            model.id = dict[@"id"];
//            model.isVideoMuted = [dict[@"isVideoMuted"] intValue];
//            for (VLRoomSeatModel *seatModel in self.seatsArray) {
//                if ([seatModel.userNo isEqualToString:model.userNo]) {
//                    seatModel.isVideoMuted = model.isVideoMuted;
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                        [self.roomPersonView updateSeatsByModel:seatModel];
//                    });
//                }
//            }
        }else if([dict[@"messageType"] intValue] == VLSendMessageTypeSeeScore) { //观众看到打分
//            [self.MVView setVoicePitch:@[@(voicePitch)]];
            if (self.chooseSongDidChanged) {
                double voicePitch = [dict[@"pitch"] doubleValue];
                VLRoomSelSongModel* songModel = [VLRoomSelSongModel new];
                songModel.userNo = dict[@"userNo"];
                songModel.score = voicePitch;
                self.chooseSongDidChanged(KTVSubscribeUpdated, songModel);
            }
            return;
        }
        else if([dict[@"messageType"] intValue] == VLSendMessageAuditFail) {
//            VLLog(@"Agora - Received audit message");
//            if ([dict[@"userNo"] isEqualToString:VLUserCenter.user.userNo]) {
//                dispatch_async(dispatch_get_main_queue(), ^{
//                    [VLToast toast:NSLocalizedString(@"您的行为存在涉嫌违法违规内容，请规范行为。", nil)];
//                });
//            }
        }
        
        
        if (self.messageDidChanged == nil) {
            return;
        }
        self.messageDidChanged(channel, message, member);
    }
}

- (void)channel:(AgoraRtmChannel * _Nonnull)channel
attributeUpdate:(NSArray< AgoraRtmChannelAttribute *> * _Nonnull)attributes {
    NSLog(@"%@",attributes);
}

#pragma mark AgoraRtmDelegate
- (void)rtmKit:(AgoraRtmKit *)kit
connectionStateChanged:(AgoraRtmConnectionState)state
        reason:(AgoraRtmConnectionChangeReason)reason {
//    NSString *message = [NSString stringWithFormat:@"connection state changed: %ld", state];
}

- (void)rtmKit:(AgoraRtmKit *)kit
messageReceived:(AgoraRtmMessage *)message
      fromPeer:(NSString *)peerId {
    
}

@end
