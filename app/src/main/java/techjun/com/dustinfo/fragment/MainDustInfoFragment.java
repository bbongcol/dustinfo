package techjun.com.dustinfo.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import techjun.com.dustinfo.R;
import techjun.com.dustinfo.model.Dust;
import techjun.com.dustinfo.service.DustDBService;
import techjun.com.dustinfo.utils.LocationUtil;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainDustInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainDustInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainDustInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView address, textPM10, textPM25, textPM10Data, textPM25Data, textLastTime;

    private OnFragmentInteractionListener mListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    DustDBService mDustDBService;
    boolean mBound = false;
    final static String TAG = "MainDustInfoFragment";

    public MainDustInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainDustInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainDustInfoFragment newInstance(String param1, String param2) {
        MainDustInfoFragment fragment = new MainDustInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main_dust_info, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new UpdateDustInfoBGTask().execute();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.color_scheme_1_1, R.color.color_scheme_1_2,
                R.color.color_scheme_1_3, R.color.color_scheme_1_4
        );

        ImageView image = (ImageView) v.findViewById(R.id.image);
        image.setImageResource(R.mipmap.bg_sky);

        address = (TextView)v.findViewById(R.id.address);
        textPM10 = (TextView)v.findViewById(R.id.textViewPM10);
        textPM25 = (TextView)v.findViewById(R.id.textViewPM25);

        textPM10Data = (TextView)v.findViewById(R.id.testViewPM10Data);
        textPM25Data = (TextView)v.findViewById(R.id.textViewPM25Data);
        textLastTime = (TextView)v.findViewById(R.id.textLastTime);

        mSwipeRefreshLayout.setRefreshing(true);
        doBindService();

        return v;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            DustDBService.LocalBinder binder = (DustDBService.LocalBinder) service;
            mBound = true;
            mDustDBService = binder.getService();
            mDustDBService.registerCallback(new DustDBService.ICurrentDustCallback() {
                @Override
                public void OnCurrentDust(ArrayList<Dust> curDustArrayList) {
                    Log.d(TAG,"OnCurrentDust");
                    if(curDustArrayList !=null) {
                        textPM10Data.setText("" + curDustArrayList.get(0).getmPM10());
                        textPM25Data.setText("" + curDustArrayList.get(0).getmPM25());
                        textLastTime.setText("" + curDustArrayList.get(0).getmDateTime());
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
            new UpdateDustInfoBGTask().execute();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    void doBindService() {
        Log.d(TAG, "doBindService()");
        getActivity().bindService(new Intent(getActivity(), DustDBService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindService() {
        Log.d(TAG, "doUnbindService()");
        if (mBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        doUnbindService();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public String displayAddress (String[] address) {
        String displayAddress = null;
        if(address[2] != null) {
            displayAddress = address[1] + " " + address[2];
        } else {
            displayAddress = address[0] + " " + address[1];
        }
        return displayAddress;
    }
    
    private class UpdateDustInfoBGTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mDustDBService.requestDustData(LocationUtil.getInstance(getContext()).getCurrentSidoCity());
            return null;
        }
    }
}
