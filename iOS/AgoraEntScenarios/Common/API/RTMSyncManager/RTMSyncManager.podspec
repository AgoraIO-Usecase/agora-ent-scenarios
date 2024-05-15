#
# Be sure to run `pod lib lint RTMSyncManager.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'RTMSyncManager'
  s.version          = '0.1.0'
  s.summary          = 'A short description of RTMSyncManager.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/AgoraIO-Usecase/RTMSyncManager'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'Agora' => 'uikit@agora.io' }
  s.source           = { :git => 'https://github.com/AgoraIO-Usecase/RTMSyncManager.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'

  s.ios.deployment_target = '12.0'

  s.source_files = 'RTMSyncManager/Classes/**/*'
  
    
  s.static_framework = true
  
  s.swift_version = '5.0'
  
  # s.resource_bundles = {
  #   'RTMSyncManager' => ['RTMSyncManager/Assets/*.png']
  # }

  # s.public_header_files = 'Pod/Classes/**/*.h'
  # s.frameworks = 'UIKit', 'MapKit'
    s.dependency 'AgoraRtm', '~> 2.2.0'
    s.dependency 'YYModel'
 #   s.dependency 'Alamofire'
end
