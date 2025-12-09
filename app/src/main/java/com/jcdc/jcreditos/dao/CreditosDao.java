package com.jcdc.jcreditos.dao;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.util.*;
import com.jcdc.jcreditos.db.*;
import com.jcdc.jcreditos.model.*;
import java.text.*;
import java.util.*; // Necesario para el POJO Credito

public class CreditosDao {

		private DbHelper dbHelper;
		private PlanesDao planesDao; // Necesario para obtener la información del Plan
		
		// 💡 NUEVA VARIABLE: Instancia de CuotasDao
		private CuotasDao cuotasDao;
		// 💡 FORMATO ESTÁNDAR PARA SQLITE (yyyy-MM-dd)
		private final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US); 
		
		// Formato estándar para almacenar y leer fechas en SQLite (TEXT)
		//private static final String DATE_FORMAT = "dd/MM/yyyy";
		//private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DB_DATE_FORMAT, Locale.US);
		
		// Nombres de columna auxiliares para la unión (para evitar conflictos)
		private static final String COL_NOMBRE_CLIENTE_ALIAS = "nombre_cliente_alias";
		private static final String COL_NOMBRE_PLAN_ALIAS = "nombre_plan_alias";

		public CreditosDao(Context context) {
				dbHelper = new DbHelper(context);
				planesDao = new PlanesDao(context); 
				// 💡 INICIALIZACIÓN: Inicializar CuotasDao
				cuotasDao = new CuotasDao(context);
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
						//creditosValues.put(DatabaseContract.Creditos.FECHA_INICIO, credito.getFechaInicio());
						// ✅ SOLUCIÓN: Conversión de Date a String

// 1. Obtener el objeto Date
						Date fechaInicioObjeto = credito.getFechaInicio();

// 2. Convertir el objeto Date a String usando el formato de la DB
						String fechaInicioString = DB_DATE_FORMAT.format(fechaInicioObjeto);

// 3. Poner el String en ContentValues
						creditosValues.put(DatabaseContract.Creditos.FECHA_INICIO, fechaInicioString);
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
				calendar.setTime(credito.getFechaInicio());
				
				calendar.add(Calendar.DAY_OF_MONTH, plan.getFrecuencia());

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

						Date fechaPago = calendar.getTime();

						
						String logFecha = DB_DATE_FORMAT.format(fechaPago);
						Log.d("GeneracionCuotas", 
							  "Credito ID: " + credito.getId() + 
							  " | Cuota #" + i + 
							  " | Monto: " + String.format("%.2f", montoPorCuota) +
							  " | Fecha Vencimiento (DB Format): " + logFecha);
						// 3. INSERTAR CUOTA
						Cuota cuota = new Cuota();
						cuota.setCreditoId(credito.getId());
						cuota.setNumeroCuota(i);
						cuota.setMontoCuota(montoPorCuota);
						cuota.setFechaPago(fechaPago);
						cuota.setPagada(0); // Nueva cuota, no pagada

						// LLAMADA AL DAO DE CUOTAS
						long cuotaRowId = cuotasDao.insertCuota(db, cuota); // Usar el método de CuotasDao
						
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
				//credito.setFechaInicio(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.FECHA_INICIO)));
				// 1. Obtener la String de la fecha del Cursor
				String fechaString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.FECHA_INICIO));

				try {
						// 2. Usar el DB_DATE_FORMAT (yyyy-MM-dd) para convertir la String a un objeto Date
						Date fechaInicioObjeto = DB_DATE_FORMAT.parse(fechaString);

						// 3. Asignar el objeto Date al POJO
						credito.setFechaInicio(fechaInicioObjeto);

					} catch (ParseException e) {
						// Manejar el error si la fecha en la DB no tiene el formato esperado
						e.printStackTrace();
						// Opcional: Asignar null o una fecha segura en caso de fallo
						credito.setFechaInicio(null); 
					}
				credito.setCreadoTs(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.CREADO_TS)));

				// 5. Asignar Estado (INTEGER)
				credito.setEstado(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Creditos.ESTADO)));

				return credito;
			}
		// ***
		/**
		 * Obtiene la lista de todos los Créditos registrados en la base de datos.
		 * @return Una lista de objetos Credito.
		 */
		/**
		 * Obtiene todos los Créditos junto con el nombre del Cliente y el nombre del Plan.
		 * @return Una lista de objetos Credito.
		 */
		public List<Credito> getAllCreditos() {
				List<Credito> creditosList = new ArrayList<>();
				SQLiteDatabase db = dbHelper.getReadableDatabase();
				Cursor cursor = null;

				try {
						String CRED = DatabaseContract.Creditos.TABLE;
						String CLI = DatabaseContract.Clientes.TABLE;
						String PLAN = DatabaseContract.Planes.TABLE;

						// Usamos nombres de columnas con alias para distinguir CLIENTE.NOMBRE y PLANES.NOMBRE
						String selectQuery = 
							"SELECT " +
							"T1.*, " + // T1 es la tabla de CREDITOS (trae todas las columnas originales)
							"T2." + DatabaseContract.Clientes.NOMBRE + " AS " + COL_NOMBRE_CLIENTE_ALIAS + ", " + 
							"T3." + DatabaseContract.Planes.NOMBRE + " AS " + COL_NOMBRE_PLAN_ALIAS + 
							" FROM " + CRED + " T1 " +
							// INNER JOIN para Cliente
							"INNER JOIN " + CLI + " T2 ON T1." + DatabaseContract.Creditos.CLIENTE_ID + " = T2." + DatabaseContract.Clientes.ID + " " +
							// INNER JOIN para Plan
							"INNER JOIN " + PLAN + " T3 ON T1." + DatabaseContract.Creditos.PLAN_ID + " = T3." + DatabaseContract.Planes.ID + " " +
							"ORDER BY T1." + DatabaseContract.Creditos.CREADO_TS + " DESC";

						cursor = db.rawQuery(selectQuery, null);

						if (cursor.moveToFirst()) {
								do {
										Credito credito = cursorToCredito(cursor);

										// Asignar los campos extra del JOIN
										credito.setNombreCliente(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_CLIENTE_ALIAS)));
										credito.setNombrePlan(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_PLAN_ALIAS)));

										creditosList.add(credito);
									} while (cursor.moveToNext());
							}

					} catch (Exception e) {
						Log.e("CreditosDao", "Error en getAllCreditos con JOIN: " + e.getMessage());
					} finally {
						if (cursor != null) {
								cursor.close();
							}
						db.close();
					}

				return creditosList;
			}
		/**
		 * Actualiza la información principal de un crédito existente.
		 * Nota: La actualización de un crédito no regenera las cuotas automáticamente.
		 * @param credito Objeto Credito con los nuevos datos.
		 * @return Número de filas afectadas (1 si fue exitoso, 0 si no).
		 */
		public int updateCredito(Credito credito) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				ContentValues values = new ContentValues();

				// Asignar los valores actualizables (excluyendo CREADO_TS, ID)
				values.put(DatabaseContract.Creditos.CLIENTE_ID, credito.getClienteId());
				values.put(DatabaseContract.Creditos.PLAN_ID, credito.getPlanId());
				values.put(DatabaseContract.Creditos.CAPITAL, credito.getCapital());
				values.put(DatabaseContract.Creditos.INTERES_PORCENTAJE, credito.getInteresPorcentaje());
				values.put(DatabaseContract.Creditos.INTERES_MONTO, credito.getInteresMonto());
				values.put(DatabaseContract.Creditos.TOTAL, credito.getTotal());
				//values.put(DatabaseContract.Creditos.FECHA_INICIO, credito.getFechaInicio());
				// 1. Obtener el objeto Date del POJO Credito
				Date fechaInicioObjeto = credito.getFechaInicio();

