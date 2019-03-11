package com.duke.dpermission;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class DemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        DPermission dPermission = new DPermission(this);
        dPermission.setCallback(new DPermission.PermissionCallback() {
            @Override
            public void onResult(ArrayList<DPermission.PermissionBean> permissionBeans) {

            }
        }).startRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }
}
