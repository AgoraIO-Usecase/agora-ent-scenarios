//
//  LSTPopView+KTVModal.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import <AgoraCommon/LSTPopView.h>
#import "VLPopMoreSelView.h"
#import "VLPopSelBgView.h"
#import "VLDropOnLineView.h"
#import "VLAudioEffectPicker.h"
#import "VLBadNetWorkView.h"
#import "VLEffectView.h"
#import "DHCVLKTVSettingView.h"
#import "VLEarSettingView.h"
#import "DHCDebugView.h"
NS_ASSUME_NONNULL_BEGIN

@interface LSTPopView (DHCModal)

+ (LSTPopView*)getDHCPopViewWithCustomView:(UIView*)customView;

//更换MV背景
+ (LSTPopView*)popDHCSelMVBgViewWithParentView:(UIView*)parentView
                                    bgModel:(VLKTVSelBgModel*)bgModel
                               withDelegate:(id<VLPopSelBgViewDelegate>)delegate;

//弹出更多
+ (LSTPopView*)popDHCSelMoreViewWithParentView: (UIView*)parentView
                               withDelegate:(id<VLPopMoreSelViewDelegate>)delegate;


//弹出下麦视图
+ (LSTPopView*)popDHCDropLineViewWithParentView:(UIView*)parentView
                               withSeatModel:(VLRoomSeatModel *)seatModel
                                withDelegate:(id)delegate;

//弹出美声视图
+ (LSTPopView*)popDHCBelcantoViewWithParentView:(UIView*)parentView
                           withBelcantoModel:(VLBelcantoModel *)belcantoModel
                                withDelegate:(id<VLAudioEffectPickerDelegate>)delegate;

//弹出点歌视图
+ (LSTPopView*)popDHCUpChooseSongViewWithParentView:(UIView*)parentView
                                        isChorus:(BOOL)isChorus
                                 chooseSongArray: (NSArray*)chooseSongArray
                                      withRoomNo:(NSString*)roomNo
                                    withDelegate:(id)delegate;

//弹出音效
+ (LSTPopView*)popDHCSetSoundEffectViewWithParentView:(UIView*)parentView
                                         soundView:(VLEffectView*)soundView
                                      withDelegate:(id<VLEffectViewDelegate>)delegate;

//网络差视图
+ (LSTPopView*)popDHCBadNetWrokTipViewWithParentView:(UIView*)parentView
                                     withDelegate:(id<VLBadNetWorkViewDelegate>)delegate;


//控制台
+ (LSTPopView*)popDHCSettingViewWithParentView:(UIView*)parentView
                               settingView:(nullable DHCVLKTVSettingView*)settingView
                               withDelegate:(id)delegate;

//弹出耳返视图
+ (LSTPopView*)popDHCEarSettingViewWithParentView:(UIView*)parentView
                                   isEarOn:(BOOL)isEarOn
                                           vol:(CGFloat)vol
                                  withDelegate:(id<VLEarSettingViewViewDelegate>)delegate;

//弹出DebugView
+ (LSTPopView*)popDHCDebugViewWithParentView:(UIView*)parentView
                                   isDebugMode:(BOOL)isDebugMode
                                  withDelegate:(id<DHCDebugViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
