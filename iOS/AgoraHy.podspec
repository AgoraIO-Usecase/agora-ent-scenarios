Pod::Spec.new do |s| 
   s.name          = "AgoraHy"
   s.version       = "1.0.0"
   s.summary       = "Agora iOS SDK"
   s.description   = "iOS library for agora A/V communication, broadcasting and data channel service." 
   s.homepage      = "https://docs.agora.io/en/Agora%20Platform/downloads" 
   s.license       = { "type" => "Copyright", "text" => "Copyright 2022 agora.io. All rights reserved.n"} 
   s.author        = { "Agora Lab" => "developer@agora.io" } 
   s.platform      = :ios,9.0 
   s.source        = { :http => 'https://download.agora.io/sdk/release/AgoraRtm-2.2.1.zip'}
   
   s.prepare_command = <<-CMD
      rm -rf AgoraHyLibs
      folder="AgoraHyLibs"
      if [ ! -d "$folder" ]; then
         mkdir -p "$folder"
      fi
      curl -L -o AgoraHyLibs/AgoraHy.zip https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/resource/asr/ios/AgoraHy.zip
      unzip -o AgoraHyLibs/AgoraHy.zip -d AgoraHyLibs
   CMD

   s.vendored_frameworks = 'AgoraHyLibs/*.*framework'
  
end 
