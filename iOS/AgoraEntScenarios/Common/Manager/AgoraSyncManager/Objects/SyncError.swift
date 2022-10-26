//
//  Error.swift
//  SyncManager
//
//  Created by ZYP on 2021/11/15.
//

import Foundation

@objc public class SyncError: NSObject, LocalizedError {
    public let message: String
    @objc public let code: Int
    let domain: SyncErrorDomain
    public var domainName: String { domain.name }

    @objc override public var description: String {
        return "[\(domainName)]: " + "(\(code))" + message
    }

    @objc public var errorDescription: String? {
        return description
    }

    public convenience init(message: String,
                            code: Int)
    {
        self.init(message: message,
                  code: code,
                  domain: .rtm)
    }

    required init(message: String,
                  code: Int,
                  domain: SyncErrorDomain)
    {
        self.message = message
        self.code = code
        self.domain = domain
    }

    @objc static func ask(message: String,
                          code: Int) -> SyncError
    {
        SyncError(message: message, code: code, domain: .ask)
    }
}

enum SyncErrorDomain {
    case rtm
    case ask

    var name: String {
        switch self {
        case .rtm:
            return "Agora Rtm"
        case .ask:
            return "Agora Ask"
        }
    }
}

//  ask 错误码
//  ErrOK                              = 0,
//  ErrUnknown                         = 1,
//  ErrEmptyKey                        = 2,
//  ErrKeyNotFound                     = 3,
//  ErrValueProvided                   = 4,
//  ErrLeaseProvided                   = 5,
//
//  ErrTooManyOps                      = 6,
//  ErrDuplicateKey                    = 7,
//  ErrCompacted                       = 8,
//  ErrFutureRev                       = 9,
//  ErrNoSpace                         = 10,
//
//  ErrLeaseNotFound                   = 11,
//  ErrLeaseExist                      = 12,
//  ErrLeaseTTLTooLarge                = 13,
//
//  ErrMemberExist                     = 14,
//  ErrPeerURLExist                    = 15,
//  ErrMemberNotEnoughStarted          = 16,
//  ErrMemberBadURLs                   = 17,
//  ErrMemberNotFound                  = 18,
//  ErrMemberNotLearner                = 19,
//  ErrLearnerNotReady                 = 20,
//  ErrTooManyLearners                 = 21,
//
//  ErrRequestTooLarge                 = 22,
//  ErrRequestTooManyRequests          = 23,
//
//  ErrRootUserNotExist                = 24,
//  ErrRootRoleNotExist                = 25,
//  ErrUserAlreadyExist                = 26,
//  ErrUserEmpty                       = 27,
//  ErrUserNotFound                    = 28,
//  ErrRoleAlreadyExist                = 29,
//  ErrRoleNotFound                    = 30,
//  ErrRoleEmpty                       = 31,
//  ErrAuthFailed                      = 32,
//  ErrPermissionDenied                = 33,
//  ErrRoleNotGranted                  = 34,
//  ErrPermissionNotGranted            = 35,
//  ErrAuthNotEnabled                  = 36,
//  ErrInvalidAuthToken                = 37,
//  ErrInvalidAuthMgmt                 = 38,
//
//  ErrNoLeader                        = 39,
//  ErrNotLeader                       = 40,
//  ErrLeaderChanged                   = 41,
//  ErrNotCapable                      = 42,
//  ErrStopped                         = 43,
//  ErrTimeout                         = 44,
//  ErrTimeoutDueToLeaderFail          = 45,
//  ErrTimeoutDueToConnectionLost      = 46,
//  ErrUnhealthy                       = 47,
//  ErrCorrupt                         = 48,
//  ErrGPRCNotSupportedForLearner      = 49,
//  ErrBadLeaderTransferee             = 50,
//
//  ErrNetwork                         = 101,
//  ErrUnknownUri                      = 201,
//  ErrMetaProxyOnly                   = 202,
//  ErrMetaClientOnly                  = 203,
//  ErrMetaInvalidFirstPack            = 204,
//  ErrMetaServerNotAvailable          = 205,
//  ErrUnknownHash                     = 206,
//
//  ErrMetaInvalidKey                  = 301,
//  ErrMetaUnknownHost                 = 302,

//  kCollNotEmpty                      = 1001,
//  kObjectNotFound                    = 1002,
//  kDocExists                         = 1003,
//  kNoLeaseGranted                    = 1004,
//  kNotSubscribed                     = 1005,
//  kLocalFirstNotEnabled              = 1006
