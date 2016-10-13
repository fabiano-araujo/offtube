package br.com.developer.fabiano.ytdownload.adapters;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import br.com.developer.fabiano.ytdownload.R;

/**
 * Created by Fabiano on 06/08/2016.
 */
//https://www.googleapis.com/youtube/v3/channels?part=statistics&id=UC3KQ5GWANYF8lChqjZpXsQw&key=AIzaSyBGjwxwBfwHBlzBfpxZrgxvLKLzQhVnfeU
public class DownloadTask {
    private Context context;
    private int idNotification = 0;
    private PowerManager.WakeLock mWakeLock;
    private String title;
    private String formato;
    public Thread thread;
    private String result;
    private boolean cancelar = false;
    OutputStream output = null;
    List<DownloadTask> downloadTaskList;
    public DownloadTask downloadTask;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    public DownloadTask(Context context,String title,String formato,List<DownloadTask> downloadTasks){
        this.context = context;
        this.title = title;
        this.formato = formato;
        downloadTaskList = downloadTasks;
        this.idNotification = downloadTasks.size();
    }

    public NotificationCompat.Builder createNoficatiion(String titulo){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setTicker(titulo);
        builder.setContentTitle(titulo);
        builder.setSmallIcon(R.drawable.ic_file_download_white_24dp);
        return builder;
    }
    public String execute(final String... sUrl){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();

        builder = createNoficatiion("Baixando "+title);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setProgress(100,0,false);

        notificationManager.notify(idNotification, builder.build());
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream input = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(sUrl[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                         result = "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage();
                         cancelar = true;
                        Log.i("download",result);
                    }
                    if (result == null){
                        // this will be useful to display download percentage
                        // might be -1: server did not report the length
                        int fileLength = connection.getContentLength();

                        // download the file
                        input = connection.getInputStream();
                        String path = Environment.getExternalStorageDirectory().getPath()+"/YTdownload";
                        if (!new File(path).exists()) {
                            (new File(path)).mkdirs();
                        }
                        Log.i("Contente",connection.getHeaderField("Content-Disposition"));
                        output = new FileOutputStream(path+"/"+title+formato);

                        byte data[] = new byte[4096];
                        long total = 0;
                        int count;
                        while ((count = input.read(data)) != -1) {
                            Log.i("count",count+"");
                            // allow canceling with back button
                            if (cancelar) {
                                Log.i("download","cancelou");
                                input.close();
                                result = null;
                                break;
                            }
                            total += count;
                            // publishing the progress....
                            if (fileLength > 0){
                                builder.setProgress(100,(int) (total * 100 / fileLength),false);
                                notificationManager.notify(idNotification, builder.build());
                            }
                            output.write(data, 0, count);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result = e.toString();
                    Log.i("download","exception");
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    } catch (IOException ignored) {
                    }

                    if (connection != null)
                        connection.disconnect();
                }
                mWakeLock.release();
                if (result != null){
                    notificationManager.notify(idNotification, createNoficatiion("Ocorreu um erro no download").build());
                }
                else{
                    /*builder = createNoficatiion("Download terminado -\""+title+"\"!");
                    downloadTaskList.remove(downloadTask);
                    Intent it = new Intent(context,Player.class);
                    it.setData(Uri.parse(output.toString()));
                    it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    PendingIntent p = PendingIntent.getActivity(context, 0,it, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(p);
                    notificationManager.notify(idNotification, builder.build());*/
                }
            }
        });
        thread.start();
        return result;
    }
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}

