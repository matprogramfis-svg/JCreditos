package com.jcdc.jcreditos.ui;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.jcdc.jcreditos.*;
import com.jcdc.jcreditos.adapter.*;
import com.jcdc.jcreditos.dao.*;
import com.jcdc.jcreditos.model.*;
import java.util.*;

public class CreditosActivity extends Activity {

		private CreditosDao creditosDao;
		private ListView listViewCreditos;
		private CreditosAdapter adapter;
		private Button btnAgregar;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_creditos);

				creditosDao = new CreditosDao(this);
				listViewCreditos = findViewById(R.id.listview_creditos);
				btnAgregar = findViewById(R.id.btn_agregar_credito);

				loadCreditosList();

				// --- Crear nuevo crédito ---
				btnAgregar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									Intent intent = new Intent(CreditosActivity.this, CreditoDetalleActivity.class);
									startActivity(intent);
								}
						});

				// --- ABRIR CUOTAS AL SELECCIONAR UN CRÉDITO ---
				listViewCreditos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

									// id viene de getItemId() → es el ID del crédito
									int creditoId = (int) id;

									Intent intent = new Intent(CreditosActivity.this, CuotasActivity.class);
									intent.putExtra("CREDITO_ID", creditoId);

									startActivity(intent);
								}
						});
			}

		private void loadCreditosList() {
				List<Credito> lista = creditosDao.getAllCreditos();

				if (adapter == null) {
						adapter = new CreditosAdapter(this, lista);
						listViewCreditos.setAdapter(adapter);
					} else {
						adapter.updateData(lista);
					}

				if (lista.isEmpty()) {
						Toast.makeText(this, "No hay créditos registrados. Agrega uno.", Toast.LENGTH_LONG).show();
					}
			}

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
