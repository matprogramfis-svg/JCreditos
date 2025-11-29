package com.jcdc.jcreditos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jcdc.jcreditos.R;
import com.jcdc.jcreditos.model.Cliente;

import java.util.List;

public class ClientesAdapter extends BaseAdapter {

		private final Context context;
		private List<Cliente> listaClientes;
		private final LayoutInflater inflater;

		public ClientesAdapter(Context context, List<Cliente> listaClientes) {
				this.context = context;
				this.listaClientes = listaClientes;
				this.inflater = LayoutInflater.from(context);
			}

		// Necesario para actualizar la lista después de crear o editar un plan
		public void updateData(List<Cliente> nuevaLista) {
				this.listaClientes = nuevaLista;
				notifyDataSetChanged();
			}

		@Override
		public int getCount() {
				return listaClientes.size();
			}

		@Override
		public Object getItem(int position) {
				return listaClientes.get(position);
			}

		/*@Override
		 public long getItemId(int position) {
		 return listaPlanes.get(position).getId();
		 }*/

		@Override
		public long getItemId(int position) {
				Integer id = listaClientes.get(position).getId();
				return id != null ? id : -1;
			}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
						convertView = inflater.inflate(R.layout.item_cliente, parent, false);
					}

				Cliente cliente = listaClientes.get(position);

				TextView tvNombre = convertView.findViewById(R.id.tv_cliente_nombre);
				TextView tvDetalles = convertView.findViewById(R.id.tv_cliente_detalles);

				tvNombre.setText(cliente.getNombre());

				// Generar la cadena de detalles
				/*String salto = (plan.getSaltoDomingo() == 1) ? " (Salta Domingo)" : " (No Salta)";
				 String detalles = plan.getCuotasTotales() + " cuotas" + salto + 
				 " | " + plan.getMetodoAmortizacion();

				 tvDetalles.setText(detalles);*/

				return convertView;
			}
	}
