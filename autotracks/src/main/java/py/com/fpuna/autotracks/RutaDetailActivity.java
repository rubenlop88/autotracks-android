package py.com.fpuna.autotracks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

public class RutaDetailActivity extends ActionBarActivity {

    public static void startActivity(Context context, String id) {
        Intent detailIntent = new Intent(context, RutaDetailActivity.class);
        detailIntent.putExtra(RutaDetailFragment.EXTRA_RUTA_ID, id);
        context.startActivity(detailIntent);
    }

    private String rutaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruta_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            rutaId = getIntent().getStringExtra(RutaDetailFragment.EXTRA_RUTA_ID);
            RutaDetailFragment fragment = RutaDetailFragment.newInstance(rutaId);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, RutaListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
