package com.jcdc.jcreditos.dao;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import com.jcdc.jcreditos.db.*;
import com.jcdc.jcreditos.model.*;
import java.util.*;

public class CuotasDao {

		private DbHelper dbHelper;

		public CuotasDao(Context context) {
				dbHelper = new DbHelper(context);
			}

		// Obtener todas las cuotas de un crédito específico
		/*public Cursor getCuotasByCreditoId(int creditoId) {
				SQLiteDatabase db = dbHelper.getReadableDatabase();

				String selection = DatabaseContract.Cuotas.CREDITO_ID + " = ?";
				String[] selectionArgs = { String.valueOf(creditoId) };

				String sortOrder = DatabaseContract.Cuotas.NUMERO_CUOTA + " ASC";

				Cursor cursor = db.query(
					DatabaseContract.Cuotas.TABLE,
					null, // Devolvemos todas las columnas de la cuota
					selection,
					selectionArgs,
					null,
					null,
					sortOrder
				);

				// No cerramos la DB ni el Cursor aquí
				return cursor;
			}*/

		// Marcar una cuota como pagada (Usa el TRIGGER que ya definiste en DbHelper)
		public int markCuotaAsPaid(int cuotaId) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(DatabaseContract.Cuotas.PAGADA, 1); // 1 = Pagada

				String selection = DatabaseContract.Cuotas.ID + " = ?";
				String[] selectionArgs = { String.valueOf(cuotaId) };

				int count = db.update(
					DatabaseContract.Cuotas.TABLE,
					values,
					selection,
					selectionArgs
				);

				db.close();
				return count; // Retorna el número de filas afectadas
			}
		// ***
		// Nuevo método dentro de CuotasDao

// --- Auxiliar: Mapea Cursor a Cuota POJO ---
		private Cuota cursorToCuota(Cursor cursor) {
				Cuota cuota = new Cuota();
				cuota.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.ID)));
				cuota.setCreditoId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.CREDITO_ID)));
				cuota.setNumeroCuota(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.NUMERO_CUOTA)));
				cuota.setMontoCuota(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.MONTO_CUOTA)));
				cuota.setFechaPago(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.FECHA_PAGO)));
				cuota.setPagada(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.PAGADA)));

				// PAGADA_TS puede ser NULL en la BD
				int pagadaTsIndex = cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.PAGADA_TS);
				cuota.setPagadaTs(cursor.isNull(pagadaTsIndex) ? null : cursor.getString(pagadaTsIndex));

				return cuota;
			}

// --- Obtener Todas las Cuotas de un Crédito (Retorna Lista de POJOs) ---
		public List<Cuota> getCuotasByCreditoId(int creditoId) {
				List<Cuota> lista = new ArrayList<>();
				SQLiteDatabase db = dbHelper.getReadableDatabase();

				String selection = DatabaseContract.Cuotas.CREDITO_ID + " = ?";
				String[] selectionArgs = { String.valueOf(creditoId) };
				String sortOrder = DatabaseContract.Cuotas.NUMERO_CUOTA + " ASC";

				Cursor cursor = db.query(DatabaseContract.Cuotas.TABLE, null, selection, selectionArgs, null, null, sortOrder);

				if (cursor.moveToFirst()) {
						do {
								lista.add(cursorToCuota(cursor));
							} while (cursor.moveToNext());
					}

				cursor.close();
				db.close();
				return lista;
			}

		// *** Faltarían métodos para insertar las cuotas (usado al crear un crédito) ***
	}
