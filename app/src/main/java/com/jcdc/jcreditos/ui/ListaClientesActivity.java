package com.jcdc.jcreditos.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.jcdc.jcreditos.R;
import com.jcdc.jcreditos.dao.ClientesDao;
import com.jcdc.jcreditos.model.Cliente;

import java.util.List;
import java.util.ArrayList;

public class ListaClientesActivity extends Activity {

		private ListView lvClientes;
		private Button btnNuevoCliente;
		private ClientesDao clientesDao;

		// Código de solicitud para identificar el resultado de ClienteActivity
		private static final int REQUEST_CODE_CLIENTE = 1;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_lista_clientes);

				clientesDao = new ClientesDao(this);

				lvClientes = (ListView) findViewById(R.id.lv_clientes);
				btnNuevoCliente = (Button) findViewById(R.id.btn_nuevo_cliente);

				btnNuevoCliente.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									// Navegar a ClienteActivity para agregar uno nuevo
									Intent intent = new Intent(ListaClientesActivity.this, ClienteActivity.class);
									// Usamos startActivityForResult para saber cuándo regresa y actualizar la lista
									startActivityForResult(intent, REQUEST_CODE_CLIENTE); 
								}
						});

				// Cargar los datos iniciales
				cargarListaClientes();
			}

		/**
		 * Recupera los clientes de la BD y los muestra en el ListView.
		 */
		private void cargarListaClientes() {
				// Usamos nuestro método getAllClientes() del DAO
				List<Cliente> listaClientes = clientesDao.getAllClientes();

				// 1. Preparamos los datos para el ArrayAdapter
				// Aquí creamos una lista simple de Strings para mostrar la información principal
				List<String> nombresClientes = new ArrayList<>();
				for (Cliente cliente : listaClientes) {
						nombresClientes.add(cliente.getNombre() + " (CI: " + cliente.getCi() + ")");
					}

				// 2. Creamos el adaptador simple
				// Usamos android.R.layout.simple_list_item_1, ya que no estamos usando AppCompat
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					this, 
					android.R.layout.simple_list_item_1, // Layout simple para una sola línea de texto
					nombresClientes
				);

				// 3. Asignamos el adaptador al ListView
				lvClientes.setAdapter(adapter);

				// TODO: Implementar el onItemClickListener para editar/ver un cliente
			}

		// Método para actualizar la lista cuando volvemos de ClienteActivity
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				super.onActivityResult(requestCode, resultCode, data);

				if (requestCode == REQUEST_CODE_CLIENTE) {
						if (resultCode == RESULT_OK) {
								// El cliente se guardó/editó con éxito
								cargarListaClientes();
								Toast.makeText(this, "Lista de clientes actualizada.", Toast.LENGTH_SHORT).show();
							}
					}
			}

		// Nota: El método onResume() también puede ser usado para recargar la lista
		/*
		 @Override
		 protected void onResume() {
		 super.onResume();
		 cargarListaClientes();
		 }
		 */
	}
