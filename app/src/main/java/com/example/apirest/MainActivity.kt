package com.example.apirest

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.app.AlertDialog
import android.graphics.Color
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {

    data class records(
        val id:String,
        val nombre:String,
        val usuario:String,
        val contrasena:String
    )

    data class Resultados(
        val records:List<records>
    )

    interface usuariosDBService{
        @GET("logina.php")
        fun loginUsuario(@Query("usuario") usuario: String, @Query("contrasena") contrasena: String): Call<Resultados>
    }

    object UsuariosDBClient {
        private val retrofit = Retrofit.Builder()
            .baseUrl("https://listasmig.000webhostapp.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service=retrofit.create(usuariosDBService::class.java)

    }

    private lateinit var builder: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*  window.setFlags(

              WindowManager.LayoutParams.FLAG_FULLSCREEN,

              WindowManager.LayoutParams.FLAG_FULLSCREEN
          )*/
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#E91E63"));
        setContentView(R.layout.activity_main)

        //builder=AlertDialog.Builder(this@MainActivity)
        //Ocultar la barra de acciones

        val actionBar = supportActionBar

        actionBar?.hide()
        //Acultar la barra de progreso

        progressBar1.visibility = View.GONE


        //Cargar los datos del usuario

        val preferencias = getSharedPreferences("datos", Context.MODE_PRIVATE)

        editText.setText(preferencias.getString("usuario", ""))

        editText2.setText(preferencias.getString("contrasena", ""))

        //Si hay datos activar recordar

        if (preferencias.getString("usuario", "") != "")

            checkBox.isChecked = true


        //si pulsamos en el boton de registrar

        button2.setOnClickListener {
            val intento2= Intent(this,Registrarme::class.java)
            startActivity(intento2)
        }


        //Si pulsamos en el boton de entrar
        button.setOnClickListener(View.OnClickListener { view ->
            // Do some work here
            //  Toast.makeText(view.context,"No encontrado",Toast.LENGTH_LONG).show()
            progressBar1.visibility= View.VISIBLE;
            val intento1 = Intent(this, Inicio::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                val miusuario = UsuariosDBClient.service.loginUsuario(editText.text.toString(),
                    editText2.text.toString())
                val body = miusuario.execute().body()

                if (body != null) {
                    runOnUiThread {
                        progressBar1.visibility= View.GONE;
                        if (body.records.size != 0) {
                            Log.d("MainActivity", "Usuario:${body.records[0].nombre}")
                            obteneryguardarPropiedades()
                            startActivity(intento1)
                        } else {
                            Log.d("MainActivity", "No Hay usuarios")
                            // Alerta("Usuarios","No esta registrado este usuario!")
                            Toast.makeText(view.context, "Usuario no encontrado!!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }

    fun obteneryguardarPropiedades()
    {
        val preferencias = getSharedPreferences("datos", Context.MODE_PRIVATE)
        val editor = preferencias.edit()

        if(checkBox.isChecked)

        {


            editor.putString("usuario", editText.text.toString())

            editor.putString("contrasena", editText2.text.toString())

            editor.commit()

        }else

        {

            val editor = preferencias.edit()

            editor.putString("usuario", "")

            editor.putString("contrasena", "")

            editor.commit()

        }
    }

}