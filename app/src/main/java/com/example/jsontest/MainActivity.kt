package com.example.jsontest

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import org.json.JSONObject
import timber.log.Timber
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    val REQUEST_READ_EXTERNAL = 1
    var carregouOK = false
    var msgErro: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setContentView(R.layout.activity_main)

        if ( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL)
        } else {
            parseJson()
        }
        if ( carregouOK == false ) {
            buttonErro.setVisibility(View.VISIBLE)
            buttonErro.isClickable=true
            buttonErro.setText("  Falha ao carregar arquivo " + msgErro + " ")
            buttonErro.setOnClickListener {
                Timber.e("Falha ao carregar arquivo" + msgErro)
                finish();
                System.exit(0)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ( requestCode == REQUEST_READ_EXTERNAL) {
            parseJson()
        }
    }

    private fun parseJson() {
        var jsonObject : JSONObject? = null

        msgErro = "Arquivo Invalido"
        try {
            jsonObject = JSONObject(readFile())
        } catch (e:Exception) {
            Timber.e("Erro: %s", e.message.toString())
        }

        if ( jsonObject != null ) {
            try {
                msgErro = "Carregando dono"
                val owner = getOwner(jsonObject.getJSONObject("dono"))
                msgErro = "Carregando qtdDeCaes"
                val numberOfDogs = jsonObject.getInt("qtdDeCaes")
                msgErro = "Carregando racas"
                var dogBreads = getDogBreads(jsonObject.getJSONArray("racas"))
                msgErro = "Carregando caes"
                var dogs = getDogs(jsonObject.getJSONArray("caes"))

                msgErro = ""

                var string = "${owner.firstName} ${owner.lastName} (${owner.age})\n" +
                        "tem $numberOfDogs cães em ${owner.city}\n" +
                        "os cães são:"
                dogs.forEach {
                    string += "${it.name},"
                }

                string += "\nAs raças são: "

                dogBreads.forEach {
                    string += when {
                        it != dogBreads.last() -> " $it, "
                        else -> " $it."
                    }
                }

                text_view.text = string

                carregouOK = true
            } catch (e:Exception) {
                Timber.e("Erro: %s", e.message.toString())
            }
        }


    }

    private fun getDogs(jsonArray: JSONArray): ArrayList<Dog> {
        var dogs = ArrayList<Dog>()
        var x = 0
        while (x < jsonArray.length()) {
            dogs.add( Dog(
                jsonArray.getJSONArray(x).getString(0),
                jsonArray.getJSONArray(x).getString(1),
                jsonArray.getJSONArray(x).getInt(2),
                jsonArray.getJSONArray(x).getString(3).toFloat()
            ))
            x++
        }
        return dogs

    }

    private fun getDogBreads(jsonArray: JSONArray): ArrayList<String> {
        var dogBreads = ArrayList<String>()
        var x = 0
        while (x < jsonArray.length()) {
            dogBreads.add(jsonArray[x].toString())
            x++
        }
        return dogBreads
    }

    private fun getOwner(jsonObject: JSONObject): Owner {
        return Owner(
            jsonObject.getString("nome"),
            jsonObject.getString("sobrenome"),
            jsonObject.getString("cidade"),
            jsonObject.getInt("idade")
        )
    }


    private fun readFile(): String {

        val file = "/storage/emulated/0/JMGames/teste.json"
        val mtStream = FileInputStream(file)
        var jsonString = ""

        mtStream.use { stream ->
            val fileChannel = stream.channel
            val mappedByteBuffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()
            )

            Timber.i("File size : %d", fileChannel.size())

            jsonString = Charset.defaultCharset().decode(mappedByteBuffer).toString()
            Timber.i("Lido: [%s]", jsonString)

        }
        return jsonString
    }
}
