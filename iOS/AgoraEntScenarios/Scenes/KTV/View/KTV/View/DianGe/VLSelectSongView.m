//
//  VLSelectSongView.m
//  VoiceOnLine
//

#import "VLSelectSongView.h"
#import <JXCategoryView/JXCategoryView.h>
#import "VLSelectSongTableItemView.h"
#import "VLSearchSongResultView.h"
#import "VLRoomListModel.h"
#import "VLSongItmModel.h"
#import "VLHotSpotBtn.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;

@interface VLSelectSongView ()<
JXCategoryViewDelegate,
JXCategoryListContainerViewDelegate,
VLSearchSongResultViewDelegate,
UITextFieldDelegate
>

@property(nonatomic, weak) id <VLSelectSongViewDelegate>delegate;
@property (nonatomic, strong) UIView *bgView;
@property (nonatomic, strong) UITextField *searchTF;
@property (nonatomic, strong) VLHotSpotBtn *cancelButton;
@property (nonatomic, strong) JXCategoryTitleView *categoryView;
@property (nonatomic, strong) JXCategoryListContainerView *listContainerView;
@property (nonatomic, strong) VLSearchSongResultView *resultView;

@property (nonatomic, strong) VLRoomListModel *roomModel;

@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, assign) BOOL ifChorus;

@end

@implementation VLSelectSongView

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSelectSongViewDelegate>)delegate
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
    self.searchTF.placeholder = KTVLocalizedString(@"搜索歌曲,歌手");
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
    [cancelButton setTitle:KTVLocalizedString(@"取消")
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
        KTVLocalizedString(@"嗨唱推荐"),
        KTVLocalizedString(@"抖音热歌"),
        KTVLocalizedString(@"New Songs List"),
        KTVLocalizedString(@"KTV必唱")];
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
    self.listContainerView.frame = CGRectMake(0, 95, SCREEN_WIDTH, SCREEN_HEIGHT*0.7-95);
    // 关联到 categoryView
    self.categoryView.listContainer = self.listContainerView;
    
    //搜索结果
    self.resultView = [[VLSearchSongResultView alloc]initWithFrame:CGRectMake(0, bgView.bottom+4, SCREEN_WIDTH, self.height-bgView.bottom-4)
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

#pragma mark --delegate
// 点击选中或者滚动选中都会调用该方法。适用于只关心选中事件，不关心具体是点击还是滚动选中的。
- (void)categoryView:(JXCategoryBaseView *)categoryView didSelectedItemAtIndex:(NSInteger)index {
    
}

// 返回列表的数量
- (NSInteger)numberOfListsInlistContainerView:(JXCategoryListContainerView *)listContainerView {
    return 4;
}
// 根据下标 index 返回对应遵守并实现 `JXCategoryListContentViewDelegate` 协议的列表实例
- (id<JXCategoryListContentViewDelegate>)listContainerView:(JXCategoryListContainerView *)listContainerView initListForIndex:(NSInteger)index {
    VLSelectSongTableItemView *selSongView = [[VLSelectSongTableItemView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)
                                                                                    withRooNo:self.roomNo
                                                                                     ifChorus:self.ifChorus];
    [selSongView loadDatasWithIndex:index+1 ifRefresh:YES];
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
