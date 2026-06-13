package pt.ligix.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ligix.app.R
import pt.ligix.app.ui.auth.DarkBlue
import pt.ligix.app.ui.auth.FieldGrey
import pt.ligix.app.util.PhoneCountry
import pt.ligix.app.util.PhoneNumberValidator

@Composable
fun PhoneNumberInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "912345678",
    labelColor: Color = Color.Gray,
    containerColor: Color = FieldGrey,
    dismissController: DropdownDismissController? = null,
    dropdownId: String = "phone_country_picker"
) {
    val countries = remember { PhoneNumberValidator.countries }
    val initialState = remember { PhoneNumberValidator.inputState(value) }
    var selectedCountry by remember { mutableStateOf(initialState.country) }
    var nationalNumber by remember { mutableStateOf(initialState.nationalNumber) }
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val activeDropdownId = dismissController?.activeId
    val filteredCountries = remember(countries, search) {
        val term = search.trim()
        if (term.isBlank()) {
            countries
        } else {
            countries.filter { country ->
                country.countryName.contains(term, ignoreCase = true) ||
                    country.regionCode.contains(term, ignoreCase = true) ||
                    country.callingCode.contains(term)
            }
        }
    }

    LaunchedEffect(expanded) {
        if (!expanded) {
            search = ""
            dismissController?.hide(dropdownId)
        }
    }

    LaunchedEffect(activeDropdownId) {
        if (dismissController != null && activeDropdownId != dropdownId) {
            expanded = false
        }
    }

    LaunchedEffect(value) {
        val currentValue = PhoneNumberValidator.composeE164Candidate(
            country = selectedCountry,
            nationalNumber = nationalNumber
        )
        if (value != currentValue) {
            val nextState = PhoneNumberValidator.inputState(value)
            selectedCountry = nextState.country
            nationalNumber = nextState.nationalNumber
        }
    }

    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 10.sp,
            color = labelColor,
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedButton(
                onClick = {
                    if (expanded) {
                        expanded = false
                        dismissController?.hide(dropdownId)
                    } else {
                        expanded = true
                        dismissController?.show(dropdownId)
                    }
                },
                modifier = Modifier
                    .width(112.dp)
                    .height(56.dp)
                    .then(
                        dismissController?.let {
                            Modifier.dropdownDismissBounds(it, dropdownId, expanded, boundsId = "anchor")
                        } ?: Modifier
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor)
            ) {
                Text(
                    selectedCountry.compactLabel,
                    color = Color.Black,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = DarkBlue
                )
            }

            OutlinedTextField(
                value = nationalNumber,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    nationalNumber = digits
                    onValueChange(
                        PhoneNumberValidator.composeE164Candidate(
                            country = selectedCountry,
                            nationalNumber = digits
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text(placeholder, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = containerColor
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
        }

        if (expanded) {
            CountryPicker(
                countries = filteredCountries,
                search = search,
                onSearchChange = { search = it },
                onSelect = { country ->
                    selectedCountry = country
                    expanded = false
                    onValueChange(
                        PhoneNumberValidator.composeE164Candidate(
                            country = country,
                            nationalNumber = nationalNumber
                        )
                    )
                },
                modifier = dismissController?.let {
                    Modifier.dropdownDismissBounds(it, dropdownId, expanded)
                } ?: Modifier
            )
        }
    }
}

@Composable
private fun CountryPicker(
    countries: List<PhoneCountry>,
    search: String,
    onSearchChange: (String) -> Unit,
    onSelect: (PhoneCountry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            placeholder = { Text(stringResource(R.string.search_country_or_code), color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = DarkBlue
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (countries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_countries_found), color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
            ) {
                items(countries, key = { it.regionCode }) { country ->
                    DropdownMenuItem(
                        text = {
                            Text(country.displayLabel, fontSize = 14.sp)
                        },
                        onClick = { onSelect(country) }
                    )
                }
            }
        }
    }
}
