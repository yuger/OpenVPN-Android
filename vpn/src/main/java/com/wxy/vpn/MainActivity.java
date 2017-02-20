package com.wxy.vpn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.R;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;

public class MainActivity extends AppCompatActivity {
    private SwitchCompat scOpenVpn;
    private boolean isVpnStarted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VpnStatus.initLogCache(getApplicationContext().getCacheDir());
        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        scOpenVpn = (SwitchCompat) findViewById(R.id.sc_vpn);
        scOpenVpn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    startVpn();
                    isVpnStarted = true;
                } else {
                    closeVpn();
                    isVpnStarted = false;
                }
            }
        });
    }

    private void startVpn() {
        try {
            InputStream conf = getAssets().open("client.ovpn");// your own file in /assets/client.ovpn
            InputStreamReader isr = new InputStreamReader(conf);
            BufferedReader br = new BufferedReader(isr);
            String config = "";
            String line;
            while (true) {
                line = br.readLine();
                if (line == null) break;
                config += line + "\n";
            }
            br.readLine();
            OpenVpnApi.startVpn(this, config, null, null);
        } catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
    }

    //==============
    protected OpenVPNService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    private void closeVpn() {
        ProfileManager.setConntectedVpnProfileDisconnected(this);
        if (mService != null && mService.getManagement() != null) {
            mService.getManagement().stopVPN(false);
        }
    }
}
