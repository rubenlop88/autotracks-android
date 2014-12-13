package py.com.fpuna.autotracks.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import nl.qbusict.cupboard.CupboardBuilder;
import nl.qbusict.cupboard.CupboardFactory;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Ruta;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class AutotracksDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "autotracks.db";

    private static final int VERSION = 3;

    private static final int DATABASE_VERSION = VERSION;

    static {
        CupboardFactory.setCupboard(new CupboardBuilder().useAnnotations().build());
        cupboard().register(Localizacion.class);
        cupboard().register(Ruta.class);
    }

    public AutotracksDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        cupboard().withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        cupboard().withDatabase(db).upgradeTables();
    }

}
