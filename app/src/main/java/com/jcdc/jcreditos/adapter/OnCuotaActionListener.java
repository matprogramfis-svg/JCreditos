package com.jcdc.jcreditos.adapter;

import com.jcdc.jcreditos.model.Cuota;

/**
 * Interfaz para manejar las interacciones del usuario en el CuotasAdapter
 * y notificar a la Activity principal.
 */
public interface OnCuotaActionListener {

		/**
		 * Se llama cuando se marca una cuota como Pagada (Acción original).
		 * @param cuotaId ID de la cuota pagada.
		 */
		void onCuotaPaid(int cuotaId);

		/**
		 * 🔥 NUEVA ACCIÓN: Se llama cuando se pulsa el botón de una cuota ya Pagada.
		 * Esto dispara el diálogo de edición/reversión en la Activity.
		 * @param cuota Objeto Cuota completo.
		 */
		void onCuotaEdit(Cuota cuota);
	}
