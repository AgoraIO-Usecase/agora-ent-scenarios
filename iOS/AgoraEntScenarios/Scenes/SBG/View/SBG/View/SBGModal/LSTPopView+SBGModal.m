//
//  LSTPopView+KTVModal.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import "LSTPopView+SBGModal.h"
#import "VLMacroDefine.h"
#import "SBGMacro.h"
#import "UIView+VL.h"
#import "VLSBGEffectView.h"
#import "VLSBGVoiceShowView.h"
#import "VLSBGVoicePerShowView.h"

@implementation LSTPopView (SBGModal)

#pragma mark private method
//公共弹窗视图设置
+ (LSTPopView*)_createRSPopContainerWithContentView:(UIView*)contentView
                                      withParentView:(UIView*)parentView {
    LSTPopView *popView = [LSTPopView initWithCustomView:contentView
                                              parentView:parentView
                                                popStyle:LSTPopStyleSmoothFromBottom
                                            dismissStyle:LSTDismissStyleSmoothToBottom];
    popView.hemStyle = LSTHemStyleBottom;
    popView.popDuration = 0.5;
    popView.dismissDuration = 0.5;
    popView.cornerRadius = 20;
    LSTPopViewWK(popView)
    popView.isClickFeedback = YES;
    popView.bgClickBlock = ^{
        [wk_popView dismiss];
    };
    popView.rectCorners = UIRectCornerTopLeft | UIRectCornerTopRight;
    return  popView;
}

#pragma mark public method

+ (LSTPopView*)getSBGPopViewWithCustomView:(UIView*)customView {
    UIView* superView = customView.superview;
    while (superView != nil) {
        if ([superView isKindOfClass:[LSTPopView class]]) {
            return (LSTPopView*)superView;
        }
        
        superView = superView.superview;
    }
    
    return nil;
}

