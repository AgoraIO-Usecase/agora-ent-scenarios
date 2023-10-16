//
//  UIButton+EntLayout.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/12/19.
//

import Foundation

// MARK: - title insert
extension UIButton {
    @objc var spacingBetweenImageAndTitle: CGFloat {
        set {
//            self.set
            self.setInsets(contentPadding: UIEdgeInsets.zero, imageTitlePadding: newValue)
        }
        get {
            return 0
        }
    }
    
    func setInsets(contentPadding: UIEdgeInsets,
                   imageTitlePadding: CGFloat) {
        self.contentEdgeInsets = UIEdgeInsets(
            top: contentPadding.top,
            left: contentPadding.left,
            bottom: contentPadding.bottom,
            right: contentPadding.right + imageTitlePadding
        )
        self.titleEdgeInsets = UIEdgeInsets(
            top: 0,
            left: imageTitlePadding,
            bottom: 0,
            right: -imageTitlePadding
        )
    }
}

// MARK: - tap range expand
var expandSizeKey = "expandSizeKey"
extension UIButton {
    public func vm_expandSize(size: CGFloat) {
        objc_setAssociatedObject(self, &expandSizeKey, size, objc_AssociationPolicy.OBJC_ASSOCIATION_COPY)
    }

    private func expandRect() -> CGRect {
        let expandSize = objc_getAssociatedObject(self, &expandSizeKey)
        if expandSize != nil {
            return CGRect(x: bounds.origin.x - (expandSize as! CGFloat), y: bounds.origin.y - (expandSize as! CGFloat), width: bounds.size.width + 2 * (expandSize as! CGFloat), height: bounds.size.height + 2 * (expandSize as! CGFloat))
        } else {
            return bounds
        }
    }

    override open func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        let buttonRect = expandRect()
        if buttonRect.equalTo(bounds) {
            return super.point(inside: point, with: event)
        } else {
            return buttonRect.contains(point)
        }
    }
}
// MARK: - image position
@objc public extension UIButton {
    /// Enum to determine the title position with respect to the button image
    ///
    /// - top: title above button image
    /// - bottom: title below button image
    /// - left: title to the left of button image
    /// - right: title to the right of button image
    @objc enum Position: Int {
        case top, bottom, left, right
    }

    /// This method sets an image and title for a UIButton and
    ///   repositions the titlePosition with respect to the button image.
    ///
    /// - Parameters:
    ///   - image: Button image
    ///   - title: Button title
    ///   - titlePosition: UIViewContentModeTop, UIViewContentModeBottom, UIViewContentModeLeft or UIViewContentModeRight
    ///   - additionalSpacing: Spacing between image and title
    ///   - state: State to apply this behaviour
    func set(image: UIImage?, title: String, titlePosition: Position, additionalSpacing: CGFloat, state: UIControl.State) {
        imageView?.contentMode = .center
        setImage(image, for: state)
        setTitle(title, for: state)
        titleLabel?.contentMode = .center

        adjust(title: title as NSString, at: titlePosition, with: additionalSpacing)
    }

    /// This method sets an image and an attributed title for a UIButton and
    ///   repositions the titlePosition with respect to the button image.
    ///
    /// - Parameters:
    ///   - image: Button image
    ///   - title: Button attributed title
    ///   - titlePosition: UIViewContentModeTop, UIViewContentModeBottom, UIViewContentModeLeft or UIViewContentModeRight
    ///   - additionalSpacing: Spacing between image and title
    ///   - state: State to apply this behaviour
    func set(image: UIImage?, attributedTitle title: NSAttributedString, at position: Position, width spacing: CGFloat, state: UIControl.State) {
        imageView?.contentMode = .center
        setImage(image, for: state)

        adjust(attributedTitle: title, at: position, with: spacing)

        titleLabel?.contentMode = .center
        setAttributedTitle(title, for: state)
    }

    // MARK: Private Methods

