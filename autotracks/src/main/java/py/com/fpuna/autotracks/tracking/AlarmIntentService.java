package py.com.fpuna.autotracks.tracking;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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

public class AlarmIntentService extends WakefulIntentService {

    public static void startService(Context context) {
        WakefulIntentService.sendWakefulWork(context, AlarmIntentService.class);
    }

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {

        long rutaId = -1;
        Ruta ruta = null;
        List<Ruta> rutas = new ArrayList<Ruta>();

        for (Localizacion localizacion : getLocalizacionesNoEnviadas()) {
            if (localizacion.getRuta() != rutaId) {
                rutaId = localizacion.getRuta();
                ruta = getRuta(rutaId);
                ruta.setLocalizaciones(new ArrayList<Localizacion>());
                rutas.add(ruta);
            }
            ruta.getLocalizaciones().add(localizacion);
        }

        WebService.RutasResource rutasResource = WebService.getRutasResource();

        for (Ruta r : rutas) {
            Resultado resultado = rutasResource.guardarRuta(r);
            if (resultado != null && resultado.isExitoso()) {
                updateRuta(r, resultado.getId());
                updateLocalizaciones(r);
            }
        }

    }

    /**
     * Obtiene todas las localizaciones no enviadas aun al servidor.
     */
    private Iterable<Localizacion> getLocalizacionesNoEnviadas() {
        return cupboard().withContext(getApplicationContext())
                .query(Localizaciones.CONTENT_URI, Localizacion.class)
                .withSelection(Localizaciones.ENVIADO + " = ? ", new String[]{ Localizacion.Enviado.FALSE })
                .orderBy(Localizaciones.RUTA)
                .query();
    }

    /**
     * Obtiene una ruta.
     */
    private Ruta getRuta(long rutaId) {
        Uri uri = Rutas.buildUri(String.valueOf(rutaId));
        Ruta ruta = cupboard().withContext(getApplicationContext()).get(uri, Ruta.class);
        return ruta;
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
