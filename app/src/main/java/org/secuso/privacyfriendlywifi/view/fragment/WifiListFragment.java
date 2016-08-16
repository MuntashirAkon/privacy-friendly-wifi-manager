package org.secuso.privacyfriendlywifi.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.secuso.privacyfriendlywifi.logic.types.WifiLocationEntry;
import org.secuso.privacyfriendlywifi.logic.util.IOnDialogClosedListener;
import org.secuso.privacyfriendlywifi.logic.util.StaticContext;
import org.secuso.privacyfriendlywifi.logic.util.WifiListHandler;
import org.secuso.privacyfriendlywifi.service.ManagerService;
import org.secuso.privacyfriendlywifi.service.receivers.AlarmReceiver;
import org.secuso.privacyfriendlywifi.view.adapter.WifiListAdapter;
import org.secuso.privacyfriendlywifi.view.decoration.DividerItemDecoration;
import org.secuso.privacyfriendlywifi.view.dialog.WifiPickerDialog;

import java.util.Observable;
import java.util.Observer;

import secuso.org.privacyfriendlywifi.R;

public class WifiListFragment extends Fragment implements IOnDialogClosedListener, Observer {
    private static final String TAG = WifiListFragment.class.getSimpleName();
    private IOnDialogClosedListener thisClass;
    private WifiListHandler wifiListHandler;

    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    protected FragmentActivity mActivity;

    public WifiListFragment() {
        // Required empty public constructor
        this.thisClass = this;
    }

    public static WifiListFragment newInstance() {
        WifiListFragment fragment = new WifiListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_wifilist, container, false);

        // Set substring in actionbar
        ActionBar actionBar = ((AppCompatActivity) this.mActivity).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setSubtitle(R.string.fragment_wifilist);
        }

        // setup the floating action button
        this.fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        if (this.fab != null) {
            this.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final WifiManager wifiMan = (WifiManager) StaticContext.getContext().getSystemService(Context.WIFI_SERVICE);
                    if (!wifiMan.isWifiEnabled()) {
                        Snackbar.make(view, R.string.wifi_enable_wifi_to_scan, Snackbar.LENGTH_LONG)
                                .setAction(R.string.wifi_enable_wifi, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                wifiMan.setWifiEnabled(true);
                                                fab.callOnClick();
                                            }
                                        }
                                ).show();
                    } else {
                        WifiPickerDialog dialog = new WifiPickerDialog();
                        dialog.addOnDialogClosedListener(thisClass);
                        dialog.show(mActivity, container);
                    }
                }
            });
        }

        // setup recycler view
        this.recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        WifiListAdapter itemsAdapter = new WifiListAdapter(R.layout.list_item_wifilist, this.wifiListHandler, this.recyclerView, fab);

        this.recyclerView.setAdapter(itemsAdapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        StaticContext.setContext(context);

        this.mActivity = getActivity();

        this.wifiListHandler = new WifiListHandler(context);
        this.wifiListHandler.sort(); // important here: we are sorting before we are waiting for changes since we are creating the list later anyway
        this.wifiListHandler.addObserver(this);
    }

    @Override
    public void onDialogClosed(int returnCode, Object... returnValue) {
        if (returnCode == DialogInterface.BUTTON_POSITIVE) {
            this.wifiListHandler.add((WifiLocationEntry) returnValue[0]);
            this.recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void update(Observable o, Object data) {
        if (ManagerService.isServiceActive(StaticContext.getContext())) {
            AlarmReceiver.fireAndSchedule(StaticContext.getContext());
        }


        if (this.mActivity != null) {
            this.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!((WifiListAdapter) recyclerView.getAdapter()).isDeleteModeActive()) {
                        recyclerView.swapAdapter(new WifiListAdapter(R.layout.list_item_wifilist, wifiListHandler, recyclerView, fab), false);
                    }

                    recyclerView.requestLayout();
                    recyclerView.invalidate();
                }
            });
        }
    }


}