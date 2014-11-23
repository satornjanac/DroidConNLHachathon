package rs.pstech.hotelmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.relayr.model.TransmitterDevice;

/**
 * Created by nikola.gencic on 11/23/2014.
 */
public class RoomsAdapter extends BaseAdapter {

    static final Map<String, String> roomsMap = new HashMap<String, String>();

    static {
        roomsMap.put("d1f6553d-57b3-4490-82c1-0692e0e02950", "Lobby");
    }

    private List<TransmitterDevice> mDevices;
    private Context mContext;
    private LayoutInflater mInflater;

    public RoomsAdapter(Context context, List<TransmitterDevice> devices){
        super();
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDevices = devices;
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.list_row_room, null);
        TextView textViewRoom = (TextView)view.findViewById(R.id.textview_room);
        TransmitterDevice device = mDevices.get(position);
        textViewRoom.setText(roomsMap.get(device.id));
        return view;
    }
}
