package com.example.garageqrdisplay

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import com.example.garageqrdisplay.databinding.ActivityMainBinding
import android.view.MotionEvent
import android.provider.Settings

import android.graphics.PointF
import android.os.Build
import android.util.Log
import android.view.View

import android.view.View.OnTouchListener
import androidx.annotation.RequiresApi
import java.util.logging.Logger
import android.graphics.Bitmap

import android.content.ContextWrapper
import java.lang.Exception



import android.graphics.BitmapFactory
import java.io.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val pickImage = 100
    private var imageUri: Uri? = null
    private var isMovableImg = false
    private var sMode = 0
    private var sBrightness = 0
    private val image_path = "qr.jpg"
    companion object {
        private const val WRITE_SETTINGS_PERMISSION = 100
        private const val MAX_BRIGHTNESS = 255
        private const val MIN_BRIGHTNESS = 20
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap): String? {
        val cw = ContextWrapper(applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory: File = cw.getDir("imageDir", MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, image_path)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.getAbsolutePath()
    }

    private fun loadImageFromStorage() :Boolean{
        var result = false
        try {
            val cw = ContextWrapper(applicationContext)
            // path to /data/data/yourapp/app_data/imageDir
            val directory: File = cw.getDir("imageDir", MODE_PRIVATE)
            // Create imageDir
            val mypath = File(directory, image_path)
            if(mypath.exists()){
                val b = BitmapFactory.decodeStream(FileInputStream(mypath))
                val img = findViewById<ImageView>(R.id.image_qr)
                img.setImageBitmap(b)
                result = true
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return result
    }
    // Check whether this app has android write settings permission.
    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasWriteSettingsPermission(context: Context): Boolean {
        // Get the result from below code.
        return Settings.System.canWrite(context)
    }

    // Start can modify system settings panel to let user change the write
    // settings permission.
    private fun changeWriteSettingsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun createBrightness(){
        // Check whether has the write settings permission or not.
        val context = applicationContext
        val settingsCanWrite = hasWriteSettingsPermission(context)
        // If do not have then open the Can modify system settings panel.
        if (!settingsCanWrite) {
            changeWriteSettingsPermission(context)
        }
    }

    // This function only take effect in real physical android device,
    // it can not take effect in android emulator.
    private fun changeScreenBrightness(context: Context, screenBrightnessValue: Int) {
        // Change the screen brightness change mode to manual.
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        // Apply the screen brightness value to the system, this will change
        // the value in Settings ---> Display ---> Brightness level.
        // It will also change the screen brightness for the device.
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS, screenBrightnessValue
        )
    }

    // This function only take effect in real physical android device,
    // it can not take effect in android emulator.
    private fun changeScreenMode(context: Context, screenModeValue: Int) {
        // Change the screen brightness change mode to manual.
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            screenModeValue
        )
    }

    private fun getScreenBrightness(context: Context): Int {
        // Change the screen brightness change mode to manual.
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        return Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS)
    }

    private fun getScreenMode(context: Context): Int {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.getGlobal().warning("brightness $sBrightness")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        // storing ID of the button
        // in a variable
        val button = findViewById<Button>(R.id.button_upload_img)

        // operations to be performed
        // when user tap on the button
        button?.setOnClickListener()
            {
                val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(gallery, pickImage)
            }
        createBrightness()
        loadImageFromStorage()
        //saveScreenState()
        //Logger.getGlobal().warning("create brightness $sBrightness")
        //changeScreenBrightness(applicationContext, MAX_BRIGHTNESS)
        /* TODO: add movable button
        val buttonMove = findViewById<Button>(R.id.button_mov)
        buttonMove?.setOnClickListener(){
            isMovableImg = !isMovableImg
        }


        val img = findViewById<ImageView>(R.id.image_qr)
        img.setOnTouchListener(object : OnTouchListener {
            var DownPT = PointF() // Record Mouse Position When Pressed Down
            var StartPT = PointF() // Record Start Position of 'img'
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                if (isMovableImg){
                    when (event.action) {
                        MotionEvent.ACTION_MOVE -> {
                            img.setX((StartPT.x + event.x - DownPT.x))
                            img.setY((StartPT.y + event.y - DownPT.y))
                            StartPT[img.getX()] = img.getY()
                        }
                        MotionEvent.ACTION_DOWN -> {
                            DownPT[event.x] = event.y
                            StartPT[img.getX()] = img.getY()
                        }
                        MotionEvent.ACTION_UP -> {
                        }
                        else -> {
                        }
                    }
                }

                return true
            }
        })
        */



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            saveToInternalStorage(bitmap)
            val im = findViewById<ImageView>(R.id.image_qr)
            im.setImageURI(imageUri)
        }
    }

    private fun revertScreenState(){
        changeScreenBrightness(applicationContext, sBrightness)
        changeScreenMode(applicationContext, sMode)
    }

    private fun saveScreenState(){
        sMode = getScreenMode(applicationContext)
        sBrightness = getScreenBrightness(applicationContext)
    }

    override fun onStart() {
        Logger.getGlobal().warning("start brightness $sBrightness")
        super.onStart()
    }

    override fun onStop() {
        Logger.getGlobal().warning("stop brightness $sBrightness")
        super.onStop()
    }
    override fun onPause() {
        revertScreenState()
        Logger.getGlobal().warning("pause brightness $sBrightness")
        super.onPause()
    }



    override fun onResume() {
        Logger.getGlobal().warning("resume0 brightness $sBrightness")
        saveScreenState()
        Logger.getGlobal().warning("resume1 brightness $sBrightness")

        changeScreenBrightness(applicationContext, MAX_BRIGHTNESS)
        Logger.getGlobal().warning("resume2 brightness $sBrightness")
        super.onResume()
    }
}