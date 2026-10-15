package com.example.frontend.ui.screens.Profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.effects.entrance
import com.example.frontend.effects.rememberEntranceTrigger
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.AppTextField
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.Otp.formatCountdown
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.screens.Registration.Province
import com.example.frontend.ui.theme.BrandNavy
import com.example.frontend.ui.theme.BrandNavyDeep
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.DividerColor
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.GoldAccent
import com.example.frontend.ui.theme.InfoContainer
import com.example.frontend.ui.theme.InfoContent
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessBorder
import com.example.frontend.ui.theme.SuccessContainer
import com.example.frontend.ui.theme.SuccessContent
import com.example.frontend.ui.theme.WarningContainer
import com.example.frontend.ui.theme.WarningText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Translated label for a stored province name; unknown names are shown as-is. */
@Composable
private fun provinceLabel(apiName: String): String =
    Province.fromApiName(apiName)?.let { stringResource(it.displayNameRes) } ?: apiName

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialName: String = "Thandi Nkosi",
    initialIdNumber: String = "9201185082089",
    initialPhone: String = "0821234567",
    initialStreet: String = "124 Market Street",
    initialCity: String = "Johannesburg",
    initialProvince: String = "Gauteng",
    initialPostalCode: String = "2001",
    onSaveProfile: (phoneChanged: Boolean, newPhone: String) -> Unit = { _, _ -> },
    onSaveIdentity: (newName: String, newIdNumber: String) -> Unit = { _, _ -> },
    selectedTab: BottomTab = BottomTab.Settings,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val entered by rememberEntranceTrigger()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Form fields local states initialized with registration data or voter defaults
    val effectiveName = initialName.ifBlank { "Thandi Nkosi" }
    val effectiveIdNumber = initialIdNumber.ifBlank { "9201185082089" }
    val effectivePhone = initialPhone.ifBlank { "0821234567" }
    val effectiveStreet = initialStreet.ifBlank { "124 Market Street" }
    val effectiveCity = initialCity.ifBlank { "Johannesburg" }
    val effectiveProvince = initialProvince.ifBlank { "Gauteng" }
    val effectivePostalCode = initialPostalCode.ifBlank { "2001" }

    // Legal identity fields (Name and ID)
    var fullName by rememberSaveable { mutableStateOf(effectiveName) }
    var currentFullName by rememberSaveable { mutableStateOf(effectiveName) }

    var idNumber by rememberSaveable { mutableStateOf(effectiveIdNumber) }
    var currentIdNumber by rememberSaveable { mutableStateOf(effectiveIdNumber) }

    var isEditingIdentity by remember { mutableStateOf(false) }

    // Contact & address fields
    var phone by rememberSaveable { mutableStateOf(effectivePhone) }
    var currentPhone by rememberSaveable { mutableStateOf(effectivePhone) }

    var street by rememberSaveable { mutableStateOf(effectiveStreet) }
    var city by rememberSaveable { mutableStateOf(effectiveCity) }
    var province by rememberSaveable { mutableStateOf(effectiveProvince) }
    var postalCode by rememberSaveable { mutableStateOf(effectivePostalCode) }

    var provinceExpanded by remember { mutableStateOf(false) }

    // Biometric Face Verification Modal states for Name/ID update
    var showFaceScanDialog by remember { mutableStateOf(false) }
    var isVerifyingFace by remember { mutableStateOf(false) }
    var faceVerifiedSuccess by remember { mutableStateOf(false) }
    var faceScanError by remember { mutableStateOf<String?>(null) }

    // OTP Modal states for Cellphone update
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<Int?>(null) }

    // Resolved up front: snackbars are shown from coroutines, outside composition.
    val addressUpdatedMessage = stringResource(R.string.profile_snackbar_address_updated)
    val identityUpdatedMessage = stringResource(R.string.profile_snackbar_identity_updated)
    val phoneUpdatedMessage = stringResource(R.string.profile_snackbar_phone_updated)
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf(30) }

    val identityHasChanged = (fullName.trim() != currentFullName.trim() || idNumber.trim() != currentIdNumber.trim()) &&
        fullName.isNotBlank() && idNumber.isNotBlank()
    val phoneHasChanged = phone.trim() != currentPhone.trim() && phone.isNotBlank()
    val addressHasChanged = (street.trim() != initialStreet.trim() ||
        city.trim() != initialCity.trim() ||
        province.trim() != initialProvince.trim() ||
        postalCode.trim() != initialPostalCode.trim()) &&
        street.isNotBlank() && city.isNotBlank() && province.isNotBlank() && postalCode.isNotBlank()

    val isFormValid = fullName.isNotBlank() &&
        idNumber.isNotBlank() &&
        phone.isNotBlank() &&
        street.isNotBlank() &&
        city.isNotBlank() &&
        province.isNotBlank() &&
        postalCode.isNotBlank()

    val hasPendingChanges = (identityHasChanged || phoneHasChanged || addressHasChanged) && isFormValid

    // Countdown timer effect for OTP dialog
    LaunchedEffect(showOtpDialog, countdownSeconds) {
        if (showOtpDialog && countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_profile_title),
                subtitle = stringResource(R.string.profile_subtitle),
                onBackClick = onBackClick
            )
        },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.section)
        ) {
            // Profile Badge Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 0),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.card),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.item)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(BrandNavy, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentFullName.split(" ")
                                .mapNotNull { it.firstOrNull()?.uppercase() }
                                .take(2)
                                .joinToString(""),
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = currentFullName,
                            style = MaterialTheme.typography.titleMedium,
                            color = BrandNavy,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessContent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.profile_verified_voter),
                                style = MaterialTheme.typography.labelMedium,
                                color = SuccessContent
                            )
                        }
                    }
                }
            }

            // Section 1: Identity Information (Name & ID - Requires Face Verification)
            Column(
                modifier = Modifier.entrance(entered, index = 1),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeading(
                        title = stringResource(R.string.profile_identity_title),
                        description = stringResource(R.string.profile_identity_description),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { isEditingIdentity = !isEditingIdentity }
                    ) {
                        Icon(
                            imageVector = if (isEditingIdentity) Icons.Default.Lock else Icons.Default.Edit,
                            contentDescription = null,
                            tint = BrandNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(if (isEditingIdentity) R.string.action_cancel else R.string.action_edit),
                            color = BrandNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.item),
                        verticalArrangement = Arrangement.spacedBy(Spacing.item)
                    ) {
                        if (!isEditingIdentity) {
                            ReadOnlyDetailRow(
                                icon = Icons.Default.Person,
                                label = stringResource(R.string.profile_full_names),
                                value = currentFullName
                            )
                            HorizontalDivider(color = DividerColor)
                            ReadOnlyDetailRow(
                                icon = Icons.Default.AccountBox,
                                label = stringResource(R.string.profile_id_or_passport),
                                value = formatIdNumber(currentIdNumber),
                                isProtected = true
                            )
                            HorizontalDivider(color = DividerColor)
                            ReadOnlyDetailRow(
                                icon = Icons.Default.Shield,
                                label = stringResource(R.string.profile_district_status),
                                value = stringResource(
                                    R.string.profile_district_value,
                                    "19700183",
                                    provinceLabel(province)
                                )
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.profile_full_legal_names),
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandNavy
                            )
                            AppTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = stringResource(R.string.profile_full_legal_names_hint),
                                leadingIcon = Icons.Default.Person,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = stringResource(R.string.profile_id_or_passport),
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandNavy
                            )
                            AppTextField(
                                value = idNumber,
                                onValueChange = { idNumber = it },
                                placeholder = stringResource(R.string.profile_id_hint),
                                leadingIcon = Icons.Default.AccountBox,
                                keyboardType = KeyboardType.Number,
                                maxLength = 13,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Surface(
                                color = WarningContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = WarningText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.profile_identity_face_warning),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WarningText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Contact Details (Editable Cellphone)
            Column(
                modifier = Modifier.entrance(entered, index = 2),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SectionHeading(
                    title = stringResource(R.string.profile_contact_title),
                    description = stringResource(R.string.profile_contact_description)
                )
                AppTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = stringResource(R.string.profile_phone_hint),
                    leadingIcon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.fillMaxWidth()
                )
                Surface(
                    color = WarningContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = WarningText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.profile_phone_otp_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningText
                        )
                    }
                }
            }

            // Section 3: Registered Address Details (Editable)
            Column(
                modifier = Modifier.entrance(entered, index = 3),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SectionHeading(
                    title = stringResource(R.string.profile_address_title),
                    description = stringResource(R.string.profile_address_description)
                )

                // Province Dropdown Field
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = provinceLabel(province),
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text(stringResource(R.string.address_province), color = MutedText) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = stringResource(R.string.profile_select_province),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = CardBorder,
                            disabledTextColor = MaterialTheme.colorScheme.primary,
                            disabledLabelColor = MutedText,
                            disabledContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { provinceExpanded = true }
                    )
                    DropdownMenu(
                        expanded = provinceExpanded,
                        onDismissRequest = { provinceExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Province.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = stringResource(item.displayNameRes), color = BrandNavy) },
                                onClick = {
                                    province = item.apiName
                                    provinceExpanded = false
                                }
                            )
                        }
                    }
                }

                AppTextField(
                    value = city,
                    onValueChange = { city = it },
                    placeholder = stringResource(R.string.profile_city_hint),
                    leadingIcon = Icons.Default.Home,
                    modifier = Modifier.fillMaxWidth()
                )

                AppTextField(
                    value = street,
                    onValueChange = { street = it },
                    placeholder = stringResource(R.string.profile_street_hint),
                    leadingIcon = Icons.Default.Home,
                    modifier = Modifier.fillMaxWidth()
                )

                AppTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    placeholder = stringResource(R.string.profile_postal_hint),
                    leadingIcon = Icons.Default.Badge,
                    keyboardType = KeyboardType.Number,
                    maxLength = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Section 4: Action Button
            Column(
                modifier = Modifier.entrance(entered, index = 4),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SimpleButton(
                    label = stringResource(
                        if (identityHasChanged) R.string.profile_verify_face_save else R.string.profile_save_changes
                    ),
                    onClick = {
                        if (identityHasChanged) {
                            showFaceScanDialog = true
                            faceScanError = null
                        } else if (phoneHasChanged) {
                            showOtpDialog = true
                            countdownSeconds = 30
                            otpCode = ""
                            otpError = null
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(addressUpdatedMessage)
                            }
                            onSaveProfile(false, phone)
                        }
                    },
                    enabled = hasPendingChanges,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Primary,
                    leadingIcon = if (identityHasChanged) Icons.Default.Face else null
                )
            }
        }
    }

    // Biometric Face Recognition Verification Dialog for Name/ID update
    if (showFaceScanDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isVerifyingFace) {
                    showFaceScanDialog = false
                    faceScanError = null
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    tint = BrandNavy,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.profile_face_required_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandNavy,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.item),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.profile_face_required_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText,
                        textAlign = TextAlign.Center
                    )

                    // Face Scanner Oval Frame Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandNavyDeep),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val w = size.width * 0.55f
                            val h = w * 1.3f
                            drawOval(
                                color = if (faceVerifiedSuccess) SuccessBorder else Color.White.copy(alpha = 0.8f),
                                topLeft = Offset((size.width - w) / 2, (size.height - h) / 2),
                                size = Size(w, h),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        if (isVerifyingFace) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.small)
                            ) {
                                CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(36.dp))
                                Text(
                                    text = stringResource(R.string.profile_face_matching),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        } else if (faceVerifiedSuccess) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessBorder,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    if (faceScanError != null) {
                        Text(
                            text = faceScanError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.profile_face_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                SimpleButton(
                    label = stringResource(
                        if (isVerifyingFace) R.string.profile_scanning else R.string.profile_verify_face_and_save
                    ),
                    onClick = {
                        isVerifyingFace = true
                        faceScanError = null
                        scope.launch {
                            delay(1200L) // Simulate biometric facial template matching
                            isVerifyingFace = false
                            faceVerifiedSuccess = true
                            delay(400L)
                            showFaceScanDialog = false
                            faceVerifiedSuccess = false

                            // Commit Identity changes
                            currentFullName = fullName
                            currentIdNumber = idNumber
                            isEditingIdentity = false
                            onSaveIdentity(fullName, idNumber)

                            // If phone also changed, trigger OTP
                            if (phoneHasChanged) {
                                showOtpDialog = true
                                countdownSeconds = 30
                                otpCode = ""
                            } else {
                                snackbarHostState.showSnackbar(identityUpdatedMessage)
                                onSaveProfile(false, phone)
                            }
                        }
                    },
                    enabled = !isVerifyingFace,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Primary,
                    leadingIcon = Icons.Default.Face
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showFaceScanDialog = false
                        faceScanError = null
                    },
                    enabled = !isVerifyingFace
                ) {
                    Text(stringResource(R.string.action_cancel), color = BrandNavy)
                }
            }
        )
    }

    // OTP Verification Dialog for Phone Change
    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = {
                showOtpDialog = false
                otpCode = ""
                otpError = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = BrandNavy,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.profile_otp_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandNavy
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.item),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.profile_otp_body, phone),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText,
                        textAlign = TextAlign.Center
                    )

                    AppTextField(
                        value = otpCode,
                        onValueChange = {
                            if (it.length <= 6) {
                                otpCode = it
                                otpError = null
                            }
                        },
                        placeholder = "0 0 0 0 0 0",
                        keyboardType = KeyboardType.Number,
                        maxLength = 6,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (otpError != null) {
                        Text(
                            text = stringResource(otpError!!),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (countdownSeconds > 0) {
                                stringResource(R.string.otp_resend_in, formatCountdown(countdownSeconds))
                            } else {
                                stringResource(R.string.profile_otp_not_received)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                        TextButton(
                            enabled = countdownSeconds == 0,
                            onClick = {
                                countdownSeconds = 30
                                otpCode = ""
                                otpError = null
                            }
                        ) {
                            Text(
                                stringResource(R.string.profile_otp_resend_sms),
                                color = if (countdownSeconds == 0) BrandNavy else MutedText
                            )
                        }
                    }
                }
            },
            confirmButton = {
                SimpleButton(
                    label = stringResource(
                        if (isVerifyingOtp) R.string.register_verifying else R.string.profile_verify_and_save
                    ),
                    onClick = {
                        if (otpCode.length == 6) {
                            isVerifyingOtp = true
                            scope.launch {
                                delay(600) // Simulate OTP verification check
                                showOtpDialog = false
                                isVerifyingOtp = false
                                currentPhone = phone
                                snackbarHostState.showSnackbar(phoneUpdatedMessage)
                                onSaveProfile(true, phone)
                            }
                        } else {
                            otpError = R.string.profile_otp_incomplete
                        }
                    },
                    enabled = otpCode.length == 6 && !isVerifyingOtp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showOtpDialog = false
                        otpCode = ""
                        otpError = null
                    }
                ) {
                    Text(stringResource(R.string.action_cancel), color = BrandNavy)
                }
            }
        )
    }
}

/** Component for displaying read-only identity detail rows. */
@Composable
private fun ReadOnlyDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isProtected: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.item)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(InfoContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = InfoContent,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = BrandNavy,
                fontWeight = FontWeight.Medium
            )
        }
        if (isProtected) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = stringResource(R.string.profile_read_only),
                tint = MutedText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatIdNumber(id: String): String {
    val clean = id.filter { it.isDigit() }
    return if (clean.length == 13) {
        "${clean.substring(0, 6)} ${clean.substring(6, 10)} ${clean.substring(10)}"
    } else {
        id
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    FrontendTheme {
        ProfileScreen(onBackClick = {})
    }
}
