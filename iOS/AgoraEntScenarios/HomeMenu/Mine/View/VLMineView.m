//
//  VLMineView.m
//  VoiceOnLine
//

#import "VLMineView.h"
#import "VLHotSpotBtn.h"
#import "VLMineTCell.h"
#import "VLLoginModel.h"
#import "VLMacroDefine.h"
#import "UIView+VL.h"
#import "MenuUtils.h"
#import "VLMineCellModel.h"
#import "AgoraEntScenarios-Bridging-Header.h"
#import "AESMacro.h"
#import "VLURLPathConfig.h"
#import "VLToast.h"

@import SDWebImage;

static NSString * const kSwitchCellID = @"switchCellID";
static NSString * const kDefaultCellID = @"kDefaultCellID";

@interface VLMineView ()<UITableViewDelegate,UITableViewDataSource, UITextFieldDelegate>

@property(nonatomic, weak) id <VLMineViewDelegate>delegate;
@property (nonatomic, strong) UIView *mineTopView;
@property (nonatomic, strong) UIImageView *avatarImgView;
@property (nonatomic, strong) UITextField *nickNameTF;
@property (nonatomic, strong) UILabel *IDLabel;
@property (nonatomic, strong) VLHotSpotBtn *editBtn;

@property (nonatomic, strong) UITableView *mineTable;
@property (nonatomic, strong) NSMutableArray<NSArray *> *dataArray;

@end

@implementation VLMineView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLMineViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self setupView];
        [self setupData];
    }
    return self;
}

- (void)setupView {
    [self addSubview:self.mineTable];
    [self.mineTable.leadingAnchor constraintEqualToAnchor:self.leadingAnchor constant:20].active = YES;
    [self.mineTable.topAnchor constraintEqualToAnchor:self.topAnchor].active = YES;
    [self.mineTable.trailingAnchor constraintEqualToAnchor:self.trailingAnchor constant:-20].active = YES;
    [self.mineTable.bottomAnchor constraintEqualToAnchor:self.bottomAnchor].active = YES;
    
    [self.mineTopView addSubview:self.avatarImgView];
    [self.mineTopView addSubview:self.nickNameTF];
    [self.mineTopView addSubview:self.IDLabel];
    [self.mineTopView addSubview:self.editBtn];
    [self.avatarImgView.widthAnchor constraintEqualToConstant:VLREALVALUE_WIDTH(80)].active = YES;
    [self.avatarImgView.heightAnchor constraintEqualToConstant:VLREALVALUE_WIDTH(80)].active = YES;
    [self.avatarImgView.centerXAnchor constraintEqualToAnchor:self.mineTopView.centerXAnchor].active = YES;
    [self.avatarImgView.bottomAnchor constraintEqualToAnchor:self.mineTopView.centerYAnchor constant:0].active = YES;
    
    [self.nickNameTF.centerXAnchor constraintEqualToAnchor:self.mineTopView.centerXAnchor].active = YES;
    [self.nickNameTF.topAnchor constraintEqualToAnchor:self.avatarImgView.bottomAnchor constant:12].active = YES;
    
    [self.editBtn.centerYAnchor constraintEqualToAnchor:self.nickNameTF.centerYAnchor].active = YES;
    [self.editBtn.leadingAnchor constraintEqualToAnchor:self.nickNameTF.trailingAnchor constant:5].active = YES;
    
    [self.IDLabel.centerXAnchor constraintEqualToAnchor:self.mineTopView.centerXAnchor].active = YES;
    [self.IDLabel.topAnchor constraintEqualToAnchor:self.nickNameTF.bottomAnchor constant:5].active = YES;
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(keyboardWillHide:)
                                                     name:UIKeyboardWillHideNotification
                                                   object:nil];
}

- (void)keyboardWillHide:(NSNotification *)notification {
    self.editBtn.hidden = NO;
}
- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)setupData {
    BOOL developIsOn = [AppContext shared].isDebugMode;
    if (developIsOn) {
        VLMineCellModel *model = [VLMineCellModel modelWithItemImg:@"mine_debug_icon" title:NSLocalizedString(@"app_debug_mode", nil) style:VLMineCellStyleSwitch];
        [self.dataArray addObject:@[model]];
    }
    
    CGFloat height = 0;
    for (int i = 0; i < self.dataArray.count; i++) {
        height += 16.0 + self.dataArray[i].count * VLREALVALUE_WIDTH(58);
    }
    height = self.frame.size.height - height - 20;
    height = height < 155 ? 155 : height;
    self.mineTopView.frame = CGRectMake(0, 0, SCREEN_WIDTH, height);
    [self.mineTable reloadData];
}

