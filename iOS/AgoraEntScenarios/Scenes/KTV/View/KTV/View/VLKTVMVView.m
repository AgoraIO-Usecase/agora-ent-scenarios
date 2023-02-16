//
//  VLKTVMVView.m
//  VoiceOnLine
//

#import "VLKTVMVView.h"
#import "VLKTVSelBgModel.h"

#import "VLJoinChorusView.h"
#import "VLStartSoloView.h"
#import "VLKTVMVIdleView.h"
#import "HWWeakTimer.h"
#import "VLHotSpotBtn.h"
#import "VLUserCenter.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "KTVMacro.h"
@import Masonry;

@interface VLKTVMVView () <VLKTVMVIdleViewDelegate,VLJoinChorusViewDelegate,VLStartSoloViewDelegate,AgoraKaraokeScoreDelegate>

@property(nonatomic, weak) id <VLKTVMVViewDelegate>delegate;

@property (nonatomic, strong) UILabel *musicTitleLabel;
@property (nonatomic, strong) UILabel *scoreLabel;
// 分数分开优化性能
@property (nonatomic, strong) UILabel *scoreUnitLabel;

@property (nonatomic, strong) VLHotSpotBtn *pauseBtn; /// 暂停播放
@property (nonatomic, strong) VLHotSpotBtn *nextButton; /// 下一首

@property (nonatomic, strong) UIButton *originBtn;  /// 原唱按钮
@property (nonatomic, strong) VLHotSpotBtn *settingBtn; /// 设置参数按钮
@property (nonatomic, strong) VLJoinChorusView *joinChorusView; // 合唱倒计时视图
@property (nonatomic, strong) VLStartSoloView *startSoloView; // 独唱倒计时视图
@property (nonatomic, strong) VLKTVMVIdleView *idleView;//没有人演唱视图

@property (nonatomic, strong) AgoraLrcScoreConfigModel *config;
@property (nonatomic, assign) int totalLines;
@property (nonatomic, assign) double totalScore;
@property (nonatomic, assign) double currentTime;

@property (nonatomic, assign) BOOL isPlayAccompany;
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

- (void)layoutSubviews {
    [super layoutSubviews];
    
    [self.originBtn sizeToFit];
    self.originBtn.frame = CGRectMake(self.width-20-self.originBtn.width, _pauseBtn.top, self.originBtn.width, 24);
    self.settingBtn.frame = CGRectMake(_originBtn.left-20-24, _pauseBtn.top, 24, 24);
}

#pragma mark private

- (void)setupView {
    self.bgImgView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height)];
    self.bgImgView.image = [UIImage sceneImageWithName:@"ktv_mv_tempBg"];
    self.bgImgView.layer.cornerRadius = 20;
    self.bgImgView.layer.masksToBounds = YES;
    [self addSubview:self.bgImgView];
    
    UIImageView *currentPlayImgView = [[UIImageView alloc]initWithFrame:CGRectMake(9, 2, 39, 39)];
    currentPlayImgView.image = [UIImage sceneImageWithName:@"ktv_currentPlay_icon"];
    [self addSubview:currentPlayImgView];

    self.musicTitleLabel.frame = CGRectMake(currentPlayImgView.right+2, currentPlayImgView.centerY-9, 120, 18);
    [self addSubview:self.musicTitleLabel];

    CGFloat lY = CGRectGetMaxX(currentPlayImgView.frame);
    CGFloat lH = self.height - lY;
    self.lrcView.frame = CGRectMake(0, lY, self.width, lH);
    [self addSubview:self.lrcView];
    
    self.scoreLabel.hidden = YES;
    [self.lrcView addSubview:self.scoreLabel];
    [self.scoreLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(10);
        make.top.mas_equalTo(35);
    }];
    
    self.scoreUnitLabel.hidden = YES;
    [self.lrcView addSubview:self.scoreUnitLabel];
    [self.scoreUnitLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.scoreLabel.mas_right);
        make.bottom.mas_equalTo(self.scoreLabel.mas_bottom).offset(-2.5);
    }];
    
    self.pauseBtn.frame = CGRectMake(20, self.height-24-12, 24, 24);
    [self addSubview:self.pauseBtn];

    self.nextButton.frame = CGRectMake(_pauseBtn.right+20, _pauseBtn.top, 24, 24);
    [self addSubview:self.nextButton];
    
    self.originBtn.frame = CGRectMake(self.width-20-48, _pauseBtn.top, 48, 24);
    [self addSubview:self.originBtn];
    
    self.settingBtn.frame = CGRectMake(_originBtn.left-20-24, _pauseBtn.top, 24, 24);
    [self addSubview:self.settingBtn];
        
    self.joinChorusView = [[VLJoinChorusView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height) withDelegate:self];
    self.joinChorusView.hidden = NO;
    [self addSubview:self.joinChorusView];
    
    self.startSoloView = [[VLStartSoloView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height) withDelegate:self];
    self.startSoloView.hidden = YES;
    [self addSubview:self.startSoloView];
    
    self.idleView = [[VLKTVMVIdleView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height) withDelegate:self];
    self.idleView.hidden = NO;
    [self addSubview:self.idleView];
    
    self.lrcView.config = self.config;
    [self setPlayerViewsHidden:YES nextButtonHidden:YES playButtonHidden:YES];
}

