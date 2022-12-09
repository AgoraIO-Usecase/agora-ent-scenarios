//
//  VLLoginModel.h
//  VoiceOnLine
//

#import "VLBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLLoginModel : VLBaseModel
@property (nonatomic, copy) NSString *openId;
@property (nonatomic, copy) NSString *userNo;    //account user id
@property (nonatomic, copy) NSString *headUrl;
@property (nonatomic, copy) NSString *mobile;
@property (nonatomic, assign) NSInteger status;
@property (nonatomic, copy) NSString *sex;
@property (nonatomic, copy) NSString *name;
@property (nonatomic, copy) NSString *token;
@property (nonatomic, copy) NSString *id;  //uid (mpk)

//自己是否是房主
@property (nonatomic, assign) BOOL ifMaster;
@property (nonatomic, copy) NSString *agoraRTMToken;
@property (nonatomic, copy) NSString *agoraRTCToken;
@property (nonatomic, copy) NSString *agoraPlayerRTCToken;

//for sync manager
@property (nonatomic, copy, nullable) NSString* objectId;

@property (nonatomic) NSString *chat_uid;
@property (nonatomic) NSString *rtc_uid;
@property (nonatomic) NSString *channel_id;
@property (nonatomic) NSString *im_token;
@property (nonatomic) NSString *authorization;
@property (nonatomic) BOOL hasVoiceRoomUserInfo;
@end

NS_ASSUME_NONNULL_END
