//
//  AGResourceManager+Ent.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/12.
//

import AGResourceManager

extension AGResourceManager {
    static func autoDownload(uris: [String]? = nil, 
                             progress: ((Double)-> Void)? = nil,
                             completion: ((NSError?)-> Void)? = nil) {
        let url = KeyCenter.DynamicResourceUrl ?? ""
        if url.isEmpty {
            completion?(NSError(domain: "dynamicResourceUrl is empty", code: -1))
            return
        }
        
        AGResourceManager.shared.downloadManifestList(url: url) { _ in
        } completionHandler: { _, err in
            if let err = err {
                completion?(err)
                return
            }
            
            var resources: [AGResource] = []
            for uri in uris ?? [] {
                if let resource = AGResourceManager.shared.getResource(uri: uri) {
                    resources.append(resource)
                }
            }
            AGResourceManager.shared.downloadResources(resources: resources) { percent in
                progress?(percent)
            } completion: { err in
                completion?(err)
            }

        }
    }
}
