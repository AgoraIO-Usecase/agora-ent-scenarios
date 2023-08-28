//
//  VLKTVMVView.m
//  VoiceOnLine
//

#import "VLKTVMVView.h"
#import "VLKTVSelBgModel.h"

#import "VLKTVMVIdleView.h"
//#import "HWWeakTimer.h"
#import "VLHotSpotBtn.h"
#import "VLUserCenter.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "AESMacro.h"
@import Masonry;

@interface VLKTVMVView () <VLKTVMVIdleViewDelegate, KaraokeDelegate>

@property(nonatomic, weak) id <VLKTVMVViewDelegate>delegate;

@property (nonatomic, strong) UIActivityIndicatorView* loadingView;  //加载中
@property (nonatomic, strong) UILabel* loadingTipsLabel;  //加载结果提示
@property (nonatomic, strong) UIButton* retryButton;   //重试按钮
@property (nonatomic, strong) UIView* contentView;

@property (nonatomic, strong) UILabel *musicTitleLabel;
@property (nonatomic, strong) UILabel *scoreLabel;
// 分数分开优化性能
@property (nonatomic, strong) UILabel *scoreUnitLabel;

@property (nonatomic, strong) VLHotSpotBtn *pauseBtn; /// 暂停播放
@property (nonatomic, strong) VLHotSpotBtn *nextButton; /// 下一首

@property (nonatomic, strong) UIButton *originBtn;  /// 原唱按钮
@property (nonatomic, strong) VLHotSpotBtn *settingBtn; /// 设置参数按钮
@property (nonatomic, strong) VLKTVMVIdleView *idleView;//没有人演唱视图


//@property (nonatomic, strong) AgoraLrcScoreConfigModel *config;
@property (nonatomic, assign) int totalLines;
@property (nonatomic, assign) double totalScore;
@property (nonatomic, assign) double currentTime;

@property (nonatomic, assign) BOOL isPlayAccompany;
@property (nonatomic, strong) UIButton *leaveChorusBtn;
@property (nonatomic, strong) UIView *BotView;
@end

@implementation VLKTVMVView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVMVViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self setupView];
        self.currentTime = 0;
    }
    return self;
}

#pragma setter
- (void)setIsPlayAccompany:(BOOL)isPlayAccompany {
    [self.originBtn setSelected:!isPlayAccompany];
    [self _refreshOriginButton];
    
    VLKTVMVViewActionType targetOrigin = isPlayAccompany ? VLKTVMVViewActionTypeSingAcc : VLKTVMVViewActionTypeSingOrigin;
    if ([self.delegate respondsToSelector:@selector(onKTVMVView:btnTappedWithActionType:)]) {
        [self.delegate onKTVMVView:self btnTappedWithActionType:targetOrigin];
    }
}

- (void)setLoadingType:(VLKTVMVLoadingState)loadingType {
    _loadingType = loadingType;
    
    if (loadingType == VLKTVMVViewStateLoading) {
        [self.loadingView startAnimating];
        [self.loadingView setHidden:NO];
        [self.contentView setHidden:YES];
        [self.retryButton setHidden:YES];
        [self.loadingTipsLabel setHidden:NO];
        self.loadingTipsLabel.text = @"";
    } else if (loadingType == VLKTVMVViewStateLoadFail) {
        [self.loadingView stopAnimating];
        [self.loadingView setHidden:YES];
        [self.contentView setHidden:NO];
        [self.retryButton setHidden:NO];
        [self.loadingTipsLabel setHidden:NO];
        self.loadingTipsLabel.text = @"歌词加载失败";
    } else if (loadingType == VLKTVMVViewStateIdle){
        [self.loadingView stopAnimating];
        [self.loadingView setHidden:YES];
        [self.contentView setHidden:NO];
        [self.retryButton setHidden:YES];
        [self.loadingTipsLabel setHidden:YES];
    }
    [self setNeedsLayout];
}

- (void)setLoadingProgress:(NSInteger)loadingProgress {
    _loadingProgress = loadingProgress;
#if DEBUG
    self.loadingTipsLabel.text = [NSString stringWithFormat:@"loading %ld%%", loadingProgress];
#else
    self.loadingTipsLabel.text = @"加载中";
#endif
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    [self.loadingView sizeToFit];
    self.loadingView.center = CGRectGetCenter(self.bounds);
    CGFloat tipsTop = self.loadingType == VLKTVMVViewStateLoading ? self.loadingView.bottom + 5 : self.incentiveView.bottom + 10;
    self.loadingTipsLabel.frame = CGRectMake(self.loadingView.centerX - 100, tipsTop, 200, 40);
    
    [self.retryButton sizeToFit];
    self.retryButton.center = CGPointMake(self.loadingView.centerX, self.loadingTipsLabel.bottom + self.retryButton.height / 2);
}

