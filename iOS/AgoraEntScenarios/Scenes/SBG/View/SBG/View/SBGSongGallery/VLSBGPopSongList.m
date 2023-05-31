//
//  VLPopChooseSongView.m
//  VoiceOnLine
//

#import "VLSBGPopSongList.h"
#import "VLSBGSelectedSongList.h"
#import "VLSBGSongList.h"
#import "VLHotSpotBtn.h"
#import "SBGMacro.h"
#import "VLToast.h"
#import <QuartzCore/QuartzCore.h>

@interface VLSBGPopSongList ()<VLSBGSelectedSongListDelegate,VLSBGSongListDelegate>

@property(nonatomic, weak) id <VLSBGPopSongListDelegate>delegate;

@property (nonatomic, strong) VLHotSpotBtn *dianGeBtn;
@property (nonatomic, strong) VLHotSpotBtn *choosedBtn;
@property (nonatomic, strong) UILabel      *choosedCountLabel;
@property (nonatomic, strong) UILabel      *sourceLabel;
@property (nonatomic, strong) VLSBGSelectedSongList *selsectSongView;
@property (nonatomic, strong) VLSBGSongList *choosedSongView;

@property (nonatomic, copy) NSString *roomNo;

@property (nonatomic, assign) BOOL ifChorus;
@property (nonatomic, strong) UIView *songNumView;
@property (nonatomic, strong) UILabel *songNumLabel;
@property (nonatomic, strong) UIButton *songNumBtn;
@end

@implementation VLSBGPopSongList

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSBGPopSongListDelegate>)delegate
                   withRoomNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus{
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        self.roomNo = roomNo;
        self.ifChorus = ifChorus;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    [self addSubview:self.dianGeBtn];
    [self addSubview:self.choosedBtn];
    [self addSubview:self.choosedCountLabel];
    [self addSubview:self.sourceLabel];
    [self addSubview:self.selsectSongView];
    [self addSubview:self.choosedSongView];
    [self addSubview:self.songNumView];
    [self.songNumView addSubview:self.songNumLabel];
    [self.songNumView addSubview:self.songNumBtn];
}

