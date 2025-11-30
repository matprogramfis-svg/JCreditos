package com.jcdc.jcreditos;

import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.os.*;
import android.util.*;
import com.jcdc.jcreditos.dao.*;
import com.jcdc.jcreditos.db.*;
import com.jcdc.jcreditos.model.*;
import com.jcdc.jcreditos.ui.*;
import java.util.*;
import android.view.View.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity implements OnClickListener
	{
		//Button btnPlan;

		@Override
		public void onClick(View btn)
			{
				switch(btn.getId())
					{
						case R.id.btn_plan:	
							Intent nuevo = new Intent(this,PlanesActivity.class);
							startActivity(nuevo);	
							break;
						case R.id.btn_cliente:	
							Intent clie = new Intent(this,ClientesActivity.class);
							startActivity(clie);	
							break;
						case R.id.btn_credito:	
							Intent crd = new Intent(this,CreditosActivity.class);
							//plan.putExtra("stIdCr","");
							startActivity(crd);	
							break;
					}
				// Iniciar la Activity de gestión de planes
               /* Intent intent = new Intent(this, PlanesActivity.class);
                // Usamos startActivityForResult para saber cuándo volver y recargar el Spinner
                startActivityForResult(intent, 2);*/
			}
		
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		findViewById(R.id.btn_plan).setOnClickListener(this);
		findViewById(R.id.btn_cliente).setOnClickListener(this);
		findViewById(R.id.btn_credito).setOnClickListener(this);
		// 🔥 LLAMAR AQUÍ AL DUMP
      /*  DbHelper helper = new DbHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        dumpTable(db, DatabaseContract.Estados.TABLE);   // Ejemplo
        dumpTable(db, DatabaseContract.Creditos.TABLE); // Otro ejemplo
        dumpTable(db, DatabaseContract.Cuotas.TABLE);    // Otro ejemplo
		dumpTable(db, DatabaseContract.Planes.TABLE);
        db.close();*/
		//runFullIntegrationTest(this);
		//pruebaDao();
		//runTestAndDisplayCredit(this);
    }
	// ***
		public void dumpTable(SQLiteDatabase db, String tableName) {
				Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);

				if (cursor.moveToFirst()) {
						do {
								StringBuilder row = new StringBuilder();

								for (int i = 0; i < cursor.getColumnCount(); i++) {
										row.append(cursor.getColumnName(i))
											.append("=")
											.append(cursor.getString(i))
											.append(" | ");
									}

								Log.d("DB_DUMP_" + tableName, row.toString());
							} while (cursor.moveToNext());
					} else {
						Log.d("DB_DUMP_" + tableName, "La tabla está vacía.");
					}

				cursor.close();
			}
	// ***
	private void pruebaDao()
	{
		EstadosDao dao = new EstadosDao(this);

		// Prueba 1 – obtener todos los estados
		List<String> lista = dao.getAllEstados();
		for (int i = 0; i < lista.size(); i++) {
				Log.d("DAO_ESTADOS", "Estado: " + lista.get(i));
			}

		// Prueba 2 – descripción por id
		String desc = dao.getDescripcionById(2);
		Log.d("DAO_ESTADOS", "Estado con id=2 es: " + desc);
	}
	// ***
		// Asegúrate de importar Log de Android
		//import android.util.Log;
// ... y todos tus POJOs y DAOs

		/*private void runFullIntegrationTest(Context context) {
				Log.d("TEST_DB", "--- INICIANDO PRUEBA DE INTEGRACIÓN DE CRÉDITO ---");

				// Inicializa DAOs
				ClientesDao clientesDao = new ClientesDao(context);
				PlanesDao planesDao = new PlanesDao(context);
				CreditosDao creditosDao = new CreditosDao(context);
				CuotasDao cuotasDao = new CuotasDao(context);

				// El DbHelper es necesario para la inserción de Plan de prueba
				DbHelper dbHelper = new DbHelper(context);

				// --- PASO 1: Insertar Cliente de Prueba ---
				Cliente nuevoCliente = new Cliente();
				nuevoCliente.setNombre("Carla Testeando");
				nuevoCliente.setCi("8765432");
				nuevoCliente.setEstado(1); // Activo
				long clienteId = clientesDao.insertCliente(nuevoCliente);

				if (clienteId < 0) {
						Log.e("TEST_DB", "FALLO: No se pudo insertar el cliente.");
						return;
					}
				Log.d("TEST_DB", "Cliente de prueba insertado. ID: " + clienteId);

				// --- PASO 2: Insertar Plan de Prueba (Diario con Salto Domingo) ---
				// Usamos el writable DB directamente para insertar un Plan para la prueba
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				ContentValues planValues = new ContentValues();
				planValues.put(DatabaseContract.Planes.NOMBRE, "Diario con Salto Domingo");
				planValues.put(DatabaseContract.Planes.TIPO, "DIARIO");
				planValues.put(DatabaseContract.Planes.FRECUENCIA, 1); // Cada 1 día
				planValues.put(DatabaseContract.Planes.CUOTAS_TOTALES, 5); // Solo 5 cuotas para probar
				planValues.put(DatabaseContract.Planes.SALTO_DOMINGO, 1); // 1 = SALTA DOMINGO
				planValues.put(DatabaseContract.Planes.METODO_AMORTIZACION, "FRANCES");
				long planId = db.insert(DatabaseContract.Planes.TABLE, null, planValues);
				db.close();

				if (planId < 0) {
						Log.e("TEST_DB", "FALLO: No se pudo insertar el plan.");
						return;
					}
				Log.d("TEST_DB", "Plan de prueba insertado. ID: " + planId);

				// --- PASO 3: Insertar el Crédito y sus Cuotas ---
				// La fecha de hoy es Miércoles 2025-11-26
				String fechaInicio = "2025-11-26"; 

				Credito nuevoCredito = new Credito();
				nuevoCredito.setClienteId((int) clienteId);
				nuevoCredito.setPlanId((int) planId);
				nuevoCredito.setCapital(500.0);
				nuevoCredito.setInteresPorcentaje(10.0);
				nuevoCredito.setInteresMonto(50.0);
				nuevoCredito.setTotal(550.0);
				nuevoCredito.setFechaInicio(fechaInicio);

				long nuevoCreditoId = creditosDao.insertCreditoAndCuotas(nuevoCredito);

				if (nuevoCreditoId > 0) {
						Log.d("TEST_DB", "🎉 ÉXITO: Crédito creado con éxito! ID: " + nuevoCreditoId);

						// --- PASO 4: Verificar Cuotas Generadas ---
						List<Cuota> cuotas = cuotasDao.getCuotasByCreditoId((int) nuevoCreditoId);
						Log.d("TEST_DB", "Cuotas generadas: " + cuotas.size());

						// La prueba del salto de domingo es crucial: 
						// 2025-11-26 (Miércoles)
						// 2025-11-27 (Jueves)
						// 2025-11-28 (Viernes)
						// 2025-11-29 (Sábado)
						// 2025-11-30 (Domingo) -> DEBE SALTAR AL 2025-12-01 (Lunes)

						String expectedDates[] = {"2025-11-26", "2025-11-27", "2025-11-28", "2025-11-29", "2025-12-01"};

						for (int i = 0; i < cuotas.size(); i++) {
								Cuota c = cuotas.get(i);
								Log.d("TEST_DB", 
									  String.format("Cuota #%d: Fecha Pago: %s | Esperado: %s", 
													c.getNumeroCuota(), c.getFechaPago(), expectedDates[i])
									  );

								if (!c.getFechaPago().equals(expectedDates[i])) {
										Log.e("TEST_DB", "FALLO DE LÓGICA DE FECHA: La cuota " + c.getNumeroCuota() + 
											  " no generó la fecha esperada.");
									}
							}

					} else {
						Log.e("TEST_DB", "❌ FALLO al insertar el crédito o las cuotas. Revise Logcat para errores de ParseException.");
					}
			}*/
		// ***

