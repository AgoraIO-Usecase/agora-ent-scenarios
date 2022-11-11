//
//  VLKTVMVView.m
//  VoiceOnLine
//

#import "VLKTVMVView.h"
#import "VLKTVSelBgModel.h"

#import "VLRobMicrophoneView.h"
#import "VLSoloSongView.h"
#import "VLNoBodyOnLineView.h"
#import "HWWeakTimer.h"
#import "VLHotSpotBtn.h"
#import "VLUserCenter.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "KTVMacro.h"

@import QMUIKit;
@import YYCategories;
@import Masonry;

@class QMUIButton;
@interface VLKTVMVView () < AgoraLrcDownloadDelegate,VLNoBodyOnLineViewDelegate,VLRobMicrophoneViewDelegate,VLSoloSongViewDelegate,AgoraKaraokeScoreDelegate>

@property(nonatomic, weak) id <VLKTVMVViewDelegate>delegate;

@property (nonatomic, strong) UILabel *musicTitleLabel;
@property (nonatomic, strong) UILabel *scoreLabel;
// 分数分开优化性能
@property (nonatomic, strong) UILabel *scoreUnitLabel;

@property (nonatomic, strong) VLHotSpotBtn *pauseBtn; /// 暂停播放
@property (nonatomic, strong) VLHotSpotBtn *nextButton; /// 下一首
@property (nonatomic, strong) QMUIButton *originBtn;  /// 原唱按钮
@property (nonatomic, strong) VLHotSpotBtn *subtitleBtn; /// 设置参数按钮
@property (nonatomic, strong) VLRobMicrophoneView *robMicrophoneView; // 合唱倒计时视图
@property (nonatomic, strong) VLSoloSongView *soloSongView; // 独唱倒计时视图
@property (nonatomic, strong) VLNoBodyOnLineView *noBodyOnLineView;//没有人演唱视图

@property (nonatomic, strong) NSTimer *soloTimer;    //独唱倒计时
@property (nonatomic, strong) NSTimer *robMicroPhoneTimer; //抢麦倒计时

@property (nonatomic, strong) AgoraLrcScoreConfigModel *config;
@property (nonatomic, assign) VLKTVMVViewActionType singType;
@property (nonatomic, assign) int totalLines;
@property (nonatomic, assign) double totalScore;
@property (nonatomic, assign) double currentTime;
@end

@implementation VLKTVMVView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVMVViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self setupView];
        self.singType = VLKTVMVViewActionTypeSingOrigin;
        self.currentTime = 0;
    }
    return self;
}

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
    
    self.subtitleBtn.frame = CGRectMake(_originBtn.left-20-24, _pauseBtn.top, 24, 24);
    [self addSubview:self.subtitleBtn];
        
    self.robMicrophoneView = [[VLRobMicrophoneView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height) withDelegate:self];
    self.robMicrophoneView.hidden = NO;
    [self addSubview:self.robMicrophoneView];
    
    self.soloSongView = [[VLSoloSongView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height) withDelegate:self];
    self.soloSongView.hidden = YES;
    [self addSubview:self.soloSongView];
    
    self.noBodyOnLineView = [[VLNoBodyOnLineView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height) withDelegate:self];
    self.noBodyOnLineView.hidden = NO;
    [self addSubview:self.noBodyOnLineView];
    
    self.lrcView.config = self.config;
    [self setPlayerViewsHidden:YES nextButtonHidden:YES];
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

