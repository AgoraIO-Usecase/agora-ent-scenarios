//
//  VLRoomListModel.h
//  VoiceOnLine
//

#import "VLBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLRoomListModel : VLBaseModel

//@property (nonatomic, copy) NSString *id;
@property (nonatomic, copy) NSString *name;
@property (nonatomic, assign) BOOL isPrivate;
@property (nonatomic, copy) NSString *password;
@property (nonatomic, copy) NSString *creator;
@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, copy) NSString *isChorus;
@property (nonatomic, assign) NSInteger bgOption;
@property (nonatomic, copy) NSString *soundEffect;
@property (nonatomic, copy) NSString *belCanto;
@property (nonatomic, copy, nullable) NSString *createdAt;
@property (nonatomic, copy, nullable) NSString *updatedAt;
@property (nonatomic, copy) NSString *status;
@property (nonatomic, copy) NSString *deletedAt;
@property (nonatomic, copy) NSString *roomPeopleNum;
@property (nonatomic, copy) NSString *icon;

///新加字段 当前房间的创建者
@property (nonatomic, copy) NSString *creatorNo;

@end

NS_ASSUME_NONNULL_END
