//
//  VLKTVTopView.m
//  VoiceOnLine
//

#import "VLKTVTopView.h"
#import "VLHotSpotBtn.h"
#import "AgoraEntScenarios-Swift.h"
@import AgoraCommon;
@interface VLKTVTopView ()

@property(nonatomic, weak) id <VLKTVTopViewDelegate>delegate;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIButton *networkStatusBtn;
@property (nonatomic, strong) UILabel *countLabel;

@end

@implementation VLKTVTopView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVTopViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    UIImageView *logoImgView = [[UIImageView alloc]initWithFrame:CGRectMake(20, 10, 20, 20)];
    logoImgView.image = [UIImage sceneImageWithName:@"ktv_logo_icon"];
    [self addSubview:logoImgView];
    
    VLHotSpotBtn *closeBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(SCREEN_WIDTH-27-20, logoImgView.top, 20, 20)];
    [closeBtn setImage:[UIImage sceneImageWithName:@"ktv_close_icon"] forState:UIControlStateNormal];
    [closeBtn addTarget:self action:@selector(closeBtnEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:closeBtn];
    
    UIButton *moreButton = [[UIButton alloc] init];
    [moreButton setImage:[UIImage sceneImageWithName:@"icon_live_more" bundleName:@"VoiceChatRoomResource"] forState:(UIControlStateNormal)];
    [moreButton addTarget:self action:@selector(moreBtnEvent:) forControlEvents:(UIControlEventTouchUpInside)];
    [self addSubview:moreButton];
    moreButton.translatesAutoresizingMaskIntoConstraints = NO;
    [[moreButton.trailingAnchor constraintEqualToAnchor:closeBtn.leadingAnchor constant:-15]setActive:YES];
    [[moreButton.centerYAnchor constraintEqualToAnchor:closeBtn.centerYAnchor]setActive:YES];
    [[moreButton.widthAnchor constraintEqualToConstant:24]setActive:YES];
    
    self.titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(logoImgView.right+5, logoImgView.centerY-11, 120, 22)];
    self.titleLabel.font = UIFontBoldMake(16);
    self.titleLabel.textColor = UIColorWhite;
    [self addSubview:self.titleLabel];
    
//    self.networkStatusBtn = [[QMUIButton alloc] qmui_initWithImage:[UIImage sceneImageWithName:@"ktv_network_wellIcon"]
//                                                             title:KTVLocalizedString(@"本机网络好")];
    self.networkStatusBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.networkStatusBtn setTitle:KTVLocalizedString(@"ktv_net_status_good") forState:UIControlStateNormal];
    [self.networkStatusBtn setImage:[UIImage sceneImageWithName:@"ktv_network_wellIcon"] forState:UIControlStateNormal];
//    self.networkStatusBtn.frame = CGRectMake(closeBtn.left-15-75, closeBtn.top, 75, 20);
//    self.networkStatusBtn.imagePosition = QMUIButtonImagePositionLeft;
    self.networkStatusBtn.spacingBetweenImageAndTitle = 4;
    self.networkStatusBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
    [self.networkStatusBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
    self.networkStatusBtn.titleLabel.font = UIFontMake(10.0);
    [self addSubview:self.networkStatusBtn];
    self.networkStatusBtn.translatesAutoresizingMaskIntoConstraints = NO;
    [[self.networkStatusBtn.trailingAnchor constraintEqualToAnchor:moreButton.leadingAnchor constant:-10]setActive:YES];
    [[self.networkStatusBtn.centerYAnchor constraintEqualToAnchor:moreButton.centerYAnchor]setActive:YES];
    
    self.countLabel = [[UILabel alloc]initWithFrame:CGRectMake(logoImgView.left, logoImgView.bottom+10, 120, 14)];
    self.countLabel.font = UIFontMake(10);
    self.countLabel.textColor = UIColorMakeWithHex(@"#979CBB");
    [self addSubview:self.countLabel];
}

#pragma mark --Event
- (void)closeBtnEvent:(id)sender {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLKTVTopView:closeBtnTapped:)]) {
        [self.delegate onVLKTVTopView:self closeBtnTapped:sender];
    }
}

- (void)moreBtnEvent: (id)sender {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLKTVTopView:closeBtnTapped:)]) {
        [self.delegate onVLKTVTopView:self moreBtnTapped:sender];
    }
}

- (void)setListModel:(VLRoomListModel *)listModel {
    _listModel = listModel;
    self.titleLabel.text = listModel.name;
    NSString *roomCountPre = KTVLocalizedString(@"ktv_room_count");
    
    if (listModel.roomPeopleNum) {
        NSString *roomCountString = [NSString stringWithFormat:@"%@:%@", roomCountPre, listModel.roomPeopleNum];
        self.countLabel.text = roomCountString;
    }else{
        NSString *roomCountString = [NSString stringWithFormat:@"%@:%i", roomCountPre, 1];
        self.countLabel.text = roomCountString;
    }
}

// TODO: icon for OK and bad.
- (void)setNetworkQuality:(int)quality
{
    if(quality == 0) {
        [self.networkStatusBtn setImage:[UIImage sceneImageWithName:@"ktv_network_wellIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"ktv_net_status_good") forState:UIControlStateNormal];
    }
    else if (quality == 1) {
        [self.networkStatusBtn setImage:[UIImage sceneImageWithName:@"ktv_network_okIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"ktv_net_status_m") forState:UIControlStateNormal];
    }
    else if(quality == 2) {
        [self.networkStatusBtn setImage:[UIImage sceneImageWithName:@"ktv_network_badIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"ktv_net_status_low") forState:UIControlStateNormal];
    }
    else {
        [self.networkStatusBtn setImage:[UIImage sceneImageWithName:@"ktv_network_wellIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"ktv_net_status_good") forState:UIControlStateNormal];
    }
}

@end
