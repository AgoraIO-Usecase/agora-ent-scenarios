//
//  VLKTVMVView.m
//  VoiceOnLine
//

#import "VLKTVMVView.h"
#import "VLKTVSelBgModel.h"

#import "VLKTVMVIdleView.h"
#import "HWWeakTimer.h"
#import "VLHotSpotBtn.h"
#import "VLUserCenter.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "KTVMacro.h"
@import Masonry;

@interface VLKTVMVView () <VLKTVMVIdleViewDelegate, KaraokeDelegate>

@property(nonatomic, weak) id <VLKTVMVViewDelegate>delegate;

@property (nonatomic, strong) UIActivityIndicatorView* loadingView;  //加载中
@property (nonatomic, strong) UILabel* loadingTipsLabel;  //加载提示
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
@property (nonatomic, strong) UIButton *joinChorusBtn;
@property (nonatomic, strong) UIButton *leaveChorusBtn;
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

- (void)setIsLoading:(BOOL)isLoading {
    _isLoading = isLoading;
    
    if (_isLoading) {
        [self.loadingView startAnimating];
        [self.contentView setHidden:YES];
        [self.loadingTipsLabel setHidden:NO];
    } else {
        NSLog(@"curThread: %@", [NSThread currentThread]);
        [self.loadingView stopAnimating];
        [self.contentView setHidden:NO];
        [self.loadingTipsLabel setHidden:YES];
    }
}

- (void)setLoadingProgress:(NSInteger)loadingProgress {
    _loadingProgress = loadingProgress;
#if DEBUG
    self.loadingTipsLabel.text = [NSString stringWithFormat:@"loading %ld%%", loadingProgress];
#else
    self.loadingTipsLabel.text = @"loading";
#endif
}

- (void)layoutSubviews {
    [super layoutSubviews];
//
//    [self.originBtn sizeToFit];
//    self.originBtn.frame = CGRectMake(self.width-20-self.originBtn.width, _pauseBtn.top, self.originBtn.width, 24);
//    self.settingBtn.frame = CGRectMake(_originBtn.left-20-24, _pauseBtn.top, 24, 24);
    [self.loadingView sizeToFit];
    self.loadingView.center = CGRectGetCenter(self.bounds);
    self.loadingTipsLabel.frame = CGRectMake(self.loadingView.centerX - 50, self.loadingView.bottom + 5, 200, 40);
}

#pragma mark private

