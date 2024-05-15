//
//  LSTPopView+KTVModal.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import <LSTPopView/LSTPopView.h>
#import "VLPopMoreSelView.h"
#import "VLPopSelBgView.h"
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
#import "VLEarSettingView.h"
#import "VLDebugView.h"
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
                                    setting:(VLKTVSettingModel*)settingModel
                               settingView:(VLKTVSettingView*)settingView
                               withDelegate:(id<VLKTVSettingViewDelegate>)delegate;

//弹出人声突出
+ (LSTPopView*)popVoiceShowViewWithParentView:(UIView*)parentView
                                         showView:(VLVoiceShowView*)showView
                                   imgSource:(NSArray *)imgSource
                                    nameSource:(NSArray *)nameSource
                                  selectUserNo:(NSString *)selectUserNo
                                  userNoArray:(NSArray *)userNoArray
                                 UIUpdateAble:(BOOL)UIUpdateAble
                                      withDelegate:(id<VLVoiceShowViewDelegate>)delegate;


+ (LSTPopView*)popVoicePerViewWithParentView:(UIView*)parentView
                              isProfessional: (BOOL) isProfessional
                                    aecState:(BOOL)state
                                    aecLevel:(NSInteger)level
                                     isDelay: (BOOL) isDelay
                                    volGrade:(NSInteger)volGrade
                                       grade:(NSInteger)grade
                                    isRoomOwner: (BOOL) isRoomOwner
                                         perView:(VLVoicePerShowView*)perView
                                      withDelegate:(id<VLVoicePerShowViewDelegate>)delegate;

//弹出耳返视图
+ (LSTPopView*)popEarSettingViewWithParentView:(UIView*)parentView
                                   isEarOn:(BOOL)isEarOn
                                           vol:(CGFloat)vol
                                  withDelegate:(id<VLEarSettingViewViewDelegate>)delegate;

//弹出虚拟声卡视图
+ (LSTPopView*)popSoundCardViewWithParentView:(UIView*)parentView
                                    soundOpen:(BOOL)isOpen
                                    gainValue:(double)gain
                                    typeValue:(NSInteger)type
                                   effectType:(NSInteger)effect;

+ (LSTPopView*)popSoundCardViewWithParentView:(UIView*)parentView
                                soundCardView:(UIView *)soundCardView;

//弹出DebugView
+ (LSTPopView*)popDebugViewWithParentView:(UIView*)parentView
                                    channelName:(NSString *)name
                                   sdkVer:(NSString *)ver
                                   isDebugMode:(BOOL)isDebugMode
                             withDelegate:(id<VLDebugViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
