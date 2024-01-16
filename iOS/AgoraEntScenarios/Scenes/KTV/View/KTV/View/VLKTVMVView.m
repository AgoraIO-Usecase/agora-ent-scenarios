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
@import SDWebImage;

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

//@property (nonatomic, strong) UIButton *originBtn;  /// 原唱按钮
@property (nonatomic, strong) UIButton *trackBtn;  /// track按钮
@property (nonatomic, strong) VLHotSpotBtn *settingBtn; /// 设置参数按钮
@property (nonatomic, strong) VLKTVMVIdleView *idleView;//没有人演唱视图


//@property (nonatomic, strong) AgoraLrcScoreConfigModel *config;
@property (nonatomic, assign) int totalLines;
@property (nonatomic, assign) double totalScore;
@property (nonatomic, assign) double currentTime;

@property (nonatomic, assign) BOOL isPlayAccompany;
@property (nonatomic, strong) UIButton *leaveChorusBtn;
@property (nonatomic, strong) UIView *BotView;
@property (nonatomic, assign) VLKTVMVViewActionType actionType;
@property (nonatomic, strong) UIView *perShowView;//突出人声视图
@property (nonatomic, strong) UIImageView *iconView;
@end

@implementation VLKTVMVView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVMVViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self setupView];
        self.currentTime = 0;
        self.actionType = VLKTVMVViewActionTypeSingAcc;
    }
    return self;
}

#pragma setter
- (void)setIsPlayAccompany:(BOOL)isPlayAccompany {
    [self.trackBtn setSelected:!isPlayAccompany];
   // [self _refreshOriginButton];
    
    VLKTVMVViewActionType targetOrigin = isPlayAccompany ? VLKTVMVViewActionTypeSingAcc : VLKTVMVViewActionTypeSingOrigin;
    if ([self.delegate respondsToSelector:@selector(onKTVMVView:btnTappedWithActionType:)]) {
        [self.delegate onKTVMVView:self btnTappedWithActionType:targetOrigin];
    }
}

-(void)setOriginType:(BOOL)isLeader{
    _isOriginLeader = isLeader;
}

- (void)setLoadingProgress:(NSInteger)loadingProgress {
    _loadingProgress = loadingProgress;
#if DEBUG
    self.loadingTipsLabel.text = [NSString stringWithFormat:@"loading %ld%%", loadingProgress];
#else
    self.loadingTipsLabel.text = KTVLocalizedString(@"ktv_lrc_loading");
#endif
    [self.loadingTipsLabel setHidden:loadingProgress == 100];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    [self.loadingView sizeToFit];
    self.loadingView.center = CGRectGetCenter(self.bounds);
    CGFloat tipsTop = self.loadingView.bottom + 5;
    self.loadingTipsLabel.frame = CGRectMake(self.loadingView.centerX - 100, tipsTop, 200, 40);
    
    [self.retryButton sizeToFit];
    self.retryButton.center = CGPointMake(self.loadingView.centerX, self.loadingTipsLabel.bottom + self.retryButton.height / 2);
}

#pragma mark private

