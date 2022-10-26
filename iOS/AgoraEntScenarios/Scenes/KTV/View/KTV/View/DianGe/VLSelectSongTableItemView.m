//
//  VLSelectSongTableItemView.m
//  VoiceOnLine
//

#import "VLSelectSongTableItemView.h"
#import "VLSelectSongTCell.h"
#import "VLSongItmModel.h"
#import "VLRoomSelSongModel.h"
#import "VLMacroDefine.h"
#import "VLAPIRequest.h"
#import "VLURLPathConfig.h"
#import "VLUserCenter.h"
#import "VLToast.h"
#import "AppContext+KTV.h"
@import QMUIKit;
@import MJRefresh;

@interface VLSelectSongTableItemView ()<UITableViewDataSource,UITableViewDelegate>

@property (nonatomic, strong) UITableView    *tableView;
@property (nonatomic, strong) NSMutableArray *songsMuArray;
@property (nonatomic, assign) NSInteger        page;

@property (nonatomic, strong) NSArray *selSongsArray;
@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, assign) BOOL ifChorus;
@property (nonatomic, assign) NSInteger pageType;

@end

@implementation VLSelectSongTableItemView


- (instancetype)initWithFrame:(CGRect)frame withRooNo:(NSString *)roomNo ifChorus:(BOOL)ifChorus {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.page = 1;
        self.roomNo = roomNo;
        self.ifChorus = ifChorus;
        [self setupView];
    }
    return self;
}

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


- (UIView *)listView {
    return self;
}

- (void)loadDatasWithIndex:(NSInteger)pageType ifRefresh:(BOOL)ifRefresh{
    self.pageType = pageType;
    self.page = ifRefresh ? 1 : self.page;
    
    [[AppContext ktvServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        
        self.selSongsArray = songArray;
        NSDictionary *param = @{
            @"type":@(pageType),
            @"size":@(20),
            @"current":@(self.page)
        };
        
        [VLAPIRequest getRequestURL:kURLGetSongsList parameter:param showHUD:NO success:^(VLResponseDataModel * _Nonnull response) {
            if (response.code == 0) {
                [self.tableView.mj_header endRefreshing];
                self.page += 1;
                NSArray *tempArray = response.data[@"records"];
                NSArray *modelsArray = [VLSongItmModel vj_modelArrayWithJson:tempArray];
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
                
                for (VLSongItmModel *itemModel in self.songsMuArray) {
                    for (VLRoomSelSongModel *selModel in self.selSongsArray) {
                        if ([itemModel.songNo isEqualToString:selModel.songNo]) {
                            itemModel.ifChoosed = YES;
                        }
                    }
                }
                
                [self.tableView reloadData];
                if (modelsArray.count < 5) {
                    [self.tableView.mj_footer endRefreshingWithNoMoreData];
                }else{
                    [self.tableView.mj_footer endRefreshing];
                }
            }else{
                [self.tableView.mj_header endRefreshing];
            }
            
        } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
            [self.tableView.mj_header endRefreshing];
        }];

    }];
    
//    NSDictionary *param = @{
//        @"roomNo" :self.roomNo
//    };
//
//    [VLAPIRequest getRequestURL:kURLChoosedSongs parameter:param showHUD:NO success:^(VLResponseDataModel * _Nonnull response) {
//        if (response.code == 0) {
//            self.selSongsArray = [VLRoomSelSongModel vj_modelArrayWithJson:response.data];
//            NSDictionary *param = @{
//                @"type":@(pageType),
//                @"size":@(20),
//                @"current":@(self.page)
//            };
//
//            [VLAPIRequest getRequestURL:kURLGetSongsList parameter:param showHUD:NO success:^(VLResponseDataModel * _Nonnull response) {
//                if (response.code == 0) {
//                    [self.tableView.mj_header endRefreshing];
//                    self.page += 1;
//                    NSArray *tempArray = response.data[@"records"];
//                    NSArray *modelsArray = [VLSongItmModel vj_modelArrayWithJson:tempArray];
//                    if (ifRefresh) {
//                        [self.songsMuArray removeAllObjects];
//                        self.songsMuArray = modelsArray.mutableCopy;
//                        if (modelsArray.count > 0) {
//                            self.tableView.mj_footer.hidden = NO;
//                        }else{
//                            self.tableView.mj_footer.hidden = YES;
//                        }
//                    }else{
//                        for (VLSongItmModel *model in modelsArray) {
//                            [self.songsMuArray addObject:model];
//                        }
//                    }
//
//                    for (VLSongItmModel *itemModel in self.songsMuArray) {
//                        for (VLRoomSelSongModel *selModel in self.selSongsArray) {
//                            if ([itemModel.songNo isEqualToString:selModel.songNo]) {
//                                itemModel.ifChoosed = YES;
//                            }
//                        }
//                    }
//
//                    [self.tableView reloadData];
//                    if (modelsArray.count < 5) {
//                        [self.tableView.mj_footer endRefreshingWithNoMoreData];
//                    }else{
//                        [self.tableView.mj_footer endRefreshing];
//                    }
//                }else{
//                    [self.tableView.mj_header endRefreshing];
//                }
//
//            } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
//                [self.tableView.mj_header endRefreshing];
//            }];
//        }
//    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
//
//    }];
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
    if(model == nil || model.songNo == nil || model.songName == nil ) {
        [VLToast toast:NSLocalizedString(@"点歌失败，请重试", nil)];
        return;
    }
    
    model.ifChorus = self.ifChorus;
    
    
    KTVChooseSongInputModel* inputModel = [KTVChooseSongInputModel new];
    inputModel.isChorus = model.ifChorus;
    inputModel.songName = model.songName;
    inputModel.songNo = model.songNo;
    inputModel.songUrl = model.songUrl;
    inputModel.imageUrl = model.imageUrl;
    inputModel.singer = model.singer;
    [[AppContext ktvServiceImp] chooseSongWithInput:inputModel
                                         completion:^(NSError * error) {
        if (error != nil) {
            [self dianGeFailedWithModel:model];
            return;
        }
        //点歌完成发送通知
        [self dianGeSuccessWithModel:model];
        [[NSNotificationCenter defaultCenter]postNotificationName:kDianGeSuccessNotification object:model];
    }];
    
    
//    NSDictionary *param = @{
//        @"isChorus" : @(self.ifChorus),
//        @"roomNo": self.roomNo,
//        @"songName":model.songName,
//        @"songNo":model.songNo,
////        @"songUrl":model.songUrl,
//        @"userNo":VLUserCenter.user.userNo
//    };
//    [VLAPIRequest getRequestURL:kURLChooseSong parameter:param showHUD:NO success:^(VLResponseDataModel * _Nonnull response) {
//        if (response.code == 0) {
//            //点歌完成发送通知
//            [self dianGeSuccessWithModel:model];
//
//            [[NSNotificationCenter defaultCenter]postNotificationName:kDianGeSuccessNotification object:model];
//        }
//        else {
//            [self dianGeFailedWithModel:model];
//        }
//    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
//        [self dianGeFailedWithModel:model];
//    }];
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
