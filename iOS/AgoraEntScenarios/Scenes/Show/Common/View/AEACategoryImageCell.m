//
//  AEACategoryImageCell.m
//  AgoraEditAvatar
//
//  Created by FanPengpeng on 2022/9/20.
//

#import "AEACategoryImageCell.h"

@interface AEACategoryImageCell ()

@property (strong, nonatomic) UIImageView *imageView;

@end

@implementation AEACategoryImageCell


- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self setupUI];
    }
    return self;
}

- (void)setupUI {
    _imageView = [UIImageView new];
    
    [self.contentView addSubview:_imageView];
    
    _imageView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [[_imageView.leftAnchor constraintEqualToAnchor:self.contentView.leftAnchor  constant:5] setActive:YES];
    [[_imageView.rightAnchor constraintEqualToAnchor:self.contentView.rightAnchor constant:-5] setActive:YES];
    [[_imageView.topAnchor constraintEqualToAnchor:self.contentView.topAnchor constant:5] setActive:YES];
    [[_imageView.bottomAnchor constraintEqualToAnchor:self.contentView.bottomAnchor constant:-5] setActive:YES];
}

- (void)setNormalImage:(UIImage *)normalImage {
    _normalImage = normalImage;
    self.imageView.image = normalImage;
}

- (void)setSelected:(BOOL)selected {
    [super setSelected:selected];
    self.imageView.image = selected ? self.selectedImage : self.normalImage;
}

@end
