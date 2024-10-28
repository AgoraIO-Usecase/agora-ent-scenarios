//
//  ColorTheme.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import Foundation
import UIKit


/// LightnessThemeStyle means that the lightness of the color percentage is 0-100.
enum LightnessThemeStyle: UInt {
    case zero = 0
    case one = 10
    case two = 20
    case three = 30
    case four = 40
    case five = 50
    case six = 60
    case seven = 70
    case seventyFive = 75
    case eight = 80
    case eightyFive = 85
    case ninety = 90
    case ninetyFive = 95
    case ninetyEight = 98
    case oneHundred = 100
}

enum AlphaThemeStyle: CGFloat {
    case zero = 0
    case one = 0.1
    case two = 0.2
    case three = 0.3
    case four = 0.4
    case five = 0.5
    case six = 0.6
    case seven = 0.7
    case eight = 0.8
    case ninety = 0.9
    case ninetyFive = 0.95
    case ninetyEight = 0.98
    case oneHundred = 1
}



fileprivate func hueToRGB(_ m: CGFloat, _ l: CGFloat, _ hue: CGFloat) -> CGFloat {
    var hue = hue
    
    if hue < 0.0 {
        hue += 1.0
    }
    
    if hue > 1.0 {
        hue -= 1.0
    }
    
    if hue < 1.0/6.0 {
        return m + (l - m) * 6.0 * hue
    } else if hue < 1.0/2.0 {
        return l
    } else if hue < 2.0/3.0 {
        return m + (l - m) * (2.0/3.0 - hue) * 6.0
    }
    
    return m
}



public extension UIColor {
    
    /// init with HSLA color.
    /// - Parameters:
    ///   - hue: Color's hue.
    ///   - saturation: Color's saturation.
    ///   - lightness: Color's lightness.
    ///   - alpha: Color's alpha.
    @objc convenience init(hue: CGFloat, saturation: CGFloat, lightness: CGFloat, alpha: CGFloat) {
        let hue = max(0.0, min(1.0, hue))
        let saturation = max(0.0, min(1.0, saturation))
        let lightness = max(0.0, min(1.0, lightness))
        let alpha = max(0.0, min(1.0, alpha))
        
        let l = lightness <= 0.5 ? (lightness * (saturation + 1.0)) : (lightness + saturation - lightness * saturation)
        let m = 2.0 * lightness - l
        
        let r = hueToRGB(m, l, hue + 1.0/3.0)
        let g = hueToRGB(m, l, hue)
        let b = hueToRGB(m, l, hue - 1.0/3.0)
        
        self.init(red: r, green: g, blue: b, alpha: alpha)
    }
    
    
    /// Color with hex value.
    /// - Parameters:
    ///   - hexValue: Similar to 0x000000.
    ///   - alpha: alpha
    ///   How to use?
    ///   `UIColor(0x000000)`
    convenience init(_ hexValue: UInt,_ alpha: CGFloat = 1.0) {
        self.init(red: CGFloat((CGFloat((hexValue & 0xff0000) >> 16)) / 255.0),
                  green: CGFloat((CGFloat((hexValue & 0x00ff00) >> 8)) / 255.0),
                  blue: CGFloat((CGFloat(hexValue & 0x0000ff)) / 255.0),
                  alpha: alpha)
    }
    
    /// UIColor theme of ChatroomUIKit.Contain primary, secondary, error, neutral, neutralSpecial  color series .Every series has 13 colors.
    @objc static let theme: ColorTheme = ColorTheme()
        
    @objcMembers class ColorTheme: NSObject {
        
        public static func switchHues(hues: [CGFloat]) {
            if let primaryHue = hues[safe: 0] {
                self.primaryHue = primaryHue
            }
            if let secondaryHue = hues[safe: 1] {
                self.secondaryHue = secondaryHue
            }
            if let errorHue = hues[safe: 2] {
                self.errorHue = errorHue
            }
            if let neutralHue = hues[safe: 3] {
                self.neutralHue = neutralHue
            }
            if let neutralSpecialHue = hues[safe: 4] {
                self.neutralSpecialHue = neutralSpecialHue
            }
            if let gradientEndHue = hues[safe: 5] {
                self.gradientEndHue = gradientEndHue
            }
        }
        
