package com.jcdc.jcreditos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jcdc.jcreditos.R;
import com.jcdc.jcreditos.model.Plan;

import java.util.List;

public class PlanesAdapter extends BaseAdapter {

		private final Context context;
		private List<Plan> listaPlanes;
		private final LayoutInflater inflater;

		public PlanesAdapter(Context context, List<Plan> listaPlanes) {
				this.context = context;
				this.listaPlanes = listaPlanes;
				this.inflater = LayoutInflater.from(context);
			}

		// Necesario para actualizar la lista después de crear o editar un plan
		public void updateData(List<Plan> nuevaLista) {
				this.listaPlanes = nuevaLista;
				notifyDataSetChanged();
			}

		@Override
		public int getCount() {
				return listaPlanes.size();
			}

		@Override
		public Object getItem(int position) {
				return listaPlanes.get(position);
			}

		/*@Override
		public long getItemId(int position) {
				return listaPlanes.get(position).getId();
			}*/
			
		@Override
		public long getItemId(int position) {
				Integer id = listaPlanes.get(position).getId();
				return id != null ? id : -1;
			}
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
						convertView = inflater.inflate(R.layout.item_plan, parent, false);
					}

				Plan plan = listaPlanes.get(position);

				TextView tvNombre = convertView.findViewById(R.id.tv_plan_nombre);
				TextView tvDetalles = convertView.findViewById(R.id.tv_plan_detalles);

				tvNombre.setText(plan.getNombre());

				// Generar la cadena de detalles
				/*String salto = (plan.getSaltoDomingo() == 1) ? " (Salta Domingo)" : " (No Salta)";
				String detalles = plan.getCuotasTotales() + " cuotas" + salto + 
					" | " + plan.getMetodoAmortizacion();

				tvDetalles.setText(detalles);*/

				return convertView;
			}
	}
