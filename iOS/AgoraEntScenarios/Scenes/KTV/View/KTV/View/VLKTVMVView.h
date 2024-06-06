//
//  VLKTVMVView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "AgoraEntScenarios-swift.h"
@import ScoreEffectUI;
@import AgoraLyricsScore;

NS_ASSUME_NONNULL_BEGIN
@class VLKTVSelBgModel;

typedef NS_ENUM(NSUInteger, LRCLEVEL) {
    LRCLEVELOW = 0,
    LRCLEVELMID = 1,
    LRCLEVELHIGH = 2,
};

//加入为伴唱状态
typedef enum : NSUInteger {
    KTVJoinCoSingerStateIdle = 0,         //无按钮
    KTVJoinCoSingerStateWaitingForJoin,   //按钮显示:加入合唱
    KTVJoinCoSingerStateJoinNow,          //按钮显示:加入中
    KTVJoinCoSingerStateWaitingForLeave,   //按钮显示:退出合唱
} KTVJoinCoSingerState;

typedef enum : NSUInteger {
    VLKTVMVViewStateIdle = 0,
    VLKTVMVViewStateLoading,
    VLKTVMVViewStateLoadFail,
} VLKTVMVLoadingState;

typedef enum : NSUInteger {
    VLKTVMVViewActionTypeSetParam = 0,  // 设置参数
    VLKTVMVViewActionTypeMVPlay,     // play
    VLKTVMVViewActionTypeMVPause,    // parse
    VLKTVMVViewActionTypeMVNext,     // 播放下一首
    VLKTVMVViewActionTypeSingOrigin, // 原唱
    VLKTVMVViewActionTypeSingAcc,    // 伴奏
    VLKTVMVViewActionTypeSingLead,   //导唱
    VLKTVMVViewActionTypeRetryLrc    // 歌曲重试
} VLKTVMVViewActionType;

typedef enum : NSUInteger {
    VLKTVMVViewStateNone = 0,  // 当前无人点歌
    VLKTVMVViewStateMusicLoading = 1,  // 当前歌曲加载中
    VLKTVMVViewStateAudience = 2, //观众
    VLKTVMVViewStateOwnerSing = 3, //房主点歌演唱
    VLKTVMVViewStateOwnerAudience = 4, //房主未加入合唱
    VLKTVMVViewStateJoinChorus = 5,//加入合唱中
    VLKTVMVViewStateOwnerChorus = 6, //房主合唱
    VLKTVMVViewStateNotOwnerChorus = 7, //非房主演唱
    VLKTVMVViewStateMusicOwnerLoadFailed = 8, //点歌人歌曲加载失败(房主或者点歌者 一样的)
    VLKTVMVViewStateMusicLoadFailed = 9, //观众歌曲加载失败
    VLKTVMVViewStateMusicOwnerLoadLrcFailed = 10, //点歌人歌曲加载失败(房主)
    VLKTVMVViewStateMusicLoadLrcFailed = 11, //观众歌词加载失败
} VLKTVMVViewState; //主要用来记录各种情况下的显示状态

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
@property (nonatomic, assign) NSInteger loadingProgress;
@property (nonatomic, strong) KaraokeView *karaokeView;
@property (nonatomic, strong) GradeView *gradeView;
@property (nonatomic, strong) IncentiveView *incentiveView;
@property (nonatomic, strong) LineScoreView *lineScoreView;
@property (nonatomic, strong) UIButton *joinChorusBtn;
@property (nonatomic, assign) BOOL isOriginLeader;
- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVMVViewDelegate>)delegate;

//更改背景
- (void)changeBgViewByModel:(VLKTVSelBgModel *)selBgModel;
@property (nonatomic, strong) UIImageView *bgImgView;

@property (nonatomic, assign) VLKTVMVViewState mvState;//记录各种情况下的按钮状态

//- (void)cleanMusicText;
- (int)getSongScore;
- (void)setSongScore:(int)score;
- (int)getAvgSongScore;

- (void)setOriginBtnState:(VLKTVMVViewActionType)type;
- (void)setJoinChorusFailedLoadingWith:(NSString *)msg;

#pragma mark - 歌词相关

/// 重置分数
- (void)reset;

-(void)setPerViewAvatar:(NSString *)url;

-(void)setSongNameWith:(NSString *)text;

-(void)setPlayState:(BOOL)isPlaying;

-(void)setLrcLevelWith:(LRCLEVEL)level;

@end

NS_ASSUME_NONNULL_END