- (void)setupView {
    self.bgImgView = [[UIImageView alloc]initWithFrame:self.bounds];
    self.bgImgView.image = [UIImage ktv_sceneImageWithName:@"bg-lyric" ];
//    self.bgImgView.layer.cornerRadius = 10;
//    self.bgImgView.layer.masksToBounds = YES;
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
    [self.retryButton setTitle:KTVLocalizedString(@"ktv_retry") forState:UIControlStateNormal];
    [self.retryButton sizeToFit];
    self.retryButton.layer.cornerRadius = self.retryButton.height / 2;
    self.retryButton.layer.borderWidth = 1;
    self.retryButton.layer.borderColor = [UIColor whiteColor].CGColor;
    [self addSubview:self.retryButton];
    self.retryButton.hidden = YES;
    
    UIImageView *currentPlayImgView = [[UIImageView alloc]initWithFrame:CGRectMake(9, 2, 39, 39)];
    currentPlayImgView.image = [UIImage ktv_sceneImageWithName:@"ktv_currentPlay_icon" ];
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
    _karaokeView.lyricsView.inactiveLineTextColor = [UIColor colorWithRed:1 green:1 blue:1 alpha:0.5];
    _karaokeView.lyricsView.activeLinePlayedTextColor = [UIColor colorWithHexString:@"#FF8AB4"];
    _karaokeView.lyricsView.lyricLineSpacing = 6;
   // _karaokeView.scoringView.showDebugView = true;
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
    
    self.trackBtn.frame = CGRectMake(self.width-20-48, 0, 34, 54);
    [self updateBtnLayout:self.trackBtn];
    [self.BotView addSubview:self.trackBtn];
    
    self.settingBtn.frame = CGRectMake(_trackBtn.left-10-34, 0, 34, 54);
    [self updateBtnLayout:self.settingBtn];
    [self.BotView addSubview:self.settingBtn];
    
    self.idleView = [[VLKTVMVIdleView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height) withDelegate:self];
    self.idleView.hidden = NO;
    [self addSubview:self.idleView];
    
    self.joinChorusBtn = [[UIButton alloc]initWithFrame:CGRectMake(self.width / 2.0 - 56, 10, 112, 34)];
    [self.joinChorusBtn setBackgroundImage:[UIImage ktv_sceneImageWithName:@"ic_join_chorus" ] forState:UIControlStateNormal];
    [self.joinChorusBtn setBackgroundImage:[UIImage ktv_sceneImageWithName:@"ic_join_chorus_loading" ] forState:UIControlStateSelected];
    _joinChorusBtn.layer.cornerRadius = 17;
    _joinChorusBtn.layer.masksToBounds = true;
    [self.joinChorusBtn addTarget:self action:@selector(joinChorus:) forControlEvents:UIControlEventTouchUpInside];
    [self.BotView addSubview:_joinChorusBtn];
    
    self.leaveChorusBtn = [[UIButton alloc]initWithFrame:CGRectMake(15, 0, 54, 54)];
    [self.leaveChorusBtn setTitle:KTVLocalizedString(@"ktv_leave_chorus") forState:UIControlStateNormal];
    [self.leaveChorusBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [self.leaveChorusBtn setFont:UIFontMake(10.0)];
    [self.leaveChorusBtn setImage:[UIImage ktv_sceneImageWithName:@"Union" ] forState:UIControlStateNormal];
    [self.leaveChorusBtn addTarget:self action:@selector(leaveChorus) forControlEvents:UIControlEventTouchUpInside];
    [self updateBtnLayout:self.leaveChorusBtn];
    [self.BotView addSubview:self.leaveChorusBtn];
    
    _perShowView = [[UIView alloc]initWithFrame:CGRectMake(0, self.bounds.size.height / 2.0 - 12, 80, 24)];
    _perShowView.backgroundColor = [UIColor colorWithRed:8/255.0 green:6/255.0 blue:47/255.0 alpha:0.3];
    CAShapeLayer *maskLayer = [CAShapeLayer layer];
    UIBezierPath *path = [UIBezierPath bezierPathWithRoundedRect:_perShowView.bounds
                                                   byRoundingCorners:UIRectCornerTopRight | UIRectCornerBottomRight
                                                         cornerRadii:CGSizeMake(10.f, 10.f)];
    maskLayer.path = path.CGPath;
    _perShowView.layer.mask = maskLayer;
    [self addSubview:_perShowView];
    
    UILabel *perLabel = [[UILabel alloc]initWithFrame:CGRectMake(8, 6, 45, 12)];
    perLabel.text = KTVLocalizedString(@"ktv_show_vol");
    perLabel.font = [UIFont systemFontOfSize:11];
    perLabel.textColor = [UIColor whiteColor];
    [_perShowView addSubview:perLabel];
    
    _iconView = [[UIImageView alloc]initWithFrame:CGRectMake(57, 2, 20, 20)];
    _iconView.image = [UIImage ktv_sceneImageWithName:@"ktv_showVoice" ];
    [_perShowView addSubview:_iconView];
    _perShowView.hidden = true;
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(voiceChoose)];
    _perShowView.userInteractionEnabled = true;
    [_perShowView addGestureRecognizer:tap];
}

-(void)voiceChoose{
    if([self.delegate respondsToSelector:@selector(didShowVoiceChooseView)]){
        [self.delegate didShowVoiceChooseView];
    }
}

