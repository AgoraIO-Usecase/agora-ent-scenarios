//
//  VLHomeOnLineListView.m
//  VoiceOnLine
//

#import "VLSBGHomeOnLineListView.h"
#import "VLSBGHomeOnLineListCCell.h"
#import "VLSBGRoomListModel.h"
#import "VLSBGListEmptyView.h"
#import "VLMacroDefine.h"
#import "VLURLPathConfig.h"
#import "SBGMacro.h"
#import "AppContext+SBG.h"
@import MJRefresh;

@interface VLSBGHomeOnLineListView ()<UICollectionViewDataSource,UICollectionViewDelegate>

@property(nonatomic, weak) id <VLSBGHomeOnLineListViewDelegate>delegate;

@property (nonatomic, strong) NSMutableArray *roomListModeArray;
@property (nonatomic, strong) UIButton *createBtn;
//@property (nonatomic, strong) NSArray *roomListArray;

@property (nonatomic, assign) NSInteger        page;
@property (nonatomic, strong) VLSBGListEmptyView *emptyView;

@end

@implementation VLSBGHomeOnLineListView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGHomeOnLineListViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    VL(weakSelf);
    [self addSubview:self.emptyView];
    [self addSubview:self.listCollectionView];
    [self addSubview:self.createBtn];
    
    self.listCollectionView.mj_header = [MJRefreshNormalHeader headerWithRefreshingBlock:^{
        [weakSelf getRoomListIfRefresh:YES];
    }];
    
    self.listCollectionView.mj_footer = [MJRefreshAutoStateFooter footerWithRefreshingBlock:^{
        [weakSelf getRoomListIfRefresh:NO];
    }];
}

-(void)loadData {
    [self.listCollectionView.refreshControl beginRefreshing];
    [self getRoomListIfRefresh:YES];
}

- (void)getRoomListIfRefresh:(BOOL)ifRefresh {
    self.page = ifRefresh ? 0 : self.page;
    
    [[AppContext sbgServiceImp] getRoomListWithPage:self.page
                                         completion:^(NSError * error, NSArray<VLSBGRoomListModel *> * roomArray) {
        if (error != nil) {
            [self.listCollectionView.mj_header endRefreshing];
            [self.listCollectionView.mj_footer endRefreshing];
            return;
        }
        
        [self.listCollectionView.mj_header endRefreshing];
        self.page += 1;
        NSArray *array = roomArray;
        
        if (ifRefresh) {
            [self.roomListModeArray removeAllObjects];
            NSMutableArray *filteredArray = [NSMutableArray arrayWithArray:array.mutableCopy];
            NSPredicate *predicate = [NSPredicate predicateWithFormat:@"objectId != nil"];
            [filteredArray filterUsingPredicate:predicate];
            self.roomListModeArray = filteredArray;
            if (array.count > 0) {
                self.listCollectionView.mj_footer.hidden = NO;
            }else{
                self.listCollectionView.mj_footer.hidden = YES;
            }
        }else{
            for (VLSBGRoomListModel *model in array) {
                [self.roomListModeArray addObject:model];
            }
        }
        [self.listCollectionView reloadData];
        if (array.count < 10) {
            [self.listCollectionView.mj_footer endRefreshing];
            self.listCollectionView.mj_footer.hidden = YES;
        }else{
            [self.listCollectionView.mj_footer endRefreshing];
        }
        if(self.roomListModeArray.count > 0) {
            self.emptyView.hidden = YES;
        }
        else {
            self.emptyView.hidden = NO;
        }
    }];
}

//- (void)exitRoomEvent {
//    [self getRoomListIfRefresh:YES];
//}

#pragma mark - UITableViewDelegate,UITableViewDataSource
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.roomListModeArray.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
  //  VL(weakSelf);
    VLSBGHomeOnLineListCCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:[VLSBGHomeOnLineListCCell className] forIndexPath:indexPath];
    cell.bgImgView.image = [UIImage sbg_sceneImageWithName:[NSString stringWithFormat:@"create_bg_%li" ,indexPath.row % 5]];
    cell.listModel = self.roomListModeArray[indexPath.row];
    return cell;
}

