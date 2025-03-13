//
//  VLSelectSongView.m
//  VoiceOnLine
//

#import "VLSBGSelectedSongList.h"
#import <JXCategoryView/JXCategoryView.h>
#import "VLSBGSelectSongTableItemView.h"
#import "VLSBGSearchSongResultView.h"
#import "VLSBGRoomListModel.h"
#import "VLSBGSongItmModel.h"
#import "SBGMacro.h"

#define BASICVCINDEX 100

@interface VLSBGSelectedSongList ()<
VLSBGSearchSongResultViewDelegate,
UITextFieldDelegate
>

@property(nonatomic, weak) id <VLSBGSelectedSongListDelegate>delegate;
@property (nonatomic, strong) UIView *bgView;
@property (nonatomic, strong) UITextField *searchTF;
@property (nonatomic, strong) VLHotSpotBtn *cancelButton;
@property (nonatomic, strong) JXCategoryListContainerView *listContainerView;
@property (nonatomic, strong) VLSBGSearchSongResultView *resultView;
@property (nonatomic, assign) NSInteger currentIndex;
@property (nonatomic, strong) VLSBGRoomListModel *roomModel;
@property (nonatomic, strong) VLSBGSelectSongTableItemView *selSongView;
@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, assign) BOOL ifChorus;
@property (nonatomic, assign) BOOL isTaped;
@end

@implementation VLSBGSelectedSongList

- (void)setSelSongsArray:(NSArray *)selSongsArray {
    _selSongsArray = selSongsArray;
    [self.selSongView setSelSongArrayWith:selSongsArray];
}

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSBGSelectedSongListDelegate>)delegate
                   withRoomNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus{
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        self.roomNo = roomNo;
        self.ifChorus = ifChorus;
        [self setupView];
    }
    return self;
}

- (void)setupView{
    UIView *bgView = [[UIView alloc]initWithFrame:CGRectMake(20, 0, self.width-40, 40)];
    bgView.layer.cornerRadius = 20;
    bgView.layer.masksToBounds = YES;
    self.bgView = bgView;
    bgView.backgroundColor = UIColorMakeWithRGBA(4, 9, 37, 0.35);
    [self addSubview:bgView];
    
    UIImageView *searchIcon = [[UIImageView alloc]initWithFrame:CGRectMake(15, 11, 18, 18)];
    searchIcon.image = [UIImage sbg_sceneImageWithName:@"ktv_search_icon"];
    [bgView addSubview:searchIcon];
    
    self.searchTF = [[UITextField alloc] initWithFrame:CGRectMake(searchIcon.right+8, 5, self.width-40-15-18-6-15, 30)];
    self.searchTF.textColor = UIColorMakeWithHex(@"#979CBB");
    self.searchTF.placeholder = SBGLocalizedString(@"sbg_search_music_singer");
    self.searchTF.font = UIFontMake(15);
    self.searchTF.clearButtonMode = UITextFieldViewModeWhileEditing;
    self.searchTF.tintColor = UIColorMakeWithHex(@"#2753FF");
    self.searchTF.returnKeyType = UIReturnKeySearch;
    self.searchTF.delegate = self;
    [self.searchTF addTarget:self
                      action:@selector(textChangeAction)
            forControlEvents:UIControlEventEditingChanged];
    self.searchTF.attributedPlaceholder = [[NSAttributedString alloc] initWithString:self.searchTF.placeholder
                                                                          attributes:@{NSForegroundColorAttributeName:UIColorMakeWithHex(@"#979CBB")}];
    [bgView addSubview:self.searchTF];
    
    VLHotSpotBtn *clearButton = [self.searchTF valueForKey:@"_clearButton"];
    //frame必须设置 否则 点击删除键后 clearButton 会变小（系统默认是19*19）
    clearButton.frame = CGRectMake(0, 0, 20, 20);
    [clearButton setImage:[UIImage sbg_sceneImageWithName:@"ktv_search_clearIcon"]
                 forState:UIControlStateNormal];
    [clearButton setImage:[UIImage sbg_sceneImageWithName:@"ktv_search_clearIcon"]
                 forState:UIControlStateHighlighted];
    [clearButton setImage:[UIImage sbg_sceneImageWithName:@"ktv_search_clearIcon"]
                 forState:UIControlStateSelected];
    
    //取消
    VLHotSpotBtn *cancelButton = [[VLHotSpotBtn alloc] initWithFrame:CGRectMake(self.width-50, bgView.top, 30, 40)];
    [cancelButton setTitle:SBGLocalizedString(@"sbg_cancel")
                  forState:UIControlStateNormal];
    [cancelButton setTitleColor:UIColorMakeWithHex(@"#C6C4DE")
                       forState:UIControlStateNormal];
    cancelButton.titleLabel.font = UIFontMake(14);
    self.cancelButton = cancelButton;
    self.cancelButton.hidden = YES;
    [cancelButton addTarget:self
                     action:@selector(cancelBtnClickEvent)
           forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:cancelButton];
    
    self.selSongView = [[VLSBGSelectSongTableItemView alloc] initWithFrame:CGRectMake(0, bgView.bottom+4, SCREEN_WIDTH, self.height + 5)
                                                                 withRooNo:self.roomNo
                                                                  ifChorus:self.ifChorus];
    [self.selSongView loadDatasWithIndex:0 ifRefresh:YES];
    [self addSubview:self.selSongView];
    self.selSongView.frame = CGRectMake(0, self.searchTF.bottom + 10, SCREEN_WIDTH, SCREEN_HEIGHT*0.7-95);
    
    //搜索结果
    self.resultView = [[VLSBGSearchSongResultView alloc]initWithFrame:CGRectMake(0, bgView.bottom+4, SCREEN_WIDTH, self.height + 5)
                                                      withDelegate:self
                                                        withRoomNo:self.roomNo
                                                          ifChorus:self.ifChorus];
    self.resultView.hidden = YES;
    [self addSubview:self.resultView];
}

#pragma mark --Event
- (void)cancelBtnClickEvent{
    [self.searchTF resignFirstResponder];
    [UIView animateWithDuration:0.25 animations:^{
        self.bgView.width = self.width-40;
        self.searchTF.width = self.width-40-15-18-6-15;
        self.cancelButton.hidden = YES;
        self.searchTF.text = @"";
    }];
    self.resultView.hidden = YES;
}

- (void)textChangeAction {
    if (self.searchTF.text.length > 0) {
        [UIView animateWithDuration:0.25 animations:^{
            self.bgView.width = self.width-40-20-30-5;
            self.searchTF.width = self.bgView.width - 56;
            self.cancelButton.hidden = NO;
        }];
    }
}

- (BOOL)textFieldShouldReturn:(UITextField*)textField {
    [self.searchTF resignFirstResponder];
    if(textField.returnKeyType == UIReturnKeySearch)
    {
        if ([self.searchTF.text length] > 0)
        {
            
            self.resultView.hidden = NO;
            [self.resultView loadSearchDataWithKeyWord:self.searchTF.text ifRefresh:YES];
        }
    }
    return YES;
}

@end
