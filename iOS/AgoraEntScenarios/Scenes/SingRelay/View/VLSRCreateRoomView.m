//
//  VLCreateRoomView.m
//  VoiceOnLine
//

#import "VLSRCreateRoomView.h"
#import "VLSRAddRoomModel.h"
#import "VLMacroDefine.h"
#import "VLToast.h"
#import "AESMacro.h"
#import "MenuUtils.h"
#import <Masonry/Masonry.h>
#import "AgoraEntScenarios-Swift.h"

@interface VLSRCreateRoomView ()

@property(nonatomic, weak) id <VLSRCreateRoomViewDelegate>delegate;
@property (nonatomic, strong) UITextField *inputTF;
@property (nonatomic, strong) UIView *screatView;
@property (nonatomic, strong) UIImageView *iconImgView;
@property (nonatomic, strong) UIButton *publicBtn;
@property (nonatomic, strong) UIButton *screatBtn;
@property (nonatomic, strong) VLSRAddRoomModel *addRoomModel;
@property (nonatomic, strong) UIView *warningView;
@property (nonatomic, strong) UIButton *enBtn;
@property (nonatomic, strong) UILabel *setLabel;
@property (nonatomic, strong) VerifyCodeView *codeView;
@property (nonatomic, strong) NSArray *titlesArray;
@end

@implementation VLSRCreateRoomView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRCreateRoomViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        self.addRoomModel = [[VLSRAddRoomModel alloc]init];
        [self createRandomNumber];
        [self setupView];
    }
    return self;
}

- (void)setupView {
    VL(weakSelf);
    self.addRoomModel.isPrivate = NO;
    
    NSString *text = SRLocalizedString(@"sr_create_tips");
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
    warImgView.image = [UIImage sr_sceneImageWithName:@"zhuyi" ];
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
    roomTitleLabel.text = SRLocalizedString(@"sr_room_title");
    [self addSubview:roomTitleLabel];

    UIButton *randomBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [randomBtn setTitle:SRLocalizedString(@"sr_random") forState:UIControlStateNormal];
    [randomBtn setImage:[UIImage sr_sceneImageWithName:@"online_create_randomIcon" ] forState:UIControlStateNormal];
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
    self.inputTF.placeholder = SRLocalizedString(@"sr_room_placeHolder");
    [self.inputTF addTarget:self action:@selector(textChangeAction:)forControlEvents:UIControlEventEditingChanged];
    [inputBgView addSubview:self.inputTF];
    self.inputTF.delegate = self;
    
    UILabel *secretLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, inputBgView.bottom+VLREALVALUE_WIDTH(24), 100, 20)];
    secretLabel.font = UIFontMake(14);
    secretLabel.textColor = UIColorMakeWithHex(@"#000000");
    secretLabel.text = SRLocalizedString(@"sr_room_is_encryption");
    [secretLabel sizeToFit];
    [self addSubview:secretLabel];
    
    self.enBtn = [[UIButton alloc]initWithFrame:CGRectMake(secretLabel.right + 8, inputBgView.bottom+VLREALVALUE_WIDTH(24), 32, 20)];
    [self.enBtn setBackgroundImage:[UIImage sr_sceneImageWithName:@"guan" ] forState:UIControlStateNormal];
    [self.enBtn setBackgroundImage:[UIImage sr_sceneImageWithName:@"open" ] forState:UIControlStateSelected];
    [self.enBtn addTarget:self action:@selector(enChange:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.enBtn];
    
    self.setLabel = [[UILabel alloc]initWithFrame:CGRectMake(SCREEN_WIDTH - 170, inputBgView.bottom+VLREALVALUE_WIDTH(25.5), 150, 17)];
    self.setLabel.font = UIFontMake(12);
    self.setLabel.textColor = UIColorMakeWithHex(@"#FA396A");
    self.setLabel.text = SRLocalizedString(@"sr_please_input_4_pwd");
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
                [weakSelf.delegate didCreateRoomAction:weakSelf.addRoomModel.isPrivate ? SRCreateRoomActionTypeEncrypt : SRCreateRoomActionTypeNormal];
        }
    };
    [self.screatView addSubview:pwdView];
    self.codeView = pwdView;

    UIButton *createBtn = [[UIButton alloc] init];
    [createBtn setBackgroundImage:[UIImage sr_sceneImageWithName:@"createRoomBtn" ] forState:UIControlStateNormal];
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
    self.iconImgView.image = [UIImage sr_sceneImageWithName: iconName ];
}

-(void)enChange:(UIButton *)btn {
    btn.selected = !btn.selected;
    self.addRoomModel.isPrivate = btn.isSelected;
    self.screatView.hidden = !btn.selected;
    self.setLabel.hidden = !btn.selected;
    [self endEditing:YES];
    if(self.delegate && [self.delegate respondsToSelector:@selector(didCreateRoomAction:)]){
        [self.delegate didCreateRoomAction:btn.isSelected ? SRCreateRoomActionTypeEncrypt : SRCreateRoomActionTypeNormal];
    }
}

- (void)createBtnClickEvent {
    if (!(self.inputTF.text.length > 0)) {
        [VLToast toast:SRLocalizedString(@"sr_insert_title")];
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

// 实现 textFieldShouldReturn: 方法
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];  // 收起键盘
    if(self.delegate && [self.delegate respondsToSelector:@selector(didCreateRoomAction:)]){
        [self.delegate didCreateRoomAction:self.addRoomModel.isPrivate ? SRCreateRoomActionTypeEncrypt : SRCreateRoomActionTypeNormal];
    }
    return YES;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [super touchesBegan:touches withEvent:event];
    [self endEditing:YES];
    if(self.delegate && [self.delegate respondsToSelector:@selector(didCreateRoomAction:)]){
        [self.delegate didCreateRoomAction:self.addRoomModel.isPrivate ? SRCreateRoomActionTypeEncrypt : SRCreateRoomActionTypeNormal];
    }
}


- (NSArray *)titlesArray {
    if (!_titlesArray) {
        _titlesArray = @[
            SRLocalizedString(@"sr_create_room_title1"),
            SRLocalizedString(@"sr_create_room_title2"),
            SRLocalizedString(@"sr_create_room_title3"),
            SRLocalizedString(@"sr_create_room_title4"),
            SRLocalizedString(@"sr_create_room_title5"),
            SRLocalizedString(@"sr_create_room_title6"),
            SRLocalizedString(@"sr_create_room_title7"),
            SRLocalizedString(@"sr_create_room_title8"),
            SRLocalizedString(@"sr_create_room_title9"),
            SRLocalizedString(@"sr_create_room_title10"),
            SRLocalizedString(@"sr_create_room_title11"),
            SRLocalizedString(@"sr_create_room_title12"),
            SRLocalizedString(@"sr_create_room_title13"),
            SRLocalizedString(@"sr_create_room_title14"),
            SRLocalizedString(@"sr_create_room_title15"),
            SRLocalizedString(@"sr_create_room_title16"),
            SRLocalizedString(@"sr_create_room_title17"),
            SRLocalizedString(@"sr_create_room_title18"),
            SRLocalizedString(@"sr_create_room_title19"),
            SRLocalizedString(@"sr_create_room_title20"),
            SRLocalizedString(@"sr_create_room_title21")
        ];
    }
    return _titlesArray;
}


@end