- (void)_refreshOriginButton {
    if (self.originBtn.selected) {
        [self.originBtn setTitle:KTVLocalizedString(@"原唱") forState:UIControlStateNormal];
        [self.originBtn setTitle:KTVLocalizedString(@"原唱") forState:UIControlStateSelected];
    } else {
        [self.originBtn setTitle:KTVLocalizedString(@"伴奏") forState:UIControlStateNormal];
        [self.originBtn setTitle:KTVLocalizedString(@"伴奏") forState:UIControlStateSelected];
    }
    [self setNeedsLayout];
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

//- (void)setVoicePitch:(NSArray <NSNumber *> *)pitch {
//    [self.lrcView setVoicePitch:pitch];
//}

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

- (void)configPlayerControls:(VLRoomSelSongModel *)song {
    // 是主唱/伴唱
    if (song.isSongOwner) {
        [self setPlayerViewsHidden:NO nextButtonHidden:NO playButtonHidden:NO];
    } else if ([song isSongCoSinger]) {
        [self setPlayerViewsHidden:NO nextButtonHidden:YES playButtonHidden:YES];
    } else if(VLUserCenter.user.ifMaster) {
        [self setPlayerViewsHidden:YES nextButtonHidden:NO playButtonHidden:YES];
    } else {
        [self setPlayerViewsHidden:YES nextButtonHidden:YES playButtonHidden:YES];
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


//- (void)receiveCountDown:(int)countDown onSeat:(BOOL)onSeat currentSong:(VLRoomSelSongModel *)currentSong {
////    [self updateUIWithUserOnSeat:onSeat song:currentSong];
//    if(onSeat) {
//        if (countDown > 0) {
//            self.joinChorusView.hidden = NO;
//            if (currentSong) {
//                NSString *songText = [NSString stringWithFormat:@"%@-%@",currentSong.songName,currentSong.singer];
//                self.joinChorusView.musicLabel.text = songText;
//            }
//            self.joinChorusView.countDownLabel.text = [NSString stringWithFormat:@"00:%02d",countDown];
//        } else {
//            self.joinChorusView.hidden = YES;
//            [self soloBtnClickAction];
//        }
//    }
//}

- (void)updateUIWithSong:(VLRoomSelSongModel * __nullable)song onSeat:(BOOL)onSeat {
    self.idleView.hidden = song;
    self.joinChorusView.hidden = !(song && song.isChorus && ![self isPlaying:song]);
    
    //config score label visibility
    self.config.isHiddenScoreView = NO;
    [self.lrcView setConfig:self.config];
    self.scoreLabel.hidden = NO;
    
    if(song) {
        NSString *songText = [NSString stringWithFormat:@"%@-%@",song.songName,song.singer];
        self.musicTitleLabel.text = songText;
        self.startSoloView.musicLabel.text = songText;
        self.joinChorusView.musicLabelText = songText;
        [self configPlayerControls:song];
    }
    
    self.joinChorusView.hidden = !(![self isPlaying:song] && song.isChorus && song.chorusNo.length == 0 && !song.isSongOwner);
    self.startSoloView.hidden = !(![self isPlaying:song] && song.isChorus && song.chorusNo.length == 0 && song.isSongOwner);
    
//    if (!onSeat) return;
//
//    //有正在演唱的歌曲
//    if ([self isPlaying:song]) {
//        self.robMicrophoneView.hidden = self.soloSongView.hidden = YES;
//
//    //根据歌曲判断,如果歌曲是合唱那么显示倒计时视图,如果是独唱就开始播放歌曲 2. 播放歌曲的时候如果歌曲是当前用户点的歌曲(显示底部按钮,可以切歌,暂停,) 如果不是就不显示
//    } else {
//        VL(weakSelf);
//        [self configLrcViewUIWithCurrentSong:song];
//        // 歌曲是合唱
//        if (song.isChorus) {
//            // 歌曲是本人点的 (不等了、独唱)
//            if (song.isSongOwner) {
//                if (self.soloTimer) return;
//                self.soloSongView.hidden = NO;
//                self.robMicrophoneView.hidden = YES;
//                self.soloSongView.musicLabel.text = songText;
//                //开始倒计时(独唱倒计时)
//                __block int leftSecond = 20;
//                self.soloTimer = [HWWeakTimer scheduledTimerWithTimeInterval:1.0f block:^(id userInfo) {
//                    leftSecond -= 1;
//                    weakSelf.soloSongView.countDownLabel.text = [NSString stringWithFormat:@"00:%02d",leftSecond];
//                    if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(onKTVMVView:timerCountDown:)]) {
//                        [weakSelf.delegate onKTVMVView:self timerCountDown:leftSecond];
//                    }
//                    if (leftSecond == 0) {
//                        //倒计时结束、执行独唱（销毁定时器）
//                        [weakSelf soloBtnClickAction];
//                    }
//
//                } userInfo:@"Fire" repeats:YES];
//                [self.soloTimer fire];
//            }else{
//                if (self.robMicroPhoneTimer) return;
//                self.soloSongView.hidden = YES;
//                self.robMicrophoneView.hidden = NO;
//                self.robMicrophoneView.musicLabel.text = songText;
//                //开始倒计时(抢麦)
//                //开始倒计时(独唱倒计时)
//                __block int leftSecond = 20;
//                self.robMicroPhoneTimer = [HWWeakTimer scheduledTimerWithTimeInterval:1.0f block:^(id userInfo) {
//                    leftSecond -= 1;
//                    weakSelf.robMicrophoneView.countDownLabel.text = [NSString stringWithFormat:@"00:%02d",leftSecond];
//                    //执行合唱
//                    if (leftSecond == 0) {
//                        //倒计时结束、执行独唱（销毁定时器）
//                        [weakSelf soloBtnClickAction];
//                    }
//                } userInfo:@"Fire" repeats:YES];
//                [self.robMicroPhoneTimer fire];
//            }
//        } else{                       //准备播放歌曲
//            self.noBodyOnLineView.hidden = self.robMicrophoneView.hidden = self.soloSongView.hidden = YES;
//        }
//    }
}

- (void)setChorusOptViewHidden { //独唱
    self.startSoloView.hidden = YES;
    self.joinChorusView.hidden = YES;
}

- (void)setCoundDown:(NSInteger)seconds
{
    self.joinChorusView.countDownLabel.text = [NSString stringWithFormat:@"00:%02ld",seconds];
    self.startSoloView.countDownLabel.text = [NSString stringWithFormat:@"00:%02ld",seconds];
}

#pragma mark - VLSoloSongViewDelegate

- (void)onStartSoloBtn {
    [self.delegate onKTVMVView:self chorusSingAction:VLKTVMVViewSingActionTypeSolo];
}

#pragma mark - 合唱代理
- (void)onJoinChorusBtn {
    [self.delegate onKTVMVView:self chorusSingAction:VLKTVMVViewSingActionTypeJoinChorus];
}

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


#pragma mark -

- (void)loadLrcURL:(NSString *)lrcURL {
    [_lrcView setLrcUrlWithUrl:lrcURL];
}

- (void)start {
    [_lrcView start];
}

- (void)stop {
    [_lrcView stop];
}

- (void)reset {
    [_lrcView stop];
    [_lrcView reset];
    [self setSongScore:0];
    self.isPlayAccompany = YES;
    [_lrcView resetTime];
    [self cleanMusicText];
}

- (void)resetTime {
    [_lrcView resetTime];
}

- (void)scrollToTime:(NSTimeInterval)time {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.lrcView scrollToTimeWithTimestamp:time];
    });
}

