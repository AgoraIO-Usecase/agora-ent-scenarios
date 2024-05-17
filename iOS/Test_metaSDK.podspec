Pod::Spec.new do |spec|
   spec.name          = "Test_metaSDK"
   spec.version       = "1.2.1"
   spec.summary       = "Agora iOS video SDK"
   spec.description   = "iOS library for agora A/V communication, broadcasting and data channel service."
   spec.homepage      = "https://docs.agora.io/en/Agora%20Platform/downloads"
   spec.license       = { "type" => "Copyright", "text" => "Copyright 2018 agora.io. All rights reserved.n"}
   spec.author        = { "Agora Lab" => "developer@agora.io" }
   spec.platform      = :ios
   spec.source        = { :http => "https://download.agora.io/demo/test/AgoraMetaKitSDKv1_2_1.zip"}
   spec.vendored_frameworks = "AgoraMetaKitSDK/*.xcframework"
   #spec.source        = { :http => 'https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/sdk/AgoraRtmKit2_0.2.0.zip'}
   #spec.vendored_frameworks = "AgoraRtmKit2/AgoraRtmKit2.xcframework"
   spec.requires_arc  = true
   spec.ios.deployment_target  = '9.0'
 end
