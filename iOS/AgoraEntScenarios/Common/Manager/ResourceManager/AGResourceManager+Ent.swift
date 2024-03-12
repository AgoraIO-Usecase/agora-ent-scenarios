//
//  AGResourceManager+Ent.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/12.
//

import AGResourceManager

extension AGResourceManager {
    static func autoDownload() {
        let url = "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/resource/manifest/manifestList"
        AGResourceManager.shared.downloadManifestList(url: url) { _ in
        } completionHandler: { _, _ in
        }
    }
}
