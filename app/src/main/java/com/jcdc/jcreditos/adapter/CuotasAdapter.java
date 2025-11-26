package com.jcdc.jcreditos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jcdc.jcreditos.R;
import com.jcdc.jcreditos.model.Cuota;

import java.util.List;
import java.util.Locale;

// Extender de BaseAdapter para ListView
public class CuotasAdapter extends BaseAdapter { 

		private final Context context;
		private final List<Cuota> listaCuotas;
		private final LayoutInflater inflater;

		public CuotasAdapter(Context context, List<Cuota> listaCuotas) {
				this.context = context;
				this.listaCuotas = listaCuotas;
				this.inflater = LayoutInflater.from(context);
			}

		@Override
		public int getCount() {
				return listaCuotas.size();
			}

		@Override
		public Object getItem(int position) {
				return listaCuotas.get(position);
			}

		@Override
		public long getItemId(int position) {
				return position;
			}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
				// Reutilización de vistas (ListView requiere esta optimización manual)
				if (convertView == null) {
						// Usamos el mismo layout item_cuota.xml
						convertView = inflater.inflate(R.layout.item_cuota, parent, false);
					}

				Cuota cuota = listaCuotas.get(position);

				// Obtenemos las referencias de los TextViews
				TextView tvNumero = convertView.findViewById(R.id.tv_cuota_numero);
				TextView tvFecha = convertView.findViewById(R.id.tv_cuota_fecha);
				TextView tvMonto = convertView.findViewById(R.id.tv_cuota_monto);

				// Llenamos los datos
				tvNumero.setText(String.valueOf(cuota.getNumeroCuota()));
				tvFecha.setText(cuota.getFechaPago());
				tvMonto.setText(String.format(Locale.getDefault(), "%.2f", cuota.getMontoCuota()));

				return convertView;
			}
	}
