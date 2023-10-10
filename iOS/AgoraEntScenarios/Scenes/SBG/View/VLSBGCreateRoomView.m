//
//  VLCreateRoomView.m
//  VoiceOnLine
//

#import "VLSBGCreateRoomView.h"
#import "VLSBGAddRoomModel.h"
#import "VLMacroDefine.h"
#import "VLToast.h"
#import "SBGMacro.h"
#import "MenuUtils.h"
#import "AgoraEntScenarios-Swift.h"

@interface VLSBGCreateRoomView ()

@property(nonatomic, weak) id <VLSBGCreateRoomViewDelegate>delegate;
@property (nonatomic, strong) UITextField *inputTF;
@property (nonatomic, strong) UIView *screatView;
@property (nonatomic, strong) UIImageView *iconImgView;
@property (nonatomic, strong) UIButton *publicBtn;
@property (nonatomic, strong) UIButton *screatBtn;
@property (nonatomic, strong) VLSBGAddRoomModel *addRoomModel;

@property (nonatomic, strong) NSArray *titlesArray;
@end

@implementation VLSBGCreateRoomView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGCreateRoomViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        self.addRoomModel = [[VLSBGAddRoomModel alloc]init];
        [self createRandomNumber];
        [self setupView];
    }
    return self;
}

- (void)setupView {
    VL(weakSelf);
    self.addRoomModel.isPrivate = NO;
    UIImageView *iconImgView = [[UIImageView alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-VLREALVALUE_WIDTH(104))*0.5, VLREALVALUE_WIDTH(50), VLREALVALUE_WIDTH(104), VLREALVALUE_WIDTH(104))];
    iconImgView.layer.cornerRadius = 20;
    iconImgView.layer.masksToBounds = YES;
    self.iconImgView = iconImgView;
    [self addSubview:iconImgView];
    
    UILabel *roomTitleLabel = [[UILabel alloc]initWithFrame:CGRectMake(40, iconImgView.bottom+VLREALVALUE_WIDTH(40), 70, 20)];
    roomTitleLabel.font = UIFontMake(14);
    roomTitleLabel.textColor = UIColorMakeWithHex(@"#000000");
    roomTitleLabel.text = AGLocalizedString(@"房间标题");
    [self addSubview:roomTitleLabel];
    
//    QMUIButton *randomBtn = [[QMUIButton alloc] qmui_initWithImage:[UIImage sceneImageWithName:@"online_create_randomIcon"]
//                                                             title:KTVLocalizedString(@"随机")];
    UIButton *randomBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [randomBtn setTitle:SBGLocalizedString(@"随机") forState:UIControlStateNormal];
    [randomBtn setImage:[UIImage sceneImageWithName:@"online_create_randomIcon"] forState:UIControlStateNormal];
