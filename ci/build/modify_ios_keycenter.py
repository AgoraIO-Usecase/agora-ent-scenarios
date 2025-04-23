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
            'APP_ID': env_vars.get('APP_ID', ''),
            'APP_CERT': env_vars.get('APP_CERT', ''),
            'IM_APP_KEY': env_vars.get('IM_APP_KEY', ''),
            'IM_CLIENT_ID': env_vars.get('IM_CLIENT_ID', ''),
            'IM_CLIENT_SECRET': env_vars.get('IM_CLIENT_SECRET', ''),
            'SUB_APP_ID': env_vars.get('SUB_APP_ID', ''),
            'SUB_APP_KEY': env_vars.get('SUB_APP_KEY', '')
        }

        # 替换每个变量
        for key, value in replacements.items():
            pattern = f'static let {key} = ".*?"'
            replacement = f'static let {key} = "{value}"'
            content = re.sub(pattern, replacement, content)

        # 写回文件
        with open(keycenter_path, 'w') as f:
            f.write(content)

        print("KeyCenter文件修改成功")
    except Exception as e:
        print(f"修改KeyCenter文件时发生错误: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("用法: python3 modify_keycenter.py <keycenter_path>")
        sys.exit(1)

    keycenter_path = sys.argv[1]
    
    # 获取环境变量
    env_vars = {
        'APP_ID': os.environ.get('APP_ID', ''),
        'APP_CERT': os.environ.get('APP_CERT', ''),
        'IM_APP_KEY': os.environ.get('IM_APP_KEY', ''),
        'IM_CLIENT_ID': os.environ.get('IM_CLIENT_ID', ''),
        'IM_CLIENT_SECRET': os.environ.get('IM_CLIENT_SECRET', ''),
        'SUB_APP_ID': os.environ.get('SUB_APP_ID', ''),
        'SUB_APP_KEY': os.environ.get('SUB_APP_KEY', '')
    }

    modify_keycenter(keycenter_path, env_vars) 