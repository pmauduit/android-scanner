package beneth.fr.androidscanner;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by pmauduit on 5/14/16.
 */
public class OcrTask extends AsyncTask<Object, Integer, String>
        implements TessBaseAPI.ProgressNotifier {

    private Button button;
    private TextView textView;
    private String recognized;
    private Bitmap bitmap;
    private TessBaseAPI baseApi;

    public OcrTask(Bitmap b, Button btn, TextView v) {
        button = btn;
        bitmap = b;
        textView = v;
        button.setEnabled(false);
        button.setText("Acquiring ...");
    }

    @Override
    protected String doInBackground(Object[] params) {
        baseApi = new TessBaseAPI(this);
        baseApi.init("/mnt/sdcard/tesseract", "eng");
        try {
            baseApi.setImage(bitmap);
            recognized = baseApi.getUTF8Text();
        } finally {
            baseApi.end();
        }
        return recognized;
    }

    @Override
    protected void onPostExecute(String l) {
        textView.setText(l);
        button.setEnabled(true);
        button.setText("Acquire from camera");
    }

    @Override
    public void onProgressUpdate(Integer... progressValue) {
        String msg = "Computing: " + progressValue[0] + "%";
        Log.d(this.getClass().getSimpleName(), msg);
        button.setText(msg);
    }

    @Override
    public void onProgressValues(TessBaseAPI.ProgressValues progressValues) {
        publishProgress(progressValues.getPercent());
    }
}
