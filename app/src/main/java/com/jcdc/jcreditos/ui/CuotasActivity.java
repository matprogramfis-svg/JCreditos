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

public class CuotasActivity extends Activity implements OnCuotaActionListener {

		private ListView listView;
		private CuotasAdapter adapter;
		private List<Cuota> listaCuotas;
		private int creditoId;
		private CuotasDao dao;
		private TextView tvClienteNombre;
		private TextView tvPlanNombre;
		private CreditosDao creditosDao;
		private ClientesDao clientesDao;

        // 🆕 NUEVAS DECLARACIONES DE VISTAS Y ESTADO 🆕
		private LinearLayout llTotalAcumulado; // Layout para el total (el área azul)
		private TextView tvMontoAcumulado;     // TextView para mostrar el monto (ej: Bs. 40.00)
		private Button btnPagarSeleccionadas;  // El nuevo botón de acción flotante
		private double montoTotalAcumulado = 0.0; // Variable de suma
		// 🆕 NUEVA DECLARACIÓN 🆕
		private TextView tvSaldoPendiente;
		// 🆕 NUEVA VARIABLE PARA EL SALDO TOTAL BASE 🆕
		private double saldoInicialCredito = 0.0;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_cuotas);

				listView = findViewById(R.id.listview_cuotas);

				// DAO
				dao = new CuotasDao(this);
				creditosDao = new CreditosDao(this);
				clientesDao = new ClientesDao(this);

                // 1. REFERENCIAR VISTAS EXISTENTES
				tvClienteNombre = findViewById(R.id.tv_cuotas_cliente_nombre);
				tvPlanNombre = findViewById(R.id.tv_cuotas_plan_nombre);

                // 2. REFERENCIAR LAS NUEVAS VISTAS DEL XML
                // Estos IDs provienen del layout XML que modificamos.
				llTotalAcumulado = (LinearLayout) findViewById(R.id.ll_total_acumulado);
				tvMontoAcumulado = (TextView) findViewById(R.id.tv_monto_acumulado);
				btnPagarSeleccionadas = (Button) findViewById(R.id.btn_pagar_seleccionadas);
				// 🆕 REFERENCIA A LA NUEVA VISTA 🆕
				tvSaldoPendiente = findViewById(R.id.tv_saldo_pendiente);

				// RECIBIR EL ID DEL CRÉDITO
				creditoId = getIntent().getIntExtra("CREDITO_ID", -1);

				// CARGAR DATOS
				loadHeaderData(creditoId);
				loadCuotasData(); // Separamos la carga de datos en un método

                // 3. AGREGAR LISTENER AL NUEVO BOTÓN DE PAGO FINAL
                btnPagarSeleccionadas.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									pagarCuotasSeleccionadas();
								}
						});
			}

        // 🆕 NUEVO MÉTODO PARA CARGAR DATOS INICIALES Y RECAGAR 🆕
        private void loadCuotasData() {
				// CARGAR LISTA DE CUOTAS
				listaCuotas = dao.getCuotasByCreditoId(creditoId);

				if (adapter == null) {
						// ADAPTER CON LISTENER (Inicialización)
						adapter = new CuotasAdapter(this, listaCuotas, this);
						listView.setAdapter(adapter);
					} else {
						// Actualizar el adaptador (USANDO EL NUEVO MÉTODO)
						adapter.updateData(listaCuotas);
					}
				// Asegurarse de que el estado de selección esté limpio
				resetearEstadoDeSeleccion();
			}

		// ===========================================================
		//          IMPLEMENTACIÓN DE LA INTERFAZ OnCuotaActionListener
		// ===========================================================

        // 1. MÉTODO EXISTENTE: Llamado al revertir un pago (cuota.getPagada() == 1)
		@Override
		public void onCuotaEdit(Cuota cuota) {
				// EJEMPLO: Revertir el pago (poner Pagada = 0)
				int filas = dao.revertirPago(cuota.getId()); // <-- Este método debe existir en tu DAO

				if (filas > 0) {
						Toast.makeText(CuotasActivity.this,
									   "Pago revertido para Cuota #" + cuota.getNumeroCuota(),
									   Toast.LENGTH_SHORT).show();
						loadCuotasData(); // Recargar la lista
					} else {
						Toast.makeText(CuotasActivity.this,
									   "Error al revertir el pago.",
									   Toast.LENGTH_SHORT).show();
					}
			}

        // 2. MÉTODO ANTICUADO: onCuotaPaid ya no se usa con selección múltiple
        // Lo dejamos como estaba en tu código original, pero ya no se llama desde el adapter
		@Override
		public void onCuotaPaid(int cuotaId) {
				loadCuotasData(); // Recargar la lista
				Toast.makeText(CuotasActivity.this, "Cuota actualizada", Toast.LENGTH_SHORT).show();
			}

        // 3. 🆕 NUEVO MÉTODO CLAVE: SUMA/RESTA DE MONTO 🆕
        @Override
        public void onCuotaSelectionChange(double monto) {
				// Este método se llama desde el adaptador cada vez que se selecciona/deselecciona
				montoTotalAcumulado += monto;
				
				// 2. 🆕 CÁLCULO DINÁMICO DEL SALDO 🆕
				double saldoDinamico = saldoInicialCredito - montoTotalAcumulado;

				// 3. Actualizar el TextView del Saldo Pendiente (el que está junto a Cliente/Plan)
				tvSaldoPendiente.setText("Saldo : " + String.format("%.2f", saldoDinamico)+ " Bs.");

				// 1. Actualizar el texto del total
				String totalFormat = String.format("%.2f", montoTotalAcumulado);
				String textTotal = "Bs. " + totalFormat;

				tvMontoAcumulado.setText(textTotal);

				// 2. Actualizar el texto del botón de pago
				//btnPagarSeleccionadas.setText("Pagar Cuotas Seleccionadas (" + textTotal + ")");

				// 3. Controlar la visibilidad
				/*if (montoTotalAcumulado > 0) {
						llTotalAcumulado.setVisibility(View.VISIBLE);
						//btnPagarSeleccionadas.setVisibility(View.VISIBLE);
					} else {
						llTotalAcumulado.setVisibility(View.GONE);
						//btnPagarSeleccionadas.setVisibility(View.GONE);
					}*/
			}

        // ===========================================================
		//          LÓGICA DE PAGO FINAL
		// ===========================================================

        // 🆕 MÉTODO PARA EJECUTAR EL PAGO DE TODAS LAS CUOTAS SELECCIONADAS 🆕
        private void pagarCuotasSeleccionadas() {
				// Obtener el mapa de IDs de cuotas seleccionadas (Map<Long, Boolean>)
				Map<Long, Boolean> seleccionadas = adapter.getSelectedCuotas();

				if (seleccionadas.isEmpty()) {
						Toast.makeText(this, "No hay cuotas seleccionadas para pagar.", Toast.LENGTH_SHORT).show();
						return;
					}

				// Crear una lista de IDs para pagar
				List<Long> idsParaPagar = new ArrayList<>(); 
				for (Long cuotaId : seleccionadas.keySet()) {
						// cuotaId es Long, ya que arreglamos el tipo en el adaptador
						idsParaPagar.add(cuotaId);
					}

				// Ejecutar los pagos
				int pagosExitosos = 0;

				for (Long cuotaId : idsParaPagar) {
						// Llama al método del DAO para marcar como pagada
						// Asumiendo que tu método markCuotaAsPaid acepta un 'long' o 'int'
						// y que lo tienes implementado en CuotasDao
						int filasAfectadas = dao.markCuotaAsPaid(cuotaId); 
						if (filasAfectadas > 0) {
								pagosExitosos++;
							}
					}

				// Mostrar resultado
				if (pagosExitosos > 0) {
						/*Toast.makeText(this, 
									   pagosExitosos + " Cuota(s) pagada(s) por un total de Bs. " + String.format("%.2f", montoTotalAcumulado), 
									   Toast.LENGTH_LONG).show();*/
						Toast.makeText(this, " Cuota(s) pagada(s) ", Toast.LENGTH_LONG).show();
						volver();	   
					} else {
						Toast.makeText(this, "Error: No se pudo registrar el pago de ninguna cuota.", Toast.LENGTH_LONG).show();
					}

				// Recargar la lista para reflejar los pagos y limpiar la selección
				//loadCuotasData();
			}

        // 🆕 MÉTODO PARA LIMPIAR EL ESTADO DE SELECCIÓN 🆕
        private void resetearEstadoDeSeleccion() {
				montoTotalAcumulado = 0.0;

				// Limpia el estado de la vista
				tvMontoAcumulado.setText("Bs. 0.00");
				//btnPagarSeleccionadas.setText("Pagar Cuotas Seleccionadas (Bs. 0.00)");

				// Ocultar las vistas
				//llTotalAcumulado.setVisibility(View.GONE);
				//btnPagarSeleccionadas.setVisibility(View.GONE);

				// Limpiar la selección en el Adaptador
				if (adapter != null) {
						adapter.getSelectedCuotas().clear(); 
						// No es estrictamente necesario notifyDataSetChanged aquí si loadCuotasData lo llama,
						// pero ayuda si la Activity se recarga sin llamar a loadCuotasData.
					}
			}

        // ===========================================================
		//          MÉTODOS EXISTENTES
		// ===========================================================

		private void loadHeaderData(int id) {
				Credito credito = creditosDao.getCreditoById(id);

				if (credito != null) {
						Cliente cliente = clientesDao.getClienteById(credito.getClienteId());

						if (cliente != null) {
								tvClienteNombre.setText("Cliente: " + cliente.getNombre());
							} else {
								tvClienteNombre.setText("Cliente: Desconocido");
							}

						Plan plan = new PlanesDao(this).getPlanById(credito.getPlanId());
						if (plan != null) {
								tvPlanNombre.setText("Plan: " + plan.getNombre());
							} else {
								tvPlanNombre.setText("Plan: N/A");
							}
						// 🆕 LÓGICA CLAVE: CALCULAR Y MOSTRAR SALDO PENDIENTE 🆕

						// El método calcularSaldoRestante espera un 'long', así que convertimos el 'int' de la Activity.
						// creditosDao es la instancia correcta para este método.
						// 1. Calcular el saldo actual (estático)
						double saldo = creditosDao.calcularSaldoRestante((long) id);
						// 2. Almacenar este saldo como base para el cálculo dinámico
						saldoInicialCredito = saldo; 
						// 3. Mostrar el saldo inicial
						//tvSaldoPendiente.setText("Bs. " + String.format("%.2f", saldo));
						//double saldo = creditosDao.calcularSaldoRestante((long) id);
						// 2. Mostrar el saldo pendiente
						tvSaldoPendiente.setText("Saldo : " + String.format("%.2f", saldo)+ " Bs");

						// 3. Opcional: Cambiar color basado en el saldo
						/*if (saldo <= 0) {
								tvSaldoPendiente.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
							} else {
								tvSaldoPendiente.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
							}*/
					
					}
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
				getMenuInflater().inflate(R.menu.menu_cuotas, menu);
				return true;
			}

		@Override
		public boolean onOptionsItemSelected(android.view.MenuItem item) {

				switch (item.getItemId()) {
						case R.id.itmVolver:
							volver();
							return true;
						case R.id.itmEditarCuota:
							adapter.setEditMode(true);
							return true;
						case R.id.itmArchivarTarjeta:
							/*Intent back = new Intent(this,CreditosActivity.class);
							 startActivity(back);*/
							return true;

					}


				return super.onOptionsItemSelected(item);
			}
		// ***
		private void volver()
		{
			Intent back = new Intent(this,CreditosActivity.class);
			startActivity(back);
		}
	}
