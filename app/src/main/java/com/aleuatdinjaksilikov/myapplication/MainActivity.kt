package com.aleuatdinjaksilikov.myapplication

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private var cancellationSignal : CancellationSignal?=null

    private val authenticationCallback : BiometricPrompt.AuthenticationCallback
        get() = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                notifyUser("Ошибка аутентификации: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                notifyUser("Аутентификация успешна")
                newActivity()
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkBiometricSupport()

        //mainExecutor - чтобы работал в MainThread

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            val biometricPrompt = BiometricPrompt.Builder(this)
                .setTitle("Biometric Auth")
                .setSubtitle("Проверка")
                .setDescription("Приложите свой отпечаток пальца")
                .setNegativeButton("Отмена",mainExecutor,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        notifyUser("Аутентификация отменена")
                    }).build()
            biometricPrompt.authenticate(getCancellationSignal(),mainExecutor,authenticationCallback)
        }
    }

    private fun checkBiometricSupport():Boolean{
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        // Проверка включена ли аутентификация по отпечатку пальца isDeviceSecure
        if (!keyguardManager.isDeviceSecure){
            notifyUser("Аутентификация по отпечатку пальца не была включена в настройках")
            return false
        }

        if (ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.USE_BIOMETRIC)!=
            PackageManager.PERMISSION_GRANTED){
            notifyUser("Разрешение на аутентификацию по отпечатку пальца не включено")
            return false
        }

        //Проверка поддерживает ли сканер отпечатков пальцов
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            true
        }
        else true
    }


    private fun getCancellationSignal():CancellationSignal{
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Аутентификация была отменена пользователем")
        }
        return cancellationSignal as CancellationSignal
    }

    private fun notifyUser(message:String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    private fun newActivity(){
        val intent = Intent(this,MainActivity2::class.java)
        startActivity(intent)
    }
}