- (void)setVoicePitch:(NSArray <NSNumber *> *)pitch {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.lrcView setVoicePitch:pitch];
    });
    
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
    if (sender == self.subtitleBtn) {
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

- (void)configLrcViewUIWithCurrentSong:(VLRoomSelSongModel *)song {
    // 是自己点的歌曲
    if (song.isOwnSong) {
        [self setPlayerViewsHidden:NO nextButtonHidden:NO];
    }
    else if(VLUserCenter.user.ifMaster) {
        [self setPlayerViewsHidden:YES nextButtonHidden:NO];
    }
    else {
        [self setPlayerViewsHidden:YES nextButtonHidden:YES];
    }
}

- (void)setPlayerViewsHidden:(BOOL)hidden nextButtonHidden:(BOOL)nextButtonHidden{
    self.pauseBtn.hidden = hidden;
    self.nextButton.hidden = nextButtonHidden;
    self.originBtn.hidden = hidden;
    self.subtitleBtn.hidden = hidden;
}

- (BOOL)isPlaying:(VLRoomSelSongModel *)song {
    if (song.status == 2) {
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

- (void)updateUIWithUserOnSeat:(BOOL)onSeat song:(VLRoomSelSongModel *)song {
    if (onSeat) {
        if (!song) {
            self.noBodyOnLineView.hidden = NO;
        } else {
            self.noBodyOnLineView.hidden = YES;
        }
    } else {
        self.config.isHiddenScoreView = YES;
        self.scoreLabel.hidden = YES;
        self.scoreUnitLabel.hidden = YES;
        
        if(!song) {
            self.noBodyOnLineView.hidden = NO;
        } else {
            self.noBodyOnLineView.hidden = YES;
        }
        self.robMicrophoneView.hidden = YES;
        self.soloSongView.hidden = YES;
    }
    
    // 是否是合唱歌曲
    if (song.isChorus) {
        self.config.isHiddenScoreView = NO;
        self.scoreLabel.hidden = NO;
        self.scoreUnitLabel.hidden = NO;
    } else {
        self.config.isHiddenScoreView = NO;
        self.scoreLabel.hidden = NO;
        self.scoreUnitLabel.hidden = NO;
    }

    self.lrcView.config = self.config;
}

- (void)receiveCountDown:(int)countDown onSeat:(BOOL)onSeat currentSong:(VLRoomSelSongModel *)currentSong {
    [self updateUIWithUserOnSeat:onSeat song:currentSong];
    if(onSeat) {
        if (countDown > 0) {
            self.robMicrophoneView.hidden = NO;
            if (currentSong) {
                NSString *songText = [NSString stringWithFormat:@"%@-%@",currentSong.songName,currentSong.singer];
                self.robMicrophoneView.musicLabel.text = songText;
            }
            self.robMicrophoneView.countDownLabel.text = [NSString stringWithFormat:@"00:%02d",countDown];
        } else {
            self.robMicrophoneView.hidden = YES;
            [self soloBtnClickAction];
        }
    }
}

- (void)updateUIWithSong:(VLRoomSelSongModel * __nullable)song onSeat:(BOOL)onSeat {
    [self updateUIWithUserOnSeat:onSeat song:song];
    if (!song) return;
//    self.scoreLabel.text = @"0";
    NSString *songText = [NSString stringWithFormat:@"%@-%@",song.songName,song.singer];
    self.musicTitleLabel.text = songText;
    self.noBodyOnLineView.hidden = YES;
    
    [self configLrcViewUIWithCurrentSong:song];
    
    if (!onSeat) return;
    
    //有正在演唱的歌曲
    if ([self isPlaying:song]) {
        self.robMicrophoneView.hidden = self.soloSongView.hidden = YES;
    
    //根据歌曲判断,如果歌曲是合唱那么显示倒计时视图,如果是独唱就开始播放歌曲 2. 播放歌曲的时候如果歌曲是当前用户点的歌曲(显示底部按钮,可以切歌,暂停,) 如果不是就不显示
    } else {
        VL(weakSelf);
        [self configLrcViewUIWithCurrentSong:song];
        // 歌曲是合唱
        if (song.isChorus) {
            // 歌曲是本人点的 (不等了、独唱)
            if (song.isOwnSong) {
                if (self.soloTimer) return;
                self.soloSongView.hidden = NO;
                self.robMicrophoneView.hidden = YES;
                self.soloSongView.musicLabel.text = songText;
                //开始倒计时(独唱倒计时)
                __block int leftSecond = 20;
                self.soloTimer = [HWWeakTimer scheduledTimerWithTimeInterval:1.0f block:^(id userInfo) {
                    leftSecond -= 1;
                    weakSelf.soloSongView.countDownLabel.text = [NSString stringWithFormat:@"00:%02d",leftSecond];
                    if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(onKTVMVView:timerCountDown:)]) {
                        [weakSelf.delegate onKTVMVView:self timerCountDown:leftSecond];
                    }
                    if (leftSecond == 0) {
                        //倒计时结束、执行独唱（销毁定时器）
                        [weakSelf soloBtnClickAction];
                    }

                } userInfo:@"Fire" repeats:YES];
                [self.soloTimer fire];
            }else{
                if (self.robMicroPhoneTimer) return;
                self.soloSongView.hidden = YES;
                self.robMicrophoneView.hidden = NO;
                self.robMicrophoneView.musicLabel.text = songText;
                //开始倒计时(抢麦)
                //开始倒计时(独唱倒计时)
                __block int leftSecond = 20;
                self.robMicroPhoneTimer = [HWWeakTimer scheduledTimerWithTimeInterval:1.0f block:^(id userInfo) {
                    leftSecond -= 1;
                    weakSelf.robMicrophoneView.countDownLabel.text = [NSString stringWithFormat:@"00:%02d",leftSecond];
                    //执行合唱
                    if (leftSecond == 0) {
                        //倒计时结束、执行独唱（销毁定时器）
                        [weakSelf soloBtnClickAction];
                    }
                } userInfo:@"Fire" repeats:YES];
                [self.robMicroPhoneTimer fire];
            }
        } else{                       //准备播放歌曲
            self.noBodyOnLineView.hidden = self.robMicrophoneView.hidden = self.soloSongView.hidden = YES;
        }
    }
}

- (void)setJoinInViewHidden { //独唱
    self.soloSongView.hidden = YES;
    self.robMicrophoneView.hidden = YES;
    [self.robMicroPhoneTimer invalidate];
    self.robMicroPhoneTimer = nil;
    [self.soloTimer invalidate];
    self.soloTimer = nil;
}

#pragma mark - VLSoloSongViewDelegate

- (void)soloBtnClickAction {
    [self.soloTimer invalidate];
    self.soloTimer = nil;
    if ([self.delegate respondsToSelector:@selector(onKTVMVView:startSingType:)]) {
        self.soloSongView.hidden = YES;
        [self.delegate onKTVMVView:self startSingType:VLKTVMVViewSingActionTypeSolo];
    }
}

#pragma mark - 合唱代理
- (void)robViewChorusAction {
    //抢麦
    [self.robMicroPhoneTimer invalidate];
    self.robMicroPhoneTimer = nil;
    if ([self.delegate respondsToSelector:@selector(onKTVMVView:startSingType:)]) {
        self.robMicrophoneView.hidden = YES;
        [self.delegate onKTVMVView:self startSingType:VLKTVMVViewSingActionTypeJoinChorus];
    }
//    if([self.delegate respondsToSelector:@selector(ktvIsMyselfOnSeat)]) {
//        if([self.delegate ktvIsMyselfOnSeat]) {
//            [self.robMicroPhoneTimer invalidate];
//            self.robMicroPhoneTimer = nil;
//            if ([self.delegate respondsToSelector:@selector(ktvMVViewDidClickSingType:)]) {
//                self.robMicrophoneView.hidden = YES;
//                [self.delegate ktvMVViewDidClickSingType:VLKTVMVViewSingActionTypeJoinChorus];
//            }
//        }
//        else {
//            if([self.delegate respondsToSelector:@selector(ktvNotifyUserNotOnSeat)]) {
//                [self.delegate ktvNotifyUserNotOnSeat];
//            }
//        }
//    }
}


- (void)originClick:(UIButton *)button {
    button.selected = !button.selected;
    self.singType = button.selected ? VLKTVMVViewActionTypeSingOrigin : VLKTVMVViewActionTypeSingAcc;
    if (button.selected) {
        [self.originBtn setTitle:KTVLocalizedString(@"原唱") forState:UIControlStateNormal];
        [self.originBtn setTitle:KTVLocalizedString(@"原唱") forState:UIControlStateSelected];
    }
    else {
        [self.originBtn setTitle:KTVLocalizedString(@"伴奏") forState:UIControlStateNormal];
        [self.originBtn setTitle:KTVLocalizedString(@"伴奏") forState:UIControlStateSelected];
    }
    
    [self validateSingType];
}

- (void)validateSingType
{
    if ([self.delegate respondsToSelector:@selector(onKTVMVView:btnTappedWithActionType:)]) {
        [self.delegate onKTVMVView:self btnTappedWithActionType:self.singType];
    }
}

#pragma mark - AgoraLrcDownloadDelegate

- (void)beginDownloadLrcWithUrl:(NSString *)url {
    VLLog(@"\n歌词开始下载\n%@",url);
}

- (void)downloadLrcFinishedWithUrl:(NSString *)url {
    VLLog(@"\n歌词下载完成\n%@",url);
}

- (void)downloadLrcProgressWithUrl:(NSString *)url progress:(double)progress {
    VLLog(@"\n歌词下载进度\n%@,%f",url,progress);
}

- (void)downloadLrcErrorWithUrl:(NSString *)url error:(NSError *)error {
    VLLog(@"\n歌词下载失败\n%@\n%@",url,error);
}

- (void)beginParseLrc {
    VLLog(@"歌词开始解析");
}

- (void)parseLrcFinished {
    VLLog(@"歌词解析完成");
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
    self.totalLines = 0;
    self.totalScore = 0.0f;
}

- (void)stop {
    [_lrcView stop];
}

- (void)reset {
    [_lrcView reset];
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
        scoreConfig.scoreViewHeight = 59; // 评分视图高度
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
        lrcConfig.tipsColor = [UIColor whiteColor];

        _config.lrcConfig = lrcConfig;
        _config.scoreConfig = scoreConfig;
    }
    return _config;
}

