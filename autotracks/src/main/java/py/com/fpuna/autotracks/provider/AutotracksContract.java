package py.com.fpuna.autotracks.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class AutotracksContract {

    private AutotracksContract() {
    }

    public static final String CONTENT_AUTHORITY = "py.com.fpuna.autotracks";

    private static Uri buildContentUri(String path) {
        return Uri.parse("content://" + CONTENT_AUTHORITY).buildUpon().appendPath(path).build();
    }

    private static String buildContentType(String type) {
        return "vnd.android.cursor.dir/vnd.fpuna.autotracks.".concat(type);
    }

    private static String buildContentItemType(String type) {
        return "vnd.android.cursor.item/vnd.fpuna.autotracks.".concat(type);
    }

    private static Uri buildUri(Uri uri, String id) {
        return uri.buildUpon().appendPath(id).build();
    }

    public static String getId(Uri uri) {
        return uri.getPathSegments().get(1);
    }

    interface RutaColumns {
        String FECHA = "fecha";
        String SERVER_ID = "server_id";
        String FIN = "fin";
    }

    interface LocalizacionesColumns {
        String RUTA = "ruta";
        String IMEI = "imei";
        String LATITUD = "latitud";
        String LONGITUD = "longitud";
        String ALTITUD = "altitud";
        String EXACTITUD = "exactitud";
        String DIRECCION = "direccion";
        String VELOCIDAD = "velocidad";
        String FECHA = "fecha";
        String ENVIADO = "enviado";
    }

    public static class Rutas implements RutaColumns, BaseColumns {

        static final String TYPE = "ruta";
        static final String PATH = "rutas";
        static final String PATH_ID = PATH + "/*";

        public static final String CONTENT_TYPE = buildContentType(TYPE);
        public static final String CONTENT_ITEM_TYPE = buildContentItemType(TYPE);
        public static final Uri CONTENT_URI = buildContentUri(PATH);

        public static Uri buildUri(String id) {
            return AutotracksContract.buildUri(CONTENT_URI, id);
        }

        public static String getId(Uri uri) {
            return AutotracksContract.getId(uri);
        }

    }

    public static class Localizaciones implements LocalizacionesColumns, BaseColumns {

        static final String TYPE = "localizacion";
        static final String PATH = "localizaciones";
        static final String PATH_ID = PATH + "/*";

        public static final String CONTENT_TYPE = buildContentType(TYPE);
        public static final String CONTENT_ITEM_TYPE = buildContentItemType(TYPE);
        public static final Uri CONTENT_URI = buildContentUri(PATH);

        public static Uri buildUri(String id) {
            return AutotracksContract.buildUri(CONTENT_URI, id);
        }

        public static String getId(Uri uri) {
            return AutotracksContract.getId(uri);
        }

    }

}
