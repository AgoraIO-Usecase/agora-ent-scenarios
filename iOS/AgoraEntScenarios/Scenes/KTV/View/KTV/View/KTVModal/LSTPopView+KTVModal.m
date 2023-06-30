//
//  LSTPopView+KTVModal.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/10.
//

#import "LSTPopView+KTVModal.h"
#import "VLMacroDefine.h"
#import "KTVMacro.h"
#import "UIView+VL.h"
#import "VLEffectView.h"
#import "VLEarSettingView.h"
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
                                withDelegate:(id<VLAudioEffectPickerDelegate>)delegate {
    CGFloat popViewH = 175 + kSafeAreaBottomHeight;
    VLAudioEffectPicker *belcantoView = [[VLAudioEffectPicker alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
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
                                    withDelegate:(id<VLPopSongListDelegate>)delegate {
    CGFloat popViewH = SCREEN_HEIGHT * 0.7;
    VLPopSongList *chooseSongView = [[VLPopSongList alloc]
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
                                         soundView:(VLEffectView*)soundView
                                      withDelegate:(id<VLEffectViewDelegate>)delegate {
    CGFloat popViewH = 88+17+270+kSafeAreaBottomHeight;                                                                                     
    VLEffectView* EffectView = [[VLEffectView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH)
                                                                                            withDelegate:delegate];
    
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:EffectView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出专业主播
+ (LSTPopView*)popVoicePerViewWithParentView:(UIView*)parentView
                              isProfessional: (BOOL) isProfessional
                                     isDelay: (BOOL) isDelay
                                    volGrade:(NSInteger)volGrade
                                       grade:(NSInteger)grade
                                    isRoomOwner: (BOOL) isRoomOwner
                                         perView:(VLVoicePerShowView*)perView
                                      withDelegate:(id<VLVoicePerShowViewDelegate>)delegate {
    VLVoicePerShowView* voiceView = [[VLVoicePerShowView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, isRoomOwner ? 316 : 244)
                                                                                            isProfessional:isProfessional
                                                                                            isDelay:isDelay
                                                                                            isRoomOwner:isRoomOwner
                                                                                            volGrade:volGrade
                                                                                            aecGrade:grade
                                                                                            withDelegate:delegate ];
    
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:voiceView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出人声突出
+ (LSTPopView*)popVoiceShowViewWithParentView:(UIView*)parentView
                                         showView:(VLVoiceShowView*)showView
                                   imgSource:(NSArray *)imgSource
                                    nameSource:(NSArray *)nameSource
                                  selectIndex:(NSInteger)index
                                      withDelegate:(id<VLVoiceShowViewDelegate>)delegate {
    CGFloat popViewH = 88+17+ (imgSource.count > 4 ? 200 : 100)+kSafeAreaBottomHeight;
    VLVoiceShowView* voiceView = [[VLVoiceShowView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) withDelegate:delegate imgSource:imgSource nameSource:nameSource selectIndex:index
                                                                                             ];
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:voiceView
                                                       withParentView:parentView];
    [popView pop];
    
    return popView;
}

//弹出耳返视图
+ (LSTPopView*)popEarSettingViewWithParentView:(UIView*)parentView
                                   isEarOn:(BOOL)isEarOn
                                           vol:(CGFloat)vol
                                      withDelegate:(id<VLEarSettingViewViewDelegate>)delegate {
    CGFloat popViewH = 88+17+270+kSafeAreaBottomHeight;
    VLEarSettingView *earView = [[VLEarSettingView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) isEarOn:isEarOn vol:vol withDelegate:delegate];
    
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:earView
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

//控制台
+ (LSTPopView*)popSettingViewWithParentView:(UIView*)parentView
                               settingView:(VLKTVSettingView*)settingView
                               withDelegate:(id<VLKTVSettingViewDelegate>)delegate {
    VLKTVSettingView* _settingView = settingView ? settingView : [[VLKTVSettingView alloc] initWithSetting:nil];
    _settingView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 340);
    _settingView.backgroundColor = UIColorMakeWithHex(@"#152164");
    [_settingView vl_radius:20 corner:UIRectCornerTopLeft | UIRectCornerTopRight];
    _settingView.delegate = delegate;
    
    LSTPopView* popView = [self _createKTVPopContainerWithContentView:_settingView
                                                       withParentView:parentView];
    popView.isAvoidKeyboard = NO;
    [popView pop];
    
    return popView;
}
@end
