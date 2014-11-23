package rs.pstech.hotelmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import io.relayr.model.TransmitterDevice;

/**
 * Created by nikola.gencic on 11/23/2014.
 */
public class RoomsActivity extends AbsWunderbarActivity {

    ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);
        mListView = (ListView)findViewById(R.id.listview_rooms);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(RoomsActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onDevicesDetcted(final List<TransmitterDevice> devices){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RoomsAdapter adapter = new RoomsAdapter(RoomsActivity.this, devices);
                mListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
