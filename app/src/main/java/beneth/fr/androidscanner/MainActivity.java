package beneth.fr.androidscanner;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final File dir = new File(System.getenv("EXTERNAL_STORAGE") +
        File.separator + "android-scanner"
    );

    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button capture = (Button) findViewById(R.id.acquire);

        dir.mkdirs();
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    File image = File.createTempFile(
                            "ascanner",
                            ".jpg",
                            dir
                    );
                    photoPath = image.getAbsolutePath();
                    Uri outputFileUri = Uri.fromFile(image);
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    startActivityForResult(cameraIntent, 0);
                } catch (IOException e) {
                    Log.e(MainActivity.class.getName(), "Unable to read photo", e);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            // Should not happen
            if (photoPath == null) {
                return;
            }
            File acquired = new File(photoPath);
            try {
                Log.d(this.getLocalClassName(), "Picture acquired");
                Log.d(this.getLocalClassName(), "Trying to OCR the picture ...");

                String f = acquired.getAbsolutePath();

                // Rotates if needed
                ExifInterface exif = new ExifInterface(f);
                int exifOrientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                int rotate = 0;
                switch (exifOrientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate = 270;
                        break;
                }
                Bitmap bitmap = BitmapFactory.decodeFile(f);
                if (rotate != 0) {
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();
                    Matrix mtx = new Matrix();
                    mtx.preRotate(rotate);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                }
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(
                        Bitmap.createScaledBitmap(bitmap,
                                320,
                                200, false)
                );
                Button btn = (Button) findViewById(R.id.acquire);
                TextView tv = (TextView) findViewById(R.id.editText);

                new OcrTask(bitmap, btn, tv).execute((Object[]) null);

                Log.d(this.getLocalClassName(), "Done.");

            } catch (IOException e) {
                Log.e(this.getLocalClassName(), "Unable to read photo", e);

            } catch (Throwable throwable) {
                Log.e(this.getLocalClassName(), "Other error occurred", throwable);
            }

            finally {
                FileUtils.deleteQuietly(acquired);
            }
        }
    }
}
