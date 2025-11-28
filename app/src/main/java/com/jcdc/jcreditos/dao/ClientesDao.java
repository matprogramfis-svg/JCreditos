package com.jcdc.jcreditos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jcdc.jcreditos.db.DatabaseContract;
import com.jcdc.jcreditos.db.DbHelper;
import com.jcdc.jcreditos.model.Cliente; // Importamos el POJO

import java.util.ArrayList;
import java.util.List;

public class ClientesDao {

		private DbHelper dbHelper;

		public ClientesDao(Context context) {
				dbHelper = new DbHelper(context);
			}

		// --- Auxiliar: Mapea Cursor a Cliente POJO ---
		private Cliente cursorToCliente(Cursor cursor) {
				Cliente cliente = new Cliente();
				cliente.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Clientes.ID)));
				cliente.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Clientes.NOMBRE)));
				cliente.setCi(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Clientes.CI)));
				cliente.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Clientes.TELEFONO)));
				cliente.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Clientes.DIRECCION)));
				cliente.setGarantia(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Clientes.GARANTIA)));
				cliente.setCreadoTs(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Clientes.CREADO_TS)));
				cliente.setEstado(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Clientes.ESTADO)));
				return cliente;
			}

		// --- Insertar Cliente (C) ---
		public long insertCliente(Cliente cliente) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(DatabaseContract.Clientes.NOMBRE, cliente.getNombre());
				values.put(DatabaseContract.Clientes.CI, cliente.getCi());
				values.put(DatabaseContract.Clientes.TELEFONO, cliente.getTelefono());
				values.put(DatabaseContract.Clientes.DIRECCION, cliente.getDireccion());
				values.put(DatabaseContract.Clientes.GARANTIA, cliente.getGarantia());
				values.put(DatabaseContract.Clientes.ESTADO, cliente.getEstado());

				long newRowId = db.insert(DatabaseContract.Clientes.TABLE, null, values);
				db.close();
				return newRowId;
			}

		// --- Obtener por ID (R) ---
		public Cliente getClienteById(int id) {
				Cliente cliente = null;
				SQLiteDatabase db = dbHelper.getReadableDatabase();

				String selection = DatabaseContract.Clientes.ID + " = ?";
				String[] selectionArgs = { String.valueOf(id) };

				Cursor cursor = db.query(DatabaseContract.Clientes.TABLE, null, selection, selectionArgs, null, null, null);

				if (cursor.moveToFirst()) {
						cliente = cursorToCliente(cursor);
					}

				cursor.close();
				db.close();
				return cliente;
			}

		// --- Obtener Todos (R) - Solo ACTIVOS ---
		public List<Cliente> getAllClientes() {
				List<Cliente> lista = new ArrayList<>();
				SQLiteDatabase db = dbHelper.getReadableDatabase();

				String selection = DatabaseContract.Clientes.ESTADO + " = ?";
				String[] selectionArgs = { String.valueOf(1) };
				String sortOrder = DatabaseContract.Clientes.NOMBRE + " ASC";

				Cursor cursor = db.query(DatabaseContract.Clientes.TABLE, null, selection, selectionArgs, null, null, sortOrder);

				if (cursor.moveToFirst()) {
						do {
								lista.add(cursorToCliente(cursor));
							} while (cursor.moveToNext());
					}

				cursor.close();
				db.close();
				return lista;
			}

		// --- Actualizar Cliente (U) ---
		public int updateCliente(Cliente cliente) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(DatabaseContract.Clientes.NOMBRE, cliente.getNombre());
				values.put(DatabaseContract.Clientes.CI, cliente.getCi());
				values.put(DatabaseContract.Clientes.TELEFONO, cliente.getTelefono());
				values.put(DatabaseContract.Clientes.DIRECCION, cliente.getDireccion());
				values.put(DatabaseContract.Clientes.GARANTIA, cliente.getGarantia());
				values.put(DatabaseContract.Clientes.ESTADO, cliente.getEstado());

				String selection = DatabaseContract.Clientes.ID + " = ?";
				String[] selectionArgs = { String.valueOf(cliente.getId()) };

				int count = db.update(
					DatabaseContract.Clientes.TABLE,
					values,
					selection,
					selectionArgs
				);
				db.close();
				return count; 
			}

		// --- Desactivar (Eliminar Lógicamente) Cliente (D) ---
		public int desactivarCliente(int id) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(DatabaseContract.Clientes.ESTADO, 0); // 0 = Desactivado

				String selection = DatabaseContract.Clientes.ID + " = ?";
				String[] selectionArgs = { String.valueOf(id) };

				int count = db.update(
					DatabaseContract.Clientes.TABLE,
					values,
					selection,
					selectionArgs
				);
				db.close();
				return count; 
			}
	}
