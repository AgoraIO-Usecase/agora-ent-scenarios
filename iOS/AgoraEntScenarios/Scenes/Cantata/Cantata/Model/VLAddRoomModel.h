//
//  VLAddRoomModel.h
//  VoiceOnLine
//

#import "VLBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, StreamMode){
    StreamModeNormol = 1,
    StreamModeVol = 2,
    StreamModeMix = 3,
};

@interface VLAddRoomModel : VLBaseModel

@property (nonatomic, copy) NSString *belCanto;
@property (nonatomic, copy) NSString *bgOption;
@property (nonatomic, assign)BOOL    isPrivate;
@property (nonatomic, copy) NSString *name;
@property (nonatomic, copy) NSString *password;
@property (nonatomic, copy) NSString *soundEffect;
@property (nonatomic, copy) NSString *userNo;
@property (nonatomic, copy) NSString *icon;
@property (nonatomic, assign) int streamMode;
@end

NS_ASSUME_NONNULL_END
