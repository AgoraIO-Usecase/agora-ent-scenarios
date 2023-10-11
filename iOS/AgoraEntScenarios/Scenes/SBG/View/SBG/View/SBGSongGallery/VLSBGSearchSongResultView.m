//
//  VLSearchSongResultView.m
//  VoiceOnLine
//

#import "VLSBGSearchSongResultView.h"
#import "VLSBGSelectedSongListCell.h"
#import "VLSBGSongItmModel.h"
#import "VLURLPathConfig.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "VLUserCenter.h"
#import "AppContext+SBG.h"
#import "SBGMacro.h"
#import "NSString+Helper.h"
#import "VLToast.h"
@import MJRefresh;

@interface VLSBGSearchSongResultView()<
UITableViewDataSource,
UITableViewDelegate
>

@property(nonatomic, weak) id <VLSBGSearchSongResultViewDelegate>delegate;
@property (nonatomic, strong) UILabel *emptyLabel;
@property (nonatomic, strong) UITableView  *tableView;
@property (nonatomic, strong) NSMutableArray *songsMuArray;
@property (nonatomic, assign) NSInteger        page;
@property (nonatomic, copy) NSString *keyWord;
@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, assign) BOOL ifChorus;
@property (nonatomic, assign) BOOL isFull;
@property (nonatomic, assign) BOOL isTaped;
@end

@implementation VLSBGSearchSongResultView

- (void)dealloc {
}

- (void)setSelSongsArray:(NSArray *)selSongsArray {
    _selSongsArray = selSongsArray;
    self.isFull = selSongsArray.count >= 8;
    [self.tableView reloadData];
}

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSBGSearchSongResultViewDelegate>)delegate
                   withRoomNo:(nonnull NSString *)roomNo
                     ifChorus:(BOOL)ifChorus{
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.ifChorus = ifChorus;
        self.roomNo = roomNo;
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}


- (void)appendDatasWithSongList:(NSArray<VLSBGSongItmModel*>*)songList {
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
        for (VLSBGSongItmModel *model in modelsArray) {
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
    
    NSDictionary *dict = @{
        @"pitchType":@(1),
        @"needHighPart": @(YES),
    };
    NSString *extra = [NSString convertToJsonData:dict];
    
    [[AppContext shared].sbgAPI searchMusicWithKeyword:keyWord ? keyWord : @""
                                                  page:self.page
                                              pageSize:5
                                            jsonOption:extra
                                            completion:^(NSString * requestId, AgoraMusicContentCenterStatusCode status, AgoraMusicCollection * result) {
        NSMutableArray* songArray = [NSMutableArray array];
        [result.musicList enumerateObjectsUsingBlock:^(AgoraMusic * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            VLSBGSongItmModel* model = [VLSBGSongItmModel new];
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
    self.emptyLabel.font = VLUIFontMake(13);
    self.emptyLabel.textColor = [UIColor colorWithHexString:@"#979CBB"];
    self.emptyLabel.text = SBGLocalizedString(@"未找到相关结果");
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
    VLSBGSelectedSongListCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseCell];
    if (cell == nil) {
        cell = [[VLSBGSelectedSongListCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseCell];
    }
    cell.userInteractionEnabled = !self.isTaped;
    VLSBGSongItmModel *model = self.songsMuArray[indexPath.row];
    model.isFull = self.isFull;
    cell.songItemModel = model;
    cell.dianGeBtnClickBlock = ^(VLSBGSongItmModel * _Nonnull model) {
        weakSelf.isTaped = true;
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(300 * NSEC_PER_MSEC)), dispatch_get_main_queue(), ^{
                weakSelf.isTaped = false;
            });
        [weakSelf dianGeWithModel:model];
    };
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 78;
}

- (void)dianGeWithModel:(VLSBGSongItmModel*)model {
    SBGChooseSongInputModel* inputModel = [SBGChooseSongInputModel new];
    inputModel.isChorus = self.ifChorus;
    inputModel.songName = model.songName;
    inputModel.songNo = model.songNo;
//    inputModel.songUrl = model.songUrl;
    inputModel.singer = model.singer;
    [[AppContext sbgServiceImp] chooseSongWithInput:inputModel
                                         completion:^(NSError * error) {
        if (error != nil) {
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

- (void)dianGeFailedWithModel:(VLSBGSongItmModel *)songItemModel {
    for (VLSBGSongItmModel *model in self.songsMuArray) {
        if (songItemModel.songNo == model.songNo) {
            model.ifChoosed = NO;
        }
    }
    [self.tableView reloadData];
}

- (void)dianGeSuccessWithModel:(VLSBGSongItmModel *)songItemModel {
    for (VLSBGSongItmModel *model in self.songsMuArray) {
        if (songItemModel.songNo == model.songNo) {
            model.ifChoosed = YES;
        }
    }
    if(self.selSongsArray.count == 8){
        [VLToast toast:@"歌曲已满，开始游戏吧"];
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
