//
//  VLCreateRoomView.m
//  VoiceOnLine
//

#import "VLCreateRoomView.h"
#import "TXLimitedTextField.h"
#import "CRLineView.h"
#import "CRSecrectImageView.h"
#import "VLAddRoomModel.h"
#import "VLMacroDefine.h"
#import "VLToast.h"
#import "KTVMacro.h"
@import CRBoxInputView;
@import QMUIKit;
@import YYCategories;
@import IQKeyboardManager;

@interface VLCreateRoomView ()

@property(nonatomic, weak) id <VLCreateRoomViewDelegate>delegate;
@property (nonatomic, strong) TXLimitedTextField *inputTF;
@property (nonatomic, strong) UIView *screatView;
@property (nonatomic, strong) UIImageView *iconImgView;
@property(nonatomic, strong) CRBoxInputView *boxInputView;
@property (nonatomic, strong) QMUIButton *publicBtn;
@property (nonatomic, strong) QMUIButton *screatBtn;
@property (nonatomic, strong) VLAddRoomModel *addRoomModel;

@property (nonatomic, strong) NSArray *titlesArray;
@end

@implementation VLCreateRoomView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLCreateRoomViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        self.addRoomModel = [[VLAddRoomModel alloc]init];
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
    self.iconImgView.image = [UIImage imageNamed:[NSString stringWithFormat:@"icon_room_cover%@",self.addRoomModel.icon]];
    [self addSubview:iconImgView];
    
    UILabel *roomTitleLabel = [[UILabel alloc]initWithFrame:CGRectMake(40, iconImgView.bottom+VLREALVALUE_WIDTH(40), 70, 20)];
    roomTitleLabel.font = UIFontMake(14);
    roomTitleLabel.textColor = UIColorMakeWithHex(@"#000000");
    roomTitleLabel.text = @"房间标题";
    [self addSubview:roomTitleLabel];
    
    QMUIButton *randomBtn = [[QMUIButton alloc] qmui_initWithImage:[UIImage sceneImageWithName:@"online_create_randomIcon"]
                                                             title:KTVLocalizedString(@"随机")];
    randomBtn.frame = CGRectMake(SCREEN_WIDTH-50-50, roomTitleLabel.top, 50, 20);
    randomBtn.imagePosition = QMUIButtonImagePositionLeft;
    randomBtn.spacingBetweenImageAndTitle = 3;
    randomBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [randomBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    randomBtn.titleLabel.font = UIFontMake(14.0);
    randomBtn.adjustsButtonWhenHighlighted = NO;
    [randomBtn addTarget:self action:@selector(randomBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:randomBtn];
    
    UIView *inputBgView = [[UIView alloc] initWithFrame:CGRectMake(30, roomTitleLabel.bottom+15, SCREEN_WIDTH-60, 48)];
    inputBgView.layer.cornerRadius = 24;
    inputBgView.layer.masksToBounds = YES;
    inputBgView.backgroundColor = UIColorMakeWithHex(@"#FFFFFF");
    [self addSubview:inputBgView];

    self.inputTF = [[TXLimitedTextField alloc] initWithFrame:CGRectMake(30, 9, inputBgView.width-60, 30)];
    self.inputTF.textColor = UIColorMakeWithHex(@"#040925");
    self.inputTF.text = self.addRoomModel.name;
    self.inputTF.font = UIFontBoldMake(18);
    self.inputTF.clearButtonMode = UITextFieldViewModeWhileEditing;
    self.inputTF.tintColor = UIColorMakeWithHex(@"#345DFF");
    [self.inputTF addTarget:self action:@selector(textChangeAction:)forControlEvents:UIControlEventEditingChanged];
    [inputBgView addSubview:self.inputTF];
    
    UILabel *secretLabel = [[UILabel alloc]initWithFrame:CGRectMake(40, inputBgView.bottom+VLREALVALUE_WIDTH(30), 100, 20)];
    secretLabel.font = UIFontMake(14);
    secretLabel.textColor = UIColorMakeWithHex(@"#000000");
    secretLabel.text = KTVLocalizedString(@"房间是否加密");
    [self addSubview:secretLabel];
    
    QMUIButton *publicBtn = [[QMUIButton alloc] qmui_initWithImage:[UIImage sceneImageWithName:@"online_create_screatNormalIcon"]
                                                             title:KTVLocalizedString(@"公开")];
    publicBtn.frame = CGRectMake(secretLabel.left-3, secretLabel.bottom+13, 58, 24);
    publicBtn.imagePosition = QMUIButtonImagePositionLeft;
    publicBtn.spacingBetweenImageAndTitle = 3;
    publicBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [publicBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    publicBtn.titleLabel.font = UIFontMake(14.0);
    publicBtn.adjustsButtonWhenHighlighted = NO;
    [publicBtn setImage:[UIImage sceneImageWithName:@"online_create_screatNormalIcon"] forState:UIControlStateNormal];
    [publicBtn setImage:[UIImage sceneImageWithName:@"online_create_screatSelIcon"] forState:UIControlStateSelected];
    publicBtn.tag = 0;
    publicBtn.selected = YES;
    self.publicBtn = publicBtn;
    [publicBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:publicBtn];
    
    QMUIButton *screatBtn = [[QMUIButton alloc] qmui_initWithImage:[UIImage sceneImageWithName:@"online_create_screatNormalIcon"]
                                                             title:KTVLocalizedString(@"加密")];
    screatBtn.frame = CGRectMake(publicBtn.right+40, publicBtn.top, 58, 24);
    screatBtn.imagePosition = QMUIButtonImagePositionLeft;
    screatBtn.spacingBetweenImageAndTitle = 3;
    screatBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [screatBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    screatBtn.titleLabel.font = UIFontMake(14.0);
    screatBtn.adjustsButtonWhenHighlighted = NO;
    [screatBtn setImage:[UIImage sceneImageWithName:@"online_create_screatNormalIcon"] forState:UIControlStateNormal];
    [screatBtn setImage:[UIImage sceneImageWithName:@"online_create_screatSelIcon"] forState:UIControlStateSelected];
    screatBtn.tag = 1;
    self.screatBtn = screatBtn;
    [screatBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:screatBtn];
    
    
    self.screatView = [[UIView alloc]initWithFrame:CGRectMake(40, publicBtn.bottom+15, SCREEN_WIDTH-80, 48+12+17)];
    self.screatView.hidden = YES;
    [self addSubview:self.screatView];
    
    CRBoxInputCellProperty *cellProperty = [CRBoxInputCellProperty new];
    cellProperty.cellBgColorNormal = UIColorMakeWithHex(@"#FFFFFF");
    cellProperty.cellBgColorSelected = UIColorMakeWithHex(@"#FFFFFF");
    cellProperty.cellCursorColor = UIColorMakeWithHex(@"#009FFF");
    cellProperty.cellCursorWidth = 1;
    cellProperty.cellCursorHeight = 20;
    cellProperty.borderWidth = 0;
    cellProperty.cornerRadius = 8;
    cellProperty.cellFont = UIFontBoldMake(18);
    cellProperty.cellTextColor = UIColorMakeWithHex(@"#040925");

    self.boxInputView = [[CRBoxInputView alloc] initWithCodeLength:4];
    self.boxInputView.frame = CGRectMake(0, 0, SCREEN_WIDTH-80, 55);
    self.boxInputView.boxFlowLayout.itemSize = CGSizeMake(62, 48);
    self.boxInputView.customCellProperty = cellProperty;
    [self.boxInputView loadAndPrepareViewWithBeginEdit:NO];
    [self.screatView addSubview:self.boxInputView];
    self.boxInputView.textDidChangeblock = ^(NSString * _Nullable text, BOOL isFinished) {
        if (isFinished) {
            weakSelf.addRoomModel.password = text;
        }
    };
    
    IQKeyboardManager *iqMagager = [IQKeyboardManager sharedManager];
    iqMagager.keyboardDistanceFromTextField = 85;

    UILabel *setLabel = [[UILabel alloc]initWithFrame:CGRectMake(0, self.boxInputView.bottom+12, 150, 17)];
    setLabel.font = UIFontMake(12);
    setLabel.textColor = UIColorMakeWithHex(@"#FA396A");
    setLabel.text = KTVLocalizedString(@"请设置4位数房间密码");
    [self.screatView addSubview:setLabel];
    
    UIButton *createBtn = [[UIButton alloc] initWithFrame:CGRectMake(VLREALVALUE_WIDTH(30), SCREEN_HEIGHT-VLREALVALUE_WIDTH(25)-48-kTopNavHeight-kSafeAreaBottomHeight, SCREEN_WIDTH-2*VLREALVALUE_WIDTH(30), 48)];
    createBtn.layer.cornerRadius = 24;
    createBtn.layer.masksToBounds = YES;
    [createBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
    [createBtn setTitle:KTVLocalizedString(@"创建") forState:UIControlStateNormal];
    createBtn.titleLabel.font = UIFontBoldMake(16.0);
    createBtn.adjustsImageWhenHighlighted = NO;
    [createBtn addTarget:self action:@selector(createBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    createBtn.backgroundColor = UIColorMakeWithHex(@"#2753FF");
    [self addSubview:createBtn];
    
//    本应用为测试产品，请勿商用
    
    UILabel *topLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-160)*0.5+5, createBtn.top-20-30, 160, 17)];
    topLabel.font = UIFontMake(12);
    topLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    topLabel.text = KTVLocalizedString(@"本应用为测试产品，请勿商用");
    [self addSubview:topLabel];
    
    UIImageView *tipImgView = [[UIImageView alloc]initWithFrame:CGRectMake(topLabel.left-16, topLabel.centerY-7.5, 16, 15)];
    tipImgView.image = [UIImage sceneImageWithName:@"online_create_tipIcon"];
    [self addSubview:tipImgView];
    
    UILabel *bottomLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-240)*0.5, topLabel.bottom, 240, 17)];
    bottomLabel.font = UIFontMake(12);
    bottomLabel.textAlignment = NSTextAlignmentCenter;
    bottomLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    bottomLabel.text = KTVLocalizedString(@"单次K歌最长20分钟，每个房间最多8人");
    [self addSubview:bottomLabel];
    
}

