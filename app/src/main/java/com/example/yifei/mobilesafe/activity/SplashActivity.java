package com.example.yifei.mobilesafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yifei.mobilesafe.R;
import com.example.yifei.mobilesafe.utils.StreamUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends Activity {

    private static final int CODE_UPDATE_DIALOG = 0;
    private static final int CODE_URL_ERROR = 1;
    private static final int CODE_NET_ERROR = 2;
    private static final int CODE_JSON_ERROR = 3;

    private TextView tvVersion;

    /*
    * 服务器传回的数据
    * */
    private String mVersionName;//获取到的最新版本名
    private int mVersionCode;//获取到的最新版本号
    private String mDescription;//获取到的最新版本描述
    private String mDownloadUrl;//获取到的最新下载链接

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CODE_UPDATE_DIALOG:
                    showUpdateDialog();
                    break;
                case CODE_URL_ERROR:
                    Toast.makeText(SplashActivity.this,"URL异常", Toast.LENGTH_SHORT).show();
                    break;
                case CODE_NET_ERROR:
                    Toast.makeText(SplashActivity.this,"网络异常", Toast.LENGTH_SHORT).show();
                    break;
                case CODE_JSON_ERROR:
                    Toast.makeText(SplashActivity.this,"更新数据异常", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvVersion = (TextView) findViewById(R.id.tv_version);
        String versionName = getVersionName();
        tvVersion.setText("版本号："+ versionName);
        checkVersion();
    }

    /*
    * 获取本地APP版本名
    * */
    private String getVersionName(){
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            String versionName = packageInfo.versionName;
            //Log.d("SplashActivity.class", "VersionName: "+versionName + ", code: "+ versionCode);
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }


    /*
    * 获取本地APP版本号
    * */
    private int getVersionCode(){
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            //Log.d("SplashActivity.class", "code: "+ versionCode);
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return -1;
    }


    /*
    * 从服务器获取json文件，并解析，获取更新信息
    * */
    private void checkVersion(){
        //启动子线程建立网络连接
        new Thread(){
            @Override
            public void run() {
                Message msg = Message.obtain();
                HttpURLConnection conn = null;
                try {
                    URL url = new URL("http://10.0.2.2:8080/update.json");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.connect();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200){
                        InputStream inputStream = conn.getInputStream();
                        String result = StreamUtils.readFromStream(inputStream);

                        //Log.d("SplashActivity.class", result);

                        JSONObject jo = new JSONObject(result);
                        mVersionName = jo.getString("versionName");
                        mVersionCode = jo.getInt("versionCode");
                        mDescription = jo.getString("description");
                        mDownloadUrl = jo.getString("downloadUrl");

                        //Log.d("SplashActivity.class", "版本名："+ mVersionName + " 版本号： " +
                        //mVersionCode + " 版本描述： " + mDescription + " 下载链接： " + mDownloadUrl);

                        if (mVersionCode > getVersionCode()){//判断是否有更新
                            //如果有更新,弹出对话框
                            msg.what = CODE_UPDATE_DIALOG;
                        }
                    }
                } catch (MalformedURLException e) {
                    //url异常
                    msg.what = CODE_URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    //网络错误异常
                    msg.what = CODE_NET_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    //JSON错误异常
                    msg.what = CODE_JSON_ERROR;
                    e.printStackTrace();
                }finally {
                    mHandler.sendMessage(msg);//发送消息
                    if (conn != null) {
                        conn.disconnect();//关闭网络连接
                    }
                }
            }
        }.start();

    }

    private void showUpdateDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最新版本:" + mVersionName);
        builder.setMessage(mDescription);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("SplashActivity", "立即更新");
            }
        });
        builder.setNegativeButton("下次更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("SplashActivity", "下次更新");
            }
        });
        builder.show();
    }
}
