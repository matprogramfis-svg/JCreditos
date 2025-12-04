package com.jcdc.jcreditos.dao;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import com.jcdc.jcreditos.db.*;
import com.jcdc.jcreditos.model.*;
import java.text.*;
import java.util.*;

public class CuotasDao {

		// 💡 FORMATO ESTÁNDAR PARA ALMACENAR EN SQLITE (yyyy-MM-dd)
		private static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		
		private DbHelper dbHelper;

		public CuotasDao(Context context) {
				dbHelper = new DbHelper(context);
			}
		// ***
		public int revertirPago(int cuotaId) {
				/*ContentValues cv = new ContentValues();
				cv.put("pagada", 0);
				return db.update("cuotas", cv, "id=?", new String[]{String.valueOf(cuotaId)});*/
				SQLiteDatabase db = dbHelper.getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(DatabaseContract.Cuotas.PAGADA, 0); // 0 = Revierte

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
		/**
		 * 🔥 NUEVO MÉTODO: Marca una cuota como NO PAGADA (Estado = 0).
		 */
		public int markCuotaAsUnpaid(int cuotaId) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put(DatabaseContract.Cuotas.PAGADA, 0); // Revertir el estado a pendiente

				String selection = DatabaseContract.Cuotas.ID + " = ?";
				String[] selectionArgs = { String.valueOf(cuotaId) };

				int rowsAffected = db.update(DatabaseContract.Cuotas.TABLE, values, selection, selectionArgs);
				db.close();
				return rowsAffected;
			}
		// Nuevo método dentro de CuotasDao

// --- Auxiliar: Mapea Cursor a Cuota POJO ---
		private Cuota cursorToCuota(Cursor cursor) {
				Cuota cuota = new Cuota();
				cuota.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.ID)));
				cuota.setCreditoId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.CREDITO_ID)));
				cuota.setNumeroCuota(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.NUMERO_CUOTA)));
				cuota.setMontoCuota(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.MONTO_CUOTA)));
				// 💡 CONVERSIÓN DE Date A String para SQLite
				// Obtener la fecha de la base de datos (String)
				String fechaString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.FECHA_PAGO));

				// Convertir el String de la BD a un objeto Date para el POJO
				try {
						// 💡 1. Definir el formato de fecha que usaste al GUARDAR en la DB
						// Generalmente es "yyyy-MM-dd" o similar. Asegúrate de que este formato
						// coincida con cómo se guardó la fecha inicialmente.
						SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US); 

						// 💡 2. Parsear el String para obtener el objeto Date
						Date fechaObjeto = dbDateFormat.parse(fechaString);

						// 💡 3. Asignar el objeto Date al POJO
						cuota.setFechaPago(fechaObjeto); 

					} catch (ParseException e) {
						// Manejar el error si el String no tiene el formato esperado
						e.printStackTrace();
						// Opcional: Asignar null si falla la conversión
						cuota.setFechaPago(null); 
					}
				//cuota.setFechaPago(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.FECHA_PAGO)));
				cuota.setPagada(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.PAGADA)));

				// PAGADA_TS puede ser NULL en la BD
				int pagadaTsIndex = cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.PAGADA_TS);
				cuota.setPagadaTs(cursor.isNull(pagadaTsIndex) ? null : cursor.getString(pagadaTsIndex));

				return cuota;
			}

