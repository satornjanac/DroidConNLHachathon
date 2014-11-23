package rs.pstech.hotelmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
        mListView.setEmptyView(findViewById(R.id.progress_bar));
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.app_name);
        } else {
            // toolbar.setTitle(R.string.app_name);
            //toolbar.setTitleTextColor(getResources().getColor(R.color.textColorToolbar));
            toolbar.setLogo(R.drawable.logo);
            //toolbar.setNavigationIcon(R.drawable.logo);
            toolbar.inflateMenu(R.menu.menu_main);
            toolbar.setOnMenuItemClickListener(new android.support.v7.widget.Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.menu_settings:
                            Intent i = new Intent(RoomsActivity.this, PreferencesActivity.class);
                            startActivity(i);
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }
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
