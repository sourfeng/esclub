package com.ww.android.esclub.activity.start;

import android.Manifest;
import android.content.DialogInterface;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eftimoff.androipathview.PathView;
import com.trello.rxlifecycle.ActivityEvent;
import com.ww.android.esclub.BaseApplication;
import com.ww.android.esclub.R;
import com.ww.android.esclub.activity.MainActivity;
import com.ww.android.esclub.activity.base.BaseActivity;
import com.ww.android.esclub.activity.base.rx.HttpSubscriber;
import com.ww.android.esclub.api.ApiException;
import com.ww.android.esclub.bean.ResponseBean;
import com.ww.android.esclub.bean.start.SystemConfigBean;
import com.ww.android.esclub.bean.start.SystemFlagBean;
import com.ww.android.esclub.bean.start.UserBean;
import com.ww.android.esclub.utils.AppUtils;
import com.ww.android.esclub.utils.DialogUtil;
import com.ww.android.esclub.vm.models.start.StartModel;
import com.ww.mvp.view.VoidView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

/**
 * Created by feng on 2017/6/11.
 * 开始页面，用于登录和注册
 */

public class StartActivity extends BaseActivity<VoidView,StartModel> {

    @BindView(R.id.pathView)
    PathView pathView;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_start;
    }

    @Override
    protected void init() {
        pathView.getPathAnimator().
                delay(100).
                duration(2500).
                interpolator(new AccelerateDecelerateInterpolator()).
                start();

        pathView.postDelayed(new Runnable() {
            @Override
         public void run() {

                onSysParams();
            }
        },2500);

    }


    private void startActivity(){
        UserBean user = BaseApplication.getInstance().getUserBean();
        if (user!=null){
            MainActivity.start(StartActivity.this);
        }else {
            LoginActivity.start(StartActivity.this);

        }
        finish();
    }

    private void onSysParams(){
        if (AppUtils.isNetworkConnected(this)) {
            SystemFlagBean flagBean = null;
            m.onSysParams(flagBean, bindUntilEvent(ActivityEvent.DESTROY), new HttpSubscriber<SystemConfigBean>(this, true) {

                @Override
                public void onNext(SystemConfigBean systemConfigBean) {
                    onStartPermissionn();
                }

                @Override
                public void onFail(Throwable e) {
                    if (e instanceof ApiException) {
                        String errorCode = ((ApiException) e).getCode();
                        if (!TextUtils.isEmpty(errorCode) && TextUtils.equals("Error0003",
                                errorCode)) {
                            String updateUri = null;
                            ResponseBean responseBean = ((ApiException) e).getResponseBean();
                            String data = responseBean.getData();
                            JSONObject json = JSON.parseObject(data);
                            if (json != null && json.containsKey("obj")) {
                                updateUri = json.getJSONObject("obj").getString("update_uri");
                            }
                            showUpLoadAppDialogErr(e.getMessage(), updateUri);
                        } else {
                            showDialogErr(e.getMessage());
                        }
                    }
                }
            });
        }else {
            showDialogErr("无可用网络");
        }
    }


    private void onStartPermissionn(){
        final List<PermissionItem> permissionItems = new ArrayList<>();
        permissionItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE,
                "Phone",
                R.drawable.permission_ic_phone));
        permissionItems.add(new PermissionItem(Manifest.permission.CAMERA,
                "Camera",
                R.drawable.permission_ic_camera));
        permissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        "Storage",
                        R.drawable.permission_ic_storage)
                );
        HiPermission.create(this).
                permissions(permissionItems)
                .style(R.style.PermissionBlueStyle)
                .filterColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                .checkMutiPermission(new PermissionCallback() {
            @Override
            public void onClose() {
                showToast("权限请求取消");
            }

            @Override
            public void onFinish() {

                startActivity();
            }

            @Override
            public void onDeny(String permission, int position) {
            }

            @Override
            public void onGuarantee(String permission, int position) {

            }
        });

    }

//
private void showDialogErr(String str) {
    try {
        DialogUtil.showDialog(this, getString(R.string.dialog_title_tip)
                , str, getString(R.string.dialog_btn_repay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSysParams();
                    }
                }, getString(R.string.dialog_btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, false);
    } catch (Exception e) {
        e.printStackTrace();
        showToast("应用发生错误，请重新启动试试..");
        finish();
    }
}


    private void showUpLoadAppDialogErr(String str, final String updateUrl) {
        DialogUtil.showDialog(this, getString(R.string.dialog_title_tip)
                , str, getString(R.string.dialog_btn_ver_update_sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AppUtils.startBrowser(StartActivity.this, updateUrl);
                        finish();
                    }
                }, getString(R.string.dialog_btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, false);
    }

}
