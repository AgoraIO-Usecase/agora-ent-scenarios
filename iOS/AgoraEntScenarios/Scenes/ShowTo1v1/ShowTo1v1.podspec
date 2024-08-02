#
# Be sure to run `pod lib lint ShowTo1v1.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'ShowTo1v1'
  s.version          = '0.1.0'
  s.summary          = 'A short description of ShowTo1v1.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/AgoraIO-Community/ShowTo1v1'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'Agora Lab' => 'developer@agora.io' }
  s.source           = { :git => 'https://github.com/AgoraIO-Community/ShowTo1v1.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'

  s.ios.deployment_target = '13.0'
  
  s.xcconfig = {'ENABLE_BITCODE' => 'NO'}
  
  s.static_framework = true
  s.swift_version = '5.0'

  s.source_files = 'ShowTo1v1/Classes/**/*'
  s.resource = 'ShowTo1v1/Resources/*.bundle'
  
  # s.resource_bundles = {
  #   'ShowTo1v1' => ['ShowTo1v1/Assets/*.png']
  # }

  # s.public_header_files = 'Pod/Classes/**/*.h'
  # s.frameworks = 'UIKit', 'MapKit'
  s.dependency 'CallAPI'
  s.dependency 'VideoLoaderAPI'
  s.dependency 'AgoraSyncManager'
  s.dependency 'YYModel'
  s.dependency 'YYCategories'
  s.dependency 'SDWebImage'
  s.dependency 'SnapKit'
  s.dependency 'SwiftyBeaver'
  s.dependency 'SDWebImage'
  s.dependency 'FLAnimatedImage'
  s.dependency 'AgoraCommon'
end
