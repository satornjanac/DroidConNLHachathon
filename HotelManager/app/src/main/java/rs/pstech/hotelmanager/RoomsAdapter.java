package rs.pstech.hotelmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.relayr.model.TransmitterDevice;

/**
 * Created by nikola.gencic on 11/23/2014.
 */
public class RoomsAdapter extends ArrayAdapter<TransmitterDevice> {

    static final Map<String, String> roomsMap = new HashMap<String, String>();
    static final Map<String, Integer> imagesMap = new HashMap<String, Integer>();

    static {
        roomsMap.put("d1f6553d-57b3-4490-82c1-0692e0e02950", "Lobby");
    }

    static {
        imagesMap.put("d1f6553d-57b3-4490-82c1-0692e0e02950", R.drawable.lobby);
    }

    private Context mContext;
    private LayoutInflater mInflater;

    public RoomsAdapter(Context context, List<TransmitterDevice> devices){
        super(context, R.layout.list_row_room, devices);
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.list_row_room, null);
        TextView textViewRoom = (TextView)view.findViewById(R.id.textview_room);
        TransmitterDevice device = getItem(position);
        textViewRoom.setText(roomsMap.get(device.id));
        ((ImageView)view.findViewById(R.id.imageview_room)).setImageResource(imagesMap.get(device.id));
        return view;
    }
}
