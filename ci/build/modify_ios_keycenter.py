#!/usr/bin/env python3
import os
import sys
import re

def modify_keycenter(keycenter_path, env_vars):
    """
    修改KeyCenter文件内容
    :param keycenter_path: KeyCenter文件路径
    :param env_vars: 环境变量字典
    """
    if not os.path.exists(keycenter_path):
        print(f"错误: KeyCenter文件不存在: {keycenter_path}")
        sys.exit(1)

    try:
        with open(keycenter_path, 'r') as f:
            content = f.read()

        # 定义需要替换的变量及其对应的环境变量名
        replacements = {
            'AppId': env_vars.get('APP_ID', ''),
            'Certificate': env_vars.get('APP_CERT', ''),
            'IMAppKey': env_vars.get('IM_APP_KEY', ''),
            'IMClientId': env_vars.get('IM_CLIENT_ID', ''),
            'IMClientSecret': env_vars.get('IM_CLIENT_SECRET', ''),
            'SUDMGP_APP_ID': env_vars.get('SUB_APP_ID', ''),
            'SUDMGP_APP_KEY': env_vars.get('SUB_APP_KEY', '')
        }

        # 替换每个变量
        for key, value in replacements.items():
            pattern = f'static (?:let|var) {key}: String.*$'
            replacement = f'static let {key}: String = "{value}"'
            content = re.sub(pattern, replacement, content, flags=re.MULTILINE)

        # 替换DynamicResourceUrl
        manifest_url = env_vars.get('manifest_url', '')
        if manifest_url:
            pattern = r'static (?:let|var) DynamicResourceUrl: String\?.*$'
            replacement = f'static let DynamicResourceUrl: String? = "{manifest_url}"'
            content = re.sub(pattern, replacement, content, flags=re.MULTILINE)

        # 写回文件
        with open(keycenter_path, 'w') as f:
            f.write(content)

        print("KeyCenter文件修改成功")
    except Exception as e:
        print(f"修改KeyCenter文件时发生错误: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    # 测试路径
    test_path = "/Users/jonathan/Desktop/SRC/agora-ent-scenarios/iOS/AgoraEntScenarios/KeyCenter.swift"
    
    # 测试环境变量
    test_env_vars = {
        'APP_ID': 'aabb',
        'APP_CERT': 'aabb',
        'IM_APP_KEY': 'aabb',
        'IM_CLIENT_ID': 'aabb',
        'IM_CLIENT_SECRET': 'aabb',
        'SUB_APP_ID': 'aabb',
        'SUB_APP_KEY': 'aabb',
        'manifest_url': 'aabb'
    }

    print("开始测试KeyCenter文件修改...")
    modify_keycenter(test_path, test_env_vars)
    print("测试完成") 