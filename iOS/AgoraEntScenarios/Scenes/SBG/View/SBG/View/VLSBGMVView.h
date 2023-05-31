//
//  VLSBGMVView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "AgoraEntScenarios-swift.h"
#import "VLSBGRoomSelSongModel.h"
@import ScoreEffectUI;
@import AgoraLyricsScore;

NS_ASSUME_NONNULL_BEGIN
@class VLSBGSelBgModel;

//加入为伴唱状态
typedef enum : NSUInteger {
    RSJoinCoSingerStateIdle = 0,         //无按钮
    RSJoinCoSingerStateWaitingForJoin,   //按钮显示:加入合唱
    RSJoinCoSingerStateJoinNow,          //按钮显示:加入中
    RSJoinCoSingerStateWaitingForLeave,   //按钮显示:退出合唱
} RSJoinCoSingerState;

typedef enum : NSUInteger {
    VLSBGMVViewStateIdle = 0,
    VLSBGMVViewStateLoading,
    VLSBGMVViewStateLoadFail,
} VLSBGMVLoadingState;

typedef enum : NSUInteger {
    VLSBGMVViewActionTypeSetParam = 0,  // 设置参数
    VLSBGMVViewActionTypeMVPlay,     // play
    VLSBGMVViewActionTypeMVPause,    // parse
    VLSBGMVViewActionTypeMVNext,     // 播放下一首
    VLSBGMVViewActionTypeSingOrigin, // 原唱
    VLSBGMVViewActionTypeSingAcc,    // 伴奏
    VLSBGMVViewActionTypeSingLead,   //导唱
    VLSBGMVViewActionTypeRetryLrc    // 歌曲重试
} VLSBGMVViewActionType;

@class VLSBGMVView;
@protocol VLSBGMVViewDelegate <NSObject>

- (void)onRSMVView:(VLSBGMVView*)view btnTappedWithActionType:(VLSBGMVViewActionType)type;

/// 打分实时回调
/// @param score 分数
- (void)onRSMVView:(VLSBGMVView*)view scoreDidUpdate:(int)score;

-(void)didJoinChours;

-(void)didLeaveChours;

@end

@interface VLSBGMVView : UIView
@property (nonatomic, assign) VLSBGMVLoadingState loadingType;
@property (nonatomic, assign) RSJoinCoSingerState joinCoSingerState;   //加入合唱状态
@property (nonatomic, assign) NSInteger loadingProgress;
@property (nonatomic, strong) KaraokeView *karaokeView;
@property (nonatomic, strong) GradeView *gradeView;
@property (nonatomic, strong) IncentiveView *incentiveView;
@property (nonatomic, strong) LineScoreView *lineScoreView;
@property (nonatomic, strong) UIButton *joinChorusBtn;
@property (nonatomic, assign) BOOL isOriginLeader;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGMVViewDelegate>)delegate;

//更改背景
- (void)changeBgViewByModel:(VLSBGSelBgModel *)selBgModel;

/// 更换播放按钮状态
/// @param state 状态
- (void)updateMVPlayerState:(VLSBGMVViewActionType)state;


@property (nonatomic, strong) UIImageView *bgImgView;

/// 当前用户上麦下麦
/// @param song 歌曲信息
/// @param role 当前用户角色
- (void)updateUIWithSong:(VLSBGRoomSelSongModel * __nullable)song role:(SBGSingRole)role;

//- (void)cleanMusicText;
- (int)getSongScore;
- (void)setSongScore:(int)score;
- (int)getAvgSongScore;

//- (void)setPlayerViewsHidden:(BOOL)hidden nextButtonHidden:(BOOL)nextButtonHidden;
- (void)setOriginBtnState:(VLSBGMVViewActionType)type;

#pragma mark - 歌词相关

/// 重置分数
- (void)reset;

-(void)setBotViewHidden:(BOOL)isHidden;

@end

NS_ASSUME_NONNULL_END
