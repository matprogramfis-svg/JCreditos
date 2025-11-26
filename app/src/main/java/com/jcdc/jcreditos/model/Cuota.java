package com.jcdc.jcreditos.model;

public class Cuota {
		private int id;
		private int creditoId;
		private int numeroCuota;
		private double montoCuota;
		private String fechaPago; // Usar String para DATE
		private int pagada; // 0 o 1
		private String pagadaTs; // Usar String para DATETIME

		// Constructor, Getters y Setters
		// (Omito la implementación de Getters/Setters para ahorrar espacio, pero debes incluirlos)
		// ...
		// ...


		public void setId(int id)
			{
				this.id = id;
			}

		public int getId()
			{
				return id;
			}

		public void setCreditoId(int creditoId)
			{
				this.creditoId = creditoId;
			}

		public int getCreditoId()
			{
				return creditoId;
			}

		public void setNumeroCuota(int numeroCuota)
			{
				this.numeroCuota = numeroCuota;
			}

		public int getNumeroCuota()
			{
				return numeroCuota;
			}

		public void setMontoCuota(double montoCuota)
			{
				this.montoCuota = montoCuota;
			}

		public double getMontoCuota()
			{
				return montoCuota;
			}

		public void setFechaPago(String fechaPago)
			{
				this.fechaPago = fechaPago;
			}

		public String getFechaPago()
			{
				return fechaPago;
			}

		public void setPagada(int pagada)
			{
				this.pagada = pagada;
			}

		public int getPagada()
			{
				return pagada;
			}

		public void setPagadaTs(String pagadaTs)
			{
				this.pagadaTs = pagadaTs;
			}

		public String getPagadaTs()
			{
				return pagadaTs;
			}}
