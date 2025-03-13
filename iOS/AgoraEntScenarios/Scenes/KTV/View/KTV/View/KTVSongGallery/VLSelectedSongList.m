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
VLSearchSongResultViewDelegate,
UITextFieldDelegate
>

@property(nonatomic, weak) id <VLSelectedSongListDelegate>delegate;
@property (nonatomic, strong) UIView *bgView;
@property (nonatomic, strong) UITextField *searchTF;
@property (nonatomic, strong) VLHotSpotBtn *cancelButton;
@property (nonatomic, strong) VLSearchSongResultView *resultView;
@property (nonatomic, strong) VLRoomListModel *roomModel;
@property (nonatomic, strong) VLSelectSongTableItemView *selSongView;
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
    
    self.selSongView = [[VLSelectSongTableItemView alloc] initWithFrame:CGRectMake(0, bgView.bottom+4, SCREEN_WIDTH, self.height-bgView.bottom-4)
                                                              withRooNo:self.roomNo];
    [self.selSongView loadDatasWithIndex:0 ifRefresh:YES];
    [self addSubview:self.selSongView];
    self.selSongView.frame = CGRectMake(0, self.searchTF.bottom + 10, SCREEN_WIDTH, SCREEN_HEIGHT*0.7-95);
    
    //搜索结果
    self.resultView = [[VLSearchSongResultView alloc]initWithFrame:CGRectMake(0, bgView.bottom+4, SCREEN_WIDTH, self.height-bgView.bottom-4)
                                                      withDelegate:self
                                                        withRoomNo:self.roomNo];
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

-(void)setSelSongArrayWith:(NSArray *)array {
    [self updateUIWithSelSongsArray:array];
}

-(void)updateUIWithSelSongsArray:(NSArray *)array {
    [self.selSongView setSelSongArrayWith:array];
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
