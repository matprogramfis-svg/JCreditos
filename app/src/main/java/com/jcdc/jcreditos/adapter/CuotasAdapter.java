package com.jcdc.jcreditos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jcdc.jcreditos.R;
import com.jcdc.jcreditos.model.Cuota;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.jcdc.jcreditos.dao.CuotasDao; // <-- AGREGAR ESTO
// Importar la interfaz
import com.jcdc.jcreditos.adapter.OnCuotaActionListener;

public class CuotasAdapter extends BaseAdapter {

		private Context context;
		private List<Cuota> cuotasList;
		private LayoutInflater inflater;
		private CuotasDao cuotasDao; // <-- AGREGAR ESTO
		private OnCuotaActionListener listener; // <-- AGREGAR ESTO
		
		// 💡 INTERFAZ DE COMUNICACIÓN (Debe estar dentro de la clase)
		/*public interface OnCuotaActionListener {
				void onCuotaPaid(int cuotaId);
			}*/

		// Formato de fecha que usaste para mostrar (ej: 30/11/2025)
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US); 

		/*public CuotasAdapter(Context context, List<Cuota> cuotasList) {
				this.context = context;
				this.cuotasList = cuotasList;
				this.inflater = LayoutInflater.from(context);
			}*/
			
		// ✅ CONSTRUCTOR CORREGIDO: Acepta el listener
		public CuotasAdapter(Context context, List<Cuota> cuotasList, OnCuotaActionListener listener) {
				this.context = context;
				this.cuotasList = cuotasList;
				this.inflater = LayoutInflater.from(context);
				this.cuotasDao = new CuotasDao(context); // Inicializar DAO
				this.listener = listener; // Guardar el listener
			}
			
		// Dentro de CuotasAdapter.java

		public void updateData(List<Cuota> nuevaLista) {
				this.cuotasList.clear();
				this.cuotasList.addAll(nuevaLista);
				notifyDataSetChanged();
			}

		@Override
		public int getCount() {
				return cuotasList.size();
			}

		@Override
		public Object getItem(int position) {
				return cuotasList.get(position);
			}

		@Override
		public long getItemId(int position) {
				return cuotasList.get(position).getId();
			}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;

				if (convertView == null) {
						// 1. Inflar el layout item_cuota.xml
						convertView = inflater.inflate(R.layout.item_cuota, parent, false);

						// 2. Inicializar el ViewHolder
						holder = new ViewHolder();
						holder.tvNumero = convertView.findViewById(R.id.tv_cuota_numero);
						holder.tvFecha = convertView.findViewById(R.id.tv_cuota_fecha);
						holder.tvMonto = convertView.findViewById(R.id.tv_cuota_monto);
						holder.tvEstado = convertView.findViewById(R.id.tv_cuota_estado);
						holder.btnPagar = convertView.findViewById(R.id.btn_pagar_cuota);

						convertView.setTag(holder);
					} else {
						holder = (ViewHolder) convertView.getTag();
					}

				// 3. Obtener el objeto Cuota actual
				final Cuota cuota = cuotasList.get(position);

				// 4. Asignar datos a las vistas
				holder.tvNumero.setText("# " + cuota.getNumeroCuota());
				// ✅ CORRECCIÓN CLAVE: Verificar si la fecha es nula
				if (cuota.getFechaPago() != null) {
						holder.tvFecha.setText(dateFormat.format(cuota.getFechaPago()));
					} else {
						// Proporcionar un texto de reserva en caso de que la fecha sea nula
						holder.tvFecha.setText("Fecha no asignada");
					}
				//holder.tvFecha.setText(dateFormat.format(cuota.getFechaPago()));
				holder.tvMonto.setText("Bs. " + String.format("%.2f", cuota.getMontoCuota()));

				// 5. Lógica de Estado (0=Pendiente, 1=Pagada)
				if (cuota.getPagada() == 1) {
						holder.tvEstado.setText("Pagada");
						holder.tvEstado.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
						holder.btnPagar.setImageResource(android.R.drawable.ic_menu_edit); // Icono para 'ver' o 'editar pago'
						holder.btnPagar.setEnabled(false); // Deshabilitar el botón si ya está pagada
					} else {
						holder.tvEstado.setText("Pendiente");
						holder.tvEstado.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
						holder.btnPagar.setImageResource(android.R.drawable.ic_input_add); // Icono para 'Pagar'
						holder.btnPagar.setEnabled(true);
					}

				// 6. Listener para el botón "Pagar"
				holder.btnPagar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									// Aquí se implementaría la lógica para registrar el pago.
									// Por ahora, solo mostramos un Toast. La lógica de DAO va después.
									/*Toast.makeText(context, 
												   "Pagar Cuota #" + cuota.getNumeroCuota() + " (ID: " + cuota.getId() + ")", 
												   Toast.LENGTH_SHORT).show();*/
									if (cuota.getPagada() == 0) { // Solo si está pendiente

											// 1. Llamar al DAO para actualizar la base de datos
											int filasAfectadas = cuotasDao.markCuotaAsPaid(cuota.getId()); // <-- Debes tener este método en CuotasDao

											if (filasAfectadas > 0) {
													Toast.makeText(context, "Cuota #" + cuota.getNumeroCuota() + " Pagada!", Toast.LENGTH_SHORT).show();

													// 2. Notificar a la Activity para recargar la lista
													if (listener != null) {
															listener.onCuotaPaid(cuota.getId());
														}
												} else {
													Toast.makeText(context, "Error al marcar el pago.", Toast.LENGTH_SHORT).show();
												}
											}
								}
						});

				return convertView;
			}

		// Patrón ViewHolder para optimizar el rendimiento de la ListView
		static class ViewHolder {
				TextView tvNumero;
				TextView tvFecha;
				TextView tvMonto;
				TextView tvEstado;
				ImageButton btnPagar;
			}
	}
