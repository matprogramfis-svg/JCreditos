package com.jcdc.jcreditos.dao;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import com.jcdc.jcreditos.db.*;
import com.jcdc.jcreditos.model.*;
import java.util.*;

public class PlanesDao {

		private final DbHelper dbHelper; // Ya lo tienes
		private final Context context;    // <<< NECESITAS ESTA VARIABLE

		public PlanesDao(Context context) {
				this.context = context; // <<< GUARDA EL CONTEXTO
				this.dbHelper = new DbHelper(context);
			}

		// --- Auxiliar: Mapea Cursor a Plan POJO ---
		private Plan cursorToPlan(Cursor cursor) {
				Plan plan = new Plan();
				plan.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Planes.ID)));
				plan.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Planes.NOMBRE)));
				plan.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Planes.DESCRIPCION)));
				plan.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Planes.TIPO)));
				plan.setFrecuencia(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Planes.FRECUENCIA)));
				plan.setCuotasTotales(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Planes.CUOTAS_TOTALES)));
				plan.setSaltoDomingo(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Planes.SALTO_DOMINGO)));
				plan.setMetodoAmortizacion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Planes.METODO_AMORTIZACION)));
				plan.setCreadoTs(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Planes.CREADO_TS)));
				return plan;
			}

		// --- Obtener Plan por ID (Retorna POJO) ---
		public Plan getPlanById(int id) {
				Plan plan = null;
				SQLiteDatabase db = dbHelper.getReadableDatabase();

				String selection = DatabaseContract.Planes.ID + " = ?";
				String[] selectionArgs = { String.valueOf(id) };

				Cursor cursor = db.query(DatabaseContract.Planes.TABLE, null, selection, selectionArgs, null, null, null);

				if (cursor.moveToFirst()) {
						plan = cursorToPlan(cursor);
					}

				cursor.close();
				db.close();
				return plan;
			}

		// --- Obtener todos los Planes (Retorna Lista de POJOs) ---
		public List<Plan> getAllPlanes() {
				List<Plan> lista = new ArrayList<>();
				SQLiteDatabase db = dbHelper.getReadableDatabase();

				String sortOrder = DatabaseContract.Planes.NOMBRE + " ASC";

				Cursor cursor = db.query(DatabaseContract.Planes.TABLE, null, null, null, null, null, sortOrder);

				if (cursor.moveToFirst()) {
						do {
								lista.add(cursorToPlan(cursor));
							} while (cursor.moveToNext());
					}

				cursor.close();
				db.close();
				return lista;
			}
		// ***
		// --- Insertar Plan ---
		public long insertPlan(Plan plan) {
				SQLiteDatabase db = new DbHelper(context).getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(DatabaseContract.Planes.NOMBRE, plan.getNombre());
				values.put(DatabaseContract.Planes.DESCRIPCION, plan.getDescripcion());
				values.put(DatabaseContract.Planes.TIPO, plan.getTipo());
				values.put(DatabaseContract.Planes.FRECUENCIA, plan.getFrecuencia());
				values.put(DatabaseContract.Planes.CUOTAS_TOTALES, plan.getCuotasTotales());
				values.put(DatabaseContract.Planes.SALTO_DOMINGO, plan.getSaltoDomingo());
				values.put(DatabaseContract.Planes.METODO_AMORTIZACION, plan.getMetodoAmortizacion());

				long newRowId = db.insert(DatabaseContract.Planes.TABLE, null, values);
				db.close();
				return newRowId;
			}
		// ***
		// --- Actualizar Plan ---
		public int updatePlan(Plan plan) {
				SQLiteDatabase db = new DbHelper(context).getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(DatabaseContract.Planes.NOMBRE, plan.getNombre());
				values.put(DatabaseContract.Planes.DESCRIPCION, plan.getDescripcion());
				values.put(DatabaseContract.Planes.TIPO, plan.getTipo());
				values.put(DatabaseContract.Planes.FRECUENCIA, plan.getFrecuencia());
				values.put(DatabaseContract.Planes.CUOTAS_TOTALES, plan.getCuotasTotales());
				values.put(DatabaseContract.Planes.SALTO_DOMINGO, plan.getSaltoDomingo());
				values.put(DatabaseContract.Planes.METODO_AMORTIZACION, plan.getMetodoAmortizacion());

				String selection = DatabaseContract.Planes.ID + " = ?";
				String[] selectionArgs = { String.valueOf(plan.getId()) };

				int count = db.update(DatabaseContract.Planes.TABLE, values, selection, selectionArgs);
				db.close();
				return count;
				}
			
			
	}
