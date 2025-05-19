//
//  File.swift
//  AgoraEntScenarios
//
//  Created by qinhui on 2025/5/13.
//

import Foundation
import AgoraCommon

class SecurityManager {
    
    // Paths required for jailbreak detection
    static private let jailbreakToolPaths = [
        "/Applications/Cydia.app",
        "/Library/MobileSubstrate/MobileSubstrate.dylib",
        "/bin/bash",
        "/usr/sbin/sshd",
        "/etc/apt"
    ]
    
    // Expected developer team ID for code signing check
    static private let expectedTeamID = "48TB6ZZL5S"
    static private let logTag = "SecurityCheck"
    
    /// Main security check method - checks for jailbreak and tampering
    /// - Returns: true if device environment is secure, false if any security issue detected
    static func check() -> Bool {
        // Check if device is jailbroken
        if isJailBroken() {
            CommonLogger.default_info("Security check failed: Device is jailbroken", tag: logTag)
            return false
        }
        
        // Check if app has been tampered with
        if checkMachOTampered() {
            CommonLogger.default_info("Security check failed: App has been tampered with", tag: logTag)
            return false
        }
        
        // Check code signing
        if !checkCodeSign(withProvisionID: expectedTeamID) {
            CommonLogger.default_info("Security check failed: Invalid code signature", tag: logTag)
            return false
        }
        
        // All security checks passed
        CommonLogger.default_info("Security check passed: Environment is secure", tag: logTag)
        return true
    }
    
    /// Check if the device is jailbroken
    /// - Returns: If the device is jailbroken, return true; otherwise, return false.
    static private func isJailBroken() -> Bool {
        if isSimulator() {
            return false
        }
        
        for path in jailbreakToolPaths {
            if FileManager.default.fileExists(atPath: path) {
                CommonLogger.default_info("Device is jailbroken!", tag: logTag)
                return true
            }
        }
        
        CommonLogger.default_info("Device is not jailbroken", tag: logTag)
        return false
    }
    
    /// Check whether it is currently running on an emulator
    /// - Returns: Returns true if it is an emulator, otherwise returns false
    static private func isSimulator() -> Bool {
        #if targetEnvironment(simulator)
        return true
        #else
        return false
        #endif
    }
    
    /// Check if the Mach-O file has been tampered with
    /// - Returns: Returns true if tampered, otherwise returns false
    static private func checkMachOTampered() -> Bool {
        let bundle = Bundle.main
        if let info = bundle.infoDictionary {
            if info["SignerIdentity"] != nil {
                // If this key exists, it means the app has been repackaged
                CommonLogger.default_info("The app has been repackaged!", tag: logTag)
                return true
            }
        }
        
        CommonLogger.default_info("The app has not been repackaged", tag: logTag)
        return false
    }
    
    static private func checkCodeSign(withProvisionID provisionID: String) -> Bool {
        let embeddedPath = Bundle.main.path(forResource: "embedded", ofType: "mobileprovision")
        if let embeddedPath = embeddedPath, FileManager.default.fileExists(atPath: embeddedPath) {
            CommonLogger.default_info("Found mobileprovision file, path: \(embeddedPath)", tag: logTag)
            
            // Try different ways to read the file
            var embeddedProvisioning: String? = nil
            
            // Method 1: ASCII encoding
            embeddedProvisioning = try? String(contentsOfFile: embeddedPath, encoding: .ascii)
            
            // Method 2: UTF8 encoding
            if embeddedProvisioning == nil {
                embeddedProvisioning = try? String(contentsOfFile: embeddedPath, encoding: .utf8)
                CommonLogger.default_info("Try UTF8 encoding \(embeddedProvisioning != nil ? "succeeded" : "failed")", tag: logTag)
            }
            
            // Method 3: Binary reading
            if embeddedProvisioning == nil {
                do {
                    let data = try Data(contentsOf: URL(fileURLWithPath: embeddedPath))
                    CommonLogger.default_info("Binary reading succeeded, size: \(data.count) bytes", tag: logTag)
                    
                    // Try different encodings to convert to string
                    let encodings: [String.Encoding] = [.ascii, .utf8, .isoLatin1, .isoLatin2, .macOSRoman]
                    for encoding in encodings {
                        if let string = String(data: data, encoding: encoding) {
                            embeddedProvisioning = string
                            CommonLogger.default_info("Successfully converted binary data using \(encoding) encoding", tag: logTag)
                            break
                        }
                    }
                } catch {
                    CommonLogger.default_info("Binary reading failed: \(error.localizedDescription)", tag: logTag)
                }
            }
            
            // If file content was successfully read
            if let embeddedProvisioning = embeddedProvisioning {
                let embeddedProvisioningLines = embeddedProvisioning.components(separatedBy: .newlines)
                CommonLogger.default_info("Successfully parsed into \(embeddedProvisioningLines.count) lines", tag: logTag)
                
                for i in 0..<embeddedProvisioningLines.count {
                    if embeddedProvisioningLines[i].range(of: "application-identifier") != nil {
                        CommonLogger.default_info("Found application-identifier at line \(i)", tag: logTag)
                        
                        if i + 1 < embeddedProvisioningLines.count {
                            let line = embeddedProvisioningLines[i + 1]
                            CommonLogger.default_info("Next line content: \(line)", tag: logTag)
                            
                            guard let fromPositionRange = line.range(of: "<string>") else {
                                CommonLogger.default_info("Cannot find <string> tag", tag: logTag)
                                continue
                            }
                            let fromPosition = line.index(fromPositionRange.upperBound, offsetBy: 0)
                            
                            guard let toPositionRange = line.range(of: "</string>") else {
                                CommonLogger.default_info("Cannot find </string> tag", tag: logTag)
                                continue
                            }
                            let toPosition = toPositionRange.lowerBound
                            
                            let fullIdentifier = String(line[fromPosition..<toPosition])
                            CommonLogger.default_info("Extracted identifier: \(fullIdentifier)", tag: logTag)
                            
                            let identifierComponents = fullIdentifier.components(separatedBy: ".")
                            if let appIdentifier = identifierComponents.first {
                                CommonLogger.default_info("Extracted TeamID: \(appIdentifier), Expected value: \(provisionID)", tag: logTag)
                                
                                // Compare signature ID
                                if appIdentifier != provisionID {
                                    return false
                                } else {
                                    return true
                                }
                            }
                        }
                    }
                }
            } else {
                CommonLogger.default_info("Unable to read mobileprovision file content after trying multiple encodings", tag: logTag)
            }
        } else {
            CommonLogger.default_info("Cannot find mobileprovision file", tag: logTag)
        }
        return true
    }
}
