#
# Be sure to run `pod lib lint VideoLoaderAPI.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'VideoLoaderAPI'
  s.version          = '0.1.3'
  s.summary          = 'A short description of VideoLoaderAPI.'

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
  
  s.xcconfig = {'ENABLE_BITCODE' => 'NO'}
  
  s.static_framework = true
  s.swift_version = '5.0'

  s.source_files = 'VideoLoaderAPI/Classes/**/*'
  
  # s.resource_bundles = {
  #   'VideoLoaderAPI' => ['VideoLoaderAPI/Assets/*.png']
  # }

  # s.public_header_files = 'Pod/Classes/**/*.h'
  s.dependency 'AgoraRtcEngine_iOS'
end