// 2. CONVERSIÓN CRUCIAL: Convertir el objeto Date a String (yyyy-MM-dd)
				String fechaInicioString = DB_DATE_FORMAT.format(fechaInicioObjeto);

// 3. Pasar la String a ContentValues (¡Ya no da error!)
				values.put(DatabaseContract.Creditos.FECHA_INICIO, fechaInicioString);
				values.put(DatabaseContract.Creditos.ESTADO, credito.getEstado());

				// Cláusula WHERE para asegurar que solo se actualice el registro correcto
				String selection = DatabaseContract.Creditos.ID + " = ?";
				String[] selectionArgs = { String.valueOf(credito.getId()) };

				// Ejecutar la actualización
				int rowsAffected = db.update(
					DatabaseContract.Creditos.TABLE,
					values,
					selection,
					selectionArgs
				);

				db.close();
				return rowsAffected;
			}
			// ***
		// --- Obtener créditos por ID de cliente ---
		/*public List<Credito> getCreditosByCliente(int clienteId) {
				List<Credito> lista = new ArrayList<>();
				SQLiteDatabase db = dbHelper.getReadableDatabase();
				Cursor cursor = null;

				String selection = DatabaseContract.Creditos.CLIENTE_ID + " = ? AND " +
					DatabaseContract.Creditos.ESTADO + " = ?";
				String[] selectionArgs = { String.valueOf(clienteId), "1" }; // solo créditos activos

				try {
						cursor = db.query(
							DatabaseContract.Creditos.TABLE,
							null,
							selection,
							selectionArgs,
							null,
							null,
							DatabaseContract.Creditos.ID + " DESC"
						);

						if (cursor.moveToFirst()) {
								do {
										lista.add(cursorToCredito(cursor));
									} while (cursor.moveToNext());
							}

					} catch (Exception e) {
						Log.e("CreditosDao", "Error getCreditosByCliente: " + e.getMessage());
					} finally {
						if (cursor != null) cursor.close();
						db.close();
					}

				return lista;
			}*/
			// ***
		// package com.jcdc.jcreditos.dao;

