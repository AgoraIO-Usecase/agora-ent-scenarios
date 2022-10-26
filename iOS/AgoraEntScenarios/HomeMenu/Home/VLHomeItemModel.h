//
//  VLHomeItemModel.h
//  VoiceOnLine
//

#import "VLBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLHomeItemModel : VLBaseModel

@property (nonatomic, copy) NSString *bgImgStr;
@property (nonatomic, copy) NSString *iconImgStr;
@property (nonatomic, copy) NSString *titleStr;
@property (nonatomic, copy) NSString *subTitleStr;

@end

NS_ASSUME_NONNULL_END