-(void)setPerViewHidden:(BOOL)isHidden {
    _perShowView.hidden = isHidden;
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
    if(self.actionType == VLKTVMVViewActionTypeSingAcc){
        [self.trackBtn setTitle:KTVLocalizedString(@"ktv_ori_sing") forState:UIControlStateNormal];
    } else if (self.actionType == VLKTVMVViewActionTypeSingLead) {
        [self.trackBtn setTitle:KTVLocalizedString(@"ktv_lead_sing") forState:UIControlStateHighlighted];
    } else if (self.actionType == VLKTVMVViewActionTypeSingOrigin) {
        [self.trackBtn setTitle:KTVLocalizedString(@"ktv_ori_sing") forState:UIControlStateSelected];
    }
    [self setNeedsLayout];
}

-(void)joinChorus:(UIButton *)btn{
    //加入合唱
    if(btn.selected == true){
        return;
    }
    btn.selected = true;
    if([self.delegate respondsToSelector:@selector(didJoinChours)]) {
        [self.delegate didJoinChours];
    }
}

-(void)leaveChorus{
    //离开合唱
    self.joinChorusBtn.selected = false;
    if([self.delegate respondsToSelector:@selector(didLeaveChours)]) {
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
    self.bgImgView.image = [UIImage ktv_sceneImageWithName:selBgModel.imageName ];
}

/***
 VLKTVMVViewStateNone = 0,  // 当前无人点歌
 VLKTVMVViewStateMusicLoading = 1,  // 当前歌曲加载中
 VLKTVMVViewStateAudience = 2, //观众
 VLKTVMVViewStateOwnerSing = 3, //房主点歌演唱
 VLKTVMVViewStateOwnerAudience = 4, //房主未加入合唱
 VLKTVMVViewStateOwnerChorus = 5, //房主合唱
 VLKTVMVViewStateNotOwnerChorus = 6, //非房主演唱
 VLKTVMVViewStateMusicOwnerLoadFailed = 7, //点歌人歌曲加载失败(房主或者点歌者 一样的)
 VLKTVMVViewStateMusicLoadFailed = 8, //观众歌曲加载失败
 VLKTVMVViewStateMusicOwnerLoadLrcFailed = 9, //点歌人歌曲加载失败(房主)
 VLKTVMVViewStateMusicLoadLrcFailed = 10, //观众歌词加载失败
*/
-(void)setMvState:(VLKTVMVViewState)mvState {
    _mvState = mvState;
    switch (mvState) {
        case VLKTVMVViewStateNone://无人演唱
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = YES;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = YES;
            self.nextButton.hidden = YES;
            self.trackBtn.hidden = YES;
            self.contentView.hidden = true;
            self.idleView.hidden = NO;
            self.retryButton.hidden = YES;
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = YES;
            break;
        case VLKTVMVViewStateMusicLoading:
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = YES;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = YES;
            self.nextButton.hidden = YES;
            self.trackBtn.hidden = YES;
            self.contentView.hidden = YES;
            self.idleView.hidden = YES;
            self.retryButton.hidden = YES;
            self.musicTitleLabel.hidden = YES;
            self.loadingView.hidden = NO;
            break;
        case VLKTVMVViewStateAudience:
            self.joinChorusBtn.hidden = NO;
            self.joinChorusBtn.selected = NO;
            self.leaveChorusBtn.hidden = YES;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = YES;
            self.nextButton.hidden = YES;
            self.trackBtn.hidden = YES;
            self.contentView.hidden = NO;
            self.idleView.hidden = YES;
            self.retryButton.hidden = YES;
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = NO;
            break;
        case VLKTVMVViewStateOwnerSing:
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = YES;
            self.pauseBtn.hidden = NO;
            self.pauseBtn.selected = YES;
            self.settingBtn.hidden = NO;
            self.nextButton.hidden = NO;
            self.trackBtn.hidden = NO;
            self.contentView.hidden = NO;
            self.idleView.hidden = YES;
            self.retryButton.hidden = YES;
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = NO;
            break;
        case VLKTVMVViewStateOwnerAudience:
            self.joinChorusBtn.hidden = NO;
            self.joinChorusBtn.selected = NO;
            self.leaveChorusBtn.hidden = YES;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = YES;
            self.nextButton.hidden = NO;
            self.trackBtn.hidden = YES;
            self.contentView.hidden = NO;
            self.idleView.hidden = YES;
            self.retryButton.hidden = YES;
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = NO;
            break;
        case VLKTVMVViewStateOwnerChorus:
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = NO;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = NO;
            self.nextButton.hidden = NO;
            self.trackBtn.hidden = NO;
            self.contentView.hidden = NO;
            self.idleView.hidden = YES;
            self.retryButton.hidden = YES;
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = NO;
            break;
        case VLKTVMVViewStateNotOwnerChorus:
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = NO;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = NO;
            self.nextButton.hidden = YES;
            self.trackBtn.hidden = NO;
            self.contentView.hidden = NO;
            self.idleView.hidden = YES;
            self.retryButton.hidden = YES;
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = NO;
            break;
        case VLKTVMVViewStateMusicOwnerLoadFailed:
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = YES;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = YES;
            self.nextButton.hidden = NO;
            self.trackBtn.hidden = YES;
            self.contentView.hidden = NO;
            self.idleView.hidden = YES;
            self.retryButton.hidden = YES;
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = NO;
            break;
        case VLKTVMVViewStateMusicLoadFailed:
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = YES;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = YES;
            self.nextButton.hidden = YES;
            self.trackBtn.hidden = YES;
            self.contentView.hidden = NO;
            self.idleView.hidden = YES;
            self.retryButton.hidden = YES;
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = NO;
            break;
        case VLKTVMVViewStateMusicOwnerLoadLrcFailed:
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = YES;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = YES;
            self.nextButton.hidden = NO;
            self.trackBtn.hidden = YES;
            self.contentView.hidden = NO;
            self.idleView.hidden = YES;
            self.retryButton.hidden = NO;
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = NO;
            [self bringSubviewToFront:self.retryButton];
            break;
        case VLKTVMVViewStateMusicLoadLrcFailed:
            self.joinChorusBtn.hidden = YES;
            self.leaveChorusBtn.hidden = YES;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = YES;
            self.nextButton.hidden = YES;
            self.trackBtn.hidden = YES;
            self.contentView.hidden = NO;
            self.idleView.hidden = YES;
            self.retryButton.hidden = NO;
            [self bringSubviewToFront:self.retryButton];
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = NO;
            break;
        case VLKTVMVViewStateJoinChorus:
            self.joinChorusBtn.hidden = NO;
            self.joinChorusBtn.selected = YES;
            self.leaveChorusBtn.hidden = YES;
            self.pauseBtn.hidden = YES;
            self.settingBtn.hidden = YES;
            self.nextButton.hidden = YES;
            self.trackBtn.hidden = YES;
            self.contentView.hidden = NO;
            self.idleView.hidden = YES;
            self.retryButton.hidden = YES;
            self.loadingView.hidden = YES;
            self.musicTitleLabel.hidden = NO;
            break;
        default:
            break;
    }
    
    if((self.pauseBtn.isHidden == NO || self.leaveChorusBtn.hidden == NO) && self.nextButton.isHidden == NO){
        self.nextButton.frame = CGRectMake(_pauseBtn.right+10, _pauseBtn.top, 34, 54);
    } else {
        self.nextButton.frame = CGRectMake(20, 0, 34, 54);
    }
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

-(void)setSongNameWith:(NSString *)text{
    self.musicTitleLabel.text = text;
}

- (void)setSongScore:(int)score {
    self.scoreLabel.text = [NSString stringWithFormat:@"%d",score];
}

-(void)setPlayState:(BOOL)isPlaying{
    self.pauseBtn.selected = !isPlaying;
}

-(void)setPerViewAvatar:(NSString *)url {
    if([url isEqualToString:@""]){
        _iconView.image = [UIImage ktv_sceneImageWithName:@"ktv_showVoice" ];
    } else {
        [_iconView sd_setImageWithURL:[NSURL URLWithString:url]];
    }
}

#pragma mark - 合唱代理
- (void)setOriginBtnState:(VLKTVMVViewActionType)type
{
    switch (type) {
        case VLKTVMVViewActionTypeSingOrigin:
            _trackBtn.selected = YES;
            [_trackBtn setTitle:KTVLocalizedString(@"ktv_ori_sing") forState:UIControlStateSelected];
            [self.trackBtn setImage:[UIImage ktv_sceneImageWithName:@"original" ] forState:UIControlStateSelected];
            break;
        case VLKTVMVViewActionTypeSingLead:
            _trackBtn.selected = NO;
            [_trackBtn setTitle:KTVLocalizedString(@"ktv_lead_sing") forState:UIControlStateNormal];
            [self.trackBtn setImage:[UIImage ktv_sceneImageWithName:@"original" ] forState:UIControlStateNormal];
            break;
        case VLKTVMVViewActionTypeSingAcc:
            _trackBtn.selected = NO;
            [_trackBtn setTitle:KTVLocalizedString(@"ktv_ori_sing") forState:UIControlStateNormal];
            [self.trackBtn setImage:[UIImage ktv_sceneImageWithName:@"acc" ] forState:UIControlStateNormal];
            break;
        default:
            break;
    }
}

- (void)originClick:(UIButton *)button {
    VLKTVMVViewActionType targetOrigin = VLKTVMVViewActionTypeSingAcc;
    if(self.isOriginLeader){
        if(self.actionType == VLKTVMVViewActionTypeSingOrigin){
            targetOrigin = VLKTVMVViewActionTypeSingAcc;
        } else if(self.actionType == VLKTVMVViewActionTypeSingLead) {
            targetOrigin = VLKTVMVViewActionTypeSingOrigin;
        } else if(self.actionType == VLKTVMVViewActionTypeSingAcc) {
            targetOrigin = VLKTVMVViewActionTypeSingLead;
        }
    } else {
        button.selected = !button.isSelected;
        targetOrigin = button.isSelected ? VLKTVMVViewActionTypeSingOrigin : VLKTVMVViewActionTypeSingAcc;
    }
    self.actionType = targetOrigin;
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
        [self.pauseBtn setTitle:KTVLocalizedString(@"ktv_room_player_play") forState:UIControlStateNormal];
        [self.pauseBtn setTitle:KTVLocalizedString(@"ktv_room_player_pause") forState:UIControlStateSelected];
        [_pauseBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_pause_icon" ] forState:UIControlStateSelected];
        [_pauseBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_pause_resumeicon" ] forState:UIControlStateNormal];
        _pauseBtn.selected = NO;
        [_pauseBtn addTarget:self action:@selector(playClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _pauseBtn;
}

- (VLHotSpotBtn *)nextButton {
    if (!_nextButton) {
        _nextButton = [[VLHotSpotBtn alloc] init];
        [_nextButton setImage:[UIImage ktv_sceneImageWithName:@"ktv_playNext_icon" ] forState:UIControlStateNormal];
        [self.nextButton setTitle:KTVLocalizedString(@"ktv_room_change_song") forState:UIControlStateNormal];
        [self.nextButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        self.nextButton.titleLabel.font = UIFontMake(10.0);
        [_nextButton addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _nextButton;
}


- (VLHotSpotBtn *)trackBtn {
    if (!_trackBtn) {
        _trackBtn = [[VLHotSpotBtn alloc] init];
        [self.trackBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        self.trackBtn.titleLabel.font = UIFontMake(10.0);
        [self.trackBtn setTitle:KTVLocalizedString(@"ktv_ori_sing") forState:UIControlStateNormal];
        [self.trackBtn setTitle:KTVLocalizedString(@"ktv_lead_sing") forState:UIControlStateSelected];
        [_trackBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_mic_acc" ] forState:UIControlStateSelected];
        [_trackBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_mic_origin" ] forState:UIControlStateNormal];
        _trackBtn.selected = NO;
        [_trackBtn addTarget:self action:@selector(originClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _trackBtn;
}

- (VLHotSpotBtn *)settingBtn {
    if (!_settingBtn) {
        _settingBtn = [[VLHotSpotBtn alloc] init];
        [_settingBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_subtitle_icon" ] forState:UIControlStateNormal];
        _settingBtn.accessibilityIdentifier = @"ktv_room_setting_button_id";
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
