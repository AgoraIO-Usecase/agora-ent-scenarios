//
//  VLHomeOnLineListView.m
//  VoiceOnLine
//

#import "VLHomeOnLineListView.h"
#import "VLHomeOnLineListCCell.h"
#import "VLRoomListModel.h"
#import "VLListEmptyView.h"
#import "VLMacroDefine.h"
#import "VLURLPathConfig.h"
#import "KTVMacro.h"
#import "AppContext+KTV.h"

@interface VLHomeOnLineListView ()<UICollectionViewDataSource,UICollectionViewDelegate>

@property(nonatomic, weak) id <VLHomeOnLineListViewDelegate>delegate;

@property (nonatomic, strong) NSMutableArray *roomListModeArray;
@property (nonatomic, strong) UIButton *createBtn;
//@property (nonatomic, strong) NSArray *roomListArray;

@property (nonatomic, assign) NSInteger        page;
@property (nonatomic, strong) VLListEmptyView *emptyView;
@property (nonatomic, strong) UIRefreshControl *refreshControl;

@end

@implementation VLHomeOnLineListView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLHomeOnLineListViewDelegate>)delegate {
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
    
    _refreshControl = [[UIRefreshControl alloc]init];
    self.listCollectionView.refreshControl = _refreshControl;
    [_refreshControl addTarget:self action:@selector(loadData) forControlEvents:UIControlEventValueChanged];

}

-(void)loadData {
    [self.listCollectionView.refreshControl beginRefreshing];
    [self getRoomListIfRefresh:YES];
}

- (void)getRoomListIfRefresh:(BOOL)ifRefresh {
    self.page = ifRefresh ? 0 : self.page;
    
    [[AppContext ktvServiceImp] getRoomListWithPage:self.page
                                         completion:^(NSError * error, NSArray<VLRoomListModel *> * roomArray) {
        if (error != nil) {
            [self.listCollectionView.refreshControl endRefreshing];
            return;
        }
        
        [self.listCollectionView.refreshControl endRefreshing];
        self.page += 1;
        NSArray *array = roomArray;
        
        if (ifRefresh) {
            [self.roomListModeArray removeAllObjects];
            self.roomListModeArray = array.mutableCopy;
        }else{
            for (VLRoomListModel *model in array) {
                [self.roomListModeArray addObject:model];
            }
        }
        [self.listCollectionView reloadData];
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
    VLHomeOnLineListCCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:[VLHomeOnLineListCCell className] forIndexPath:indexPath];
    cell.listModel = self.roomListModeArray[indexPath.row];
    return cell;
}

static long lastClickTime = 0;
static const int INTERVAL = 1000; // 时间间隔为1秒
- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath{
    VLRoomListModel *listModel = self.roomListModeArray[indexPath.row];
    if (self.delegate && [self.delegate respondsToSelector:@selector(listItemClickAction:)]) {
        long currentTime = [[NSDate date] timeIntervalSince1970] * 1000;
        if (currentTime - lastClickTime > INTERVAL) { // 执行点击事件
            [self.delegate listItemClickAction:listModel];
            lastClickTime = currentTime;
        }
    }
}


- (void)joinBtnClickAction:(VLRoomListModel *)model {
    
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
        [_listCollectionView registerClass:[VLHomeOnLineListCCell class] forCellWithReuseIdentifier:[VLHomeOnLineListCCell className]];

    }
    return _listCollectionView;
}

- (UIButton *)createBtn {
    if (!_createBtn) {
        
//        _createBtn = [[QMUIButton alloc] qmui_initWithImage:[UIImage sceneImageWithName:@"online_list_addIcon"]
//                                                      title:KTVLocalizedString(@"创建房间")];
        _createBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        [_createBtn setTitle:KTVLocalizedString(@"创建房间") forState:UIControlStateNormal];
        [_createBtn setImage:[UIImage sceneImageWithName:@"online_list_addIcon"] forState:UIControlStateNormal];
        _createBtn.frame = CGRectMake((SCREEN_WIDTH-195)*0.5, SCREEN_HEIGHT-34-kSafeAreaBottomHeight-48-kTopNavHeight, 195, 48);
        _createBtn.layer.cornerRadius = 24;
        _createBtn.layer.masksToBounds = YES;
//        _createBtn.imagePosition = QMUIButtonImagePositionLeft;
        _createBtn.spacingBetweenImageAndTitle = 7;
        _createBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
        [_createBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
        _createBtn.titleLabel.font = UIFontBoldMake(16.0);
//        _createBtn.adjustsButtonWhenHighlighted = NO;
        [_createBtn addTarget:self action:@selector(createBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
        _createBtn.backgroundColor = UIColorMakeWithHex(@"#2753FF");
    }
    return _createBtn;
}

- (NSMutableArray *)roomListModeArray{
    if (!_roomListModeArray) {
        _roomListModeArray = [NSMutableArray array];
    }
    return _roomListModeArray;
}

- (VLListEmptyView *)emptyView {
    if (!_emptyView) {
        _emptyView = [[VLListEmptyView alloc]initWithFrame:CGRectMake(0, 22+40,SCREEN_WIDTH, 90+VLREALVALUE_WIDTH(30)+202)];
        _emptyView.hidden = YES;
    }
    return _emptyView;
}

@end
