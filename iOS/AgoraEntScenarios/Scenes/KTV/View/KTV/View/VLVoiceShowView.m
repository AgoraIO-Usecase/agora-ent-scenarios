//
//  VLVoiceShowView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import "VLVoiceShowView.h"
#import <SDWebImage/UIImageView+WebCache.h>
#import "AgoraEntScenarios-Swift.h"
@interface VLVoiceShowView()
@property(nonatomic, weak) id <VLVoiceShowViewDelegate>delegate;
@property (nonatomic,strong) UILabel *selLabel;
@property (nonatomic,strong) UIImageView *selCoverImg;
@property (nonatomic, assign) NSInteger selectIndex;
@property (nonatomic, assign) BOOL UIUpdateAble;
@property (nonatomic, assign) BOOL perHasSet;
@end

@implementation VLVoiceShowView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLVoiceShowViewDelegate>)delegate imgSource:(NSArray *)imgSource nameSource:(NSArray *)nameSource userNoArray: (NSArray *)userNoArray selectUserNo:(nonnull NSString *)selectUserNo UIUpdateAble:(BOOL)UIUpdateAble {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
       // self.selectIndex = selectIndex;
        self.UIUpdateAble = UIUpdateAble;
        self.perHasSet = (UIUpdateAble == false || selectUserNo.length > 0);
        [self layoutUIWithDataSource: imgSource nameSource:nameSource selectUserNo:selectUserNo userNoArray:userNoArray];
    }
    return self;
}

-(void)layoutUIWithDataSource:(NSArray *)imgSource nameSource:(NSArray *)nameSource selectUserNo:(NSString *)selectUserNo userNoArray:(NSArray *)userNoArray{
    NSInteger realIndex = -1;
    for(int i=0;i<nameSource.count;i++){
        if([selectUserNo isEqualToString:userNoArray[i]]){
            realIndex = i;
        }
    }
    
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = KTVLocalizedString(@"ktv_vol_setting");
    titleLabel.font = UIFontMake(18);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    CGFloat width = 54;
    CGFloat btnHeight = 92;
    CGFloat imgHeight = 54;
    CGFloat titleHeight = 17;
    CGFloat margin = (self.bounds.size.width - 4 * 54) / 5;
    for(int i=0;i< nameSource.count; i++){
        CGFloat tx =  margin + (i) % 4 * (width + margin);
        CGFloat ty = margin + (i) / 4 * (btnHeight + margin) + 42 + 62;
        NSLog(@"t:frame %f--%f--%f--%f", tx, ty, width, titleHeight);
        UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(tx, ty, width, titleHeight)];
        titleLabel.text = nameSource[i];
        titleLabel.tag = 1000 + i;
        titleLabel.textAlignment = NSTextAlignmentCenter;
        titleLabel.textColor = i == realIndex ? [UIColor whiteColor] : [UIColor lightGrayColor];
        titleLabel.tag = i;
        if(i == realIndex){
            self.selLabel = titleLabel;
        }
        titleLabel.font = [UIFont systemFontOfSize:12];
        [self addSubview:titleLabel];
        
        CGFloat ix =  margin + (i) % 4 * (width + margin);
        CGFloat iy = margin + (i) / 4 * (btnHeight + margin) + 42;
        NSLog(@"i:frame %f--%f--%f--%f", ix, iy, width, imgHeight);
        UIImageView *imgView = [[UIImageView alloc]initWithFrame:CGRectMake(ix, iy, width, imgHeight)];
        [imgView sd_setImageWithURL:[NSURL URLWithString:imgSource[i]]];
        [self addSubview:imgView];
        
        UIImageView *coverImgView = [[UIImageView alloc]initWithFrame:CGRectMake(ix, iy, width, imgHeight)];
        coverImgView.image = [UIImage ktv_sceneImageWithName:@"ktv_selIcon" ];
        coverImgView.tag = 300 + i;
        coverImgView.hidden = i != realIndex;
        if(i == realIndex){
            self.selCoverImg = coverImgView;
        }
        [self addSubview:coverImgView];
        
        CGFloat bx =  margin + (i) % 4 * (width + margin);
        CGFloat by = margin + (i) / 4 * (btnHeight + margin) + 42;
        NSLog(@"b:frame %f--%f--%f--%f", bx, by, width, btnHeight);
        UIButton *btn = [[UIButton alloc]initWithFrame:CGRectMake(bx, by, width, btnHeight)];
        btn.tag = 200 + i;
        [btn addTarget:self action:@selector(click:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:btn];
    }
}

-(void)click:(UIButton *)btn {
    
//    if(self.UIUpdateAble == false){
//        if([self.delegate respondsToSelector:@selector(voiceItemClickAction:)]){
//            [self.delegate voiceItemClickAction:-1];
//            return;
//        }
//    }
    
    if(self.perHasSet == true){
        if([self.delegate respondsToSelector:@selector(voiceItemClickAction:)]){
            [self.delegate voiceItemClickAction:-1];
            return;
        }
        return;
    }
    
    self.perHasSet = true;
    self.selLabel.textColor = [UIColor lightGrayColor];
    self.selCoverImg.hidden = true;
    
    UIImageView *selImg = [self viewWithTag:btn.tag + 100];
    UILabel *selLabel = [self viewWithTag:btn.tag - 100];
    selImg.hidden = false;
    selLabel.textColor = [UIColor whiteColor];
    self.selLabel = selLabel;
    self.selCoverImg = selImg;
    if([self.delegate respondsToSelector:@selector(voiceItemClickAction:)]){
        [self.delegate voiceItemClickAction:btn.tag - 200];
    }
}

-(void)layoutSubviews{
    [super layoutSubviews];
}

@end
