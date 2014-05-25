package py.com.fpuna.autotracks;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.provider.AutotracksContract.Localizaciones;
import py.com.fpuna.autotracks.provider.AutotracksContract.Rutas;
import py.com.fpuna.autotracks.tracking.AlarmIntentService;

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
                enviarRutas();
            case R.id.menu_item_eliminar_ruta:
                eliminarRuta();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void enviarRutas() {
        AlarmIntentService.startService(getActivity());

    }

    private void eliminarRuta() {
        Uri uri = Rutas.buildUri(rutaId);
        getActivity().getContentResolver().delete(uri, null, null);
        getActivity().finish();
    }

    private void initMap() {
        Iterable<Localizacion> localizacions = getLocalizacionesIterable();
        if (localizacions.iterator().hasNext()) {
            PolylineOptions polyline = new PolylineOptions();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Localizacion localizacion : localizacions) {
                LatLng position = new LatLng(localizacion.getLatitud(), localizacion.getLongitud());
                getMap().addMarker(new MarkerOptions().position(position));
                builder.include(position);
                polyline.add(position);
            }
            getMap().addPolyline(polyline.width(5));
            moveCamera(builder);
        }
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