#pragma mark private

- (void)setupView {
    self.bgImgView = [[UIImageView alloc]initWithFrame:self.bounds];
    self.bgImgView.image = [UIImage sceneImageWithName:@"ktv_mv_tempBg"];
    self.bgImgView.layer.cornerRadius = 10;
    self.bgImgView.layer.masksToBounds = YES;
    [self addSubview:self.bgImgView];
    
    self.contentView = [[UIView alloc] initWithFrame:self.bounds];
    self.contentView.backgroundColor = [UIColor clearColor];
    [self addSubview:self.contentView];
    
    self.loadingView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleLarge];
    self.loadingView.color = [UIColor whiteColor];
    [self.loadingView setHidden:YES];
    [self addSubview:self.loadingView];
    
    self.loadingTipsLabel = [[UILabel alloc] init];
    self.loadingTipsLabel.textAlignment = NSTextAlignmentCenter;
    self.loadingTipsLabel.font = [UIFont systemFontOfSize:16];
    self.loadingTipsLabel.textColor = [UIColor whiteColor];
    [self addSubview:self.loadingTipsLabel];
    
    self.retryButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.retryButton addTarget:self action:@selector(onRetryAction:) forControlEvents:UIControlEventTouchUpInside];
    self.retryButton.titleLabel.font = self.loadingTipsLabel.font;
    [self.retryButton setTitle:@"  点击重试  " forState:UIControlStateNormal];
    [self.retryButton sizeToFit];
    self.retryButton.layer.cornerRadius = self.retryButton.height / 2;
    self.retryButton.layer.borderWidth = 1;
    self.retryButton.layer.borderColor = [UIColor whiteColor].CGColor;
    [self addSubview:self.retryButton];
    self.retryButton.hidden = YES;
    
    UIImageView *currentPlayImgView = [[UIImageView alloc]initWithFrame:CGRectMake(9, 2, 39, 39)];
    currentPlayImgView.image = [UIImage sceneImageWithName:@"ktv_currentPlay_icon"];
    [self.contentView addSubview:currentPlayImgView];

    self.musicTitleLabel.frame = CGRectMake(currentPlayImgView.right+2, currentPlayImgView.centerY-9, 120, 18);
    [self.contentView addSubview:self.musicTitleLabel];
    
    self.gradeView = [[GradeView alloc]init];
    self.gradeView.frame = CGRectMake(15, 15, self.width - 30, 30);
    [self.contentView addSubview:self.gradeView];

    CGFloat lY = CGRectGetMaxX(currentPlayImgView.frame);
    CGFloat lH = self.height - 54 - lY;
   // [KaraokeView setLogWithPrintToConsole:true writeToFile:true];
    _karaokeView = [[KaraokeView alloc] initWithFrame:CGRectMake(0, lY, self.width, lH) loggers:@[[FileLogger new]]];
    _karaokeView.scoringView.viewHeight = 60;
    _karaokeView.scoringView.topSpaces = 5;
   // _karaokeView.lyricsView.textSelectedColor = [UIColor colorWithHexString:@"#33FFFFFF"];
    _karaokeView.lyricsView.textNormalColor = [UIColor colorWithRed:1 green:1 blue:1 alpha:0.5];
    _karaokeView.lyricsView.textHighlightedColor = [UIColor colorWithHexString:@"#FF8AB4"];
    _karaokeView.lyricsView.lyricLineSpacing = 6;
   // _karaokeView.scoringView.showDebugView = true;
    _karaokeView.backgroundImage = [UIImage imageNamed:@"ktv_top_bgIcon"];
    [self.contentView addSubview:_karaokeView];
    
    self.incentiveView = [[IncentiveView alloc]init];
    self.incentiveView.frame = CGRectMake(15, 55, 192, 45);
    [self.karaokeView addSubview:self.incentiveView];
    
    self.lineScoreView = [[LineScoreView alloc]init];
    self.lineScoreView.frame = CGRectMake(self.karaokeView.scoringView.defaultPitchCursorX, self.karaokeView.scoringView.topSpaces + self.karaokeView.top, 50, self.karaokeView.scoringView.viewHeight);
    [self.contentView addSubview:self.lineScoreView];
    
    self.BotView = [[UIView alloc]initWithFrame:CGRectMake(0, self.height-54, self.width, 54)];
    self.BotView.backgroundColor = [UIColor clearColor];
    [self addSubview:self.BotView];

    self.pauseBtn.frame = CGRectMake(20, 0, 34, 54);
    [self updateBtnLayout:self.pauseBtn];
    [self.BotView addSubview:self.pauseBtn];

    self.nextButton.frame = CGRectMake(_pauseBtn.right+10, 0, 34, 54);
    [self updateBtnLayout:self.nextButton];
    [self.BotView addSubview:self.nextButton];
    
    self.originBtn.frame = CGRectMake(self.width-20-48, 0, 34, 54);
    [self updateBtnLayout:self.originBtn];
    [self.BotView addSubview:self.originBtn];
    
    self.settingBtn.frame = CGRectMake(_originBtn.left-10-34, 0, 34, 54);
    [self updateBtnLayout:self.settingBtn];
    [self.BotView addSubview:self.settingBtn];
    
    self.idleView = [[VLKTVMVIdleView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height) withDelegate:self];
    self.idleView.hidden = NO;
    [self addSubview:self.idleView];

    [self setPlayerViewsHidden:YES nextButtonHidden:YES playButtonHidden:YES];

 //   [self setPlayerViewsHidden:YES nextButtonHidden:YES];
    
    self.joinChorusBtn = [[UIButton alloc]initWithFrame:CGRectMake(self.width / 2.0 - 56, 10, 112, 34)];
    [self.joinChorusBtn setBackgroundImage:[UIImage sceneImageWithName:@"ic_join_chorus"] forState:UIControlStateNormal];
    [self.joinChorusBtn setBackgroundImage:[UIImage sceneImageWithName:@"ic_join_chorus_loading"] forState:UIControlStateDisabled];
    _joinChorusBtn.layer.cornerRadius = 17;
    _joinChorusBtn.layer.masksToBounds = true;
    [self.joinChorusBtn addTarget:self action:@selector(joinChorus) forControlEvents:UIControlEventTouchUpInside];
    [self.BotView addSubview:_joinChorusBtn];
    
    self.leaveChorusBtn = [[UIButton alloc]initWithFrame:CGRectMake(15, 0, 54, 54)];
    [self.leaveChorusBtn setTitle:@"退出合唱" forState:UIControlStateNormal];
    [self.leaveChorusBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [self.leaveChorusBtn setFont:UIFontMake(10.0)];
    [self.leaveChorusBtn setImage:[UIImage sceneImageWithName:@"Union"] forState:UIControlStateNormal];
    [self.leaveChorusBtn addTarget:self action:@selector(leaveChorus) forControlEvents:UIControlEventTouchUpInside];
    [self updateBtnLayout:self.leaveChorusBtn];
    [self.BotView addSubview:self.leaveChorusBtn];
    _joinChorusBtn.hidden = _leaveChorusBtn.hidden = YES;
}

