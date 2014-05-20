package py.com.fpuna.autotracks;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Resultado;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.provider.AutotracksContract.Localizaciones;
import py.com.fpuna.autotracks.provider.AutotracksContract.Rutas;
import retrofit.RestAdapter;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class RutaDetailFragment extends Fragment {

    public static final String EXTRA_RUTA_ID = "ruta_id";

    public static RutaDetailFragment newInstance(String rutaId) {
        Bundle arguments = new Bundle();
        arguments.putString(RutaDetailFragment.EXTRA_RUTA_ID, rutaId);
        RutaDetailFragment fragment = new RutaDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    private String rutaId;
    private WebView webView = null;

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
        getActivity().finish();
    }

    public class EnviarRutaTask extends AsyncTask<String, Void, Resultado> {

        @Override
        protected Resultado doInBackground(String... params) {
            RestAdapter adapter = new RestAdapter.Builder().setEndpoint(WebService.ENDPOINT).setLogLevel(RestAdapter.LogLevel.FULL).build();
            WebService webService = adapter.create(WebService.class);
            // TODO eliminar esta AsyncTask y usa un Callback en su lugar
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
        if (webView == null) {
            webView = (WebView) getView().findViewById(R.id.webViewRuta);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {

                public void onPageFinished(WebView view, String url) {
                    Iterable<Localizacion> localizacions = getLocalizacionesIterable();

                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObject = new JSONObject();

                    try {
                        if (localizacions.iterator().hasNext()) {
                            for (Localizacion localizacion : localizacions) {
                                jsonObject = new JSONObject();
                                jsonObject.put("latitud", localizacion.getLatitud());
                                jsonObject.put("longitud", localizacion.getLongitud());
                                jsonArray.put(jsonObject);
                            }
                        }
                        jsonObject = new JSONObject();
                        jsonObject.put("ruta", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    view.loadUrl("javascript:dibujarRuta(" + jsonObject.toString() + ");");
                }
            });
            webView.loadUrl("file:///android_asset/rutas.html");
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
                //getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
            }
        });
    }

    private Iterable<Localizacion> getLocalizacionesIterable() {
        return cupboard().withContext(getActivity())
                .query(Localizaciones.CONTENT_URI, Localizacion.class)
                .withSelection(Localizaciones.RUTA + " = ? ", new String[]{rutaId})
                .query();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = (View) inflater.inflate(R.layout.activity_ruta_detail, container, false);
        return mainView;
    }
}
