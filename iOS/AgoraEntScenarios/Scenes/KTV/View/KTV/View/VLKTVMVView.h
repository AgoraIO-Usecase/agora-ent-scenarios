//
//  VLKTVMVView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "AgoraEntScenarios-swift.h"
#import "VLRoomSelSongModel.h"
#import "KTVSkipView.h"
@import ScoreEffectUI;
@import AgoraLyricsScore;

NS_ASSUME_NONNULL_BEGIN
@class VLKTVSelBgModel;

typedef enum : NSUInteger {
    VLKTVMVViewActionTypeSetParam = 0,  // 设置参数
    VLKTVMVViewActionTypeMVPlay,    // play
    VLKTVMVViewActionTypeMVPause,   // parse
    VLKTVMVViewActionTypeMVNext,     // 播放下一首
    VLKTVMVViewActionTypeSingOrigin, //原唱
    VLKTVMVViewActionTypeSingAcc   // 伴奏
    
} VLKTVMVViewActionType;

@class VLKTVMVView;
@protocol VLKTVMVViewDelegate <NSObject>

- (void)onKTVMVView:(VLKTVMVView*)view btnTappedWithActionType:(VLKTVMVViewActionType)type;

/// 打分实时回调
/// @param score 分数
- (void)onKTVMVView:(VLKTVMVView*)view scoreDidUpdate:(int)score;

-(void)didJoinChours;

-(void)didLeaveChours;

@end

@interface VLKTVMVView : UIView

@property (nonatomic, strong) KaraokeView *karaokeView;
@property (nonatomic, strong) GradeView *gradeView;
@property (nonatomic, strong) IncentiveView *incentiveView;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVMVViewDelegate>)delegate;

//更改背景
- (void)changeBgViewByModel:(VLKTVSelBgModel *)selBgModel;

/// 更换播放按钮状态
/// @param state 状态
- (void)updateMVPlayerState:(VLKTVMVViewActionType)state;


@property (nonatomic, strong) UIImageView *bgImgView;

/// 当前用户上麦下麦
/// @param song 歌曲信息
/// @param role 当前用户角色
- (void)updateUIWithSong:(VLRoomSelSongModel * __nullable)song role:(KTVSingRole)role;

//- (void)cleanMusicText;
- (int)getSongScore;
- (void)setSongScore:(int)score;
- (int)getAvgSongScore;

//- (void)setPlayerViewsHidden:(BOOL)hidden nextButtonHidden:(BOOL)nextButtonHidden;
- (void)setOriginBtnState:(VLKTVMVViewActionType)type;

#pragma mark - 歌词相关

/// 加载歌词链接
/// @param lrcURL 链接
//- (void)loadLrcURL:(NSString *)lrcURL;

/// 开始滚动歌词
- (void)start;
/// 停止滚动歌词
- (void)stop;
/// 重置歌词界面
- (void)reset;
///滚动到指定位置
//- (void)scrollToTime:(NSTimeInterval)time;

-(void)configJoinChorusState:(BOOL)isSuccess;

@end

NS_ASSUME_NONNULL_END