-(void)setBotViewHidden:(BOOL)isHidden{
    [self.BotView setHidden:isHidden];
}

-(void)updateBtnLayout:(UIButton*)button {
    CGFloat spacing = -5;
      CGSize imageSize = button.imageView.frame.size;
       CGSize titleSize = button.titleLabel.frame.size;
        CGSize textSize = [button.titleLabel.text sizeWithFont:button.titleLabel.font];
      CGSize frameSize = CGSizeMake(ceilf(textSize.width), ceilf(textSize.height));
      if (titleSize.width + 0.5 < frameSize.width) {
          titleSize.width = frameSize.width;
        }

      CGFloat totalHeight = imageSize.height + titleSize.height;
    button.imageEdgeInsets = UIEdgeInsetsMake(- (totalHeight - imageSize.height + spacing), 0.0, 15.0, - titleSize.width);
    button.titleEdgeInsets = UIEdgeInsetsMake(0, - imageSize.width, - (totalHeight - titleSize.height + spacing), 0);

}

- (void)_refreshOriginButton {
    if (self.originBtn.selected) {
        [self.originBtn setTitle:KTVLocalizedString(@"ktv_room_original") forState:UIControlStateNormal];
        [self.originBtn setTitle:KTVLocalizedString(@"ktv_room_original") forState:UIControlStateSelected];
    } else {
        [self.originBtn setTitle:KTVLocalizedString(@"ktv_room_original") forState:UIControlStateNormal];
        [self.originBtn setTitle:KTVLocalizedString(@"ktv_room_original") forState:UIControlStateSelected];
    }
    [self setNeedsLayout];
}

