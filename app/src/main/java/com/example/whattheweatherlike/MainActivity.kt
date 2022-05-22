package com.example.whattheweatherlike

//import android.R
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import kotlin.math.roundToInt
import android.content.Intent


var country_code = "SG"
var locale1 = Locale("EN", country_code)

class MainActivity : AppCompatActivity() {
    var CITY: String = "singapore, sg"
    var API:String = "125c6c233852de7920c9c5c910970c65"

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var locationLatitude: String = ""
    var locationLongitude: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

        val updated_location:EditText=findViewById(R.id.input_location)
        val update_location_btn:Button=findViewById(R.id.update_location_btn)
        val refresh_btn:Button=findViewById(R.id.refresh)

        refresh_btn.setOnClickListener {
            WeatherTask().execute()
        }
        update_location_btn.setOnClickListener {
            val new_location=updated_location.text.toString()
            if(new_location.isEmpty()){
                Toast.makeText(this,"Please include your current City",Toast.LENGTH_SHORT).show()
            }else{
                CITY = new_location
                Toast.makeText(this,new_location,Toast.LENGTH_SHORT).show()
            }
            WeatherTask().execute()
        }

        WeatherTask().execute()
    }

//    START OF: --- Attempt to get User's Current Location upon App Startup ---
    private fun getCurrentLocation(){
        if(checkPermissions())
        {
            if(isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task->
                    val currentLocation: Location?=task.result
                    if(currentLocation==null)
                    {
                        Toast.makeText(this,"No Location Received", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Location Received", Toast.LENGTH_SHORT).show()
                        locationLatitude = currentLocation.latitude.toString()
                        locationLongitude = currentLocation.longitude.toString()
                    }
                }
            }else{
                //ask user to allow settings
                Toast.makeText(this, "Please turn on location services", Toast.LENGTH_SHORT).show()
                val turnOnLocationIntent= Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(turnOnLocationIntent)
            }
        }else{
            //request permissions
            requestPermission()
        }
    }
    private fun isLocationEnabled():Boolean{
        val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),
        PERMISSION_LOCATION_ACCESS
        )
    }
    companion object{
        private const val PERMISSION_LOCATION_ACCESS=100
    }
    private fun checkPermissions():Boolean{
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this , android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== PERMISSION_LOCATION_ACCESS){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext,"Permission Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }else{
                Toast.makeText(applicationContext,"Permission Denied", Toast.LENGTH_SHORT).show()

            }
        }
    }

//    END OF: --- Attempt to get User's Current Location upon App Startup ---

    //    START OF: --- Main Weather Function ---
    inner class WeatherTask() : AsyncTask<String, Void, String>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.error_text).visibility = View.GONE
        }

        override fun doInBackground(vararg p0: String?): String?{
            if(CITY != "singapore, sg"){
                val response:String? = try{
                    URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(Charsets.UTF_8)
                } catch (e: Exception) {
                    println("debug: Weather API call FAILED")
                    null
                }
                println("debug: Weather API call SUCCESS")
                return response

            }else if(locationLatitude != "" && locationLongitude != ""){
                val response:String? = try{
                    URL("https://api.openweathermap.org/data/2.5/weather?lat=$locationLatitude&lon=$locationLongitude&units=metric&appid=$API").readText(Charsets.UTF_8)
                } catch (e: Exception) {
                    println("debug: Weather API call FAILED")
                    null
                }
                println("debug: Weather API call SUCCESS")
                return response
            }else{
                val response:String? = try{
                    URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(Charsets.UTF_8)
                } catch (e: Exception) {
                    println("debug: Weather API call FAILED")
                    null
                }
                println("debug: Weather API call SUCCESS")
                return response
            }
        }

        override fun onPostExecute(result: String){
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
                val temp_min = main.getString("temp_min")
                val temp_max = main.getString("temp_max")
                val pressure =  main.getString("pressure")
                val humidity =  main.getString("humidity")
//                28,800,000 is 8hrs in Milliseconds
                val sunrise:Long = sys.getLong("sunrise") + (8*60*60*1000)
                val sunset:Long = sys.getLong("sunset") + (8*60*60*1000)
                val wind_speed = wind.getString("speed")
                val weather_description = weather.getString("description")
                val address = jsonObj.getString("name") + ", " + sys.getString("country")

//                Capitalise Weather Description
                var weather_desc_title = weather_description
                val temp_array: MutableList<String> = ArrayList()
                if(" " in weather_desc_title){
                    val weather_desc_title_array = weather_desc_title.split(" ").toTypedArray()
                    for (word in weather_desc_title_array){
                        temp_array.add(word.replaceFirstChar { it.uppercase() })

                        weather_desc_title =temp_array.joinToString(" ")
                    }
                }
                else
                    {
                        weather_desc_title = weather_description
                    }



                val temp_float = (temp.toFloat() * 10.0).roundToInt() / 10.0
                val temp_min_float = (temp_min.toFloat() * 10.0).roundToInt() / 10.0
                val temp_max_float = (temp_max.toFloat() * 10.0).roundToInt() / 10.0

                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text = updatedAtText
                findViewById<TextView>(R.id.weather_status).text = weather_desc_title
                findViewById<TextView>(R.id.temperature).text = temp_float.toString()
                findViewById<TextView>(R.id.temperature_min).text = "Min Temp: \n " + temp_min_float.toString() + " degrees celsius"
                findViewById<TextView>(R.id.temperature_max).text = "Max Temp: \n" + temp_max_float.toString() + " degrees celsius"
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
    //    END OF: --- Main Weather Function ---
}