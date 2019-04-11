# DPermission

## 非常简单好用的动态权限申请工具类，再也不用被系统权限回调逻辑困扰。
***
## 使用方法

```java
DPermission.newInstance(this).setCallback(new DPermission.DCallback() {
            @Override
            public void onResult(ArrayList<DPermission.PermissionInfo> permissionInfoList) {
                // 权限申请回调，处理自己的业务逻辑
                
            }
        }).startRequest(需要申请的权限数组);
```

***
## 详细用法 Demo
```java
// yourPermissionString 待申请的权限
        String[] yourPermissionString = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE};
        DPermission.newInstance(this).setCallback(new DPermission.DCallback() {
            @Override
            public void onResult(ArrayList<DPermission.PermissionInfo> permissionInfoList) {
                // 权限申请回调
                if (permissionInfoList == null) {
                    return;
                }
                // 循环申请的权限集合
                int size = permissionInfoList.size();
                DPermission.PermissionInfo permissionInfo;
                for (int i = 0; i < size; i++) {
                    // 得到某一个权限申请的返回信息对象
                    permissionInfo = permissionInfoList.get(i);
                    if (permissionInfo == null) {
                        continue;
                    }

                    // 比喻：读写 SDCard 权限
                    if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissionInfo)) {
                        if (permissionInfo.isGranted) {
                            // 已经授权
                        } else {
                            // 未授权
                        }
                    }

                }
            }
        }).startRequest(yourPermissionString);
```
