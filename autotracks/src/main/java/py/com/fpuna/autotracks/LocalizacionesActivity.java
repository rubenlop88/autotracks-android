package py.com.fpuna.autotracks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

import py.com.fpuna.autotracks.adapter.LocalizacionesAdapter;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.provider.AutotracksContract;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LocalizacionesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localizaciones);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Long rutaId = getIntent().getLongExtra("track_id", 0);
        Iterable<Localizacion> localizaciones = cupboard().withContext(getApplicationContext())
                .query(AutotracksContract.Localizaciones.CONTENT_URI, Localizacion.class)
                .orderBy(AutotracksContract.Localizaciones.FECHA)
                .withSelection(AutotracksContract.Localizaciones.RUTA + " = ? ", new String[] {rutaId.toString()})
                .query();

        ArrayList<Localizacion> localizacionesArray = new ArrayList<Localizacion>();
        for(Localizacion element : localizaciones)
        {
            localizacionesArray.add(element);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.localizacioens_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LocalizacionesAdapter mAdapter =  new LocalizacionesAdapter(localizacionesArray.toArray(new Localizacion[]{}));

        recyclerView.setAdapter(mAdapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());


    }
}
