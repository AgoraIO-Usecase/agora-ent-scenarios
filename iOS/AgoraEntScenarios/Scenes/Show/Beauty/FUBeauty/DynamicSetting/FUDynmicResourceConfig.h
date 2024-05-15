//
//  FUDynmicResourceConfig.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/12.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface FUDynmicResourceConfig : NSObject

+ (instancetype)shareInstance;

/// 相芯美颜资源目录
@property (nonatomic, copy, nullable) NSString* resourceFolderPath;

/// 相芯美颜lic路径
@property (nonatomic, copy, nullable) NSString* licFilePath;

@end

NS_ASSUME_NONNULL_END
