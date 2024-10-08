//
//  VLSBGBottomView.m
//  VoiceOnLine
//

#import "VLSBGBottomToolbar.h"
#import "VLHotSpotBtn.h"
#import "VLUserCenter.h"
#import "VLURLPathConfig.h"
#import "AgoraEntScenarios-Swift.h"
#import "AppContext+SBG.h"
#import "VLMacroDefine.h"
#import "SBGMacro.h"

typedef void (^actionSuccess)(BOOL ifSuccess);

@interface VLSBGBottomToolbar ()

@property(nonatomic, weak) id <VLSBGBottomToolbarDelegate>delegate;
@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, strong) NSArray <VLSBGRoomSeatModel *> *seatsArray;
@property (nonatomic, assign) NSInteger isSelfMuted;
@property (nonatomic, assign) NSInteger isVideoMuted;
@property (nonatomic, strong)VLHotSpotBtn *audioBtn;
@property (nonatomic, strong)VLHotSpotBtn *videoBtn;

@end

@implementation VLSBGBottomToolbar

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGBottomToolbarDelegate>)delegate withRoomNo:(NSString *)roomNo withData:(NSArray <VLSBGRoomSeatModel *> *)seatsArray{
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        self.roomNo = roomNo;
        self.seatsArray = seatsArray;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    self.audioBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(12, (self.height-38)*0.5, 38, 38)];
    [self.audioBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_mic_mute"] forState:UIControlStateNormal];
    [self.audioBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_mic_unmute"] forState:UIControlStateSelected];
    self.audioBtn.tag = VLSBGBottomBtnClickTypeAudio;
    [self.audioBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.audioBtn];
    
    self.videoBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(self.audioBtn.right+8, (self.height-38)*0.5, 38, 38)];
    [self.videoBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_cam_mute"] forState:UIControlStateNormal];
    [self.videoBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_cam_unmute"] forState:UIControlStateSelected];
    self.videoBtn.tag = VLSBGBottomBtnClickTypeVideo;
    [self.videoBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.videoBtn];
    
    VLHotSpotBtn *moreBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(self.videoBtn.right+15, (self.height-24)*0.5, 24, 24)];
    [moreBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_moreItem_icon"] forState:UIControlStateNormal];
    moreBtn.tag = VLSBGBottomBtnClickTypeMore;
    [moreBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:moreBtn];
    moreBtn.hidden = true;
   // moreBtn.alpha = 0;
    
    VLHotSpotBtn *dianGeBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(self.width-20-70, (self.height-32)*0.5, 70, 32)];
    [dianGeBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_diange_icon"] forState:UIControlStateNormal];
    [dianGeBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    dianGeBtn.tag = VLSBGBottomBtnClickTypeChoose;
    [self addSubview:dianGeBtn];
    dianGeBtn.hidden = true;
    
    VLHotSpotBtn *voiceShowBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(moreBtn.right + 14 , (self.height-24)*0.5, 24, 24)];
    [voiceShowBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_moreItem_icon"] forState:UIControlStateNormal];
    [voiceShowBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    voiceShowBtn.tag = VLSBGBottomBtnClickTypeShowVoice;
    [self addSubview:voiceShowBtn];
    voiceShowBtn.hidden = YES;
    
    for (VLSBGRoomSeatModel *info in self.seatsArray) {
        if ([info.rtcUid integerValue] == [VLUserCenter.user.id integerValue]) {
            //is self
            //TODO
            self.isSelfMuted = info.isAudioMuted;
            self.isVideoMuted = info.isVideoMuted;
            
            self.audioBtn.selected = !self.isSelfMuted;
            self.videoBtn.selected = !self.isVideoMuted;

//            if (!info.isAudioMuted) {
//                [self.audioBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
//            }
//            else{
//                [self.audioBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_self_muteIcon"] forState:UIControlStateNormal];
//            }
//            if (!info.isVideoMuted) {
//                [self.videoBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_video_icon"] forState:UIControlStateNormal];
//            }
//            else{
//                [self.videoBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
//            }
//            if (self.delegate && [self.delegate respondsToSelector:@selector(bottomSetAudioMute:)]) {
//                [self.delegate bottomSetAudioMute:info.isSelfMuted];
//            }
//            if (self.delegate && [self.delegate respondsToSelector:@selector(bottomSetVideoMute:)]) {
//                [self.delegate bottomSetVideoMute:info.isVideoMuted];
//            }
//            if(info.seatIndex == 0){
//                voiceShowBtn.hidden = NO;
//            }
            break;
        }
    }
}

-(void)setAudioBtnEnabled:(BOOL)enabled {
    _audioBtn.userInteractionEnabled = enabled;
}

- (void)bottomBtnClickEvent:(VLHotSpotBtn *)sender {
//    VL(weakSelf);
//    if (sender.tag == VLSBGBottomBtnClickTypeAudio) {
//        [[AppContext RSServiceImp] muteWithMuteStatus:self.isSelfMuted == 1 ? NO : YES
//                                            completion:^(NSError * error) {
//            if (error != nil) {
//                return;
//            }
//
//            if (weakSelf.isSelfMuted == 1) {
//                weakSelf.isSelfMuted = 0;
//            }
//            else{
//                weakSelf.isSelfMuted = 1;
//            }
//            if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(bottomAudionBtnAction:)]) {
//                [weakSelf.delegate bottomAudionBtnAction:weakSelf.isSelfMuted];
//            }
//            if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(bottomSetAudioMute:)]) {
//                [weakSelf.delegate bottomSetAudioMute:weakSelf.isSelfMuted];
//            }
//
//            if (weakSelf.isSelfMuted == 0){
//                [weakSelf.audioBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
//            }
//            else{
//                [weakSelf.audioBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_self_muteIcon"] forState:UIControlStateNormal];
//            }
//        }];
//    }else if (sender.tag == VLSBGBottomBtnClickTypeVideo){
//        [[AppContext RSServiceImp] openVideoStatusWithStatus:self.isVideoMuted == 0 ? YES : NO
//                                                   completion:^(NSError * error) {
//            if (error != nil) {
//                return;
//            }
//
//            if (weakSelf.isVideoMuted == 1) {
//                weakSelf.isVideoMuted = 0;
//            }
//            else{
//                weakSelf.isVideoMuted = 1;
//            }
//            if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(bottomSetVideoMute:)]) {
//                [weakSelf.delegate bottomSetVideoMute:self.isVideoMuted];
//            }
//            if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(bottomVideoBtnAction:)]) {
//                [weakSelf.delegate bottomVideoBtnAction:self.isVideoMuted];
//            }
//            if (weakSelf.isVideoMuted == 1) {
//                [weakSelf.videoBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_video_icon"] forState:UIControlStateNormal];
//            }
//            else{
//                [weakSelf.videoBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
//            }
//        }];
//    }else{
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLSBGBottomView:btnTapped:withValues:)]) {
        [self.delegate onVLSBGBottomView:self btnTapped:sender withValues:sender.tag];
    }
//    }
    
}

//- (bool)isAudioMute
//{
//    return (self.isSelfMuted == 1 ? YES : NO);
//}

- (void)updateAudioBtn:(BOOL)audioMuted
{
    self.isSelfMuted = audioMuted;
    self.audioBtn.selected = !audioMuted;
//    if (!audioMuted) {
//        [self.audioBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
//    }
//    else{
//        [self.audioBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_self_muteIcon"] forState:UIControlStateNormal];
//    }
}

- (void)updateVideoBtn:(BOOL)videoMuted
{
    self.isVideoMuted = videoMuted;
    self.videoBtn.selected = !videoMuted;
//    if (!videoMuted) {
//        [self.videoBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_video_icon"] forState:UIControlStateNormal];
//    }
//    else{
//        [self.videoBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
//    }
}

- (void)resetBtnStatus
{
    self.isSelfMuted = NO;
    self.isVideoMuted = YES;
    self.audioBtn.selected = NO;
    self.videoBtn.selected = NO;
//    [self.audioBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
//    [self.videoBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
}


@end
