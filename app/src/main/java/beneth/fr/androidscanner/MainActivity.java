package beneth.fr.androidscanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final File dir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button capture = (Button) findViewById(R.id.acquire);

        dir.mkdirs();

        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String file = "acquired.jpg";
                File newfile = new File(file);
                try {
                    newfile.createNewFile();
                } catch (IOException e) {
                }

                Uri outputFileUri = Uri.fromFile(newfile);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

                startActivityForResult(cameraIntent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            File acquired = new File(dir, "acquired.jpg");
            try {
                FileUtils.readFileToByteArray(acquired);
                Log.d(this.getLocalClassName(), "Picture acquired");
            } catch (IOException e) {
                Log.e(this.getLocalClassName(), "Unable to read photo", e);
            } finally {
                FileUtils.deleteQuietly(acquired);
            }
        }

    }
}
