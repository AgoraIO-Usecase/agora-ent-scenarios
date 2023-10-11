//
//  VLRoomPersonView.m
//  VoiceOnLine
//

#import "VLSBGMicSeatList.h"
#import "VLSBGMicSeatCell.h"
#import "VLSBGRoomSeatModel.h"
#import "VLSBGRoomSelSongModel.h"
#import "VLMacroDefine.h"
#import "VLUserCenter.h"
#import "SBGMacro.h"
@import YYCategories;
@import SDWebImage;

@interface VLSBGMicSeatList ()<UICollectionViewDataSource,UICollectionViewDelegate>

@property(nonatomic, weak) id <VLSBGMicSeatListDelegate>delegate;

@property (nonatomic, strong) UICollectionView *personCollectionView;
@property (nonatomic, copy) NSString *currentPlayingSongCode;
@property (nonatomic, copy) NSString *winNo;
@end

@implementation VLSBGMicSeatList

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGMicSeatListDelegate>)delegate withRTCkit:(AgoraRtcEngineKit *)RTCkit{
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        self.roomSeatsArray = [[NSArray alloc]init];
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    UICollectionViewFlowLayout *flowLayOut = [[UICollectionViewFlowLayout alloc]init];
    flowLayOut.scrollDirection = UICollectionViewScrollDirectionVertical;
    
    CGFloat itemW = VLREALVALUE_WIDTH(54);
    CGFloat middleMargin = (SCREEN_WIDTH - 40 - 2*27 - 4*itemW)/3.0;
    CGFloat itemH = VLREALVALUE_WIDTH(54)+33;
    flowLayOut.itemSize = CGSizeMake(itemW, itemH);
    flowLayOut.minimumInteritemSpacing = middleMargin;
    flowLayOut.minimumLineSpacing = 15;
    
    self.personCollectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(20, 0, SCREEN_WIDTH - 40, itemH*2+15) collectionViewLayout:flowLayOut];
    self.personCollectionView.dataSource = self;
    self.personCollectionView.delegate = self;
    self.personCollectionView.alwaysBounceVertical = true;
    self.personCollectionView.showsHorizontalScrollIndicator = false;
    self.personCollectionView.showsVerticalScrollIndicator = false;
    self.personCollectionView.backgroundColor = UIColorClear;
    self.personCollectionView.scrollEnabled = NO;
    if (@available(iOS 11, *)) {
        self.personCollectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }
    [self.personCollectionView registerClass:[VLSBGMicSeatCell class] forCellWithReuseIdentifier:[VLSBGMicSeatCell className]];
    [self addSubview:self.personCollectionView];
    
}

- (void)setRoomSeatsArray:(NSArray *)roomSeatsArray {
    _roomSeatsArray = [[NSArray alloc]initWithArray:roomSeatsArray];
//    [self.personCollectionView reloadData];
}

- (void)reloadSeatIndex: (NSUInteger)seatIndex {
    [self.personCollectionView reloadItemsAtIndexPaths:@[[NSIndexPath indexPathForRow:seatIndex inSection:0]]];
}

- (void)updateIfNeeded {
    [self.personCollectionView reloadData];
}

#pragma mark - UITableViewDelegate,UITableViewDataSource
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.roomSeatsArray.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    VLSBGMicSeatCell *cell = nil;
    cell = [collectionView dequeueReusableCellWithReuseIdentifier:[VLSBGMicSeatCell className] forIndexPath:indexPath];
    
    for (UIView *view in cell.videoView.subviews) {
        if (view.tag > viewTag) {
            [view removeFromSuperview];
        }
    }
    VLSBGRoomSeatModel *seatModel = self.roomSeatsArray[indexPath.row];
    
    if (seatModel.name.length > 0) {
        cell.nickNameLabel.text = seatModel.name;
    }else{
        cell.nickNameLabel.text = [NSString stringWithFormat:SBGLocalizedString(@"%d号麦"), (int)indexPath.row + 1];
    }
    if (seatModel.isMaster) {
        cell.avatarImgView.layer.borderWidth = 2.0;
        cell.avatarImgView.layer.borderColor = UIColorMakeWithHex(@"#75ADFF").CGColor;
        cell.roomerImgView.hidden = cell.roomerLabel.hidden = NO;
        cell.nickNameLabel.textColor = UIColorMakeWithHex(@"#DBDAE9");
    }else{
        cell.roomerImgView.hidden = cell.roomerLabel.hidden = YES;
        cell.nickNameLabel.textColor = UIColorMakeWithHex(@"#AEABD0");
        cell.avatarImgView.layer.borderColor = UIColorClear.CGColor;
    }
    cell.roomerLabel.text = SBGLocalizedString(@"房主");
    if (seatModel.headUrl.length > 0) {
        [cell.avatarImgView sd_setImageWithURL:[NSURL URLWithString:seatModel.headUrl]];
    }else{
        cell.avatarImgView.image = [UIImage sceneImageWithName:@"ktv_emptySeat_icon"];
    }
    cell.singingBtn.hidden = !seatModel.isOwner;
    
    cell.muteImgView.hidden = !seatModel.isAudioMuted;
    
    //if([seatModel.chorusSongCode isEqualToString:self.currentPlayingSongCode]){
