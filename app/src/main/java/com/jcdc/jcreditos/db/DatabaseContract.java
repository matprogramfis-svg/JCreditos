package com.jcdc.jcreditos.db;

public final class DatabaseContract {

		private DatabaseContract() {} // evitar instancias

		// Nombre y versión de la base
		public static final String DATABASE_NAME = "jcreditos.db";
		public static final int DATABASE_VERSION = 1;

		// ============================
		// TABLA CLIENTES
		// ============================
		public static final class Clientes {
			// ... campos sin cambios
			public static final String TABLE = "clientes";
			public static final String ID = "id";
			public static final String NOMBRE = "nombre";
			public static final String CI = "ci";
			public static final String TELEFONO = "telefono";
			public static final String DIRECCION = "direccion";
			public static final String GARANTIA = "garantia";
			public static final String CREADO_TS = "creado_ts";
			public static final String ESTADO = "estado";
		}

		// ============================
		// TABLA CREDITOS
		// ============================
		// NOTA: El interés (en porcentaje) es un dato del crédito individual.
		public static final class Creditos {
				public static final String TABLE = "creditos";

				public static final String ID = "id";
				public static final String CLIENTE_ID = "cliente_id";
				public static final String PLAN_ID = "plan_id"; // Clave foránea a la tabla PLANES
				public static final String CAPITAL = "capital";
				public static final String INTERES_PORCENTAJE = "interes_porcentaje"; // Nuevo campo para el porcentaje (ej: 10.0)
				public static final String INTERES_MONTO = "interes_monto"; // Interés total calculado en monto
				public static final String TOTAL = "total";
				public static final String FECHA_INICIO = "fecha_inicio";
				public static final String ESTADO = "estado";
				public static final String CREADO_TS = "creado_ts";
			}

		// ============================
		// TABLA CUOTAS
		// ============================
		public static final class Cuotas {
				// ... campos sin cambios
				public static final String TABLE = "cuotas";
				public static final String ID = "id";
				public static final String CREDITO_ID = "credito_id";
				public static final String NUMERO_CUOTA = "numero_cuota";
				public static final String MONTO_CUOTA = "monto_cuota";
				public static final String FECHA_PAGO = "fecha_pago";
				public static final String PAGADA = "pagada";
				public static final String PAGADA_TS = "pagada_ts";
			}
		// ============================
		// TABLA PLANES (Optimizado)
		// ============================
		public static final class Planes {
				public static final String TABLE = "planes";

				public static final String ID = "id";
				public static final String NOMBRE = "nombre";
				public static final String DESCRIPCION = "descripcion";
				public static final String TIPO = "tipo"; // diario, semanal, quincenal
				public static final String FRECUENCIA = "frecuencia_dias"; // Intervalo de días (1, 7, 15, 30)
				public static final String CUOTAS_TOTALES = "cuotas_totales"; // El número fijo de cuotas a generar (24, 30, 4, 2, 1)
				public static final String SALTO_DOMINGO = "salto_domingo"; // Booleano (0 o 1) para la excepción de 24 días
				public static final String METODO_AMORTIZACION = "metodo_amortizacion"; 
				public static final String CREADO_TS = "creado_ts";

				// Constantes para METODO_AMORTIZACION
				public static final String METODO_LINEAL_TOTAL = "LINEAL_TOTAL"; // Cuota fija, amortiza Capital + Interés (para 24, 30, Semanal, Quincenal)
				public static final String METODO_SOLO_INTERES = "SOLO_INTERES"; // Cuota solo del interés devengado (para Variable)
			}

		// ============================
		// TABLA ESTADOS (opcional)
		// ============================
		public static final class Estados {
				// ... campos sin cambios
				public static final String TABLE = "estados_credito";
				public static final String ID = "id";
				public static final String DESCRIPCION = "descripcion";
			}
	}
