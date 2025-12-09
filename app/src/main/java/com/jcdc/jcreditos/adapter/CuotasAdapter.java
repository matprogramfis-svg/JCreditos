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
import java.util.Date;

import com.jcdc.jcreditos.dao.CuotasDao; // <-- AGREGAR ESTO
// Importar la interfaz
import com.jcdc.jcreditos.adapter.OnCuotaActionListener;
// ... (tus importaciones existentes)
import java.util.HashMap; // 🆕 NUEVO
import java.util.Map;     // 🆕 NUEVO


public class CuotasAdapter extends BaseAdapter {

		private Context context;
		private List<Cuota> cuotasList;
		private LayoutInflater inflater;
		private CuotasDao cuotasDao; // <-- AGREGAR ESTO
		private OnCuotaActionListener listener; // <-- AGREGAR ESTO
		
		// 1. Variable de estado en la clase CuotasAdapter
		private boolean isEditMode = false; // Por defecto: modo normal

		// Formato de fecha que usaste para mostrar (ej: 30/11/2025)
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US); 

		// 🆕 1. ESTRUCTURA PARA RASTREAR SELECCIÓN 🆕
		// Usamos un mapa para saber qué cuotas están seleccionadas (Cuota ID -> Boolean)
		private Map<Long, Boolean> selectedCuotas = new HashMap<>();
			
		// ✅ CONSTRUCTOR CORREGIDO: Acepta el listener
		public CuotasAdapter(Context context, List<Cuota> cuotasList, OnCuotaActionListener listener) {
				this.context = context;
				this.cuotasList = cuotasList;
				this.inflater = LayoutInflater.from(context);
				this.cuotasDao = new CuotasDao(context); // Inicializar DAO
				this.listener = listener; // Guardar el listener
			}
			
		// 🆕 MÉTODO CLAVE: Obtener el mapa de seleccionadas 🆕
		public Map<Long, Boolean> getSelectedCuotas() {
				return selectedCuotas;
			}
			
		// Dentro de CuotasAdapter.java

		public void updateData(List<Cuota> nuevaLista) {
				this.cuotasList.clear();
				this.cuotasList.addAll(nuevaLista);
				notifyDataSetChanged();
			}
			
		// 2. Método público para cambiar el estado desde la Activity
		public void setEditMode(boolean isEditMode) {
				this.isEditMode = isEditMode;
				notifyDataSetChanged(); // Forzar a la lista a redibujarse
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
				
				// =========================================================================
				// 🔥 LÓGICA DE ESTADO: Pagada, Vencida, o Pendiente
				// =========================================================================
				// El estado y color de texto deben reflejar si la cuota está seleccionada
				boolean isSelected = selectedCuotas.containsKey(cuota.getId());
				
				if (cuota.getPagada() == 1) {
						// 5. Caso: PAGADA
						holder.tvEstado.setText("Pagada");
						holder.tvEstado.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
						holder.btnPagar.setImageResource(android.R.drawable.ic_menu_edit); // Icono para 'ver' o 'editar pago'
						holder.btnPagar.setEnabled(this.isEditMode); // Deshabilitar el botón si ya está pagada
					} else {

						// 6. Caso: PENDIENTE o VENCIDA (No está pagada)
						Date fechaActual = new Date();
						Date fechaVencimiento = cuota.getFechaPago();

						// Comprobar si la fecha de vencimiento ya pasó
						if (fechaVencimiento != null && fechaVencimiento.before(fechaActual)) {
								// ESTADO VENCIDA
								holder.tvEstado.setText("Vencida");
								// Color diferente para indicar atraso (ej. naranja o marrón)
								holder.tvEstado.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
							} else {
								// ESTADO PENDIENTE
								holder.tvEstado.setText("Pendiente");
								holder.tvEstado.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
							}

						// El botón de pagar siempre debe estar habilitado si la cuota no está pagada
						holder.btnPagar.setImageResource(android.R.drawable.ic_input_add); // Icono para 'Pagar'
						holder.btnPagar.setEnabled(true);
						
							// 🆕 CAMBIO VISUAL SI ESTÁ SELECCIONADA 🆕
							if (isSelected) {
									// Si está seleccionada, cambia el icono a una marca de verificación (check)
									holder.btnPagar.setImageResource(android.R.drawable.checkbox_on_background);
									// Opcional: Cambiar el color de fondo de toda la fila para resaltar
									convertView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
								} else {
									// Si no está seleccionada, usa el icono normal (el '+' o input_add)
									holder.btnPagar.setImageResource(android.R.drawable.ic_input_add);
									// Restablecer el color de fondo
									convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
								}
					}
					
				// 7. Listener para el botón "Pagar"
				/*holder.btnPagar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									// Aquí se implementaría la lógica para registrar el pago.
									// Por ahora, solo mostramos un Toast. La lógica de DAO va después.
									
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
						});*/
						// ***
				holder.btnPagar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {

									// SI YA ESTÁ PAGADA → modo edición / reversión
									if (cuota.getPagada() == 1) {
											if (listener != null) {
													listener.onCuotaEdit(cuota);
												}
											return; // salir para NO ejecutar la lógica de pago
										}

									// SI ESTÁ PENDIENTE → pagar normal
									/*int filasAfectadas = cuotasDao.markCuotaAsPaid(cuota.getId());

									if (filasAfectadas > 0) {
											Toast.makeText(context, 
														   "Cuota #" + cuota.getNumeroCuota() + " Pagada!", 
														   Toast.LENGTH_SHORT).show();

											if (listener != null) {
													listener.onCuotaPaid(cuota.getId());
												}

										} else {
											Toast.makeText(context, 
														   "Error al marcar el pago.", 
														   Toast.LENGTH_SHORT).show();
										}*/
										
									// 🔥 LÓGICA DE SELECCIÓN/DESELECCIÓN 🔥
									if (selectedCuotas.containsKey(cuota.getId())) {
											// DESELECCIONAR
											selectedCuotas.remove(cuota.getId());
											// Notificar a la Activity para RESTAR el monto
											if (listener != null) {
													listener.onCuotaSelectionChange(-cuota.getMontoCuota()); // Monto negativo para restar
												}
										} else {
											// SELECCIONAR
											selectedCuotas.put(cuota.getId(), true);
											// Notificar a la Activity para SUMAR el monto
											if (listener != null) {
													listener.onCuotaSelectionChange(cuota.getMontoCuota()); // Monto positivo para sumar
												}
										}

									notifyDataSetChanged(); // Redibujar la lista para actualizar el icono/color
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