    @objc private func adjust(title: NSString, at position: Position, with spacing: CGFloat) {
        let imageRect: CGRect = self.imageRect(forContentRect: frame)

        // Use predefined font, otherwise use the default
        let titleFont: UIFont = titleLabel?.font ?? UIFont()
        let titleSize: CGSize = title.size(withAttributes: [NSAttributedString.Key.font: titleFont])

        arrange(titleSize: titleSize, imageRect: imageRect, atPosition: position, withSpacing: spacing)
    }

    @objc private func adjust(attributedTitle: NSAttributedString, at position: Position, with spacing: CGFloat) {
        let imageRect: CGRect = self.imageRect(forContentRect: frame)
        let titleSize = attributedTitle.size()

        arrange(titleSize: titleSize, imageRect: imageRect, atPosition: position, withSpacing: spacing)
    }

    @objc private func arrange(titleSize: CGSize, imageRect: CGRect, atPosition position: Position, withSpacing spacing: CGFloat) {
        switch position {
        case .top:
            titleEdgeInsets = UIEdgeInsets(top: -(imageRect.height + titleSize.height + spacing), left: -(imageRect.width), bottom: 0, right: 0)
            imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: -titleSize.width)
            contentEdgeInsets = UIEdgeInsets(top: spacing / 2 + titleSize.height, left: -imageRect.width / 2, bottom: 0, right: -imageRect.width / 2)
        case .bottom:
            titleEdgeInsets = UIEdgeInsets(top: imageRect.height + titleSize.height + spacing, left: -(imageRect.width), bottom: 0, right: 0)
            imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: -titleSize.width)
            contentEdgeInsets = UIEdgeInsets(top: 0, left: -imageRect.width / 2, bottom: spacing / 2 + titleSize.height, right: -imageRect.width / 2)
        case .left:
            titleEdgeInsets = UIEdgeInsets(top: 0, left: -(imageRect.width * 2), bottom: 0, right: 0)
            imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: -(titleSize.width * 2 + spacing))
            contentEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: spacing / 2)
        case .right:
            titleEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: -spacing)
            imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0)
            contentEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: spacing / 2)
        }
    }
    /// Image position in counterclockwise direction🔄
//    @objc enum ImgPosition: Int { case top, left, bottom, right }

    /// Reset the position of image and title (default spacing is 0)
//    @objc func adjustImageTitlePosition(_ position: ImgPosition, spacing: CGFloat = 0 ) {
//         self.sizeToFit()
//         
//         let imageWidth = self.imageView?.image?.size.width
//         let imageHeight = self.imageView?.image?.size.height
//         
//         let labelWidth = self.titleLabel?.frame.size.width
//         let labelHeight = self.titleLabel?.frame.size.height
//         
//         switch position {
//         case .top:
//             imageEdgeInsets = UIEdgeInsets(top: -labelHeight! - spacing / 2, left: 0, bottom: 0, right: -labelWidth!)
//             titleEdgeInsets = UIEdgeInsets(top: 0, left: -imageWidth!, bottom: -imageHeight! - spacing / 2, right: 0)
//             self.titleLabel?.frame = CGRect(x: 0, y: 0, width: labelWidth!, height: labelWidth!)
//             self.imageView?.frame = CGRect(x: 0, y: 0, width: imageWidth!, height: imageWidth!)
//             break
//             
//         case .left:
//             imageEdgeInsets = UIEdgeInsets(top: 0, left: -spacing / 2, bottom: 0, right: 0)
//             titleEdgeInsets = UIEdgeInsets(top: 0, left: spacing * 1.5, bottom: 0, right: 0)
//             break
//             
//         case .bottom:
//             imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: -labelHeight! - spacing / 2, right: -labelWidth!)
//             titleEdgeInsets = UIEdgeInsets(top: -imageHeight! - spacing / 2, left: -imageWidth!, bottom: 0, right: 0)
//             break
//             
//         case .right:
//             imageEdgeInsets = UIEdgeInsets(top: 0, left: labelWidth! + spacing / 2, bottom: 0, right: -labelWidth! - spacing / 2)
//             titleEdgeInsets = UIEdgeInsets(top: 0, left: -imageWidth! - spacing / 2, bottom: 0, right: imageWidth! + spacing / 2)
//             break
//         }
//     }
}
