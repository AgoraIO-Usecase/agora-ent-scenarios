//
//  VLKTVMVView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLRoomSelSongModel.h"
@import AgoraLyricsScore;

NS_ASSUME_NONNULL_BEGIN
@class VLKTVSelBgModel;

typedef enum : NSUInteger {
    VLKTVMVViewActionTypeSetParam = 0,  // 设置参数
    VLKTVMVViewActionTypeMVPlay,    // play
    VLKTVMVViewActionTypeMVPause,   // parse
    VLKTVMVViewActionTypeMVNext,     // 播放下一首
    VLKTVMVViewActionTypeSingOrigin, //原唱
    VLKTVMVViewActionTypeSingAcc,   // 伴奏
    VLKTVMVViewActionTypeExit
    
} VLKTVMVViewActionType;

typedef enum : NSUInteger {
    VLKTVMVViewSingActionTypeSolo = 0,  // 独唱
    VLKTVMVViewSingActionTypeJoinChorus,    // 加入合唱
} VLKTVMVViewSingActionType;

@protocol VLKTVMVViewDelegate <NSObject>

- (BOOL)ktvIsMyselfOnSeat;

- (void)ktvNotifyUserNotOnSeat;

- (void)ktvMVViewDidClickSingType:(VLKTVMVViewSingActionType)singType;

- (void)ktvMVViewDidClick:(VLKTVMVViewActionType)type;

- (void)ktvMVViewTimerCountDown:(NSInteger)countDownSecond;

/// 获取当前歌曲播放进度（秒）。
/// 你需要自定实现如下逻辑：通过 AgoraRtcMediaPlayerProtocol 类的 getPosition 获取当前播放进度
/// 并将返回值传入歌词组件。
- (NSTimeInterval)ktvMVViewMusicCurrentTime;

/// 获取歌曲总时长（秒）
/// 你需要自定实现如下逻辑：通过 AgoraRtcMediaPlayerProtocol 的 getDuration 获取歌曲总时长，
/// 并将返回值传入歌词组件
- (NSTimeInterval)ktvMVViewMusicTotalTime;

@optional

/// 定位到指定的播放位置
/// @param time 指定位置
- (void)ktvMVViewMusicSeekToTime:(NSTimeInterval)time;
/// 获取当前播放的歌词和进度
/// @param lrc 路径
/// @param progress 进度
- (void)ktvMVViewMusicLrc:(NSString *)lrc progress:(CGFloat)progress;
/// 获取每个歌词文字的标准 pitch
/// @param pitch 歌词
/// @param totoalCount 个数
- (void)ktvMVViewMusicAgoraWordPitch:(NSInteger)pitch totoalCount:(NSInteger)totoalCount;

/// 打分实时回调
/// @param score 分数
- (void)ktvMVViewMusicScore:(int)score;

@end

@interface VLKTVMVView : UIView

@property (nonatomic, strong) AgoraLrcScoreView *lrcView;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVMVViewDelegate>)delegate;

//更改背景
- (void)changeBgViewByModel:(VLKTVSelBgModel *)selBgModel;

/// 更换播放按钮状态
/// @param state 状态
- (void)updateMVPlayerState:(VLKTVMVViewActionType)state;

- (void)setVoicePitch:(NSArray <NSNumber *> *)pitch;

- (void)updateUIWithSong:(VLRoomSelSongModel* _Nullable)song onSeat:(BOOL)onSeat;

- (void)setJoinInViewHidden;

@property (nonatomic, strong) UIImageView *bgImgView;

/// 收到倒计时消息 (只有上麦)
/// @param countDown 倒计时 <= 0 时候不处理
/// @param onSeat 是否是上麦状态
/// @param currentSong 当前歌曲
- (void)receiveCountDown:(int)countDown onSeat:(BOOL)onSeat currentSong:(VLRoomSelSongModel *)currentSong;
 
/// 当前用户上麦下麦
/// @param onSeat 当前用户上麦下麦
/// @param song 歌曲信息
- (void)updateUIWithUserOnSeat:(BOOL)onSeat song:(VLRoomSelSongModel *)song;

- (void)cleanMusicText;
- (int)getSongScore;
- (void)setSongScore:(int)score;
- (int)getAvgSongScore;

- (void)validateSingType;

- (void)setPlayerViewsHidden:(BOOL)hidden nextButtonHidden:(BOOL)nextButtonHidden;

#pragma mark - 歌词相关

/// 加载歌词链接
/// @param lrcURL 链接
- (void)loadLrcURL:(NSString *)lrcURL;

/// 开始滚动歌词
- (void)start;
/// 停止滚动歌词
- (void)stop;
/// 重置歌词界面
- (void)reset;
/// 重置歌词时间
- (void)resetTime;
///滚动到指定位置
- (void)scrollToTime:(NSTimeInterval)time;

@end

NS_ASSUME_NONNULL_END
