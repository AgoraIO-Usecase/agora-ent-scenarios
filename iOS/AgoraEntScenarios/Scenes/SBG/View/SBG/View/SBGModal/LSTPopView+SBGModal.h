//
//  LSTPopView+KTVModal.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import <AgoraCommon/LSTPopView.h>
#import "VLSBGPopMoreSelView.h"
#import "VLSBGPopSelBgView.h"
#import "VLSBGRoomSeatModel.h"
#import "VLSBGDropOnLineView.h"
#import "VLSBGAudioEffectPicker.h"
#import "VLSBGBadNetWorkView.h"
#import "VLSBGPopSongList.h"
#import "VLSBGSettingView.h"
#import "VLSBGVoicePerShowView.h"
#import "VLSBGDebugView.h"
NS_ASSUME_NONNULL_BEGIN

@interface LSTPopView (SBGModal)

+ (LSTPopView*)getSBGPopViewWithCustomView:(UIView*)customView;

//更换MV背景
+ (LSTPopView*)popSBGSelMVBgViewWithParentView:(UIView*)parentView
                                    bgModel:(VLSBGSelBgModel*)bgModel
                               withDelegate:(id<VLSBGPopSelBgViewDelegate>)delegate;

//弹出更多
+ (LSTPopView*)popSBGSelMoreViewWithParentView: (UIView*)parentView
                               withDelegate:(id<VLSBGPopMoreSelViewDelegate>)delegate;


//弹出下麦视图
+ (LSTPopView*)popSBGDropLineViewWithParentView:(UIView*)parentView
                               withSeatModel:(VLSBGRoomSeatModel *)seatModel
                                withDelegate:(id<VLSBGDropOnLineViewDelegate>)delegate;

//弹出美声视图
+ (LSTPopView*)popSBGBelcantoViewWithParentView:(UIView*)parentView
                           withBelcantoModel:(VLSBGBelcantoModel *)belcantoModel
                                withDelegate:(id<VLSBGAudioEffectPickerDelegate>)delegate;

//弹出点歌视图
+ (LSTPopView*)popSBGUpChooseSongViewWithParentView:(UIView*)parentView
                                        isChorus:(BOOL)isChorus
                                 chooseSongArray: (NSArray*)chooseSongArray
                                      withRoomNo:(NSString*)roomNo
                                    withDelegate:(id<VLSBGPopSongListDelegate>)delegate;

//网络差视图
+ (LSTPopView*)popSBGBadNetWrokTipViewWithParentView:(UIView*)parentView
                                     withDelegate:(id<VLSBGBadNetWorkViewDelegate>)delegate;


//控制台
+ (LSTPopView*)popSBGSettingViewWithParentView:(UIView*)parentView
                               settingView:(VLSBGSettingView*)settingView
                               withDelegate:(id<VLSBGSettingViewDelegate>)delegate;


//弹出专业主播
+ (LSTPopView*)popSBGVoicePerViewWithParentView:(UIView*)parentView
                                         perView:(VLSBGVoicePerShowView*)perView
                                withDelegate:(id<VLSBGVoicePerShowViewDelegate>)delegate;

//弹出DebugView
+ (LSTPopView*)popSBGDebugViewWithParentView:(UIView*)parentView
                                    channelName:(NSString *)name
                                   sdkVer:(NSString *)ver
                                   isDebugMode:(BOOL)isDebugMode
                                withDelegate:(id<VLSBGDebugViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
