//
//  SenseArMaterialAction.h
//  SenseAr
//
//  Created by sluin on 2017/4/26.
//  Copyright © 2017年 SenseTime. All rights reserved.
//

#import <Foundation/Foundation.h>


/**
 触发动作
 */
typedef enum : NSUInteger {
    
    // 张嘴
    MOUTH_AH = 1,
    
    // 眨眼
    EYE_BLINK,
    
    // 点头
    HEAD_PITCH,
    
    // 摇头
    HEAD_YAW,
    
    // 挑眉
    BROW_JUMP,
    
    // 手掌
    HAND_PALM,
    
    // 大拇哥
    HAND_GOOD,
    
    // 托手
    HAND_HOLDUP,
    
    // 爱心手
    HAND_LOVE,
    
    // 恭贺(抱拳)
    HAND_CONGRATULATE,
    
    // 单手比爱心
    HAND_FINGER_HEART,
    
    // 平行手指
    HAND_TWO_INDEX_FINGER,
    
    // OK手势
    HAND_OK,
    
    // 剪刀手
    HAND_SCISSOR,
    
    // 手枪
    HAND_PISTOL,
    
    // 指尖
    FINGER_INDEX,
    
    //666
    HAND_666,
    
    //双手合十
    HAND_BLESS,
    
    //手势ILoveYou
    HAND_ILOVEYOU
    
} SenseArTriggerAction;



@interface SenseArMaterialAction : NSObject <NSCopying , NSCoding>



/**
 *  触发动作
 */
@property (nonatomic , assign , readonly) SenseArTriggerAction iTriggerAction;

/**
 *  触发动作描述
 */
@property (nonatomic , copy , readonly) NSString *strTriggerActionTip;




@end