-(void)joinChorus{
    //加入合唱
    if([self.delegate respondsToSelector:@selector(didJoinChours)]) {
        [self.delegate didJoinChours];
    }
}

-(void)leaveChorus{
    //离开合唱
    if([self.delegate respondsToSelector:@selector(didJoinChours)]) {
        [self.delegate didLeaveChours];
    }
}

#pragma mark - public

- (void)updateMVPlayerState:(VLKTVMVViewActionType)state {
    if (state == VLKTVMVViewActionTypeMVPlay) {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.pauseBtn.selected = YES;
        });
    } else if (state == VLKTVMVViewActionTypeMVPause) {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.pauseBtn.selected = NO;
        });
    }
}

#pragma mark - Action

- (void)onRetryAction:(UIButton*)button {
    if ([self.delegate respondsToSelector:@selector(onKTVMVView:btnTappedWithActionType:)]) {
        [self.delegate onKTVMVView:self btnTappedWithActionType:VLKTVMVViewActionTypeRetryLrc];
    }
}

- (void)playClick:(UIButton *)button {
    button.selected = !button.selected;
    if ([self.delegate respondsToSelector:@selector(onKTVMVView:btnTappedWithActionType:)]) {
        if (button.selected) {
            [self.delegate onKTVMVView:self btnTappedWithActionType:VLKTVMVViewActionTypeMVPlay];
        } else {
            [self.delegate onKTVMVView:self btnTappedWithActionType:VLKTVMVViewActionTypeMVPause];
        }
    }
}

- (void)buttonClick:(UIButton *)sender {
    // 设置参数
    if (sender == self.settingBtn) {
        if ([self.delegate respondsToSelector:@selector(onKTVMVView:btnTappedWithActionType:)]) {
            [self.delegate onKTVMVView:self btnTappedWithActionType:VLKTVMVViewActionTypeSetParam];
        }
    } else if (sender == self.nextButton) {
        if ([self.delegate respondsToSelector:@selector(onKTVMVView:btnTappedWithActionType:)]) {
            [self.delegate onKTVMVView:self btnTappedWithActionType:VLKTVMVViewActionTypeMVNext];
        }
    }
}

- (void)changeBgViewByModel:(VLKTVSelBgModel *)selBgModel {
    self.bgImgView.image = [UIImage sceneImageWithName:selBgModel.imageName];
}

- (void)setJoinCoSingerState:(KTVJoinCoSingerState)joinCoSingerState {
    _joinCoSingerState = joinCoSingerState;
    NSLog(@"joinCoSingerState: %li", joinCoSingerState);
    switch (joinCoSingerState) {
        case KTVJoinCoSingerStateWaitingForJoin:
            self.joinChorusBtn.enabled = YES;
            self.joinChorusBtn.hidden = NO;
            self.leaveChorusBtn.hidden = YES;
            break;
        case KTVJoinCoSingerStateJoinNow:
            self.joinChorusBtn.enabled = NO;
            self.joinChorusBtn.hidden = NO;
            self.leaveChorusBtn.hidden = YES;
            self.nextButton.hidden = YES;
            break;
        case KTVJoinCoSingerStateWaitingForLeave:
            self.joinChorusBtn.enabled = YES;
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = NO;
            break;
        case KTVJoinCoSingerStateIdle:
        default:
            self.joinChorusBtn.enabled = YES;
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = YES;
            break;
    }
    
    if(self.pauseBtn.hidden == YES && self.nextButton.hidden == NO && self.leaveChorusBtn.hidden == YES){
        self.nextButton.frame = CGRectMake(20, 0, 34, 54);
    } else {
        self.nextButton.frame = CGRectMake(_pauseBtn.right+10, _pauseBtn.top, 34, 54);
    }
    
}

