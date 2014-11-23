package rs.pstech.hotelmanager;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
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

    private TextView mTextViewMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Hotel Manager");
        } else {
            toolbar.setTitle("Hotel Manager");
        }
        mTextViewMain = (TextView)findViewById(R.id.textview_main);

        // fensi tranzicija
/*        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, transitionView, DetailActivity.EXTRA_IMAGE);
        ActivityCompat.startActivity(activity, new Intent(activity, DetailActivity.class),
                options.toBundle());*/
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
        Subscription userInfoSubscription = RelayrSdk.getRelayrApi()
                .getUserInfo()
                .subscribeOn(Schedulers.io())
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
                        mTextViewMain.setText("User: " + user.getName());
                        loadTemperatureDevice(user);
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

    private void loadTemperatureDevice(User user) {
        Subscription temperatureDeviceSubscription = RelayrSdk.getRelayrApi()
                .getTransmitters(user.id)
                .flatMap(new Func1<List<Transmitter>, Observable<List<TransmitterDevice>>>() {
                    @Override
                    public Observable<List<TransmitterDevice>> call(List<Transmitter> transmitters) {
                        // This is a naive implementation. Users may own many WunderBars or other
                        // kinds of transmitter.
                        if (transmitters.isEmpty())
                            return Observable.from(new ArrayList<List<TransmitterDevice>>());
                        return RelayrSdk.getRelayrApi().getTransmitterDevices(transmitters.get(0).id);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                            if (device.model.equals(DeviceModel.TEMPERATURE_HUMIDITY.getId())) {
                                subscribeForTemperatureUpdates(device);
                                return;
                            }
                        }
                    }
                });

    }

    private void subscribeForTemperatureUpdates(TransmitterDevice device) {
        Subscription webSocketSubscription = RelayrSdk.getWebSocketClient()
                .subscribe(device, new Subscriber<Object>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Object o) {
                        Reading reading = new Gson().fromJson(o.toString(), Reading.class);
                        mTextViewMain.setText("Sound level : " + reading.snd_level + " temp: " + reading.temp + "˚C" + " color + " + reading.clr);
                    }
                });
    }
}
