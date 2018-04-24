package com.eduid.EduIdApp.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

/**
 * Created by usi on 31.10.16.
 */
public class ResponseService {

    private Context context;
    private String thirdPartyAppID;
    private Bundle bundle;
    /**
     * Communication Params
     */
    private Messenger messenger = null; //used to make an RPC invocation
    public boolean isBound = false;
    public ServiceConnection connection;//receives callbacks from bind and unbind invocations
    private Messenger replyTo = null; //invocation replies are processed by this Messenger

    /**
     * Return data to Third Party App
     * @param bundle Response data
     */
    public ResponseService(Context context, String thirdPartyAppID, Bundle bundle){
        this.context = context;
        this.thirdPartyAppID = thirdPartyAppID;
        this.bundle = bundle;

        /**
         * Service connection creation
         */
        this.connection = new RemoteServiceConnection();
        this.replyTo = new Messenger(new IncomingHandler());
        bindService();

    }

    private void bindService(){



        // Unbind if it is bound to the service
        if (this.isBound) {
            context.unbindService(this.connection);
            this.isBound = false;
        }

        //Bind to the remote service
        Intent intent = new Intent();
        intent.setClassName(thirdPartyAppID, "com.eduid.Library.Services.AuthorizationCallbackService");

        // Third party app MUST add to manifest:
        // <service android:name="com.eduid.Library.Services.AuthorizationCallbackService"></service>

        context.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);

    }

    /**
     * Invoke service binded
     */
    private void invokeService(){
        //Setup the message for invocation
        Message message = Message.obtain(null, 1, 0, 0);
        try
        {

            //Set the ReplyTo Messenger for processing the invocation response
            message.replyTo = this.replyTo;

            message.setData(this.bundle);

            //Make the invocation
            this.messenger.send(message);
        }
        catch(RemoteException rme)
        {
            //Show an Error Message
            Toast.makeText(ResponseService.this.context, "Invocation Failed!!", Toast.LENGTH_LONG).show();
        }
    }


    private class RemoteServiceConnection implements ServiceConnection
    {

        @Override
        public void onServiceConnected(ComponentName component, IBinder binder)
        {

            ResponseService.this.messenger = new Messenger(binder);

            ResponseService.this.isBound = true;

            ResponseService.this.invokeService();

        }

        @Override
        public void onServiceDisconnected(ComponentName component)
        {

            ResponseService.this.messenger = null;

            ResponseService.this.isBound = false;

            Toast.makeText(ResponseService.this.context, "Service is not Bound!!", Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Callback do nothing, only close communication service
     */
    private class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {


            // Unbind if it is bound to the service
            if (ResponseService.this.isBound) {
                context.unbindService(ResponseService.this.connection);
                ResponseService.this.isBound = false;
            }

        }
    }

}
