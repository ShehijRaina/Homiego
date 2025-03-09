package com.example.homiego

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var model: QueryViewModel

    private var temperature:Int = 0
    private var humidity:Int = 0
    private var airQuality:String = ""
    private var light:String = ""


    private lateinit var homiegoIcon: ImageView
    private lateinit var temperatureText:TextView
    private lateinit var humidityText:TextView
    private lateinit var airQualityText:TextView
    private lateinit var lightText:TextView
    private lateinit var speakBtn:ImageButton
    private lateinit var captions: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        model = ViewModelProvider(this)[QueryViewModel::class.java]

        val listOfTemperatures = listOf(10, 15, 21, 27, 35)
        temperature = listOfTemperatures[Random.nextInt(listOfTemperatures.size)]

        val listOfHumidity = listOf(37, 45, 58, 69, 81)
        humidity = listOfHumidity[Random.nextInt(listOfHumidity.size)]

        val listOfAirQuality = listOf("Good", "Moderate", "Unhealthy", "Very Unhealthy", "Hazardous")
        airQuality = listOfAirQuality[Random.nextInt(listOfAirQuality.size)]

        val listOfLight = listOf("Low", "Medium", "High")
        light = listOfLight[Random.nextInt(listOfLight.size)]



        setContentView(R.layout.activity_main)
        /**set find view Id*/
        captions = findViewById(R.id.captions)

        homiegoIcon = findViewById(R.id.homiego_icon)


        // Icon only depends on air quality and humidity for now...
        if (airQuality.compareTo("Good") == 0 || humidity < 30)
        {
            homiegoIcon.setImageResource(R.drawable.home_good)
        }
        else if (airQuality.compareTo("Moderate") == 0 || humidity < 60)
        {
            homiegoIcon.setImageResource(R.drawable.home_neutral)
        }
        else
        {
            homiegoIcon.setImageResource(R.drawable.home_bad)
        }


        temperatureText = findViewById(R.id.temperature_text)
        humidityText = findViewById(R.id.humidity_text)
        airQualityText = findViewById(R.id.air_quality_text)
        lightText = findViewById(R.id.light_quality_text)

        speakBtn = findViewById(R.id.speak_btn)

        temperatureText.setText("Temperature: " + temperature + "°C")
        humidityText.setText("Humidity: " + humidity + "%")
        airQualityText.setText("Air Quality: " + airQuality)
        lightText.setText("Light Quality: " + light)


        // Create the observer which updates the UI.
        val captObserver = Observer<String> { newText ->
            // Update the UI, in this case, a TextView.
            captions.setVisibility(View.VISIBLE)
            captions.typeWrite(this, newText, 33L)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        model.capt.observe(this, captObserver)


        mTTs = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = mTTs.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.e("TTs", "Language is not Supported")
                }
            } else {
                Log.e("TTs", "Initialization failed")
            }
        }

            // Hide captions when clicked
            captions.setOnClickListener {
                captions.setVisibility(View.INVISIBLE)
            }

            speakBtn.setOnClickListener {
                val text = "Generating Overview..."
                val duration = Toast.LENGTH_LONG
                val toast = Toast.makeText(this, text, duration) // in Activity
                toast.show()

                model.sendMessage("You are Homiego an app that monitors a home's humidity, temperature, heat and light. The current temperature is " + temperature + " degrees celcius. The humidity is " + humidity + "%. The air quality is " + airQuality + " and the light conditions are " + light + ".Prepare a report on my home and explain it to me - a home owner in 50 words only. Also suggest improvements.")

            }


        val text = "Homiego says hello!"
        val duration = Toast.LENGTH_LONG
        val toast = Toast.makeText(this, text, duration) // in Activity
        toast.show()
    }

    override fun onDestroy() {
        if (mTTs != null) {
            mTTs.stop()
            mTTs.shutdown()
        }
        super.onDestroy()
    }



    companion object {
        private lateinit var mTTs: TextToSpeech

         fun speak(text: String)
         {
            mTTs.speak(text, TextToSpeech.QUEUE_FLUSH, null)
         }

    }

}


// Typewriter animation for captions
fun TextView.typeWrite(lifecycleOwner: LifecycleOwner, text: String, intervalMs: Long) {
    this@typeWrite.text = ""
    lifecycleOwner.lifecycleScope.launch {
        repeat(text.length) {
            delay(intervalMs)
            this@typeWrite.text = text.take(it + 1)
        }
    }
}



