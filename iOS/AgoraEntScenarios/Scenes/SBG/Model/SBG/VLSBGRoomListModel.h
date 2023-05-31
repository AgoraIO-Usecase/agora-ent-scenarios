//
//  VLSBGRoomListModel.h
//  VoiceOnLine
//

#import "VLBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLSBGRoomListModel : VLBaseModel

//@property (nonatomic, copy) NSString *id;
@property (nonatomic, copy) NSString *name;
@property (nonatomic, assign) BOOL isPrivate;
@property (nonatomic, copy) NSString *password;
@property (nonatomic, copy) NSString *creatorNo;
@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, copy) NSString *isChorus;
@property (nonatomic, assign) NSInteger bgOption;
@property (nonatomic, copy) NSString *soundEffect;
@property (nonatomic, copy) NSString *belCanto;
@property (nonatomic, assign) int64_t createdAt;
@property (nonatomic, assign) int64_t updatedAt;
@property (nonatomic, copy) NSString *status;
@property (nonatomic, copy) NSString *deletedAt;
@property (nonatomic, copy) NSString *roomPeopleNum;
@property (nonatomic, copy) NSString *icon;

@property (nonatomic, copy) NSString* objectId;

@end

NS_ASSUME_NONNULL_END
