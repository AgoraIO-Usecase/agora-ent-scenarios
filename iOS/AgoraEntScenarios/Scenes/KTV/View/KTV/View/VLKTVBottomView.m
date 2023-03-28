//
//  VLKTVBottomView.m
//  VoiceOnLine
//

#import "VLKTVBottomView.h"
#import "VLHotSpotBtn.h"
#import "VLUserCenter.h"
#import "VLURLPathConfig.h"
#import "AgoraEntScenarios-Swift.h"
#import "AppContext+KTV.h"
#import "VLMacroDefine.h"
@import QMUIKit;
@import YYCategories;

typedef void (^actionSuccess)(BOOL ifSuccess);

@interface VLKTVBottomView ()

@property(nonatomic, weak) id <VLKTVBottomViewDelegate>delegate;
@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, strong) NSArray <VLRoomSeatModel *> *seatsArray;
@property (nonatomic, assign) NSInteger isSelfMuted;
@property (nonatomic, assign) NSInteger isVideoMuted;
@property (nonatomic, strong)VLHotSpotBtn *audioBtn;
@property (nonatomic, strong)VLHotSpotBtn *videoBtn;
@end

@implementation VLKTVBottomView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVBottomViewDelegate>)delegate withRoomNo:(NSString *)roomNo withData:(NSArray <VLRoomSeatModel *> *)seatsArray{
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        self.roomNo = roomNo;
        self.seatsArray = seatsArray;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    self.audioBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(25, (self.height-24)*0.5, 24, 24)];
    [self.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_self_muteIcon"] forState:UIControlStateNormal];
    [self.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateSelected];
    self.audioBtn.tag = VLKTVBottomBtnClickTypeAudio;
    [self.audioBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.audioBtn];
    
    self.videoBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(self.audioBtn.right+15, (self.height-24)*0.5, 24, 24)];
    [self.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
    [self.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_icon"] forState:UIControlStateSelected];
    self.videoBtn.tag = VLKTVBottomBtnClickTypeVideo;
    [self.videoBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.videoBtn];
    
    VLHotSpotBtn *moreBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(self.videoBtn.right+15, (self.height-24)*0.5, 24, 24)];
    [moreBtn setImage:[UIImage sceneImageWithName:@"ktv_moreItem_icon"] forState:UIControlStateNormal];
    moreBtn.tag = VLKTVBottomBtnClickTypeMore;
    [moreBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:moreBtn];
    
    VLHotSpotBtn *dianGeBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(self.width-20-70, (self.height-32)*0.5, 70, 32)];
    [dianGeBtn setImage:[UIImage sceneImageWithName:@"ktv_diange_icon"] forState:UIControlStateNormal];
    [dianGeBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    dianGeBtn.tag = VLKTVBottomBtnClickTypeChoose;
    [self addSubview:dianGeBtn];
    
    VLHotSpotBtn *heChangeBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(dianGeBtn.left-20-70, (self.height-32)*0.5, 70, 32)];
    heChangeBtn.tag = VLKTVBottomBtnClickTypeChorus;
    [heChangeBtn setImage:[UIImage sceneImageWithName:@"ktv_hechang_icon"] forState:UIControlStateNormal];
    [heChangeBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:heChangeBtn];
    heChangeBtn.enabled = NO;
    
    for (VLRoomSeatModel *info in self.seatsArray) {
        if ([info.rtcUid integerValue] == [VLUserCenter.user.id integerValue]) {
            //is self
            //TODO
            [self updateAudioBtn:info.isAudioMuted];
            [self updateVideoBtn:info.isVideoMuted];
            break;
        }
    }
}

- (void)bottomBtnClickEvent:(VLHotSpotBtn *)sender {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLKTVBottomView:btnTapped:withValues:)]) {
        [self.delegate onVLKTVBottomView:self btnTapped:sender withValues:sender.tag];
    }
}

- (void)updateAudioBtn:(BOOL)audioMuted
{
    self.isSelfMuted = audioMuted;
    [self.audioBtn setSelected:!audioMuted];
}

- (void)updateVideoBtn:(BOOL)videoMuted
{
    self.isVideoMuted = videoMuted;
    [self.videoBtn setSelected:!videoMuted];
}

- (void)resetBtnStatus
{
    [self updateAudioBtn:YES];
    [self updateVideoBtn:YES];
}

@end
