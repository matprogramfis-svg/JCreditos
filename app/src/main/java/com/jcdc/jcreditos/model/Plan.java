package com.jcdc.jcreditos.model;

public class Plan {
		private int id;
		private String nombre;
		private String descripcion;
		private String tipo; // DIARIO, SEMANAL, etc.
		private int frecuencia; // Cuántos días/semanas entre cuotas
		private int cuotasTotales;
		private int saltoDomingo; // 0 o 1
		private String metodoAmortizacion; // FRANCES, ALEMAN
		private String creadoTs;

		// Constructor, Getters y Setters...


		public void setId(int id)
			{
				this.id = id;
			}

		public int getId()
			{
				return id;
			}

		public void setNombre(String nombre)
			{
				this.nombre = nombre;
			}

		public String getNombre()
			{
				return nombre;
			}

		public void setDescripcion(String descripcion)
			{
				this.descripcion = descripcion;
			}

		public String getDescripcion()
			{
				return descripcion;
			}

		public void setTipo(String tipo)
			{
				this.tipo = tipo;
			}

		public String getTipo()
			{
				return tipo;
			}

		public void setFrecuencia(int frecuencia)
			{
				this.frecuencia = frecuencia;
			}

		public int getFrecuencia()
			{
				return frecuencia;
			}

		public void setCuotasTotales(int cuotasTotales)
			{
				this.cuotasTotales = cuotasTotales;
			}

		public int getCuotasTotales()
			{
				return cuotasTotales;
			}

		public void setSaltoDomingo(int saltoDomingo)
			{
				this.saltoDomingo = saltoDomingo;
			}

		public int getSaltoDomingo()
			{
				return saltoDomingo;
			}

		public void setMetodoAmortizacion(String metodoAmortizacion)
			{
				this.metodoAmortizacion = metodoAmortizacion;
			}

		public String getMetodoAmortizacion()
			{
				return metodoAmortizacion;
			}

		public void setCreadoTs(String creadoTs)
			{
				this.creadoTs = creadoTs;
			}

		public String getCreadoTs()
			{
				return creadoTs;
			}}