#pragma mark -

- (AgoraLrcScoreConfigModel *)config {
    if (!_config) {
        // 配置
        _config = [[AgoraLrcScoreConfigModel alloc] init];
        /// 评分组件和歌词组件之间的间距 默认: 0
        _config.spacing = 0;
        /// 背景图
        _config.backgroundImageView = nil;
        _config.isHiddenScoreView = YES;
        
        // 评分组件配置
        AgoraScoreItemConfigModel *scoreConfig = [[AgoraScoreItemConfigModel alloc] init];
        scoreConfig.tailAnimateColor = [UIColor yellowColor];
        scoreConfig.scoreViewHeight = 100; // 评分视图高度
        scoreConfig.emitterColors = @[[UIColor purpleColor]];
        scoreConfig.isHiddenSeparatorLine = NO;
        scoreConfig.separatorLineColor = [UIColor whiteColor];
        
        // 歌词组件配置
        AgoraLrcConfigModel *lrcConfig = [[AgoraLrcConfigModel alloc] init];
        lrcConfig.lrcFontSize = VLUIFontMake(15);
        lrcConfig.isHiddenWatitingView = NO;
        lrcConfig.isHiddenBottomMask = NO;
        lrcConfig.lrcHighlightFontSize = VLUIFontMake(18);
        lrcConfig.lrcTopAndBottomMargin = 8;
        lrcConfig.isHiddenSeparator = YES;
        lrcConfig.isDrag = YES;
        lrcConfig.tipsColor = [UIColor whiteColor];

        _config.lrcConfig = lrcConfig;
        _config.scoreConfig = scoreConfig;
    }
    return _config;
}

