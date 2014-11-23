package rs.pstech.hotelmanager;

import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;
import java.util.List;

import io.relayr.LoginEventListener;
import io.relayr.RelayrSdk;
import io.relayr.model.DeviceModel;
import io.relayr.model.Transmitter;
import io.relayr.model.TransmitterDevice;
import io.relayr.model.User;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by nikola.gencic on 11/23/2014.
 */
public class AbsWunderbarActivity extends ActionBarActivity implements LoginEventListener {

    protected Subscription mWebSocketSubscriptionSound;
    protected Subscription mWebSocketSubscriptionTemp;
    protected Subscription mWebSocketSubscriptionLight;
    protected Subscription mUserInfoSubscription;
    protected Subscription mDeviceSubscription;

    @Override
    protected void onResume() {
        super.onResume();
        if (!RelayrSdk.isUserLoggedIn()) {
            RelayrSdk.logIn(this, this);
        } else {
            onLoggedIn();
        }
    }

    @Override
    public void onSuccessUserLogIn() {
        onLoggedIn();
    }

    @Override
    public void onErrorLogin(Throwable throwable) {

    }

    private void onLoggedIn() {
        mUserInfoSubscription =
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

    private void loadDevices(User user) {
        mDeviceSubscription =
                RelayrSdk.getRelayrApi().getTransmitters(user.id).flatMap(
                        new Func1<List<Transmitter>, Observable<List<TransmitterDevice>>>() {
                            @Override
                            public Observable<List<TransmitterDevice>> call(
                                    List<Transmitter> transmitters) {
                                List<TransmitterDevice> devices = new ArrayList<TransmitterDevice>();
                                for (Transmitter transmitter : transmitters){
                                    devices.add(new TransmitterDevice(transmitter.id, transmitter.secret,
                                            transmitter.owner, transmitter.getName(), transmitter.id));
                                }
                                onDevicesDetcted(devices);
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

    protected void onDevicesDetcted(List<TransmitterDevice> devices){
    }

    protected void subscribeForTemperatureUpdates(TransmitterDevice device){

    }

    protected void subscribeForLightUpdates(TransmitterDevice device){

    }

    protected void subscribeForSoundUpdates(TransmitterDevice device){

    }

    @Override
    protected void onPause() {
        super.onPause();
        unSubscribeToUpdates();
    }

    private void unSubscribeToUpdates() {
        if (mUserInfoSubscription != null && !mUserInfoSubscription.isUnsubscribed()) {
            mUserInfoSubscription.unsubscribe();
        }
        if (mDeviceSubscription != null && !mDeviceSubscription.isUnsubscribed()) {
            mDeviceSubscription.unsubscribe();
        }
        if (mWebSocketSubscriptionLight != null && !mWebSocketSubscriptionLight.isUnsubscribed()) {
            mWebSocketSubscriptionLight.unsubscribe();
        }
        if (mWebSocketSubscriptionSound != null && !mWebSocketSubscriptionSound.isUnsubscribed()) {
            mWebSocketSubscriptionSound.unsubscribe();
        }
        if (mWebSocketSubscriptionTemp != null && !mWebSocketSubscriptionTemp.isUnsubscribed()) {
            mWebSocketSubscriptionTemp.unsubscribe();
        }
    }
}
