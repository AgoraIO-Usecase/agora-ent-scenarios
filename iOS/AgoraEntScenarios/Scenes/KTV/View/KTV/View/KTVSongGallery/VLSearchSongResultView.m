//
//  VLSearchSongResultView.m
//  VoiceOnLine
//

#import "VLSearchSongResultView.h"
#import "VLSelectedSongListCell.h"
#import "VLSongItmModel.h"
#import "VLURLPathConfig.h"
#import "AppContext+KTV.h"
#import "NSString+Helper.h"
@import MJRefresh;
@import AgoraCommon;
@interface VLSearchSongResultView()<
UITableViewDataSource,
UITableViewDelegate
>

@property(nonatomic, weak) id <VLSearchSongResultViewDelegate>delegate;
@property (nonatomic, strong) UILabel *emptyLabel;
@property (nonatomic, strong) UITableView  *tableView;
@property (nonatomic, strong) NSMutableArray *songsMuArray;
@property (nonatomic, assign) NSInteger        page;
@property (nonatomic, copy) NSString *keyWord;
@property (nonatomic, copy) NSString *roomNo;

@end

@implementation VLSearchSongResultView

- (void)dealloc {
}

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSearchSongResultViewDelegate>)delegate
                   withRoomNo:(nonnull NSString *)roomNo{
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.roomNo = roomNo;
        self.delegate = delegate;
        [self setupView];
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
    [self.tableView.refreshControl endRefreshing];
    self.page = ifRefresh ? 1 : self.page;
    self.keyWord = keyWord;
    
    NSString *jsonOption = @"{\"needLyric\":true,\"pitchType\":2}";
    if ([AppContext shared].isDeveloperMode) {
        jsonOption = @"{\"pitchType\":1,\"needLyric\":true}";
    }
    [[AppContext shared].ktvAPI searchMusicWithKeyword:keyWord ? keyWord : @""
                                                  page:self.page
                                              pageSize:5
                                            jsonOption:jsonOption
                                            completion:^(NSString * requestId, AgoraMusicContentCenterStateReason reason, AgoraMusicCollection * result) {
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
    }];
}

- (void)setupView{
//    self.emptyResultView = [UITextView alloc] initWithFrame:<#(CGRect)#>
//    _cLabel = [[UILabel alloc] init];
//    _cLabel.font = VLUIFontMake(13);
//    _cLabel.textColor = [UIColor whiteColor];
//
    self.emptyLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 60, SCREEN_WIDTH, 30)];
    self.emptyLabel.font = [UIFont systemFontOfSize:13];
    self.emptyLabel.textColor = [UIColor colorWithHexString:@"#979CBB"];
    self.emptyLabel.text = KTVLocalizedString(@"ktv_empty_search");
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

-(void)loadData {
    [self.tableView.refreshControl beginRefreshing];
    [self loadSearchDataWithKeyWord:self.keyWord ifRefresh:YES];
}

#pragma mark -- UITableViewDataSource UITableViewDelegate
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.songsMuArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    VL(weakSelf);
    static NSString *reuseCell = @"reuse";
    VLSelectedSongListCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseCell];
    if (cell == nil) {
        cell = [[VLSelectedSongListCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseCell];
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
    inputModel.songName = model.songName;
    inputModel.songNo = model.songNo;
    inputModel.imageUrl = model.imageUrl;
//    inputModel.songUrl = model.songUrl;
    inputModel.singer = model.singer;
    [[AppContext ktvServiceImp] chooseSongWithInputModel:inputModel
                                              completion:^(NSError * error) {
        if (error != nil) {
            [VLToast toast: error.localizedDescription];
            return;
        }
        
        if (error.code == 0) {
            //点歌完成发送通知
            [self dianGeSuccessWithModel:model];
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
@end
