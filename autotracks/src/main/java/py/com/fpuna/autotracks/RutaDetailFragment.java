package py.com.fpuna.autotracks;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Resultado;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.provider.AutotracksContract.Localizaciones;
import py.com.fpuna.autotracks.provider.AutotracksContract.Rutas;
import retrofit.RestAdapter;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class RutaDetailFragment extends SupportMapFragment {

    public static final String EXTRA_RUTA_ID = "ruta_id";

    public static RutaDetailFragment newInstance(String rutaId) {
        Bundle arguments = new Bundle();
        arguments.putString(RutaDetailFragment.EXTRA_RUTA_ID, rutaId);
        RutaDetailFragment fragment = new RutaDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    private String rutaId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments().containsKey(EXTRA_RUTA_ID)) {
            rutaId = getArguments().getString(EXTRA_RUTA_ID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initMap();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_ruta_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_enviar_ruta:
                enviarRuta();
                return true;
            case R.id.menu_item_eliminar_ruta:
                eliminarRuta();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void enviarRuta() {
        new EnviarRutaTask().execute(rutaId);
    }

    private void eliminarRuta() {
        Uri uri = Rutas.buildUri(rutaId);
        getActivity().getContentResolver().delete(uri, null, null);
    }

    public class EnviarRutaTask extends AsyncTask<String, Void, Resultado> {

        @Override
        protected Resultado doInBackground(String... params) {
            RestAdapter adapter = new RestAdapter.Builder().setEndpoint(WebService.ENDPOINT).setLogLevel(RestAdapter.LogLevel.FULL).build();
            WebService webService = adapter.create(WebService.class);
            Resultado resultado = webService.guardarRuta(getRuta());
            return resultado;
        }

        @Override
        protected void onPostExecute(Resultado resultado) {
            if (resultado != null) {
                if (resultado.isExitoso()) {
                    Toast.makeText(getActivity(), "Ruta guardada!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error: " + resultado.getMensaje(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        private Ruta getRuta() {
            Uri uri = Rutas.buildUri(rutaId);
            Ruta ruta = cupboard().withContext(getActivity()).get(uri, Ruta.class);
            ruta.setLocalizaciones(getLocalizaciones());
            ruta.setId(null); // para que Retrofit no envie en el JSON
            return ruta;
        }

        private List<Localizacion> getLocalizaciones() {
            List<Localizacion> localizaciones = new ArrayList<Localizacion>();
            for (Localizacion localizacion : getLocalizacionesIterable()) {
                localizacion.setId(null);  // para que Retrofit no envie en el JSON
                localizaciones.add(localizacion);
            }
            return localizaciones;
        }

    }

    private void initMap() {
        PolylineOptions polyline = new PolylineOptions();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Localizacion localizacion : getLocalizacionesIterable()) {
            LatLng position = new LatLng(localizacion.getLatitud(), localizacion.getLongitud());
            getMap().addMarker(new MarkerOptions().position(position));
            builder.include(position);
            polyline.add(position);
        }
        getMap().addPolyline(polyline.width(5));
        moveCamera(builder);
    }

    private void moveCamera(final LatLngBounds.Builder builder) {
        getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
            }
        });
    }

    private Iterable<Localizacion> getLocalizacionesIterable() {
        return cupboard().withContext(getActivity())
                .query(Localizaciones.CONTENT_URI, Localizacion.class)
                .withSelection(Localizaciones.RUTA + " = ? ", new String[]{rutaId})
                .query();
    }

}
