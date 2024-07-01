Pod::Spec.new do |s| 
   s.name          = "AgoraRtm" 
   s.version       = "2.2.0"
   s.summary       = "Agora iOS SDK" 
   s.description   = "iOS library for agora A/V communication, broadcasting and data channel service." 
   s.homepage      = "https://docs.agora.io/en/Agora%20Platform/downloads" 
   s.license       = { "type" => "Copyright", "text" => "Copyright 2022 agora.io. All rights reserved.n"} 
   s.author        = { "Agora Lab" => "developer@agora.io" } 
   s.platform      = :ios,9.0 
   s.source        = { :http => 'https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/release/sdk/AgoraRtm2.2.0.zip'}
   
   s.prepare_command = <<-CMD
       curl -L -o rtmSDKResource.zip https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/release/sdk/AgoraRtm2.2.0.zip
       unzip rtmSDKResource.zip -d libs
       rm rtmSDKResource.zip
   CMD

   s.vendored_frameworks = 'libs/AgoraRtmKit.xcframework'
  
end 