        /// You can change the hue of the base color, and then change the thirteen UIColor objects of the related color series. The UI components that use the relevant color series in the ease chat UIKit will also change accordingly. The default value is 203/360.0.
        ///  How to use?
        ///  `ColorTheme.primaryHue = 0.7`
        public static var primaryHue: CGFloat =  203/360.0
        /// You can change the hue of the base color, and then change the thirteen UIColor objects of the related color series. The UI components that use the relevant color series in the ease chat UIKit will also change accordingly . The default value is 155/360.0.
        ///  How to use?
        ///  `ColorTheme.secondaryHue = 0.`7
        public static var secondaryHue: CGFloat = 155/360.0
        /// You can change the hue of the base color, and then change the thirteen UIColor objects of the related color series. The UI components that use the relevant color series in the ease chat UIKit will also change accordingly . The default value is 350/360.0.
        ///  How to use?
        ///  `ColorTheme.errorHue = 0.7`
        public static var errorHue: CGFloat = 350/360.0
        /// You can change the hue of the base color, and then change the thirteen UIColor objects of the related color series. The UI components that use the relevant color series in the ease chat UIKit will also change accordingly. The default value is 203/360.0.
        ///  How to use?
        ///  ColorTheme.neutralHue = 0.7
        public static var neutralHue: CGFloat = 203/360.0
        /// You can change the hue of the base color, and then change the thirteen UIColor objects of the related color series. The UI components that use the relevant color series in the ease chat UIKit will also change accordingly. The default value is 220/360.0
        ///  How to use?
        ///  `ColorTheme.neutralSpecialHue = 0.7`
        public static var neutralSpecialHue: CGFloat = 220/360.0
        
        /// You can modify this value to change the value of all gradient end colors.
        public static var gradientEndHue: CGFloat = 233/360.0
        
