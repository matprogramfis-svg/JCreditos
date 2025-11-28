package com.jcdc.jcreditos.ui;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.jcdc.jcreditos.*;
import com.jcdc.jcreditos.dao.*;
import com.jcdc.jcreditos.model.*;

public class PlanDetalleActivity extends Activity {

		private PlanesDao planesDao;
		private int planId = -1;

		private EditText etNombre, etFrecuencia, etCuotas, etMeses;
		private Spinner spTipo;
		private CheckBox cbSalto;
		private Button btnGuardar, btnEliminar;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_plan_detalle);

				planesDao = new PlanesDao(this);

				etNombre = findViewById(R.id.et_plan_nombre);
				//etInteres = findViewById(R.id.et_plan_interes);
				etFrecuencia = findViewById(R.id.et_plan_frecuencia);
				etCuotas = findViewById(R.id.et_plan_cuotas);
				etMeses = findViewById(R.id.et_plan_num_meses);
				spTipo = findViewById(R.id.sp_plan_tipo);
				cbSalto = findViewById(R.id.chk_plan_salto_domingo);
				btnGuardar = findViewById(R.id.btn_plan_guardar);
				btnEliminar = findViewById(R.id.btn_plan_eliminar);

				// Spinner tipos (opcional, sirve como etiqueta / ayuda al usuario)
				String[] tipos = {"DIARIO", "SEMANAL", "QUINCENAL", "MENSUAL"};
				ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
																  android.R.layout.simple_spinner_item, tipos);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spTipo.setAdapter(adapter);

				// Si se abrió para editar
				planId = getIntent().getIntExtra("PLAN_ID", -1);
				if (planId != -1) {
						loadPlanData(planId);
						btnEliminar.setVisibility(View.VISIBLE);
						btnGuardar.setText("ACTUALIZAR PLAN");
					} else {
						btnEliminar.setVisibility(View.GONE);
					}

				btnGuardar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) { savePlan(); }
						});

				btnEliminar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									if (planId != -1) {
											new AlertDialog.Builder(PlanDetalleActivity.this)
												.setTitle("Eliminar Plan")
												.setMessage("¿Seguro que deseas eliminar este plan?")
												.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog, int which) {
																planesDao.deletePlan(planId);
																setResult(RESULT_OK);
																finish();
															}
													})
												.setNegativeButton("No", null)
												.show();
										}
								}
						});
			}

		private void loadPlanData(int id) {
				Plan p = planesDao.getPlanById(id);
				if (p == null) return;

				etNombre.setText(p.getNombre());
				//etInteres.setText(String.valueOf(p.getInteres())); // entero
				etFrecuencia.setText(String.valueOf(p.getFrecuencia()));
				etCuotas.setText(String.valueOf(p.getCuotasTotales()));
				etMeses.setText(String.valueOf(p.getNumeroMeses()));
				cbSalto.setChecked(p.getSaltoDomingo() == 1);

				// Seleccionar tipo en spinner si coincide
				for (int i = 0; i < spTipo.getCount(); i++) {
						if (spTipo.getItemAtPosition(i).toString().equalsIgnoreCase(p.getTipo())) {
								spTipo.setSelection(i);
								break;
							}
					}
			}

		// COMPLETAMENTE SIN INTERES
		private void savePlan() {

				if (etNombre.getText().toString().trim().isEmpty() ||
					etFrecuencia.getText().toString().trim().isEmpty() ||
					etCuotas.getText().toString().trim().isEmpty() ||
					etMeses.getText().toString().trim().isEmpty()) {

						Toast.makeText(this, "Faltan campos obligatorios.", Toast.LENGTH_SHORT).show();
						return;
					}

				int frecuencia = Integer.parseInt(etFrecuencia.getText().toString().trim());
				int cuotas = Integer.parseInt(etCuotas.getText().toString().trim());
				int meses = Integer.parseInt(etMeses.getText().toString().trim());
				String tipo = spTipo.getSelectedItem().toString();

				Plan plan = new Plan();
				plan.setNombre(etNombre.getText().toString().trim());
				plan.setFrecuencia(frecuencia);
				plan.setTipo(tipo);
				plan.setCuotasTotales(cuotas);
				plan.setNumeroMeses(meses);
				plan.setSaltoDomingo(cbSalto.isChecked() ? 1 : 0);

				long res;
				if (planId == -1) {
						res = planesDao.insertPlan(plan);
						if (res > 0) plan.setId((int) res);
					} else {
						plan.setId(planId);
						res = planesDao.updatePlan(plan);
					}

				if (res > 0) Toast.makeText(this, "Guardado correctamente.", Toast.LENGTH_SHORT).show();
				else Toast.makeText(this, "Error al guardar.", Toast.LENGTH_SHORT).show();

				setResult(RESULT_OK);
				finish();
			}

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
							Intent back = new Intent(this,PlanesActivity.class);
							startActivity(back);
							return true;
							
					}
			
				
				return super.onOptionsItemSelected(item);
			}
			
	}
