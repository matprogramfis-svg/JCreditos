package com.jcdc.jcreditos.ui;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.jcdc.jcreditos.*;
import com.jcdc.jcreditos.adapter.*;
import com.jcdc.jcreditos.dao.*;
import com.jcdc.jcreditos.model.*;
import java.text.*;
import java.util.*;

public class CreditoDetalleActivity extends Activity implements OnCuotaActionListener
	{

		@Override
		public void onCuotaPaid(int cuotaId)
			{
				// 💡 Paso 1: Verificamos que tengamos un crédito actual cargado
				if (creditoActual != null) {

						// 💡 Paso 2: Llamamos al método que recarga la lista de cuotas.
						// Esto consulta la base de datos de nuevo, encuentra la cuota como pagada,
						// y le dice al ListView que se redibuje.
						loadCuotasList(creditoActual.getId()); 
					}
			}
		
		private boolean bloqueandoTexto = false;
		
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

		private int creditoId = -1; // -1 indica que es un nuevo crédito
		private Credito creditoActual;
		//private List<Cliente> listaClientes;
		private List<Plan> listaPlanes;
		
		// Nuevas Variables para el Autocompletado
		private EditText etClienteNombre; // El campo de texto (ya lo tenías como etCliente)
		private ListView lvSugerenciasClientes; // El ListView flotante
		private int clienteSeleccionadoId = -1; // ID del cliente final
		//private int clienteSeleccionadoId = -1; // <-- INICIALIZACIÓN

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
				// Código de ejemplo para el Click Listener del botón GUARDAR CRÉDITO

				/*btnGuardar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									// 1. Obtener el nombre ingresado
									String nombreClienteIngresado = etClienteNombre.getText().toString().trim();

									if (nombreClienteIngresado.isEmpty()) {
											Toast.makeText(CreditoDetalleActivity.this, "El nombre del cliente es obligatorio.", Toast.LENGTH_SHORT).show();
											return;
										}

									clienteSeleccionadoId = obtenerClienteId(); // Método auxiliar que devuelve el ID

									// 2. LÓGICA CLAVE: Si no hay ID, crear un nuevo cliente
									if (clienteSeleccionadoId == -1) {

											// Si el campo de Cliente NO está vacío, creamos un nuevo cliente.

											// a. Crear objeto Cliente
											Cliente nuevoCliente = new Cliente();
											nuevoCliente.setNombre(nombreClienteIngresado);
											// IMPORTANTE: Asegúrate de configurar otros campos obligatorios (CI, Teléfono, etc.)
											// Si estos campos son obligatorios, debes mostrarlos en la UI.

											// b. Insertar en la base de datos
											ClientesDao clientesDao = new ClientesDao(CreditoDetalleActivity.this);
											long newRowId = clientesDao.insertCliente(nuevoCliente);

											if (newRowId > 0) {
													clienteSeleccionadoId = (int) newRowId;
													Toast.makeText(CreditoDetalleActivity.this, "Cliente nuevo creado con éxito.", Toast.LENGTH_SHORT).show();
												} else {
													Toast.makeText(CreditoDetalleActivity.this, "Error al crear nuevo cliente.", Toast.LENGTH_SHORT).show();
													return;
												}
										}

									// 3. CONTINUAR CON EL GUARDADO DEL CRÉDITO
									guardarCredito(); // Llamar a tu método de guardado de crédito
								}
						});*/
					// ***
				btnGuardar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {

									String nombreClienteIngresado = etClienteNombre.getText().toString().trim();

									if (nombreClienteIngresado.isEmpty()) {
											Toast.makeText(CreditoDetalleActivity.this, "El nombre del cliente es obligatorio.", Toast.LENGTH_SHORT).show();
											return;
										}

									// El autocomplete ya debe haber asignado el ID si el cliente existe.
									// Si el usuario escribe un nombre nuevo, clienteSeleccionadoId seguirá en -1.

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
				//lvCuotas = findViewById(R.id.lv_cuotas); // Solo visible en modo Edición
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
								etTasaInteres.setText(String.valueOf(creditoActual.getInteresPorcentaje()));
								// 1. Manejo de la Fecha de Inicio (¡Crucial!)
//    Debe usar el formato de visualización (dd/MM/yyyy), no solo String.valueOf(Date)
								if (creditoActual.getFechaInicio() != null) {
										String fechaInicioStr = DISPLAY_DATE_FORMAT.format(creditoActual.getFechaInicio());
										etFechaInicio.setText(fechaInicioStr);
									}

// 2. Rellenar el Autocompletado del Cliente
//    Necesitas el ID para el guardado posterior. Asumimos que tienes getNombreCliente() en Credito.java.
								etClienteNombre.setText(creditoActual.getNombreCliente());
								clienteSeleccionadoId = creditoActual.getClienteId(); // <-- IMPORTANTE: Guardar el ID para edición/guardado

// 3. SELECCIÓN DEL SPINNER DE PLANES (spPlan)
								int planIdActual = creditoActual.getPlanId();
								int posicionSeleccionada = 0; // Por defecto, el primer elemento

// Recorrer la lista de planes que cargaste previamente
								for (int i = 0; i < listaPlanes.size(); i++) {
										Plan plan = listaPlanes.get(i);
										if (plan.getId() == planIdActual) {
												posicionSeleccionada = i;
												break; // Detener el bucle al encontrar el plan
											}
									}

// Aplicar la selección
								spPlan.setSelection(posicionSeleccionada);
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

							/*@Override
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
								}*/
							@Override
							public void onTextChanged(CharSequence s, int start, int before, int count) {

									if (bloqueandoTexto) {
											return; // Evita que borre el clienteSeleccionadoId
										}

									String query = s.toString().trim();

									if (query.length() > 0) {
											List<Cliente> coincidencias = clientesDao.searchClientes(query);
											updateSugerenciasList(coincidencias);
										} else {
											lvSugerenciasClientes.setVisibility(View.GONE);
											clienteSeleccionadoId = -1;
										}
								}
							

							@Override
							public void afterTextChanged(Editable s) {}
						});
						
				// 2. Configurar el Listener de clic en la sugerencia
				/*lvSugerenciasClientes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
						});*/
						
				lvSugerenciasClientes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

									Cliente cliente = (Cliente) parent.getItemAtPosition(position);

									bloqueandoTexto = true; // ← ACTIVAR BLOQUEO

									etClienteNombre.setText(cliente.getNombre());

									bloqueandoTexto = false; // ← DESACTIVAR BLOQUEO

									clienteSeleccionadoId = cliente.getId();

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
				/*CuotasAdapter adapter = new CuotasAdapter(this, cuotas,this);
				lvCuotas.setAdapter(adapter);*/
				
				// ✅ ESTO ES LO QUE ARREGLA EL PROBLEMA DE LAS 4 CUOTAS
				//setListViewHeightBasedOnChildren(lvCuotas);

				// Si la lista está vacía, ocultar la ListView o mostrar un mensaje
				/*if (cuotas.isEmpty()) {
						lvCuotas.setVisibility(View.GONE);
					} else {
						lvCuotas.setVisibility(View.VISIBLE);
					}*/
			}

		
		/*private void guardarCredito() {
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
			}*/
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
		/*private void guardarCredito() {
				// 1. Validación y Extracción de datos del formulario
				String capitalStr = etCapital.getText().toString();
				String tasaStr = etTasaInteres.getText().toString();
				String fechaInicioStr = etFechaInicio.getText().toString();
				String nombreClienteIngresado = etClienteNombre.getText().toString().trim(); // <-- OBTENER NOMBRE DEL CAMPO
				Date fechaInicioObjeto;

				// --- VALIDACIONES INICIALES ---
				if (nombreClienteIngresado.isEmpty()) {
						Toast.makeText(this, "El nombre del cliente es obligatorio.", Toast.LENGTH_SHORT).show();
						return;
					}
				if (capitalStr.isEmpty()) {
						Toast.makeText(this, "Debe ingresar el capital.", Toast.LENGTH_SHORT).show();
						return;
					}

				// VALIDACIÓN Y CONVERSIÓN DE LA FECHA
				try {
						fechaInicioObjeto = DISPLAY_DATE_FORMAT.parse(fechaInicioStr);
					} catch (ParseException e) {
						Toast.makeText(this, "Error: El formato de fecha debe ser dd/MM/yyyy.", Toast.LENGTH_LONG).show();
						e.printStackTrace();
						return;
					}

				// =======================================================
				// 2. LÓGICA DE CLIENTE NUEVO / SELECCIONADO
				// =======================================================
				int clienteIdFinal = clienteSeleccionadoId; // Usamos el ID previamente seleccionado (si es -1, es nuevo)

				if (clienteIdFinal == -1) {
						// A. SI EL ID ES -1 (Cliente Nuevo o no Seleccionado de Sugerencias)
						Cliente nuevoCliente = new Cliente();
						nuevoCliente.setNombre(nombreClienteIngresado);
						nuevoCliente.setEstado(1); // Activo
						// === SOLUCIÓN CRUCIAL: ASIGNAR VALORES SEGUROS ===
						// Asignar valores por defecto para evitar NullPointerException al leer
						nuevoCliente.setCi("");         // O "N/A"
						nuevoCliente.setTelefono("");   // O "N/A"
						nuevoCliente.setDireccion("");  // O "N/A"
						nuevoCliente.setGarantia("");  // O "N/A"
						
						Log.d("CI ", "Cliente: " + nuevoCliente.getCi());
						
						// ===============================================
						//ClientesDao clientesDao = new ClientesDao(this);
						//long newRowId = clientesDao.insertCliente(nuevoCliente);
						ClientesDao clientesDao = new ClientesDao(this);
						long newRowId = clientesDao.insertCliente(nuevoCliente);

						if (newRowId > 0) {
								clienteIdFinal = (int) newRowId; // Asignar el nuevo ID
								clienteSeleccionadoId = clienteIdFinal;   // ← ← ← AGREGAR ESTA LÍNEA
								Toast.makeText(this, "Cliente nuevo ('" + nombreClienteIngresado + "') registrado.", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(this, "Error al registrar el nuevo cliente.", Toast.LENGTH_LONG).show();
								return; // Detener si falla la inserción del cliente
							}
					} 
				// Si clienteIdFinal NO es -1, se usa el ID del cliente ya existente/seleccionado.

				// 3. Obtener objetos seleccionados
				Plan planSeleccionado = (Plan) spPlan.getSelectedItem();

				// 4. Crear o actualizar objeto Credito
				if (creditoActual == null) {
						creditoActual = new Credito();
					}

				// Setear el ID del cliente (Nuevo o Existente)
				creditoActual.setClienteId(clienteIdFinal);
				creditoActual.setPlanId(planSeleccionado.getId());

				// Setear los datos financieros
				creditoActual.setCapital(Double.parseDouble(capitalStr));
				creditoActual.setInteresPorcentaje(Double.parseDouble(tasaStr));
				creditoActual.setFechaInicio(fechaInicioObjeto);

				// 5. Ejecución de la operación
				long resultado;
				if (creditoId == -1) {
						resultado = creditosDao.insertCreditoAndCuotas(creditoActual);
						Toast.makeText(this, "Crédito Creado!", Toast.LENGTH_SHORT).show();
					} else {
						resultado = creditosDao.updateCredito(creditoActual);
						Toast.makeText(this, "Crédito Actualizado!", Toast.LENGTH_SHORT).show();
					}

				if (resultado > 0) {
						if (creditoId == -1) {
								// MODO CREACIÓN: Redirigir a CuotasActivity

								// 1. Obtener el ID del crédito insertado (si es nuevo)
								// Ya que usamos 'resultado' para guardar el ID de la nueva fila (long),
								// y tu objeto creditoActual ya tiene el ID asignado dentro de insertCreditoAndCuotas,
								// podemos usar el ID del objeto.
								int nuevoCreditoId = creditoActual.getId(); 

								Intent intent = new Intent(this, CuotasActivity.class);
								// Usa la clave correcta que estableciste en CreditosActivity.java: "CREDITO_ID"
								intent.putExtra("CREDITO_ID", nuevoCreditoId); 

								startActivity(intent);

								// Cierra la Activity de Detalle/Creación
								finish(); 

							} else {
								// MODO EDICIÓN: Vuelve a la lista principal (comportamiento anterior)
								setResult(RESULT_OK);
								finish();
							}
					} else {
						Toast.makeText(this, "Error al guardar el crédito.", Toast.LENGTH_LONG).show();
					}
			}*/
		// Dentro de CreditoDetalleActivity.java

		private void guardarCredito() {
				// 1. Obtener datos
				String nombreClienteIngresado = etClienteNombre.getText().toString().trim();
				String capitalStr = etCapital.getText().toString().trim();
				String tasaStr = etTasaInteres.getText().toString().trim();
				String fechaInicioStr = etFechaInicio.getText().toString().trim();

				if (nombreClienteIngresado.isEmpty()) {
						Toast.makeText(this, "El nombre del cliente es obligatorio.", Toast.LENGTH_SHORT).show();
						return;
					}

				if (capitalStr.isEmpty()) {
						Toast.makeText(this, "Debe ingresar el capital.", Toast.LENGTH_SHORT).show();
						return;
					}

				Date fechaInicioObjeto;
				try {
						fechaInicioObjeto = DISPLAY_DATE_FORMAT.parse(fechaInicioStr);
					} catch (Exception e) {
						Toast.makeText(this, "Formato de fecha inválido (dd/MM/yyyy).", Toast.LENGTH_LONG).show();
						return;
					}

				// =====================================================
				// 2. CREAR CLIENTE SOLO SI NO EXISTE
				// =====================================================
				int clienteIdFinal = clienteSeleccionadoId;

				if (clienteIdFinal == -1) {
						Cliente nuevo = new Cliente();
						nuevo.setNombre(nombreClienteIngresado);
						nuevo.setEstado(1);
						nuevo.setCi("");
						nuevo.setTelefono("");
						nuevo.setDireccion("");
						nuevo.setGarantia("");

						ClientesDao clientesDao = new ClientesDao(this);
						long newRowId = clientesDao.insertCliente(nuevo);

						if (newRowId <= 0) {
								Toast.makeText(this, "Error al crear el cliente.", Toast.LENGTH_LONG).show();
								return;
							}

						clienteIdFinal = (int) newRowId;
						clienteSeleccionadoId = clienteIdFinal;

						Toast.makeText(this, "Cliente '" + nombreClienteIngresado + "' creado.", Toast.LENGTH_SHORT).show();
					}

				// =====================================================
				// 3. Guardar crédito
				// =====================================================

				Plan planSeleccionado = (Plan) spPlan.getSelectedItem();

				if (creditoActual == null) {
						creditoActual = new Credito();
					}

				creditoActual.setClienteId(clienteIdFinal);
				creditoActual.setPlanId(planSeleccionado.getId());
				creditoActual.setCapital(Double.parseDouble(capitalStr));
				creditoActual.setInteresPorcentaje(Double.parseDouble(tasaStr));
				creditoActual.setFechaInicio(fechaInicioObjeto);

				long resultado;

				if (creditoId == -1) {
						resultado = creditosDao.insertCreditoAndCuotas(creditoActual);
						Toast.makeText(this, "Crédito creado.", Toast.LENGTH_SHORT).show();
					} else {
						resultado = creditosDao.updateCredito(creditoActual);
						Toast.makeText(this, "Crédito actualizado.", Toast.LENGTH_SHORT).show();
					}

				if (resultado > 0) {
						int nuevoCreditoId = creditoActual.getId();

						Intent intent = new Intent(this, CuotasActivity.class);
						intent.putExtra("CREDITO_ID", nuevoCreditoId);
						startActivity(intent);
						finish();

					} else {
						Toast.makeText(this, "Error al guardar crédito.", Toast.LENGTH_LONG).show();
					}
			}
	}
