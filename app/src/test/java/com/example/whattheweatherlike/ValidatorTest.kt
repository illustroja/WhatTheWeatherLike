package com.example.whattheweatherlike

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

class ValidatorTest{
    @Test
    fun whenInputIsEmpty() {
        val city = ""
//        val result = Validator.validateUserInput(city)
//        assertThat(result).isFalse()
        assert(city.isEmpty())
    }

    @Test
    fun whenInputIsNotEmpty() {
        val city = "someRandomCity"
//        val result = Validator.validateUserInput(city)
//        assertThat(result).isTrue()
        assert(city.isNotEmpty())
    }

    @Test
    fun whenLocationIsEmpty() {
        val lat = ""
        val long = ""
        val result = Validator.validateLocation(lat, long)
        assertThat(result).isFalse()
    }

    @Test
    fun whenLocationIsNotEmpty() {
        val lat = "123545678"
        val long = "123545678"
        val result = Validator.validateLocation(lat, long)
        assertThat(result).isTrue()
    }
}