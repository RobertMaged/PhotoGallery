package z.com.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";


    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getActivity(), "got a broadcast" + intent.getAction(), Toast.LENGTH_LONG).show();
            Log.i(TAG, "mOnShowNotificationOnReceive: " + intent.getAction());
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        //only our permission can trigger this register
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PREM_PRIVATE, null);
        Log.i(TAG, "onStartVisibleFragment: " + filter.getAction(0));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }
}
