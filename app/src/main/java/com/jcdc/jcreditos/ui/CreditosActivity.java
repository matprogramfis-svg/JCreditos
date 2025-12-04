package com.jcdc.jcreditos.ui;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.jcdc.jcreditos.*;
import com.jcdc.jcreditos.adapter.*;
import com.jcdc.jcreditos.dao.*;
import com.jcdc.jcreditos.model.*;
import java.util.*;

public class CreditosActivity extends Activity {

		private CreditosDao creditosDao;
		private ClientesDao clientesDao;

		private ListView listViewCreditos;
		private CreditosAdapter adapter;

		private EditText etBuscarCliente;
		private ListView listaSugerencias;

		private Button btnAgregar;

		private int clienteSeleccionadoId = -1;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_creditos);

				creditosDao = new CreditosDao(this);
				clientesDao = new ClientesDao(this);

				listViewCreditos = findViewById(R.id.listview_creditos);
				listaSugerencias = findViewById(R.id.lista_sugerencias_clientes);
				etBuscarCliente = findViewById(R.id.etBuscarCliente);
				btnAgregar = findViewById(R.id.btn_agregar_credito);

				loadCreditosList(); // muestra todos al entrar

				setupAutocomplete(); // 🔥 activar buscador de clientes

				// --- Botón agregar ---
				btnAgregar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									Intent intent = new Intent(CreditosActivity.this, CreditoDetalleActivity.class);
									startActivity(intent);
								}
						});

				// --- Abrir cuotas al seleccionar crédito ---
				listViewCreditos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									int creditoId = (int) id;

									Intent intent = new Intent(CreditosActivity.this, CuotasActivity.class);
									intent.putExtra("CREDITO_ID", creditoId);
									startActivity(intent);
								}
						});
			}

		// -------------------------------------------------------------
		// 🔎 AUTOCOMPLETE DE CLIENTES
		// -------------------------------------------------------------
		private void setupAutocomplete() {

				etBuscarCliente.addTextChangedListener(new TextWatcher() {

							@Override
							public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

							@Override
							public void onTextChanged(CharSequence s, int start, int before, int count) {

									String texto = s.toString().trim();

									if (texto.length() == 0) {
											clienteSeleccionadoId = -1;
											listaSugerencias.setVisibility(View.GONE);
											loadCreditosList(); // restaurar todos
											return;
										}

									final List<Cliente> lista = clientesDao.searchClientes(texto);

									if (lista.isEmpty()) {
											listaSugerencias.setVisibility(View.GONE);
											return;
										}

									// Crear lista de nombres
									List<String> nombres = new ArrayList<>();
									for (Cliente c : lista) {
											nombres.add(c.getNombre());
										}

									// Mostrar sugerencias
									ArrayAdapter<String> sugAdapter =
										new ArrayAdapter<String>(CreditosActivity.this,
																 android.R.layout.simple_list_item_1, nombres);

									listaSugerencias.setAdapter(sugAdapter);
									listaSugerencias.setVisibility(View.VISIBLE);

									// Manejar selección de cliente
									listaSugerencias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
												@Override
												public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

														Cliente seleccionado = lista.get(position);

														clienteSeleccionadoId = seleccionado.getId();
														etBuscarCliente.setText(seleccionado.getNombre());

														listaSugerencias.setVisibility(View.GONE);

														// 🔥 Cargar créditos solo de ese cliente
														loadCreditosPorCliente(clienteSeleccionadoId);
													}
											});
								}

							@Override
							public void afterTextChanged(Editable s) {}
						});
			}

		// -------------------------------------------------------------
		// CARGAR TODOS LOS CRÉDITOS
		// -------------------------------------------------------------
		private void loadCreditosList() {
				List<Credito> lista = creditosDao.getAllCreditos();

				if (adapter == null) {
						adapter = new CreditosAdapter(this, lista);
						listViewCreditos.setAdapter(adapter);
					} else {
						adapter.updateData(lista);
					}

				if (lista.isEmpty()) {
						Toast.makeText(this, "No hay créditos registrados.", Toast.LENGTH_LONG).show();
					}
			}

		// -------------------------------------------------------------
		// 🔥 CARGAR CRÉDITOS DE UN CLIENTE
		// -------------------------------------------------------------
		private void loadCreditosPorCliente(int clienteId) {

				List<Credito> lista = creditosDao.getCreditosByCliente(clienteId);

				if (adapter == null) {
						adapter = new CreditosAdapter(this, lista);
						listViewCreditos.setAdapter(adapter);
					} else {
						adapter.updateData(lista);
					}

				if (lista.isEmpty()) {
						Toast.makeText(this, "Este cliente no tiene créditos.", Toast.LENGTH_SHORT).show();
					}
			}

		// -------------------------------------------------------------
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
				getMenuInflater().inflate(R.menu.menu_volver, menu);
				return true;
			}

		@Override
		public boolean onOptionsItemSelected(android.view.MenuItem item) {

				switch (item.getItemId()) {
						case android.R.id.home:
							finish();
							return true;
						case R.id.itmVolver:
							Intent back = new Intent(this, MainActivity.class);
							startActivity(back);
							return true;
					}

				return super.onOptionsItemSelected(item);
			}
	}
