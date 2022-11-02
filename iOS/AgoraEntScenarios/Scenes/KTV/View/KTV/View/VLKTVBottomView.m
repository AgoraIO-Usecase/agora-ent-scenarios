//
//  VLKTVBottomView.m
//  VoiceOnLine
//

#import "VLKTVBottomView.h"
#import "VLHotSpotBtn.h"
#import "VLUserCenter.h"
#import "VLAPIRequest.h"
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
    [self.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
    [self.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateSelected];
    self.audioBtn.tag = VLKTVBottomBtnClickTypeAudio;
    [self.audioBtn addTarget:self action:@selector(bottomBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.audioBtn];
    
    self.videoBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(self.audioBtn.right+15, (self.height-24)*0.5, 24, 24)];
    [self.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
    [self.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateSelected];
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
    
    for (VLRoomSeatModel *info in self.seatsArray) {
        if ([info.id integerValue] == [VLUserCenter.user.id integerValue]) {
            self.isSelfMuted = info.isSelfMuted;
            self.isVideoMuted = info.isVideoMuted;

            if (info.isSelfMuted == 0) {
                [self.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
            }
            else{
                [self.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_self_muteIcon"] forState:UIControlStateNormal];
            }
            if (info.isVideoMuted == 1) {
                [self.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_icon"] forState:UIControlStateNormal];
            }
            else{
                [self.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
            }
            if (self.delegate && [self.delegate respondsToSelector:@selector(bottomSetAudioMute:)]) {
                [self.delegate bottomSetAudioMute:info.isSelfMuted];
            }
            if (self.delegate && [self.delegate respondsToSelector:@selector(bottomSetVideoMute:)]) {
                [self.delegate bottomSetVideoMute:info.isVideoMuted];
            }
            break;
        }
    }
}

- (void)bottomBtnClickEvent:(VLHotSpotBtn *)sender {
    VL(weakSelf);
    if (sender.tag == VLKTVBottomBtnClickTypeAudio) {
        [[AppContext ktvServiceImp] muteWithMuteStatus:self.isSelfMuted == 1 ? NO : YES
                                            completion:^(NSError * error) {
            if (error != nil) {
                return;
            }
            
            if (weakSelf.isSelfMuted == 1) {
                weakSelf.isSelfMuted = 0;
            }
            else{
                weakSelf.isSelfMuted = 1;
            }
            if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(bottomAudionBtnAction:)]) {
                [weakSelf.delegate bottomAudionBtnAction:weakSelf.isSelfMuted];
            }
            if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(bottomSetAudioMute:)]) {
                [weakSelf.delegate bottomSetAudioMute:weakSelf.isSelfMuted];
            }

            if (weakSelf.isSelfMuted == 0){
                [weakSelf.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
            }
            else{
                [weakSelf.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_self_muteIcon"] forState:UIControlStateNormal];
            }
        }];
        
//        NSString *setStatus = @"";
//        if(self.isSelfMuted == 1) {
//            setStatus = @"0";
//        }
//        else {
//            setStatus = @"1";
//        }
//        NSDictionary *param = @{
//            @"roomNo": self.roomNo,
//            @"userNo": VLUserCenter.user.userNo,
//            @"setStatus": setStatus
//        };
//        [VLAPIRequest getRequestURL:kURLIfSetMute parameter:param showHUD:NO success:^(VLResponseDataModel * _Nonnull response) {
//            if (response.code == 0) {
//                if (self.isSelfMuted == 1) {
//                    self.isSelfMuted = 0;
//                }
//                else{
//                    self.isSelfMuted = 1;
//                }
//                if (self.delegate && [self.delegate respondsToSelector:@selector(bottomAudionBtnAction:)]) {
//                    [self.delegate bottomAudionBtnAction:self.isSelfMuted];
//                }
//                if (self.delegate && [self.delegate respondsToSelector:@selector(bottomSetAudioMute:)]) {
//                    [self.delegate bottomSetAudioMute:self.isSelfMuted];
//                }
//
//                if (self.isSelfMuted == 0){
//                    [self.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
//                }
//                else{
//                    [self.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_self_muteIcon"] forState:UIControlStateNormal];
//                }
//
//
//            }
//        } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
//
//        }];
    
    }else if (sender.tag == VLKTVBottomBtnClickTypeVideo){
        [[AppContext ktvServiceImp] openVideoStatusWithStatus:self.isVideoMuted == 0 ? YES : NO
                                                   completion:^(NSError * error) {
            if (error != nil) {
                return;
            }
            
            if (weakSelf.isVideoMuted == 1) {
                weakSelf.isVideoMuted = 0;
            }
            else{
                weakSelf.isVideoMuted = 1;
            }
            if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(bottomSetVideoMute:)]) {
                [weakSelf.delegate bottomSetVideoMute:self.isVideoMuted];
            }
            if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(bottomVideoBtnAction:)]) {
                [weakSelf.delegate bottomVideoBtnAction:self.isVideoMuted];
            }
            if (weakSelf.isVideoMuted == 1) {
                [weakSelf.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_icon"] forState:UIControlStateNormal];
            }
            else{
                [weakSelf.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
            }
        }];
        
//        NSString *setStatus = @"";
//        if(self.isVideoMuted == 1) {
//            setStatus = @"0";
//        }
//        else {
//            setStatus = @"1";
//        }
//        NSDictionary *param = @{
//            @"roomNo": self.roomNo,
//            @"userNo": VLUserCenter.user.userNo,
//            @"setStatus": setStatus
//        };
//        [VLAPIRequest getRequestURL:kURLIfOpenVido parameter:param showHUD:NO success:^(VLResponseDataModel * _Nonnull response) {
//            if (response.code == 0) {
//                if (self.isVideoMuted == 1) {
//                    self.isVideoMuted = 0;
//                }
//                else{
//                    self.isVideoMuted = 1;
//                }
//                if (self.delegate && [self.delegate respondsToSelector:@selector(bottomSetVideoMute:)]) {
//                    [self.delegate bottomSetVideoMute:self.isVideoMuted];
//                }
//                if (self.delegate && [self.delegate respondsToSelector:@selector(bottomVideoBtnAction:)]) {
//                    [self.delegate bottomVideoBtnAction:self.isVideoMuted];
//                }
//                if (self.isVideoMuted == 1) {
//                    [self.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_icon"] forState:UIControlStateNormal];
//                }
//                else{
//                    [self.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
//                }
//            }
//        } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
//            
//        }];
    }else{
        if (self.delegate && [self.delegate respondsToSelector:@selector(bottomBtnsClickAction: withSender:)]) {
            [self.delegate bottomBtnsClickAction:sender.tag withSender:sender];
        }
    }
    
}

- (bool)isAudioMute
{
    return (self.isSelfMuted == 1 ? YES : NO);
}

- (void)resetBtnStatus
{
    self.isSelfMuted = NO;
    self.isVideoMuted = YES;
    [self.audioBtn setImage:[UIImage sceneImageWithName:@"ktv_audio_icon"] forState:UIControlStateNormal];
    [self.videoBtn setImage:[UIImage sceneImageWithName:@"ktv_video_muteIcon"] forState:UIControlStateNormal];
}

@end
