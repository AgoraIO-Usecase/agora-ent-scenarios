//
//  LSTPopView+KTVModal.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import <LSTPopView/LSTPopView.h>
#import "VLSRPopMoreSelView.h"
#import "VLSRPopSelBgView.h"
#import "VLSRDropOnLineView.h"
#import "VLSRAudioEffectPicker.h"
#import "VLSRBadNetWorkView.h"
#import "VLSRPopSongList.h"
#import "VLSREffectView.h"
#import "VLSRSettingView.h"
#import "VLSRBelcantoModel.h"
NS_ASSUME_NONNULL_BEGIN

@interface LSTPopView (SRModal)

+ (LSTPopView*)getPopViewWithCustomView:(UIView*)customView;

//更换MV背景
+ (LSTPopView*)popSelMVBgViewWithParentView:(UIView*)parentView
                                    bgModel:(VLSRSelBgModel*)bgModel
                               withDelegate:(id<VLSRPopSelBgViewDelegate>)delegate;

//弹出更多
+ (LSTPopView*)popSelMoreViewWithParentView: (UIView*)parentView
                               withDelegate:(id<VLSRPopMoreSelViewDelegate>)delegate;


//弹出下麦视图
+ (LSTPopView*)popDropLineViewWithParentView:(UIView*)parentView
                               withSeatModel:(VLSRRoomSeatModel *)seatModel
                                withDelegate:(id<VLSRDropOnLineViewDelegate>)delegate;

//弹出美声视图
+ (LSTPopView*)popBelcantoViewWithParentView:(UIView*)parentView
                           withBelcantoModel:(VLSRBelcantoModel *)belcantoModel
                                withDelegate:(id<VLSRAudioEffectPickerDelegate>)delegate;

//弹出点歌视图
+ (LSTPopView*)popUpChooseSongViewWithParentView:(UIView*)parentView
                                        isChorus:(BOOL)isChorus
                                 chooseSongArray: (NSArray*)chooseSongArray
                                      withRoomNo:(NSString*)roomNo
                                    withDelegate:(id<VLSRPopSongListDelegate>)delegate;

//弹出音效
+ (LSTPopView*)popSetSoundEffectViewWithParentView:(UIView*)parentView
                                         soundView:(VLSREffectView*)soundView
                                      withDelegate:(id<VLSREffectViewDelegate>)delegate;

//网络差视图
+ (LSTPopView*)popBadNetWrokTipViewWithParentView:(UIView*)parentView
                                     withDelegate:(id<VLSRBadNetWorkViewDelegate>)delegate;


//控制台
+ (LSTPopView*)popSettingViewWithParentView:(UIView*)parentView
                               settingView:(VLSRSettingView*)settingView
                               withDelegate:(id<VLSRSettingViewDelegate>)delegate;
@end

NS_ASSUME_NONNULL_END
