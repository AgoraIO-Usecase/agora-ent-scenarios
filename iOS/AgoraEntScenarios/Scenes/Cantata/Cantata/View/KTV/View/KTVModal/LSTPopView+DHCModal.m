//
//  LSTPopView+KTVModal.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import "LSTPopView+DHCModal.h"
#import "VLEffectView.h"
#import "VLEarSettingView.h"
@implementation LSTPopView (DHCModal)

#pragma mark private method
//公共弹窗视图设置
+ (LSTPopView*)_createDHCPopContainerWithContentView:(UIView*)contentView
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

+ (LSTPopView*)getDHCPopViewWithCustomView:(UIView*)customView {
    UIView* superView = customView.superview;
    while (superView != nil) {
        if ([superView isKindOfClass:[LSTPopView class]]) {
            return (LSTPopView*)superView;
        }
        
        superView = superView.superview;
    }
    
    return nil;
}

+ (LSTPopView*)popDHCSelMVBgViewWithParentView:(UIView*)parentView
                                    bgModel:(VLKTVSelBgModel*)bgModel
                               withDelegate:(id<VLPopSelBgViewDelegate>)delegate {
    CGFloat popViewH = (SCREEN_WIDTH - 60) / 3.0 * 0.75 * 3 + 100 + kSafeAreaBottomHeight;
    VLPopSelBgView *changeBgView = [[VLPopSelBgView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                            withDelegate:delegate];
    changeBgView.selBgModel = bgModel;
    
    LSTPopView* popView = [self _createDHCPopContainerWithContentView:changeBgView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出更多
+ (LSTPopView*)popDHCSelMoreViewWithParentView:(UIView*)parentView
                               withDelegate:(id<VLPopMoreSelViewDelegate>)delegate {
    CGFloat popViewH = 190 + kSafeAreaBottomHeight;
    VLPopMoreSelView *moreView = [[VLPopMoreSelView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                            withDelegate:delegate];
    
    LSTPopView* popView = [self _createDHCPopContainerWithContentView:moreView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出下麦视图
+ (LSTPopView*)popDHCDropLineViewWithParentView:(UIView*)parentView
                               withSeatModel:(VLRoomSeatModel *)seatModel
                               withDelegate:(id)delegate {
    CGFloat popViewH = 212 + kSafeAreaBottomHeight + 32;
    VLDropOnLineView* dropLineView = [[VLDropOnLineView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                               withDelegate:delegate];
    dropLineView.seatModel = seatModel;
    
    LSTPopView* popView = [self _createDHCPopContainerWithContentView:dropLineView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}


//弹出美声视图
+ (LSTPopView*)popDHCBelcantoViewWithParentView:(UIView*)parentView
                           withBelcantoModel:(VLBelcantoModel *)belcantoModel
                                withDelegate:(id<VLAudioEffectPickerDelegate>)delegate {
    CGFloat popViewH = 175 + kSafeAreaBottomHeight;
    VLAudioEffectPicker *belcantoView = [[VLAudioEffectPicker alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                                        withDelegate:delegate];
    belcantoView.selBelcantoModel = belcantoModel;
    LSTPopView* popView = [self _createDHCPopContainerWithContentView:belcantoView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

////弹出点歌视图
//+ (LSTPopView*)popUpChooseSongViewWithParentView:(UIView*)parentView
//                                        isChorus:(BOOL)isChorus
//                                 chooseSongArray: (NSArray*)chooseSongArray
//                                      withRoomNo:(NSString*)roomNo
//                                    withDelegate:(id<VLPopSongListDelegate>)delegate {
//    CGFloat popViewH = SCREEN_HEIGHT * 0.7;
//    VLPopSongList *chooseSongView = [[VLPopSongList alloc]
//                                           initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
//                                           withDelegate:delegate
//                                           withRoomNo:roomNo
//                                           ifChorus:isChorus];
//    chooseSongView.selSongsArray = chooseSongArray;
//    chooseSongView = chooseSongView;
//    LSTPopView* popView = [self _createKTVPopContainerWithContentView:chooseSongView
//                                                          withParentView:parentView];
//    popView.isAvoidKeyboard = NO;
//    [popView pop];
//
//    return popView;
//}

//弹出音效
+ (LSTPopView*)popDHCSetSoundEffectViewWithParentView:(UIView*)parentView
                                         soundView:(VLEffectView*)soundView
                                      withDelegate:(id<VLEffectViewDelegate>)delegate {
    CGFloat popViewH = 88+17+270+kSafeAreaBottomHeight;                                                                                     
    VLEffectView* EffectView = [[VLEffectView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                                                            withDelegate:delegate];
    
    LSTPopView* popView = [self _createDHCPopContainerWithContentView:EffectView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//网络差视图
+ (LSTPopView*)popDHCBadNetWrokTipViewWithParentView:(UIView*)parentView
                                     withDelegate:(id<VLBadNetWorkViewDelegate>)delegate {
    CGFloat popViewH = 276;
    VLBadNetWorkView* badNetView = [[VLBadNetWorkView alloc]initWithFrame:CGRectMake(40, 0, SCREEN_WIDTH - 80, popViewH)
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
+ (LSTPopView*)popDHCSettingViewWithParentView:(UIView*)parentView
                               settingView:(nullable DHCVLKTVSettingView*)settingView
                               withDelegate:(id)delegate {
    VLKTVSettingView* _settingView = settingView ? settingView : [[DHCVLKTVSettingView alloc] initWithSetting:nil];
    _settingView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 400);
    _settingView.backgroundColor = UIColorMakeWithHex(@"#152164");
    //cp todo
  //  [_settingView vl_radius:20 corner:UIRectCornerTopLeft | UIRectCornerTopRight];
    _settingView.delegate = delegate;
    
    LSTPopView* popView = [self _createDHCPopContainerWithContentView:_settingView
                                                       withParentView:parentView];
    popView.isAvoidKeyboard = NO;
    [popView pop];
    
    return popView;
}

//弹出耳返视图
+ (LSTPopView*)popDHCEarSettingViewWithParentView:(UIView*)parentView
                                   isEarOn:(BOOL)isEarOn
                                           vol:(CGFloat)vol
                                  withDelegate:(id<VLEarSettingViewViewDelegate>)delegate {
    CGFloat popViewH = 88+17+270+kSafeAreaBottomHeight;
    VLEarSettingView *earView = [[VLEarSettingView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) isEarOn:isEarOn vol:vol withDelegate:delegate];
    
    LSTPopView* popView = [self _createDHCPopContainerWithContentView:earView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出DebugView
+ (LSTPopView*)popDHCDebugViewWithParentView:(UIView*)parentView
                                   isDebugMode:(BOOL)isDebugMode
                             withDelegate:(id<DHCDebugViewDelegate>)delegate {
    CGFloat popViewH = 480;
    DHCDebugView *debugView = [[DHCDebugView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) isDumpMode:isDebugMode withDelegate:delegate];
    
    LSTPopView* popView = [self _createDHCPopContainerWithContentView:debugView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

@end
