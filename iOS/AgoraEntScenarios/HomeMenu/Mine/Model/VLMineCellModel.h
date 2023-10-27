//
//  VLMineCellModel.h
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/2/6.
//

#import <Foundation/Foundation.h>
#import "VLMineView.h"

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    VLMineCellStyleDefault,
    VLMineCellStyleSwitch,
} VLMineCellStyle;

@interface VLMineCellModel : NSObject

@property (nonatomic, copy) NSString *itemImgStr;
@property (nonatomic, copy) NSString *titleStr;
@property (nonatomic, assign) VLMineCellStyle style;
@property (nonatomic, assign) VLMineViewClickType clickType;

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title;

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title style: (VLMineCellStyle)style;

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title style: (VLMineCellStyle)style clickType:(VLMineViewClickType)clickType;

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title clickType: (VLMineViewClickType)clickType;

@end

NS_ASSUME_NONNULL_END
