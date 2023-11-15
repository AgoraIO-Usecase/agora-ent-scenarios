//
//  VLChoosedSongView.m
//  VoiceOnLine
//

#import "VLSongList.h"
#import "VLSongListCell.h"
#import "VLUserCenter.h"
#import "VLMacroDefine.h"
#import "VLURLPathConfig.h"
#import "AppContext+DHCKTV.h"
#import "AESMacro.h"

@interface VLSongList ()<UITableViewDataSource,UITableViewDelegate>

@property(nonatomic, weak) id <VLSongListDelegate>delegate;

@property (nonatomic, strong) UITableView  *tableView;

@end

@implementation VLSongList

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSongListDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    self.tableView = [[UITableView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, self.height)];
    self.tableView.dataSource = self;
    self.tableView.delegate = self;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.tableView.backgroundColor = UIColorMakeWithHex(@"#152164");
    [self addSubview:self.tableView];
    
}

#pragma mark -- UITableViewDataSource UITableViewDelegate
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.selSongsArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    VL(weakSelf);
    static NSString *reuseCell = @"reuse";
    VLSongListCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseCell];
    if (cell == nil) {
        cell = [[VLSongListCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseCell];
    }
    cell.selSongModel = self.selSongsArray[indexPath.row];
    cell.numberLabel.text = [NSString stringWithFormat:@"%d",(int)(indexPath.row+1)];
    cell.sortBtnClickBlock = ^(VLRoomSelSongModel * _Nonnull model) {
        if (model.status == VLSongPlayStatusPlaying) {
            return;
        }
        if (VLUserCenter.user.ifMaster) {
            [weakSelf sortSongEvent:model];
        }
    };
    cell.deleteBtnClickBlock = ^(VLRoomSelSongModel * _Nonnull model) {
        if (model.status == VLSongPlayStatusPlaying) {
            return;
        }
        if (VLUserCenter.user.ifMaster || [VLUserCenter.user.id isEqualToString:cell.selSongModel.userNo]) {
            [weakSelf deleteSongEvent:model];
        }
        
    };
    
    if(VLUserCenter.user.ifMaster) {
        if(indexPath.row == 0 || indexPath.row == 1) {
            cell.sortBtn.hidden = YES;
        }
        else {
            cell.sortBtn.hidden = NO;
        }
    }
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 76;
}


- (void)sortSongEvent:(VLRoomSelSongModel *)model {
    
    KTVMakeSongTopInputModel* inputModel = [KTVMakeSongTopInputModel new];
    inputModel.songNo = model.songNo;
    inputModel.objectId = model.objectId;
    [[AppContext ktvServiceImp] pinSongWith:inputModel
                                      completion:^(NSError * error) {
    }];
}

- (void)deleteSongEvent:(VLRoomSelSongModel *)model {
    KTVRemoveSongInputModel* inputModel = [KTVRemoveSongInputModel new];
    inputModel.songNo = model.songNo;
    inputModel.objectId = model.objectId;
    [[AppContext ktvServiceImp] removeSongWith:inputModel
                                         completion:^(NSError * error) {
        if (error != nil) {
            return;
        }
    }];
}

- (void)setSelSongsArray:(NSArray *)selSongsArray {
    _selSongsArray = selSongsArray;
    [self.tableView reloadData];
}

@end
