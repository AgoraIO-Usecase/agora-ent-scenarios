//
//  VLChoosedSongView.m
//  VoiceOnLine
//

#import "VLSongList.h"
#import "VLSongListCell.h"
#import "VLUserCenter.h"
#import "VLMacroDefine.h"
#import "AESMacro.h"
#import "AppContext+DHCKTV.h"

@interface VLSongList ()<UITableViewDataSource,UITableViewDelegate>

@property (nonatomic, strong) UITableView  *tableView;

@property (nonatomic, strong) NSArray *selSongsArray;
@property (nonatomic, assign) BOOL isOwner;

@end

@implementation VLSongList

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        [self setupView];
    }
    return self;
}

- (void)setSelSongsArray:(NSArray *)selSongsArray isOwner:(BOOL)isOwner {
    self.selSongsArray = selSongsArray;
    self.isOwner = isOwner;
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
    VLRoomSelSongModel* selSongModel = self.selSongsArray[indexPath.row];
    [cell setSelSongModel:selSongModel isOwner:self.isOwner];
    cell.numberLabel.text = [NSString stringWithFormat:@"%d",(int)(indexPath.row+1)];
    cell.sortBtnClickBlock = ^(VLRoomSelSongModel * _Nonnull model) {
        if (model.status == VLSongPlayStatusPlaying) {
            return;
        }
        if (self.isOwner) {
            [weakSelf sortSongEvent:model];
        }
    };
    cell.deleteBtnClickBlock = ^(VLRoomSelSongModel * _Nonnull model) {
        if (model.status == VLSongPlayStatusPlaying) {
            return;
        }
        if (self.isOwner || [VLUserCenter.user.id isEqualToString:selSongModel.userNo]) {
            [weakSelf deleteSongEvent:model];
        }
    };
    
    if(self.isOwner) {
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
    KTVMakeSongTopInputModel *inputModel = [[KTVMakeSongTopInputModel alloc] init];
    inputModel.songNo = model.songNo;
    inputModel.objectId = model.objectId;
    [[AppContext dhcServiceImp] pinSongWith:inputModel completion:^(NSError * _Nullable) {
        //
    }];
}

- (void)deleteSongEvent:(VLRoomSelSongModel *)model {
    KTVRemoveSongInputModel *inputModel = [[KTVRemoveSongInputModel alloc] init];
    inputModel.songNo = model.songNo;
    inputModel.objectId = model.objectId;
    [[AppContext dhcServiceImp] removeSongWith:inputModel completion:^(NSError * _Nullable error) {
        if(error == nil) {return;}
        [VLToast toast:[error localizedDescription]];
    }];
}

- (void)setSelSongsArray:(NSArray *)selSongsArray {
    _selSongsArray = selSongsArray;
    [self.tableView reloadData];
}

@end
