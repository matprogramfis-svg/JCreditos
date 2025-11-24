package com.jcdc.jcreditos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
				super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
			}

		@Override
		public void onCreate(SQLiteDatabase db) {

				// ============================
				// TABLA CLIENTES
				// ============================
				db.execSQL(
					"CREATE TABLE " + DatabaseContract.Clientes.TABLE + " (" +
					DatabaseContract.Clientes.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					DatabaseContract.Clientes.NOMBRE + " TEXT NOT NULL, " +
					DatabaseContract.Clientes.CI + " TEXT, " +
					DatabaseContract.Clientes.TELEFONO + " TEXT, " +
					DatabaseContract.Clientes.DIRECCION + " TEXT, " +
					DatabaseContract.Clientes.GARANTIA + " TEXT, " +
					DatabaseContract.Clientes.CREADO_TS + " DATETIME DEFAULT (datetime('now','localtime')), " +
					DatabaseContract.Clientes.ESTADO + " INTEGER DEFAULT 1" +
					");"
				);

				// ============================
				// TABLA ESTADOS
				// ============================
				db.execSQL(
					"CREATE TABLE " + DatabaseContract.Estados.TABLE + " (" +
					DatabaseContract.Estados.ID + " INTEGER PRIMARY KEY, " +
					DatabaseContract.Estados.DESCRIPCION + " TEXT NOT NULL" +
					");"
				);

				// Insertar estados iniciales
				db.execSQL("INSERT INTO " + DatabaseContract.Estados.TABLE + " VALUES (1,'activo');");
				db.execSQL("INSERT INTO " + DatabaseContract.Estados.TABLE + " VALUES (2,'vencido');");
				db.execSQL("INSERT INTO " + DatabaseContract.Estados.TABLE + " VALUES (3,'archivo');");

				// ============================
				// TABLA PLANES
				// ============================
				db.execSQL(
					"CREATE TABLE " + DatabaseContract.Planes.TABLE + " (" +
					DatabaseContract.Planes.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					DatabaseContract.Planes.NOMBRE + " TEXT NOT NULL, " +
					DatabaseContract.Planes.DESCRIPCION + " TEXT, " +
					DatabaseContract.Planes.TIPO + " TEXT NOT NULL, " +
					DatabaseContract.Planes.FRECUENCIA + " INTEGER NOT NULL, " +
					DatabaseContract.Planes.CUOTAS_TOTALES + " INTEGER NOT NULL, " +
					DatabaseContract.Planes.SALTO_DOMINGO + " INTEGER DEFAULT 0, " +
					DatabaseContract.Planes.METODO_AMORTIZACION + " TEXT NOT NULL, " +
					DatabaseContract.Planes.CREADO_TS + " DATETIME DEFAULT (datetime('now','localtime'))" +
					");"
				);

				// ============================
				// TABLA CREDITOS
				// ============================
				db.execSQL(
					"CREATE TABLE " + DatabaseContract.Creditos.TABLE + " (" +
					DatabaseContract.Creditos.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					DatabaseContract.Creditos.CLIENTE_ID + " INTEGER NOT NULL, " +
					DatabaseContract.Creditos.PLAN_ID + " INTEGER NOT NULL, " +
					DatabaseContract.Creditos.CAPITAL + " REAL NOT NULL, " +
					DatabaseContract.Creditos.INTERES_PORCENTAJE + " REAL NOT NULL, " +
					DatabaseContract.Creditos.INTERES_MONTO + " REAL NOT NULL, " +
					DatabaseContract.Creditos.TOTAL + " REAL NOT NULL, " +
					DatabaseContract.Creditos.FECHA_INICIO + " DATE NOT NULL, " +
					DatabaseContract.Creditos.ESTADO + " INTEGER DEFAULT 1, " +
					DatabaseContract.Creditos.CREADO_TS + " DATETIME DEFAULT (datetime('now','localtime')), " +

					// relaciones
					"FOREIGN KEY(" + DatabaseContract.Creditos.CLIENTE_ID + ") REFERENCES " +
					DatabaseContract.Clientes.TABLE + "(" + DatabaseContract.Clientes.ID + "), " +
					"FOREIGN KEY(" + DatabaseContract.Creditos.PLAN_ID + ") REFERENCES " +
					DatabaseContract.Planes.TABLE + "(" + DatabaseContract.Planes.ID + ")" +
					");"
				);

				// ============================
				// TABLA CUOTAS
				// ============================
				db.execSQL(
					"CREATE TABLE " + DatabaseContract.Cuotas.TABLE + " (" +
					DatabaseContract.Cuotas.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					DatabaseContract.Cuotas.CREDITO_ID + " INTEGER NOT NULL, " +
					DatabaseContract.Cuotas.NUMERO_CUOTA + " INTEGER NOT NULL, " +
					DatabaseContract.Cuotas.MONTO_CUOTA + " REAL NOT NULL, " +
					DatabaseContract.Cuotas.FECHA_PAGO + " DATE NOT NULL, " +
					DatabaseContract.Cuotas.PAGADA + " INTEGER DEFAULT 0, " +
					DatabaseContract.Cuotas.PAGADA_TS + " DATETIME, " +
					"FOREIGN KEY(" + DatabaseContract.Cuotas.CREDITO_ID + ") REFERENCES " +
					DatabaseContract.Creditos.TABLE + "(" + DatabaseContract.Creditos.ID + ")" +
					");"
				);

				// ============================
				// TRIGGER: Marcar cuota como pagada
				// ============================
				db.execSQL(
					"CREATE TRIGGER IF NOT EXISTS trg_cuota_pagada " +
					"AFTER UPDATE OF " + DatabaseContract.Cuotas.PAGADA + " ON " + DatabaseContract.Cuotas.TABLE + " " +
					"FOR EACH ROW " +
					"WHEN NEW." + DatabaseContract.Cuotas.PAGADA + " = 1 " +
					"BEGIN " +
					"UPDATE " + DatabaseContract.Cuotas.TABLE +
					" SET " + DatabaseContract.Cuotas.PAGADA_TS + " = datetime('now','localtime')" +
					" WHERE id = NEW.id; " +
					"END;"
				);
			}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				// Futuras migraciones aquí
			}
	}

