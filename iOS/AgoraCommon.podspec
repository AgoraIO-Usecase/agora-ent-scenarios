#
# Be sure to run `pod lib lint VideoLoaderAPI.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'AgoraCommon'
  s.version          = '0.1.0'
  s.summary          = 'A short description of AgoraCommon.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/AgoraIO-Community/VideoLoaderAPI'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'Agora Lab' => 'developer@agora.io' }
  s.source           = { :git => 'https://github.com/AgoraIO-Community/VideoLoaderAPI.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'
    
  s.ios.deployment_target = '10.0'
  s.prefix_header_contents = '
    #if __has_include(<AgoraCommon/AgoraCommon-Swift.h>)
        #import <AgoraCommon/AgoraCommon-Swift.h>
    #else
        #import "AgoraCommon-Swift.h"
    #endif
  '
  s.xcconfig = {'ENABLE_BITCODE' => 'NO'}
  
  s.static_framework = true
  s.swift_version = '5.0'

  s.source_files = 'AgoraCommon/**/*.{h,m,swift}'
  
  # s.resource_bundles = {
  #   'VideoLoaderAPI' => ['VideoLoaderAPI/Assets/*.png']
  # }

  # s.public_header_files = 'AgoraCommon/Common/**/AESMacro.h'
  s.dependency 'AgoraRtcEngine_Special_iOS'
  s.dependency 'YYModel'
  s.dependency 'YYCategories'
  s.dependency 'SwiftyBeaver'
  s.dependency 'Bugly'
  s.dependency 'SocketRocket'
  s.dependency 'AgoraRtm_iOS'
  s.dependency 'AgoraSyncManager'
  s.dependency 'ZSwiftBaseLib'
  s.dependency 'SVProgressHUD'
  s.dependency 'Masonry'
  s.dependency 'AFNetworking'
end

