package riccardobusetti.globee;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

public class PlaceholderUiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }
}