- (void)setupView {
    self.bgImgView = [[UIImageView alloc]initWithFrame:self.bounds];
    self.bgImgView.image = [UIImage sceneImageWithName:@"ktv_mv_tempBg"];
    self.bgImgView.layer.cornerRadius = 10;
    self.bgImgView.layer.masksToBounds = YES;
    [self addSubview:self.bgImgView];
    
    self.loadingView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleLarge];
    self.loadingView.color = [UIColor whiteColor];
    [self.loadingView setHidden:YES];
    [self addSubview:self.loadingView];
    
    self.loadingTipsLabel = [[UILabel alloc] init];
    self.loadingTipsLabel.font = [UIFont systemFontOfSize:14];
    self.loadingTipsLabel.textColor = [UIColor whiteColor];
    [self addSubview:self.loadingTipsLabel];
    
    self.contentView = [[UIView alloc] initWithFrame:self.bounds];
    self.contentView.backgroundColor = [UIColor clearColor];
    [self addSubview:self.contentView];
    
    UIImageView *currentPlayImgView = [[UIImageView alloc]initWithFrame:CGRectMake(9, 2, 39, 39)];
    currentPlayImgView.image = [UIImage sceneImageWithName:@"ktv_currentPlay_icon"];
    [self.contentView addSubview:currentPlayImgView];

    self.musicTitleLabel.frame = CGRectMake(currentPlayImgView.right+2, currentPlayImgView.centerY-9, 120, 18);
    [self.contentView addSubview:self.musicTitleLabel];
    
    self.gradeView = [[GradeView alloc]init];
    self.gradeView.frame = CGRectMake(15, 15, self.width - 30, 30);
    [self.contentView addSubview:self.gradeView];

    CGFloat lY = CGRectGetMaxX(currentPlayImgView.frame);
    CGFloat lH = self.height - lY;
   // [KaraokeView setLogWithPrintToConsole:true writeToFile:true];
    _karaokeView = [[KaraokeView alloc] initWithFrame:CGRectMake(0, lY, self.width, lH - 40) loggers:@[[FileLogger new]]];
    _karaokeView.scoringView.viewHeight = 50;
    _karaokeView.scoringView.topSpaces = 5;
   // _karaokeView.scoringView.showDebugView = true;
    _karaokeView.backgroundImage = [UIImage imageNamed:@"ktv_top_bgIcon"];
    [self.contentView addSubview:_karaokeView];
    
    self.incentiveView = [[IncentiveView alloc]init];
    self.incentiveView.frame = CGRectMake(15, 55, 192, 45);
    [self.karaokeView addSubview:self.incentiveView];

    self.pauseBtn.frame = CGRectMake(20, self.height-54, 34, 54);
    [self updateBtnLayout:self.pauseBtn];
    [self addSubview:self.pauseBtn];

    self.nextButton.frame = CGRectMake(_pauseBtn.right+10, _pauseBtn.top, 34, 54);
    [self updateBtnLayout:self.nextButton];
    [self addSubview:self.nextButton];
    
    self.originBtn.frame = CGRectMake(self.width-20-48, _pauseBtn.top, 34, 54);
    [self updateBtnLayout:self.originBtn];
    [self addSubview:self.originBtn];
    
    self.settingBtn.frame = CGRectMake(_originBtn.left-10-34, _pauseBtn.top, 34, 54);
    [self updateBtnLayout:self.settingBtn];
    [self addSubview:self.settingBtn];
    
    self.idleView = [[VLKTVMVIdleView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height) withDelegate:self];
    self.idleView.hidden = NO;
    [self addSubview:self.idleView];

    [self setPlayerViewsHidden:YES nextButtonHidden:YES playButtonHidden:YES];

 //   [self setPlayerViewsHidden:YES nextButtonHidden:YES];
    
    self.joinChorusBtn = [[UIButton alloc]initWithFrame:CGRectMake(self.width / 2.0 - 56, self.height - 44, 112, 34)];
    [self.joinChorusBtn setBackgroundImage:[UIImage sceneImageWithName:@"ic_join_chorus"] forState:UIControlStateNormal];
    [self.joinChorusBtn setBackgroundImage:[UIImage sceneImageWithName:@"ic_join_chorus_loading"] forState:UIControlStateSelected];
    _joinChorusBtn.layer.cornerRadius = 17;
    _joinChorusBtn.layer.masksToBounds = true;
    [self.joinChorusBtn addTarget:self action:@selector(joinChorus) forControlEvents:UIControlEventTouchUpInside];
    [self.contentView addSubview:_joinChorusBtn];
    
    self.leaveChorusBtn = [[UIButton alloc]initWithFrame:CGRectMake(10, _pauseBtn.top, 54, 54)];
    [self.leaveChorusBtn setTitle:@"退出合唱" forState:UIControlStateNormal];
    [self.leaveChorusBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [self.leaveChorusBtn setFont:[UIFont systemFontOfSize:11]];
    [self.leaveChorusBtn setImage:[UIImage sceneImageWithName:@"leaveChorus"] forState:UIControlStateNormal];
    [self.leaveChorusBtn addTarget:self action:@selector(leaveChorus) forControlEvents:UIControlEventTouchUpInside];
    [self updateBtnLayout:self.leaveChorusBtn];
    [self addSubview:self.leaveChorusBtn];
    _joinChorusBtn.hidden = _leaveChorusBtn.hidden = YES;
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
    button.imageEdgeInsets = UIEdgeInsetsMake(- (totalHeight - imageSize.height + spacing), 0.0, 10.0, - titleSize.width);
    button.titleEdgeInsets = UIEdgeInsetsMake(0, - imageSize.width, - (totalHeight - titleSize.height + spacing), 0);

}