+ (LSTPopView*)popSBGSelMVBgViewWithParentView:(UIView*)parentView
                                    bgModel:(VLSBGSelBgModel*)bgModel
                               withDelegate:(id<VLSBGPopSelBgViewDelegate>)delegate {
    CGFloat popViewH = (SCREEN_WIDTH - 60) / 3.0 * 0.75 * 3 + 100 + kSafeAreaBottomHeight;
    VLSBGPopSelBgView *changeBgView = [[VLSBGPopSelBgView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                            withDelegate:delegate];
    changeBgView.selBgModel = bgModel;
    
    LSTPopView* popView = [self _createRSPopContainerWithContentView:changeBgView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出更多
+ (LSTPopView*)popSBGSelMoreViewWithParentView:(UIView*)parentView
                               withDelegate:(id<VLSBGPopMoreSelViewDelegate>)delegate {
    CGFloat popViewH = 190 + kSafeAreaBottomHeight;
    VLSBGPopMoreSelView *moreView = [[VLSBGPopMoreSelView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                            withDelegate:delegate];
    
    LSTPopView* popView = [self _createRSPopContainerWithContentView:moreView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出下麦视图
+ (LSTPopView*)popSBGDropLineViewWithParentView:(UIView*)parentView
                               withSeatModel:(VLSBGRoomSeatModel *)seatModel
                               withDelegate:(id<VLSBGDropOnLineViewDelegate>)delegate {
    CGFloat popViewH = 212 + kSafeAreaBottomHeight + 32;
    VLSBGDropOnLineView* dropLineView = [[VLSBGDropOnLineView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                               withDelegate:delegate];
    dropLineView.seatModel = seatModel;
    
    LSTPopView* popView = [self _createRSPopContainerWithContentView:dropLineView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}


//弹出美声视图
+ (LSTPopView*)popSBGBelcantoViewWithParentView:(UIView*)parentView
                           withBelcantoModel:(VLSBGBelcantoModel *)belcantoModel
                                withDelegate:(id<VLSBGAudioEffectPickerDelegate>)delegate {
    CGFloat popViewH = 175 + kSafeAreaBottomHeight;
    VLSBGAudioEffectPicker *belcantoView = [[VLSBGAudioEffectPicker alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                                        withDelegate:delegate];
    belcantoView.selBelcantoModel = belcantoModel;
    LSTPopView* popView = [self _createRSPopContainerWithContentView:belcantoView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出点歌视图
+ (LSTPopView*)popSBGUpChooseSongViewWithParentView:(UIView*)parentView
                                        isChorus:(BOOL)isChorus
                                 chooseSongArray: (NSArray*)chooseSongArray
                                      withRoomNo:(NSString*)roomNo
                                    withDelegate:(id<VLSBGPopSongListDelegate>)delegate {
    CGFloat popViewH = SCREEN_HEIGHT * 0.7;
    VLSBGPopSongList *chooseSongView = [[VLSBGPopSongList alloc]
                                           initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                           withDelegate:delegate
                                           withRoomNo:roomNo
                                           ifChorus:isChorus];
    chooseSongView.selSongsArray = chooseSongArray;
    chooseSongView = chooseSongView;
    LSTPopView* popView = [self _createRSPopContainerWithContentView:chooseSongView
                                                          withParentView:parentView];
    popView.isAvoidKeyboard = NO;
    [popView pop];
    
    return popView;
}

//弹出音效
+ (LSTPopView*)popSBGSetSoundEffectViewWithParentView:(UIView*)parentView
                                         soundView:(VLSBGEffectView*)soundView
                                      withDelegate:(id<VLSBGEffectViewDelegate>)delegate {
    CGFloat popViewH = 88+17+270+kSafeAreaBottomHeight;                                                                                     
    VLSBGEffectView* EffectView = [[VLSBGEffectView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                                                            withDelegate:delegate];
    
    LSTPopView* popView = [self _createRSPopContainerWithContentView:EffectView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出专业主播
+ (LSTPopView*)popSBGVoicePerViewWithParentView:(UIView*)parentView
                                         perView:(VLSBGVoicePerShowView*)perView
                                      withDelegate:(id<VLSBGVoicePerShowViewDelegate>)delegate {
   // CGFloat popViewH = 88+17+270+kSafeAreaBottomHeight;
    VLSBGVoicePerShowView* voiceView = [[VLSBGVoicePerShowView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, 120)
                                                                                            withDelegate:delegate ];
    
    LSTPopView* popView = [self _createRSPopContainerWithContentView:voiceView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出人声突出
+ (LSTPopView*)popSBGVoiceShowViewWithParentView:(UIView*)parentView
                                         showView:(VLSBGVoiceShowView*)showView
                                   dataSource:(NSArray *)array
                                      withDelegate:(id<VLSBGVoiceShowViewDelegate>)delegate {
    CGFloat popViewH = 88+17+270+kSafeAreaBottomHeight;
    VLSBGVoiceShowView* voiceView = [[VLSBGVoiceShowView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                                                            withDelegate:delegate dataSource:array];
    
    LSTPopView* popView = [self _createRSPopContainerWithContentView:voiceView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//网络差视图
+ (LSTPopView*)popSBGBadNetWrokTipViewWithParentView:(UIView*)parentView
                                     withDelegate:(id<VLSBGBadNetWorkViewDelegate>)delegate {
    CGFloat popViewH = 276;
    VLSBGBadNetWorkView* badNetView = [[VLSBGBadNetWorkView alloc]initWithFrame:CGRectMake(40, 0, SCREEN_WIDTH - 80, popViewH)
                                                             withDelegate:delegate];
    
    LSTPopView *popView = [LSTPopView initWithCustomView:badNetView
                                              parentView:parentView
                                                popStyle:LSTPopStyleFade
                                            dismissStyle:LSTDismissStyleFade];
    popView.hemStyle = LSTHemStyleCenter;
    popView.popDuration = 0.5;
    popView.dismissDuration = 0.5;
    popView.cornerRadius = 20;
    popView.isClickFeedback = NO;
    
    [popView pop];
    
    return popView;
}

//控制台
+ (LSTPopView*)popSBGSettingViewWithParentView:(UIView*)parentView
                               settingView:(VLSBGSettingView*)settingView
                               withDelegate:(id<VLSBGSettingViewDelegate>)delegate {
    VLSBGSettingView* _settingView = settingView ? settingView : [[VLSBGSettingView alloc] initWithSetting:nil];
    _settingView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 300);
    _settingView.backgroundColor = UIColorMakeWithHex(@"#152164");
    [_settingView vl_radius:20 corner:UIRectCornerTopLeft | UIRectCornerTopRight];
    _settingView.delegate = delegate;
    
    LSTPopView* popView = [self _createRSPopContainerWithContentView:_settingView
                                                       withParentView:parentView];
    popView.isAvoidKeyboard = NO;
    [popView pop];
    
    return popView;
}
@end
