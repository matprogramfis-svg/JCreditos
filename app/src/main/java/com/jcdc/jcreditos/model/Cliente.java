package com.jcdc.jcreditos.model;

public class Cliente {
		private int id;
		private String nombre;
		private String ci;
		private String telefono;
		private String direccion;
		private String garantia;
		private String creadoTs; // Usar String para DATETIME
		private int estado;

		// Constructor vacío
		public Cliente() {}

		// Constructor completo
		public Cliente(int id, String nombre, String ci, String telefono, String direccion, String garantia, String creadoTs, int estado) {
				this.id = id;
				this.nombre = nombre;
				this.ci = ci;
				this.telefono = telefono;
				this.direccion = direccion;
				this.garantia = garantia;
				this.creadoTs = creadoTs;
				this.estado = estado;
			}

		// --- Getters y Setters ---

		public int getId() { return id; }
		public void setId(int id) { this.id = id; }

		public String getNombre() { return nombre; }
		public void setNombre(String nombre) { this.nombre = nombre; }

		public String getCi() { return ci; }
		public void setCi(String ci) { this.ci = ci; }

		public String getTelefono() { return telefono; }
		public void setTelefono(String telefono) { this.telefono = telefono; }

		public String getDireccion() { return direccion; }
		public void setDireccion(String direccion) { this.direccion = direccion; }

		public String getGarantia() { return garantia; }
		public void setGarantia(String garantia) { this.garantia = garantia; }

		public String getCreadoTs() { return creadoTs; }
		public void setCreadoTs(String creadoTs) { this.creadoTs = creadoTs; }

		public int getEstado() { return estado; }
		public void setEstado(int estado) { this.estado = estado; }
	}