- (void)_refreshOriginButton {
    if (self.originBtn.selected) {
        [self.originBtn setTitle:KTVLocalizedString(@"原唱") forState:UIControlStateNormal];
        [self.originBtn setTitle:KTVLocalizedString(@"原唱") forState:UIControlStateSelected];
    } else {
        [self.originBtn setTitle:KTVLocalizedString(@"原唱") forState:UIControlStateNormal];
        [self.originBtn setTitle:KTVLocalizedString(@"原唱") forState:UIControlStateSelected];
    }
    [self setNeedsLayout];
}

-(void)joinChorus{
    //加入合唱
    if([self.delegate respondsToSelector:@selector(didJoinChours)]) {
        self.joinChorusBtn.selected = false;
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

-(void)configJoinChorusState:(BOOL)isSuccess {
    if(isSuccess){
        self.joinChorusBtn.selected = false;
        self.joinChorusBtn.hidden = true;
    } else {
        _joinChorusBtn.selected = false;
        _joinChorusBtn.hidden = false;
    }
}

- (void)configPlayerControls:(VLRoomSelSongModel *)song role:(KTVSingRole)role {
    // 是主唱/伴唱
    switch (role) {
        case KTVSingRoleSoloSinger:
        case KTVSingRoleLeadSinger: {
            [self setPlayerViewsHidden:NO nextButtonHidden:NO playButtonHidden:NO];
            _joinChorusBtn.hidden = YES;
            _leaveChorusBtn.hidden = YES;
        } break;
        case KTVSingRoleCoSinger: {
//        case KTVSingRoleFollowSinger:
            BOOL isNextEnable = !VLUserCenter.user.ifMaster;
            [self setPlayerViewsHidden:NO nextButtonHidden:isNextEnable playButtonHidden:YES];
            _joinChorusBtn.hidden = YES;
            _leaveChorusBtn.hidden = NO;
        } break;
        case KTVSingRoleAudience:
        default: {
            if(VLUserCenter.user.ifMaster) {
                [self setPlayerViewsHidden:YES nextButtonHidden:NO playButtonHidden:YES];
            } else {
                [self setPlayerViewsHidden:YES nextButtonHidden:YES playButtonHidden:YES];
            }
            
            _joinChorusBtn.hidden = NO;
            _leaveChorusBtn.hidden = YES;
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
    self.idleView.hidden = song;
    self.scoreLabel.hidden = NO;
    
    if(song) {
        NSString *songText = [NSString stringWithFormat:@"%@-%@",song.songName,song.singer];
        self.musicTitleLabel.text = songText;
        [self configPlayerControls:song role:role];
    } else {
        _joinChorusBtn.hidden = YES;
        _leaveChorusBtn.hidden = YES;
    }
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

- (void)start {
}

- (void)stop {
    [_karaokeView reset];
}

- (void)reset {
    KTVLogInfo(@"VLKTVMVView reset [%@]", self.musicTitleLabel.text);
    [_karaokeView reset];
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
        [_originBtn setTitle:KTVLocalizedString(@"原唱") forState:UIControlStateNormal];
        _originBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
        _originBtn.titleLabel.font = UIFontMake(10.0);
        [self.originBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [self.originBtn setImage:[UIImage sceneImageWithName:@"ic_play_original_off"] forState:UIControlStateNormal];
        [self.originBtn setImage:[UIImage sceneImageWithName:@"ic_play_original_on"] forState:UIControlStateSelected];
        _originBtn.selected = NO;
        [_originBtn addTarget:self action:@selector(originClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _originBtn;
}

- (VLHotSpotBtn *)settingBtn {
    if (!_settingBtn) {
        _settingBtn = [[VLHotSpotBtn alloc] init];
        [_settingBtn setImage:[UIImage sceneImageWithName:@"ktv_subtitle_icon"] forState:UIControlStateNormal];
        [self.settingBtn setTitle:@"调音" forState:UIControlStateNormal];
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
        _scoreUnitLabel.text = KTVLocalizedString(@"分");
    }
    return _scoreUnitLabel;
}

@end
