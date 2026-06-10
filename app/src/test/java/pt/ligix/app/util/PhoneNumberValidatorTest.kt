package pt.ligix.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberValidatorTest {
    @Test
    fun normalizaNumeroPortuguesParaE164() {
        val resultado = PhoneNumberValidator.normalizeToE164("+351912345678", required = true)

        assertTrue(resultado.isValid)
        assertEquals("+351912345678", resultado.e164)
    }

    @Test
    fun aceitaNumeroInternacionalValido() {
        val resultado = PhoneNumberValidator.normalizeToE164("+41446681800", required = true)

        assertTrue(resultado.isValid)
        assertEquals("+41446681800", resultado.e164)
    }

    @Test
    fun criaEstadoDeInputAPartirDeE164() {
        val estado = PhoneNumberValidator.inputState("+351912345678")

        assertEquals("PT", estado.country.regionCode)
        assertEquals("912345678", estado.nationalNumber)
    }

    @Test
    fun rejeitaNumeroCurto() {
        val resultado = PhoneNumberValidator.normalizeToE164("+351123", required = true)

        assertFalse(resultado.isValid)
    }
}
