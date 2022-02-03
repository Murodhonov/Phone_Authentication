package uz.umarxon.phoneauthentication

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Data.mywindow = window.currentFocus


    }

    override fun onBackPressed() {
        finishAffinity()
    }
}



@SuppressLint("StaticFieldLeak")
object Data{
    var mywindow :View? = null
}