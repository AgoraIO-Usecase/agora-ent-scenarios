//
//  VLSongItmModel.h
//  VoiceOnLine
//

#import "VLBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface DHCSongItmModel : VLBaseModel

///演唱
@property (nonatomic, copy) NSString *singer;
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


@end

NS_ASSUME_NONNULL_END
