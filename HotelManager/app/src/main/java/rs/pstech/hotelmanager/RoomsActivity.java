package rs.pstech.hotelmanager;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import io.relayr.model.TransmitterDevice;

/**
 * Created by nikola.gencic on 11/23/2014.
 */
public class RoomsActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);
    }

    protected void onDevicesDetcted(List<TransmitterDevice> devices){
        setListAdapter(new RoomsAdapter(this, devices));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
