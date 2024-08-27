#
# Be sure to run `pod lib lint AIChat.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'AIChat'
  s.version          = '0.1.0'
  s.summary          = 'A short description of AIChat.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/zjc19891106/AIChat'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'zjc19891106' => '984065974@qq.com' }
  s.source           = { :git => 'https://github.com/zjc19891106/AIChat.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'

  s.ios.deployment_target = '13.0'

  s.source_files = 'AIChat/Classes/**/*'
  s.static_framework = true
  
  s.resource_bundles = {
    'AIChat' => ['AIChat/Assets/*.bundle']
  }
  # s.public_header_files = 'Pod/Classes/**/*.h'
  s.frameworks = 'UIKit', 'Foundation', 'AVFoundation'
  s.dependency 'AFNetworking'
  s.dependency 'SDWebImage'
  s.dependency 'AUIKitCore/UI'
  s.dependency 'SVProgressHUD'
  s.dependency 'Agora_Chat_iOS'
  s.dependency 'ZSwiftBaseLib'
  s.dependency 'AgoraCommon'
end
