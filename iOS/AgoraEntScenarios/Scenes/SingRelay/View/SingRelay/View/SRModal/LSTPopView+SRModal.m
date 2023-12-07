//
//  LSTPopView+KTVModal.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import "LSTPopView+SRModal.h"
#import "VLMacroDefine.h"
#import "AESMacro.h"
#import "UIView+VL.h"

@implementation LSTPopView (SRModal)

#pragma mark private method
+ (LSTPopView*)_createSRPopContainerWithContentView:(UIView*)contentView
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
+ (LSTPopView*)getSRPopViewWithCustomView:(UIView*)customView {
    UIView* superView = customView.superview;
    while (superView != nil) {
        if ([superView isKindOfClass:[LSTPopView class]]) {
            return (LSTPopView*)superView;
        }
        
        superView = superView.superview;
    }
    
    return nil;
}

+ (LSTPopView*)popSRSelMVBgViewWithParentView:(UIView*)parentView
                                    bgModel:(VLSRSelBgModel*)bgModel
                               withDelegate:(id<VLSRPopSelBgViewDelegate>)delegate {
    CGFloat popViewH = (SCREEN_WIDTH - 60) / 3.0 * 0.75 * 3 + 100 + kSafeAreaBottomHeight;
    VLSRPopSelBgView *changeBgView = [[VLSRPopSelBgView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                            withDelegate:delegate];
    changeBgView.selBgModel = bgModel;
    
    LSTPopView* popView = [self _createSRPopContainerWithContentView:changeBgView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出更多
+ (LSTPopView*)popSRSelMoreViewWithParentView:(UIView*)parentView
                               withDelegate:(id<VLSRPopMoreSelViewDelegate>)delegate {
    CGFloat popViewH = 190 + kSafeAreaBottomHeight;
    VLSRPopMoreSelView *moreView = [[VLSRPopMoreSelView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                            withDelegate:delegate];
    
    LSTPopView* popView = [self _createSRPopContainerWithContentView:moreView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出下麦视图
+ (LSTPopView*)popSRDropLineViewWithParentView:(UIView*)parentView
                               withSeatModel:(VLSRRoomSeatModel *)seatModel
                               withDelegate:(id<VLSRDropOnLineViewDelegate>)delegate {
    CGFloat popViewH = 212 + kSafeAreaBottomHeight + 32;
    VLSRDropOnLineView* dropLineView = [[VLSRDropOnLineView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                               withDelegate:delegate];
    dropLineView.seatModel = seatModel;
    
    LSTPopView* popView = [self _createSRPopContainerWithContentView:dropLineView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}


//弹出美声视图
+ (LSTPopView*)popSRBelcantoViewWithParentView:(UIView*)parentView
                           withBelcantoModel:(VLSRBelcantoModel *)belcantoModel
                                withDelegate:(id<VLSRAudioEffectPickerDelegate>)delegate {
    CGFloat popViewH = 175 + kSafeAreaBottomHeight;
    VLSRAudioEffectPicker *belcantoView = [[VLSRAudioEffectPicker alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                                        withDelegate:delegate];
    belcantoView.selBelcantoModel = belcantoModel;
    LSTPopView* popView = [self _createSRPopContainerWithContentView:belcantoView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出点歌视图
+ (LSTPopView*)popSRUpChooseSongViewWithParentView:(UIView*)parentView
                                        isChorus:(BOOL)isChorus
                                 chooseSongArray: (NSArray*)chooseSongArray
                                      withRoomNo:(NSString*)roomNo
                                    withDelegate:(id<VLSRPopSongListDelegate>)delegate {
    CGFloat popViewH = SCREEN_HEIGHT * 0.7;
    VLSRPopSongList *chooseSongView = [[VLSRPopSongList alloc]
                                           initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                           withDelegate:delegate
                                           withRoomNo:roomNo
                                           ifChorus:isChorus];
    chooseSongView.selSongsArray = chooseSongArray;
    chooseSongView = chooseSongView;
    LSTPopView* popView = [self _createSRPopContainerWithContentView:chooseSongView
                                                          withParentView:parentView];
    popView.isAvoidKeyboard = NO;
    [popView pop];
    
    return popView;
}

//网络差视图
+ (LSTPopView*)popSRBadNetWrokTipViewWithParentView:(UIView*)parentView
                                     withDelegate:(id<VLSRBadNetWorkViewDelegate>)delegate {
    CGFloat popViewH = 276;
    VLSRBadNetWorkView* badNetView = [[VLSRBadNetWorkView alloc]initWithFrame:CGRectMake(40, 0, SCREEN_WIDTH - 80, popViewH)
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
+ (LSTPopView*)popSRSettingViewWithParentView:(UIView*)parentView
                               settingView:(VLSRSettingView*)settingView
                               withDelegate:(id<VLSRSettingViewDelegate>)delegate {
    VLSRSettingView* _settingView = settingView ? settingView : [[VLSRSettingView alloc] initWithSetting:nil];
    _settingView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 300);
    _settingView.backgroundColor = UIColorMakeWithHex(@"#152164");
    [_settingView vl_radius:20 corner:UIRectCornerTopLeft | UIRectCornerTopRight];
    _settingView.delegate = delegate;
    
    LSTPopView* popView = [self _createSRPopContainerWithContentView:_settingView
                                                       withParentView:parentView];
    popView.isAvoidKeyboard = NO;
    [popView pop];
    
    return popView;
}

//弹出DebugView
+ (LSTPopView*)popSRDebugViewWithParentView:(UIView*)parentView
                                    channelName:(NSString *)name
                                   sdkVer:(NSString *)ver
                                   isDebugMode:(BOOL)isDebugMode
                             withDelegate:(id<VLSRDebugViewDelegate>)delegate {
    CGFloat popViewH = 480;
    VLSRDebugView *debugView = [[VLSRDebugView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) channelName:(NSString *)name sdkVer:(NSString *)ver isDumpMode:isDebugMode withDelegate:delegate];
    
    LSTPopView* popView = [self _createSRPopContainerWithContentView:debugView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}
@end
