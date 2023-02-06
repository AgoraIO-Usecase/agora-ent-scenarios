//
//  SenseArMaterial.h
//  SenseArMaterial
//
//  Created by sluin on 16/10/8.
//  Copyright © 2016年 SenseTime. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SenseArMaterialAction.h"

/**
 素材类型
 */
typedef enum : NSUInteger {
    
    // 直播特效
    CPT_AD = 1,
    
    // 直播前贴片
    LIVE_PRE_AD,
    
    // 趣味特效
    SPECIAL_EFFECT,
    
    // 短视频特效
    SMALL_VIDEO_EFFECT,
    
    // 拍摄入口前贴片
    CAMERA_PRE_AD,
    
    // 拍摄入口特效
    CAMERA_EFFECT,
    
    // 直播特效列表
    LIVE_EFFECT,
    
    // 短视频前贴片
    SMALL_VIDEO_PRE_AD
    
} SenseArMaterialType;


@interface SenseArMaterial : NSObject <NSCoding , NSMutableCopying>

/**
 *  素材ID
 */
@property (nonatomic , copy , readonly) NSString *strID;


/**
 *  素材文件ID
 */
@property (nonatomic , copy , readonly) NSString *strMaterialFileID;


/**
 *  素材类型
 */
@property (nonatomic , assign , readonly) SenseArMaterialType iMaterialType;


/**
 *  素材缩略图地址
 */
@property (nonatomic , copy , readonly) NSString *strThumbnailURL;


/**
 *  素材文件地址
 */
@property (nonatomic , copy , readonly) NSString *strMaterialURL;


/**
 *  素材文件本地路径
 */
@property (nonatomic , copy , readonly) NSString *strMaterialPath;


/**
 *  素材触发信息数组 , 触发信息包含触发的动作类型及触发动作的提示
 */
@property (nonatomic , copy , readonly) NSArray <SenseArMaterialAction *>* arrMaterialTriggerActions;


/**
 *  素材名称
 */
@property (nonatomic , copy , readonly) NSString *strName;


/**
 *  素材描述
 */
@property (nonatomic , copy , readonly) NSString *strInstructions;


/**
 *  话题
 */
@property (nonatomic , copy , readonly) NSString *strExtendInfo;


/**
 *  扩展信息
 */
@property (nonatomic , copy , readonly) NSString *strExtendInfo2;


/**
    请求标识
 */
@property (nonatomic , copy , readonly) NSString *strRequestID;


/**
    广告语
 */
@property (nonatomic , copy , readonly) NSString *strAdSlogen;


/**
    ideaID
 */
@property (nonatomic , copy , readonly) NSString *strIdeaID;


/**
 广告链接
 */
@property (nonatomic , copy , readonly) NSString *strAdLink;

@end
