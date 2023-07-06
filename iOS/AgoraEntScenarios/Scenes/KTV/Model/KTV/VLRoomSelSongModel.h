//
//  VLRoomSelSongModel.h
//  VoiceOnLine
//

#import "VLBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    VLSongPlayStatusIdle = 0, //未播放
    VLSongPlayStatusPlaying = 2   //正在播放
} VLSongPlayStatus;

@interface VLRoomSelSongModel : VLBaseModel

@property (nonatomic, copy) NSString *imageUrl;

///是否原唱
@property (nonatomic, copy) NSString *isOriginal;
@property (nonatomic, copy) NSString *singer;
@property (nonatomic, copy) NSString *songName;
@property (nonatomic, copy) NSString *songNo;
//@property (nonatomic, copy) NSString *songUrl;
///歌词
//@property (nonatomic, copy) NSString *lyric;
///创建时间
@property (nonatomic, assign) int64_t createAt;
//置顶时间
@property (nonatomic, assign) int64_t pinAt;

///0 未开始 1.已唱 2.正在唱 3. match完毕
@property (nonatomic, assign) VLSongPlayStatus status;
///是谁点的歌
@property (nonatomic, copy) NSString *userNo;

///点歌人昵称
@property (nonatomic, copy) NSString *name;

@property (nonatomic, copy, nullable) NSString* objectId;


/// 是否是自己点的歌曲
- (BOOL)isSongOwner;


/// 上麦之后的麦位id，通过songNo+createAt拼接
- (NSString*)chorusSongId;

@end

NS_ASSUME_NONNULL_END
