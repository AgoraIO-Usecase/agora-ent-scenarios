//
//  VLCreateRoomView.m
//  VoiceOnLine
//

#import "VLCreateRoomView.h"
#import "VLAddRoomModel.h"
#import "VLMacroDefine.h"
#import <Masonry/Masonry.h>

@import AgoraCommon;
@interface VLCreateRoomView ()<UITextFieldDelegate>

@property(nonatomic, weak) id <VLCreateRoomViewDelegate>delegate;
@property (nonatomic, strong) UITextField *inputTF;
@property (nonatomic, strong) UIView *screatView;
@property (nonatomic, strong) UIImageView *iconImgView;
@property (nonatomic, strong) UIButton *publicBtn;
@property (nonatomic, strong) UIButton *screatBtn;
@property (nonatomic, strong) VLAddRoomModel *addRoomModel;
@property (nonatomic, strong) UIView *warningView;
@property (nonatomic, strong) UIButton *enBtn;
@property (nonatomic, strong) UILabel *setLabel;
@property (nonatomic, strong) NSArray *titlesArray;
@property (nonatomic, strong) VerifyCodeView *codeView;
@property (nonatomic, strong) UIButton *norBtn;
@property (nonatomic, strong) UIButton *volBtn;
@property (nonatomic, strong) UIButton *mixBtn;
@property (nonatomic, strong) UIButton *selBtn;
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
    self.addRoomModel.streamMode = StreamModeNormol;
    NSString *text = @"ktv_create_tips".toSceneLocalization;
    UIFont *font = UIFontMake(12);
    CGSize constraintSize = CGSizeMake(self.width - 78, CGFLOAT_MAX);
    NSDictionary *attributes = @{NSFontAttributeName: font};
    CGRect textRect = [text boundingRectWithSize:constraintSize
                                         options:NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading
                                      attributes:attributes
                                         context:nil];
    CGFloat textHeight = ceil(CGRectGetHeight(textRect));
    
    self.warningView = [[UIView alloc]initWithFrame:CGRectMake(20, 20, self.width - 40, textHeight + 10)];
    self.warningView.backgroundColor = UIColorMakeWithHex(@"#FA396A1A");
    self.warningView.layer.cornerRadius = 5;
    self.warningView.layer.masksToBounds = true;
    [self addSubview:self.warningView];
    
    UIImageView *warImgView = [[UIImageView alloc]initWithFrame:CGRectMake(10, 6, 16, 16)];
    warImgView.image = [UIImage dhc_sceneImageWith:@"zhuyi"];
    warImgView.contentMode = UIViewContentModeScaleAspectFit;
    [self.warningView addSubview:warImgView];
    
    UILabel *contentLabel = [[UILabel alloc]initWithFrame:CGRectMake(30, 5, self.warningView.width - 38, textHeight)];
    contentLabel.numberOfLines = 0;
    NSMutableAttributedString *attributedText = [[NSMutableAttributedString alloc] initWithString:text];
    [attributedText addAttribute:NSForegroundColorAttributeName value:[UIColor colorWithRed:48/255.0 green:53/255.0 blue:83/255.0 alpha:1] range:NSMakeRange(0, 77)];
    [attributedText addAttribute:NSForegroundColorAttributeName value:[UIColor colorWithRed:250/255.0 green:57/255.0 blue:106/255.0 alpha:1] range:NSMakeRange(77, 41)];
    contentLabel.font = UIFontMake(12);
    contentLabel.attributedText = attributedText;
    [self.warningView addSubview:contentLabel];
    
    UILabel *roomTitleLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, self.warningView.bottom+VLREALVALUE_WIDTH(20), 70, 20)];
    roomTitleLabel.font = UIFontMake(14);
    roomTitleLabel.textColor = UIColorMakeWithHex(@"#000000");
    roomTitleLabel.text = @"ktv_room_title".toSceneLocalization;
    [self addSubview:roomTitleLabel];

    UIButton *randomBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [randomBtn setTitle:@"ktv_random".toSceneLocalization forState:UIControlStateNormal];
    [randomBtn setImage:[UIImage dhc_sceneImageWith:@"online_create_randomIcon" ] forState:UIControlStateNormal];
    randomBtn.spacingBetweenImageAndTitle = 3;
    randomBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [randomBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    randomBtn.titleLabel.font = UIFontMake(14.0);
    [randomBtn addTarget:self action:@selector(randomBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:randomBtn];
    [randomBtn sizeToFit];
    randomBtn.frame = CGRectMake(SCREEN_WIDTH - randomBtn.width - 20, roomTitleLabel.top, randomBtn.width, 20);
    
    UIView *inputBgView = [[UIView alloc] initWithFrame:CGRectMake(20, roomTitleLabel.bottom+8, SCREEN_WIDTH-40, 48)];
    inputBgView.layer.cornerRadius = 8;
    inputBgView.layer.masksToBounds = YES;
    inputBgView.backgroundColor = UIColorMakeWithHex(@"#F5F8FF");
    [self addSubview:inputBgView];

    self.inputTF = [[UITextField alloc] initWithFrame:CGRectMake(12, 9, inputBgView.width - 24, 30)];
    self.inputTF.accessibilityIdentifier = @"ktv_create_room_textfield_id";
    self.inputTF.textColor = UIColorMakeWithHex(@"#040925");
    self.inputTF.font = UIFontBoldMake(15);
    self.inputTF.clearButtonMode = UITextFieldViewModeWhileEditing;
    self.inputTF.tintColor = UIColorMakeWithHex(@"#345DFF");
    self.inputTF.placeholder = @"ktv_room_placeHolder".toSceneLocalization;
    [self.inputTF addTarget:self action:@selector(textChangeAction:)forControlEvents:UIControlEventEditingChanged];
    [inputBgView addSubview:self.inputTF];
    self.inputTF.delegate = self;
    
    UILabel *streamLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, inputBgView.bottom+VLREALVALUE_WIDTH(14), 100, 20)];
    streamLabel.font = UIFontMake(14);
    streamLabel.textColor = UIColorMakeWithHex(@"#000000");
    streamLabel.text = @"ktv_stream_style".toSceneLocalization;
    [streamLabel sizeToFit];
    [self addSubview:streamLabel];
    
    //默认
    UIButton *norBtn = [[UIButton alloc]initWithFrame:CGRectMake(20, streamLabel.bottom + VLREALVALUE_WIDTH(10), 60, 20)];
    norBtn.titleLabel.font = UIFontMake(14.0);
    [norBtn setTitle:@"ktv_stream_normal".toSceneLocalization forState:UIControlStateNormal];
    [norBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    [norBtn setImage:[UIImage dhc_sceneImageWith:@"check-circle"] forState:UIControlStateNormal];
    [norBtn setImage:[UIImage dhc_sceneImageWith:@"check-circle-fill"] forState:UIControlStateSelected];
    // 调整文字和图片的边距，使其左对齐
    norBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    norBtn.tag = 100;
    norBtn.selected = true;
    [norBtn addTarget:self action:@selector(streamChange:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:norBtn];
    self.norBtn = norBtn;
    self.selBtn = norBtn;
    
    //齐声
    UIButton *volBtn = [[UIButton alloc]initWithFrame:CGRectMake(self.bounds.size.width / 2.0 - 30, streamLabel.bottom + VLREALVALUE_WIDTH(10), 60, 20)];
    volBtn.titleLabel.font = UIFontMake(14.0);
    [volBtn setTitle:@"ktv_stream_vol".toSceneLocalization forState:UIControlStateNormal];
    [volBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    [volBtn setImage:[UIImage dhc_sceneImageWith:@"check-circle"] forState:UIControlStateNormal];
    [volBtn setImage:[UIImage dhc_sceneImageWith:@"check-circle-fill"] forState:UIControlStateSelected];
    [volBtn addTarget:self action:@selector(streamChange:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:volBtn];
    volBtn.tag = 101;
    self.volBtn = volBtn;
    
    //混合
    UIButton *mixBtn = [[UIButton alloc]initWithFrame:CGRectMake(UIScreen.mainScreen.bounds.size.width - 100, streamLabel.bottom + VLREALVALUE_WIDTH(10), 60, 20)];
    [mixBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    mixBtn.titleLabel.font = UIFontMake(14.0);
    [mixBtn setImage:[UIImage dhc_sceneImageWith:@"check-circle"] forState:UIControlStateNormal];
    [mixBtn setImage:[UIImage dhc_sceneImageWith:@"check-circle-fill"] forState:UIControlStateSelected];
    mixBtn.contentHorizontalAlignment =UIControlContentHorizontalAlignmentRight;
    [mixBtn setTitle:@"ktv_stream_mix".toSceneLocalization forState:UIControlStateNormal];
    [mixBtn addTarget:self action:@selector(streamChange:) forControlEvents:UIControlEventTouchUpInside];
    mixBtn.tag = 102;
    [self addSubview:mixBtn];
    self.mixBtn = mixBtn;
    
    UILabel *secretLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, norBtn.bottom+VLREALVALUE_WIDTH(10), 100, 20)];
    secretLabel.font = UIFontMake(14);
    secretLabel.textColor = UIColorMakeWithHex(@"#000000");
    secretLabel.text = @"ktv_room_is_encryption".toSceneLocalization;
    [secretLabel sizeToFit];
    [self addSubview:secretLabel];
    
    self.enBtn = [[UIButton alloc]initWithFrame:CGRectMake(secretLabel.right + 8, norBtn.bottom+VLREALVALUE_WIDTH(10), 32, 20)];
    [self.enBtn setBackgroundImage:[UIImage dhc_sceneImageWith:@"guan" ] forState:UIControlStateNormal];
    [self.enBtn setBackgroundImage:[UIImage dhc_sceneImageWith:@"open" ] forState:UIControlStateSelected];
    [self.enBtn addTarget:self action:@selector(enChange:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.enBtn];
    
    self.setLabel = [[UILabel alloc]initWithFrame:CGRectMake(SCREEN_WIDTH - 170, norBtn.bottom+VLREALVALUE_WIDTH(10), 150, 17)];
    self.setLabel.font = UIFontMake(12);
    self.setLabel.textColor = UIColorMakeWithHex(@"#FA396A");
    self.setLabel.text = @"ktv_please_input_4_pwd".toSceneLocalization;
    self.setLabel.textAlignment = NSTextAlignmentRight;
    [self.setLabel setHidden:true];
    [self addSubview:self.setLabel];
    
    self.screatView = [[UIView alloc]initWithFrame:CGRectMake(20, self.setLabel.bottom+VLREALVALUE_WIDTH(8), SCREEN_WIDTH-40, 48)];
    self.screatView.hidden = YES;
    [self addSubview:self.screatView];

    VerifyCodeView *pwdView = [[VerifyCodeView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH-40, 48) codeNumbers:4 space:10 padding:00];
    pwdView.inputFinish = ^(NSString * _Nonnull pwd) {
        weakSelf.addRoomModel.password = pwd;
        if(weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(didCreateRoomAction:)]){
                [weakSelf.delegate didCreateRoomAction:weakSelf.addRoomModel.isPrivate ? DHCCreateRoomActionTypeEncrypt : DHCCreateRoomActionTypeNormal];
        }
    };
    [self.screatView addSubview:pwdView];
    self.codeView = pwdView;

    UIButton *createBtn = [[UIButton alloc] init];
    [createBtn setBackgroundImage:[UIImage dhc_sceneImageWith:@"createRoomBtn" ] forState:UIControlStateNormal];
    createBtn.accessibilityIdentifier = @"ktv_create_room_button_id";
    createBtn.titleLabel.font = UIFontBoldMake(16.0);
    createBtn.adjustsImageWhenHighlighted = NO;
    [createBtn addTarget:self action:@selector(createBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:createBtn];
    
    [createBtn mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.mas_equalTo(self.mas_left).offset(20);
            make.right.mas_equalTo(self.mas_right).offset(-20);
            make.height.mas_equalTo(48);
            make.bottom.mas_equalTo(self.mas_bottom).offset(-30);
    }];
    
    [self randomBtnClickEvent];
}

#pragma mark - Event
- (void)randomBtnClickEvent {
    [self createRandomNumber];
    self.inputTF.text = self.addRoomModel.name;
    NSString* iconName = [NSString stringWithFormat:@"icon_room_cover%@.jpg",self.addRoomModel.icon];
    self.iconImgView.image = [UIImage dhc_sceneImageWith: iconName];
}

-(void)enChange:(UIButton *)btn {
    btn.selected = !btn.selected;
    self.addRoomModel.isPrivate = btn.isSelected;
    self.screatView.hidden = !btn.selected;
    self.setLabel.hidden = !btn.selected;
    dispatch_async(dispatch_get_main_queue(), ^{
        if(btn.isSelected){
            [self.codeView.textFiled becomeFirstResponder];
        } else {
            [self.codeView.textFiled resignFirstResponder];
        }
    });
    
    [self endEditing:YES];
    if(self.delegate && [self.delegate respondsToSelector:@selector(didCreateRoomAction:)]){
        [self.delegate didCreateRoomAction:btn.isSelected ? DHCCreateRoomActionTypeEncrypt : DHCCreateRoomActionTypeNormal];
    }
}

- (void)createBtnClickEvent {
    if (!(self.inputTF.text.length > 0)) {
        [VLToast toast:@"ktv_insert_title".toSceneLocalization];
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
        [self.codeView.textFiled becomeFirstResponder];
    }else{
        self.publicBtn.selected = NO;
        self.screatBtn.selected = YES;
        self.addRoomModel.isPrivate = YES;
        [self.codeView.textFiled resignFirstResponder];
    }
    self.screatView.hidden = !self.screatBtn.selected;
}

- (void)textChangeAction:(UITextField *)sender {
    
}

// 实现 textFieldShouldReturn: 方法
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];  // 收起键盘
    if(self.delegate && [self.delegate respondsToSelector:@selector(didCreateRoomAction:)]){
        [self.delegate didCreateRoomAction:self.addRoomModel.isPrivate ? DHCCreateRoomActionTypeEncrypt : DHCCreateRoomActionTypeNormal];
    }
    return YES;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [super touchesBegan:touches withEvent:event];
    [self endEditing:YES];
    if(self.delegate && [self.delegate respondsToSelector:@selector(didCreateRoomAction:)]){
        [self.delegate didCreateRoomAction:self.addRoomModel.isPrivate ? DHCCreateRoomActionTypeEncrypt : DHCCreateRoomActionTypeNormal];
    }
}

