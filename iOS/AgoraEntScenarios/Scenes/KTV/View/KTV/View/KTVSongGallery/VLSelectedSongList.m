//
//  VLSelectSongView.m
//  VoiceOnLine
//

#import "VLSelectedSongList.h"
#import <JXCategoryView/JXCategoryView.h>
#import "VLSelectSongTableItemView.h"
#import "VLSearchSongResultView.h"
#import "VLSongItmModel.h"
#import "VLHotSpotBtn.h"
#import "AESMacro.h"
#import "AgoraEntScenarios-Swift.h"

#define BASICVCINDEX 100

#define BASICVCINDEX 100

@interface VLSelectedSongList ()<
JXCategoryViewDelegate,
JXCategoryListContainerViewDelegate,
VLSearchSongResultViewDelegate,
UITextFieldDelegate
>

@property(nonatomic, weak) id <VLSelectedSongListDelegate>delegate;
@property (nonatomic, strong) UIView *bgView;
@property (nonatomic, strong) UITextField *searchTF;
@property (nonatomic, strong) VLHotSpotBtn *cancelButton;
@property (nonatomic, strong) JXCategoryTitleView *categoryView;
@property (nonatomic, strong) JXCategoryListContainerView *listContainerView;
@property (nonatomic, strong) VLSearchSongResultView *resultView;
@property (nonatomic, assign) NSInteger currentIndex;
@property (nonatomic, strong) VLRoomListModel *roomModel;
@property (nonatomic, strong) NSMutableSet *selSongViews;
@property (nonatomic, copy) NSString *roomNo;
@end

@implementation VLSelectedSongList

- (void)setSelSongsArray:(NSArray *)selSongsArray {
    _selSongsArray = selSongsArray;
    [self updateUIWithSelSongsArray:selSongsArray];
}

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSelectedSongListDelegate>)delegate
                   withRoomNo:(NSString *)roomNo{
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        self.roomNo = roomNo;
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
    searchIcon.image = [UIImage ktv_sceneImageWithName:@"ktv_search_icon" ];
    [bgView addSubview:searchIcon];
    
    self.searchTF = [[UITextField alloc] initWithFrame:CGRectMake(searchIcon.right+8, 5, self.width-40-15-18-6-15, 30)];
    self.searchTF.accessibilityIdentifier = @"ktv_search_song_textfield_id";
    self.searchTF.textColor = UIColorMakeWithHex(@"#979CBB");
    self.searchTF.placeholder = KTVLocalizedString(@"ktv_dialog_music_list_search_hint");
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
    [clearButton setImage:[UIImage ktv_sceneImageWithName:@"ktv_search_clearIcon" ]
                 forState:UIControlStateNormal];
    [clearButton setImage:[UIImage ktv_sceneImageWithName:@"ktv_search_clearIcon" ]
                 forState:UIControlStateHighlighted];
    [clearButton setImage:[UIImage ktv_sceneImageWithName:@"ktv_search_clearIcon" ]
                 forState:UIControlStateSelected];
    
    //取消
    VLHotSpotBtn *cancelButton = [[VLHotSpotBtn alloc] initWithFrame:CGRectMake(self.width-50, bgView.top, 30, 40)];
    [cancelButton setTitle:KTVLocalizedString(@"ktv_cancel")
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
    
    self.categoryView = [[JXCategoryTitleView alloc] initWithFrame:CGRectMake(0, bgView.bottom+4, SCREEN_WIDTH, 40)];
    self.categoryView.delegate = self;
    [self addSubview:self.categoryView];
    
    self.categoryView.titles = @[
        KTVLocalizedString(@"ktv_song_rank_2"),
        KTVLocalizedString(@"ktv_song_rank_3"),
        KTVLocalizedString(@"ktv_song_rank_7"),
        KTVLocalizedString(@"ktv_song_rank_5")];
    self.categoryView.titleSelectedColor = UIColorWhite;
    self.categoryView.titleFont = UIFontMake(12);
    self.categoryView.titleColor = UIColorMakeWithHex(@"#979CBB");
    self.categoryView.titleColorGradientEnabled = YES;
    
    //添加指示器
    JXCategoryIndicatorLineView *lineView = [[JXCategoryIndicatorLineView alloc] init];
    lineView.indicatorColor = UIColorMakeWithHex(@"#009FFF");
    lineView.indicatorWidth = 18;
    lineView.height = 2;
    self.categoryView.indicators = @[lineView];
    
    self.listContainerView = [[JXCategoryListContainerView alloc] initWithType:JXCategoryListContainerType_ScrollView delegate:self];
    [self addSubview:self.listContainerView];
    self.listContainerView.frame = CGRectMake(0, self.categoryView.bottom + 10, SCREEN_WIDTH, SCREEN_HEIGHT*0.7-95);
    // 关联到 categoryView
    self.categoryView.listContainer = self.listContainerView;
    
    //搜索结果
    self.resultView = [[VLSearchSongResultView alloc]initWithFrame:CGRectMake(0, bgView.bottom+4, SCREEN_WIDTH, self.height-bgView.bottom-4)
                                                      withDelegate:self
                                                        withRoomNo:self.roomNo];
    self.resultView.hidden = YES;
    [self addSubview:self.resultView];
    
    self.selSongViews = [NSMutableSet set];
    self.currentIndex = 100;
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

-(void)setSelSongArrayWith:(NSArray *)array {
    [self updateUIWithSelSongsArray:array];
}

-(void)updateUIWithSelSongsArray:(NSArray *)array {
    VLSelectSongTableItemView *selView = nil;
    for(VLSelectSongTableItemView *view in self.selSongViews){
        if (view.tag  == self.currentIndex) {
            selView = view;
            [selView setSelSongArrayWith:array];
            return;
        }
    }
}

#pragma mark --delegate
- (void)categoryView:(JXCategoryBaseView *)categoryView didSelectedItemAtIndex:(NSInteger)index {
    self.currentIndex = index + BASICVCINDEX;
    [self setSelSongArrayWith:self.selSongsArray];
}

// 返回列表的数量
- (NSInteger)numberOfListsInlistContainerView:(JXCategoryListContainerView *)listContainerView {
    return 4;
}
// 根据下标 index 返回对应遵守并实现 `JXCategoryListContentViewDelegate` 协议的列表实例
- (id<JXCategoryListContentViewDelegate>)listContainerView:(JXCategoryListContainerView *)listContainerView initListForIndex:(NSInteger)index {
    VLSelectSongTableItemView *selSongView = [[VLSelectSongTableItemView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)
                                                                                    withRooNo:self.roomNo];
   // selSongView.selSongsArray = self.selSongsArray;
    [selSongView loadDatasWithIndex:index+1 ifRefresh:YES];
    selSongView.tag = BASICVCINDEX + index;
    [self.selSongViews addObject:selSongView];
    return selSongView;
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
