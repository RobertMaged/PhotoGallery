package z.com.android.photogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context){
        return new Intent(context, PhotoGalleryActivity.class);
    }

    @Override
    public Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}