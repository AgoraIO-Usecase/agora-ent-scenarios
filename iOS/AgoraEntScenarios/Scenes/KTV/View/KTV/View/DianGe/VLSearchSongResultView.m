//
//  VLSearchSongResultView.m
//  VoiceOnLine
//

#import "VLSearchSongResultView.h"
#import "VLSelectSongTCell.h"
#import "VLSongItmModel.h"
#import "VLAPIRequest.h"
#import "VLURLPathConfig.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "VLUserCenter.h"
#import "AppContext+KTV.h"
#import "KTVMacro.h"
@import QMUIKit;
@import MJRefresh;
@import YYCategories;

@interface VLSearchSongResultView()<
UITableViewDataSource,
UITableViewDelegate,
AgoraMusicContentCenterEventDelegate
>

@property(nonatomic, weak) id <VLSearchSongResultViewDelegate>delegate;
@property (nonatomic, strong) UILabel *emptyLabel;
@property (nonatomic, strong) UITableView  *tableView;
@property (nonatomic, strong) NSMutableArray *songsMuArray;
@property (nonatomic, assign) NSInteger        page;
@property (nonatomic, copy) NSString *keyWord;
@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, assign) BOOL ifChorus;

@property (nonatomic, copy) NSString* requestId;

@end

@implementation VLSearchSongResultView

- (void)dealloc {
    [[AppContext shared] unregisterEventDelegate:self];
}

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSearchSongResultViewDelegate>)delegate
                   withRoomNo:(nonnull NSString *)roomNo
                     ifChorus:(BOOL)ifChorus{
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.ifChorus = ifChorus;
        self.roomNo = roomNo;
        self.delegate = delegate;
        [self setupView];
        [[AppContext shared] registerEventDelegate:self];
    }
    return self;
}


- (void)appendDatasWithSongList:(NSArray<VLSongItmModel*>*)songList {
    [self.tableView.mj_header endRefreshing];
    if (songList.count == 0) {
        return;
    }
    BOOL ifRefresh = self.page == 1 ? YES : NO;
    NSArray *tempArray = songList;
    
    if(ifRefresh) {
        if([tempArray count] == 0) {
            self.tableView.hidden = YES;
            self.emptyLabel.hidden = NO;
            return;
        }
        else {
            self.tableView.hidden = NO;
            self.emptyLabel.hidden = YES;
        }
    }
    
    self.page += 1;
    
    NSArray *modelsArray = tempArray;
    if (ifRefresh) {
        [self.songsMuArray removeAllObjects];
        self.songsMuArray = modelsArray.mutableCopy;
        if (modelsArray.count > 0) {
            self.tableView.mj_footer.hidden = NO;
        }else{
            self.tableView.mj_footer.hidden = YES;
        }
    }else{
        for (VLSongItmModel *model in modelsArray) {
            [self.songsMuArray addObject:model];
        }
    }
    [self.tableView reloadData];
    if (modelsArray.count < 5) {
        [self.tableView.mj_footer endRefreshingWithNoMoreData];
    }else{
        [self.tableView.mj_footer endRefreshing];
    }
}


- (void)loadSearchDataWithKeyWord:(NSString *)keyWord ifRefresh:(BOOL)ifRefresh {
    self.page = ifRefresh ? 1 : self.page;
    self.keyWord = keyWord;
    
    self.requestId =
    [[AppContext shared].agoraMcc searchMusicWithKeyWord:keyWord ? keyWord : @""
                                                    page:self.page
                                                pageSize:5
                                              jsonOption:nil];
}

- (void)setupView{
//    self.emptyResultView = [UITextView alloc] initWithFrame:<#(CGRect)#>
//    _cLabel = [[UILabel alloc] init];
//    _cLabel.font = VLUIFontMake(13);
//    _cLabel.textColor = [UIColor whiteColor];
//
    self.emptyLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 60, SCREEN_WIDTH, 30)];
    self.emptyLabel.font = VLUIFontMake(13);
    self.emptyLabel.textColor = [UIColor colorWithHexString:@"#979CBB"];
    self.emptyLabel.text = KTVLocalizedString(@"未找到相关结果");
    self.emptyLabel.textAlignment = NSTextAlignmentCenter;
    [self addSubview:self.emptyLabel];
    self.emptyLabel.hidden = YES;
    
    
    self.tableView = [[UITableView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, self.height)];
    self.tableView.dataSource = self;
    self.tableView.delegate = self;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.tableView.backgroundColor = UIColorMakeWithHex(@"#152164");
    [self addSubview:self.tableView];
    self.tableView.hidden = NO;
    
    VL(weakSelf);
    self.tableView.mj_header = [MJRefreshNormalHeader headerWithRefreshingBlock:^{
        [weakSelf loadSearchDataWithKeyWord:self.keyWord ifRefresh:YES];
    }];
    
    self.tableView.mj_footer = [MJRefreshAutoStateFooter footerWithRefreshingBlock:^{
        [weakSelf loadSearchDataWithKeyWord:self.keyWord ifRefresh:NO];
    }];
}