#pragma mark - Event
- (void)randomBtnClickEvent {
    [self createRandomNumber];
    self.inputTF.text = self.addRoomModel.name;
    self.iconImgView.image = [UIImage imageNamed:[NSString stringWithFormat:@"icon_room_cover%@",self.addRoomModel.icon]];
}

- (void)createBtnClickEvent {
    if (!(self.inputTF.text.length > 0)) {
        [VLToast toast:KTVLocalizedString(@"请输入标题")];
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

- (void)itemBtnClickEvent:(QMUIButton *)sender {
    if (sender.tag == 0) {
//        self.publicBtn.selected = !self.publicBtn.selected;
        self.screatBtn.selected = NO;
        self.publicBtn.selected = YES;
        self.addRoomModel.isPrivate = NO;
    }else{
//        self.screatBtn.selected = !self.screatBtn.selected;
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
            KTVLocalizedString(@"和你一起看月亮"),
            KTVLocalizedString(@"治愈"),
            KTVLocalizedString(@"一锤定音"),
            KTVLocalizedString(@"有酒吗"),
            KTVLocalizedString(@"早安序曲"),
            KTVLocalizedString(@"风情万种的歌房"),
            KTVLocalizedString(@"近在远方"),
            KTVLocalizedString(@"风中诗"),
            KTVLocalizedString(@"那年风月"),
            KTVLocalizedString(@"那年风月"),
            KTVLocalizedString(@"三万余年"),
            KTVLocalizedString(@"七十二街"),
            KTVLocalizedString(@"情怀如诗"),
            KTVLocalizedString(@"简遇而安"),
            KTVLocalizedString(@"十里笙歌"),
            KTVLocalizedString(@"回风舞雪"),
            KTVLocalizedString(@"梦初醒处"),
            KTVLocalizedString(@"别来无恙"),
            KTVLocalizedString(@"三里清风"),
            KTVLocalizedString(@"烟雨万重"),
            KTVLocalizedString(@"水洗晴空"),
            KTVLocalizedString(@"轻风淡月"),
        ];
    }
    return _titlesArray;
}


@end
