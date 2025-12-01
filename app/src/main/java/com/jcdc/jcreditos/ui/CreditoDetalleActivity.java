package com.jcdc.jcreditos.ui;

import android.app.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.jcdc.jcreditos.*;
import com.jcdc.jcreditos.adapter.*;
import com.jcdc.jcreditos.dao.*;
import com.jcdc.jcreditos.model.*;
import java.text.*;
import java.util.*;

public class CreditoDetalleActivity extends Activity {

		// Formato que el usuario ingresa/ve
		private final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		// Formato que la DB (y la lógica interna) requiere
		private final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		private CreditosDao creditosDao;
		private ClientesDao clientesDao; // Necesario para cargar el Spinner de Clientes
		private PlanesDao planesDao;     // Necesario para cargar el Spinner de Planes
		// ... en CreditoDetalleActivity.java
		private CuotasDao cuotasDao;

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
				// ... dentro de onCreate()
				cuotasDao = new CuotasDao(this);

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
							// 💡 IMPLEMENTACIÓN DE FECHA ACTUAL
							//SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US); 
							String fechaHoy = DISPLAY_DATE_FORMAT.format(new Date()); // Asume que tienes importado java.util.Date
							etFechaInicio.setText(fechaHoy);
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
		// Método basado en Premisa 5 (Ahora implementado)
		private void loadCuotasList(int id) {
				// 1. Obtener la lista de cuotas
				List<Cuota> cuotas = cuotasDao.getCuotasByCreditoId(id);

				// 2. Crear y asignar el adaptador
				CuotasAdapter adapter = new CuotasAdapter(this, cuotas);
				lvCuotas.setAdapter(adapter);

				// Si la lista está vacía, ocultar la ListView o mostrar un mensaje
				if (cuotas.isEmpty()) {
						lvCuotas.setVisibility(View.GONE);
					} else {
						lvCuotas.setVisibility(View.VISIBLE);
					}
			}

		// Método basado en Premisa 4 (Lógica de guardado)
		/*private void guardarCredito() {
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
			}*/
		private void guardarCredito() {
				// 1. Validación y Extracción de datos del formulario
				String capitalStr = etCapital.getText().toString();
				String tasaStr = etTasaInteres.getText().toString(); // Asumiendo que tienes un etTasa
				String fechaInicioStr = etFechaInicio.getText().toString(); // 💡 Obtener la fecha de la UI
				Date fechaInicioObjeto;

				// Validaciones
				if (capitalStr.isEmpty()) {
						Toast.makeText(this, "Debe ingresar el capital.", Toast.LENGTH_SHORT).show();
						return;
					}

				// VALIDACIÓN CRUCIAL DEL CLIENTE
				if (clienteSeleccionadoId == -1) {
						Toast.makeText(this, "Debe seleccionar un cliente de la lista de sugerencias.", Toast.LENGTH_LONG).show();
						return;
					}

				// VALIDACIÓN Y CONVERSIÓN DE LA FECHA
				try {
						// 💡 CONVERSIÓN: String (dd/MM/yyyy) -> Date
						fechaInicioObjeto = DISPLAY_DATE_FORMAT.parse(fechaInicioStr);
					} catch (ParseException e) {
						Toast.makeText(this, "Error: El formato de fecha debe ser dd/MM/yyyy.", Toast.LENGTH_LONG).show();
						e.printStackTrace();
						return; 
					}

				// 2. Obtener objetos seleccionados (asumiendo que los Spinners devuelven el POJO)
				Plan planSeleccionado = (Plan) spPlan.getSelectedItem();

				// 3. Crear o actualizar objeto Credito
				if (creditoActual == null) {
						creditoActual = new Credito();
					}

				// Setear el ID del cliente seleccionado
				creditoActual.setClienteId(clienteSeleccionadoId);
				creditoActual.setPlanId(planSeleccionado.getId());

				// Setear los datos financieros
				creditoActual.setCapital(Double.parseDouble(capitalStr));
				creditoActual.setInteresPorcentaje(Double.parseDouble(tasaStr)); // Asumiendo setTasa en Credito

				// 💡 ASIGNAR EL OBJETO DATE AL CRÉDITO
				// Asegúrate de que Credito.java tenga setFechaInicio(Date)
				creditoActual.setFechaInicio(fechaInicioObjeto);

				// 4. Ejecución de la operación
				long resultado;
				if (creditoId == -1) {
						// CREAR: Usar el método transaccional que inserta y genera cuotas
						resultado = creditosDao.insertCreditoAndCuotas(creditoActual);
						Toast.makeText(this, "Crédito Creado!", Toast.LENGTH_SHORT).show();
					} else {
						// EDITAR: Solo actualizar el crédito principal (la modificación de cuotas es más compleja)
						// Ya corregimos el error de updateCredito en el primer turno.
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
				// 1. Listener para Capital y Tasa (al escribir)
				TextWatcher textWatcher = new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
								calcularTotales();
							}
						@Override
						public void afterTextChanged(Editable s) {}
					};

				etCapital.addTextChangedListener(textWatcher);
				etTasaInteres.addTextChangedListener(textWatcher);

				// 2. Listener para el Plan (al seleccionar)
				spPlan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
									calcularTotales();
								}
							@Override
							public void onNothingSelected(AdapterView<?> parent) {
									// No hacer nada
								}
						});
			}

		// Método de Cálculo (Lógica de Negocio)
		private void calcularTotales() {
				// 1. Obtener valores y validar
				double capital;
				double interesPorcentaje;

				try {
						capital = Double.parseDouble(etCapital.getText().toString());
						interesPorcentaje = Double.parseDouble(etTasaInteres.getText().toString());
					} catch (NumberFormatException e) {
						// Si no son números válidos, ponemos 0.00 en los resultados
						tvInteresMonto.setText("Bs. 0.00");
						tvTotalCredito.setText("Bs. 0.00");
						return;
					}

				Plan planSeleccionado = (Plan) spPlan.getSelectedItem();
				if (planSeleccionado == null) {
						// Si no hay plan seleccionado, no podemos calcular
						return;
					}

				// 2. Lógica de Cálculo (Interés simple sobre el capital)

				// Ejemplo: Si la tasa es 20%, el interés total es 20% del capital.
				double interesMonto = capital * (interesPorcentaje / 100.0); 

				// Si tu lógica incluye el número de cuotas, tendrías que calcularlo así:
				// int cuotas = planSeleccionado.getCuotasTotales(); 
				// Si el interés es por mes, la fórmula sería más compleja. 

				double totalAPagar = capital + interesMonto;

				// 3. Mostrar Resultados (Formatear a 2 decimales)
				tvInteresMonto.setText("Bs. " + String.format("%.2f", interesMonto));
				tvTotalCredito.setText("Bs. " + String.format("%.2f", totalAPagar));

				// 4. (Opcional) Guardar los valores calculados en el objeto Credito actual
				if (creditoActual == null) {
						creditoActual = new Credito();
					}
				creditoActual.setCapital(capital);
				creditoActual.setInteresPorcentaje(interesPorcentaje);
				creditoActual.setInteresMonto(interesMonto);
				creditoActual.setTotal(totalAPagar);
			}

		// ... (Métodos de Menú/ActionBar similares a PlanesActivity)
	}
