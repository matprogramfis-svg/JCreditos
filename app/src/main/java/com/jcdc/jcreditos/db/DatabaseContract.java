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
				public static final String TIPO = "tipo";
				public static final String FRECUENCIA = "frecuencia_dias";
				public static final String CUOTAS_TOTALES = "cuotas_totales";
				public static final String NUMERO_MESES = "numero_meses";
				public static final String SALTO_DOMINGO = "salto_domingo";
				public static final String CREADO_TS = "creado_ts";
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
