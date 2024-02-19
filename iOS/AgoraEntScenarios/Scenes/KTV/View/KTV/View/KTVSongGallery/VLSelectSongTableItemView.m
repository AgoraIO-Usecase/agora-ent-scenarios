//
//  VLSelectSongTableItemView.m
//  VoiceOnLine
//

#import "VLSelectSongTableItemView.h"
#import "VLSelectedSongListCell.h"
#import "VLSongItmModel.h"
#import "VLMacroDefine.h"
#import "VLURLPathConfig.h"
#import "VLUserCenter.h"
#import "VLToast.h"
#import "AppContext+KTV.h"
#import "AESMacro.h"
#import "NSString+Helper.h"
@import MJRefresh;

@interface VLSelectSongTableItemView ()<
UITableViewDataSource,
UITableViewDelegate
>
@property (nonatomic, strong) UITableView    *tableView;
@property (nonatomic, strong) NSMutableArray *songsMuArray;
@property (nonatomic, assign) NSInteger page;

@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, assign) BOOL ifChorus;
@property (nonatomic, assign) NSInteger pageType;

@end

@implementation VLSelectSongTableItemView

- (void)setSelSongsArray:(NSArray *)selSongsArray {
    _selSongsArray = selSongsArray;
    
    [self calcSelectedStatus];
    [self.tableView reloadData];
}

- (void)dealloc {
}

- (instancetype)initWithFrame:(CGRect)frame
                    withRooNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.page = 1;
        self.roomNo = roomNo;
        self.ifChorus = ifChorus;
        [self setupView];
    }
    return self;
}

#pragma mark private method
- (void)setupView {
    self.tableView = [[UITableView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT*0.7-20-22-15-40-40-4-20)];
    self.tableView.dataSource = self;
    self.tableView.delegate = self;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.tableView.backgroundColor = UIColorMakeWithHex(@"#152164");
    [self addSubview:self.tableView];
    
    VL(weakSelf);
    self.tableView.mj_header = [MJRefreshNormalHeader headerWithRefreshingBlock:^{
        [weakSelf loadDatasWithIndex:self.pageType ifRefresh:YES];
    }];
    
    self.tableView.mj_footer = [MJRefreshAutoStateFooter footerWithRefreshingBlock:^{
        [weakSelf loadDatasWithIndex:self.pageType ifRefresh:NO];
    }];

}

-(void)loadData {
    [self.tableView.refreshControl beginRefreshing];
    [self loadDatasWithIndex:self.pageType ifRefresh:YES];
}

- (void)calcSelectedStatus {
    for (VLSongItmModel *itemModel in self.songsMuArray) {
        itemModel.ifChoosed = NO;
        for (VLRoomSelSongModel *selModel in self.selSongsArray) {
            if ([itemModel.songNo isEqualToString:selModel.songNo]) {
                itemModel.ifChoosed = YES;
                break;
            }
        }
    }
}

- (void)appendDatasWithSongList:(NSArray<VLSongItmModel*>*)songList {
    [self.tableView.mj_header endRefreshing];
    if (songList.count == 0) {
        return;
    }
    BOOL ifRefresh = self.page == 1 ? YES : NO;
    self.page += 1;
    NSArray *modelsArray = songList;
    if (ifRefresh) {
        [self.songsMuArray removeAllObjects];
        self.songsMuArray = modelsArray.mutableCopy;
    }else{
        for (VLSongItmModel *model in modelsArray) {
            [self.songsMuArray addObject:model];
        }
    }
    

//    [self calcSelectedStatus];
//
//    [self.tableView reloadData];

    [self updateData];
    if (modelsArray.count < 20) {
        [self.tableView.mj_footer endRefreshingWithNoMoreData];
    }else{
        [self.tableView.mj_footer endRefreshing];
    }
}


#pragma mark public method
- (UIView *)listView {
    return self;
}

- (void)loadDatasWithIndex:(NSInteger)pageType
                 ifRefresh:(BOOL)ifRefresh {
    self.pageType = pageType;
    self.page = ifRefresh ? 1 : self.page;
    
    [[AppContext ktvServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        
        self.selSongsArray = songArray;
       
        NSArray* chartIds = @[@(3), @(4), @(2), @(6)];
        NSInteger chartId = [[chartIds objectAtIndex:MIN(pageType - 1, chartIds.count - 1)] intValue];
        NSDictionary *dict = @{
            @"pitchType":@(1),
            @"needLyric": @(YES),
        };
        NSString *extra = [NSString convertToJsonData:dict];
        
        [[AppContext shared].ktvAPI searchMusicWithMusicChartId:chartId
                                                           page:self.page
                                                       pageSize:20
                                                     jsonOption:extra
                                                     completion:^(NSString * requestId, AgoraMusicContentCenterStatusCode status, AgoraMusicCollection * result) {
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
    }];
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
    if(model == nil || model.songNo == nil || model.songName == nil ) {
        [VLToast toast:KTVLocalizedString(@"ktv_chooseSong_failed")];
        return;
    }
    
    model.ifChorus = self.ifChorus;
    
    KTVChooseSongInputModel* inputModel = [KTVChooseSongInputModel new];
    inputModel.isChorus = model.ifChorus;
    inputModel.songName = model.songName;
    inputModel.songNo = model.songNo;
//    inputModel.songUrl = model.songUrl;
    inputModel.imageUrl = model.imageUrl;
    inputModel.singer = model.singer;
    [[AppContext ktvServiceImp] chooseSongWithInputModel:inputModel
                                              completion:^(NSError * error) {
        if (error != nil) {
            [self dianGeFailedWithModel:model];
            [VLToast toast:KTVLocalizedString(@"ktv_choose_fail") duration:2];
            return;
        }
        //点歌完成发送通知
        [self dianGeSuccessWithModel:model];
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

//更新别人点的歌曲状态
- (void)setSelSongArrayWith:(NSArray *)array {
    self.selSongsArray = array;
    [self updateData];
}

-(void)updateData  {
    for (VLSongItmModel *itemModel in self.songsMuArray) {
        for (VLRoomSelSongModel *selModel in self.selSongsArray) {
            if ([itemModel.songNo isEqualToString:selModel.songNo]) {
                itemModel.ifChoosed = YES;
            }
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
