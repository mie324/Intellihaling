package com.project.ece1778_project_intellihaling;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EmergencyActivity extends AppCompatActivity {

    private static final String TAG = "EmergencyActivity";
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    //UI
    private TextView CurrentTimeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        CurrentTimeView = findViewById(R.id.em_time);

        setUp();
        if (ContextCompat.checkSelfPermission(EmergencyActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // 没有获得授权，申请授权
            if (ActivityCompat.shouldShowRequestPermissionRationale(EmergencyActivity.this, Manifest.permission.CALL_PHONE)) {
                // 返回值：
                //如果app之前请求过该权限,被用户拒绝, 这个方法就会返回true.
                //如果用户之前拒绝权限的时候勾选了对话框中”Don’t ask again”的选项,那么这个方法会返回false.
                //如果设备策略禁止应用拥有这条权限, 这个方法也返回false.
                // 弹窗需要解释为何需要该权限，再次请求授权
                Toast.makeText(EmergencyActivity.this, "请授权！", Toast.LENGTH_LONG).show();
                // 帮跳转到该应用的设置界面，让用户手动授权
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                // 不需要解释为何需要该权限，直接请求授权
                ActivityCompat.requestPermissions(EmergencyActivity.this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
            }
        } else {
            // 已经获得授权，可以打电话
            CallPhone();
        }
    }

    private void CallPhone() {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_CALL);
            //url:统一资源定位符
            //uri:统一资源标示符（更广）
            intent.setData(Uri.parse("tel:" + "911"));
            //开启系统拨号器
            startActivity(intent);

    }

    private void setUp(){

        String yyyy, MM, dd, HH, mm;
        String tsTmp = OnceAttackRecordStatic.getAttackTimestamp();
        if(tsTmp != null){
            yyyy = OnceAttackRecordStatic.getAttackTimestampYear();
            MM = OnceAttackRecordStatic.getAttackTimestampMonth();
            dd = OnceAttackRecordStatic.getAttackTimestampDay();
            HH = OnceAttackRecordStatic.getAttackTimestampHour();
            mm = OnceAttackRecordStatic.getAttackTimestampMinute();
        }else{
            Date ct = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy");
            yyyy = df.format(ct);

            df = new SimpleDateFormat("MM");
            MM = df.format(ct);

            df = new SimpleDateFormat("dd");
            dd = df.format(ct);

            df = new SimpleDateFormat("HH");
            HH = df.format(ct);

            df = new SimpleDateFormat("mm");
            mm = df.format(ct);
        }

        String currentTime = "Start: " + yyyy + "/" + MM + "/" + dd + " " + HH + ":" + mm;
        CurrentTimeView.setText(currentTime);
    }

}
