package com.example.whattheweatherlike

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.concurrent.schedule
//IMPORT UNIT TEST CASES
import com.example.whattheweatherlike.Validator.validateUserInput

var country_code = "SG"
var locale1 = Locale("EN", country_code)

class MainActivity : AppCompatActivity() {
    private fun toastIt(message: String) {
        // this is for fragment, you can use 'this' for Activity
        this.runOnUiThread {
            println("TRYING TO TOAST DAMMIT")
            // Your Toast
            val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
            toast.show()
        }
    }
    private var city: String = "singapore"
    private var api:String = "125c6c233852de7920c9c5c910970c65"

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationLatitude: String = ""
    private var locationLongitude: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setContentView(binding.root)

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()



        val updateLocationBtn:Button=findViewById(R.id.update_location_btn)
        val refreshBtn:Button=findViewById(R.id.refresh)

        refreshBtn.setOnClickListener {
            toastIt("Refreshing App")
            getWeatherInit("refresh")
        }
        updateLocationBtn.setOnClickListener {
            getWeatherInit("get_new")
        }

        getWeatherInit("location")
    }
    override fun onStop() {
//      In case APP stops unexpectedly, show Error Screen
        super.onStop()
        findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
        findViewById<TextView>(R.id.error_text).visibility = View.GONE
        refreshApp()
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
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
        PERMISSION_LOCATION_ACCESS
        )
    }

    companion object{
        private const val PERMISSION_LOCATION_ACCESS=100
    }
    private fun checkPermissions():Boolean{
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
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
    private fun getWeatherInit(intention: String) {
        findViewById<TextView>(R.id.error_text).visibility = View.GONE
        findViewById<TextView>(R.id.error_text_noInput).visibility = View.GONE
        var newLocation=""
        val updatedLocation:EditText=findViewById(R.id.input_location)
        newLocation=updatedLocation.text.toString()
        println(validateUserInput(newLocation))
        println("PRINTING NEW LOCATION")
        println(newLocation)

        if(intention=="get_new" && newLocation.isEmpty() ){
            toastIt("Please include a valid city")

            return
        }else if( intention=="get_new" && newLocation.isNotEmpty()){
            city = newLocation
            toastIt("Getting Info on $newLocation")
        }
        var url = ""
        val queue: RequestQueue
        val stringRequest: StringRequest

        if (city != "singapore") {
            url = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$api"
            queue = Volley.newRequestQueue(this)
            stringRequest = StringRequest(Request.Method.GET,
                url,
                {
                    //                    Response Success
                        response ->
                    Log.d("Log: ", "Response: $response")
                    println("debug: Weather api call SUCCESS")
                    getWeatherResponse(response)
                },
                //                Else Error found
                {
                    Log.d("Log: ", "Volley error: $it")
                    println("debug: Weather api call FAILED")

                    toastIt("No such Location as $city")
//                    Timer("Preparing to Restart", false).schedule(500) {
//                        refreshApp()
//                    }

                }
            )
            queue.add(stringRequest)


        } else if (locationLatitude != "" && locationLongitude != "" && newLocation=="") {
            url =
                "https://api.openweathermap.org/data/2.5/weather?lat=$locationLatitude&lon=$locationLongitude&units=metric&appid=$api"
            queue = Volley.newRequestQueue(this)
            stringRequest = StringRequest(Request.Method.GET,
                url,
                {
                    //                    Response Success
                        response ->
                    Log.d("Log: ", "Response: $response")
                    println("debug: Weather api call SUCCESS")
                    getWeatherResponse(response)
                },
                //                Else Error found
                {
                    Log.d("Log: ", "Volley error: $it")
                    println("debug: Weather api call FAILED")

                    toastIt("Weather API Failed - LatLong doesn't exist")
//                    Timer("Preparing to Restart", false).schedule(500) {
//                        refreshApp()
//                    }
                }
            )
            queue.add(stringRequest)

        } else {
            url =
                "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$api"
            queue = Volley.newRequestQueue(this)
            stringRequest = StringRequest(Request.Method.GET,
                url,
                {
                    //                    Response Success
                        response ->
                    Log.d("Log: ", "Response: $response")
                    println("debug: Weather api call SUCCESS")
                    getWeatherResponse(response)
                },
                //                Else Error found
                {
                    Log.d("Log: ", "Volley error: $it")
                    println("debug: Weather api call FAILED")

                    toastIt("Weather API Failed - SG Boleh")
//                    Timer("Preparing to Restart", false).schedule(500) {
//                        refreshApp()
//                    }
                }
            )
            queue.add(stringRequest)
        }
    }
    //    END OF: --- Main Weather Function ---


    @SuppressLint("SetTextI18n")
    private fun getWeatherResponse(response:String) {
        findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
        val jsonObj = JSONObject(response)
        val rMain = jsonObj.getJSONObject("main")
        val rSys = jsonObj.getJSONObject("sys")
        val rWind = jsonObj.getJSONObject("wind")
        val rWeather = jsonObj.getJSONArray("weather").getJSONObject(0)
        val rUpdatedAt:Long = jsonObj.getLong("dt")
        val rUpdatedAtText = "Update at: " +SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(rUpdatedAt*1000))
        val rTemp = rMain.getString("temp")
        val rTempMin = rMain.getString("temp_min")
        val rTempMax = rMain.getString("temp_max")
        val rPressure =  rMain.getString("pressure")
        val rHumidity =  rMain.getString("humidity")
//                28,800,000 is 8hrs in Milliseconds
        val rSunrise:Long = rSys.getLong("sunrise") + (8*60*60*1000)
        val rSunset:Long = rSys.getLong("sunset") + (8*60*60*1000)
        val rWindSpeed = rWind.getString("speed")
        val rWeatherDescription = rWeather.getString("description")
        val rAddress = jsonObj.getString("name") + ", " + rSys.getString("country")

//                Capitalise Weather Description
        var weatherDescTitle = rWeatherDescription
        val tempArray: MutableList<String> = ArrayList()
        if(" " in weatherDescTitle){
            val weatherDescTitleArray =  rWeatherDescription
                .split(" ").toTypedArray()
            for (word in weatherDescTitleArray){
                tempArray.add(word.replaceFirstChar { it.uppercase() })

                weatherDescTitle =tempArray.joinToString(" ")
            }
        }
        else
        {
            weatherDescTitle = rWeatherDescription
        }



        val tempFloat = (rTemp.toFloat() * 10.0).roundToInt() / 10.0
        val tempMinFloat = (rTempMin.toFloat() * 10.0).roundToInt() / 10.0
        val tempMaxFloat = (rTempMax.toFloat() * 10.0).roundToInt() / 10.0

        findViewById<TextView>(R.id.address).text = rAddress
        findViewById<TextView>(R.id.updated_at).text = rUpdatedAtText
        findViewById<TextView>(R.id.weather_status).text = weatherDescTitle
        findViewById<TextView>(R.id.temperature).text = tempFloat.toString()
        findViewById<TextView>(R.id.temperature_min).text =
            "Min Temp: \n $tempMinFloat degrees celsius"
        findViewById<TextView>(R.id.temperature_max).text =
            "Max Temp: \n$tempMaxFloat degrees celsius"
        findViewById<TextView>(R.id.sunrise_timing).text = SimpleDateFormat("hh:mm a", locale1).format(Date(rSunrise*1000))
        findViewById<TextView>(R.id.sunset_timing).text = SimpleDateFormat("hh:mm a", locale1).format(Date(rSunset*1000))

        findViewById<TextView>(R.id.wind_speed).text = rWindSpeed
        findViewById<TextView>(R.id.pressure).text = rPressure
        findViewById<TextView>(R.id.humidity).text = rHumidity

        findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

        //logic to change background automatically based on weather
        val layout = findViewById<RelativeLayout>(R.id.root)
//                weather_description = "thunderstorm "
        //RAIN
        if ("rain" in rWeatherDescription ){
            val randomGen = Random.nextInt(0, 2)
            if (randomGen == 0){
                layout.setBackgroundResource(R.drawable.rain_1)
            }else{
                layout.setBackgroundResource(R.drawable.rain_2)
            }
        }
        //THUNDERSTORM
        if ("storm" in rWeatherDescription ){
            val randomGen = Random.nextInt(0, 2)
            if (randomGen == 0){
                layout.setBackgroundResource(R.drawable.stormy_1)
            }else{
                layout.setBackgroundResource(R.drawable.stormy_2)
            }
        }
        //BROKEN or OVERCAST CLOUDS - 51%-100%
        if ("broken" in rWeatherDescription || "overcast" in rWeatherDescription ){
            val randomGen = Random.nextInt(0, 2)
            if (randomGen == 0){
                layout.setBackgroundResource(R.drawable.cloudy_1)
            }else{
                layout.setBackgroundResource(R.drawable.cloudy_2)
            }
        }
        //FEW or SCATTERED CLOUDS - 11%-50%
        if ("few" in rWeatherDescription || "scattered" in rWeatherDescription ){
            val randomGen = Random.nextInt(0, 3)
            if (randomGen == 0){
                layout.setBackgroundResource(R.drawable.few_clouds_1)
            }else if(randomGen == 1){
                layout.setBackgroundResource(R.drawable.few_clouds_2)
            }
            else{
                layout.setBackgroundResource(R.drawable.few_clouds_3)
            }
        }
        //CLEAR
        if ("clear" in rWeatherDescription ){
            val randomGen = Random.nextInt(0, 3)
            if (randomGen == 0){
                layout.setBackgroundResource(R.drawable.clear_1)
            }else if(randomGen == 1){
                layout.setBackgroundResource(R.drawable.clear_2)
            }
            else{
                layout.setBackgroundResource(R.drawable.clear_3)
            }
        }

        findViewById<ProgressBar>(R.id.loader).visibility = View.GONE


    }


    private fun refreshApp() {
        // after on CLick we are using finish to close and then just after that
        // we are calling startactivity(getIntent()) to open our application
        finish()
        startActivity(intent)

        // this basically provides animation
        overridePendingTransition(0, 0)

    }

}
