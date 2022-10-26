//
//  VLBelcantoModel.h
//  VoiceOnLine
//

#import "VLBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLBelcantoModel : VLBaseModel

@property (nonatomic, copy) NSString *imageName;
@property (nonatomic, copy) NSString *titleStr;
@property (nonatomic, assign) BOOL ifSelect;

@end

NS_ASSUME_NONNULL_END
