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

		// 1. Constante para el código de solicitud
		private static final int REQUEST_CODE_EDITAR_CREDITO = 2; // Usamos 2 o diferente al de Planes

		// 2. Adaptar las variables de instancia a Créditos
		private CreditosDao creditosDao;
		private ListView listViewCreditos;
		private CreditosAdapter adapter;
		private Button btnAgregar; // Asumo que usas un Button en R.layout.activity_creditos

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				// Usar el layout de créditos
				setContentView(R.layout.activity_creditos); 

				// 3. Inicializar componentes
				creditosDao = new CreditosDao(this);
				listViewCreditos = findViewById(R.id.listview_creditos); // ID debe coincidir con activity_creditos.xml
				btnAgregar = findViewById(R.id.btn_agregar_credito); // ID debe coincidir con activity_creditos.xml

				// 4. Cargar y configurar la lista de créditos
				loadCreditosList();

				// 5. Listener para crear un nuevo crédito (Patrón de PlanesActivity)
				btnAgregar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									// Iniciar la Activity de Formulario/Detalle de Crédito
									Intent intent = new Intent(CreditosActivity.this, CreditoDetalleActivity.class);
									// Nota: No pasamos ID, indicando que es una creación nueva
									startActivityForResult(intent, REQUEST_CODE_EDITAR_CREDITO);
								}
						});

				// 6. Listener para editar/ver detalle de un crédito existente (Patrón de PlanesActivity)
				listViewCreditos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									// 'id' es el Credito ID que retornó getItemId() del Adapter

									Intent intent = new Intent(CreditosActivity.this, CreditoDetalleActivity.class);
									intent.putExtra("CREDITO_ID", (int) id); // Pasar el ID del crédito seleccionado
									startActivityForResult(intent, REQUEST_CODE_EDITAR_CREDITO);
								}
						});
			}

		// Método adaptado para Créditos
		private void loadCreditosList() {
				List<Credito> lista = creditosDao.getAllCreditos(); // Usamos el método de CreditosDao

				if (adapter == null) {
						// Usamos el adaptador de créditos
						adapter = new CreditosAdapter(this, lista); 
						listViewCreditos.setAdapter(adapter);
					} else {
						// Si el adaptador ya existe, actualizar los datos
						adapter.updateData(lista);
					}

				if (lista.isEmpty()) {
						Toast.makeText(this, "No hay créditos registrados. Agrega uno.", Toast.LENGTH_LONG).show();
					}
			}

		// Manejo del resultado al volver de CreditoDetalleActivity
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				super.onActivityResult(requestCode, resultCode, data);

				if (requestCode == REQUEST_CODE_EDITAR_CREDITO && resultCode == RESULT_OK) {
						// Si la operación fue exitosa, recargar la lista
						loadCreditosList();
					}
			}

		// Menú de Opciones (Se copia directamente)
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
							Intent back = new Intent(this,MainActivity.class);
							startActivity(back);
							return true;

					}


				return super.onOptionsItemSelected(item);
			}

	}
