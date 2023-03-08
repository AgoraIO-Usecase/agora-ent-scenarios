//
//  EKBackgroundView.swift
//  SwiftEntryKit
//
//  Created by Daniel Huri on 4/20/18.
//  Copyright (c) 2018 huri000@gmail.com. All rights reserved.
//

import UIKit

final class EKBackgroundView: EKStyleView {
    struct Style {
        let background: EKAttributes.BackgroundStyle
        let displayMode: EKAttributes.DisplayMode
    }

    // MARK: Props

    private let visualEffectView: UIVisualEffectView
    private let imageView: UIImageView
    private let gradientView: GradientView

    // MARK: Setup

    init() {
        imageView = UIImageView()
        visualEffectView = UIVisualEffectView(effect: nil)
        gradientView = GradientView()
        super.init(frame: UIScreen.main.bounds)

        addSubview(imageView)
        imageView.contentMode = .scaleAspectFill
        imageView.fillSuperview()

        addSubview(visualEffectView)
        visualEffectView.fillSuperview()

        addSubview(gradientView)
        gradientView.fillSuperview()
    }

    @available(*, unavailable)
    required init?(coder _: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    // Background setter
    var style: Style! {
        didSet {
            guard let style = style else {
                return
            }
            var gradient: EKAttributes.BackgroundStyle.Gradient?
            var backgroundEffect: UIBlurEffect?
            var backgroundColor: UIColor = .clear
            var backgroundImage: UIImage?

            switch style.background {
            case let .color(color: color):
                backgroundColor = color.color(for: traitCollection,
                                              mode: style.displayMode)
            case let .gradient(gradient: value):
                gradient = value
            case let .image(image: image):
                backgroundImage = image
            case let .visualEffect(style: value):
                backgroundEffect = value.blurEffect(for: traitCollection,
                                                    mode: style.displayMode)
            case .clear:
                break
            }

            gradientView.style = GradientView.Style(gradient: gradient,
                                                    displayMode: style.displayMode)
            visualEffectView.effect = backgroundEffect
            layer.backgroundColor = backgroundColor.cgColor
            imageView.image = backgroundImage
        }
    }

    override func traitCollectionDidChange(_: UITraitCollection?) {
        guard let style = style else { return }
        switch style.background {
        case let .color(color: color):
            layer.backgroundColor = color.color(for: traitCollection,
                                                mode: style.displayMode).cgColor
        case let .visualEffect(style: value):
            visualEffectView.effect = value.blurEffect(for: traitCollection,
                                                       mode: style.displayMode)
        default:
            break
        }
    }
}
