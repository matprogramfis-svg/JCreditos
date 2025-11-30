package com.jcdc.jcreditos.ui;

import android.app.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.jcdc.jcreditos.*;
import com.jcdc.jcreditos.dao.*;
import com.jcdc.jcreditos.model.*;
import java.util.*;

public class CreditoDetalleActivity extends Activity {

		private CreditosDao creditosDao;
		private ClientesDao clientesDao; // Necesario para cargar el Spinner de Clientes
		private PlanesDao planesDao;     // Necesario para cargar el Spinner de Planes

		// Componentes del Formulario
		private Spinner spPlan;
		private EditText etCapital, etTasaInteres, etFechaInicio;
		private TextView tvInteresMonto, tvTotalCredito;
		private Button btnGuardar;
		private ListView lvCuotas; // Para mostrar las cuotas del crédito

		private int creditoId = -1; // -1 indica que es un nuevo crédito
		private Credito creditoActual;
		//private List<Cliente> listaClientes;
		private List<Plan> listaPlanes;
		
		// Nuevas Variables para el Autocompletado
		private EditText etClienteNombre; // El campo de texto (ya lo tenías como etCliente)
		private ListView lvSugerenciasClientes; // El ListView flotante
		private int clienteSeleccionadoId = -1; // ID del cliente final

