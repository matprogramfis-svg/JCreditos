package com.jcdc.jcreditos.dao;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jcdc.jcreditos.db.DatabaseContract;
import com.jcdc.jcreditos.db.DbHelper;
import com.jcdc.jcreditos.model.Plan;

import java.util.ArrayList;
import java.util.List;

public class PlanesDao {

		private DbHelper dbHelper;

		public PlanesDao(Context ctx) {
				dbHelper = new DbHelper(ctx);
			}

		// ============================
		// INSERTAR
		// ============================
		public long insertPlan(Plan plan) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(DatabaseContract.Planes.NOMBRE, plan.getNombre());
				values.put(DatabaseContract.Planes.TIPO, plan.getTipo());
				values.put(DatabaseContract.Planes.FRECUENCIA, plan.getFrecuencia());
				values.put(DatabaseContract.Planes.CUOTAS_TOTALES, plan.getCuotasTotales());
				values.put(DatabaseContract.Planes.NUMERO_MESES, plan.getNumeroMeses());
				values.put(DatabaseContract.Planes.SALTO_DOMINGO, plan.getSaltoDomingo());

				return db.insert(DatabaseContract.Planes.TABLE, null, values);
			}

		// ============================
		// ACTUALIZAR
		// ============================
		public int updatePlan(Plan plan) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(DatabaseContract.Planes.NOMBRE, plan.getNombre());
				values.put(DatabaseContract.Planes.TIPO, plan.getTipo());
				values.put(DatabaseContract.Planes.FRECUENCIA, plan.getFrecuencia());
				values.put(DatabaseContract.Planes.CUOTAS_TOTALES, plan.getCuotasTotales());
				values.put(DatabaseContract.Planes.NUMERO_MESES, plan.getNumeroMeses());
				values.put(DatabaseContract.Planes.SALTO_DOMINGO, plan.getSaltoDomingo());

				String where = DatabaseContract.Planes.ID + "=?";
				String[] args = { String.valueOf(plan.getId()) };

				return db.update(DatabaseContract.Planes.TABLE, values, where, args);
			}

		// ============================
		// ELIMINAR
		// ============================
		public int deletePlan(int id) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				return db.delete(DatabaseContract.Planes.TABLE,
								 DatabaseContract.Planes.ID + "=?",
								 new String[]{String.valueOf(id)});
			}

		// ============================
		// OBTENER POR ID
		// ============================
		public Plan getPlanById(int id) {
				SQLiteDatabase db = dbHelper.getReadableDatabase();

				Cursor c = db.query(
					DatabaseContract.Planes.TABLE,
					null,
					DatabaseContract.Planes.ID + "=?",
					new String[]{String.valueOf(id)},
					null, null, null
				);

				if (c != null && c.moveToFirst()) {
						Plan p = cursorToPlan(c);
						c.close();
						return p;
					}
				return null;
			}

		// ============================
		// LISTAR TODOS
		// ============================
		public List<Plan> getAllPlanes() {
				List<Plan> lista = new ArrayList<>();

				SQLiteDatabase db = dbHelper.getReadableDatabase();
				Cursor c = db.query(DatabaseContract.Planes.TABLE,
									null, null, null, null, null,
									DatabaseContract.Planes.NOMBRE + " ASC");

				if (c != null && c.moveToFirst()) {
						do {
								lista.add(cursorToPlan(c));
							} while (c.moveToNext());
						c.close();
					}

				return lista;
			}

		// ============================
		// MAPEAR CURSOR → OBJETO
		// ============================
		private Plan cursorToPlan(Cursor c) {
				Plan p = new Plan();

				p.setId(c.getInt(c.getColumnIndex(DatabaseContract.Planes.ID)));
				p.setNombre(c.getString(c.getColumnIndex(DatabaseContract.Planes.NOMBRE)));
				//p.setInteres(c.getInt(c.getColumnIndex(DatabaseContract.Planes.INTERES)));
				p.setTipo(c.getString(c.getColumnIndex(DatabaseContract.Planes.TIPO)));
				p.setFrecuencia(c.getInt(c.getColumnIndex(DatabaseContract.Planes.FRECUENCIA)));
				p.setCuotasTotales(c.getInt(c.getColumnIndex(DatabaseContract.Planes.CUOTAS_TOTALES)));
				p.setNumeroMeses(c.getInt(c.getColumnIndex(DatabaseContract.Planes.NUMERO_MESES)));
				p.setSaltoDomingo(c.getInt(c.getColumnIndex(DatabaseContract.Planes.SALTO_DOMINGO)));

				return p;
			}
	}