        /// UIColor Extension
        ///  `UIColor.theme.primaryColor0`
        public var primaryColor0: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .zero)
        }
        
        public var primaryColor1: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .one)
        }
        
        public var primaryColor2: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .two)
        }
        
        public var primaryColor3: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .three)
        }
        
        public var primaryColor4: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .four)
        }
        
        public var primaryColor5: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .five)
        }
        
        public var primaryColor6: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .six)
        }
        
        public var primaryColor7: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .seven)
        }
        
        public var primaryColor8: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .eight)
        }
        
        public var primaryColor9: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .ninety)
        }
        
        public var primaryColor95: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .ninetyFive)
        }
        
        public var primaryColor98: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .ninetyEight)
        }
        
        public var primaryColor100: UIColor {
            UIColor.ColorTheme.primaryColor(lightness: .oneHundred)
        }
        
        /// primary color constructor.
        /// - Parameter style: LightnessThemeStyle
        /// - Returns: UIColor instance.
        static func primaryColor(lightness style: LightnessThemeStyle) -> UIColor {
            UIColor(hue: ColorTheme.primaryHue, saturation: 1, lightness: CGFloat(style.rawValue)/100.0, alpha: 1)
        }
        /// UIColor Extension
        /// `UIColor.theme.secondaryColor0`
        public var secondaryColor0: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .zero)
        }
        
        public var secondaryColor1: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .one)
        }
        
        public var secondaryColor2: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .two)
        }
        
        public var secondaryColor3: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .three)
        }
        
        public var secondaryColor4: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .four)
        }
        
        public var secondaryColor5: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .five)
        }
        
        public var secondaryColor6: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .six)
        }
        
        public var secondaryColor7: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .seven)
        }
        
        public var secondaryColor8: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .eight)
        }
        
        public var secondaryColor9: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .ninety)
        }
        
        public var secondaryColor95: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .ninetyFive)
        }
        
        public var secondaryColor98: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .ninetyEight)
        }
        
        public var secondaryColor100: UIColor {
            UIColor.ColorTheme.secondaryColor(lightness: .oneHundred)
        }
        /// secondary color constructor.
        /// - Parameter style: LightnessThemeStyle
        /// - Returns: UIColor instance.
        static func secondaryColor(lightness style: LightnessThemeStyle) -> UIColor {
            UIColor(hue: ColorTheme.secondaryHue, saturation: 1, lightness: CGFloat(style.rawValue)/100.0, alpha: 1)
        }
        /// UIColor Extension
        /// `UIColor.theme.errorColor0`
        public var errorColor0: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .zero)
        }
        
        public var errorColor1: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .one)
        }
        
        public var errorColor2: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .two)
        }
        
        public var errorColor3: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .three)
        }
        
        public var errorColor4: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .four)
        }
        
        public var errorColor5: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .five)
        }
        
        public var errorColor6: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .six)
        }
        
        public var errorColor7: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .seven)
        }
        
        public var errorColor8: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .eight)
        }
        public var errorColor9: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .ninety)
        }
        public var errorColor95: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .ninetyFive)
        }
        public var errorColor98: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .ninetyEight)
        }
        public var errorColor100: UIColor {
            UIColor.ColorTheme.errorColor(lightness: .oneHundred)
        }
        /// error color constructor.
        /// - Parameter style: LightnessThemeStyle
        /// - Returns: UIColor instance.
        static func errorColor(lightness style: LightnessThemeStyle) -> UIColor {
            UIColor(hue: ColorTheme.errorHue, saturation: 1, lightness: CGFloat(style.rawValue)/100.0, alpha: 1)
        }
        /// UIColor Extension
        /// `UIColor.theme.neutralColor0`
        public var neutralColor0: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .zero)
        }
        public var neutralColor1: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .one)
        }
        public var neutralColor2: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .two)
        }
        public var neutralColor3: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .three)
        }
        public var neutralColor4: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .four)
        }
        public var neutralColor5: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .five)
        }
        public var neutralColor6: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .six)
        }
        public var neutralColor7: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .seven)
        }
        public var neutralColor8: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .eight)
        }
        public var neutralColor9: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .ninety)
        }
        public var neutralColor95: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .ninetyFive)
        }
        public var neutralColor98: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .ninetyEight)
        }
        public var neutralColor100: UIColor {
            UIColor.ColorTheme.neutralColor(lightness: .oneHundred)
        }
        /// neutral color constructor.
        /// - Parameter style: LightnessThemeStyle
        /// - Returns: UIColor instance.
        static func neutralColor(lightness style: LightnessThemeStyle) -> UIColor {
            UIColor(hue: ColorTheme.neutralHue, saturation: 0.08, lightness: CGFloat(style.rawValue)/100.0, alpha: 1)
        }
        /// UIColor Extension
        /// `UIColor.theme.neutralSpecialColor0`
        public var neutralSpecialColor0: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .zero)
        }
        public var neutralSpecialColor1: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .one)
        }
        public var neutralSpecialColor2: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .two)
        }
        public var neutralSpecialColor3: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .three)
        }
        public var neutralSpecialColor4: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .four)
        }
        public var neutralSpecialColor5: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .five)
        }
        public var neutralSpecialColor6: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .six)
        }
        public var neutralSpecialColor7: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .seven)
        }
        public var neutralSpecialColor8: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .eight)
        }
        public var neutralSpecialColor9: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .ninety)
        }
        public var neutralSpecialColor95: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .ninetyFive)
        }
        public var neutralSpecialColor98: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .ninetyEight)
        }
        public var neutralSpecialColor100: UIColor {
            UIColor.ColorTheme.neutralSpecialColor(lightness: .oneHundred)
        }
        /// neutral special color constructor.
        /// - Parameter style: LightnessThemeStyle
        /// - Returns: UIColor instance.
        static func neutralSpecialColor(lightness style: LightnessThemeStyle) -> UIColor {
            UIColor(hue: ColorTheme.neutralSpecialHue, saturation: 0.36, lightness: CGFloat(style.rawValue)/100.0, alpha: 1)
        }
        /// UIColor Extension
        /// `UIColor.theme.barrageLightColor0`
        public var barrageLightColor0: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .zero)
        }
        public var barrageLightColor1: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .one)
        }
        public var barrageLightColor2: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .two)
        }
        public var barrageLightColor3: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .three)
        }
        public var barrageLightColor4: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .four)
        }
        public var barrageLightColor5: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .five)
        }
        public var barrageLightColor6: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .six)
        }
        public var barrageLightColor7: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .seven)
        }
        public var barrageLightColor8: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .eight)
        }
        public var barrageLightColor9: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .ninety)
        }
        public var barrageLightColor95: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .ninetyFive)
        }
        public var barrageLightColor98: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .ninetyEight)
        }
        public var barrageLightColor100: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .zero, alpha: .oneHundred)
        }
        public var barrageDarkColor0: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .zero)
        }
        public var barrageDarkColor1: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .one)
        }
        public var barrageDarkColor2: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .two)
        }
        public var barrageDarkColor3: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .three)
        }
        public var barrageDarkColor4: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .four)
        }
        public var barrageDarkColor5: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .five)
        }
        public var barrageDarkColor6: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .six)
        }
        public var barrageDarkColor7: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .seven)
        }
        public var barrageDarkColor8: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .eight)
        }
        public var barrageDarkColor9: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .ninety)
        }
        public var barrageDarkColor95: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .ninetyFive)
        }
        public var barrageDarkColor98: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .ninetyEight)
        }
        public var barrageDarkColor100: UIColor {
            UIColor.ColorTheme.barrageColor(lightness: .oneHundred, alpha: .oneHundred)
        }
        
        
        
        /// neutral special color constructor.
        /// - Parameter style: LightnessThemeStyle
        /// - Returns: UIColor instance.
        static func barrageColor(lightness style: LightnessThemeStyle,alpha: AlphaThemeStyle) -> UIColor {
            UIColor(hue: 0, saturation: 0, lightness: CGFloat(style.rawValue)/100.0, alpha: alpha.rawValue)
        }
        
        
        /// Gradient Colors constructor method.
        /// - Parameter style: LightnessThemeStyle
        /// - Returns: UIColor instance
        static func gradientEndColor(lightness style: LightnessThemeStyle) -> UIColor {
            UIColor(hue: ColorTheme.gradientEndHue, saturation: 1, lightness: CGFloat(style.rawValue)/100.0, alpha: 1)
        }
        
        var interruptUnselectedColor = UIColor(hexString: "979cbb")
        var interruptSelectedColor = UIColor(hexString: "009fff")
    }
}


