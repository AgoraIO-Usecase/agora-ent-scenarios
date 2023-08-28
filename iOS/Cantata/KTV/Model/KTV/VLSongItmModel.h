//
//  VLSongItmModel.h
//  VoiceOnLine
//

#import "VLBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLSongItmModel : VLBaseModel

//@property (nonatomic, strong) NSArray *lyricType;
//@property (nonatomic, copy) NSString *releaseTime;
///演唱
@property (nonatomic, copy) NSString *singer;
//@property (nonatomic, copy) NSString *vendorId;
//@property (nonatomic, copy) NSString *mv;
//@property (nonatomic, copy) NSString *updateTime;
//@property (nonatomic, copy) NSString *pitchType;
//@property (nonatomic, copy) NSString *type;
//@property (nonatomic, copy) NSString *duration;
@property (nonatomic, copy) NSString *songName;
@property (nonatomic, copy) NSString *songNo;
@property (nonatomic, copy) NSString *imageUrl;
//@property (nonatomic, copy) NSString *highPart;
//@property (nonatomic, copy) NSString *status;
///歌曲链接
//@property (nonatomic, copy) NSString *songUrl;
///歌词
@property (nonatomic, copy) NSString *lyric;

//是否被点过
@property (nonatomic, assign) BOOL ifChoosed;
@property (nonatomic, assign) BOOL ifChorus;


@end

NS_ASSUME_NONNULL_END
