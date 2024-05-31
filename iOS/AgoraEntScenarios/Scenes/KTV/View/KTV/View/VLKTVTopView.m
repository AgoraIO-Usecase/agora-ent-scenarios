//
//  VLKTVTopView.m
//  VoiceOnLine
//

#import "VLKTVTopView.h"
#import "VLHotSpotBtn.h"
#import "AESMacro.h"
#import "AgoraEntScenarios-Swift.h"
#import <SDWebImage/UIImageView+WebCache.h>
@interface VLKTVTopView ()

@property(nonatomic, weak) id <VLKTVTopViewDelegate>delegate;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIButton *networkStatusBtn;
@property (nonatomic, strong) UILabel *countLabel;
@property (nonatomic, strong) UIImageView *logoImgView;
@end

@implementation VLKTVTopView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    UIView *backView = [[UIView alloc]initWithFrame:CGRectMake(20, 10, 191, 34)];
    backView.backgroundColor = [UIColor colorWithRed:8/255.0 green:6/255.0 blue:47/255.0 alpha:0.3];
    backView.layer.cornerRadius = 17;
    backView.layer.masksToBounds = true;
    [self addSubview:backView];
    
    UIImageView *logoImgView = [[UIImageView alloc]initWithFrame:CGRectMake(20, 10, 34, 34)];
    logoImgView.image = [UIImage ktv_sceneImageWithName:@"" ];
    logoImgView.layer.cornerRadius = 17;
    logoImgView.layer.masksToBounds = true;
    logoImgView.contentMode = UIViewContentModeScaleAspectFill;
    [self addSubview:logoImgView];
    self.logoImgView = logoImgView;
    
    VLHotSpotBtn *closeBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(UIScreen.mainScreen.bounds.size.width-27-20, 10 + 7, 20, 20)];
    [closeBtn setImage:[UIImage ktv_sceneImageWithName:@"close_room" ] forState:UIControlStateNormal];
    [closeBtn addTarget:self action:@selector(closeBtnEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:closeBtn];
    
    UIButton *moreButton = [[UIButton alloc] init];
    [moreButton setImage:[UIImage ktv_sceneImageWithName:@"icon_live_more" ] forState:(UIControlStateNormal)];
    [moreButton addTarget:self action:@selector(moreBtnEvent:) forControlEvents:(UIControlEventTouchUpInside)];
    [self addSubview:moreButton];
    moreButton.translatesAutoresizingMaskIntoConstraints = NO;
    [[moreButton.trailingAnchor constraintEqualToAnchor:closeBtn.leadingAnchor constant:-15]setActive:YES];
    [[moreButton.centerYAnchor constraintEqualToAnchor:closeBtn.centerYAnchor]setActive:YES];
    [[moreButton.widthAnchor constraintEqualToConstant:24]setActive:YES];
    
    self.titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(54+10, 10, 100, 18)];
    self.titleLabel.font = [UIFont systemFontOfSize:14];
    self.titleLabel.textColor = [UIColor whiteColor];
    [self addSubview:self.titleLabel];

    self.countLabel = [[UILabel alloc]initWithFrame:CGRectMake(54 + 5, 30, 50, 12)];
    self.countLabel.font = [UIFont systemFontOfSize:12];
    self.countLabel.textAlignment = NSTextAlignmentRight;
    self.countLabel.textColor = [UIColor whiteColor];
    [self addSubview:self.countLabel];
    
    self.networkStatusBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.networkStatusBtn.frame = CGRectMake(110 + 5, 30, 70, 12);
    [self.networkStatusBtn setTitle:KTVLocalizedString(@"ktv_net_status_good") forState:UIControlStateNormal];
    [self.networkStatusBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_network_wellIcon" ] forState:UIControlStateNormal];
    self.networkStatusBtn.spacingBetweenImageAndTitle = 0;
    self.networkStatusBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
    [self.networkStatusBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.networkStatusBtn.titleLabel.font = [UIFont systemFontOfSize:12];
    [self addSubview:self.networkStatusBtn];
    
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

- (void)setListModel:(SyncRoomInfo *)listModel {
    _listModel = listModel;
    self.titleLabel.text = listModel.name;
    NSString *roomCountPre = KTVLocalizedString(@"ktv_room_count");
    self.logoImgView.image = [UIImage imageNamed:listModel.creatorAvatar];
    if (listModel.roomPeopleNum) {
        NSString *roomCountString = [NSString stringWithFormat:@"%ld%@  |", listModel.roomPeopleNum, roomCountPre];
        self.countLabel.text = roomCountString;
    }else{
        NSString *roomCountString = [NSString stringWithFormat:@"%i%@  |",1, roomCountPre];
        self.countLabel.text = roomCountString;
    }
}

// TODO: icon for OK and bad.
- (void)setNetworkQuality:(int)quality
{
    if(quality == 0) {
        [self.networkStatusBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_network_wellIcon" ] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"ktv_net_status_good") forState:UIControlStateNormal];
    }
    else if (quality == 1) {
        [self.networkStatusBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_network_okIcon" ] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"ktv_net_status_m") forState:UIControlStateNormal];
    }
    else if(quality == 2) {
        [self.networkStatusBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_network_badIcon" ] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"ktv_net_status_low") forState:UIControlStateNormal];
    }
    else {
        [self.networkStatusBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_network_wellIcon" ] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"ktv_net_status_good") forState:UIControlStateNormal];
    }
}

@end