		// Adaptador simple para mostrar las sugerencias de nombres
		private ArrayAdapter<String> sugerenciasAdapter;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_credito_detalle); 

				// 1. Inicializar DAOs
				creditosDao = new CreditosDao(this);
				clientesDao = new ClientesDao(this);
				planesDao = new PlanesDao(this);

				// 2. Referenciar Vistas
				setupViews();

				// 3. Cargar Spinners (Clientes y Planes)
				loadSpinners();

				// 4. Revisar si es Edición
				loadCreditoData();

				setupAutocomplete(); // Llamada al nuevo método
				
				// 5. Listener de Guardado
				btnGuardar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									guardarCredito();
								}
						});
				// 6. Listener para Cálculos automáticos (al cambiar Capital, Tasa o Plan)
				setupCalculationListeners();
			}

		private void setupViews() {
				//spCliente = findViewById(R.id.sp_cliente);
				spPlan = findViewById(R.id.sp_plan);
				etCapital = findViewById(R.id.et_capital);
				etTasaInteres = findViewById(R.id.et_tasa_interes);
				etFechaInicio = findViewById(R.id.et_fecha_inicio);
				tvInteresMonto = findViewById(R.id.tv_interes_monto);
				tvTotalCredito = findViewById(R.id.tv_total_credito);
				btnGuardar = findViewById(R.id.btn_guardar_credito);
				lvCuotas = findViewById(R.id.lv_cuotas); // Solo visible en modo Edición
				// Autocompletado:
				etClienteNombre = findViewById(R.id.et_cliente_nombre);
				lvSugerenciasClientes = findViewById(R.id.lv_sugerencias_clientes);
			}

		// Método basado en Premisa 2
		private void loadSpinners() {
				//listaClientes = clientesDao.getAllClientes();
				listaPlanes = planesDao.getAllPlanes();

				// Adaptador para Clientes (Mostrar nombre)
				/*ArrayAdapter<Cliente> clienteAdapter = new ArrayAdapter<>(this, 
																		  android.R.layout.simple_spinner_dropdown_item, listaClientes);
				spCliente.setAdapter(clienteAdapter);*/
				
				// Adaptador para Planes (Mostrar nombre)
				ArrayAdapter<Plan> planAdapter = new ArrayAdapter<>(this, 
																	android.R.layout.simple_spinner_dropdown_item, listaPlanes);
				spPlan.setAdapter(planAdapter);
			}

		// Método basado en Premisa 1
		private void loadCreditoData() {
				creditoId = getIntent().getIntExtra("CREDITO_ID", -1);
				if (creditoId != -1) {
						// Modo Edición/Detalle
						creditoActual = creditosDao.getCreditoById(creditoId);
						if (creditoActual != null) {
								// Rellenar campos del formulario con los datos de creditoActual
								etCapital.setText(String.valueOf(creditoActual.getCapital()));
								// ... (rellenar otros campos)

								// Cargar la lista de cuotas (Premisa 5)
								loadCuotasList(creditoActual.getId());

								setTitle("Detalle de Crédito #" + creditoId);
							}
					} else {
						// Modo Creación
						setTitle("Nuevo Crédito");
						// Inicializar fecha de inicio a hoy
						// etFechaInicio.setText(dateFormat.format(new Date()));
					}
			}
			
		// --- NUEVO MÉTODO: setupAutocomplete() ---
		private void setupAutocomplete() {
				// 1. Configurar el Listener de escritura
				etClienteNombre.addTextChangedListener(new TextWatcher() {
							@Override
							public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

							@Override
							public void onTextChanged(CharSequence s, int start, int before, int count) {
									String query = s.toString().trim();
									if (query.length() > 0) {
											// Llama al DAO para obtener coincidencias (Premisa: se necesita un nuevo método en ClientesDao)
											List<Cliente> coincidencias = clientesDao.searchClientes(query); 
											updateSugerenciasList(coincidencias);
										} else {
											lvSugerenciasClientes.setVisibility(View.GONE);
											clienteSeleccionadoId = -1; // Deseleccionar si el campo se borra
										}
								}

							@Override
							public void afterTextChanged(Editable s) {}
						});
						
				// 2. Configurar el Listener de clic en la sugerencia
				lvSugerenciasClientes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									// El objeto Cliente se obtiene del adaptador en la posición clicada
									Cliente cliente = (Cliente) parent.getItemAtPosition(position);

									// 3. Colocar el nombre en el EditText y guardar el ID
									etClienteNombre.setText(cliente.getNombre());
									clienteSeleccionadoId = cliente.getId(); // Guardamos el ID real

									// 4. Ocultar la lista
									lvSugerenciasClientes.setVisibility(View.GONE);
								}
						});

						// 3. Colocar el nombre en el EditText y guardar el ID
						/*etClienteNombre.setText(cliente.getNombre());
						clienteSeleccionadoId = cliente.getId();

						// 4. Ocultar la lista
						lvSugerenciasClientes.setVisibility(View.GONE);*/
					}
					
		// --- NUEVO MÉTODO AUXILIAR: updateSugerenciasList() ---
		private void updateSugerenciasList(List<Cliente> coincidencias) {
				if (coincidencias.isEmpty()) {
						lvSugerenciasClientes.setVisibility(View.GONE);
					} else {
						// En lugar de ArrayAdapter<String>, usamos un adaptador que contenga objetos Cliente
						// para poder obtener el ID fácilmente al hacer clic.

						// NOTA: Para simplicidad, usa un adaptador personalizado si quieres mostrar más datos
						// O usa el List<Cliente> directamente si el POJO Cliente tiene un toString() adecuado.

						// Creamos un adaptador temporal con la lista de Cliente (asumiendo toString() devuelve el nombre)
						ArrayAdapter<Cliente> adapter = new ArrayAdapter<>(this, 
																		   android.R.layout.simple_list_item_1, coincidencias);

						lvSugerenciasClientes.setAdapter(adapter);
						lvSugerenciasClientes.setVisibility(View.VISIBLE);
					}
			}

		// Método basado en Premisa 5
		private void loadCuotasList(int id) {
				// CuotasDao debe tener un método para obtener cuotas por creditoId
				// List<Cuota> cuotas = cuotasDao.getCuotasByCreditoId(id);
				// CuotasAdapter adapter = new CuotasAdapter(this, cuotas);
				// lvCuotas.setAdapter(adapter);
			}

		// Método basado en Premisa 4 (Lógica de guardado)
		private void guardarCredito() {
				// 1. Validación y Extracción de datos del formulario
				if (etCapital.getText().toString().isEmpty()) {
						Toast.makeText(this, "Debe ingresar el capital.", Toast.LENGTH_SHORT).show();
						return;
					}
					
				// VALIDACIÓN CRUCIAL DEL CLIENTE
				if (clienteSeleccionadoId == -1) {
						Toast.makeText(this, "Debe seleccionar un cliente de la lista de sugerencias.", Toast.LENGTH_LONG).show();
						return;
					}

				// 2. Obtener objetos seleccionados (asumiendo que los Spinners devuelven el POJO)
				//Cliente clienteSeleccionado = (Cliente) spCliente.getSelectedItem();
				Plan planSeleccionado = (Plan) spPlan.getSelectedItem();

				// 3. Crear o actualizar objeto Credito
				if (creditoActual == null) {
						creditoActual = new Credito();
					}

				//creditoActual.setClienteId(clienteSeleccionado.getId());
				creditoActual.setPlanId(planSeleccionado.getId());
				// ... (settear el resto de campos: capital, tasa, fechas, etc.)
				// Setear el ID del cliente seleccionado
				creditoActual.setClienteId(clienteSeleccionadoId);

				// 4. Ejecución de la operación
				long resultado;
				if (creditoId == -1) {
						// CREAR: Usar el método transaccional que inserta y genera cuotas
						resultado = creditosDao.insertCreditoAndCuotas(creditoActual);
						Toast.makeText(this, "Crédito Creado!", Toast.LENGTH_SHORT).show();
					} else {
						// EDITAR: Solo actualizar el crédito principal (la modificación de cuotas es más compleja)
						resultado = creditosDao.updateCredito(creditoActual);
						Toast.makeText(this, "Crédito Actualizado!", Toast.LENGTH_SHORT).show();
					}

				if (resultado > 0) {
						setResult(RESULT_OK); // Indicar a CreditosActivity que recargue la lista
						finish();
					} else {
						Toast.makeText(this, "Error al guardar el crédito.", Toast.LENGTH_LONG).show();
					}
			}

		// Método basado en Premisa 3
		private void setupCalculationListeners() {
				// Implementar TextWatchers en etCapital y etTasaInteres, y OnItemSelectedListener en spPlan
				// para llamar a calcularTotales() cada vez que los valores cambien.
			}

		// Método de Cálculo (Lógica de Negocio)
		private void calcularTotales() {
				// Lógica de cálculo:
				/*
				 double capital = parse(etCapital);
				 double tasa = parse(etTasaInteres);

				 // Simple: Interés = Capital * Tasa * Plazo(en años) -- Depende de la lógica de tu plan.
				 double interesMonto = capital * (tasa / 100); 
				 double total = capital + interesMonto;

				 tvInteresMonto.setText(String.format("%.2f", interesMonto));
				 tvTotalCredito.setText(String.format("%.2f", total));
				 */
			}

		// ... (Métodos de Menú/ActionBar similares a PlanesActivity)
	}
