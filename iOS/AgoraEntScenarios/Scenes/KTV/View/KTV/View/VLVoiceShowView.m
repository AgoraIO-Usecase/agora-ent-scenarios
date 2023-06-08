//
//  VLVoiceShowView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import "VLVoiceShowView.h"
#import "KTVMacro.h"
#import <SDWebImage/UIImageView+WebCache.h>
@interface VLVoiceShowView()
@property(nonatomic, weak) id <VLVoiceShowViewDelegate>delegate;
@property (nonatomic,strong) UILabel *selLabel;
@property (nonatomic,strong) UIImageView *selCoverImg;
@property (nonatomic, assign) NSInteger selectIndex;
@end

@implementation VLVoiceShowView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLVoiceShowViewDelegate>)delegate imgSource:(NSArray *)imgSource nameSource:(NSArray *)nameSource selectIndex:(NSInteger)selectIndex {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        self.selectIndex = selectIndex;
        [self layoutUIWithDataSource: imgSource nameSource:nameSource selectIndex:selectIndex];
    }
    return self;
}

-(void)layoutUIWithDataSource:(NSArray *)imgSource nameSource:(NSArray *)nameSource selectIndex:(NSInteger)selectIndex {
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = KTVLocalizedString(@"设置人声");
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
        titleLabel.textColor = i == selectIndex ? [UIColor whiteColor] : [UIColor lightGrayColor];
        titleLabel.tag = i;
        if(i == selectIndex){
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
        coverImgView.image = [UIImage sceneImageWithName:@"ktv_selIcon"];
        coverImgView.tag = 300 + i;
        coverImgView.hidden = i != selectIndex;
        if(i == selectIndex){
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
    if(btn.tag - 200 == _selectIndex){
        return;
    }
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
