package com.lukeshaun.mobileca1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.lukeshaun.mobileca1.service.ChatService;

public class ChatActivity extends AppCompatActivity {

    private final String TAG = "DEBUG " + this.getClass().getSimpleName();

    Messenger mService = null;

    // Check if service is bound
    boolean mIsBound;

    // Text information on bound service
    TextView mCallbackText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mCallbackText = findViewById(R.id.callbackText);
        doBindService();
    }

    // Handle incoming messages
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ChatService.MSG_SET_VALUE:
                    Bundle bundle = (Bundle) msg.obj;
                    String message = bundle.getString("message");

                    mCallbackText.setText(message);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    // Send messages from service handler
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    // Interact with service
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // Returns service to connect with.
            mService = new Messenger(service);
            mCallbackText.setText("Awaiting message.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        ChatService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // Service crashed, nothing can be done here.
            }

            // As part of the sample, tell the user what happened.
            Toast.makeText(ChatActivity.this, R.string.remote_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mCallbackText.setText("Disconnected.");

            // As part of the sample, tell the user what happened.
            Toast.makeText(ChatActivity.this, R.string.remote_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        Log.d(TAG,"Attempting to bind to chat service...");

        // Establish connection
        bindService(new Intent(ChatActivity.this,
                ChatService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        mCallbackText.setText("Connecting...");
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            ChatService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            mCallbackText.setText("Unbinding.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