- (AgoraLrcScoreView *)lrcView {
    if (!_lrcView) {
        _lrcView = [[AgoraLrcScoreView alloc] initWithDelegate:self];
        _lrcView.downloadDelegate = self;
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

- (QMUIButton *)originBtn {
    if (!_originBtn) {
        _originBtn = [[QMUIButton alloc] qmui_initWithImage:nil title:KTVLocalizedString(@"原唱")];
        _originBtn.imagePosition = QMUIButtonImagePositionLeft;
        _originBtn.spacingBetweenImageAndTitle = 2;
        _originBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
        [_originBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
        _originBtn.titleLabel.font = UIFontMake(10.0);
        [_originBtn setImage:[UIImage sceneImageWithName:@"ktv_origin_playOn"] forState:UIControlStateNormal];
        [_originBtn setImage:[UIImage sceneImageWithName:@"ktv_origin_playOn"] forState:UIControlStateSelected];
        _originBtn.selected = YES;
        [_originBtn addTarget:self action:@selector(originClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _originBtn;
}

- (VLHotSpotBtn *)subtitleBtn {
    if (!_subtitleBtn) {
        _subtitleBtn = [[VLHotSpotBtn alloc] init];
        [_subtitleBtn setImage:[UIImage sceneImageWithName:@"ktv_subtitle_icon"] forState:UIControlStateNormal];
        [_subtitleBtn addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _subtitleBtn;
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
