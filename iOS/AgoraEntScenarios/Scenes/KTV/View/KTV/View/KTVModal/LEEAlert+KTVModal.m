//
//  LEEAlert+KTVModal.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/9.
//

#import "LEEAlert+KTVModal.h"
#import "KTVMacro.h"
@import QMUIKit;

@implementation LEEAlert (KTVModal)
+ (void)popForceLeaveRoomDialogWithCompletion:(void(^)(void))completion {
    [LEEAlert alert].config
        .LeeAddTitle(^(UILabel *label) {
            label.text = KTVLocalizedString(@"房主已解散房间,请确认离开房间");
            label.textColor = UIColorMakeWithHex(@"#040925");
            label.font = UIFontBoldMake(16);
        })
        .LeeAddAction(^(LEEAction *action) {
            action.type = LEEActionTypeCancel;
            action.title = KTVLocalizedString(@"确定");
            action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
            action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
            action.cornerRadius = 20;
            action.height = 40;
            action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
            action.font = UIFontBoldMake(16);
            action.clickBlock = ^{
                if (completion) {
                    completion();
                }
            };
        })
        .LeeShow();
}

+ (void)popRemoveRoomDialogWithCancelBlock:(void(^)(void))cancelBlock
                             withDoneBlock:(void(^)(void))doneBlock {
    [LEEAlert alert].config
    .LeeAddTitle(^(UILabel *label) {
        label.text = KTVLocalizedString(@"解散房间");
        label.textColor = UIColorMakeWithHex(@"#040925");
        label.font = UIFontBoldMake(16);
    })
    .LeeAddContent(^(UILabel *label) {
        label.text = KTVLocalizedString(@"确定解散该房间吗？");
        label.textColor = UIColorMakeWithHex(@"#6C7192");
        label.font = UIFontMake(14);
        
    })
    .LeeAddAction(^(LEEAction *action) {
        action.type = LEEActionTypeCancel;
        action.title = KTVLocalizedString(@"取消");
        action.titleColor = UIColorMakeWithHex(@"#000000");
        action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.font = UIFontBoldMake(16);
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
        action.clickBlock = ^{
            // 取消点击事件Block
            if (cancelBlock) {
                cancelBlock();
            }
        };
    })
    .LeeAddAction(^(LEEAction *action) {
        action.type = LEEActionTypeCancel;
        action.title = KTVLocalizedString(@"确定");
        action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
        action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.font = UIFontBoldMake(16);
        action.clickBlock = ^{
            if (doneBlock) {
                doneBlock();
            }
        };
    })
    .LeeShow();
}

+ (void)popLeaveRoomDialogWithCancelBlock:(void(^)(void))cancelBlock
                            withDoneBlock:(void(^)(void))doneBlock {
    [LEEAlert alert].config
    .LeeAddTitle(^(UILabel *label) {
        label.text = KTVLocalizedString(@"退出房间");
        label.textColor = UIColorMakeWithHex(@"#040925");
        label.font = UIFontBoldMake(16);
    })
    .LeeAddContent(^(UILabel *label) {
        label.text = KTVLocalizedString(@"确定退出该房间吗？");
        label.textColor = UIColorMakeWithHex(@"#6C7192");
        label.font = UIFontMake(14);
        
    })
    .LeeAddAction(^(LEEAction *action) {
        action.type = LEEActionTypeCancel;
        action.title = KTVLocalizedString(@"取消");
        action.titleColor = UIColorMakeWithHex(@"#000000");
        action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.font = UIFontBoldMake(16);
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
        action.clickBlock = ^{
            // 取消点击事件Block
            if (cancelBlock) {
                cancelBlock();
            }
        };
    })
    .LeeAddAction(^(LEEAction *action) {
        action.type = LEEActionTypeCancel;
        action.title = KTVLocalizedString(@"确定");
        action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
        action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.font = UIFontBoldMake(16);
        action.clickBlock = ^{
            if (doneBlock) {
                doneBlock();
            }
        };
    })
    .LeeShow();
}

+ (void)popSwitchSongDialogWithCancelBlock:(void(^)(void))cancelBlock
                             withDoneBlock:(void(^)(void))doneBlock {
    [LEEAlert alert].config
        .LeeAddTitle(^(UILabel *label) {
            label.text = KTVLocalizedString(@"切换歌曲");
            label.textColor = UIColorMakeWithHex(@"#040925");
            label.font = UIFontBoldMake(16);
        })
        .LeeAddContent(^(UILabel *label) {
            label.text = KTVLocalizedString(@"切换下一首歌曲？");
            label.textColor = UIColorMakeWithHex(@"#6C7192");
            label.font = UIFontMake(14);
            
        })
        .LeeAddAction(^(LEEAction *action) {
            action.type = LEEActionTypeCancel;
            action.title = KTVLocalizedString(@"取消");
            action.titleColor = UIColorMakeWithHex(@"#000000");
            action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
            action.cornerRadius = 20;
            action.height = 40;
            action.font = UIFontBoldMake(16);
            action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
            action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
            action.clickBlock = ^{
                // 取消点击事件Block
                if (cancelBlock) {
                    cancelBlock();
                }
            };
        })
        .LeeAddAction(^(LEEAction *action) {
            action.type = LEEActionTypeCancel;
            action.title = KTVLocalizedString(@"确定");
            action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
            action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
            action.cornerRadius = 20;
            action.height = 40;
            action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
            action.font = UIFontBoldMake(16);
            action.clickBlock = ^{
                if (doneBlock) {
                    doneBlock();
                }
            };
        })
        .LeeShow();
}


@end
