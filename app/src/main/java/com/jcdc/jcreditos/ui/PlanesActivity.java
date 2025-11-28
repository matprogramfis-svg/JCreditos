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

public class PlanesActivity extends Activity {

		private static final int REQUEST_CODE_EDITAR_PLAN = 1;

		private PlanesDao planesDao;
		private ListView listViewPlanes;
		private PlanesAdapter adapter;
		private Button btnAgregar;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_planes); 

				planesDao = new PlanesDao(this);
				listViewPlanes = findViewById(R.id.listview_planes);
				btnAgregar = findViewById(R.id.btn_agregar_plan);

				// 1. Cargar y configurar la lista
				loadPlanesList();

				// 2. Listener para crear un nuevo plan
				btnAgregar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									// Iniciar PlanDetalleActivity sin pasar ID (crear nuevo)
									Intent intent = new Intent(PlanesActivity.this, PlanDetalleActivity.class);
									startActivityForResult(intent, REQUEST_CODE_EDITAR_PLAN);
								}
						});

				// 3. Listener para editar un plan existente
				listViewPlanes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									// 'id' es el Plan ID que retornó getItemId() del Adapter

									Intent intent = new Intent(PlanesActivity.this, PlanDetalleActivity.class);
									intent.putExtra("PLAN_ID", (int) id); // Pasar el ID del plan seleccionado
									startActivityForResult(intent, REQUEST_CODE_EDITAR_PLAN);
								}
						});
			}

		private void loadPlanesList() {
				List<Plan> lista = planesDao.getAllPlanes();

				if (adapter == null) {
						adapter = new PlanesAdapter(this, lista);
						listViewPlanes.setAdapter(adapter);
					} else {
						// Si el adaptador ya existe (por ejemplo, al volver de editar)
						adapter.updateData(lista);
					}

				if (lista.isEmpty()) {
						Toast.makeText(this, "No hay planes creados. Agrega uno.", Toast.LENGTH_LONG).show();
					}
			}

		// Este método se llama cuando PlanDetalleActivity termina
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				super.onActivityResult(requestCode, resultCode, data);

				if (requestCode == REQUEST_CODE_EDITAR_PLAN && resultCode == RESULT_OK) {
						// Si el planDetalle se guardó/actualizó (RESULT_OK), recargar la lista
						loadPlanesList();
					}
			}
		// ***
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
