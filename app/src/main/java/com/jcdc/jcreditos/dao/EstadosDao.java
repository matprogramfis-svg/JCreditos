package com.jcdc.jcreditos.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.jcdc.jcreditos.db.DatabaseContract;
import com.jcdc.jcreditos.db.DbHelper;
import java.util.ArrayList;
import java.util.List;

public class EstadosDao {

		private DbHelper dbHelper;

		public EstadosDao(Context context) {
				dbHelper = new DbHelper(context);
			}

		// Obtener todos los estados
		public List<String> getAllEstados() {
				List<String> lista = new ArrayList<String>();
				SQLiteDatabase db = dbHelper.getReadableDatabase();

				Cursor cursor = db.rawQuery(
					"SELECT " + DatabaseContract.Estados.DESCRIPCION +
					" FROM " + DatabaseContract.Estados.TABLE,
					null
				);

				if (cursor.moveToFirst()) {
						do {
								lista.add(cursor.getString(0));
							} while (cursor.moveToNext());
					}

				cursor.close();
				db.close();

				return lista;
			}

		// Obtener descripción por id
		public String getDescripcionById(int id) {
				String descripcion = null;
				SQLiteDatabase db = dbHelper.getReadableDatabase();

				Cursor cursor = db.rawQuery(
					"SELECT " + DatabaseContract.Estados.DESCRIPCION +
					" FROM " + DatabaseContract.Estados.TABLE +
					" WHERE " + DatabaseContract.Estados.ID + "=?",
					new String[]{ String.valueOf(id) }
				);

				if (cursor.moveToFirst()) {
						descripcion = cursor.getString(0);
					}

				cursor.close();
				db.close();

				return descripcion;
			}
	}
