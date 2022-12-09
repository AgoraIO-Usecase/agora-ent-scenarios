//
//  LEEAlert+KTVModal.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/9.
//

#import <LEEAlert/LEEAlert.h>


@interface LEEAlert (KTVModal)
//用户弹框离开房间
+ (void)popForceLeaveRoomDialogWithCompletion:(void(^)(void))completion;

+ (void)popRemoveRoomDialogWithCancelBlock:(void(^)(void))cancelBlock
                             withDoneBlock:(void(^)(void))doneBlock;

+ (void)popLeaveRoomDialogWithCancelBlock:(void(^)(void))cancelBlock
                            withDoneBlock:(void(^)(void))doneBlock;

+ (void)popSwitchSongDialogWithCancelBlock:(void(^)(void))cancelBlock
                             withDoneBlock:(void(^)(void))doneBlock;
@end