static long lastClickTime = 0;
static const int INTERVAL = 1000; // 时间间隔为1秒
- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath{
    VLSBGRoomListModel *listModel = self.roomListModeArray[indexPath.row];
    if (self.delegate && [self.delegate respondsToSelector:@selector(listItemClickAction:)]) {
        long currentTime = [[NSDate date] timeIntervalSince1970] * 1000;
        if (currentTime - lastClickTime > INTERVAL) { // 执行点击事件
            [self.delegate listItemClickAction:listModel];
            lastClickTime = currentTime;
        }
    }
}


- (void)joinBtnClickAction:(VLSBGRoomListModel *)model {
    
}

#pragma mark - Event
- (void)createBtnClickEvent {
    if (self.delegate && [self.delegate respondsToSelector:@selector(createBtnAction)]) {
        [self.delegate createBtnAction];
    }
}

- (UICollectionView *)listCollectionView {
    if (!_listCollectionView) {
        UICollectionViewFlowLayout *flowLayOut = [[UICollectionViewFlowLayout alloc]init];
        flowLayOut.scrollDirection = UICollectionViewScrollDirectionVertical;
        CGFloat middleMargin = 15;
        CGFloat itemW = (SCREEN_WIDTH-40-middleMargin)/2.0;
        CGFloat itemH = itemW;
        flowLayOut.itemSize = CGSizeMake(itemW, itemH);
        flowLayOut.minimumInteritemSpacing = middleMargin;
        flowLayOut.minimumLineSpacing = 20;
        
        _listCollectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(0, 22, SCREEN_WIDTH, SCREEN_HEIGHT-kTopNavHeight-34-48-kSafeAreaBottomHeight) collectionViewLayout:flowLayOut];
        _listCollectionView.dataSource = self;
        _listCollectionView.delegate = self;
        _listCollectionView.alwaysBounceVertical = true;
        _listCollectionView.showsHorizontalScrollIndicator = false;
        _listCollectionView.showsVerticalScrollIndicator = false;
        _listCollectionView.backgroundColor = UIColorClear;
        _listCollectionView.contentInset = UIEdgeInsetsMake(0, 20, 30, 20);
        if (@available(iOS 11, *)) {
            _listCollectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
        }
        [_listCollectionView registerClass:[VLSBGHomeOnLineListCCell class] forCellWithReuseIdentifier:[VLSBGHomeOnLineListCCell className]];

    }
    return _listCollectionView;
}

- (UIButton *)createBtn {
    if (!_createBtn) {
        _createBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _createBtn.accessibilityIdentifier = @"ktv_create_button_id";
        [_createBtn setBackgroundImage:[UIImage sbg_sceneImageWithName:@"create_room"] forState:UIControlStateNormal];
        
        _createBtn.frame = CGRectMake((SCREEN_WIDTH-148)*0.5, SCREEN_HEIGHT-34-kSafeAreaBottomHeight-56-kTopNavHeight, 148, 56);
        _createBtn.imageView.contentMode = UIViewContentModeScaleAspectFit;
        _createBtn.spacingBetweenImageAndTitle = 7;
        _createBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
        [_createBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
        _createBtn.titleLabel.font = UIFontBoldMake(18.0);
        [_createBtn addTarget:self action:@selector(createBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    }
    return _createBtn;
}

- (NSMutableArray *)roomListModeArray{
    if (!_roomListModeArray) {
        _roomListModeArray = [NSMutableArray array];
    }
    return _roomListModeArray;
}

- (VLSBGListEmptyView *)emptyView {
    if (!_emptyView) {
        _emptyView = [[VLSBGListEmptyView alloc]initWithFrame:CGRectMake(0, 22+40,SCREEN_WIDTH, 90+VLREALVALUE_WIDTH(30)+202)];
        _emptyView.hidden = YES;
    }
    return _emptyView;
}

@end
