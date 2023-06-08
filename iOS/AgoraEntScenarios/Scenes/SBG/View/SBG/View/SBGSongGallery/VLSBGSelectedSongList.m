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
#import "VLHotSpotBtn.h"
#import "SBGMacro.h"

#define BASICVCINDEX 100

@interface VLSBGSelectedSongList ()<
JXCategoryViewDelegate,
JXCategoryListContainerViewDelegate,
VLSBGSearchSongResultViewDelegate,
UITextFieldDelegate
>

@property(nonatomic, weak) id <VLSBGSelectedSongListDelegate>delegate;
@property (nonatomic, strong) UIView *bgView;
@property (nonatomic, strong) UITextField *searchTF;
@property (nonatomic, strong) VLHotSpotBtn *cancelButton;
@property (nonatomic, strong) JXCategoryTitleView *categoryView;
@property (nonatomic, strong) JXCategoryListContainerView *listContainerView;
@property (nonatomic, strong) VLSBGSearchSongResultView *resultView;
@property (nonatomic, assign) NSInteger currentIndex;
@property (nonatomic, strong) VLSBGRoomListModel *roomModel;
@property (nonatomic, strong) NSMutableSet *selSongViews;
@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, assign) BOOL ifChorus;
@property (nonatomic, assign) BOOL isTaped;
@end

@implementation VLSBGSelectedSongList

- (void)setSelSongsArray:(NSArray *)selSongsArray {
    _selSongsArray = selSongsArray;
    [self updateUIWithSelSongsArray:selSongsArray];
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
    searchIcon.image = [UIImage sceneImageWithName:@"ktv_search_icon"];
    [bgView addSubview:searchIcon];
    
    self.searchTF = [[UITextField alloc] initWithFrame:CGRectMake(searchIcon.right+8, 5, self.width-40-15-18-6-15, 30)];
    self.searchTF.textColor = UIColorMakeWithHex(@"#979CBB");
    self.searchTF.placeholder = SBGLocalizedString(@"搜索歌曲,歌手");
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
    [clearButton setImage:[UIImage sceneImageWithName:@"ktv_search_clearIcon"]
                 forState:UIControlStateNormal];
    [clearButton setImage:[UIImage sceneImageWithName:@"ktv_search_clearIcon"]
                 forState:UIControlStateHighlighted];
    [clearButton setImage:[UIImage sceneImageWithName:@"ktv_search_clearIcon"]
                 forState:UIControlStateSelected];
    
    //取消
    VLHotSpotBtn *cancelButton = [[VLHotSpotBtn alloc] initWithFrame:CGRectMake(self.width-50, bgView.top, 30, 40)];
    [cancelButton setTitle:SBGLocalizedString(@"取消")
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
        SBGLocalizedString(@"嗨唱推荐"),
        SBGLocalizedString(@"抖音热歌"),
        SBGLocalizedString(@"New Songs List"),
        SBGLocalizedString(@"KTV必唱")];
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
    self.listContainerView.frame = CGRectMake(0, self.categoryView.bottom + 10, SCREEN_WIDTH, self.bounds.size.height - 40);
    // 关联到 categoryView
    self.categoryView.listContainer = self.listContainerView;
    
    //搜索结果
    self.resultView = [[VLSBGSearchSongResultView alloc]initWithFrame:CGRectMake(0, bgView.bottom+4, SCREEN_WIDTH, self.height + 5)
                                                      withDelegate:self
                                                        withRoomNo:self.roomNo
                                                          ifChorus:self.ifChorus];
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
   __block VLSBGSelectSongTableItemView *selView = nil;
    if(self.resultView){
        [self.resultView setSelSongsArray:array];
    }
    for(VLSBGSelectSongTableItemView *view in self.selSongViews){
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            if (view.tag  == self.currentIndex) {
                selView = view;
                [selView setSelSongArrayWith:array];
                return;
            }
        });
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
    VLSBGSelectSongTableItemView *selSongView = [[VLSBGSelectSongTableItemView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, self.bounds.size.height - 40)
                                                                                    withRooNo:self.roomNo
                                                                                     ifChorus:self.ifChorus];
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
