//
//  VLKTVBottomView.m
//  VoiceOnLine
//

#import "VLKTVBottomToolbar.h"
#import "VLHotSpotBtn.h"
#import "VLUserCenter.h"
#import "VLURLPathConfig.h"
#import "AgoraEntScenarios-swift.h"
#import "AppContext+KTV.h"
#import "VLMacroDefine.h"
#import "AESMacro.h"

typedef void (^actionSuccess)(BOOL ifSuccess);

@interface VLKTVBottomToolbar ()

@property(nonatomic, weak) id <VLKTVBottomToolbarDelegate>delegate;
@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, strong) NSArray <VLRoomSeatModel *> *seatsArray;
@property (nonatomic, assign) NSInteger isSelfMuted;
@property (nonatomic, assign) NSInteger isVideoMuted;
@property (nonatomic, strong)UIButton *audioBtn;
@property (nonatomic, strong)UIButton *videoBtn;

@end

@implementation VLKTVBottomToolbar

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVBottomToolbarDelegate>)delegate withRoomNo:(NSString *)roomNo withData:(NSArray <VLRoomSeatModel *> *)seatsArray{
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        self.roomNo = roomNo;
        self.seatsArray = seatsArray;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    self.audioBtn = [[UIButton alloc]initWithFrame:CGRectMake(12, (self.height-38)*0.5, 38, 38)];
    [self.audioBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_mic_mute" ] forState:UIControlStateNormal];
    [self.audioBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_mic_unmute" ] forState:UIControlStateSelected];
    self.audioBtn.tag = VLKTVBottomBtnClickTypeAudio;
    self.audioBtn.accessibilityIdentifier = @"ktv_bottom_bar_mic_mute_button";
    [self.audioBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.audioBtn];
    
    self.videoBtn = [[UIButton alloc]initWithFrame:CGRectMake(self.audioBtn.right+8, (self.height-38)*0.5, 38, 38)];
    [self.videoBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_cam_mute" ] forState:UIControlStateNormal];
    [self.videoBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_cam_unmute" ] forState:UIControlStateSelected];
    self.videoBtn.tag = VLKTVBottomBtnClickTypeVideo;
    self.videoBtn.accessibilityIdentifier = @"ktv_bottom_bar_video_mute_button";
    [self.videoBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.videoBtn];
    
    UIButton *moreBtn = [[UIButton alloc]initWithFrame:CGRectMake(self.videoBtn.right+8, (self.height-38)*0.5, 38, 38)];
    [moreBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_more" ] forState:UIControlStateNormal];
    moreBtn.tag = VLKTVBottomBtnClickTypeMore;
    moreBtn.accessibilityIdentifier = @"ktv_bottom_bar_setting_button";
    [moreBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:moreBtn];
   // moreBtn.alpha = 0;
    
    UIButton *dianGeBtn = [[UIButton alloc]initWithFrame:CGRectMake(self.width-20-70, (self.height-32)*0.5, 70, 32)];
    [dianGeBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_diange_icon" ] forState:UIControlStateNormal];
    [dianGeBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    dianGeBtn.tag = VLKTVBottomBtnClickTypeChoose;
    dianGeBtn.accessibilityIdentifier = @"ktv_bottom_bar_diange_button";
    [self addSubview:dianGeBtn];
    
    for (VLRoomSeatModel *info in self.seatsArray) {
        if ([info.owner.userId integerValue] == [VLUserCenter.user.id integerValue]) {
            //is self
            //TODO
            self.isSelfMuted = info.isAudioMuted;
            self.isVideoMuted = info.isVideoMuted;
            
            self.audioBtn.selected = !self.isSelfMuted;
            self.videoBtn.selected = !self.isVideoMuted;

//            if (!info.isAudioMuted) {
//                [self.audioBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_mic_unmute"] forState:UIControlStateNormal];
//            }
//            else{
//                [self.audioBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_mic_mute"] forState:UIControlStateNormal];
//            }
//            if (!info.isVideoMuted) {
//                [self.videoBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_cam_unmute"] forState:UIControlStateNormal];
//            }
//            else{
//                [self.videoBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_cam_mute"] forState:UIControlStateNormal];
//            }
//            if (self.delegate && [self.delegate respondsToSelector:@selector(bottomSetAudioMute:)]) {
//                [self.delegate bottomSetAudioMute:info.isSelfMuted];
//            }
//            if (self.delegate && [self.delegate respondsToSelector:@selector(bottomSetVideoMute:)]) {
//                [self.delegate bottomSetVideoMute:info.isVideoMuted];
//            }
            break;
        }
    }
}

- (void)bottomBtnClickEvent:(VLHotSpotBtn *)sender {
//    VL(weakSelf);
//    if (sender.tag == VLKTVBottomBtnClickTypeAudio) {
//        [[AppContext ktvServiceImp] muteWithMuteStatus:self.isSelfMuted == 1 ? NO : YES
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
//                [weakSelf.audioBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
//            }
//            else{
//                [weakSelf.audioBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_self_muteIcon"] forState:UIControlStateNormal];
//            }
//        }];
//    }else if (sender.tag == VLKTVBottomBtnClickTypeVideo){
//        [[AppContext ktvServiceImp] openVideoStatusWithStatus:self.isVideoMuted == 0 ? YES : NO
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
//                [weakSelf.videoBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_video_icon"] forState:UIControlStateNormal];
//            }
//            else{
//                [weakSelf.videoBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
//            }
//        }];
//    }else{
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLKTVBottomView:btnTapped:withValues:)]) {
        [self.delegate onVLKTVBottomView:self btnTapped:sender withValues:sender.tag];
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
//        [self.audioBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
//    }
//    else{
//        [self.audioBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_self_muteIcon"] forState:UIControlStateNormal];
//    }
}

- (void)updateVideoBtn:(BOOL)videoMuted
{
    self.isVideoMuted = videoMuted;
    self.videoBtn.selected = !videoMuted;
//    if (!videoMuted) {
//        [self.videoBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_video_icon"] forState:UIControlStateNormal];
//    }
//    else{
//        [self.videoBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
//    }
}

- (void)resetBtnStatus
{
    self.isSelfMuted = NO;
    self.isVideoMuted = YES;
    self.audioBtn.selected = NO;
    self.videoBtn.selected = NO;
//    [self.audioBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
//    [self.videoBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
}

@end
