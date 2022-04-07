package com.example.cedula_scanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public static final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;
    TextView tvFirstName;
    TextView tvSecondName;
    TextView tvLastName;
    TextView tvSecondLastName;
    TextView tvDocumentID;
    TextView tvGender;
    TextView tvDate;
    TextView tvRH;
    private Toolbar toolbar;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvSecondName = findViewById(R.id.tvSecondName);
        tvLastName = findViewById(R.id.tvLastName);
        tvSecondLastName = findViewById(R.id.tvsecondLastName);
        tvDocumentID = findViewById(R.id.tvDocumentID);
        tvGender = findViewById(R.id.tvGenderr);
        tvDate = findViewById(R.id.tvDate);
        tvRH = findViewById(R.id.tvRH);

        setUpToolbar();
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }



    public void setUpToolbar() {
        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.item3:
                Intent acerca = new Intent(this, about.class);
                startActivity(acerca);
                break;
            default:
        }
        return super.onOptionsItemSelected(menuItem);

    }




    public void onClick(View view) {
        if (view.getId() == R.id.btnScan) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.PDF_417);
            integrator.setPrompt("Acerca el codigo de barras de la cedula");
            integrator.setOrientationLocked(false);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setTorchEnabled(true);
            integrator.initiateScan();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != CUSTOMIZED_REQUEST_CODE && requestCode != IntentIntegrator.REQUEST_CODE) {

            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

        if (result.getContents() == null) {
            Intent originalIntent = result.getOriginalIntent();
            if (originalIntent == null) {
                Log.d("LoginActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Log.d("MainActivity", "Cancelled scan due to missing camera permission");
                Toast.makeText(this, "Cancelled due to missing camera permission", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("MainActivity", "Scanned: " + result.getContents());

            parseDataCode(result.getContents());
        }
    }

    private void parseDataCode(String barcode) {
        InfoTarjeta infoTarjeta = null;
        if (barcode != null) {

            if (barcode.length() < 150) {

            }

            infoTarjeta = new InfoTarjeta();
            String primerApellido = "", segundoApellido = "", primerNombre = "", segundoNombre = "", cedula = "", rh = "", fechaNacimiento = "", sexo = "";

            String alphaAndDigits = barcode.replaceAll("[^\\p{Alpha}\\p{Digit}\\+\\_]+", " ");
            String[] splitStr = alphaAndDigits.split("\\s+");

            if (!alphaAndDigits.contains("PubDSK")) {
                int corrimiento = 0;


                Pattern pat = Pattern.compile("[A-Z]");
                Matcher match = pat.matcher(splitStr[2 + corrimiento]);
                int lastCapitalIndex = -1;
                if (match.find()) {
                    lastCapitalIndex = match.start();
                    String TAG = "parseDataCode";
                    Log.d(TAG, "match.start: " + match.start());
                    Log.d(TAG, "match.end: " + match.end());
                    Log.d(TAG, "splitStr: " + splitStr[2 + corrimiento]);
                    Log.d(TAG, "splitStr length: " + splitStr[2 + corrimiento].length());
                    Log.d(TAG, "lastCapitalIndex: " + lastCapitalIndex);
                }
                cedula = splitStr[2 + corrimiento].substring(lastCapitalIndex - 10, lastCapitalIndex);
                primerApellido = splitStr[2 + corrimiento].substring(lastCapitalIndex);
                segundoApellido = splitStr[3 + corrimiento];
                primerNombre = splitStr[4 + corrimiento];

                if (Character.isDigit(splitStr[5 + corrimiento].charAt(0))) {
                    corrimiento--;
                } else {
                    segundoNombre = splitStr[5 + corrimiento];
                }

                sexo = splitStr[6 + corrimiento];
                rh = splitStr[6 + corrimiento].substring(splitStr[6 + corrimiento].length() - 2);
                fechaNacimiento = splitStr[6 + corrimiento].substring(2, 10);

            } else {
                int corrimiento = 0;
                Pattern pat = Pattern.compile("[A-Z]");
                if (splitStr[2 + corrimiento].length() > 7) {
                    corrimiento--;
                }


                Matcher match = pat.matcher(splitStr[3 + corrimiento]);
                int lastCapitalIndex = -1;
                if (match.find()) {
                    lastCapitalIndex = match.start();

                }

                cedula = splitStr[3 + corrimiento].substring(lastCapitalIndex - 10, lastCapitalIndex);
                primerApellido = splitStr[3 + corrimiento].substring(lastCapitalIndex);
                segundoApellido = splitStr[4 + corrimiento];
                if (splitStr[5 + corrimiento].startsWith("0")) { // UN NOMBRE UN APELLIDO
                    segundoApellido = " ";
                    primerNombre = splitStr[4 + corrimiento];
                    sexo = splitStr[5 + corrimiento].contains("M") ? "Masculino" : "Femenino";
                    rh = splitStr[5 + corrimiento].substring(splitStr[5 + corrimiento].length() - 2);
                    fechaNacimiento = splitStr[5 + corrimiento].substring(2, 10);
                } else if (splitStr[6 + corrimiento].startsWith("0")) { // DOS APELLIDOS UN NOMBRE
                    primerNombre = splitStr[5 + corrimiento];
                    segundoNombre = " ";
                    sexo = splitStr[6 + corrimiento].contains("M") ? "Masculino" : "Femenino";
                    rh = splitStr[6 + corrimiento].substring(splitStr[6 + corrimiento].length() - 2);
                    fechaNacimiento = splitStr[6 + corrimiento].substring(2, 10);
                } else { //DOS APELLIDOS DOS NOMBRES
                    primerNombre = splitStr[5 + corrimiento];
                    segundoNombre = splitStr[6 + corrimiento];
                    sexo = splitStr[7 + corrimiento].contains("M") ? "Masculino" : "Femenino";
                    rh = splitStr[7 + corrimiento].substring(splitStr[7 + corrimiento].length() - 2);
                    fechaNacimiento = splitStr[7 + corrimiento].substring(2, 10);
                }


            }
            String TAG = "parseDataCode";
            Log.d(TAG, "Nombre: " + primerNombre + " " + primerApellido);
            Log.d(TAG, "CEDULA: " + cedula);
            Log.d(TAG, "sexo: " + sexo);
            infoTarjeta.setPrimerNombre(primerNombre);
            infoTarjeta.setSegundoNombre(segundoNombre);
            infoTarjeta.setPrimerApellido(primerApellido);
            infoTarjeta.setSegundoApellido(segundoApellido);
            infoTarjeta.setCedula(cedula);
            infoTarjeta.setSexo(sexo);
            infoTarjeta.setFechaNacimiento(fechaNacimiento);
            infoTarjeta.setRh(rh);
            actualizarCampos(infoTarjeta);

        } else {
            Log.d("TAG", "No barcode capturado");
        }
    }

    private void actualizarCampos(InfoTarjeta infoTarjeta) {
        tvFirstName.setText(String.format("%s%s", infoTarjeta.getPrimerNombre(), " "));
        tvSecondName.setText(infoTarjeta.getSegundoNombre());
        tvLastName.setText(String.format("%s%s", infoTarjeta.getPrimerApellido(), " "));
        tvSecondLastName.setText(infoTarjeta.getSegundoApellido());
        tvDocumentID.setText(infoTarjeta.getCedula());
        tvGender.setText(infoTarjeta.getSexo());
        tvDate.setText(infoTarjeta.getFechaNacimiento());
        tvRH.setText(infoTarjeta.getRh());
        insertar();
    }

    public void insertar(){
        String identificacion = tvDocumentID.getText().toString().trim();
        String nombreU = tvFirstName.getText().toString().trim();
        String nombreD = tvSecondName.getText().toString().trim();
        String apellidoU = tvLastName.getText().toString().trim();
        String apellidoD = tvSecondLastName.getText().toString().trim();
        String genero = tvGender.getText().toString().trim();
        String fNacimiento = tvDate.getText().toString().trim();
        String tipoSangre = tvRH.getText().toString().trim();

        ProgressDialog progressDialog = new ProgressDialog(this);

        if (identificacion.isEmpty()){
            tvDocumentID.setError("escanne para llenar el campo");
        }
        else if (nombreU.isEmpty()){
            tvFirstName.setError("escanne para llenar el campo");
        }
        else if (nombreD.isEmpty()){
            tvSecondName.setError("escanne para llenar el campo");
        }
        else if (apellidoU.isEmpty()){
            tvLastName.setError("escanne para llenar el campo");
        }
        else if (apellidoD.isEmpty()){
            tvSecondLastName.setError("escanne para llenar el campo");
        }
        else if (genero.isEmpty()){
            tvGender.setError("escanne para llenar el campo");
        }
        else if (fNacimiento.isEmpty()){
            tvDate.setError("escanne para llenar el campo");
        }
        else if (tipoSangre.isEmpty()){
            tvRH.setError("escanne para llenar el campo");
        }
        else{
            progressDialog.show();
            StringRequest request = new StringRequest(Request.Method.POST, "https://scannercedula.000webhostapp.com/baseDatos/insertar.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.equalsIgnoreCase("datos insertados")) {
                        Toast.makeText(MainActivity.this, " datos ingresados", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }){
                protected Map <String, String> getParams()throws AuthFailureError{
                    Map<String,String>params= new HashMap<String, String>();

                    params.put("identificacion",identificacion);
                    params.put("nombreU", nombreU);
                    params.put("nombreD", nombreD);
                    params.put("apellidoU", apellidoU);
                    params.put("apellidoD", apellidoD);
                    params.put("genero", genero);
                    params.put("fNacimiento", fNacimiento);
                    params.put("tipoSangre", tipoSangre);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            requestQueue.add(request);
        }

    }
}