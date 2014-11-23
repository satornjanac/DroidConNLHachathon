package rs.pstech.hotelmanager;

import android.app.Application;

import io.relayr.RelayrSdk;

/**
 * Created by nikola.gencic on 11/23/2014.
 */
public class HotelManagerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RelayrSdk.init(this);
    }

}
