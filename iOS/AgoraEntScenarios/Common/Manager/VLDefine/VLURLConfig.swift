//
//  VLURLConfig.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/13.
//

import UIKit

enum VLURLConfig {
    static let kURLPathLogin = "/api-login/users/login" // 登录
    static let kURLPathDestroyUser = "/api-login/users/cancellation"; //注销用户
    static let kURLPathVerifyCode = "/api-login/users/verificationCode" //发送验证码
    static let kURLPathUploadImage = "/api-login/upload" //上传图片
    static let kURLPathUploadLog = "/api-login/upload/log" //上传log
    static let kURLPathFeedback = "/api-login/feedback/upload" //反馈
    static let kURLPathH5UserAgreement = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/privacy/service.html"
    static let kURLPathH5Privacy = "https://fullapp.oss-cn-beijing.aliyuncs.com/scenarios/privacy.html"
}
