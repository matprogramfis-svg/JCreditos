package com.jcdc.jcreditos.ui;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.jcdc.jcreditos.*;
import com.jcdc.jcreditos.dao.*;
import com.jcdc.jcreditos.model.*;

public class ClienteDetalleActivity extends Activity {

		private ClientesDao clientesDao;
		private int clienteId = -1;

		private EditText etNombre, etCi, etTelefono, etDireccion, etGarantia;
		//private Spinner spTipo;
		//private CheckBox cbSalto;
		private Button btnGuardar, btnEliminar;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_cliente_detalle);

				clientesDao = new ClientesDao(this);

				etNombre = findViewById(R.id.et_cliente_nombre);
				//etInteres = findViewById(R.id.et_plan_interes);
				etCi = findViewById(R.id.et_cliente_ci);
				etTelefono = findViewById(R.id.et_cliente_telefono);
				etDireccion = findViewById(R.id.et_cliente_direccion);
				etGarantia = findViewById(R.id.et_cliente_garantia);
				//cbSalto = findViewById(R.id.chk_plan_salto_domingo);
				btnGuardar = findViewById(R.id.btn_cliente_guardar);
				btnEliminar = findViewById(R.id.btn_cliente_eliminar);

				// Spinner tipos (opcional, sirve como etiqueta / ayuda al usuario)
				//String[] tipos = {"DIARIO", "SEMANAL", "QUINCENAL", "MENSUAL"};
				/*ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
																  android.R.layout.simple_spinner_item, tipos);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spTipo.setAdapter(adapter);*/

				// Si se abrió para editar
				clienteId = getIntent().getIntExtra("CLIENTE_ID", -1);
				if (clienteId != -1) {
						loadClienteData(clienteId);
						btnEliminar.setVisibility(View.VISIBLE);
						btnGuardar.setText("ACTUALIZAR CLIENTE");
					} else {
						btnEliminar.setVisibility(View.GONE);
					}

				btnGuardar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) { saveCliente(); }
						});

				btnEliminar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									if (clienteId != -1) {
											new AlertDialog.Builder(ClienteDetalleActivity.this)
												.setTitle("Eliminar Cliente")
												.setMessage("¿Seguro que deseas eliminar este cliente?")
												.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog, int which) {
																clientesDao.desactivarCliente(clienteId);
																setResult(RESULT_OK);
																finish();
															}
													})
												.setNegativeButton("No", null)
												.show();
										}
								}
						});
			}

		private void loadClienteData(int id) {
				Cliente c = clientesDao.getClienteById(id);
				if (c == null) return;

				etNombre.setText(c.getNombre());
				//etInteres.setText(String.valueOf(p.getInteres())); // entero
				etCi.setText(String.valueOf(c.getCi()));
				etTelefono.setText(String.valueOf(c.getTelefono()));
				etDireccion.setText(String.valueOf(c.getDireccion()));
				etGarantia.setText(String.valueOf(c.getGarantia()));

				// Seleccionar tipo en spinner si coincide
				/*for (int i = 0; i < spTipo.getCount(); i++) {
						if (spTipo.getItemAtPosition(i).toString().equalsIgnoreCase(p.getTipo())) {
								spTipo.setSelection(i);
								break;
							}
					}*/
			}

		// COMPLETAMENTE SIN INTERES
		private void saveCliente() {

				if (etNombre.getText().toString().trim().isEmpty() ||
					etCi.getText().toString().trim().isEmpty() ||
					etTelefono.getText().toString().trim().isEmpty() ||
					etDireccion.getText().toString().trim().isEmpty() ||
					etGarantia.getText().toString().trim().isEmpty()) {

						Toast.makeText(this, "Faltan campos obligatorios.", Toast.LENGTH_SHORT).show();
						return;
					}

				String ci = etCi.getText().toString().trim();
				String telefono = etTelefono.getText().toString().trim();
				String direccion = etDireccion.getText().toString().trim();
				String garantia = etGarantia.getText().toString().trim();

				Cliente cliente = new Cliente();
				cliente.setNombre(etNombre.getText().toString().trim());
				cliente.setCi(ci);
				cliente.setTelefono(telefono);
				cliente.setDireccion(direccion);
				cliente.setGarantia(garantia);
				//cliente.setSaltoDomingo(cbSalto.isChecked() ? 1 : 0);
				
				// ---- CORRECCIÓN: manejar estado ----
				if (clienteId == -1) {
						// nuevo cliente: asigna estado = 1 (activo)
						cliente.setEstado(1);
					} else {
						// edición: preserva el estado actual si no tienes un control en UI
						Cliente existente = clientesDao.getClienteById(clienteId);
						if (existente != null) {
								cliente.setEstado(existente.getEstado());
							} else {
								cliente.setEstado(1); // fallback seguro
							}
					}

				long res;
				if (clienteId == -1) {
						res = clientesDao.insertCliente(cliente);
						if (res > 0) cliente.setId((int) res);
					} else {
						
						cliente.setId(clienteId);
						res = clientesDao.updateCliente(cliente);
					}

				if (res > 0) Toast.makeText(this, "Guardado correctamente.", Toast.LENGTH_SHORT).show();
				else Toast.makeText(this, "Error al guardar.", Toast.LENGTH_SHORT).show();

				setResult(RESULT_OK);
				finish();
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				getMenuInflater().inflate(R.menu.menu_volver, menu);
				return true;
			}

		@Override
		public boolean onOptionsItemSelected(android.view.MenuItem item) {

				switch (item.getItemId()) 
					{
						case android.R.id.home:
							finish();
							return true;
						case R.id.itmVolver:
							Intent back = new Intent(this,ClientesActivity.class);
							startActivity(back);
							return true;

					}


				return super.onOptionsItemSelected(item);
			}
		// ***
		
		// ***

	}
