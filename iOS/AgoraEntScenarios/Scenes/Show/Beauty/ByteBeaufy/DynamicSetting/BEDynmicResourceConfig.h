//
//  BEDynmicResourceConfig.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/11.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BEDynmicResourceConfig : NSObject

+ (instancetype)shareInstance;

/// 字节美颜资源目录
@property (nonatomic, copy, nullable) NSString* resourceFolderPath;

/// 字节美颜lic路径
@property (nonatomic, copy, nullable) NSString* licFilePath;
@end

NS_ASSUME_NONNULL_END
