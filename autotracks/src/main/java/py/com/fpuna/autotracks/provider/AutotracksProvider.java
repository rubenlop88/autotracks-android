package py.com.fpuna.autotracks.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.provider.AutotracksContract.Rutas;
import py.com.fpuna.autotracks.provider.AutotracksContract.Localizaciones;

import java.util.ArrayList;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class AutotracksProvider extends ContentProvider {

    private AutotracksDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher;
    private static final int RUTAS = 100;
    private static final int RUTAS_ID = 101;
    private static final int LOCALIZACIONES = 200;
    private static final int LOCALIZACIONES_ID = 201;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AutotracksContract.CONTENT_AUTHORITY, Rutas.PATH, RUTAS);
        sUriMatcher.addURI(AutotracksContract.CONTENT_AUTHORITY, Rutas.PATH_ID, RUTAS_ID);
        sUriMatcher.addURI(AutotracksContract.CONTENT_AUTHORITY, Localizaciones.PATH, LOCALIZACIONES);
        sUriMatcher.addURI(AutotracksContract.CONTENT_AUTHORITY, Localizaciones.PATH_ID, LOCALIZACIONES_ID);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new AutotracksDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case RUTAS:
                return Rutas.CONTENT_TYPE;
            case RUTAS_ID:
                return Rutas.CONTENT_ITEM_TYPE;
            case LOCALIZACIONES:
                return Localizaciones.CONTENT_TYPE;
            case LOCALIZACIONES_ID:
                return Localizaciones.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String id;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case RUTAS:
                id = insertOrThrow(uri, values, db, getTable(Ruta.class));
                return Rutas.buildUri(id);
            case LOCALIZACIONES:
                id = insertOrThrow(uri, values, db, getTable(Localizacion.class));
                return Localizaciones.buildUri(id);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SelectionBuilder builder = buildSelection(uri).where(selection, selectionArgs);
        Cursor cursor = builder.query(db, projection, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        SelectionBuilder builder = buildSelection(uri).where(selection, selectionArgs);
        int result = builder.delete(db);
        notifyChange(uri);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        SelectionBuilder builder = buildSelection(uri).where(selection, selectionArgs);
        int result = builder.update(db, values);
        notifyChange(uri);
        return result;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int n = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[n];
            for (int i = 0; i < n; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    private SelectionBuilder buildSelection(Uri uri) {
        SelectionBuilder builder = new SelectionBuilder();
        String selection = BaseColumns._ID + " = ?";
        switch (sUriMatcher.match(uri)) {
            case RUTAS:
                return builder.table(getTable(Ruta.class));
            case RUTAS_ID:
                return builder.table(getTable(Ruta.class)).where(selection, Rutas.getId(uri));
            case LOCALIZACIONES:
                return builder.table(getTable(Localizacion.class));
            case LOCALIZACIONES_ID:
                return builder.table(getTable(Localizacion.class)).where(selection, Localizaciones.getId(uri));
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private String getTable(Class<?> entityClass) {
        return cupboard().getTable(entityClass);
    }

    private String insertOrThrow(Uri uri, ContentValues values, SQLiteDatabase db, String table) {
        long id = db.insertOrThrow(table, null, values);
        notifyChange(uri);
        return String.valueOf(id);
    }

    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null, false);
    }

}
