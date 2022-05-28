package com.example.whattheweatherlike

object Validator{
    fun validateUserInput(city: String):Boolean{
        return city.isNotEmpty()
    }
    fun validateLocation(lat: String, long: String):Boolean{
        return (lat.isNotEmpty() && long.isNotEmpty())
    }

}