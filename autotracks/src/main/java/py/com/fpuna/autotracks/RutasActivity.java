package py.com.fpuna.autotracks;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import py.com.fpuna.autotracks.adapter.RutasAdapter;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.provider.AutotracksContract;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class RutasActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutas);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Iterable<Ruta> rutas = cupboard().withContext(getApplicationContext())
                .query(AutotracksContract.Rutas.CONTENT_URI, Ruta.class)
                .orderBy(AutotracksContract.Rutas.FECHA)
                .query();

        ArrayList<Ruta> rutasArray = new ArrayList<Ruta>();
        for(Ruta element : rutas)
        {
            rutasArray.add(element);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rutas_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RutasAdapter mAdapter =  new RutasAdapter(rutasArray.toArray(new Ruta[]{}));

        recyclerView.setAdapter(mAdapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());


    }
}