//    if([self.winNo isEqualToString:seatModel.userNo]){
//        cell.joinChorusBtn.hidden = NO;
//    } else {
//        cell.joinChorusBtn.hidden = YES;
//    }
    
    if (seatModel.rtcUid == nil) {
        cell.muteImgView.hidden = YES;
        cell.singingBtn.hidden = YES;
    }
    
    //only display when rtcUid exists (on mic seat), and video is not muted
    cell.videoView.hidden = !(seatModel.rtcUid != nil && !seatModel.isVideoMuted);
    //avatar or camera will only be displayed 1 at atime
    cell.avatarImgView.hidden = !cell.videoView.isHidden;
    if (!seatModel.isVideoMuted && seatModel.rtcUid != nil) { //开启了视频
        [self.delegate onVLRoomPersonView:self onRenderVideo:seatModel inView:cell.videoView atIndex:indexPath.row];
    }
    
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    VLSBGRoomSeatModel *roomSeatModel = self.roomSeatsArray[indexPath.row];
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLRoomPersonView:seatItemTappedWithModel:atIndex:)]) {
        [self.delegate onVLRoomPersonView:self seatItemTappedWithModel:roomSeatModel atIndex:indexPath.row];
    }
}

- (void)updateSingBtnWithChoosedSongArray:(NSArray *)choosedSongArray {
    NSMutableSet* changeSet = [NSMutableSet set];
    if(choosedSongArray.count == 0){
        self.currentPlayingSongCode = @"0";
    }
    if (choosedSongArray.count > 0) {
        VLSBGRoomSelSongModel *songModel = choosedSongArray.firstObject;
        self.winNo = songModel.winnerNo;
       // self.currentPlayingSongCode = songModel.chorusSongId;
        for (VLSBGRoomSeatModel *seatModel in self.roomSeatsArray) {
            BOOL isOwner = [seatModel.userNo isEqualToString:songModel.winnerNo];
            //if (isOwner != seatModel.isOwner) {
                seatModel.isOwner = isOwner;
                [changeSet addObject:@(seatModel.seatIndex)];
            //}
            //检查麦上用户
//            BOOL needtoJoinChorus = [seatModel.chorusSongCode isEqualToString:[songModel chorusSongId]];
////            cell.joinsttus
//            NSIndexPath *path = [NSIndexPath indexPathForRow:seatModel.seatIndex inSection:0];
//            VLSBGMicSeatCell* cell = [self.personCollectionView cellForItemAtIndexPath:path];
//            if (needtoJoinChorus != !cell.joinChorusBtn.isHidden){
//                //            if (![seatModel.chorusSongCode isEqualToString:[songModel chorusSongId]] && seatModel.chorusSongCode) {
//                // seatModel.chorusSongCode = @"";
//                [changeSet addObject:@(seatModel.seatIndex)];
//            }
//            }
            NSLog(@"seat: %@--%@--%li", seatModel.chorusSongCode, songModel.chorusSongId, seatModel.seatIndex);
            
        }
    }else{
        for (VLSBGRoomSeatModel *seatModel in self.roomSeatsArray) {
            if (seatModel.isOwner) {
                seatModel.isOwner = NO;
                [changeSet addObject:@(seatModel.seatIndex)];
            }
//            if (seatModel.chorusSongCode.length > 0) {
//               // seatModel.chorusSongCode = @"";
//                [changeSet addObject:@(seatModel.seatIndex)];
//            }
        }
    }
    
    if (changeSet.count == 0) {
        return;
    }
    
    NSMutableArray* indexPaths = [NSMutableArray array];
    for (NSNumber * index in changeSet) {
        [indexPaths addObject:[NSIndexPath indexPathForRow:[index integerValue] inSection:0]];
    }
    [self.personCollectionView reloadItemsAtIndexPaths:indexPaths];
}

@end
