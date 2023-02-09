//
//  SenseArMaterialService.h
//  SenseArMaterial
//
//  Created by sluin on 16/10/9.
//  Copyright © 2016年 SenseTime. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "SenseArMaterialGroup.h"
#import "SenseArMaterial.h"







/**
 服务器类型
 */
typedef enum : NSUInteger {
    
    /**
     国内正式服务器
     */
    DomesticServer,
  
    /**
     国际正式服务器
     */
    InternationalServer
    
} SenseArServerType;





/**
 鉴权错误码
 */
typedef enum : NSUInteger {
    
    /**
     无效 AppID/SDKKey
     */
    AUTHORIZE_ERROR_KEY_NOT_MATCHED = 1,
    
    /**
     网络不可用
     */
    AUTHORIZE_ERROR_NETWORK_NOT_AVAILABLE,
    
    /**
     解密失败
     */
    AUTHORIZE_ERROR_DECRYPT_FAILED,
    
    /**
     解析失败
     */
    AUTHORIZE_ERROR_DATA_PARSE_FAILED,
    
    /**
     无效TOKEN
     */
    TOKEN_INVALID,
    
    /**
     未知错误
     */
    AUTHORIZE_ERROR_UNKNOWN,
    
} SenseArAuthorizeError;



/*
 最大缓存值 , 不启用 LRU 淘汰规则 .
 */
FOUNDATION_EXTERN const int SENSEAR_CACHE_SIZE_MAX;

@interface SenseArMaterialService : NSObject

/**
 获取 Material 服务
 
 @return 合作商对应的 Material 服务
 */
+ (SenseArMaterialService *)sharedInstance;


/**
 释放资源
 */
+ (void)releaseResources;




/**
 远程授权,未授权的 service 不可用
 
 @param strAppID        注册的合作商 ID
 @param strAppKey       注册的合作商 key
 @param completeSuccess 注册成功后的回调
 @param completeFailure 注册失败后的回调 , iErrorCode : 错误码 , errMessage : 错误原因
 */
- (void)authorizeWithAppID:(NSString *)strAppID
                    appKey:(NSString *)strAppKey
                 onSuccess:(void (^)(void))completeSuccess
                 onFailure:(void (^)(SenseArAuthorizeError iErrorCode, NSString * errMessage))completeFailure;


/**
 获取托管在服务器的 license 数据用于 generateActiveCode / checkActiveCode , 需要在 authorizeWithAppID 成功的回调中使用

 @return license 数据
 */
- (NSData *)getLicenseData;




/**
 服务是否授权
 
 @return YES : 已授权 , NO : 未授权
 */
+ (BOOL)isAuthorized;




/**
 获取分组列表
 
 @param completeSuccess 获取分组列表成功 , arrMaterialGroups 分组列表 .
 @param completeFailure 获取分组列表失败 , iErrorCode 错误码 , strMessage 错误描述 .
 */
- (void)fetchAllGroupsOnSuccess:(void (^)(NSArray <SenseArMaterialGroup *>* arrMaterialGroups))completeSuccess
                      onFailure:(void (^)(int iErrorCode , NSString *strMessage))completeFailure;

/**
 获取 Material 列表
 
 @param strUserID       用户ID , 如 主播ID, 粉丝ID 等 .
 @param strGroupID      素材所在组的 groupID
 @param completeSuccess 获取素材列表成功 , arrMaterias 素材列表 .
 @param completeFailure 获取素材列表失败 , iErrorCode 错误码 , strMessage 错误描述 .
 */
- (void)fetchMaterialsWithUserID:(NSString *)strUserID
                         GroupID:(NSString *)strGroupID
                       onSuccess:(void (^)(NSArray <SenseArMaterial *>* arrMaterials))completeSuccess
                       onFailure:(void (^)(int iErrorCode , NSString *strMessage))completeFailure;



/**
 通过 userID , materialID 获取单个素材
 
 @param strUserID 用户标识
 @param strMaterialID 素材标识
 @param completeSuccess 获取素材成功 , material 获取的素材 .
 @param completeFailure 获取失败 , iErrorCode 错误码 , strMessage 错误描述 .
 */
- (void)fetchMaterialWithUserID:(NSString *)strUserID
                     materialID:(NSString *)strMaterialID
                      onSuccess:(void (^)(SenseArMaterial *material))completeSuccess
                      onFailure:(void (^)(int iErrorCode , NSString *strMessage))completeFailure;



/**
 素材是否已下载
 
 @param material 需要判断是否已下载的素材
 
 @return YES 已下载 , NO 未下载或下载中 .
 */
- (BOOL)isMaterialDownloaded:(SenseArMaterial *)material;


- (NSString *)getDownloadedMaterialLocalPathBy:(NSString *)strMaterialFileID;

/**
 下载素材
 
 @param material        下载的素材
 @param completeSuccess 下载成功 , material 下载的素材
 @param completeFailure 下载失败 , material 下载的素材 , iErrorCode 错误码 , strMessage 错误描述
 @param processingCallBack 下载中 , material 下载的素材 , fProgress 下载进度 , iSize 已下载大小
 */
- (void)downloadMaterial:(SenseArMaterial *)material
               onSuccess:(void (^)(SenseArMaterial *material))completeSuccess
               onFailure:(void (^)(SenseArMaterial *material , int iErrorCode , NSString *strMessage))completeFailure
              onProgress:(void (^)(SenseArMaterial *material , float fProgress , int64_t iSize))processingCallBack;

/**
 素材任务取消
 
 @param material 取消下载的素材
 @param resultCallBack 任务取消回调 , material 取消下载的素材 , iErrorCode 错误码 , strMessage 错误描述
 */
- (void)cancelDownloadingMaterial:(SenseArMaterial *)material
                         onResult:(void (^)(SenseArMaterial *material, int iErrorCode, NSString *strMessage))resultCallBack;

/**
 设置素材缓存大小 , 默认 100M 超过限制会遵循LRU淘汰规则删除已有素材包 . 如果不需要设置最大缓存可以设置为 SENSEAR_CACHE_SIZE_MAX 来禁用 LRU 淘汰规则 .
 
 @param iSize 缓存大小 (Byte)
 */
- (void)setMaxCacheSize:(int64_t)iSize;

/**
 获取素材缓存所占用的空间
 
 @return 素材缓存所占用的空间 (单位:Byte)
 */
- (int64_t)getMaterialCacheSize;

/**
 清除缓存 , 清除内存缓存和素材磁盘缓存 , 但是会保留必要的数据库文件等 .
 */
- (void)clearCache;


/**
 获取 SDK 版本号
 
 @return SDK 版本号
 */
+ (NSString *)getSdkVersion;






/**
 删除单个素材缓存

 @param  material 要删除缓存的素材
 @return 是否成功 YES：成功 NO：失败
 */
- (BOOL)clearCacheWithMaterial:(SenseArMaterial *)material;



/**
 切换服务器 , 需要先于其他网络接口调用

 @param iServerType 服务器类型
 */
+ (void)switchToServerType:(SenseArServerType)iServerType;


@end
