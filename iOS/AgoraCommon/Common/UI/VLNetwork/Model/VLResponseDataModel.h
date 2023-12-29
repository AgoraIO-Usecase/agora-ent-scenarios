//
//  VLResponseDataModel.h
//  VoiceOnLine
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface VLResponseDataModel : NSObject

@property (nonatomic, copy)  NSString *message;
@property (nonatomic, assign)  NSInteger code;
@property (nonatomic, copy)  NSString *requestId;
@property (nonatomic, copy)  id data;

@end

NS_ASSUME_NONNULL_END