- (void)editButtonClickEvent {
    if (!self.nickNameTF.isFirstResponder) {
        [self.nickNameTF becomeFirstResponder];
    } else {
        [self.nickNameTF resignFirstResponder];
    }
    self.editBtn.hidden = self.nickNameTF.isFirstResponder;
}

- (void)loadUpdateNickNameRequest:(NSString *)nickName {
    NSDictionary *param = @{
        @"userNo" : VLUserCenter.user.userNo ?: @"",
        @"name" : nickName ?: @""
    };
    [VLAPIRequest postRequestURL:kURLPathUploadUserInfo parameter:param showHUD:YES success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            [VLToast toast:AGLocalizedString(@"app_edit_success")];
            [self refreseNickName:nickName];
            VLUserCenter.user.name = nickName;
            [[VLUserCenter center] storeUserInfo:VLUserCenter.user];
        }else{
            [VLToast toast:response.message];
        }
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
    }];
}

- (void)refreseUserInfo:(VLLoginModel *)loginModel {
    self.nickNameTF.text = loginModel.name;
    [self.avatarImgView sd_setImageWithURL:[NSURL URLWithString:loginModel.headUrl] placeholderImage:[UIImage imageNamed:@"mine_avatar_placeHolder"]];
    self.IDLabel.text = [loginModel.mobile stringByReplacingCharactersInRange:NSMakeRange(3, 4) withString:@"****"];
}

- (void)refreseAvatar:(UIImage *)avatar {
    self.avatarImgView.image = avatar;
}

