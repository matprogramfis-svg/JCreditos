package com.jcdc.jcreditos.model;
import java.util.*;

public class Credito {
		private int id;
		private int clienteId;
		private int planId;
		private double capital;
		private double interesPorcentaje;
		private double interesMonto;
		private double total;
		private Date fechaInicio; // Usar String para DATE
		private int estado;
		private String creadoTs;

		// Constructor, Getters y Setters
		// (Omito la implementación de Getters/Setters para ahorrar espacio, pero debes incluirlos)
		// ...
		// ...
		// Dentro de com.jcdc.jcreditos.model.Credito.java
// ...
		private String nombreCliente;
		private String nombrePlan;

		public void setFechaInicio(Date fechaInicio)
			{
				this.fechaInicio = fechaInicio;
			}

		public Date getFechaInicio()
			{
				return fechaInicio;
			}

// --- GETTERS Y SETTERS ---
		public String getNombreCliente() { return nombreCliente; }
		public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
		public String getNombrePlan() { return nombrePlan; }
		public void setNombrePlan(String nombrePlan) { this.nombrePlan = nombrePlan; }
// ...


		public void setId(int id)
			{
				this.id = id;
			}

		public int getId()
			{
				return id;
			}

		public void setClienteId(int clienteId)
			{
				this.clienteId = clienteId;
			}

		public int getClienteId()
			{
				return clienteId;
			}

		public void setPlanId(int planId)
			{
				this.planId = planId;
			}

		public int getPlanId()
			{
				return planId;
			}

		public void setCapital(double capital)
			{
				this.capital = capital;
			}

		public double getCapital()
			{
				return capital;
			}

		public void setInteresPorcentaje(double interesPorcentaje)
			{
				this.interesPorcentaje = interesPorcentaje;
			}

		public double getInteresPorcentaje()
			{
				return interesPorcentaje;
			}

		public void setInteresMonto(double interesMonto)
			{
				this.interesMonto = interesMonto;
			}

		public double getInteresMonto()
			{
				return interesMonto;
			}

		public void setTotal(double total)
			{
				this.total = total;
			}

		public double getTotal()
			{
				return total;
			}

		

		public void setEstado(int estado)
			{
				this.estado = estado;
			}

		public int getEstado()
			{
				return estado;
			}

		public void setCreadoTs(String creadoTs)
			{
				this.creadoTs = creadoTs;
			}

		public String getCreadoTs()
			{
				return creadoTs;
			}}
