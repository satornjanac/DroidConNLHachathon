package rs.pstech.hotelmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.relayr.LoginEventListener;
import io.relayr.RelayrSdk;
import io.relayr.model.DeviceModel;
import io.relayr.model.Reading;
import io.relayr.model.Transmitter;
import io.relayr.model.TransmitterDevice;
import io.relayr.model.User;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity implements LoginEventListener {

    private TextView mTextViewSound;
    private TextView mTextViewTemp;
    private ImageView mTempImage;
    private ImageView mLightImage;

    private String KEY_LIGHT = "key_light";
    private String KEY_SOUND = "key_sound";
    private String KEY_TEMP = "key_temp";
    private String KEY_HUM = "key_hum";

    private float light_value;
    private float sound_value;
    private float temperature_value;
    private float hum_value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.app_name);
        } else {
            toolbar.setTitle(R.string.app_name);
            toolbar.inflateMenu(R.menu.menu_main);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.menu_settings:
                            Intent i = new Intent(MainActivity.this, PreferencesActivity.class);
                            startActivity(i);
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }
        init();
        if (savedInstanceState != null) {
            restoreStates(savedInstanceState);
        }
    }

    private void init(){
        mTextViewSound = (TextView) findViewById(R.id.textview_sound);
        mTextViewTemp = (TextView) findViewById(R.id.textview_temp);
        mLightImage = (ImageView) findViewById(R.id.light_image);
        mTempImage = (ImageView) findViewById(R.id.temp_image);
    }

    private void restoreStates(Bundle bundle){
        light_value = bundle.getFloat(KEY_LIGHT, 0);
        sound_value = bundle.getFloat(KEY_SOUND, 0);
        temperature_value = bundle.getFloat(KEY_TEMP, 0);
        hum_value = bundle.getFloat(KEY_HUM, 0);
        updateTemperatureUI();
        updateLightUI();
        updateSoundUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.getFloat(KEY_LIGHT, light_value);
        outState.getFloat(KEY_SOUND, sound_value);
        outState.getFloat(KEY_TEMP, temperature_value);
        outState.getFloat(KEY_HUM, hum_value);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreStates(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!RelayrSdk.isUserLoggedIn()) {
            RelayrSdk.logIn(this, this);
        } else {
            onLoggedIn();
        }
    }

    private void onLoggedIn() {
        Subscription userInfoSubscription =
                RelayrSdk.getRelayrApi().getUserInfo().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<User>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(User user) {
                                loadDevices(user);
                            }
                        });
    }

    @Override
    public void onSuccessUserLogIn() {
        onLoggedIn();
    }

    @Override
    public void onErrorLogin(Throwable throwable) {

    }

    private void loadDevices(User user) {
        Subscription temperatureDeviceSubscription =
                RelayrSdk.getRelayrApi().getTransmitters(user.id).flatMap(
                        new Func1<List<Transmitter>, Observable<List<TransmitterDevice>>>() {
                            @Override
                            public Observable<List<TransmitterDevice>> call(
                                    List<Transmitter> transmitters) {
                                // This is a naive implementation. Users may own many WunderBars or other
                                // kinds of transmitter.
                                if (transmitters.isEmpty()) {
                                    return Observable
                                            .from(new ArrayList<List<TransmitterDevice>>());
                                }
                                return RelayrSdk.getRelayrApi()
                                        .getTransmitterDevices(transmitters.get(0).id);
                            }
                        }
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<List<TransmitterDevice>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(List<TransmitterDevice> devices) {
                                for (TransmitterDevice device : devices) {
                                    if (device.model
                                            .equals(DeviceModel.TEMPERATURE_HUMIDITY.getId())) {
                                        subscribeForTemperatureUpdates(device);
                                    }
                                    if (device.model.equals(DeviceModel.LIGHT_PROX_COLOR.getId())) {
                                        subscribeForLightUpdates(device);
                                    }
                                    if (device.model.equals(DeviceModel.MICROPHONE.getId())) {
                                        subscribeForSoundUpdates(device);
                                    }
                                }
                            }
                        });

    }

    private void subscribeForTemperatureUpdates(TransmitterDevice device) {
        Subscription webSocketSubscription =
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
                        temperature_value = reading.temp;
                        hum_value = reading.hum;
                        updateTemperatureUI();
                    }
                });
    }

    private void updateTemperatureUI() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        int LOW_TEMPERATURE = Integer.parseInt(sharedPrefs.getString(getResources().getString(R.string.min_temp_key), getResources().getString(R.string.default_min_temp)));
        int HIGH_TEMPERATURE = Integer.parseInt(sharedPrefs.getString(getResources().getString(R.string.max_temp_key), getResources().getString(R.string.default_max_temp)));
        if (temperature_value < LOW_TEMPERATURE) {
            mTempImage.setBackground(getResources().getDrawable(R.drawable.low));
            mTextViewTemp.setTextColor(Color.RED);
        } else if (LOW_TEMPERATURE < temperature_value &&
                temperature_value < HIGH_TEMPERATURE) {
            mTempImage.setBackground(getResources().getDrawable(R.drawable.optimal));
            mTextViewTemp.setTextColor(Color.GREEN);
        } else {
            mTempImage.setBackground(getResources().getDrawable(R.drawable.high));
            mTextViewTemp.setTextColor(Color.RED);
        }
        mTextViewTemp.setText("Temp: " + temperature_value + "˚C, Hum. " + hum_value);
        mTempImage.setVisibility(View.VISIBLE);
        mTempImage.invalidate();
    }

    private void subscribeForLightUpdates(TransmitterDevice device) {
        Subscription webSocketSubscription =
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
                        light_value = reading.light;
                        updateLightUI();
                    }
                });
    }

    private void updateLightUI() {
        if (light_value != 0) {
            mLightImage
                    .setBackground(getResources().getDrawable(R.drawable.light_on));
        } else {
            mLightImage.setBackground(
                    getResources().getDrawable(R.drawable.light_off));
        }
        mLightImage.invalidate();
        mLightImage.setVisibility(View.VISIBLE);
    }

    private void subscribeForSoundUpdates(TransmitterDevice device) {
        Subscription webSocketSubscription =
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
                        sound_value = reading.snd_level;
                        updateSoundUI();
                    }
                });
    }

    private void updateSoundUI() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        int soundLevel = Integer.parseInt(sharedPrefs.getString(getResources().getString(R.string.sound_limit_key), getResources().getString(R.string.sound_limit_default)));
        mTextViewSound.setText("Sound: " + sound_value + "dB");
        if (sound_value > soundLevel){
            mTextViewSound.setTextColor(Color.RED);
        } else {
            mTextViewSound.setTextColor(Color.GREEN);
        }
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
}
