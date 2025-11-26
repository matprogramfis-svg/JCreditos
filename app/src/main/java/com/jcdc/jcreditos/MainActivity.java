package com.jcdc.jcreditos;

import android.app.*;
import android.database.*;
import android.database.sqlite.*;
import android.os.*;
import android.util.*;
import com.jcdc.jcreditos.db.*;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		// 🔥 LLAMAR AQUÍ AL DUMP
        DbHelper helper = new DbHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        dumpTable(db, DatabaseContract.Estados.TABLE);   // Ejemplo
        // dumpTable(db, DatabaseContract.Creditos.TABLE_NAME); // Otro ejemplo
        // dumpTable(db, DatabaseContract.Cuotas.TABLE_NAME);    // Otro ejemplo

        db.close();
    }
	// ***
		public void dumpTable(SQLiteDatabase db, String tableName) {
				Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);

				if (cursor.moveToFirst()) {
						do {
								StringBuilder row = new StringBuilder();

								for (int i = 0; i < cursor.getColumnCount(); i++) {
										row.append(cursor.getColumnName(i))
											.append("=")
											.append(cursor.getString(i))
											.append(" | ");
									}

								Log.d("DB_DUMP_" + tableName, row.toString());
							} while (cursor.moveToNext());
					} else {
						Log.d("DB_DUMP_" + tableName, "La tabla está vacía.");
					}

				cursor.close();
			}
}
