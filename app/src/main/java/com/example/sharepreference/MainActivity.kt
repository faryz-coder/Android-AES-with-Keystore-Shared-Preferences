package com.example.sharepreference

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log.d
import kotlinx.android.synthetic.main.activity_main.*
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // For generating Key ( only once / enable this code if not yet run )
//        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
//        val keyGenParameterSpec = KeyGenParameterSpec.Builder("MyKeyAlias", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
//            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//            .build()
//
//        keyGenerator.init(keyGenParameterSpec)
//        d("faris", "${keyGenerator.init(keyGenParameterSpec)}")
//        keyGenerator.generateKey()
//        d("faris", "${keyGenerator.generateKey()}")

        //Shared preference
        val sharedPreferences = getSharedPreferences("SP_INFO", Context.MODE_PRIVATE)

        button.setOnClickListener {
            val name = "faris".trim()
            val surname = "kaitou".trim()

            // encrypt
            val encrypt = encryptData(name)

            val key = Base64.getEncoder().encodeToString(encrypt.first)
            val pwd = Base64.getEncoder().encodeToString(encrypt.second)

            // Edit Shared Preference ( to put data )
            val editor = sharedPreferences.edit()

            // put data in shared preference
            editor.putString("NAME", key)
            editor.putString("SURNAME", pwd)
            editor.apply()
        }

        button2.setOnClickListener {
            // Get data from shared preferences
            val name = sharedPreferences.getString("NAME", "")
            val surname = sharedPreferences.getString("SURNAME", "")

            val key = Base64.getDecoder().decode(name)
            val pwd = Base64.getDecoder().decode(surname)

            val decrypt = decryptData(key, pwd)

            d("faris", "key is: $name , password is : $surname")
            d("faris", "decode key is: $key , decode password is : $pwd")
            d("faris", "decrypt is $decrypt")
        }



















    }

    // Fetch the key
    fun getKey(): SecretKey {
        val keystore = KeyStore.getInstance("AndroidKeyStore")

        keystore.load(null)

        val secretKeyEntry = keystore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry

        return secretKeyEntry.secretKey
    }

    fun encryptData(data: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")

        var temp = data
        while (temp.toByteArray().size % 16 != 0)
            temp += "\u0020"
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val ivBytes = cipher.iv

        val encryptedBytes = cipher.doFinal(temp.toByteArray(Charsets.UTF_8))

        return Pair(ivBytes, encryptedBytes)
    }

    fun decryptData(ivBytes: ByteArray, data: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        val spec = IvParameterSpec(ivBytes)

        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

        return cipher.doFinal(data).toString(Charsets.UTF_8).trim()
    }
}
