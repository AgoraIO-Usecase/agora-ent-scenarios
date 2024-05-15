//
//  STDynmicResourceConfig.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/11.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface STDynmicResourceConfig : NSObject

+ (instancetype)shareInstance;

/// 商汤美颜资源目录
@property (nonatomic, copy, nullable) NSString* resourceFolderPath;

/// 商汤美颜lic路径
@property (nonatomic, copy, nullable) NSString* licFilePath;
@end

NS_ASSUME_NONNULL_END
