Pod::Spec.new do |s| 
   s.name          = "AgoraRtm" 
   s.version       = "2.2.1"
   s.summary       = "Agora iOS SDK"
   s.description   = "iOS library for agora A/V communication, broadcasting and data channel service." 
   s.homepage      = "https://docs.agora.io/en/Agora%20Platform/downloads" 
   s.license       = { "type" => "Copyright", "text" => "Copyright 2022 agora.io. All rights reserved.n"} 
   s.author        = { "Agora Lab" => "developer@agora.io" } 
   s.platform      = :ios,9.0 
   s.source        = { :http => 'https://download.agora.io/sdk/release/AgoraRtm-2.2.1.zip'}
   
   s.prepare_command = <<-CMD
      rm -rf RTMLibs
      folder="RTMLibs"
      if [ ! -d "$folder" ]; then
         mkdir -p "$folder"
      fi
      curl -L -o RTMLibs/resource.zip https://download.agora.io/sdk/release/AgoraRtm-2.2.1.zip
      unzip  -n RTMLibs/resource.zip -d RTMLibs
      rm -rf RTMLibs/aosl.xcframework
      rm -rf RTMLibs/resource.zip
   CMD

   s.vendored_frameworks = 'RTMLibs/*.xcframework'
  
end 
