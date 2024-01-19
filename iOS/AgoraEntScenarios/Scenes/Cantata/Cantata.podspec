#
# Be sure to run `pod lib lint VideoLoaderAPI.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'Cantata'
  s.version          = '0.1.0'
  s.summary          = 'A short description of Cantata.'

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
    
  s.ios.deployment_target = '13.0'
  
  s.xcconfig = {'ENABLE_BITCODE' => 'NO'}
  
  s.static_framework = true
  s.swift_version = '5.0'

  s.source_files = 'Cantata/**/*.{h,m,swift}'
  s.prefix_header_contents = '
  #if __has_include(<Cantata/Cantata-Swift.h>)
      #import <Cantata/Cantata-Swift.h>
      #import "Cantata-Prefix.h"
  #endif
  '
#  s.pod_target_xcconfig = {
 #   'SWIFT_OBJC_BRIDGING_HEADER' => 'Cantata/KTV/**/Cantata-Bridging-Header.h'
 # }
 
  s.resources = ['Cantata/**/*.bundle', 'Cantata/**/*.xib']

#  s.public_header_files = ['Cantata/**/CantataPlugin.h','Cantata/**/VLKTVTopView.h', 'Cantata/**/VLKTVSelBgModel.h']
  s.public_header_files = [
    'Cantata/**/CantataPlugin.h',
    'Cantata/**/VLKTVTopView.h',
    'Cantata/**/VLKTVSelBgModel.h',
    'Cantata/**/LSTPopView+KTVModal.h',
    "Cantata/**/VLPopMoreSelView.h",
    "Cantata/**/VLPopSelBgView.h",
    "Cantata/**/VLDropOnLineView.h",
    "Cantata/**/VLAudioEffectPicker.h",
    "Cantata/**/VLBadNetWorkView.h",
    "Cantata/**/VLPopSongList.h",
    "Cantata/**/VLEffectView.h",
    "Cantata/**/DHCVLKTVSettingView.h",
    "Cantata/**/VLEarSettingView.h",
    "Cantata/**/DHCDebugView.h",
    "Cantata/**/KTVDebugInfo.h",
    "Cantata/**/KTVDebugManager.h",
    "Cantata/**/AppContext+DHCKTV.h"
    ]
    
  s.dependency 'AgoraRtcEngine_Special_iOS'
  s.dependency 'AgoraCommon'
  s.dependency 'LSTPopView'
  s.dependency 'JXCategoryView'
  s.dependency 'ScoreEffectUI'
  s.dependency 'AgoraLyricsScore'
  s.dependency 'AgoraCommon'
  s.dependency 'SDWebImage'
  s.dependency 'Zip'
  s.dependency 'MJRefresh'
  s.dependency 'AUIKitCore/UI'
  s.dependency 'SVProgressHUD'
  s.dependency 'ZSwiftBaseLib'
  s.dependency 'Masonry'
end

