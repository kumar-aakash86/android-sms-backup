package sms_backup.poliveira.org.smsbackup;

import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private final String INBOX = "content://sms/inbox";
  private final String SENT = "content://sms/sent";

  private StringBuilder dataset;
  private String firstRow = "";
  private List<String> headers;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    dataset = new StringBuilder();

    String[] permissions = new String[]{
        Manifest.permission.READ_SMS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };
    ActivityCompat.requestPermissions(this, permissions, 1);

    final TextView portView = (TextView) this.findViewById(R.id.log);
    portView.setMovementMethod(new ScrollingMovementMethod());
    Button btnBackup = (Button) this.findViewById(R.id.btnBackup);
    btnBackup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
              .mkdirs();
          Log.w("Steps", "1");
          File dir = Environment
              .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
          Log.w("Steps", "2");
          File file = new File(dir, "sms_backup.csv");
          file.createNewFile();
          FileOutputStream fos = new FileOutputStream(file);
          Log.w("Steps", "3");

          getInbox();
          getSent();

          Log.w("Steps", "4");
          for (String header : headers) {
            firstRow += header + ";";
          }
          firstRow += "\n";
          fos.write(firstRow.getBytes());
          fos.write(dataset.toString().getBytes());
          fos.flush();
          fos.close();
          portView.setText("Success!");
        } catch (Exception e) {
          Log.e("Error", e.toString());
          e.printStackTrace();
          portView.setText(e.getMessage());
        }
      }
    });
  }

  private void getInbox() throws Exception {
    Cursor cursor = this.getContentResolver().query(Uri.parse(INBOX), null, null, null, null);

    if (cursor.moveToFirst()) {
      do {
        String row = "";
        headers = new ArrayList<>();
        for (int index = 0; index < cursor.getColumnCount(); index++) {
          if (!headers.contains(cursor.getColumnName(index))) {
            headers.add(cursor.getColumnName(index));
          }
//          if(cursor.getString(index) != null){
//
//          Log.e("data", cursor.getString(index).replace("\n", " ").replace("\r", " "));
//          }
          row +=  (cursor.getString(index) != null ? cursor.getString(index).replace("\n", " ").replace("\r", " ") : "") + ";";
        }
        row += "\n";
        dataset.append(row);
      } while (cursor.moveToNext());
    } else {
      throw new Exception("Fails to retrieve SMS inbox messages.");
    }
  }

  private void getSent() throws Exception {
    Cursor cursor = this.getContentResolver().query(Uri.parse(SENT), null, null, null, null);

    if (cursor.moveToFirst()) {
      do {
        String row = "";
        for (int index = 0; index < cursor.getColumnCount(); index++) {
          row += cursor.getString(index) + ";";
        }
        row += "\n";
        dataset.append(row);
      } while (cursor.moveToNext());
    } else {
      throw new Exception("Fails to retrieve SMS sent messages.");
    }
  }
}
