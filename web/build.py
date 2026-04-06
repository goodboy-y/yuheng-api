#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
前端构建脚本
功能：
1. 执行 npm run build 打包前端项目
2. 将打包后的文件复制到 src/main/resources/static 目录
"""

import os
import sys
import shutil
import subprocess
from pathlib import Path

# 获取项目根目录
WEB_DIR = Path(__file__).parent.absolute()
PROJECT_ROOT = WEB_DIR.parent
DIST_DIR = WEB_DIR / "dist"
STATIC_DIR = PROJECT_ROOT / "src" / "main" / "resources" / "static"


def run_command(cmd, cwd=None):
    """执行命令并打印输出"""
    print(f"执行命令: {' '.join(cmd)}")
    try:
        result = subprocess.run(
            cmd,
            cwd=cwd,
            check=True,
            capture_output=False,
            shell=isinstance(cmd, str)
        )
        return result.returncode == 0
    except subprocess.CalledProcessError as e:
        print(f"命令执行失败: {e}")
        return False


def build_frontend():
    """构建前端项目"""
    print("=" * 60)
    print("开始构建前端项目...")
    print("=" * 60)

    # 检查 node_modules 是否存在
    node_modules = WEB_DIR / "node_modules"
    if not node_modules.exists():
        print("未检测到 node_modules，先执行 npm install...")
        if not run_command(["npm", "install"], cwd=WEB_DIR):
            print("npm install 失败")
            return False

    # 执行构建
    if not run_command(["npm", "run", "build"], cwd=WEB_DIR):
        print("前端构建失败")
        return False

    # 检查 dist 目录是否存在
    if not DIST_DIR.exists():
        print(f"构建完成，但未找到 dist 目录: {DIST_DIR}")
        return False

    print("前端构建成功!")
    return True


def copy_to_static():
    """将构建产物复制到 static 目录"""
    print("=" * 60)
    print("复制构建产物到 static 目录...")
    print("=" * 60)

    # 如果 static 目录存在，先清空
    if STATIC_DIR.exists():
        print(f"清空现有 static 目录: {STATIC_DIR}")
        shutil.rmtree(STATIC_DIR)

    # 创建 static 目录
    STATIC_DIR.mkdir(parents=True, exist_ok=True)

    # 复制 dist 目录内容到 static
    print(f"复制 {DIST_DIR} -> {STATIC_DIR}")
    for item in DIST_DIR.iterdir():
        if item.is_dir():
            shutil.copytree(item, STATIC_DIR / item.name)
        else:
            shutil.copy2(item, STATIC_DIR / item.name)

    print("复制完成!")
    return True


def main():
    """主函数"""
    print("\n" + "=" * 60)
    print("前端构建与部署脚本")
    print("=" * 60 + "\n")

    # 步骤1: 构建前端
    # if not build_frontend():
    #     print("\n构建失败，退出")
    #     sys.exit(1)

    # 步骤2: 复制到 static 目录
    if not copy_to_static():
        print("\n复制文件失败，退出")
        sys.exit(1)

    print("\n" + "=" * 60)
    print("所有操作完成!")
    print(f"前端资源已部署到: {STATIC_DIR}")
    print("=" * 60 + "\n")


if __name__ == "__main__":
    main()
