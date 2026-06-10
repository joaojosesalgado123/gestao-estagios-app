package pt.ligix.app.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import java.util.Locale

data class PhoneCountry(
    val regionCode: String,
    val countryName: String,
    val callingCode: String
) {
    val compactLabel: String
        get() = "$regionCode +$callingCode"

    val displayLabel: String
        get() = "$countryName ($regionCode) +$callingCode"
}

data class PhoneNumberInputState(
    val country: PhoneCountry,
    val nationalNumber: String
)

data class PhoneNumberValidationResult(
    val e164: String?,
    val errorMessage: String?
) {
    val isValid: Boolean
        get() = errorMessage == null
}

object PhoneNumberValidator {
    private const val DEFAULT_REGION = "PT"
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    private val portugueseLocale = Locale.forLanguageTag("pt-PT")

    val countries: List<PhoneCountry> by lazy {
        val allCountries = phoneUtil.supportedRegions
            .map { region ->
                PhoneCountry(
                    regionCode = region,
                    countryName = Locale.Builder().setRegion(region).build().getDisplayCountry(portugueseLocale)
                        .ifBlank { region },
                    callingCode = phoneUtil.getCountryCodeForRegion(region).toString()
                )
            }
            .sortedWith(compareBy<PhoneCountry> { it.countryName }.thenBy { it.regionCode })

        val defaultCountry = allCountries.firstOrNull { it.regionCode == DEFAULT_REGION }
        if (defaultCountry == null) {
            allCountries
        } else {
            listOf(defaultCountry) + allCountries.filterNot { it.regionCode == DEFAULT_REGION }
        }
    }

    fun defaultCountry(): PhoneCountry =
        countryForRegion(DEFAULT_REGION) ?: countries.first()

    fun countryForRegion(regionCode: String): PhoneCountry? =
        countries.firstOrNull { it.regionCode.equals(regionCode, ignoreCase = true) }

    fun inputState(rawValue: String, fallbackRegion: String = DEFAULT_REGION): PhoneNumberInputState {
        val country = detectCountry(rawValue)
            ?: countryForRegion(fallbackRegion)
            ?: defaultCountry()

        return PhoneNumberInputState(
            country = country,
            nationalNumber = extractNationalNumber(rawValue, country)
        )
    }

    fun composeE164Candidate(country: PhoneCountry, nationalNumber: String): String {
        val digits = nationalNumber.onlyDigits()
        return if (digits.isBlank()) "" else "+${country.callingCode}$digits"
    }

    fun normalizeToE164(
        rawValue: String,
        fallbackRegion: String = DEFAULT_REGION,
        required: Boolean = false
    ): PhoneNumberValidationResult {
        val trimmed = rawValue.trim()

        if (trimmed.isBlank()) {
            return if (required) {
                PhoneNumberValidationResult(
                    e164 = null,
                    errorMessage = "Insira o número de telefone."
                )
            } else {
                PhoneNumberValidationResult(e164 = null, errorMessage = null)
            }
        }

        return try {
            val parsed = phoneUtil.parse(trimmed, fallbackRegion)
            if (!phoneUtil.isValidNumber(parsed)) {
                PhoneNumberValidationResult(
                    e164 = null,
                    errorMessage = "Insira um número de telefone válido com indicativo."
                )
            } else {
                PhoneNumberValidationResult(
                    e164 = phoneUtil.format(parsed, PhoneNumberFormat.E164),
                    errorMessage = null
                )
            }
        } catch (_: NumberParseException) {
            PhoneNumberValidationResult(
                e164 = null,
                errorMessage = "Insira um número de telefone válido com indicativo."
            )
        }
    }

    fun formatForDisplay(rawValue: String?, fallback: String = "—"): String {
        val trimmed = rawValue?.trim().orEmpty()
        if (trimmed.isBlank()) return fallback

        return try {
            val parsed = phoneUtil.parse(trimmed, DEFAULT_REGION)
            if (phoneUtil.isValidNumber(parsed)) {
                phoneUtil.format(parsed, PhoneNumberFormat.INTERNATIONAL)
            } else {
                trimmed
            }
        } catch (_: NumberParseException) {
            trimmed
        }
    }

    private fun detectCountry(rawValue: String): PhoneCountry? {
        val trimmed = rawValue.trim()
        val internationalDigits = when {
            trimmed.startsWith("+") -> trimmed.onlyDigits()
            trimmed.startsWith("00") -> trimmed.onlyDigits().removePrefix("00")
            else -> return null
        }

        return countries
            .sortedByDescending { it.callingCode.length }
            .firstOrNull { internationalDigits.startsWith(it.callingCode) }
    }

    private fun extractNationalNumber(rawValue: String, country: PhoneCountry): String {
        val trimmed = rawValue.trim()
        val digits = trimmed.onlyDigits()

        return when {
            digits.isBlank() -> ""
            trimmed.startsWith("+") -> digits.removePrefix(country.callingCode)
            trimmed.startsWith("00") -> digits.removePrefix("00").removePrefix(country.callingCode)
            else -> digits
        }
    }

    private fun String.onlyDigits(): String =
        filter { it.isDigit() }
}
