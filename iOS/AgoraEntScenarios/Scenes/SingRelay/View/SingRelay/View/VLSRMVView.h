//
//  VLSRMVView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "AgoraEntScenarios-swift.h"
@import ScoreEffectUI;
@import AgoraLyricsScore;

NS_ASSUME_NONNULL_BEGIN
@class VLSRSelBgModel;

//加入为伴唱状态
typedef enum : NSUInteger {
    SRJoinCoSingerStateIdle = 0,         //无按钮
    SRJoinCoSingerStateWaitingForJoin,   //按钮显示:加入合唱
    SRJoinCoSingerStateJoinNow,          //按钮显示:加入中
    SRJoinCoSingerStateWaitingForLeave,   //按钮显示:退出合唱
} SRJoinCoSingerState;

typedef enum : NSUInteger {
    VLSRMVViewStateIdle = 0,
    VLSRMVViewStateLoading,
    VLSRMVViewStateLoadFail,
} VLSRMVLoadingState;

typedef enum : NSUInteger {
    VLSRMVViewActionTypeSetParam = 0,  // 设置参数
    VLSRMVViewActionTypeMVPlay,     // play
    VLSRMVViewActionTypeMVPause,    // parse
    VLSRMVViewActionTypeMVNext,     // 播放下一首
    VLSRMVViewActionTypeSingOrigin, // 原唱
    VLSRMVViewActionTypeSingAcc,    // 伴奏
    VLSRMVViewActionTypeRetryLrc    // 歌曲重试
} VLSRMVViewActionType;

@class VLSRMVView;
@protocol VLSRMVViewDelegate <NSObject>

- (void)onSRMVView:(VLSRMVView*)view btnTappedWithActionType:(VLSRMVViewActionType)type;

/// 打分实时回调
/// @param score 分数
- (void)onSRMVView:(VLSRMVView*)view scoreDidUpdate:(int)score;

-(void)didJoinChours;

-(void)didLeaveChours;

@end

@interface VLSRMVView : UIView
@property (nonatomic, assign) VLSRMVLoadingState loadingType;
@property (nonatomic, assign) SRJoinCoSingerState joinCoSingerState;   //加入合唱状态
@property (nonatomic, assign) NSInteger loadingProgress;
@property (nonatomic, strong) KaraokeView *karaokeView;
@property (nonatomic, strong) GradeView *gradeView;
@property (nonatomic, strong) IncentiveView *incentiveView;
@property (nonatomic, strong) LineScoreView *lineScoreView;
@property (nonatomic, strong) UIButton *joinChorusBtn;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRMVViewDelegate>)delegate;

//更改背景
- (void)changeBgViewByModel:(VLSRSelBgModel *)selBgModel;

/// 更换播放按钮状态
/// @param state 状态
- (void)updateMVPlayerState:(VLSRMVViewActionType)state;


@property (nonatomic, strong) UIImageView *bgImgView;

/// 当前用户上麦下麦
/// @param song 歌曲信息
/// @param role 当前用户角色
- (void)updateUIWithSong:(VLSRRoomSelSongModel * __nullable)song role:(SRSingRole)role;

//- (void)cleanMusicText;
- (int)getSongScore;
- (void)setSongScore:(int)score;
- (int)getAvgSongScore;

//- (void)setPlayerViewsHidden:(BOOL)hidden nextButtonHidden:(BOOL)nextButtonHidden;
- (void)setOriginBtnState:(VLSRMVViewActionType)type;

#pragma mark - 歌词相关

/// 重置分数
- (void)reset;

-(void)setBotViewHidden:(BOOL)isHidden;

@end

NS_ASSUME_NONNULL_END
