package com.consultoriasolucao.appsolucaosistemas;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class LancaDespesa extends Activity {

	public static final String EXTRA_CD_LANCAMENTO = "AppSolucaoSistemas.EXTRA_CD_LANCAMENTO";
	
	private EditText edtvalor;
	private EditText edthistorico;
	private DatabaseHelper db;
	private Uri outputFileUri;
	private int ano, mes, dia;
	private Button dataGasto;
	private Button dataVencimento;
	private Spinner categoria;
	private Spinner pagamento;
	private List<String> nomesCategoria = new ArrayList<String>();
	private List<String> nomesPagamento = new ArrayList<String>();
	private RadioGroup radioGroup;
	private RadioGroup rgreceitadespesa;
	private String datasel;
	private boolean flagvalida;
	private Date dt_lancamento, dt_vencimento;
    private String cd_lancamento;
    private SimpleDateFormat dateFormat;
    private RadioButton rgsituacaopago;
    private RadioButton rgsituacaoapagar;
    private RadioButton rgreceita;
    private RadioButton rgdespesa;
    private TableLayout tl_situacao;
    private TableLayout tl_vencimento;
    private String straux;
    private int cat_position;
    private int pag_position;
    ArrayAdapter<String> arrayAdapterCategoria;
    ArrayAdapter<String> arrayAdapterPagamento;

    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lancardespesa);
		
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().hide();
		
		db = new DatabaseHelper(this);

		this.edtvalor = (EditText) findViewById(R.id.edtvalor);
		this.edthistorico = (EditText) findViewById(R.id.edthistorico);
		this.radioGroup = (RadioGroup) findViewById(R.id.rgsituacao);
		this.rgreceitadespesa = (RadioGroup) findViewById(R.id.rgreceitadespesa);
		this.tl_situacao = (TableLayout) findViewById(R.id.tl_situacao);
		this.tl_vencimento = (TableLayout) findViewById(R.id.tl_vencimento);
		this.rgreceita = (RadioButton) findViewById(R.id.rgreceita);
		this.rgdespesa = (RadioButton) findViewById(R.id.rgdespesa);
		this.rgsituacaopago = (RadioButton) findViewById(R.id.rgsituacaopgo);
		this.rgsituacaoapagar = (RadioButton) findViewById(R.id.rgsituacaoavencer);
		this.categoria = (Spinner) findViewById(R.id.categoria);
		this.pagamento = (Spinner) findViewById(R.id.pagamento);


        
		Calendar calendar = Calendar.getInstance();
		ano = calendar.get(Calendar.YEAR);
		mes = calendar.get(Calendar.MONTH);
		dia = calendar.get(Calendar.DAY_OF_MONTH);
		dataGasto = (Button) findViewById(R.id.btdata);
		dataGasto.setText(dia + "/" + (mes + 1) + "/" + ano);
		dataVencimento = (Button) findViewById(R.id.btdtvencimento);
		dataVencimento.setText(dia + "/" + (mes + 1) + "/" + ano);


		SQLiteDatabase dbexe = db.getReadableDatabase();
		
		carregaSpinnerCategoria();
		carregaSpinnerPagamento();
		
		cd_lancamento=""; //codigo do lancamento e a flag para verificar se � edi��o ou inser��o
		Intent intent = getIntent();
		if (intent.hasExtra(EXTRA_CD_LANCAMENTO)) {//caso seja edi��o ent�o carregando os campos

			cd_lancamento = intent.getStringExtra(EXTRA_CD_LANCAMENTO);
			Cursor cursor = dbexe.rawQuery(
					"SELECT a._id, a.ds_historico, coalesce(a.vl_despesa,0), coalesce(a.vl_receita,0), a.dt_lancamento,a.dt_vencimento, a.cd_categoria, a.cd_pagamento, a.ds_situacao,  a.ds_tipo,b.ds_categoria, c.ds_pagamento from financas a join categoria b on (a.cd_categoria=b._id) "
					+ "join pagamento c on (a.cd_pagamento=c._id) where a._id="+cd_lancamento, null);
			while (cursor.moveToNext()) {
				edthistorico.setText(cursor.getString(1));				
				Double val =Double.valueOf(cursor.getString(2).toString()).doubleValue()+Double.valueOf(cursor.getString(3).toString()).doubleValue();
				edtvalor.setText(val+"");
				long dataChegada = cursor.getLong(4);
				Date dataChegadaDate = new Date(dataChegada);
				dateFormat = new SimpleDateFormat("dd/MM/yyyy");
				String periodo = dateFormat.format(dataChegadaDate);
				dataGasto.setText(periodo);
				
				dataChegada = cursor.getLong(5);
				periodo = dateFormat.format(dataChegadaDate);
				dataVencimento.setText(periodo);
				

				if (cursor.getString(8).equals("P")) //caso a situa��o seja pago
				{
					rgsituacaopago.setChecked(true);	
					rgsituacaoapagar.setChecked(false);
				} else 
				{
					rgsituacaopago.setChecked(false);	
					rgsituacaoapagar.setChecked(true);	
					tl_vencimento.setVisibility(View.VISIBLE);
				}
				
				
				if (cursor.getString(9).equals("D")) //caso o tipo seja despesa
				{
					rgreceita.setChecked(false);	
					rgdespesa.setChecked(true);
					rgsituacaopago.setText("Pago");
					rgsituacaoapagar.setText("A Pagar");
					rgsituacaoapagar.setBackgroundResource(R.drawable.rbtn_selector2);
					rgsituacaoapagar.setTextColor(R.drawable.rbtn_textcolor_selector2);
					rgsituacaopago.setBackgroundResource(R.drawable.rbtn_selector2);
					rgsituacaopago.setTextColor(R.drawable.rbtn_textcolor_selector2);
				} else 
				{
					rgreceita.setChecked(true);	
					rgdespesa.setChecked(false);
					rgsituacaopago.setText("Recebido");
					rgsituacaoapagar.setText("A Receber");
					rgsituacaoapagar.setBackgroundResource(R.drawable.rbtn_selector);
					rgsituacaoapagar.setTextColor(R.drawable.rbtn_textcolor_selector);
					rgsituacaopago.setBackgroundResource(R.drawable.rbtn_selector);
					rgsituacaopago.setTextColor(R.drawable.rbtn_textcolor_selector);
				}
				
				//verificando qual item do sppiner Categoria foi selecinado	
				straux = cursor.getString(10);			
				final int spinnerPosition = arrayAdapterCategoria.getPosition(straux);
				cat_position = spinnerPosition;
				new Handler().postDelayed(new Runnable() {        
				    public void run() {
				    	categoria.setSelection(spinnerPosition, true);
				    }
				  }, 100);

				
				//verificando qual item do sppiner Formas de Pagamento foi selecinado	
				straux = cursor.getString(11);					
				final int spinnerPosition2 = arrayAdapterPagamento.getPosition(straux);
				pag_position = spinnerPosition2;
				new Handler().postDelayed(new Runnable() {        
				    public void run() {
				    	pagamento.setSelection(spinnerPosition2, true);
				    }
				  }, 100);
				 
			}
			cursor.close();
		}
		

	}
	
	public void selecionarTipo(View view){
		if(rgdespesa.isChecked()){
			rgsituacaopago.setText("Pago");
			rgsituacaoapagar.setText("A Pagar");
			rgsituacaoapagar.setBackgroundResource(R.drawable.rbtn_selector2);
			rgsituacaoapagar.setTextColor(R.drawable.rbtn_textcolor_selector2);
			rgsituacaopago.setBackgroundResource(R.drawable.rbtn_selector2);
			rgsituacaopago.setTextColor(R.drawable.rbtn_textcolor_selector2);
//			tl_situacao.setVisibility(View.VISIBLE);
		}
		else if(rgreceita.isChecked()){
			rgsituacaopago.setText("Recebido");
			rgsituacaoapagar.setText("A Receber");
			rgsituacaoapagar.setBackgroundResource(R.drawable.rbtn_selector);
			rgsituacaoapagar.setTextColor(R.drawable.rbtn_textcolor_selector);
			rgsituacaopago.setBackgroundResource(R.drawable.rbtn_selector);
			rgsituacaopago.setTextColor(R.drawable.rbtn_textcolor_selector);
//			tl_situacao.setVisibility(View.VISIBLE);
		}
	}
	
	public void selecionarSituacao(View view){
		if(rgsituacaoapagar.isChecked()){			
			tl_vencimento.setVisibility(View.VISIBLE);			
		}
		else if(rgsituacaopago.isChecked()){
			tl_vencimento.setVisibility(View.INVISIBLE);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void selecionarDataLancamento(View view) {
		showDialog(view.getId());
		datasel = "DataLancamento";

	}

	@SuppressWarnings("deprecation")
	public void selecionarDataVencimento(View view) {
		showDialog(view.getId());
		datasel = "DataVencimento";
	}

	private OnDateSetListener listener = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			ano = year;
			mes = monthOfYear;
			dia = dayOfMonth;

			if (datasel == "DataLancamento") {
				dataGasto.setText(dia + "/" + (mes + 1) + "/" + ano);
			} else
				dataVencimento.setText(dia + "/" + (mes + 1) + "/" + ano);
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		if (R.id.btdata == id) {
			return new DatePickerDialog(this, listener, ano, mes, dia);
		}

		if (R.id.btdtvencimento == id) {
			return new DatePickerDialog(this, listener, ano, mes, dia);
		}
		return null;
	}

	public void InserirDespesa(View view) {
		flagvalida = true;

		
		if (edthistorico.getText().toString().equals("") ) {
			edthistorico.setError("Entre com a Descri��o!");
			edthistorico.requestFocus();
			flagvalida = false;

		}
		
		else if (edtvalor.getText().toString().equals("")) {
			edtvalor.setError("Entre com o Valor!");
			edtvalor.requestFocus();
			flagvalida = false;

		}
		
		else if (categoria.getSelectedItem().toString().equals("SELECIONE")){
			categoria.requestFocus();
			Toast.makeText(this, "Entre com a Categoria!",
					Toast.LENGTH_LONG).show();
			flagvalida = false;
		}
		
		else if (pagamento.getSelectedItem().toString().equals("SELECIONE")){
			pagamento.requestFocus();
			Toast.makeText(this, "Entre com a Forma de Pagamento!",
					Toast.LENGTH_LONG).show();
			flagvalida = false;
		}
		
		else if ((!rgdespesa.isChecked()) & (!rgreceita.isChecked())){
			rgreceita.setError("Entre com o Tipo do Lan�amento!");
			rgreceita.requestFocus();
			flagvalida = false;
		}
		
		
		else if ((!rgsituacaoapagar.isChecked()) & (!rgsituacaopago.isChecked())){
			rgsituacaoapagar.setError("Entre com a Situa��o do T�tulo!");
			rgsituacaoapagar.requestFocus();
			flagvalida = false;
		}

		if (flagvalida) {
			
			SQLiteDatabase banco = db.getWritableDatabase();
			
			if (cd_lancamento != ""){ //caso seja edi��o ent�o apagando o registro e inserindo outro
				
				banco.execSQL("delete from financas where _id="+cd_lancamento);	
			}
			
			ContentValues values = new ContentValues();
			values.put("ds_historico", edthistorico.getText().toString());	
			
			dt_lancamento = ConvertToDate(dataGasto.getText().toString());
			values.put("dt_lancamento", dt_lancamento.getTime());
			dt_vencimento = ConvertToDate(dataVencimento.getText().toString()); 
			values.put("dt_vencimento", dt_vencimento.getTime());

			int tipo = rgreceitadespesa.getCheckedRadioButtonId();
			if (tipo == R.id.rgdespesa) 
			{
				values.put("ds_tipo", "D");
				values.put("vl_despesa",Double.valueOf(edtvalor.getText().toString()).doubleValue());
				values.put("vl_receita", 0);
			} else 
			{  
				values.put("ds_tipo", "R");
				values.put("vl_receita",Double.valueOf(edtvalor.getText().toString()).doubleValue());
				values.put("vl_despesa", 0);
			}
			
			tipo = radioGroup.getCheckedRadioButtonId();
			if (tipo == R.id.rgsituacaopgo) {
				values.put("ds_situacao", "P");
			} else
				values.put("ds_situacao", "A");
			
			
			String cd_cat = categoria.getSelectedItem().toString();
			db = new DatabaseHelper(this);
			SQLiteDatabase d = db.getReadableDatabase();			
			Cursor cursor1 = d.rawQuery(
					"SELECT _id FROM categoria where ds_categoria=\""+ cd_cat +"\" order by ds_categoria", null);
			cursor1.moveToNext();
			cd_cat = cursor1.getString(0);
			cursor1.close();
			values.put("cd_categoria", cd_cat);
			
			
			String cd_pag = pagamento.getSelectedItem().toString();
			db = new DatabaseHelper(this);			
			SQLiteDatabase d1 = db.getReadableDatabase();			
			Cursor cursor2 = d1.rawQuery(
					"SELECT _id FROM pagamento where ds_pagamento=\""+ cd_pag +"\" order by ds_pagamento", null);
			cursor2.moveToNext();
			cd_pag = cursor2.getString(0);
			cursor2.close();
			values.put("cd_pagamento", cd_pag);
			

			banco.insert("financas", null, values);
			edthistorico.setText("");
			edtvalor.setText("");
			
			Toast.makeText(this, "Lan�amento salvo com sucesso!",
					Toast.LENGTH_LONG).show();
			
			LancaDespesa.this.finish();
		}
	}
	
	private Date ConvertToDate(String dateString)
	{
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    Date convertedDate = new Date();
	    try {
	        convertedDate = dateFormat.parse(dateString);
	    } catch (ParseException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    return convertedDate;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}

	public void cadCategoria(View view)
	{
		startActivity(new Intent(this, Categoria.class));
	}
	
	public void cadPagamento(View view)
	{
		startActivity(new Intent(this, FormaDePagamento.class));
	}

	public void carregaSpinnerCategoria(){
		nomesCategoria.clear();
		Cursor c = db.getReadableDatabase().rawQuery(
				"SELECT _id, ds_categoria FROM categoria order by ds_categoria", null);
		nomesCategoria.add("SELECIONE");
		while (c.moveToNext()) {
			nomesCategoria.add(c.getString(1));			
		}

		c.close();
		// Cria um ArrayAdapter usando um padr�o de layout da classe R do
		// android, passando o ArrayList nomes
		arrayAdapterCategoria = new ArrayAdapter<String>(this, R.layout.spinner_item, nomesCategoria);
		ArrayAdapter<String> spinnerArrayAdapter = arrayAdapterCategoria;
		spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		
		categoria.setAdapter(spinnerArrayAdapter);
	}
	
	public void carregaSpinnerPagamento(){
		nomesPagamento.clear();
		Cursor c1 = db.getReadableDatabase().rawQuery(
				"SELECT _id, ds_pagamento FROM pagamento order by ds_pagamento", null);
		nomesPagamento.add("SELECIONE");
		while (c1.moveToNext()) {
			nomesPagamento.add(c1.getString(1));			
		}

		c1.close();
		// Cria um ArrayAdapter usando um padr�o de layout da classe R do
		// android, passando o ArrayList nomes
		arrayAdapterPagamento = new ArrayAdapter<String>(this, R.layout.spinner_item, nomesPagamento);
		ArrayAdapter<String> spinnerArrayAdapter2 = arrayAdapterPagamento;
		spinnerArrayAdapter2.setDropDownViewResource(R.layout.spinner_dropdown_item);
		
		pagamento.setAdapter(spinnerArrayAdapter2);
	}
		
	public void tiraFoto(View view) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = new File(Environment.getExternalStorageDirectory(),
				"test.jpg");
		outputFileUri = Uri.fromFile(file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, 9000);

	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.solucao_sistemas, menu);
//		return true;
//	}
	
	@Override
	public void onResume(){
		super.onResume();
		carregaSpinnerCategoria();
		new Handler().postDelayed(new Runnable() {        
		    public void run() {
		    	categoria.setSelection(cat_position, true);
		    }
		  }, 100);
		
		carregaSpinnerPagamento();
		new Handler().postDelayed(new Runnable() {        
		    public void run() {
		    	pagamento.setSelection(pag_position, true);
		    }
		  }, 100);
	}

}