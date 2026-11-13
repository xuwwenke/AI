#!/usr/bin/env python3
# 测试权限绕过是否成功

import sys
sys.path.insert(0, '.')

from myapp.security import MyappSecurityManager

# 创建一个模拟的appbuilder对象
class MockAppBuilder:
    def __init__(self):
        pass

# 测试has_access方法
print("测试权限绕过...")
print("=" * 50)

# 创建MyappSecurityManager实例
sm = MyappSecurityManager(MockAppBuilder())

# 测试不同权限和视图的组合
permissions = ["can_add", "can_edit", "can_delete", "can_list", "can_show"]
views = ["User", "Project", "Task", "Model"]

for permission in permissions:
    for view in views:
        result = sm.has_access(permission, view)
        print(f"权限: {permission}, 视图: {view}, 结果: {result}")

print("=" * 50)
print("测试完成！")
print("如果所有结果都是True，说明权限绕过成功。")