- (AgoraLrcScoreView *)lrcView {
    if (!_lrcView) {
        _lrcView = [[AgoraLrcScoreView alloc] initWithDelegate:_delegate];
        _lrcView.downloadDelegate = _delegate;
        _lrcView.backgroundColor = [UIColor clearColor];
        _lrcView.scoreDelegate = self;
        _lrcView.clipsToBounds = YES;
    }
    return _lrcView;
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
        [_nextButton addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _nextButton;
}

- (UIButton *)originBtn {
    if (!_originBtn) {
//        _originBtn = [[QMUIButton alloc] qmui_initWithImage:nil title:KTVLocalizedString(@"原唱")];
//        _originBtn.imagePosition = QMUIButtonImagePositionLeft;
        _originBtn.spacingBetweenImageAndTitle = 2;
        _originBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        [_originBtn setTitle:KTVLocalizedString(@"原唱") forState:UIControlStateNormal];
        _originBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
        [_originBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
        _originBtn.titleLabel.font = UIFontMake(10.0);
        [_originBtn setImage:[UIImage sceneImageWithName:@"ktv_origin_playOn"] forState:UIControlStateNormal];
        [_originBtn setImage:[UIImage sceneImageWithName:@"ktv_origin_playOn"] forState:UIControlStateSelected];
        _originBtn.selected = NO;
        [_originBtn addTarget:self action:@selector(originClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _originBtn;
}

- (VLHotSpotBtn *)settingBtn {
    if (!_settingBtn) {
        _settingBtn = [[VLHotSpotBtn alloc] init];
        [_settingBtn setImage:[UIImage sceneImageWithName:@"ktv_subtitle_icon"] forState:UIControlStateNormal];
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