- (void)configPlayerControls:(VLRoomSelSongModel *)song role:(KTVSingRole)role {
    // 是主唱/伴唱
    switch (role) {
        case KTVSingRoleSoloSinger:
        case KTVSingRoleLeadSinger: {
            [self setPlayerViewsHidden:NO nextButtonHidden:NO playButtonHidden:NO];
            self.pauseBtn.frame = CGRectMake(20, 0, 34, 54);
            self.nextButton.frame = CGRectMake(_pauseBtn.right+10, _pauseBtn.top, 34, 54);
          //  self.joinCoSingerState = KTVJoinCoSingerStateIdle;
        } break;
        case KTVSingRoleCoSinger: {
//        case KTVSingRoleFollowSinger:
            BOOL isNextEnable = !VLUserCenter.user.ifMaster;
            [self setPlayerViewsHidden:NO nextButtonHidden:isNextEnable playButtonHidden:YES];
         //   self.joinCoSingerState = KTVJoinCoSingerStateWaitingForLeave;
        } break;
        case KTVSingRoleAudience:
        default: {
            if(VLUserCenter.user.ifMaster) {
                [self setPlayerViewsHidden:YES nextButtonHidden:NO playButtonHidden:YES];
                self.nextButton.frame = CGRectMake(20, 0, 34, 54);
            } else {
                [self setPlayerViewsHidden:YES nextButtonHidden:YES playButtonHidden:YES];
            }
            
           // self.joinCoSingerState = KTVJoinCoSingerStateWaitingForJoin;
        } break;
    }
}

- (void)setPlayerViewsHidden:(BOOL)hidden
            nextButtonHidden:(BOOL)nextButtonHidden
            playButtonHidden:(BOOL)playButtonHidden {
    self.pauseBtn.hidden = playButtonHidden;
    self.nextButton.hidden = nextButtonHidden;
    self.originBtn.hidden = hidden;
    self.settingBtn.hidden = hidden;
}

- (BOOL)isPlaying:(VLRoomSelSongModel *)song {
    if (song.status == VLSongPlayStatusPlaying) {
        return YES;
    }
    return NO;
}

- (void)cleanMusicText {
    self.musicTitleLabel.text = @"";
}

- (void)setSongScore:(int)score {
    self.scoreLabel.text = [NSString stringWithFormat:@"%d",score];
}

- (void)updateUIWithSong:(VLRoomSelSongModel * __nullable)song role:(KTVSingRole)role {
    KTVLogInfo(@"VLKTVMVView updateUIWithSong: songName: %@, name: %@, role: %ld", song.songName, song.name, role);
    dispatch_async(dispatch_get_main_queue(), ^{
        self.idleView.hidden = song;
        self.scoreLabel.hidden = NO;
        
        if(song) {
            NSString *songText = [NSString stringWithFormat:@"%@-%@",song.songName,song.singer];
            self.musicTitleLabel.text = songText;
            [self configPlayerControls:song role:role];
        } else {
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = YES;
        }
    });
}

#pragma mark - 合唱代理
- (void)setOriginBtnState:(VLKTVMVViewActionType)type
{
    _originBtn.selected = type == VLKTVMVViewActionTypeSingOrigin ? YES : NO;
    [self _refreshOriginButton];
}

- (void)originClick:(UIButton *)button {
    BOOL targetState = !button.selected;
    VLKTVMVViewActionType targetOrigin = targetState ? VLKTVMVViewActionTypeSingOrigin : VLKTVMVViewActionTypeSingAcc;
    [self setOriginBtnState:targetOrigin];
    
    if ([self.delegate respondsToSelector:@selector(onKTVMVView:btnTappedWithActionType:)]) {
        [self.delegate onKTVMVView:self btnTappedWithActionType:targetOrigin];
    }
}

- (void)reset {
    KTVLogInfo(@"VLKTVMVView reset [%@]", self.musicTitleLabel.text);
    [self setSongScore:0];
    self.isPlayAccompany = YES;
    [self cleanMusicText];
}

#pragma mark - AgoraKaraokeScoreDelegate

/// 评分回调
/// @param score 当前行得分
/// @param cumulativeScore 累计得分
/// @param totalScore 当前歌曲总得分
-(void)agoraKaraokeScoreWithScore:(double)score cumulativeScore:(double)cumulativeScore totalScore:(double)totalScore {
    double scale = cumulativeScore / totalScore;
    double realScore = scale * 100;
    self.scoreLabel.text = [NSString stringWithFormat:@"%.0lf",score];
    self.totalLines += 1;
    self.totalScore = cumulativeScore;
    VLLog(@"Recording: %d lines at totalScore: %f", self.totalLines, cumulativeScore);
    if ([self.delegate respondsToSelector:@selector(onKTVMVView:scoreDidUpdate:)]) {
        [self.delegate onKTVMVView:self scoreDidUpdate:realScore];
    }
}

