package com.jcdc.jcreditos;

import android.app.Activity; // CAMBIO CLAVE: Usamos Activity base
import android.os.Bundle;
import android.widget.ListView; // Usamos ListView
import android.widget.TextView;

// NO necesitamos los imports de androidx.recyclerview o androidx.appcompat

import com.jcdc.jcreditos.adapter.CuotasAdapter; 
import com.jcdc.jcreditos.dao.CreditosDao;
import com.jcdc.jcreditos.dao.CuotasDao;
import com.jcdc.jcreditos.model.Credito;
import com.jcdc.jcreditos.model.Cuota;

import java.util.List;
import java.util.Locale;

// CAMBIO CLAVE: Extender de Activity
public class CreditoDetalleActivity extends Activity { 

		private ListView listView; // CAMBIO CLAVE: Usamos ListView
		private TextView tvSaldo;
		private CuotasDao cuotasDao;
		private CreditosDao creditosDao;

		private int creditoId = 1; 

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_credito_detalle); 

				// 1. Inicializar DAOs y Vistas
				cuotasDao = new CuotasDao(this);
				creditosDao = new CreditosDao(this);

				tvSaldo = findViewById(R.id.tv_saldo_total); 
				listView = findViewById(R.id.listview_cuotas); // CAMBIO CLAVE: Referenciamos el ListView

				loadDetalleCredito(creditoId);
			}

		private void loadDetalleCredito(int id) {
				// A. Cargar datos del Crédito... (Lógica de TV Saldo es la misma)
				Credito credito = creditosDao.getCreditoById(id);
				if (credito != null) {
						double saldo = credito.getCapital() + credito.getInteresMonto();
						tvSaldo.setText(String.format(Locale.getDefault(), "%.2f", saldo));
					}

				// B. Cargar la Tarjeta de Cobro (Cuotas)

				// 1. Obtener la lista de cuotas de la BD
				List<Cuota> listaCuotas = cuotasDao.getCuotasByCreditoId(id);

				// 2. Crear y enlazar el Adapter (Pasamos el Contexto y la lista)
				CuotasAdapter adapter = new CuotasAdapter(this, listaCuotas);
				listView.setAdapter(adapter); // CAMBIO CLAVE: Asignar al ListView
			}
	}
