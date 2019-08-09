package cn.launcher.album;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ibbhub.album.AlbumBean;

import java.util.ArrayList;

public class MyPreviewActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_preview);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        MyPreviewFragment fragment = (MyPreviewFragment) getSupportFragmentManager().findFragmentByTag("preview");
        if (fragment == null) {
            fragment = new MyPreviewFragment();
        }

        ArrayList<AlbumBean> data;
        Uri uri = getIntent().getData();
        if (uri != null) {
            String myImageUrl = "content://media/external/images/media/***";
            // uri = Uri.parse(myImageUrl);
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor actualimagecursor = this.managedQuery(uri, proj, null, null, null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            // int dateColumn = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
            actualimagecursor.moveToFirst();
            String img_path = actualimagecursor.getString(actual_image_column_index);
            // long takendate = actualimagecursor.getLong(dateColumn);
            android.util.Log.d("wlDebug", "" + uri.toString());
            android.util.Log.d("wlDebug", "" + img_path);
            data = new ArrayList<>();
            AlbumBean _bean = new AlbumBean();
            _bean.path = img_path;
            //_bean.date = takendate;
            _bean.isChecked = true;
            data.add(_bean);
        } else {
            data = getIntent().getParcelableArrayListExtra("data");
        }

        toolbar.setSubtitle("1/" + data.size());
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("data", data);
        bundle.putInt("pos", getIntent().getIntExtra("pos", 0));
        fragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flContainer, fragment, "preview");
        ft.commit();
    }

    public static void start(Context context, ArrayList<AlbumBean> data, int pos) {
        Intent starter = new Intent(context, MyPreviewActivity.class);
        starter.putParcelableArrayListExtra("data", data);
        starter.putExtra("pos", pos);
        context.startActivity(starter);
    }

    public void setSubtitle(String subtitle) {
        toolbar.setSubtitle(subtitle);
    }
}