// Usaremos un Context de una Activity real para iniciar la nueva Activity
		/*public void runTestAndDisplayCredit(Context context) {
				Log.d("TEST_FINAL", "--- INICIANDO PRUEBA DE VISUALIZACIÓN DE CRÉDITO ---");

				// Inicializa DAOs
				ClientesDao clientesDao = new ClientesDao(context);
				CreditosDao creditosDao = new CreditosDao(context);
				DbHelper dbHelper = new DbHelper(context);

				// --- 1. DATOS DE LA UI (Vicente M, 400 Capital, 20 Interés, Plan '24 días') ---
				final double CAPITAL = 400.0;
				final double INTERES_MONTO = 20.0;
				final String FECHA_INICIO = "2025-11-26"; 
				final int TOTAL_CUOTAS = 24;
				final String NOMBRE_PLAN = "24 días";

				// Lógica: 400 + 20 = 420 Total
				double totalCredito = CAPITAL + INTERES_MONTO;

				// --- 2. PREPARACIÓN DE ENTIDADES (Cliente y Plan) ---

				// Insertar Cliente "Vicente M"
				Cliente clienteVicente = new Cliente();
				clienteVicente.setNombre("Vicente M");
				long clienteId = clientesDao.insertCliente(clienteVicente); 

				// Insertar Plan "24 días" (Diario, No Salta Domingo, 24 Cuotas)
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				ContentValues planValues = new ContentValues();
				planValues.put(DatabaseContract.Planes.NOMBRE, NOMBRE_PLAN);
				planValues.put(DatabaseContract.Planes.TIPO, "DIARIO");
				planValues.put(DatabaseContract.Planes.FRECUENCIA, 1);
				planValues.put(DatabaseContract.Planes.CUOTAS_TOTALES, TOTAL_CUOTAS);
				planValues.put(DatabaseContract.Planes.SALTO_DOMINGO, 0); // 0 = NO salta
				planValues.put(DatabaseContract.Planes.METODO_AMORTIZACION, "SIMPLE");
				long planId = db.insert(DatabaseContract.Planes.TABLE, null, planValues);
				db.close();

				// --- 3. CREAR Y GUARDAR EL CRÉDITO ---
				Credito nuevoCredito = new Credito();
				nuevoCredito.setClienteId((int) clienteId);
				nuevoCredito.setPlanId((int) planId);
				nuevoCredito.setCapital(CAPITAL);
				nuevoCredito.setInteresMonto(INTERES_MONTO);
				nuevoCredito.setTotal(totalCredito);
				nuevoCredito.setFechaInicio(FECHA_INICIO);

				long nuevoCreditoId = creditosDao.insertCreditoAndCuotas(nuevoCredito);

				// --- 4. VERIFICACIÓN Y LANZAMIENTO DE LA ACTIVITY ---
				if (nuevoCreditoId > 0) {
						Log.d("TEST_FINAL", "🎉 Crédito creado con ID: " + nuevoCreditoId + ". Lanzando Activity de Detalle.");

						// Crear el Intent para iniciar la Activity de Visualización
						Intent intent = new Intent(context, CreditoDetalleActivity.class);

						// **Clave:** Pasar el ID del crédito a la nueva Activity
						intent.putExtra("CREDITO_ID", (int) nuevoCreditoId); 

						context.startActivity(intent);

					} else {
						Log.e("TEST_FINAL", "❌ FALLO al guardar el crédito o generar las cuotas.");
					}
			}*/
			
}
