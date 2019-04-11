package com.duke.dpermission2019;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.duke.dpermission.DPermission;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    private void usage() {
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
    }
}
