package com.jcdc.jcreditos.model;

public class Plan {
		private int id;
		private String nombre;
		private int interes;
		private String tipo; // DIARIO, SEMANAL, etc.
		private int frecuencia; // Cuántos días/semanas entre cuotas
		private int cuotasTotales;
		private int numeroMeses; // 
		private int saltoDomingo; 
		private String creadoTs;

		public void setSaltoDomingo(int saltoDomingo)
			{
				this.saltoDomingo = saltoDomingo;
			}

		public int getSaltoDomingo()
			{
				return saltoDomingo;
			}

		public void setInteres(int interes)
			{
				this.interes = interes;
			}

		public int getInteres()
			{
				return interes;
			}

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

		public void setNumeroMeses(int numeroMeses)
			{
				this.numeroMeses = numeroMeses;
			}

		public int getNumeroMeses()
			{
				return numeroMeses;
			}

		public void setCreadoTs(String creadoTs)
			{
				this.creadoTs = creadoTs;
			}

		public String getCreadoTs()
			{
				return creadoTs;
			}}