// ... (resto del código)

// --- Obtener créditos por ID de cliente ---
		public List<Credito> getCreditosByCliente(int clienteId) {
				List<Credito> lista = new ArrayList<>();
				SQLiteDatabase db = dbHelper.getReadableDatabase();
				Cursor cursor = null;

				try {
						String CRED = DatabaseContract.Creditos.TABLE;
						String CLI = DatabaseContract.Clientes.TABLE;
						String PLAN = DatabaseContract.Planes.TABLE;

						// Cláusula WHERE: Filtrar por CLIENTE_ID y ESTADO = 1 (vigente)
						String whereClause = "T1." + DatabaseContract.Creditos.CLIENTE_ID + " = ? AND " +
							"T1." + DatabaseContract.Creditos.ESTADO + " = ?";
						String[] selectionArgs = { String.valueOf(clienteId), "1" };

						// CONSULTA RAW CON INNER JOIN (Copiada y adaptada de getAllCreditos)
						String selectQuery =
							"SELECT " +
							"T1.*, " + // T1 es la tabla de CREDITOS (trae todas las columnas originales)
							"T2." + DatabaseContract.Clientes.NOMBRE + " AS " + COL_NOMBRE_CLIENTE_ALIAS + ", " +
							"T3." + DatabaseContract.Planes.NOMBRE + " AS " + COL_NOMBRE_PLAN_ALIAS +
							" FROM " + CRED + " T1 " +
							// INNER JOIN para Cliente
							"INNER JOIN " + CLI + " T2 ON T1." + DatabaseContract.Creditos.CLIENTE_ID + " = T2." + DatabaseContract.Clientes.ID + " " +
							// INNER JOIN para Plan
							"INNER JOIN " + PLAN + " T3 ON T1." + DatabaseContract.Creditos.PLAN_ID + " = T3." + DatabaseContract.Planes.ID + " " +
							"WHERE " + whereClause + " " + // <-- APLICAMOS LA CLÁUSULA WHERE
							"ORDER BY T1." + DatabaseContract.Creditos.ID + " DESC";

						cursor = db.rawQuery(selectQuery, selectionArgs); // <-- Pasamos los argumentos

						if (cursor.moveToFirst()) {
								do {
										Credito credito = cursorToCredito(cursor);

										// 🔥 ASIGNAR LOS CAMPOS EXTRA DEL JOIN (Asegurar que existen)
										credito.setNombreCliente(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_CLIENTE_ALIAS)));
										credito.setNombrePlan(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_PLAN_ALIAS)));

										lista.add(credito);
									} while (cursor.moveToNext());
							}

					} catch (Exception e) {
						Log.e("CreditosDao", "Error getCreditosByCliente: " + e.getMessage());
					} finally {
						if (cursor != null) cursor.close();
						db.close();
					}

				return lista;
			}
		// ***
		public double calcularSaldoRestante(long creditoId) {
				// 1. Consulta SQL para sumar los montos de las cuotas PENDIENTES (pagada = 0)
				String sql = "SELECT SUM(monto_cuota) FROM " + DatabaseContract.Cuotas.TABLE +
					" WHERE credito_id = ? AND pagada = 0";

				SQLiteDatabase db = dbHelper.getReadableDatabase();
				Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(creditoId)});

				double saldo = 0.0;

				if (cursor.moveToFirst()) {
						// La columna 0 es el resultado del SUM
						saldo = cursor.getDouble(0); 
					}

				cursor.close();
				db.close();

				return saldo;
			}
	}
