//
//  VLRoomSelSongModel.h
//  VoiceOnLine
//

#import "VLBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLRoomSelSongModel : VLBaseModel
/// 合唱者userNo
@property (nonatomic, copy) NSString *chorusNo;
@property (nonatomic, copy) NSString *imageUrl;
///是否合唱
@property (nonatomic, assign) BOOL isChorus;
///是否原唱
@property (nonatomic, copy) NSString *isOriginal;
@property (nonatomic, copy) NSString *singer;
@property (nonatomic, copy) NSString *songName;
@property (nonatomic, copy) NSString *songNo;
@property (nonatomic, copy) NSString *songUrl;
///歌词
@property (nonatomic, copy) NSString *lyric;;
///排序
@property (nonatomic, copy) NSString *sort;
///0 未开始 1.已唱 2.正在唱
@property (nonatomic, assign) NSInteger  status;
///是谁点的歌
@property (nonatomic, copy) NSString *userNo;
@property (nonatomic, copy) NSString *userId;
///点歌人昵称
@property (nonatomic, copy) NSString *name;

/// 得分
@property (nonatomic, assign) double score;

#pragma mark - 自定义字段
/// 是否是自己点的歌曲
@property (nonatomic, assign, readonly) bool isOwnSong;


@property (nonatomic, copy, nullable) NSString* objectId;
@end

NS_ASSUME_NONNULL_END
