package com.jcdc.jcreditos.ui;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.jcdc.jcreditos.*;
import com.jcdc.jcreditos.adapter.*;
import com.jcdc.jcreditos.dao.*;
import com.jcdc.jcreditos.model.*;
import java.util.*;

public class CuotasActivity extends Activity implements OnCuotaActionListener {

		private ListView listView;
		private Button btnGuardar;
		private CuotasAdapter adapter;
		private List<Cuota> listaCuotas;
		private int creditoId;
		private CuotasDao dao;
		private TextView tvClienteNombre; // <-- Nuevo TextView
		private TextView tvPlanNombre;    // <-- Nuevo TextView

		private CreditosDao creditosDao;  // <-- Nueva instancia
		private ClientesDao clientesDao;  // <-- Nueva instancia

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_cuotas);

				listView = findViewById(R.id.listview_cuotas);
				btnGuardar = findViewById(R.id.btn_guardar_cuotas);

				// DAO
				dao = new CuotasDao(this);
				
				// 1. INICIALIZAR NUEVOS DAOS Y VIEWS
				creditosDao = new CreditosDao(this); // Necesario para obtener el Plan ID
				clientesDao = new ClientesDao(this); // Necesario para obtener el Nombre del Cliente

				tvClienteNombre = findViewById(R.id.tv_cuotas_cliente_nombre);
				tvPlanNombre = findViewById(R.id.tv_cuotas_plan_nombre);

				// RECIBIR EL ID DEL CRÉDITO
				creditoId = getIntent().getIntExtra("CREDITO_ID", -1);

				// 2. CARGAR Y MOSTRAR ENCABEZADO
				loadHeaderData(creditoId);
				
				// CARGAR LISTA DE CUOTAS
				listaCuotas = dao.getCuotasByCreditoId(creditoId);

				// ADAPTER CON LISTENER
				adapter = new CuotasAdapter(this, listaCuotas, this);
				listView.setAdapter(adapter);

				// BOTÓN GUARDAR (sin lambda)
				btnGuardar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {

									dao.actualizarCuotas(listaCuotas);

									Toast.makeText(CuotasActivity.this, "Cambios guardados", Toast.LENGTH_SHORT).show();
									finish();
								}
						});
			}

		// ===========================================================
		//          MÉTODO DEL LISTENER → recargar lista
		// ===========================================================
		/*@Override
		public void onCuotaPaid(int cuotaId) {

				// Recargar otra vez desde la BD
				listaCuotas = dao.getCuotasByCreditoId(creditoId);

				// Actualizar adapter
				adapter = new CuotasAdapter(this, listaCuotas, this);
				listView.setAdapter(adapter);
			}*/
		// Dentro de CuotasActivity.java (el método del Listener)

		@Override
		public void onCuotaPaid(int cuotaId) {

				// 1. Recargar la lista actualizada desde la BD
				listaCuotas = dao.getCuotasByCreditoId(creditoId);

				// 2. Actualizar el adaptador (USANDO EL NUEVO MÉTODO)
				adapter.updateData(listaCuotas);

				// (Ya no necesitas volver a llamar a listView.setAdapter)

				Toast.makeText(CuotasActivity.this, "Cuota actualizada", Toast.LENGTH_SHORT).show();
			}
			
		private void loadHeaderData(int id) {
				// A. Obtener el Crédito completo (para obtener ClienteID y PlanID)
				Credito credito = creditosDao.getCreditoById(id);

				if (credito != null) {
						// B. Obtener el objeto Cliente (para obtener el nombre)
						Cliente cliente = clientesDao.getClienteById(credito.getClienteId());

						// C. Mostrar datos
						if (cliente != null) {
								tvClienteNombre.setText("Cliente: " + cliente.getNombre());
							} else {
								tvClienteNombre.setText("Cliente: Desconocido");
							}

						// D. El Nombre del Plan ya lo tienes en CreditosDao.getAllCreditos()
						// Pero como getCreditoById() NO tiene el JOIN, hay dos opciones:
						// 1) Usar el PlanesDao.getPlanById(credito.getPlanId()) (RECOMENDADO)
						// 2) Modificar getCreditoById para usar un JOIN (Más complicado)

						// Opción 1: Usar PlanesDao (más simple para esta Activity)
						Plan plan = new PlanesDao(this).getPlanById(credito.getPlanId());
						if (plan != null) {
								tvPlanNombre.setText("Plan: " + plan.getNombre());
							} else {
								tvPlanNombre.setText("Plan: N/A");
							}
					}
			}
	}
