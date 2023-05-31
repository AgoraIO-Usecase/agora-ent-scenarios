//
//  LSTPopView+KTVModal.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import <LSTPopView/LSTPopView.h>
#import "VLSBGPopMoreSelView.h"
#import "VLSBGPopSelBgView.h"
#import "VLSBGRoomSeatModel.h"
#import "VLSBGDropOnLineView.h"
#import "VLSBGAudioEffectPicker.h"
#import "VLSBGBadNetWorkView.h"
#import "VLSBGPopSongList.h"
#import "VLSBGEffectView.h"
#import "VLSBGSettingView.h"
#import "VLSBGVoiceShowView.h"
#import "VLSBGVoicePerShowView.h"

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

//弹出音效
+ (LSTPopView*)popSBGSetSoundEffectViewWithParentView:(UIView*)parentView
                                         soundView:(VLSBGEffectView*)soundView
                                      withDelegate:(id<VLSBGEffectViewDelegate>)delegate;

//网络差视图
+ (LSTPopView*)popSBGBadNetWrokTipViewWithParentView:(UIView*)parentView
                                     withDelegate:(id<VLSBGBadNetWorkViewDelegate>)delegate;


//控制台
+ (LSTPopView*)popSBGSettingViewWithParentView:(UIView*)parentView
                               settingView:(VLSBGSettingView*)settingView
                               withDelegate:(id<VLSBGSettingViewDelegate>)delegate;

+ (LSTPopView*)popSBGVoiceShowViewWithParentView:(UIView*)parentView
                                         showView:(VLSBGVoiceShowView*)showView
                                   dataSource:(NSArray *)array
                                      withDelegate:(id<VLSBGVoiceShowViewDelegate>)delegate;


//弹出专业主播
+ (LSTPopView*)popSBGVoicePerViewWithParentView:(UIView*)parentView
                                         perView:(VLSBGVoicePerShowView*)perView
                                withDelegate:(id<VLSBGVoicePerShowViewDelegate>)delegate;
@end

NS_ASSUME_NONNULL_END
