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
#import <Masonry/Masonry.h>
#import "AgoraEntScenarios-Swift.h"

@interface VLSBGCreateRoomView ()

@property(nonatomic, weak) id <VLSBGCreateRoomViewDelegate>delegate;
@property (nonatomic, strong) UITextField *inputTF;
@property (nonatomic, strong) UIView *screatView;
@property (nonatomic, strong) UIImageView *iconImgView;
@property (nonatomic, strong) UIButton *publicBtn;
@property (nonatomic, strong) UIButton *screatBtn;
@property (nonatomic, strong) VLSBGAddRoomModel *addRoomModel;
@property (nonatomic, strong) UIView *warningView;
@property (nonatomic, strong) UIButton *enBtn;
@property (nonatomic, strong) UILabel *setLabel;
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
    NSString *text = SBGLocalizedString(@"sbg_create_tips");
    UIFont *font = UIFontMake(12);
    CGSize constraintSize = CGSizeMake(self.width - 40, CGFLOAT_MAX);
    NSDictionary *attributes = @{NSFontAttributeName: font};
    CGRect textRect = [text boundingRectWithSize:constraintSize
                                         options:NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading
                                      attributes:attributes
                                         context:nil];
    CGFloat textHeight = ceil(CGRectGetHeight(textRect));
    
    self.warningView = [[UIView alloc]initWithFrame:CGRectMake(10, 10, self.width - 20, textHeight + 10)];
    self.warningView.backgroundColor = UIColorMakeWithHex(@"#FA396A1A");
    self.warningView.layer.cornerRadius = 5;
    self.warningView.layer.masksToBounds = true;
    [self addSubview:self.warningView];
    
    UIImageView *warImgView = [[UIImageView alloc]initWithFrame:CGRectMake(10, 5, 14, 14)];
    warImgView.image = [UIImage sceneImageWithName:@"add_circle"];
    [self.warningView addSubview:warImgView];
    
    UILabel *contentLabel = [[UILabel alloc]initWithFrame:CGRectMake(30, 5, self.warningView.width - 40, textHeight)];
    contentLabel.numberOfLines = 0;
    NSMutableAttributedString *attributedText = [[NSMutableAttributedString alloc] initWithString:text];
    [attributedText addAttribute:NSForegroundColorAttributeName value:[UIColor blackColor] range:NSMakeRange(0, 77)];
    [attributedText addAttribute:NSForegroundColorAttributeName value:[UIColor redColor] range:NSMakeRange(77, 41)];
    contentLabel.font = UIFontMake(12);
    contentLabel.attributedText = attributedText;
    [self.warningView addSubview:contentLabel];
    
    UILabel *roomTitleLabel = [[UILabel alloc]initWithFrame:CGRectMake(40, self.warningView.bottom+VLREALVALUE_WIDTH(20), 70, 20)];
    roomTitleLabel.font = UIFontMake(14);
    roomTitleLabel.textColor = UIColorMakeWithHex(@"#000000");
    roomTitleLabel.text = SBGLocalizedString(@"sbg_room_title");
    [self addSubview:roomTitleLabel];
    
    UIButton *randomBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [randomBtn setTitle:SBGLocalizedString(@"sbg_random") forState:UIControlStateNormal];
    [randomBtn setImage:[UIImage sceneImageWithName:@"online_create_randomIcon"] forState:UIControlStateNormal];
    randomBtn.spacingBetweenImageAndTitle = 3;
    randomBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [randomBtn setTitleColor:UIColorMakeWithHex(@"#3C4267") forState:UIControlStateNormal];
    randomBtn.titleLabel.font = UIFontMake(14.0);
    [randomBtn addTarget:self action:@selector(randomBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:randomBtn];
    [randomBtn sizeToFit];
    randomBtn.frame = CGRectMake(SCREEN_WIDTH - randomBtn.width - 50, roomTitleLabel.top, randomBtn.width, 20);
    
    UIView *inputBgView = [[UIView alloc] initWithFrame:CGRectMake(30, roomTitleLabel.bottom+15, SCREEN_WIDTH-60, 48)];
    inputBgView.layer.cornerRadius = 8;
    inputBgView.layer.masksToBounds = YES;
    inputBgView.backgroundColor = UIColorMakeWithHex(@"#F5F8FF");
    [self addSubview:inputBgView];

    self.inputTF = [[UITextField alloc] initWithFrame:CGRectMake(30, 9, inputBgView.width - 60, 30)];
    self.inputTF.accessibilityIdentifier = @"sbg_create_room_textfield_id";
    self.inputTF.textColor = UIColorMakeWithHex(@"#040925");
    self.inputTF.font = UIFontBoldMake(18);
    self.inputTF.clearButtonMode = UITextFieldViewModeWhileEditing;
    self.inputTF.tintColor = UIColorMakeWithHex(@"#345DFF");
    [self.inputTF addTarget:self action:@selector(textChangeAction:)forControlEvents:UIControlEventEditingChanged];
    [inputBgView addSubview:self.inputTF];
    
    UILabel *secretLabel = [[UILabel alloc]initWithFrame:CGRectMake(40, inputBgView.bottom+VLREALVALUE_WIDTH(30), 100, 20)];
    secretLabel.font = UIFontMake(14);
    secretLabel.textColor = UIColorMakeWithHex(@"#000000");
    secretLabel.text = SBGLocalizedString(@"sbg_room_is_encryption");
    [secretLabel sizeToFit];
    [self addSubview:secretLabel];
    
    self.enBtn = [[UIButton alloc]initWithFrame:CGRectMake(secretLabel.right + 8, inputBgView.bottom+VLREALVALUE_WIDTH(30), 32, 20)];
    [self.enBtn setBackgroundImage:[UIImage sceneImageWithName:@"guan"] forState:UIControlStateNormal];
    [self.enBtn setBackgroundImage:[UIImage sceneImageWithName:@"open"] forState:UIControlStateSelected];
    [self.enBtn addTarget:self action:@selector(enChange:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.enBtn];
    
    self.setLabel = [[UILabel alloc]initWithFrame:CGRectMake(self.width - 170, inputBgView.bottom+VLREALVALUE_WIDTH(30), 150, 17)];
    self.setLabel.font = UIFontMake(12);
    self.setLabel.textColor = UIColorMakeWithHex(@"#FA396A");
    self.setLabel.text = SBGLocalizedString(@"sbg_please_input_4_pwd");
    [self.setLabel sizeToFit];
    [self.setLabel setHidden:true];
    [self addSubview:self.setLabel];
    
    self.screatView = [[UIView alloc]initWithFrame:CGRectMake(40, self.setLabel.bottom+VLREALVALUE_WIDTH(30), SCREEN_WIDTH-80, 48+12+17)];
    self.screatView.hidden = YES;
    [self addSubview:self.screatView];
    
    VerifyCodeView *pwdView = [[VerifyCodeView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH-80, 55) codeNumbers:4 space:10 padding:10];
    pwdView.inputFinish = ^(NSString * _Nonnull pwd) {
        weakSelf.addRoomModel.password = pwd;
        if(weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(didCreateRoomAction:)]){
                [weakSelf.delegate didCreateRoomAction:weakSelf.addRoomModel.isPrivate ? SBGCreateRoomActionTypeEncrypt : SBGCreateRoomActionTypeNormal];
        }
    };
    [self.screatView addSubview:pwdView];
    
    UIButton *createBtn = [[UIButton alloc] init];
    createBtn.layer.cornerRadius = 5;
    createBtn.layer.masksToBounds = YES;
    //[createBtn setBackgroundImage:[UIImage sceneImageWithName:@"createRoomBg"] forState:UIControlStateNormal];
    [createBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
    [createBtn setTitle:SBGLocalizedString(@"sbg_create_room") forState:UIControlStateNormal];
    createBtn.accessibilityIdentifier = @"sbg_create_room_button_id";
    createBtn.titleLabel.font = UIFontBoldMake(16.0);
    createBtn.adjustsImageWhenHighlighted = NO;
    [createBtn addTarget:self action:@selector(createBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    createBtn.backgroundColor = UIColorMakeWithHex(@"#2753FF");
    [self addSubview:createBtn];
    
    [createBtn mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.mas_equalTo(self.mas_left).offset(30);
            make.right.mas_equalTo(self.mas_right).offset(-30);
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
    self.iconImgView.image = [UIImage sceneImageWithName: iconName];
}

-(void)enChange:(UIButton *)btn {
    btn.selected = !btn.selected;
    self.addRoomModel.isPrivate = btn.isSelected;
    self.screatView.hidden = !btn.selected;
    self.setLabel.hidden = !btn.selected;
    [self endEditing:YES];
    if(self.delegate && [self.delegate respondsToSelector:@selector(didCreateRoomAction:)]){
        [self.delegate didCreateRoomAction:btn.isSelected ? SBGCreateRoomActionTypeEncrypt : SBGCreateRoomActionTypeNormal];
    }
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

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self endEditing:YES];
    if(self.delegate && [self.delegate respondsToSelector:@selector(didCreateRoomAction:)]){
        [self.delegate didCreateRoomAction:self.addRoomModel.isPrivate ? SBGCreateRoomActionTypeEncrypt : SBGCreateRoomActionTypeNormal];
    }
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
