package com.example.kkobak.ui.challenge;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kkobak.R;
import com.example.kkobak.data.retrofit.api.BpmDataApi;
import com.example.kkobak.data.retrofit.model.BpmDataReq;
import com.example.kkobak.data.room.dao.AccessTokenDao;
import com.example.kkobak.data.room.database.AccessTokenDatabase;
import com.example.kkobak.data.room.entity.AccessToken;
import com.example.kkobak.ui.test.TestActivity;
import com.txusballesteros.SnakeView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeditationActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager sensorManager;
    SensorEventListener sensorEventListener;
    Sensor sensor;

    AccessTokenDatabase db;
    String accessToken;

    Button heartRateBtn;
    TextView heartRateTv;
    TextView inputMinute;
    TextView inputSecond;
    LinearLayout inputLayout;
    TextView timerMinute;
    TextView timerSecond;
    LinearLayout timerLayout;

    SnakeView snakeView;

    final int TIMER = 1;

    Timer timer;
    LocalDateTime startTime;

    boolean btnState;
    int _minute, _second;

    String chlId;
//    String chlId = "133";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation);

        checkPermission();

        db = AccessTokenDatabase.getAppDatabase(this);
        try {
            accessToken = new TestActivity.getAccessTokenAsyncTask(db.accessTokenDao()).execute().get().getAccessToken();
        } catch (Exception e) {
            Toast.makeText(this, "에러 발생", Toast.LENGTH_SHORT).show();
        }

        heartRateTv = findViewById(R.id.hrmText);
        heartRateBtn = findViewById(R.id.heartRateBtn);
        btnState = false;

        inputMinute = findViewById(R.id.MedinputMinute);
        inputSecond = findViewById(R.id.MedinputSecond);
        inputLayout = findViewById(R.id.MedInputTime);

        timerMinute = findViewById(R.id.medTimerMinute);
        timerSecond = findViewById(R.id.medTimerSecond);
        timerLayout = findViewById(R.id.medTimer);

        sensorEventListener = this;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI);

        snakeView = (SnakeView)findViewById(R.id.snake);
        snakeView.setMinValue(-20500);
        snakeView.setMaxValue(20000);

        if (getIntent() != null)
            chlId = getIntent().getStringExtra("chlId");

//        Toast.makeText(this, "id: " + chlId, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sensorEventListener);
    }

    public void sendBpmData(int bpm) {
        BpmDataReq data = new BpmDataReq();

        data.setChlId(Long.parseLong(chlId));
        data.setChk(startTime.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data.setTime(LocalDateTime.now().withNano(0).toString());
        }
        data.setBpm(bpm);

        Call<Boolean> call = BpmDataApi.getBpmService().sendBpmData(accessToken, data);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(!response.isSuccessful()){
                    Toast.makeText(MeditationActivity.this, "에러: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    Toast.makeText(MeditationActivity.this, "성공: " + response.body().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(MeditationActivity.this, "그냥 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (btnState) {
            float value = sensorEvent.values[0];
            int printValue;

            value *= 100;
            snakeView.addValue(value);
            snakeView.addValue(value - 1000);
            snakeView.addValue(value + 1500);
            snakeView.addValue(value * -1);

            printValue = (int) (value / 100);
            heartRateTv.setText("" + printValue);

            sendBpmData(printValue);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
//        Toast.makeText(this, "onAccuracyChanged", Toast.LENGTH_SHORT).show();
    }

    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 1);
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case TIMER:
                    updateTimer();
                    break;
                default:
                    System.out.println("Run Default Handler");
                    break;
            }
        }
    };

    private void updateTimer() {
        if (_second != 0) {
            --_second;
        }
        else if (_minute != 0) {
            _second = 60;
            --_minute;
            --_second;
        }

        if (_second <= 9) timerSecond.setText("0" + _second);
        else        timerSecond.setText(Integer.toString(_second));
        if (_minute <= 9) timerMinute.setText("0" + _minute);
        else        timerMinute.setText(Integer.toString(_minute));

        if (_minute == 0 && _second == 0) {
            timer.cancel();

        }
    }

    public void pressBtn(View v) {
        btnState = !btnState;

        if (btnState) {
            heartRateTv.setVisibility(View.VISIBLE);

            if (inputMinute.getText().toString().equals(""))    inputMinute.setText("1");
            if (inputSecond.getText().toString().equals(""))    inputSecond.setText("00");

            _minute = Integer.parseInt(inputMinute.getText().toString());
            _second = Integer.parseInt(inputSecond.getText().toString());
            inputLayout.setVisibility(View.INVISIBLE);

            if (_minute <= 9)   timerMinute.setText("0" + _minute);
            else                    timerMinute.setText("" + _minute);
            if (_second <= 9) timerSecond.setText("0" + _second);
            else                    timerSecond.setText("" + _second);
            timerLayout.setVisibility(View.VISIBLE);

            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(TIMER);
                }
            };
            timer.schedule(timerTask, 0, 1000);

            heartRateBtn.setText("정지");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startTime = LocalDateTime.now().withNano(0);
            }
        }
        else {
            heartRateBtn.setText("시작");
            Toast.makeText(this, "정지 실행", Toast.LENGTH_SHORT).show();
        }

    }

    public static class getAccessTokenAsyncTask extends AsyncTask<Void, Void, AccessToken> {
        private final AccessTokenDao accessTokenDao;

        public getAccessTokenAsyncTask(AccessTokenDao accessTokenDao) {
            this.accessTokenDao = accessTokenDao;
        }

        @Override
        protected AccessToken doInBackground(Void... voids) {
            List<AccessToken> tokens = accessTokenDao.getAll();
            if (tokens == null || tokens.size() == 0)
                return (null);
            else
                return (tokens.get(0));
        }
    }
}
