//
//  VLSelectSongTableItemView.m
//  VoiceOnLine
//

#import "VLSBGSelectSongTableItemView.h"
#import "VLSBGSelectedSongListCell.h"
#import "VLSBGSongItmModel.h"
#import "VLSBGRoomSelSongModel.h"
#import "VLMacroDefine.h"
#import "VLURLPathConfig.h"
#import "AppContext+SBG.h"
#import "SBGMacro.h"
#import "NSString+Helper.h"
@import MJRefresh;
@import AgoraCommon;
@interface VLSBGSelectSongTableItemView ()<
UITableViewDataSource,
UITableViewDelegate
>
@property (nonatomic, strong) UITableView    *tableView;
@property (nonatomic, strong) NSMutableArray *songsMuArray;
@property (nonatomic, assign) NSInteger page;

@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, assign) BOOL ifChorus;
@property (nonatomic, assign) NSInteger pageType;
@property (nonatomic, assign) BOOL isFull;
@property (nonatomic, assign) BOOL isTaped;
@end

@implementation VLSBGSelectSongTableItemView

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
    self.tableView = [[UITableView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, self.bounds.size.height)];
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
    for (VLSBGSongItmModel *itemModel in self.songsMuArray) {
        itemModel.ifChoosed = NO;
        for (VLSBGRoomSelSongModel *selModel in self.selSongsArray) {
            if ([itemModel.songNo isEqualToString:selModel.songNo]) {
                itemModel.ifChoosed = YES;
                break;
            }
        }
    }
    
    self.isFull = self.selSongsArray.count >= 8;
    
//    NSArray *array = self.songsMuArray;
//    for(int i=0;i<array.count; i++){
//        VLSBGSongItmModel *itemModel = array[i];
//        itemModel.isFull = self.selSongsArray.count >= 8;
//        self.songsMuArray[i] = itemModel;
//    }
}

- (void)appendDatasWithSongList:(NSArray<VLSBGSongItmModel*>*)songList {
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
        for (VLSBGSongItmModel *model in modelsArray) {
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
    
    [[AppContext sbgServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLSBGRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        
        self.selSongsArray = songArray;
       
        NSArray* chartIds = @[@(3), @(4), @(2), @(6)];
        NSInteger chartId = [[chartIds objectAtIndex:MIN(pageType - 1, chartIds.count - 1)] intValue];
        NSDictionary *dict = @{
            @"pitchType":@(1),
            @"needHighPart": @(YES),
        };
        NSString *extra = [NSString convertToJsonData:dict];
        
        [[AppContext shared].sbgAPI searchMusicWithMusicChartId:chartId
                                                           page:self.page
                                                       pageSize:20
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
    }];
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

    VLSBGSongItmModel *model = self.songsMuArray[indexPath.row];
    model.isFull = self.isFull;
    cell.songItemModel = model;
    
    // 设置按钮点击事件
    cell.dianGeBtnClickBlock = ^(VLSBGSongItmModel * _Nonnull model) {
        if (weakSelf.isTaped) {
            return;
        }
        
        weakSelf.isTaped = true;
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(500 * NSEC_PER_MSEC)), dispatch_get_main_queue(), ^{
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
    if(model == nil || model.songNo == nil || model.songName == nil ) {
        [VLToast toast:SBGLocalizedString(@"sbg_chooseSong_failed")];
        return;
    }
    
    model.ifChorus = self.ifChorus;
    SBGChooseSongInputModel* inputModel = [SBGChooseSongInputModel new];
    inputModel.isChorus = model.ifChorus;
    inputModel.songName = model.songName;
    inputModel.songNo = model.songNo;
//    inputModel.songUrl = model.songUrl;
    inputModel.imageUrl = model.imageUrl;
    inputModel.singer = model.singer;
    [[AppContext sbgServiceImp] chooseSongWithInput:inputModel
                                         completion:^(NSError * error) {
        if (error != nil) {
            [self dianGeFailedWithModel:model];
            return;
        }
        //点歌完成发送通知
        [self dianGeSuccessWithModel:model];
        if(self.selSongsArray.count == 7){
            [VLToast toast:SBGLocalizedString(@"sbg_full_song")];
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
    [self.tableView reloadData];
}

//更新别人点的歌曲状态
- (void)setSelSongArrayWith:(NSArray *)array {
    self.selSongsArray = array;
    [self updateData];
}

-(void)updateData  {
    for (VLSBGSongItmModel *itemModel in self.songsMuArray) {
        for (VLSBGRoomSelSongModel *selModel in self.selSongsArray) {
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
