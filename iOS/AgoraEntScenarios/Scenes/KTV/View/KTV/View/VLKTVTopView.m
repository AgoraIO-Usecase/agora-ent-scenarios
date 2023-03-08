//
//  VLKTVTopView.m
//  VoiceOnLine
//

#import "VLKTVTopView.h"
#import "VLRoomListModel.h"
#import "VLHotSpotBtn.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;

@interface VLKTVTopView ()

@property(nonatomic, weak) id <VLKTVTopViewDelegate>delegate;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) QMUIButton *networkStatusBtn;
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
    
    self.titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(logoImgView.right+5, logoImgView.centerY-11, 120, 22)];
    self.titleLabel.font = UIFontBoldMake(16);
    self.titleLabel.textColor = UIColorWhite;
    [self addSubview:self.titleLabel];
    
    self.networkStatusBtn = [[QMUIButton alloc] qmui_initWithImage:[UIImage sceneImageWithName:@"ktv_network_wellIcon"]
                                                             title:KTVLocalizedString(@"本机网络好")];
    self.networkStatusBtn.frame = CGRectMake(closeBtn.left-15-75, closeBtn.top, 75, 20);
    self.networkStatusBtn.imagePosition = QMUIButtonImagePositionLeft;
    self.networkStatusBtn.spacingBetweenImageAndTitle = 4;
    self.networkStatusBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
    [self.networkStatusBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
    self.networkStatusBtn.titleLabel.font = UIFontMake(10.0);
    [self addSubview:self.networkStatusBtn];
    
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

- (void)setListModel:(VLRoomListModel *)listModel {
    _listModel = listModel;
    self.titleLabel.text = listModel.name;
    if (listModel.roomPeopleNum) {
        self.countLabel.text = [NSString stringWithFormat:KTVLocalizedString(@"当前在线人数：%@"), listModel.roomPeopleNum];
    }else{
        self.countLabel.text = KTVLocalizedString(@"当前在线人数：1");
    }
}

// TODO: icon for OK and bad.
- (void)setNetworkQuality:(int)quality
{
    if(quality == 0) {
        [self.networkStatusBtn setImage:[UIImage sceneImageWithName:@"ktv_network_wellIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"本机网络好") forState:UIControlStateNormal];
    }
    else if (quality == 1) {
        [self.networkStatusBtn setImage:[UIImage sceneImageWithName:@"ktv_network_okIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"本机网络良") forState:UIControlStateNormal];
    }
    else if(quality == 2) {
        [self.networkStatusBtn setImage:[UIImage sceneImageWithName:@"ktv_network_badIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"本机网络差") forState:UIControlStateNormal];
    }
    else {
        [self.networkStatusBtn setImage:[UIImage sceneImageWithName:@"ktv_network_wellIcon"] forState:UIControlStateNormal];
        [self.networkStatusBtn setTitle:KTVLocalizedString(@"本机网络好") forState:UIControlStateNormal];
    }
}

@end
