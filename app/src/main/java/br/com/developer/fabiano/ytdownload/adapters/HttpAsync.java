package br.com.developer.fabiano.ytdownload.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import java.util.HashMap;
import br.com.developer.fabiano.ytdownload.interfaces.AsyncResponse;


/**
 * Created by Fabiano on 22/07/2016.
 */
public class HttpAsync {
    HashMap<String,String> data;
    Context mContext;
    public Thread thread;
    public String result;
    public AsyncResponse delegate=null;

    public HttpAsync(HashMap<String, String> data) {
        this.data = data;
    }

    public HttpAsync(Context mContext) {
        this.mContext = mContext;
    }

    public HttpAsync(HashMap<String, String> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }
    public String execute(final String... urls){
        try{
            if (thread.isAlive()){
                thread.interrupt();
                Log.i("alivee",thread.isAlive()+"");
            }
        }catch (Exception e){}
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (urls[1].equalsIgnoreCase("get")){
                    result = HttpConnections.getJson(urls[0]);
                }else{
                    result = HttpConnections.performPostCall(urls[0],data);
                }
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            delegate.processFinish(result);   
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        thread.start();
        return result;
    }
}