#pragma mark -- UITableViewDataSource UITableViewDelegate
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.songsMuArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    VL(weakSelf);
    static NSString *reuseCell = @"reuse";
    VLSelectSongTCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseCell];
    if (cell == nil) {
        cell = [[VLSelectSongTCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseCell];
    }
    cell.songItemModel = self.songsMuArray[indexPath.row];
    cell.dianGeBtnClickBlock = ^(VLSongItmModel * _Nonnull model) {
        [weakSelf dianGeWithModel:model];
    };
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 78;
}

- (void)dianGeWithModel:(VLSongItmModel*)model {
    KTVChooseSongInputModel* inputModel = [KTVChooseSongInputModel new];
    inputModel.isChorus = self.ifChorus;
    inputModel.songName = model.songName;
    inputModel.songNo = model.songNo;
//    inputModel.songUrl = model.songUrl;
    inputModel.singer = model.singer;
    [[AppContext ktvServiceImp] chooseSongWithInput:inputModel
                                         completion:^(NSError * error) {
        if (error != nil) {
            return;
        }
        
        if (error.code == 0) {
            //点歌完成发送通知
            [self dianGeSuccessWithModel:model];
            
            [[NSNotificationCenter defaultCenter]postNotificationName:kDianGeSuccessNotification object:model];
        }
        else {
            [self dianGeFailedWithModel:model];
        }
    }];
}

- (void)dianGeFailedWithModel:(VLSongItmModel *)songItemModel {
    for (VLSongItmModel *model in self.songsMuArray) {
        if (songItemModel.songNo == model.songNo) {
            model.ifChoosed = NO;
        }
    }
    [self.tableView reloadData];
}

- (void)dianGeSuccessWithModel:(VLSongItmModel *)songItemModel {
    for (VLSongItmModel *model in self.songsMuArray) {
        if (songItemModel.songNo == model.songNo) {
            model.ifChoosed = YES;
        }
    }
    [self.tableView reloadData];
}

- (NSMutableArray *)songsMuArray {
    if (!_songsMuArray) {
        _songsMuArray = [NSMutableArray array];
    }
    return _songsMuArray;
}


#pragma mark AgoraMusicContentCenterEventDelegate
- (void)onMusicChartsResult:(NSString *)requestId
                     status:(AgoraMusicContentCenterStatusCode)status
                     result:(NSArray<AgoraMusicChartInfo*> *)result {
    if (![self.requestId isEqualToString:requestId]) {
        return;
    }
}

- (void)onMusicCollectionResult:(NSString *)requestId
                         status:(AgoraMusicContentCenterStatusCode)status
                         result:(AgoraMusicCollection *)result {
    if (![self.requestId isEqualToString:requestId]) {
        return;
    }
    
    NSMutableArray* songArray = [NSMutableArray array];
    [result.musicList enumerateObjectsUsingBlock:^(AgoraMusic * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        VLSongItmModel* model = [VLSongItmModel new];
        model.songNo = [NSString stringWithFormat:@"%ld", obj.songCode];
        model.songName = obj.name;
        model.singer = obj.singer;
        model.imageUrl = obj.poster;
        [songArray addObject:model];
    }];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self appendDatasWithSongList:songArray];
    });
}

- (void)onLyricResult:(NSString*)requestId
             lyricUrl:(NSString*)lyricUrl {
    if (![self.requestId isEqualToString:requestId]) {
        return;
    }
    
    
}

- (void)onPreLoadEvent:(NSInteger)songCode
               percent:(NSInteger)percent
                status:(AgoraMusicContentCenterPreloadStatus)status
                   msg:(NSString *)msg
              lyricUrl:(NSString *)lyricUrl {

}
@end
