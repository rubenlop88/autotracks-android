package py.com.fpuna.autotracks;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.provider.AutotracksContract.Localizaciones;
import py.com.fpuna.autotracks.provider.AutotracksContract.Rutas;
import py.com.fpuna.autotracks.tracking.AlarmIntentService;

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
                enviarRutas();
                return true;
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
        if (webView == null) {
            webView = (WebView) getView().findViewById(R.id.webViewRuta);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
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

    private Iterable<Localizacion> getLocalizacionesIterable() {
        return cupboard().withContext(getActivity())
                .query(Localizaciones.CONTENT_URI, Localizacion.class)
                .withSelection(Localizaciones.RUTA + " = ? ", new String[]{rutaId})
                .query();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.activity_ruta_detail, container, false);
        return mainView;
    }

}