//    randomBtn.imagePosition = QMUIButtonImagePositionLeft;
    randomBtn.spacingBetweenImageAndTitle = 3;
    randomBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [randomBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    randomBtn.titleLabel.font = UIFontMake(14.0);
//    randomBtn.adjustsButtonWhenHighlighted = NO;
    [randomBtn addTarget:self action:@selector(randomBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:randomBtn];
    [randomBtn sizeToFit];
    randomBtn.frame = CGRectMake(SCREEN_WIDTH - randomBtn.width - 50, roomTitleLabel.top, randomBtn.width, 20);
    
    UIView *inputBgView = [[UIView alloc] initWithFrame:CGRectMake(30, roomTitleLabel.bottom+15, SCREEN_WIDTH-60, 48)];
    inputBgView.layer.cornerRadius = 24;
    inputBgView.layer.masksToBounds = YES;
    inputBgView.backgroundColor = UIColorMakeWithHex(@"#FFFFFF");
    [self addSubview:inputBgView];

    self.inputTF = [[UITextField alloc] initWithFrame:CGRectMake(30, 9, inputBgView.width - 60, 30)];
    self.inputTF.textColor = UIColorMakeWithHex(@"#040925");
    self.inputTF.font = UIFontBoldMake(18);
    self.inputTF.clearButtonMode = UITextFieldViewModeWhileEditing;
    self.inputTF.tintColor = UIColorMakeWithHex(@"#345DFF");
    [self.inputTF addTarget:self action:@selector(textChangeAction:)forControlEvents:UIControlEventEditingChanged];
    [inputBgView addSubview:self.inputTF];
    
    UILabel *secretLabel = [[UILabel alloc]initWithFrame:CGRectMake(40, inputBgView.bottom+VLREALVALUE_WIDTH(30), 100, 20)];
    secretLabel.font = UIFontMake(14);
    secretLabel.textColor = UIColorMakeWithHex(@"#000000");
    secretLabel.text = SBGLocalizedString(@"房间是否加密");
    [secretLabel sizeToFit];
    [self addSubview:secretLabel];

    UIButton *publicBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [publicBtn setTitle:SBGLocalizedString(@"公开") forState:UIControlStateNormal];
    [publicBtn setImage:[UIImage sceneImageWithName:@"online_create_screatNormalIcon"] forState:UIControlStateNormal];
    publicBtn.frame = CGRectMake(secretLabel.left-3, secretLabel.bottom+13, 58, 24);
    publicBtn.spacingBetweenImageAndTitle = 3;
    publicBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [publicBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    publicBtn.titleLabel.font = UIFontMake(14.0);
    [publicBtn setImage:[UIImage sceneImageWithName:@"online_create_screatNormalIcon"] forState:UIControlStateNormal];
    [publicBtn setImage:[UIImage sceneImageWithName:@"online_create_screatSelIcon"] forState:UIControlStateSelected];
    publicBtn.tag = 0;
    publicBtn.selected = YES;
    self.publicBtn = publicBtn;
    [publicBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:publicBtn];
    [publicBtn sizeToFit];
    
    UIButton *screatBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [screatBtn setTitle:SBGLocalizedString(@"加密") forState:UIControlStateNormal];
    [screatBtn setImage:[UIImage sceneImageWithName:@"online_create_screatNormalIcon"] forState:UIControlStateNormal];
    screatBtn.frame = CGRectMake(publicBtn.right+40, publicBtn.top, 58, 24);
    screatBtn.spacingBetweenImageAndTitle = 3;
    screatBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [screatBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    screatBtn.titleLabel.font = UIFontMake(14.0);
    [screatBtn setImage:[UIImage sceneImageWithName:@"online_create_screatNormalIcon"] forState:UIControlStateNormal];
    [screatBtn setImage:[UIImage sceneImageWithName:@"online_create_screatSelIcon"] forState:UIControlStateSelected];
    screatBtn.tag = 1;
    self.screatBtn = screatBtn;
    [screatBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:screatBtn];
    [screatBtn sizeToFit];
    
    
    self.screatView = [[UIView alloc]initWithFrame:CGRectMake(40, publicBtn.bottom+15, SCREEN_WIDTH-80, 48+12+17)];
    self.screatView.hidden = YES;
    [self addSubview:self.screatView];

    VerifyCodeView *pwdView = [[VerifyCodeView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH-80, 55) codeNumbers:4 space:10 padding:10];
    pwdView.inputFinish = ^(NSString * _Nonnull pwd) {
        weakSelf.addRoomModel.password = pwd;
    };
    [self.screatView addSubview:pwdView];
    
//    IQKeyboardManager *iqMagager = [IQKeyboardManager sharedManager];
//    iqMagager.keyboardDistanceFromTextField = 85;

    UILabel *setLabel = [[UILabel alloc]initWithFrame:CGRectMake(0, 55+12, 150, 17)];
    setLabel.font = UIFontMake(12);
    setLabel.textColor = UIColorMakeWithHex(@"#FA396A");
    setLabel.text = SBGLocalizedString(@"请设置4位数房间密码");
    [setLabel sizeToFit];
    [self.screatView addSubview:setLabel];
    
    UIButton *createBtn = [[UIButton alloc] initWithFrame:CGRectMake(VLREALVALUE_WIDTH(30), SCREEN_HEIGHT-VLREALVALUE_WIDTH(25)-48-kTopNavHeight-kSafeAreaBottomHeight, SCREEN_WIDTH-2*VLREALVALUE_WIDTH(30), 48)];
    createBtn.layer.cornerRadius = 24;
    createBtn.layer.masksToBounds = YES;
    [createBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
    [createBtn setTitle:SBGLocalizedString(@"创建") forState:UIControlStateNormal];
    createBtn.titleLabel.font = UIFontBoldMake(16.0);
    createBtn.adjustsImageWhenHighlighted = NO;
    [createBtn addTarget:self action:@selector(createBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    createBtn.backgroundColor = UIColorMakeWithHex(@"#2753FF");
    [self addSubview:createBtn];
    
//    本应用为测试产品，请勿商用
    
    UILabel *topLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-160)*0.5+5, createBtn.top-20-30, 160, 17)];
    topLabel.font = UIFontMake(12);
    topLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    topLabel.text = SBGLocalizedString(@"本应用为测试产品，请勿商用");
    [self addSubview:topLabel];
    
    UIImageView *tipImgView = [[UIImageView alloc]initWithFrame:CGRectMake(topLabel.left-16, topLabel.centerY-7.5, 16, 15)];
    tipImgView.image = [UIImage sceneImageWithName:@"online_create_tipIcon"];
    [self addSubview:tipImgView];
    
    UILabel *bottomLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-240)*0.5, topLabel.bottom, 240, 17)];
    bottomLabel.font = UIFontMake(12);
    bottomLabel.textAlignment = NSTextAlignmentCenter;
    bottomLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    bottomLabel.text = SBGLocalizedString(@"单次嗨歌最长20分钟，每个房间最多8人");
    [self addSubview:bottomLabel];
    
    [self randomBtnClickEvent];
}

