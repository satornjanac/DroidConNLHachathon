package rs.pstech.hotelmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import io.relayr.RelayrSdk;
import io.relayr.model.Reading;
import io.relayr.model.TransmitterDevice;
import rx.Subscriber;


public class RoomInfoActivity extends AbsWunderbarActivity {

    private TextView mTextViewSound;
    private TextView mTextViewTemp;
    private TextView mLightErrorMsg;
    private ImageView mTempImage;
    private ImageView mLightImage;
    private ProgressBar mNoiseProgressBar;

    private String KEY_LIGHT = "key_light";
    private String KEY_SOUND = "key_sound";
    private String KEY_TEMP = "key_temp";
    private String KEY_HUM = "key_hum";

    private static int DECIBELS_MIN = 0;
    private static int DECIBELS_MAX = 150;

    private float mLightValue;
    private float mSoundValue;
    private float mTemperatureValue;
    private float mHumidityValue;
    private int mUsersSoundLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.app_name);
        } else {
            toolbar.setLogo(R.drawable.logo);
            toolbar.inflateMenu(R.menu.menu_main);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.menu_settings:
                            Intent i = new Intent(RoomInfoActivity.this, PreferencesActivity.class);
                            startActivity(i);
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }
        window.setAllowEnterTransitionOverlap(true);
        init();
        if (savedInstanceState != null) {
            restoreStates(savedInstanceState);
        }
    }



    private void init() {
        mTextViewSound = (TextView) findViewById(R.id.textview_sound);
        mTextViewTemp = (TextView) findViewById(R.id.textview_temp);
        mLightErrorMsg = (TextView) findViewById(R.id.light_error_info);
        mLightImage = (ImageView) findViewById(R.id.light_image);
        mTempImage = (ImageView) findViewById(R.id.temp_image);
        mNoiseProgressBar = (ProgressBar) findViewById(R.id.noise_progress_bar);
    }

    private void restoreStates(Bundle bundle) {
        mLightValue = bundle.getFloat(KEY_LIGHT, 0);
        mSoundValue = bundle.getFloat(KEY_SOUND, 0);
        mTemperatureValue = bundle.getFloat(KEY_TEMP, 0);
        mHumidityValue = bundle.getFloat(KEY_HUM, 0);
        updateTemperatureUI();
        updateLightUI();
        updateSoundUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.getFloat(KEY_LIGHT, mLightValue);
        outState.getFloat(KEY_SOUND, mSoundValue);
        outState.getFloat(KEY_TEMP, mTemperatureValue);
        outState.getFloat(KEY_HUM, mHumidityValue);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreStates(savedInstanceState);
    }

    @Override
    protected void subscribeForTemperatureUpdates(TransmitterDevice device) {
        mWebSocketSubscriptionTemp =
                RelayrSdk.getWebSocketClient().subscribe(device, new Subscriber<Object>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Object o) {
                        Reading reading = new Gson().fromJson(o.toString(), Reading.class);
                        mTemperatureValue = reading.temp;
                        mHumidityValue = reading.hum;
                        updateTemperatureUI();
                    }
                });
    }

    private void updateTemperatureUI() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int LOW_TEMPERATURE = Integer.parseInt(sharedPrefs
                .getString(getResources().getString(R.string.min_temp_key),
                        getResources().getString(R.string.default_min_temp)));
        int HIGH_TEMPERATURE = Integer.parseInt(sharedPrefs
                .getString(getResources().getString(R.string.max_temp_key),
                        getResources().getString(R.string.default_max_temp)));
        if (mTemperatureValue < LOW_TEMPERATURE) {
            mTempImage.setBackground(getResources().getDrawable(R.drawable.low));
            mTextViewTemp.setTextColor(Color.RED);
        } else if (LOW_TEMPERATURE < mTemperatureValue && mTemperatureValue < HIGH_TEMPERATURE) {
            mTempImage.setBackground(getResources().getDrawable(R.drawable.optimal));
            mTextViewTemp.setTextColor(getResources().getColor(R.color.textColorGreen));
        } else {
            mTempImage.setBackground(getResources().getDrawable(R.drawable.high));
            mTextViewTemp.setTextColor(Color.RED);
        }
        mTextViewTemp.setText(mTemperatureValue + "˚C, " + mHumidityValue + "mbar");
        mTempImage.setVisibility(View.VISIBLE);
        mTempImage.invalidate();
    }

    @Override
    protected void subscribeForLightUpdates(TransmitterDevice device) {
        mWebSocketSubscriptionLight =
                RelayrSdk.getWebSocketClient().subscribe(device, new Subscriber<Object>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Object o) {
                        Reading reading = new Gson().fromJson(o.toString(), Reading.class);
                        mLightValue = reading.light;
                        updateLightUI();
                    }
                });
    }

    private void updateLightUI() {
        if (mLightValue != 0) {
            mLightImage.setBackground(getResources().getDrawable(R.drawable.light_on));
            if (mSoundValue < 40.0f) {
                mLightErrorMsg.setTextColor(Color.RED);
                mLightErrorMsg.setText(getResources().getText(R.string.room_is_empty));
            } else {
                mLightErrorMsg.setText("");
            }
        } else {
            mLightImage.setBackground(getResources().getDrawable(R.drawable.light_off));
        }
        mLightImage.invalidate();
        mLightImage.setVisibility(View.VISIBLE);
    }

    @Override
    protected void subscribeForSoundUpdates(TransmitterDevice device) {
        mWebSocketSubscriptionSound =
                RelayrSdk.getWebSocketClient().subscribe(device, new Subscriber<Object>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Object o) {
                        Reading reading = new Gson().fromJson(o.toString(), Reading.class);
                        mSoundValue = reading.snd_level;
                        updateSoundUI();
                    }
                });
    }

    private void updateSoundUI() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mUsersSoundLevel = Integer.parseInt(sharedPrefs
                .getString(getResources().getString(R.string.sound_limit_key),
                        getResources().getString(R.string.sound_limit_default)));
        mTextViewSound.setText(mSoundValue + "dB");
        if (mSoundValue > mUsersSoundLevel) {
            mTextViewSound.setTextColor(Color.RED);
        } else {
            mTextViewSound.setTextColor(getResources().getColor(R.color.textColorGreen));
        }
        updateNoise((int) mSoundValue);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateNoise(final int decibels) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int noiseLevel = caculateNoiseLevel(decibels);
                mNoiseProgressBar.setProgress(noiseLevel);
            }
        });
    }

    private int caculateNoiseLevel(int decibels) {
        int level = 0;
        if (decibels >= DECIBELS_MIN) {
            if (decibels < mUsersSoundLevel) {
                level = (int) (((float) decibels / (float) (DECIBELS_MAX - DECIBELS_MIN)) * 100);
            } else {
                level = 100;
            }
        }
        return level;
    }

}
