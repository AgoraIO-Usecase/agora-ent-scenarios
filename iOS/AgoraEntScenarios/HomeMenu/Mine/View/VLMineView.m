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
@import SDWebImage;
@import QMUIKit;
@import YYCategories;

@interface VLMineView ()<UITableViewDelegate,UITableViewDataSource>

@property(nonatomic, weak) id <VLMineViewDelegate>delegate;
@property (nonatomic, strong) UIView *mineTopView;
@property (nonatomic, strong) UIImageView *avatarImgView;
@property (nonatomic, strong) UILabel *nickNameLabel;
@property (nonatomic, strong) UILabel *IDLabel;
@property (nonatomic, strong) VLHotSpotBtn *editBtn;

@property (nonatomic, strong) UITableView *mineTable;
@property (nonatomic, strong) NSArray *itemsArray;

@end

@implementation VLMineView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLMineViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    [self addSubview:self.mineTopView];
    [self.mineTopView addSubview:self.avatarImgView];
    [self.mineTopView addSubview:self.nickNameLabel];
    [self.mineTopView addSubview:self.IDLabel];
    [self.mineTopView addSubview:self.editBtn];
    [self addSubview:self.mineTable];
}

- (void)editButtonClickEvent {
    if ([self.delegate respondsToSelector:@selector(mineViewDidCickUser:)]) {
        [self.delegate mineViewDidCickUser:VLMineViewUserClickTypeNickName];
    }
}

- (void)refreseUserInfo:(VLLoginModel *)loginModel {
    self.nickNameLabel.text = loginModel.name;
    [self.avatarImgView sd_setImageWithURL:[NSURL URLWithString:loginModel.headUrl] placeholderImage:[UIImage imageNamed:@"mine_avatar_placeHolder"]];
    if (loginModel.userNo.length > 0) {
        self.IDLabel.text = [NSString stringWithFormat:@"ID: %@",loginModel.userNo];
    }
    
}

- (void)refreseAvatar:(UIImage *)avatar {
    self.avatarImgView.image = avatar;
}

- (void)refreseNickName:(NSString *)nickName {
    self.nickNameLabel.text = nickName;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.itemsArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *reuseCell = @"reuse";
    VLMineTCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseCell];
    if (cell == nil) {
        cell = [[VLMineTCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseCell];
    }
    cell.dict = self.itemsArray[indexPath.row];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return VLREALVALUE_WIDTH(58);
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if ([self.delegate respondsToSelector:@selector(mineViewDidCick:)]) {
        [self.delegate mineViewDidCick:indexPath.row];
    }
}

- (UIView *)mineTopView {
    if (!_mineTopView) {
        _mineTopView = [[UIView alloc]initWithFrame:CGRectMake(20, kTopNavHeight+VLREALVALUE_WIDTH(35), self.width-40, VLREALVALUE_WIDTH(60)+50)];
        _mineTopView.layer.cornerRadius = 10;
        _mineTopView.layer.masksToBounds = YES;
        _mineTopView.backgroundColor = UIColorWhite;
    }
    return _mineTopView;
}

- (UIImageView *)avatarImgView {
    if (!_avatarImgView) {
        _avatarImgView = [[UIImageView alloc] init];
        _avatarImgView.frame = CGRectMake(20, 25, VLREALVALUE_WIDTH(60), VLREALVALUE_WIDTH(60));
        _avatarImgView.layer.cornerRadius = VLREALVALUE_WIDTH(60)*0.5;
        _avatarImgView.layer.masksToBounds = YES;
        _avatarImgView.userInteractionEnabled = YES;
        _avatarImgView.contentMode = UIViewContentModeScaleAspectFit;
        _avatarImgView.image = UIImageMake(@"mine_avatar_placeHolder");
        [_avatarImgView vl_whenTapped:^{
            if ([self.delegate respondsToSelector:@selector(mineViewDidCickUser:)]) {
                [self.delegate mineViewDidCickUser:VLMineViewUserClickTypeAvatar];
            }
        }];
    }
    return _avatarImgView;
}

- (UILabel *)nickNameLabel {
    if (!_nickNameLabel) {
        _nickNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(_avatarImgView.right+15, _avatarImgView.top+5, 120, 23)];
        _nickNameLabel.font = [UIFont systemFontOfSize:18 weight:UIFontWeightSemibold];
        _nickNameLabel.textColor = UIColorMakeWithHex(@"#040925");
        _nickNameLabel.text = NSLocalizedString(@"用户名", nil);
        _nickNameLabel.userInteractionEnabled = YES;
    }
    return _nickNameLabel;
}

- (UILabel *)IDLabel {
    if (!_IDLabel) {
        _IDLabel = [[UILabel alloc] initWithFrame:CGRectMake(_avatarImgView.right+15, _avatarImgView.centerY+5, SCREEN_WIDTH-VLREALVALUE_WIDTH(60)-120, 14)];
        _IDLabel.textColor = UIColorMakeWithHex(@"#6C7192");
        _IDLabel.font = UIFontMake(12);
        _IDLabel.text = @"ID: 545021509X";
//        _IDLabel.backgroundColor = UIColorRed;
        _IDLabel.lineBreakMode = NSLineBreakByWordWrapping | NSLineBreakByCharWrapping;
        _IDLabel.userInteractionEnabled = YES;
        _IDLabel.hidden = YES;
    }
    return _IDLabel;
}

- (VLHotSpotBtn *)editBtn {
    if (!_editBtn) {
        _editBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(self.width-40-15-20, _nickNameLabel.centerY-10, 20, 20)];
        [_editBtn setImage:UIImageMake(@"mine_edit_icon") forState:UIControlStateNormal];
        [_editBtn addTarget:self action:@selector(editButtonClickEvent) forControlEvents:UIControlEventTouchUpInside];
    }
    return _editBtn;
}

- (UITableView *)mineTable{
    if(!_mineTable) {
        _mineTable = [[UITableView alloc]initWithFrame:CGRectMake(20, _mineTopView.bottom+VLREALVALUE_WIDTH(15), SCREEN_WIDTH-40, VLREALVALUE_WIDTH(58)*5+10)];
        _mineTable.dataSource = self;
        _mineTable.delegate = self;
        _mineTable.backgroundColor = UIColorWhite;
        _mineTable.layer.cornerRadius = 10;
        _mineTable.layer.masksToBounds = YES;
        _mineTable.scrollEnabled = NO;
        _mineTable.separatorStyle = UITableViewCellSeparatorStyleNone;
    }
    return _mineTable;
}

- (NSArray *)itemsArray {
    if (!_itemsArray) {
        _itemsArray = @[
            @{@"itemImgStr":@"mine_screct_icon",@"titleStr":NSLocalizedString(@"用户协议", nil)},
            @{@"itemImgStr":@"mine_userRule_icon",@"titleStr":NSLocalizedString(@"隐私政策", nil)},
            @{@"itemImgStr":@"mine_aboutus_icon",@"titleStr":NSLocalizedString(@"关于我们", nil)},
            @{@"itemImgStr":@"mine_logout_icon",@"titleStr":NSLocalizedString(@"退出登录", nil)},
            @{@"itemImgStr":@"mine_quit_icon",@"titleStr":NSLocalizedString(@"注销账号", nil)},
        ];
    }
    return _itemsArray;
}


@end
