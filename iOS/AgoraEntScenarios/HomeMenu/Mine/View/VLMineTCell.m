//
//  VLMineTCell.m
//  VoiceOnLine
//

#import "VLMineTCell.h"
#import "AESMacro.h"

@interface VLMineTCell ()
@end

@implementation VLMineTCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        self.contentView.backgroundColor = UIColorWhite;
        [self setUpView];
    }
    return self;
}

- (void)setUpView {
    self.iconImgView = [[UIImageView alloc]init];
    [self.contentView addSubview:self.iconImgView];
    
    self.itemTitleLabel = [[UILabel alloc]init];
    self.itemTitleLabel.textColor = UIColorMakeWithHex(@"#040925");
    self.itemTitleLabel.font = UIFontMake(14);
    [self.contentView addSubview:self.itemTitleLabel];
    
    self.arrowImgView = [[UIImageView alloc]init];
    self.arrowImgView.image = UIImageMake(@"mine_rightArrow_icon");
    [self.contentView addSubview:self.arrowImgView];
}

- (void)layoutSubviews {
    [super layoutSubviews];
     
    self.iconImgView.frame = CGRectMake(15, (self.height-24)*0.5, 24, 24);
    self.arrowImgView.frame = CGRectMake(self.width-15-16, (self.height-16)*0.5, 16, 16);
    CGFloat left = self.iconImgView.right + 16;
    CGFloat width = self.arrowImgView.right - 16 - left;
    self.itemTitleLabel.frame = CGRectMake(left, (self.height-24)*0.5, width, 24);
}

- (void)setDict:(NSDictionary *)dict {
    _dict = dict;
    self.iconImgView.image = UIImageMake(dict[@"itemImgStr"]);
    self.itemTitleLabel.text = dict[@"titleStr"];
}

- (void)setIconImageName:(NSString *)imgStr title:(NSString *)title {
    self.iconImgView.image = UIImageMake(imgStr);
    self.itemTitleLabel.text = title;
}

@end