-(void)streamChange:(UIButton *)btn{
    if(self.selBtn == btn){
        return;
    }
    self.selBtn.selected = false;
    btn.selected = true;
    self.selBtn = btn;
    if(btn.tag == 100){
        self.addRoomModel.streamMode = StreamModeNormol;
    } else if(btn.tag == 101) {
        self.addRoomModel.streamMode = StreamModeVol;
    } else {
        self.addRoomModel.streamMode = StreamModeMix;
    }
}

- (NSArray *)titlesArray {
    if (!_titlesArray) {
        _titlesArray = @[
            @"ktv_create_room_title1".toSceneLocalization,
            @"ktv_create_room_title2".toSceneLocalization,
            @"ktv_create_room_title3".toSceneLocalization,
            @"ktv_create_room_title4".toSceneLocalization,
            @"ktv_create_room_title5".toSceneLocalization,
            @"ktv_create_room_title6".toSceneLocalization,
            @"ktv_create_room_title7".toSceneLocalization,
            @"ktv_create_room_title8".toSceneLocalization,
            @"ktv_create_room_title9".toSceneLocalization,
            @"ktv_create_room_title10".toSceneLocalization,
            @"ktv_create_room_title11".toSceneLocalization,
            @"ktv_create_room_title12".toSceneLocalization,
            @"ktv_create_room_title13".toSceneLocalization,
            @"ktv_create_room_title14".toSceneLocalization,
            @"ktv_create_room_title15".toSceneLocalization,
            @"ktv_create_room_title16".toSceneLocalization,
            @"ktv_create_room_title17".toSceneLocalization,
            @"ktv_create_room_title18".toSceneLocalization,
            @"ktv_create_room_title19".toSceneLocalization,
            @"ktv_create_room_title20".toSceneLocalization,
            @"ktv_create_room_title21".toSceneLocalization,
        ];
    }
    return _titlesArray;
}


@end
