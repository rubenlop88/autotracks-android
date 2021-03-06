package py.com.fpuna.autotracks.tracking;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.ArrayList;
import java.util.List;

import py.com.fpuna.autotracks.WebService;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Resultado;
import py.com.fpuna.autotracks.model.Ruta;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import py.com.fpuna.autotracks.provider.AutotracksContract.Localizaciones;
import py.com.fpuna.autotracks.provider.AutotracksContract.Rutas;
import py.com.fpuna.autotracks.util.PreferenceUtils;

public class DataUploadIntentService extends WakefulIntentService {

    private static String TAG = DataUploadIntentService.class.getSimpleName();

    public static void startService(Context context) {
        WakefulIntentService.sendWakefulWork(context, DataUploadIntentService.class);
    }

    public DataUploadIntentService() {
        super("DataUploadIntentService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {

        eliminarLocalizacionesViejas();

        long rutaId = -1;
        Ruta ruta = null;
        List<Ruta> rutas = new ArrayList<>();

        for (Localizacion localizacion : getLocalizaciones()) {
            if (localizacion.getRuta() != rutaId) {
                rutaId = localizacion.getRuta();
                ruta = getRuta(rutaId);
                ruta.setLocalizaciones(new ArrayList<Localizacion>());
                rutas.add(ruta);
            }
            if (ruta!= null) {
                ruta.getLocalizaciones().add(localizacion);
            }
        }

        try {
            WebService.RutasResource rutasResource = WebService.getRutasResource();
            for (Ruta r : rutas) {
                if (r.getLocalizaciones().size() > 5) { // no enviamos rutas con menos de 5 localizaciones
                    r.setLocalizaciones(getLocalizacionesNoEnviadas(r)); // enviamos solo lacalizaciones no enviadas
                    Resultado resultado = rutasResource.guardarRuta(r);
                    if (resultado != null && resultado.isExitoso()) {
                        updateRuta(r, resultado.getId());
                        updateLocalizaciones(r);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG,"Error de comunicación", e);
        }

    }

    /**
     * Obtiene las localizaciones no enviadas de la lista de localizaciones de la ruta.
     */
    private List<Localizacion> getLocalizacionesNoEnviadas(Ruta ruta) {
        List<Localizacion> localizacionesNoEnviadas = new ArrayList<>();
        for (Localizacion l : ruta.getLocalizaciones()) {
            if (Localizacion.Enviado.FALSE.equals(l.getEnviado())) {
                localizacionesNoEnviadas.add(l);
            }
        }
        return localizacionesNoEnviadas;
    }

    /**
     * Obtiene todas las localizaciones no enviadas aun al servidor.
     */
    private Iterable<Localizacion> getLocalizaciones() {
        return cupboard().withContext(getApplicationContext())
                .query(Localizaciones.CONTENT_URI, Localizacion.class)
                .orderBy(Localizaciones.RUTA)
                .query();
    }

    /**
     * Elimina las localizaciones del dia anterior.
     */
    private void eliminarLocalizacionesViejas() {
        long fecha = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        String where = Localizaciones.FECHA + " < " + fecha;
        getContentResolver().delete(Localizaciones.CONTENT_URI, where, null);
    }

    /**
     * Obtiene una ruta.
     */
    private Ruta getRuta(long rutaId) {
        Uri uri = Rutas.buildUri(String.valueOf(rutaId));
        return cupboard().withContext(getApplicationContext()).get(uri, Ruta.class);
    }

    /**
     * Actualiza la ruta. Guarda el id generado en el servidor.
     */
    private void updateRuta(Ruta ruta, Long serverId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Rutas.SERVER_ID, serverId);
        String where = Rutas._ID + " = ? ";
        String[] selectionArgs = { String.valueOf(ruta.getId()) };
        getContentResolver().update(Rutas.CONTENT_URI, contentValues, where, selectionArgs);
    }

    /**
     * Actualiza las localizaciones. Setea enviado = true
     */
    private void updateLocalizaciones(Ruta ruta) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Localizaciones.ENVIADO, Localizacion.Enviado.TRUE);
        String where = Localizaciones.RUTA + " = ? AND " + Localizaciones.ENVIADO + " = ? ";
        String[] selectionArgs = { String.valueOf(ruta.getId()), Localizacion.Enviado.FALSE };
        getContentResolver().update(Localizaciones.CONTENT_URI, contentValues, where, selectionArgs);
    }

}
