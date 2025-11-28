package com.jcdc.jcreditos.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jcdc.jcreditos.R; // Asegúrate de que R exista con tus IDs
import com.jcdc.jcreditos.dao.ClientesDao;
import com.jcdc.jcreditos.model.Cliente;

public class ClienteActivity extends Activity implements View.OnClickListener {

		private EditText etNombre, etCi, etTelefono, etDireccion, etGarantia;
		private Button btnGuardar;
		private ClientesDao clientesDao;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				// --- 1. Asigna tu layout XML aquí ---
				// Debes crear un archivo XML en res/layout/activity_cliente.xml
				setContentView(R.layout.activity_cliente); 

				clientesDao = new ClientesDao(this);

				// --- 2. Inicializar Vistas (EditTexts y Botones) ---
				etNombre = (EditText) findViewById(R.id.et_nombre);
				etCi = (EditText) findViewById(R.id.et_ci);
				etTelefono = (EditText) findViewById(R.id.et_telefono);
				etDireccion = (EditText) findViewById(R.id.et_direccion);
				etGarantia = (EditText) findViewById(R.id.et_garantia);

				btnGuardar = (Button) findViewById(R.id.btn_guardar_cliente);
				btnGuardar.setOnClickListener(this);

				// Opcional: Podrías cargar una lista de clientes aquí si fuera necesario
			}

		@Override
		public void onClick(View v) {
				if (v.getId() == R.id.btn_guardar_cliente) {
						guardarCliente();
					}
			}

		private void guardarCliente() {
				String nombre = etNombre.getText().toString().trim();
				String ci = etCi.getText().toString().trim();
				String telefono = etTelefono.getText().toString().trim();
				String direccion = etDireccion.getText().toString().trim();
				String garantia = etGarantia.getText().toString().trim();

				if (nombre.isEmpty() || ci.isEmpty()) {
						Toast.makeText(this, "Nombre y CI son campos obligatorios.", Toast.LENGTH_SHORT).show();
						return;
					}

				// Crear el objeto Cliente
				Cliente nuevoCliente = new Cliente();
				nuevoCliente.setNombre(nombre);
				nuevoCliente.setCi(ci);
				nuevoCliente.setTelefono(telefono);
				nuevoCliente.setDireccion(direccion);
				nuevoCliente.setGarantia(garantia);
				// El estado por defecto en la BD es 1 (Activo), pero lo incluimos por buenas prácticas
				nuevoCliente.setEstado(1); 

				// Insertar en la Base de Datos
				long resultado = clientesDao.insertCliente(nuevoCliente);

				if (resultado > 0) {
						Toast.makeText(this, "Cliente guardado con ID: " + resultado, Toast.LENGTH_LONG).show();
						limpiarCampos();
						// Esto le dice a la Activity que la llamó (ListaClientesActivity) que todo fue OK
						setResult(RESULT_OK);
						finish(); // Cierra esta activity y regresa a la lista
					} else {
						Toast.makeText(this, "Error al guardar el cliente.", Toast.LENGTH_SHORT).show();
					}
			}

		private void limpiarCampos() {
				etNombre.setText("");
				etCi.setText("");
				etTelefono.setText("");
				etDireccion.setText("");
				etGarantia.setText("");
			}
	}
