//
//  VLKTVTopView.m
//  VoiceOnLine
//

#import "VLKTVTopView.h"
#import <SDWebImage/UIImageView+WebCache.h>
@import AgoraCommon;
@interface DHCVLKTVTopView ()

@property(nonatomic, weak) id <DHCVLKTVTopViewDelegate>delegate;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIButton *networkStatusBtn;
@property (nonatomic, strong) UILabel *countLabel;
@property (nonatomic, strong) UIImageView *logoImgView;
@end

@implementation DHCVLKTVTopView

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
    logoImgView.image = [UIImage dhc_sceneImageWith:@""];
    logoImgView.layer.cornerRadius = 17;
    logoImgView.layer.masksToBounds = true;
    [self addSubview:logoImgView];
    self.logoImgView = logoImgView;
    
    VLHotSpotBtn *closeBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(SCREEN_WIDTH-27-20, logoImgView.top + 7, 20, 20)];
    [closeBtn setImage:[UIImage dhc_sceneImageWith:@"dhc_close"] forState:UIControlStateNormal];
    [closeBtn addTarget:self action:@selector(closeBtnEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:closeBtn];
    
    UIButton *moreButton = [[UIButton alloc] init];
    [moreButton setImage:[UIImage dhc_sceneImageWith:@"icon_live_more"] forState:(UIControlStateNormal)];
    [moreButton addTarget:self action:@selector(moreBtnEvent:) forControlEvents:(UIControlEventTouchUpInside)];
    [self addSubview:moreButton];
    moreButton.translatesAutoresizingMaskIntoConstraints = NO;
    [[moreButton.trailingAnchor constraintEqualToAnchor:closeBtn.leadingAnchor constant:-15]setActive:YES];
    [[moreButton.centerYAnchor constraintEqualToAnchor:closeBtn.centerYAnchor]setActive:YES];
    [[moreButton.widthAnchor constraintEqualToConstant:24]setActive:YES];
    
    self.titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(logoImgView.right+10, 10, 120, 18)];
    self.titleLabel.font = UIFontBoldMake(14);
    self.titleLabel.textColor = UIColorWhite;
    [self addSubview:self.titleLabel];
    
    self.countLabel = [[UILabel alloc]initWithFrame:CGRectMake(logoImgView.right + 5, 30, 60, 12)];
    self.countLabel.font = UIFontMake(12);
    self.countLabel.textAlignment = NSTextAlignmentCenter;
    self.countLabel.textColor = UIColorMakeWithHex(@"#979CBB");
    [self addSubview:self.countLabel];
    
    // 创建右边边框的 CALayer
    CALayer *rightBorder = [CALayer layer];
    rightBorder.backgroundColor = UIColorMakeWithHex(@"#979CBB").CGColor;
    rightBorder.frame = CGRectMake(self.countLabel.frame.size.width - 1, 0, 1, self.countLabel.frame.size.height);

    // 将右边边框添加到 UILabel 的 layer 上
    [self.countLabel.layer addSublayer:rightBorder];
    
    self.networkStatusBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.networkStatusBtn.frame = CGRectMake(self.countLabel.right + 5, 30, 70, 12);
    [self.networkStatusBtn setTitle:DHCLocalizedString(@"ktv_net_status_good") forState:UIControlStateNormal];
    [self.networkStatusBtn setImage:[UIImage dhc_sceneImageWith:@"ktv_network_wellIcon"] forState:UIControlStateNormal];
  //  self.networkStatusBtn.imagePosition = QMUIButtonImagePositionLeft;
    self.networkStatusBtn.spacingBetweenImageAndTitle = 0;
    self.networkStatusBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
    [self.networkStatusBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
    self.networkStatusBtn.titleLabel.font = UIFontMake(12.0);
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

- (void)setListModel:(VLRoomListModel *)listModel {
    _listModel = listModel;
    self.titleLabel.text = listModel.name;
    NSString *roomCountPre = [@"ktv_room_count" toSceneLocalizationWith:@"DHCResource"];
    [self.logoImgView sd_setImageWithURL:listModel.creatorAvatar];
    if (listModel.roomPeopleNum) {
        NSString *roomCountString = [NSString stringWithFormat:@"%@%@",  listModel.roomPeopleNum, roomCountPre];
        self.countLabel.text = roomCountString;
       // self.countLabel.text = @"128在线  |";
    }else{
        NSString *roomCountString = [NSString stringWithFormat:@"%i%@",1, roomCountPre];
        self.countLabel.text = roomCountString;
    }
}

// TODO: icon for OK and bad.
- (void)setNetworkQuality:(int)quality
{
    if(quality == 0) {
        [self.networkStatusBtn setImage:[UIImage dhc_sceneImageWith:@"ktv_network_wellIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:DHCLocalizedString(@"ktv_net_status_good") forState:UIControlStateNormal];
    }
    else if (quality == 1) {
        [self.networkStatusBtn setImage:[UIImage dhc_sceneImageWith:@"ktv_network_okIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:DHCLocalizedString(@"ktv_net_status_m") forState:UIControlStateNormal];
    }
    else if(quality == 2) {
        [self.networkStatusBtn setImage:[UIImage dhc_sceneImageWith:@"ktv_network_badIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:DHCLocalizedString(@"ktv_net_status_low") forState:UIControlStateNormal];
    }
    else {
        [self.networkStatusBtn setImage:[UIImage dhc_sceneImageWith:@"ktv_network_wellIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:DHCLocalizedString(@"ktv_net_status_good") forState:UIControlStateNormal];
    }
}

@end
