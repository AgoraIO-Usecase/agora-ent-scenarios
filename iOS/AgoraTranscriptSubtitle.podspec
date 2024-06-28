Pod::Spec.new do |spec|
  spec.name         = "AgoraTranscriptSubtitle"
  spec.version      = "0.0.1"
  spec.summary      = "AgoraTranscriptSubtitle"
  spec.description  = "AgoraTranscriptSubtitle"

  spec.homepage     = "https://github.com/AgoraIO-Community"
  spec.license      = "MIT"
  spec.author       = { "ZYP" => "zhuyuping@shengwang.cn" }
  spec.source       = { :git => "https://github.com/AgoraIO-Community/LrcView-iOS.git", :tag => '2.0.0.131' }
  spec.source_files  = ["AgoraTranscriptSubtitle/Class/**/*.{swift,h,m}"]
  spec.public_header_files = "AgoraTranscriptSubtitle/Class/**/*.h"
  spec.pod_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64', 'DEFINES_MODULE' => 'YES' }
  spec.user_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64', 'DEFINES_MODULE' => 'YES' }
  spec.ios.deployment_target = '10.0'
  spec.swift_versions = "5.0"
  spec.requires_arc  = true
  spec.dependency 'AgoraComponetLog', "0.0.2"
  spec.dependency 'Protobuf', '3.22.1'
  spec.resource_bundles = {
    'AgoraTranscriptSubtitleBundle' => ['AgoraTranscriptSubtitle/Resources/*.xcassets']
  }
  
  spec.test_spec 'Tests' do |test_spec|
    test_spec.source_files = "AgoraTranscriptSubtitle/Tests/**/*.{swift}"
    test_spec.resource = "AgoraTranscriptSubtitle/Tests/Resource/*"
    test_spec.frameworks = 'UIKit','Foundation'
#    test_spec.requires_app_host = true
  end
  
end
