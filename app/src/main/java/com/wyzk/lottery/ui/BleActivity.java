package com.wyzk.lottery.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.csr.csrmesh2.MeshConstants;
import com.het.csrmesh.App;
import com.het.csrmesh.api.MeshLibraryManager;
import com.het.csrmesh.api.TimeModel;
import com.het.csrmesh.events.MeshResponseEvent;
import com.het.csrmesh.events.MeshSystemEvent;
import com.het.csrmesh.model.listeners.LogLevel;
import com.squareup.otto.Subscribe;
import com.wyzk.lottery.R;
import com.wyzk.lottery.utils.BuildManager;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.het.csrmesh.events.MeshResponseEvent.ResponseEvent.DATA_RECEIVE_BLOCK;
import static com.het.csrmesh.events.MeshResponseEvent.ResponseEvent.DATA_RECEIVE_STREAM;
import static com.het.csrmesh.events.MeshResponseEvent.ResponseEvent.DATA_RECEIVE_STREAM_END;
import static com.het.csrmesh.events.MeshResponseEvent.ResponseEvent.DATA_SENT;


public class BleActivity extends LotteryBaseActivity {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.title)
    View title;
    @Bind(R.id.tv_version)
    TextView tvVersion;
    long[] mHints = new long[5];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.mipmap.arrow_back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        BuildManager.setStatusTrans(this, 1, title);
        tvVersion.setText(getVersion());

        App.bus.register(this);

        MeshLibraryManager.initInstance(this, MeshLibraryManager.MeshChannel.BLUETOOTH, LogLevel.DEBUG);
    }

    @Subscribe
    public void onEventMainThread(MeshResponseEvent event) {
        if (event == null || event.data == null) {
            return;
        }
        if (event.what == DATA_RECEIVE_BLOCK || event.what == DATA_RECEIVE_STREAM || event.what == DATA_RECEIVE_STREAM_END || event.what == DATA_SENT) {
            final int deviceId = event.data.getInt(MeshConstants.EXTRA_DEVICE_ID);
            switch (event.what) {
                case DATA_RECEIVE_BLOCK: {
                    final byte[] reData = event.data.getByteArray(MeshConstants.EXTRA_DATA);
                    if (reData != null) {
                    }
                    break;
                }
                case DATA_RECEIVE_STREAM: {
                    byte[] reData = event.data.getByteArray(MeshConstants.EXTRA_DATA);
                    int dataSqn = event.data.getInt(MeshConstants.EXTRA_DATA_SQN);
                    if (reData != null) {
                    }
                    break;
                }
                case DATA_RECEIVE_STREAM_END: {
                    break;
                }
            }
        }
    }

    @Subscribe
    public void onEvent(MeshSystemEvent event) {
        switch (event.what) {
            case CHANNEL_READY:
                MeshLibraryManager.MeshChannel channel = MeshLibraryManager.getInstance().getChannel();
                if (MeshLibraryManager.getInstance().isServiceAvailable() && channel == MeshLibraryManager.MeshChannel.BLUETOOTH) {
                    TimeModel.broadcastTime();
                }
                MeshLibraryManager.getInstance().setNetworkPassPhrase("123");
                break;
            case CHANNEL_NOT_READY:


                break;
            case SERVICE_SHUTDOWN:

                break;
        }
    }

    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return this.getString(R.string.version) + version;
        } catch (Exception e) {
            e.printStackTrace();
            return this.getString(R.string.version) + "1.0.0";
        }
    }

    public void onFiveClick(View view) {
        System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);//把从第二位至最后一位之间的数字复制到第一位至倒数第一位
        mHints[mHints.length - 1] = System.currentTimeMillis();//SystemClock.uptimeMillis();//从开机到现在的时间毫秒数
        if (System.currentTimeMillis() - mHints[0] <= 1000) {//连续点击之间间隔小于一秒，有效
            Toast.makeText(this, "调试模式开启", Toast.LENGTH_SHORT).show();
            mHints = new long[5];
            //做你想做的事
            startActivity(new Intent(BleActivity.this, TestActivity.class));
        }
    }


}