//
//  LSTPopView+KTVModal.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import "LSTPopView+KTVModal.h"
#import "VLMacroDefine.h"
@import QMUIKit;

@implementation LSTPopView (KTVModal)

#pragma mark private method
//公共弹窗视图设置
+ (LSTPopView*)_createKTVPopContainerWithContentView:(UIView*)contentView
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

+ (LSTPopView*)getPopViewWithCustomView:(UIView*)customView {
    UIView* superView = customView.superview;
    while (superView != nil) {
        if ([superView isKindOfClass:[LSTPopView class]]) {
            return (LSTPopView*)superView;
        }
        
        superView = superView.superview;
    }
    
    return nil;
}

+ (LSTPopView*)popSelMVBgViewWithParentView:(UIView*)parentView
                                    bgModel:(VLKTVSelBgModel*)bgModel
                               withDelegate:(id<VLPopSelBgViewDelegate>)delegate {
    CGFloat popViewH = (SCREEN_WIDTH - 60) / 3.0 * 0.75 * 3 + 100 + kSafeAreaBottomHeight;
    VLPopSelBgView *changeBgView = [[VLPopSelBgView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                            withDelegate:delegate];
    changeBgView.selBgModel = bgModel;
    
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:changeBgView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出更多
+ (LSTPopView*)popSelMoreViewWithParentView:(UIView*)parentView
                               withDelegate:(id<VLPopMoreSelViewDelegate>)delegate {
    CGFloat popViewH = 190 + kSafeAreaBottomHeight;
    VLPopMoreSelView *moreView = [[VLPopMoreSelView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                            withDelegate:delegate];
    
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:moreView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出下麦视图
+ (LSTPopView*)popDropLineViewWithParentView:(UIView*)parentView
                               withSeatModel:(VLRoomSeatModel *)seatModel
                               withDelegate:(id<VLDropOnLineViewDelegate>)delegate {
    CGFloat popViewH = 212 + kSafeAreaBottomHeight + 32;
    VLDropOnLineView* dropLineView = [[VLDropOnLineView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                               withDelegate:delegate];
    dropLineView.seatModel = seatModel;
    
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:dropLineView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}


//弹出美声视图
+ (LSTPopView*)popBelcantoViewWithParentView:(UIView*)parentView
                           withBelcantoModel:(VLBelcantoModel *)belcantoModel
                                withDelegate:(id<VLChooseBelcantoViewDelegate>)delegate {
    CGFloat popViewH = 175 + kSafeAreaBottomHeight;
    VLChooseBelcantoView *belcantoView = [[VLChooseBelcantoView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                                        withDelegate:delegate];
    belcantoView.selBelcantoModel = belcantoModel;
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:belcantoView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出点歌视图
+ (LSTPopView*)popUpChooseSongViewWithParentView:(UIView*)parentView
                                        isChorus:(BOOL)isChorus
                                 chooseSongArray: (NSArray*)chooseSongArray
                                      withRoomNo:(NSString*)roomNo
                                    withDelegate:(id<VLPopChooseSongViewDelegate>)delegate {
    CGFloat popViewH = SCREEN_HEIGHT * 0.7;
    VLPopChooseSongView *chooseSongView = [[VLPopChooseSongView alloc]
                                           initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                           withDelegate:delegate
                                           withRoomNo:roomNo
                                           ifChorus:isChorus];
    chooseSongView.selSongsArray = chooseSongArray;
    chooseSongView = chooseSongView;
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:chooseSongView
                                                          withParentView:parentView];
    popView.isAvoidKeyboard = NO;
    [popView pop];
    
    return popView;
}

//弹出音效
+ (LSTPopView*)popSetSoundEffectViewWithParentView:(UIView*)parentView
                                      withDelegate:(id<VLsoundEffectViewDelegate>)delegate {
    CGFloat popViewH = 88+17+270+kSafeAreaBottomHeight;
    VLsoundEffectView* soundEffectView = [[VLsoundEffectView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                                    withDelegate:delegate];
    
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:soundEffectView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//网络差视图
+ (LSTPopView*)popBadNetWrokTipViewWithParentView:(UIView*)parentView
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
@end
