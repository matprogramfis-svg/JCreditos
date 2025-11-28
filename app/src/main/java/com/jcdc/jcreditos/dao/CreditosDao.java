package com.jcdc.jcreditos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jcdc.jcreditos.db.DatabaseContract;
import com.jcdc.jcreditos.db.DbHelper;
import com.jcdc.jcreditos.model.Credito;
import com.jcdc.jcreditos.model.Plan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// ... otros imports ...
import android.database.Cursor;

import com.jcdc.jcreditos.db.DatabaseContract; // Necesario para los nombres de las columnas
import com.jcdc.jcreditos.model.Credito; // Necesario para el POJO Credito

public class CreditosDao {

		private DbHelper dbHelper;
		private PlanesDao planesDao; // Necesario para obtener la información del Plan

		// Formato estándar para almacenar y leer fechas en SQLite (TEXT)
		private static final String DATE_FORMAT = "yyyy-MM-dd";
		private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);

		public CreditosDao(Context context) {
				dbHelper = new DbHelper(context);
				planesDao = new PlanesDao(context); 
			}

		/**
		 * Inserta el Crédito en la BD y genera todas sus Cuotas asociadas.
		 * La operación se realiza dentro de una Transacción.
		 * @param credito Objeto Credito a insertar.
		 * @return El ID del crédito insertado o -1 en caso de error.
		 */
		public long insertCreditoAndCuotas(Credito credito) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				long creditoId = -1;

				// 1. Obtener detalles del Plan
				Plan plan = planesDao.getPlanById(credito.getPlanId());
				if (plan == null) {
						Log.e("CreditosDao", "Plan ID " + credito.getPlanId() + " no encontrado.");
						return -1;
					}

				db.beginTransaction();
				try {
						// 2. INSERTAR EL CRÉDITO
						ContentValues creditosValues = new ContentValues();
						creditosValues.put(DatabaseContract.Creditos.CLIENTE_ID, credito.getClienteId());
						creditosValues.put(DatabaseContract.Creditos.PLAN_ID, credito.getPlanId());
						creditosValues.put(DatabaseContract.Creditos.CAPITAL, credito.getCapital());
						creditosValues.put(DatabaseContract.Creditos.INTERES_PORCENTAJE, credito.getInteresPorcentaje());
						creditosValues.put(DatabaseContract.Creditos.INTERES_MONTO, credito.getInteresMonto());
						creditosValues.put(DatabaseContract.Creditos.TOTAL, credito.getTotal());
						creditosValues.put(DatabaseContract.Creditos.FECHA_INICIO, credito.getFechaInicio());
						// ESTADO y CREADO_TS se manejan por defecto

						creditoId = db.insert(DatabaseContract.Creditos.TABLE, null, creditosValues);

						if (creditoId > 0) {
								credito.setId((int) creditoId); // Asignar el ID generado

								// 3. GENERAR E INSERTAR CUOTAS
								generateCuotas(db, credito, plan);

								// 4. Marcar Transacción como exitosa
								db.setTransactionSuccessful();
							}

					} catch (ParseException e) {
						// Manejar error de formato de fecha
						Log.e("CreditosDao", "Error al parsear fecha: " + e.getMessage());
						creditoId = -1; // Forzar error
					} catch (RuntimeException e) {
						// Manejar error en la inserción de cuota
						Log.e("CreditosDao", "Error al generar o insertar cuotas: " + e.getMessage());
						creditoId = -1; // Forzar error
					} finally {
						db.endTransaction();
						db.close();
					}
				return creditoId;
			}

		/**
		 * Lógica principal para calcular fechas e insertar cuotas.
		 */
		private void generateCuotas(SQLiteDatabase db, Credito credito, Plan plan) throws ParseException {
				int totalCuotas = plan.getCuotasTotales();
				double montoPorCuota = credito.getTotal() / totalCuotas; // Amortización simple (iguales)

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateFormat.parse(credito.getFechaInicio()));

				for (int i = 1; i <= totalCuotas; i++) {

						// 1. CALCULAR FECHA DE VENCIMIENTO
						if (i > 1) {
								// Avanzar según la frecuencia del plan (Aquí asumimos un plan diario)
								// Para plan "DIARIO" y Frecuencia=1, se agrega 1 día.
								// Para plan "SEMANAL" y Frecuencia=1, se agrega 7 días.
								// Esta es una SIMPLIFICACIÓN. En la vida real, usarías switch/if
								// para manejar TIPO (DIARIO, SEMANAL, MENSUAL).

								// --- SIMPLIFICACIÓN: Siempre agregamos la Frecuencia en días ---
								calendar.add(Calendar.DAY_OF_MONTH, plan.getFrecuencia()); 
							}

						// 2. APLICAR SALTO DOMINGO
						/*if (plan.getSaltoDomingo() == 1) {
								// Si el día de pago cae en domingo, salta al lunes
								// Calendar.SUNDAY = 1
								while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
										calendar.add(Calendar.DAY_OF_MONTH, 1); // Agregar un día más
									}
							}*/

						String fechaPago = dateFormat.format(calendar.getTime());

						// 3. INSERTAR CUOTA
						ContentValues cuotasValues = new ContentValues();
						cuotasValues.put(DatabaseContract.Cuotas.CREDITO_ID, credito.getId());
						cuotasValues.put(DatabaseContract.Cuotas.NUMERO_CUOTA, i);
						cuotasValues.put(DatabaseContract.Cuotas.MONTO_CUOTA, montoPorCuota);
						cuotasValues.put(DatabaseContract.Cuotas.FECHA_PAGO, fechaPago);

						long cuotaRowId = db.insert(DatabaseContract.Cuotas.TABLE, null, cuotasValues);
						if (cuotaRowId < 0) {
								throw new RuntimeException("Fallo al insertar la cuota número: " + i);
							}
					}
			}
			// ***
		/**
		 * Obtiene un objeto Credito completo por su ID.
		 * @param id El ID del crédito a buscar.
		 * @return El objeto Credito o null si no se encuentra.
		 */
		public Credito getCreditoById(int id) {
				Credito credito = null;
				SQLiteDatabase db = dbHelper.getReadableDatabase(); // dbHelper debe estar inicializado

				String selection = DatabaseContract.Creditos.ID + " = ?";
				String[] selectionArgs = { String.valueOf(id) };

				Cursor cursor = db.query(
					DatabaseContract.Creditos.TABLE,
					null, // Todas las columnas
					selection,
					selectionArgs,
					null,
					null,
					null
				);

				if (cursor.moveToFirst()) {
						// Usa la función auxiliar para convertir la fila del Cursor a un POJO Credito
						credito = cursorToCredito(cursor);
					}

				cursor.close();
				db.close();
				return credito;
				}
			// ***
		

// ... dentro de la clase CreditosDao ...

		/**
		 * Mapea una fila de resultados de la base de datos (Cursor) a un objeto Credito POJO.
		 */
		private Credito cursorToCredito(Cursor cursor) {
				Credito credito = new Credito();

				// 1. Asignar el ID (INTEGER)
				credito.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.ID)));

				// 2. Asignar los IDs de Foránea (INTEGER)
				credito.setClienteId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.CLIENTE_ID)));
				credito.setPlanId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.PLAN_ID)));

				// 3. Asignar Campos Monetarios (REAL / DOUBLE)
				credito.setCapital(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.CAPITAL)));
				credito.setInteresPorcentaje(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.INTERES_PORCENTAJE)));
				credito.setInteresMonto(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.INTERES_MONTO)));
				credito.setTotal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.TOTAL)));

				// 4. Asignar Campos de Texto/Fecha (TEXT / STRING)
				credito.setFechaInicio(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.FECHA_INICIO)));
				credito.setCreadoTs(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.CREADO_TS)));

				// 5. Asignar Estado (INTEGER)
				credito.setEstado(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.ESTADO)));

				return credito;
			}
	}
