package com.consultoriasolucao.appsolucaosistemas;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class Categoria extends Activity {
	private DatabaseHelper helper;
	
	EditText edtds_categoria;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_categoria);
		
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().hide();
		
		this.edtds_categoria = (EditText)findViewById(R.id.edtds_categoria);
		// prepara acesso ao banco de dados
		helper = new DatabaseHelper(this);	
		
		
		
	}
	
	public void inserirCategoria(View view)
	{
		Boolean flagvalida = true;

		if (edtds_categoria.getText().toString().equals("")) {
			edtds_categoria.setError("Entre com a descri��o");
			edtds_categoria.requestFocus();
			flagvalida = false;
		}
		
		if (flagvalida)
		{
			
		
		SQLiteDatabase db = helper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put("ds_categoria", this.edtds_categoria.getText().toString());
		long resultado = db.insert("categoria", null, values);
		Toast.makeText(this, "Registro salvo com sucesso!",
				Toast.LENGTH_SHORT).show();
		
		this.edtds_categoria.setText("");
		}
		
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.solucao_sistemas, menu);
//		return true;
//	}

}
