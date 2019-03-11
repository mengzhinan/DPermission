/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duke.dpermission;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @Author: duke
 * @DateTime: 2019-03-11 15:15
 * @Description: 请求权限代理类
 */
public class DPermission {

    private static final String TAG_FRAGMENT = String.valueOf(DPermission.class.getName().hashCode());

    private WeakReference<DPermissionFragment> mFragmentWeakReference;

    private PermissionCallback mPermissionCallback;

    public DPermission setCallback(PermissionCallback permissionCallback) {
        this.mPermissionCallback = permissionCallback;
        return this;
    }

    private DPermissionFragment getCurrentFragment() {
        if (mFragmentWeakReference != null && mFragmentWeakReference.get() != null) {
            return mFragmentWeakReference.get();
        }
        return null;
    }

    public DPermission(@NonNull final FragmentActivity activity) {
        this(activity.getSupportFragmentManager());
    }

    public DPermission(@NonNull final Fragment fragment) {
        this(fragment.getChildFragmentManager());
    }

    public DPermission(@NonNull final FragmentManager fragmentManager) {
        DPermissionFragment permissionsFragment = (DPermissionFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT);
        if (permissionsFragment == null) {
            permissionsFragment = new DPermissionFragment();
            fragmentManager
                    .beginTransaction()
                    .add(permissionsFragment, TAG_FRAGMENT)
                    .commitNow();
        }
        mFragmentWeakReference = new WeakReference<>(permissionsFragment);
    }

    private void clearInnerFragment(@NonNull final FragmentActivity activity) {
        clearInnerFragment(activity.getSupportFragmentManager());
    }

    private void clearInnerFragment(@NonNull final Fragment fragment) {
        clearInnerFragment(fragment.getChildFragmentManager());
    }

    private void clearInnerFragment(@NonNull final FragmentManager fragmentManager) {
        DPermissionFragment permissionsFragment = (DPermissionFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT);
        if (permissionsFragment != null) {
            fragmentManager
                    .beginTransaction()
                    .remove(permissionsFragment)
                    .commitNow();
        }
    }