#pragma mark --Event
- (void)itemBtnClickEvent:(VLHotSpotBtn *)sender {
    if (sender.tag == 0) {
        self.dianGeBtn.titleLabel.font = UIFontBoldMake(16);
        [self.dianGeBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
        self.choosedBtn.titleLabel.font = UIFontMake(14);
        [self.choosedBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
        self.selsectSongView.hidden = NO;
        self.choosedSongView.hidden = YES;
        [self.dianGeBtn sizeToFit];
    }else{
        self.choosedBtn.titleLabel.font = UIFontBoldMake(16);
        [self.choosedBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
        self.dianGeBtn.titleLabel.font = UIFontMake(14);
        [self.dianGeBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
        self.selsectSongView.hidden = YES;
        self.choosedSongView.hidden = NO;
        [self.choosedBtn sizeToFit];
    }
    
    if ([self.delegate respondsToSelector:@selector(chooseSongView:tabbarDidClick:)]) {
        [self.delegate chooseSongView:self tabbarDidClick:sender.tag];
    }
}

-(void)startSbg{
    if(self.selSongsArray.count < 4){
        [VLToast toast:@"至少需要4首歌才能开始游戏"];
        return;
    }
    //开始抢唱
    [self.delegate chooseSongView:self tabbarDidClick:2];
}

#pragma mark --setter,getter
- (void)setSelSongsArray:(NSArray *)selSongsArray {
    _selSongsArray = selSongsArray;
    if (selSongsArray.count > 0) {
        self.choosedCountLabel.hidden = NO;
    }else{
        self.choosedCountLabel.hidden = YES;
    }

    self.selsectSongView.selSongsArray = selSongsArray;
    self.choosedSongView.selSongsArray = selSongsArray;
    self.songNumLabel.text = [NSString stringWithFormat:@"已点%lu/8", (unsigned long)selSongsArray.count];
    self.songNumBtn.selected = selSongsArray.count < 4;
  //  self.songNumBtn.enabled = selSongsArray.count >= 4;
//    [self.choosedSongView setSelSongsUIWithArray:selSongsArray];
//    [self.selsectSongView setSelSongArrayWith: selSongsArray];

    self.choosedCountLabel.text = [NSString stringWithFormat:@"%d",(int)selSongsArray.count];
}

- (VLHotSpotBtn *)dianGeBtn {
    if (!_dianGeBtn) {
        _dianGeBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(30, 20, 34, 22)];
        [_dianGeBtn setTitle:SBGLocalizedString(@"点歌") forState:UIControlStateNormal];
        _dianGeBtn.titleLabel.font = UIFontBoldMake(16);
        [_dianGeBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
        _dianGeBtn.tag = 0;
        [_dianGeBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
        [_dianGeBtn sizeToFit];
    }
    return _dianGeBtn;
}

- (VLHotSpotBtn *)choosedBtn {
    if (!_choosedBtn) {
        _choosedBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(_dianGeBtn.right+28, 20, 34, 22)];
        [_choosedBtn setTitle:SBGLocalizedString(@"已点") forState:UIControlStateNormal];
        _choosedBtn.titleLabel.font = UIFontMake(14);
        [_choosedBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
        _choosedBtn.tag = 1;
        [_choosedBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
        [_choosedBtn sizeToFit];
    }
    return _choosedBtn;
}

- (UILabel *)choosedCountLabel {
    if (!_choosedCountLabel) {
        _choosedCountLabel = [[UILabel alloc]initWithFrame:CGRectMake(_choosedBtn.right+3, _choosedBtn.centerY-8, 18, 18)];
        _choosedCountLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
        _choosedCountLabel.font = UIFontMake(11);
        _choosedCountLabel.layer.cornerRadius = 9;
        _choosedCountLabel.layer.masksToBounds = YES;
        _choosedCountLabel.textAlignment = NSTextAlignmentCenter;
        _choosedCountLabel.backgroundColor = UIColorMakeWithHex(@"#156EF3");
    }
    return _choosedCountLabel;
}

- (UILabel *)sourceLabel {
    if (!_sourceLabel) {
        _sourceLabel = [[UILabel alloc]initWithFrame:CGRectMake(self.width-25-90, _dianGeBtn.centerY-7, 90, 14)];
        _sourceLabel.textColor = UIColorMakeWithHex(@"#979CBB");
        _sourceLabel.font = UIFontMake(10);
        _sourceLabel.text = SBGLocalizedString(@"歌曲来自咪咕音乐");
        _sourceLabel.textAlignment = NSTextAlignmentRight;
        _sourceLabel.hidden = YES;
    }
    return _sourceLabel;
}

- (UIView *)songNumView {
    if (!_songNumView) {
        _songNumView = [[UIView alloc]initWithFrame:CGRectMake(0, self.height - 80, self.width, 80)];
        _songNumView.backgroundColor = UIColorMakeWithHex(@"#152064");
        
        _songNumView.layer.borderColor = [UIColor whiteColor].CGColor;
        _songNumView.layer.borderWidth = 1;
    }
    return _songNumView;
}

- (UILabel *)songNumLabel {
    if (!_songNumLabel) {
        _songNumLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 20, 90, 14)];
        _songNumLabel.textColor = [UIColor whiteColor];
        _songNumLabel.font = UIFontMake(16);
        _songNumLabel.text = @"已点0/8";
        _songNumLabel.textAlignment = NSTextAlignmentLeft;
    }
    return _songNumLabel;
}

- (UIButton *)songNumBtn {
    if (!_songNumBtn) {
        _songNumBtn = [[UIButton alloc]initWithFrame:CGRectMake(self.width - 140, 20, 120, 34)];
        [_songNumBtn setBackgroundImage:[UIImage sceneImageWithName:@"sbg-btn-start-disabled"] forState:UIControlStateSelected];
        [_songNumBtn setBackgroundImage:[UIImage sceneImageWithName:@"sbg-btn-start"] forState:UIControlStateNormal];
        [_songNumBtn addTarget:self action:@selector(startSbg) forControlEvents:UIControlEventTouchUpInside];
        _songNumBtn.selected = true;
    }
    return _songNumBtn;
}

- (VLSBGSelectedSongList *)selsectSongView {
    if (!_selsectSongView) {
        _selsectSongView = [[VLSBGSelectedSongList alloc]initWithFrame:CGRectMake(0, _dianGeBtn.bottom+20, SCREEN_WIDTH, self.height-20-22-20) withDelegate:self withRoomNo:self.roomNo ifChorus:self.ifChorus];
    }
    return _selsectSongView;
}

- (VLSBGSongList *)choosedSongView {
    if (!_choosedSongView) {
        _choosedSongView = [[VLSBGSongList alloc]initWithFrame:CGRectMake(0, _dianGeBtn.bottom+20, SCREEN_WIDTH, self.height-20-22-20) withDelegate:self ];
        _choosedSongView.hidden = YES;
    }
    return _choosedSongView;
}

@end