- (void)refreseNickName:(NSString *)nickName {
    self.nickNameTF.text = nickName;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.dataArray.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataArray[section].count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    VLMineCellModel *model = self.dataArray[indexPath.section][indexPath.row];
    VLMineTCell *cell = [tableView dequeueReusableCellWithIdentifier:kDefaultCellID forIndexPath:indexPath];
    [cell setTitle:model.titleStr];
    if (self.dataArray[indexPath.section].count == 1) {
        cell.contentView.layer.cornerRadius = 16;
        cell.contentView.layer.masksToBounds = YES;
    } else {
        if (indexPath.row == 0) {
            cell.contentView.layer.cornerRadius = 16;
            cell.contentView.layer.masksToBounds = YES;
            cell.contentView.layer.maskedCorners = kCALayerMinXMinYCorner | kCALayerMaxXMinYCorner;
        }
        if (indexPath.row == self.dataArray[indexPath.section].count - 1) {
            cell.contentView.layer.cornerRadius = 16;
            cell.contentView.layer.masksToBounds = YES;
            cell.contentView.layer.maskedCorners = kCALayerMinXMaxYCorner | kCALayerMaxXMaxYCorner;
        }
    }
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return VLREALVALUE_WIDTH(58);
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 16.0;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.1;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if ([self.delegate respondsToSelector:@selector(mineViewDidCick:)]) {
        VLMineCellModel *model = self.dataArray[indexPath.section][indexPath.row];
        [self.delegate mineViewDidCick:model.clickType];
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    if (textField.text.length <= 0) {
        [VLToast toast:AGLocalizedString(@"input_edit_name")];
        return YES;
    }
    [self loadUpdateNickNameRequest:textField.text];
    [textField resignFirstResponder];
    return YES;
}

- (void)textFieldDidBeginEditing:(UITextField *)textField {
    self.editBtn.hidden = YES;
    // 全选文字
    UITextPosition *endDocument = textField.endOfDocument;
    UITextPosition *end = [textField positionFromPosition:endDocument offset:0];
    UITextPosition *start = [textField positionFromPosition:end offset:-textField.text.length];
    textField.selectedTextRange = [textField textRangeFromPosition:start toPosition:end];
}

- (UIView *)mineTopView {
    if (!_mineTopView) {
        _mineTopView = [[UIView alloc]init];
        _mineTopView.backgroundColor = [UIColor clearColor];
    }
    return _mineTopView;
}

- (UIImageView *)avatarImgView {
    if (!_avatarImgView) {
        _avatarImgView = [[UIImageView alloc] init];
        _avatarImgView.layer.cornerRadius = VLREALVALUE_WIDTH(80)*0.5;
        _avatarImgView.layer.masksToBounds = YES;
        _avatarImgView.userInteractionEnabled = YES;
        _avatarImgView.contentMode = UIViewContentModeScaleAspectFit;
        _avatarImgView.image = UIImageMake(@"mine_avatar_placeHolder");
        _avatarImgView.translatesAutoresizingMaskIntoConstraints = NO;
        [_avatarImgView vl_whenTapped:^{
            if ([self.delegate respondsToSelector:@selector(mineViewDidCickUser:)]) {
                [self.delegate mineViewDidCickUser:VLMineViewUserClickTypeAvatar];
            }
        }];
    }
    return _avatarImgView;
}

- (UITextField *)nickNameTF {
    if (!_nickNameTF) {
        _nickNameTF = [[UITextField alloc] init];
        _nickNameTF.font = [UIFont systemFontOfSize:20 weight:UIFontWeightMedium];
        _nickNameTF.textColor = UIColorMakeWithHex(@"#111111");
        _nickNameTF.text = AGLocalizedString(@"userName");
        _nickNameTF.translatesAutoresizingMaskIntoConstraints = NO;
        _nickNameTF.returnKeyType = UIReturnKeyDone;
        _nickNameTF.delegate = self;
    }
    return _nickNameTF;
}

- (UILabel *)IDLabel {
    if (!_IDLabel) {
        _IDLabel = [[UILabel alloc] init];
        _IDLabel.textColor = UIColorMakeWithHex(@"#6F738B");
        _IDLabel.font = UIFontMake(14);
        _IDLabel.translatesAutoresizingMaskIntoConstraints = NO;
    }
    return _IDLabel;
}

- (VLHotSpotBtn *)editBtn {
    if (!_editBtn) {
        _editBtn = [[VLHotSpotBtn alloc]init];
        [_editBtn setImage:UIImageMake(@"mine_edit_icon") forState:UIControlStateNormal];
        [_editBtn addTarget:self action:@selector(editButtonClickEvent) forControlEvents:UIControlEventTouchUpInside];
        _editBtn.translatesAutoresizingMaskIntoConstraints = NO;
    }
    return _editBtn;
}

- (UITableView *)mineTable{
    if(!_mineTable) {
        _mineTable = [[UITableView alloc]initWithFrame:CGRectZero style:(UITableViewStyleGrouped)];
        _mineTable.dataSource = self;
        _mineTable.delegate = self;
        _mineTable.backgroundColor = [UIColor clearColor];
        _mineTable.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
        _mineTable.separatorColor = [UIColor colorWithHexString:@"#F2F2F6"];
        [_mineTable registerClass:[VLMineTCell class] forCellReuseIdentifier:kDefaultCellID];
        [_mineTable registerClass:[VLMineSwitchCell class] forCellReuseIdentifier:kSwitchCellID];
        _mineTable.translatesAutoresizingMaskIntoConstraints = NO;
        _mineTable.estimatedSectionFooterHeight = 0;
        _mineTable.estimatedSectionHeaderHeight = 0;
        _mineTable.tableHeaderView = self.mineTopView;
        _mineTable.showsVerticalScrollIndicator = NO;
    }
    return _mineTable;
}

- (NSMutableArray *)dataArray {
    if (!_dataArray) {
        _dataArray = [[NSMutableArray alloc] init];
        
        NSArray *tempArray = @[[VLMineCellModel modelWithItemImg:@"" title:NSLocalizedString(@"app_my_account", nil) clickType:VLMineViewClickTypeMyAccount]];
        [_dataArray addObject:tempArray];
                      
        tempArray = @[[VLMineCellModel modelWithItemImg:@"" title:NSLocalizedString(@"app_user_agreement", nil) clickType:VLMineViewClickTypeUserProtocol],
                      [VLMineCellModel modelWithItemImg:@"" title:NSLocalizedString(@"app_privacy_agreement", nil) clickType:VLMineViewClickTypePrivacyProtocol],
                      [VLMineCellModel modelWithItemImg:@"" title:NSLocalizedString(@"app_third_party_info_data_sharing", nil) clickType:VLMineViewClickTypeThirdInfoShared],
                      [VLMineCellModel modelWithItemImg:@"" title:NSLocalizedString(@"app_personal_info_collection_checklist", nil) clickType:VLMineViewClickTypePersonInfo]];
        [_dataArray addObject:tempArray];
        
        tempArray = @[[VLMineCellModel modelWithItemImg:@"" title:NSLocalizedString(@"app_about_us", nil) clickType:VLMineViewClickTypeAboutUS],
                      [VLMineCellModel modelWithItemImg:@"" title:NSLocalizedString(@"app_submit_feedback", nil) clickType:VLMineViewClickTypSubmitFeedback]];
        [_dataArray addObject:tempArray];
    }
    return _dataArray;
}

- (void)refreshTableView {
    [self setupData];
    [self.mineTable reloadData];
}

@end

