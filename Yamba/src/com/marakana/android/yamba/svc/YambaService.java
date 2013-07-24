package com.marakana.android.yamba.svc;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.marakana.android.yamba.R;
import com.marakana.android.yamba.YambaApplication;
import com.marakana.android.yamba.clientlib.YambaClientException;


public class YambaService extends IntentService {
    private static final String TAG = "SVC";

    static final int OP_POST_COMPLETE = -1;

    private static final String PARAM_STATUS = "YambaService.STATUS";

    private static class Hdlr extends Handler {
        private final  YambaService svc;

        public Hdlr(YambaService svc) { this.svc = svc; }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case OP_POST_COMPLETE:
                    Toast.makeText(svc, msg.arg1, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    public static void post(Context ctxt, String msg) {
        Intent i = new Intent(ctxt, YambaService.class);
        i.putExtra(PARAM_STATUS, msg);
        ctxt.startService(i);
    }


    private volatile Hdlr hdlr;

    public YambaService() { super(TAG); }

    @Override
    public void onCreate() {
        super.onCreate();

        hdlr = new Hdlr(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String status = intent.getStringExtra(PARAM_STATUS);
        Log.d(TAG, "posting: " + status);

        int msg = R.string.statusFailed;
        try {
            ((YambaApplication) getApplication()).getClient().postStatus(status);
            msg = R.string.statusSucceeded;
        }
        catch (YambaClientException e) {
            Log.e(TAG, "Post failed: " + e, e);
        }

        Message.obtain(hdlr, OP_POST_COMPLETE, msg, 0).sendToTarget();
    }
}