    /**
     * 过滤无效的权限
     *
     * @param permissions
     * @return
     */
    private String[] filterPermissions(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return null;
        }
        HashSet<String> set = new HashSet<>();
        for (String permission : permissions) {
            if (isEmpty(permission)) {
                continue;
            }
            set.add(permission);
        }
        String[] permissionResult = new String[set.size()];
        Iterator<String> iterator = set.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            String p = iterator.next();
            if (isEmpty(p)) {
                continue;
            }
            permissionResult[index] = p;
            index++;
        }
        return permissionResult;
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().length() <= 0;
    }

    /**
     * 上层请求权限入口方法
     *
     * @param permissions 待申请的权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void startRequest(String... permissions) {
        permissions = filterPermissions(permissions);
        if (permissions == null || permissions.length == 0) {
            return;
        }

        ArrayList<PermissionBean> notRequestList = new ArrayList<>(permissions.length);
        ArrayList<String> needRequestList = new ArrayList<>(permissions.length);

        for (String permission : permissions) {
            if (isEmpty(permission)) {
                continue;
            }
            if (isGranted(permission)) {
                notRequestList.add(new PermissionBean(permission, true, false));
                continue;
            }
            if (isRevoked(permission)) {
                notRequestList.add(new PermissionBean(permission, false, false));
                continue;
            }

            needRequestList.add(permission);
        }

        if (mPermissionCallback != null && !notRequestList.isEmpty()) {
            mPermissionCallback.onResult(notRequestList);
        }

        if (!needRequestList.isEmpty()) {
            requestPermissionsFromFragment(needRequestList.toArray(new String[needRequestList.size()]));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionsFromFragment(String[] permissions) {
        if (getCurrentFragment() == null) {
            return;
        }
        getCurrentFragment().requestPermissions(permissions);
    }


    /**
     * 如果已经授权，则返回true。<br/>
     * 如果 SDK < 23，则永远返回true。
     */
    private boolean isGranted(String permission) {
        return !isMarshmallow() ||
                (getCurrentFragment() != null && getCurrentFragment().isGranted(permission));
    }

    /**
     * 如果权限已被策略撤销，则返回true。<br/>
     * 如果 SDK < 23 ，则永远返回false。
     */
    private boolean isRevoked(String permission) {
        return isMarshmallow() && getCurrentFragment() != null && getCurrentFragment().isRevoked(permission);
    }

    /**
     * 是否是 >= 23
     *
     * @return 是否需要动态权限适配
     */
    private boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    //============================================================

    /**
     * @Author: duke
     * @DateTime: 2019-03-11 15:12
     * @Description: 请求权限fragment
     */
    public static class DPermissionFragment extends Fragment {

        private static final int PERMISSIONS_REQUEST_CODE = 1111;

        private PermissionCallback mPermissionCallback;

        void setPermissionCallback(PermissionCallback permissionCallback) {
            this.mPermissionCallback = permissionCallback;
        }

        public DPermissionFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @TargetApi(Build.VERSION_CODES.M)
        void requestPermissions(@NonNull String... permissions) {
            // 底层请求权限的方法
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
        }

        @TargetApi(Build.VERSION_CODES.M)
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            // 系统权限回调

            if (requestCode != PERMISSIONS_REQUEST_CODE) {
                return;
            }

            ArrayList<PermissionBean> permissionResultList = new ArrayList<>(permissions.length);

            for (String permission : permissions) {
                permissionResultList.add(new PermissionBean(permission,
                        isGranted(permission),
                        shouldShowRequestPermissionRationale(permission)));
            }

            if (mPermissionCallback != null && !permissionResultList.isEmpty()) {
                // 上层回调
                mPermissionCallback.onResult(permissionResultList);
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        boolean isGranted(String permission) {
            final FragmentActivity fragmentActivity = getActivity();
            if (fragmentActivity == null) {
                throw new NullPointerException("Exception caused by fragment detached from activity.");
            }
            return fragmentActivity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        @TargetApi(Build.VERSION_CODES.M)
        boolean isRevoked(String permission) {
            final FragmentActivity fragmentActivity = getActivity();
            if (fragmentActivity == null) {
                throw new NullPointerException("Exception caused by fragment detached from activity.");
            }
            if (fragmentActivity.getPackageManager() == null) {
                throw new NullPointerException("Exception caused by activity.getPackageManager() == null.");
            }
            if (TextUtils.isEmpty(getActivity().getPackageName())) {
                throw new NullPointerException("Exception caused by activity.getPackageName() == null.");
            }
            return fragmentActivity.getPackageManager().isPermissionRevokedByPolicy(permission, getActivity().getPackageName());
        }

    }

    //============================================================

    /**
     * @Author: duke
     * @DateTime: 2019-03-11 15:10
     * @Description: 权限bean
     */
    public static class PermissionBean {

        public String name;
        public boolean isGranted;
        public boolean isShouldShowRequestPermissionRationale;

        public PermissionBean(String name, boolean isGranted, boolean isShouldShowRequestPermissionRationale) {
            this.name = name;
            this.isGranted = isGranted;
            this.isShouldShowRequestPermissionRationale = isShouldShowRequestPermissionRationale;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof PermissionBean)
                    || getClass() != o.getClass()) {
                return false;
            }

            final PermissionBean that = (PermissionBean) o;

            if (!name.equals(that.name)
                    || isGranted != that.isGranted
                    || isShouldShowRequestPermissionRationale != that.isShouldShowRequestPermissionRationale) {
                return false;
            }
            return true;
        }
    }

    //============================================================

    /**
     * @Author: duke
     * @DateTime: 2019-03-11 15:02
     * @Description: 权限请求回调
     */
    public interface PermissionCallback {

        void onResult(ArrayList<PermissionBean> permissionBeans);

    }
}
