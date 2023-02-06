//
//  VLMineCellModel.h
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/2/6.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    VLMineCellStyleDefault,
    VLMineCellStyleSwitch,
} VLMineCellStyle;

@interface VLMineCellModel : NSObject

@property (nonatomic, copy) NSString *itemImgStr;
@property (nonatomic, copy) NSString *titleStr;
@property (nonatomic, assign) VLMineCellStyle style;

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title;

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title style: (VLMineCellStyle)style;

@end

NS_ASSUME_NONNULL_END