// --- Obtener Todas las Cuotas de un Crédito (Retorna Lista de POJOs) ---
		/*public List<Cuota> getCuotasByCreditoId(int creditoId) {
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
			}*/

		// *** Faltarían métodos para insertar las cuotas (usado al crear un crédito) ***
		/**
		 * Inserta una única cuota en la base de datos.
		 * Este método está diseñado para ser llamado dentro de una transacción por CreditosDao.
		 * @param db La instancia de SQLiteDatabase (debe ser la misma que maneja la transacción).
		 * @param cuota Objeto Cuota a insertar.
		 * @return El ID de la fila insertada o -1 en caso de error.
		 */
		public long insertCuota(SQLiteDatabase db, Cuota cuota) {
				ContentValues values = new ContentValues();

				// Asignar los valores a la tabla CUOTAS
				values.put(DatabaseContract.Cuotas.CREDITO_ID, cuota.getCreditoId());
				values.put(DatabaseContract.Cuotas.NUMERO_CUOTA, cuota.getNumeroCuota());
				values.put(DatabaseContract.Cuotas.MONTO_CUOTA, cuota.getMontoCuota());
				// 💡 SOLUCIÓN: Conversión de Date a String
				String fechaPagoString = DB_DATE_FORMAT.format(cuota.getFechaPago());

				// ✅ PASAR LA STRING A CONTENTVALUES
				values.put(DatabaseContract.Cuotas.FECHA_PAGO, fechaPagoString);
				//values.put(DatabaseContract.Cuotas.FECHA_PAGO, cuota.getFechaPago());
				//values.put(DatabaseContract.Cuotas.PAGADA, cuota.isPagada()); // O 0 si no se setea
				// CORRECCIÓN: Usar getPagada() para obtener el valor INT (0 o 1)
				values.put(DatabaseContract.Cuotas.PAGADA, cuota.getPagada());

				// Nota: PAGADA_TS y CREADO_TS se manejan por defecto o en el trigger.

				// Realizar la inserción. No se cierra la DB aquí.
				long rowId = db.insert(DatabaseContract.Cuotas.TABLE, null, values);

				return rowId;
			}
		/**
		 * Recupera todas las cuotas asociadas a un ID de crédito específico.
		 */
		/*public List<Cuota> getCuotasByCreditoId(int creditoId) {
				SQLiteDatabase db = dbHelper.getReadableDatabase();
				List<Cuota> cuotasList = new ArrayList<>();

				// Consulta para obtener las cuotas ordenadas por número de cuota
				String selection = DatabaseContract.Cuotas.CREDITO_ID + " = ?";
				String[] selectionArgs = { String.valueOf(creditoId) };
				String orderBy = DatabaseContract.Cuotas.NUMERO_CUOTA + " ASC";

				Cursor cursor = null;
				try {
						cursor = db.query(
							DatabaseContract.Cuotas.TABLE,
							null, // Devolver todas las columnas
							selection,
							selectionArgs,
							null,
							null,
							orderBy
						);

						while (cursor.moveToNext()) {
								Cuota cuota = new Cuota();

								// Asegúrate de que los nombres de columnas coincidan con DatabaseContract
								cuota.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.ID)));
								cuota.setCreditoId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.CREDITO_ID)));
								cuota.setNumeroCuota(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.NUMERO_CUOTA)));
								cuota.setMontoCuota(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.MONTO_CUOTA)));
								cuota.setPagada(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.PAGADA)));

								// Convertir la fecha de String (guardada en DB) a Date
								String fechaString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Cuotas.FECHA_PAGO));
								try {
										// ✅ CORRECTO:
										//SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
										cuota.setFechaPago(DB_DATE_FORMAT.parse(fechaString));
									} catch (ParseException e) {
										e.printStackTrace();
										cuota.setFechaPago(null);
									}
									cuotasList.add(cuota);
							}
					} finally {
						if (cursor != null) {
								cursor.close();
							}
						db.close(); // Cerrar la base de datos
					}
				return cuotasList;
			}*/
		public List<Cuota> getCuotasByCreditoId(int creditoId) {
				SQLiteDatabase db = dbHelper.getReadableDatabase();
				List<Cuota> cuotasList = new ArrayList<>();

				String selection = DatabaseContract.Cuotas.CREDITO_ID + " = ?";
				String[] selectionArgs = { String.valueOf(creditoId) };
				String orderBy = DatabaseContract.Cuotas.NUMERO_CUOTA + " ASC";

				Cursor cursor = null;
				try {
						cursor = db.query(
							DatabaseContract.Cuotas.TABLE,
							null,
							selection,
							selectionArgs,
							null,
							null,
							orderBy
						);

						while (cursor.moveToNext()) {
								cuotasList.add(cursorToCuota(cursor));
							}

					} finally {
						if (cursor != null) cursor.close();
						db.close();
					}

				return cuotasList;
			}
		// =======================================================
//     ACTUALIZAR TODAS LAS CUOTAS (GUARDAR CAMBIOS)
// =======================================================
		public void actualizarCuotas(List<Cuota> listaCuotas) {

				SQLiteDatabase db = dbHelper.getWritableDatabase();
				db.beginTransaction();

				try {

						for (Cuota cuota : listaCuotas) {

								ContentValues values = new ContentValues();
								values.put(DatabaseContract.Cuotas.MONTO_CUOTA, cuota.getMontoCuota());

								// Convertir Date → String antes de guardar
								String fechaString = DB_DATE_FORMAT.format(cuota.getFechaPago());
								values.put(DatabaseContract.Cuotas.FECHA_PAGO, fechaString);

								values.put(DatabaseContract.Cuotas.PAGADA, cuota.getPagada());

								// Si está pagada → registrar timestamp
								if (cuota.getPagada() == 1) {
										values.put(DatabaseContract.Cuotas.PAGADA_TS, String.valueOf(System.currentTimeMillis()));
									}

								String where = DatabaseContract.Cuotas.ID + " = ?";
								String[] args = { String.valueOf(cuota.getId()) };

								db.update(DatabaseContract.Cuotas.TABLE, values, where, args);
							}

						db.setTransactionSuccessful();

					} finally {
						db.endTransaction();
						db.close();
					}
			}
	}