@objcMembers public class GradientForwardPoints: NSObject {
    
    public static var topLeftToBottomRight = [CGPoint(x: 0, y: 0),CGPoint(x: 1, y: 1)]

    public static var topRightToBottomLeft = [CGPoint(x: 1, y: 0),CGPoint(x: 0, y: 1)]
    
    public static var bottomLeftToTopRight = [CGPoint(x: 0, y: 1),CGPoint(x: 1, y: 0)]
    
    public static var bottomRightToTopLeft = [CGPoint(x: 1, y: 1),CGPoint(x: 0, y: 0)]
    
    public static var topToBottom = [CGPoint(x: 0.5, y: 0),CGPoint(x: 0.5, y: 1)]
    
    public static var bottomToTop = [CGPoint(x: 0.5, y: 1),CGPoint(x: 0.5, y: 0)]
    
    public static var leftToRight = [CGPoint(x: 0, y: 0.5),CGPoint(x: 1, y: 0.5)]
    
    public static var rightToLeft = [CGPoint(x: 1, y: 0.5),CGPoint(x: 0, y: 0.5)]
}

@objcMembers public class GradientColors: NSObject {
    public static var gradientColors0: [UIColor] = [UIColor.theme.primaryColor0,UIColor.ColorTheme.gradientEndColor(lightness: .two)]

    public static var graduentColors1: [UIColor] = [UIColor.theme.primaryColor1,UIColor.ColorTheme.gradientEndColor(lightness: .three)]

    public static var graduentColors2: [UIColor] = [UIColor.theme.primaryColor2,UIColor.ColorTheme.gradientEndColor(lightness: .four)]

    public static var graduentColors3: [UIColor] = [UIColor.theme.primaryColor3,UIColor.ColorTheme.gradientEndColor(lightness: .five)]

    public static var graduentColors4: [UIColor] = [UIColor.theme.primaryColor4,UIColor.ColorTheme.gradientEndColor(lightness: .six)]

    public static var graduentColors5: [UIColor] = [UIColor.theme.primaryColor5,UIColor.ColorTheme.gradientEndColor(lightness: .seven)]

    public static var graduentColors6: [UIColor] = [UIColor.theme.primaryColor6,UIColor.ColorTheme.gradientEndColor(lightness: .seventyFive)]

    static var graduentColors7: [UIColor] = [UIColor.theme.primaryColor7,UIColor.ColorTheme.gradientEndColor(lightness: .eight)]

    static var graduentColors8: [UIColor] = [UIColor.theme.primaryColor8,UIColor.ColorTheme.gradientEndColor(lightness: .eightyFive)]

    static var graduentColors9: [UIColor] = [UIColor.theme.primaryColor9,UIColor.ColorTheme.gradientEndColor(lightness: .ninety)]

    static var graduentColors95: [UIColor] = [UIColor.theme.primaryColor95,UIColor.ColorTheme.gradientEndColor(lightness: .ninetyFive)]

    static var graduentColors98: [UIColor] = [UIColor.theme.primaryColor98,UIColor.ColorTheme.gradientEndColor(lightness: .ninety)]
}
