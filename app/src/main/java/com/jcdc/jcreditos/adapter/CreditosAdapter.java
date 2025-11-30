package com.jcdc.jcreditos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.jcdc.jcreditos.R; // Asegúrate de que R apunte a tu proyecto
import com.jcdc.jcreditos.model.Credito;
import java.util.List;

public class CreditosAdapter extends BaseAdapter {

		private Context context;
		private List<Credito> creditosList;
		private LayoutInflater inflater;

		public CreditosAdapter(Context context, List<Credito> creditosList) {
				this.context = context;
				this.creditosList = creditosList;
				this.inflater = LayoutInflater.from(context);
			}

		@Override
		public int getCount() {
				return creditosList.size();
			}

		@Override
		public Object getItem(int position) {
				return creditosList.get(position);
			}

		// Método crucial: Retorna el ID de la base de datos
		@Override
		public long getItemId(int position) {
				return creditosList.get(position).getId();
			}

		// Método para actualizar los datos (usado en loadCreditosList)
		public void updateData(List<Credito> nuevaLista) {
				this.creditosList = nuevaLista;
				notifyDataSetChanged();
			}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;

				if (convertView == null) {
						convertView = inflater.inflate(R.layout.item_credito, parent, false);
						holder = new ViewHolder();
						// Referenciar los TextViews del item_credito.xml
						holder.tvClienteNombre = convertView.findViewById(R.id.tv_cliente_nombre);
						holder.tvCreditoMonto = convertView.findViewById(R.id.tv_credito_monto);
						holder.tvPlanNombre = convertView.findViewById(R.id.tv_plan_nombre);
						holder.tvCreditoEstado = convertView.findViewById(R.id.tv_credito_estado);
						convertView.setTag(holder);
					} else {
						holder = (ViewHolder) convertView.getTag();
					}

				Credito credito = creditosList.get(position);

				// Asignación de datos utilizando los campos del JOIN (Cliente y Plan)
				holder.tvClienteNombre.setText(credito.getNombreCliente());
				// Formateo del monto (puedes usar NumberFormat si lo necesitas)
				holder.tvCreditoMonto.setText("Bs. " + String.format("%.2f", credito.getTotal()));
				holder.tvPlanNombre.setText("Plan: " + credito.getNombrePlan());
				// Aquí puedes usar un switch o if para mostrar el estado como texto amigable
				holder.tvCreditoEstado.setText(getEstadoTexto(credito.getEstado())); 

				return convertView;
			}

		// Clase interna para optimización de rendimiento (ViewHolder Pattern)
		static class ViewHolder {
				TextView tvClienteNombre;
				TextView tvCreditoMonto;
				TextView tvPlanNombre;
				TextView tvCreditoEstado;
			}

		// Función auxiliar para traducir el estado (asumiendo que ESTADO es INTEGER)
		private String getEstadoTexto(int estado) {
				switch (estado) {
						case 1:
							return "Vigente";
						case 2:
							return "Cancelado";
						case 3:
							return "Atrasado";
						default:
							return "Desconocido";
					}
			}
	}