#pragma mark - Event
- (void)randomBtnClickEvent {
    [self createRandomNumber];
    self.inputTF.text = self.addRoomModel.name;
    NSString* iconName = [NSString stringWithFormat:@"icon_room_cover%@.jpg",self.addRoomModel.icon];
    self.iconImgView.image = [UIImage sceneImageWithName: iconName];
}

- (void)createBtnClickEvent {
    if (!(self.inputTF.text.length > 0)) {
        [VLToast toast:SBGLocalizedString(@"请输入标题")];
    }
    self.addRoomModel.name = self.inputTF.text;
    if (self.delegate && [self.delegate respondsToSelector:@selector(createBtnAction:)]) {
        [self.delegate createBtnAction:self.addRoomModel];
    }
}

- (void)createRandomNumber {
    int titleValue = arc4random() % [self.titlesArray count]; //0...5的随机数
    int bgValue = (arc4random() % 9) + 1; //1...9的随机数
    self.addRoomModel.name = self.titlesArray[titleValue];
    self.addRoomModel.icon = [NSString stringWithFormat:@"%d",bgValue];
}

- (void)itemBtnClickEvent:(UIButton *)sender {
    if (sender.tag == 0) {
        self.screatBtn.selected = NO;
        self.publicBtn.selected = YES;
        self.addRoomModel.isPrivate = NO;
    }else{
        self.publicBtn.selected = NO;
        self.screatBtn.selected = YES;
        self.addRoomModel.isPrivate = YES;
    }
    self.screatView.hidden = !self.screatBtn.selected;
}

- (void)textChangeAction:(UITextField *)sender {
    
}

- (NSArray *)titlesArray {
    if (!_titlesArray) {
        _titlesArray = @[
            SBGLocalizedString(@"和你一起看月亮"),
            SBGLocalizedString(@"治愈"),
            SBGLocalizedString(@"一锤定音"),
            SBGLocalizedString(@"有酒吗"),
            SBGLocalizedString(@"早安序曲"),
            SBGLocalizedString(@"风情万种的歌房"),
            SBGLocalizedString(@"近在远方"),
            SBGLocalizedString(@"风中诗"),
            SBGLocalizedString(@"那年风月"),
            SBGLocalizedString(@"那年风月"),
            SBGLocalizedString(@"三万余年"),
            SBGLocalizedString(@"七十二街"),
            SBGLocalizedString(@"情怀如诗"),
            SBGLocalizedString(@"简遇而安"),
            SBGLocalizedString(@"十里笙歌"),
            SBGLocalizedString(@"回风舞雪"),
            SBGLocalizedString(@"梦初醒处"),
            SBGLocalizedString(@"别来无恙"),
            SBGLocalizedString(@"三里清风"),
            SBGLocalizedString(@"烟雨万重"),
            SBGLocalizedString(@"水洗晴空"),
            SBGLocalizedString(@"轻风淡月"),
        ];
    }
    return _titlesArray;
}


@end
