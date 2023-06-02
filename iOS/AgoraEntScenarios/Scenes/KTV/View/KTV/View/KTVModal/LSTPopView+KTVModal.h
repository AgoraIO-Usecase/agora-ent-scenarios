//
//  LSTPopView+KTVModal.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import <LSTPopView/LSTPopView.h>
#import "VLPopMoreSelView.h"
#import "VLPopSelBgView.h"
#import "VLRoomSeatModel.h"
#import "VLDropOnLineView.h"
#import "VLAudioEffectPicker.h"
#import "VLBadNetWorkView.h"
#import "VLPopSongList.h"
#import "VLEffectView.h"
#import "VLKTVSettingView.h"
#import "VLVoiceShowView.h"
#import "VLVoicePerShowView.h"
#import "VLVoiceShowView.h"
#import "VLVoicePerShowView.h"

NS_ASSUME_NONNULL_BEGIN

@interface LSTPopView (KTVModal)

+ (LSTPopView*)getPopViewWithCustomView:(UIView*)customView;

//更换MV背景
+ (LSTPopView*)popSelMVBgViewWithParentView:(UIView*)parentView
                                    bgModel:(VLKTVSelBgModel*)bgModel
                               withDelegate:(id<VLPopSelBgViewDelegate>)delegate;

//弹出更多
+ (LSTPopView*)popSelMoreViewWithParentView: (UIView*)parentView
                               withDelegate:(id<VLPopMoreSelViewDelegate>)delegate;


//弹出下麦视图
+ (LSTPopView*)popDropLineViewWithParentView:(UIView*)parentView
                               withSeatModel:(VLRoomSeatModel *)seatModel
                                withDelegate:(id<VLDropOnLineViewDelegate>)delegate;

//弹出美声视图
+ (LSTPopView*)popBelcantoViewWithParentView:(UIView*)parentView
                           withBelcantoModel:(VLBelcantoModel *)belcantoModel
                                withDelegate:(id<VLAudioEffectPickerDelegate>)delegate;

//弹出点歌视图
+ (LSTPopView*)popUpChooseSongViewWithParentView:(UIView*)parentView
                                        isChorus:(BOOL)isChorus
                                 chooseSongArray: (NSArray*)chooseSongArray
                                      withRoomNo:(NSString*)roomNo
                                    withDelegate:(id<VLPopSongListDelegate>)delegate;

//弹出音效
+ (LSTPopView*)popSetSoundEffectViewWithParentView:(UIView*)parentView
                                         soundView:(VLEffectView*)soundView
                                      withDelegate:(id<VLEffectViewDelegate>)delegate;

//网络差视图
+ (LSTPopView*)popBadNetWrokTipViewWithParentView:(UIView*)parentView
                                     withDelegate:(id<VLBadNetWorkViewDelegate>)delegate;


//控制台
+ (LSTPopView*)popSettingViewWithParentView:(UIView*)parentView
                               settingView:(VLKTVSettingView*)settingView
                               withDelegate:(id<VLKTVSettingViewDelegate>)delegate;

+ (LSTPopView*)popVoiceShowViewWithParentView:(UIView*)parentView
                                         showView:(VLVoiceShowView*)showView
                                   dataSource:(NSArray *)array
                                      withDelegate:(id<VLVoiceShowViewDelegate>)delegate;


+ (LSTPopView*)popVoicePerViewWithParentView:(UIView*)parentView
                                       grade:(NSInteger)grade
                                    isRoomOwner: (BOOL) isRoomOwner
                                         perView:(VLVoicePerShowView*)perView
                                withDelegate:(id<VLVoicePerShowViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
