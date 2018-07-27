package com.wyzk.lottery.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wyzk.lottery.R;
import com.wyzk.lottery.constant.IConst;
import com.wyzk.lottery.model.ResultReturn;
import com.wyzk.lottery.model.TokenModel;
import com.wyzk.lottery.model.UserInfoModel;
import com.wyzk.lottery.network.Network;
import com.wyzk.lottery.utils.ACache;
import com.wyzk.lottery.utils.BuildManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 登录页面
 */
public class LoginActivity extends LotteryBaseActivity {

    @Bind(R.id.edt_username)
    EditText edt_username;
    @Bind(R.id.edt_password)
    EditText edtPassword;
    @Bind(R.id.btn_login)
    Button btnLogin;
    @Bind(R.id.tv_register)
    TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        BuildManager.setStatusTransOther(this);
        UserInfoModel cache = getSp(IConst.USER_INFO_KEY);
        if (cache != null) {
            edt_username.setText(cache.getUsername());
            edtPassword.setText(cache.getPassword());
        }
    }

    @OnClick({R.id.btn_login, R.id.tv_register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                executeLogin();
                break;
            case R.id.tv_register:
                toActivity(RegisterActivity.class);
                break;
        }
    }

    private void executeLogin() {
        String username = edt_username.getText().toString().trim();
        String pwd = edtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(LoginActivity.this, getString(R.string.username_pwd_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        showLoadingView();
        login(username, pwd);
    }

    private void login(final String username, final String password) {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        subscription = Network.getNetworkInstance().getUserApi()
                .login(username, password, imei)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResultReturn<TokenModel>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingView();
                        Toast.makeText(LoginActivity.this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ResultReturn<TokenModel> result) {
                        dismissLoadingView();
                        if (result.getCode() == ResultReturn.ResultCode.RESULT_OK.getValue()) {
                            Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();

                            setSp(IConst.USER_INFO_KEY, new UserInfoModel(username, password));

                            TokenModel tokenModel = result.getData();
                            ACache.get(LoginActivity.this).put(IConst.TOKEN, tokenModel.getToken());
                            ACache.get(LoginActivity.this).put(IConst.USER_ID, tokenModel.getUserId());

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