- (int)getSongScore {
    return [self.scoreLabel.text intValue];
}

- (int)getAvgSongScore
{
    if(self.totalLines <= 0) {
        return 0;
    }
    else {
        return (int)(self.totalScore / self.totalLines);
    }
}

- (UILabel *)musicTitleLabel {
    if (!_musicTitleLabel) {
        _musicTitleLabel = [[UILabel alloc]init];
        _musicTitleLabel.font = UIFontMake(12);
        _musicTitleLabel.text = @"";
        _musicTitleLabel.textColor = UIColorWhite;
    }
    return _musicTitleLabel;
}

- (VLHotSpotBtn *)pauseBtn {
    if (!_pauseBtn) {
         _pauseBtn = [[VLHotSpotBtn alloc] init];
        [self.pauseBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        self.pauseBtn.titleLabel.font = UIFontMake(10.0);
        [self.pauseBtn setTitle:@"暂停" forState:UIControlStateNormal];
        [self.pauseBtn setTitle:@"播放" forState:UIControlStateSelected];
        [_pauseBtn setImage:[UIImage sceneImageWithName:@"ktv_pause_icon"] forState:UIControlStateSelected];
        [_pauseBtn setImage:[UIImage sceneImageWithName:@"ktv_pause_resumeicon"] forState:UIControlStateNormal];
        _pauseBtn.selected = NO;
        [_pauseBtn addTarget:self action:@selector(playClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _pauseBtn;
}

- (VLHotSpotBtn *)nextButton {
    if (!_nextButton) {
        _nextButton = [[VLHotSpotBtn alloc] init];
        [_nextButton setImage:[UIImage sceneImageWithName:@"ktv_playNext_icon"] forState:UIControlStateNormal];
        [self.nextButton setTitle:@"切歌" forState:UIControlStateNormal];
        [self.nextButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        self.nextButton.titleLabel.font = UIFontMake(10.0);
        [_nextButton addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _nextButton;
}

- (UIButton *)originBtn {
    if (!_originBtn) {
        _originBtn.spacingBetweenImageAndTitle = 2;
        _originBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        [_originBtn setTitle:KTVLocalizedString(@"ktv_room_original") forState:UIControlStateNormal];
        _originBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
        _originBtn.titleLabel.font = UIFontMake(10.0);
        [self.originBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [self.originBtn setImage:[UIImage sceneImageWithName:@"acc"] forState:UIControlStateNormal];
        [self.originBtn setImage:[UIImage sceneImageWithName:@"original"] forState:UIControlStateSelected];
        _originBtn.selected = NO;
        [_originBtn addTarget:self action:@selector(originClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _originBtn;
}

- (VLHotSpotBtn *)settingBtn {
    if (!_settingBtn) {
        _settingBtn = [[VLHotSpotBtn alloc] init];
        [_settingBtn setImage:[UIImage sceneImageWithName:@"ktv_subtitle_icon"] forState:UIControlStateNormal];
        [self.settingBtn setTitle:KTVLocalizedString(@"ktv_room_player_tweak") forState:UIControlStateNormal];
        [self.settingBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        self.settingBtn.titleLabel.font = UIFontMake(10.0);
        [_settingBtn addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _settingBtn;
}

- (UILabel *)scoreLabel {
    if (!_scoreLabel) {
        _scoreLabel = [[UILabel alloc] init];
        _scoreLabel.font = VLUIFontMake(15);
        _scoreLabel.textColor = [UIColor whiteColor];
        _scoreLabel.text = KTVLocalizedString(@"0");
    }
    return _scoreLabel;
}

- (UILabel *)scoreUnitLabel {
    if (!_scoreUnitLabel) {
        _scoreUnitLabel = [[UILabel alloc] init];
        _scoreUnitLabel.font = VLUIFontMake(10);
        _scoreUnitLabel.textColor = [UIColor whiteColor];
        _scoreUnitLabel.text = KTVLocalizedString(@"ktv_score_formatter");
    }
    return _scoreUnitLabel;
}

@end
