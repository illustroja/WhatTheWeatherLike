package com.example.whattheweatherlike

//import android.R
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import kotlin.math.roundToInt

val country_code = "SG"
var locale1 = Locale("EN", country_code)
class MainActivity : AppCompatActivity() {
    val CITY: String = "singapore, sg"
    val API:String = "125c6c233852de7920c9c5c910970c65"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        WeatherTask().execute()
    }
    inner class WeatherTask() : AsyncTask<String, Void, String>()
    {
        override  fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.error_text).visibility = View.GONE
        }
        override fun doInBackground(vararg p0: String?): String?{
            var response:String? = try{
                URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(Charsets.UTF_8)
            } catch (e: Exception) {
                null
            }
            return response
        }

        override fun onPostExecute(result: String?){
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Update at: " +SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                val temp = main.getString("temp")
                val temp_min = "Min Temp: " + main.getString("temp_min") + "degrees celsius"
                val temp_max = "Max Temp: " + main.getString("temp_max") + "degrees celsius"
                val pressure =  main.getString("pressure")
                val humidity =  main.getString("humidity")
//                28,800,000 is 8hrs in Milliseconds
                val sunrise:Long = sys.getLong("sunrise") + (8*60*60*1000)
                val sunset:Long = sys.getLong("sunset") + (8*60*60*1000)
                val wind_speed = wind.getString("speed")
                var weather_description = weather.getString("description")
                val address = jsonObj.getString("name") + ", " + sys.getString("country")


                var temp_float = (temp.toFloat() * 10.0).roundToInt() / 10.0

                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text = updatedAtText
                findViewById<TextView>(R.id.weather_status).text = weather_description
                findViewById<TextView>(R.id.temperature).text = temp_float.toString()
                findViewById<TextView>(R.id.temperature_min).text = temp_min
                findViewById<TextView>(R.id.temperature_max).text = temp_max
                findViewById<TextView>(R.id.sunrise_timing).text = SimpleDateFormat("hh:mm a", locale1).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset_timing).text = SimpleDateFormat("hh:mm a", locale1).format(Date(sunset*1000))

                findViewById<TextView>(R.id.wind_speed).text = wind_speed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

                //logic to change background automatically based on weather
                val layout = findViewById<RelativeLayout>(R.id.root)
//                weather_description = "thunderstorm "
                //RAIN
                if ("rain" in weather_description ){
                    val random_gen = Random.nextInt(0, 2)
                    if (random_gen == 0){
                        layout.setBackgroundResource(R.drawable.rain_1)
                    }else{
                        layout.setBackgroundResource(R.drawable.rain_2)
                    }
                }
                //THUNDERSTORM
                if ("storm" in weather_description ){
                    val random_gen = Random.nextInt(0, 2)
                    if (random_gen == 0){
                        layout.setBackgroundResource(R.drawable.stormy_1)
                    }else{
                        layout.setBackgroundResource(R.drawable.stormy_2)
                    }
                }
                //SCATTERED CLOUDS
                if ("scattered " in weather_description ){
                    val random_gen = Random.nextInt(0, 2)
                    if (random_gen == 0){
                        layout.setBackgroundResource(R.drawable.cloudy_1)
                    }else{
                        layout.setBackgroundResource(R.drawable.cloudy_2)
                    }

                }
                //FEW CLOUDS
                if ("few " in weather_description ){
                    val random_gen = Random.nextInt(0, 3)
                    if (random_gen == 0){
                        layout.setBackgroundResource(R.drawable.few_clouds_1)
                    }else if(random_gen == 1){
                        layout.setBackgroundResource(R.drawable.few_clouds_2)
                    }
                    else{
                        layout.setBackgroundResource(R.drawable.few_clouds_3)
                    }
                }
                //CLEAR
                if ("clear" in weather_description ){
                    val random_gen = Random.nextInt(0, 3)
                    if (random_gen == 0){
                        layout.setBackgroundResource(R.drawable.clear_1)
                    }else if(random_gen == 1){
                        layout.setBackgroundResource(R.drawable.clear_2)
                    }
                    else{
                        layout.setBackgroundResource(R.drawable.clear_3)
                    }
                }


            }
            catch (e: Exception)
            {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.error_text).visibility = View.VISIBLE

            }
        }
    }
}