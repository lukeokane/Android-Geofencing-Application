package com.lukeshaun.mobileca1.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.lukeshaun.mobileca1.R;

import java.util.ArrayList;

public class ChatService extends Service {

    private final String TAG = "DEBUG " + this.getClass().getSimpleName();

    // Clients connected
    ArrayList<Messenger> mClients = new ArrayList<>();

    // Allows inter process communication (IPC).
    // Clients target this Messenger to communicate with service.
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    // Command to the service to register a client, receive callbacks from  service.
    public static final int MSG_REGISTER_CLIENT = 1;

    // Command to the service to unregister a client, stop receiving callbacks from  service.
    public static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    public static final int MSG_SET_VALUE = 3;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    Log.d(TAG, "New client registered, " + mClients.size() + " client(s) in total registered.");
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    Log.d(TAG, "Unregistering client, " + mClients.size() + " client(s) now registered.");
                    break;
                case MSG_SET_VALUE:
                    for (int i = mClients.size() - 1; i >=0 ; i--) {
                        try {
                            mClients.get(i).send(Message.obtain(null,
                                    MSG_SET_VALUE, 0, 0, msg.obj));
                        } catch (RemoteException e) {
                            // Client is no longer contactable.
                            mClients.remove(i);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

        // Tell the user we stopped.
        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
    }

    // Return interface for sending messages
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}