package com.jcdc.jcreditos.adapter;

import android.content.*;
import android.view.*;
import android.widget.*;
import com.jcdc.jcreditos.*;
import com.jcdc.jcreditos.dao.*;
import com.jcdc.jcreditos.model.*;
import java.util.*;

public class CreditosAdapter extends BaseAdapter {

		private Context context;
		private List<Credito> creditosList;
		private LayoutInflater inflater;
		private CreditosDao creditosDao; // 💡 Referencia al DAO

		public CreditosAdapter(Context context, List<Credito> creditosList, CreditosDao creditosDao) {
				this.context = context;
				this.creditosList = creditosList;
				this.inflater = LayoutInflater.from(context);
				this.creditosDao = creditosDao; // AHORA SÍ funciona
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
				//holder.tvClienteNombre.setText(credito.getNombreCliente());
				// Formateo del monto (puedes usar NumberFormat si lo necesitas)
				//holder.tvCreditoMonto.setText("Bs. " + String.format("%.2f", credito.getTotal()));
				
				// =========================================================
				// 🎯 CAMBIO 1: ID del Crédito junto al Nombre
				// =========================================================
				String nombreConId = credito.getNombreCliente() + " - " + credito.getId();
				holder.tvClienteNombre.setText(nombreConId);

				// =========================================================
				// 🎯 CAMBIO 2: Mostrar Saldo Restante
				// =========================================================
				double saldoRestante = 0.0;
				// ✅ Usamos el DAO para obtener el saldo restante
				if (creditosDao != null) {
						// **NOTA:** Asumo que existe el método calcularSaldoRestante(long id) en tu DAO.
						// Si no existe, deberás crearlo en CreditosDao.java
						saldoRestante = creditosDao.calcularSaldoRestante(credito.getId());
					} else {
						// Fallback si el DAO es nulo
						saldoRestante = credito.getTotal(); 
					}

				// Mostrar el saldo restante
				holder.tvCreditoMonto.setText("Bs. " + String.format("%.2f", saldoRestante));
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
