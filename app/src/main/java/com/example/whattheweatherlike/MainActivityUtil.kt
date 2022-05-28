package com.example.whattheweatherlike

object Validator{
    fun validateUserInput(city: String):Boolean{
        val alpha = "^[a-zA-Z]*$"
        return if (!city.matches(alpha.toRegex())){
            false
        }else city.isNotEmpty()

    }
    fun validateLocation(lat: String, long: String):Boolean{
        return (lat.isNotEmpty() && long.isNotEmpty())
    }

}