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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.effects.entrance
import com.example.frontend.effects.rememberEntranceTrigger
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.AppTextField
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
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

private val SouthAfricanProvinces = listOf(
    "Eastern Cape",
    "Free State",
    "Gauteng",
    "KwaZulu-Natal",
    "Limpopo",
    "Mpumalanga",
    "North West",
    "Northern Cape",
    "Western Cape"
)

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
    var otpError by remember { mutableStateOf<String?>(null) }
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
                title = "Voter Profile",
                subtitle = "View and manage your registered details",
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
                                text = "Verified Registered Voter",
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
                        title = "Verified Identity Information",
                        description = "Full legal names and South African ID / Passport Number",
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
                            text = if (isEditingIdentity) "Cancel" else "Edit",
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
                                label = "Full Names",
                                value = currentFullName
                            )
                            HorizontalDivider(color = DividerColor)
                            ReadOnlyDetailRow(
                                icon = Icons.Default.AccountBox,
                                label = "National ID / Passport Number",
                                value = formatIdNumber(currentIdNumber),
                                isProtected = true
                            )
                            HorizontalDivider(color = DividerColor)
                            ReadOnlyDetailRow(
                                icon = Icons.Default.Shield,
                                label = "Voting District Status",
                                value = "Active • District 19700183 ($province)"
                            )
                        } else {
                            Text(
                                text = "Full Legal Names",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandNavy
                            )
                            AppTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = "Full Legal Names (e.g. Thandi Nkosi)",
                                leadingIcon = Icons.Default.Person,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "National ID / Passport Number",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandNavy
                            )
                            AppTextField(
                                value = idNumber,
                                onValueChange = { idNumber = it },
                                placeholder = "13-Digit SA ID Number",
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
                                        text = "Biometric Face Recognition required: Updating your legal name or ID number triggers a face scan verification.",
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
                    title = "Contact Information",
                    description = "Your registered cellphone number for SMS notifications"
                )
                AppTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "Cellphone number (e.g. 082 123 4567)",
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
                            text = "Changing your cellphone number requires a 6-digit SMS OTP code.",
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
                    title = "Residential Address",
                    description = "Determines your provincial and regional ballot allocation"
                )

                // Province Dropdown Field
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = province,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Province", color = MutedText) },
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
                                contentDescription = "Select province",
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
                        SouthAfricanProvinces.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item, color = BrandNavy) },
                                onClick = {
                                    province = item
                                    provinceExpanded = false
                                }
                            )
                        }
                    }
                }

                AppTextField(
                    value = city,
                    onValueChange = { city = it },
                    placeholder = "City / Town (e.g. Johannesburg)",
                    leadingIcon = Icons.Default.Home,
                    modifier = Modifier.fillMaxWidth()
                )

                AppTextField(
                    value = street,
                    onValueChange = { street = it },
                    placeholder = "Street Address (e.g. 124 Market Street)",
                    leadingIcon = Icons.Default.Home,
                    modifier = Modifier.fillMaxWidth()
                )

                AppTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    placeholder = "Postal Code (e.g. 2001)",
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
                    label = if (identityHasChanged) "Verify Face & Save Changes" else "Save Profile Changes",
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
                                snackbarHostState.showSnackbar("Residential address updated successfully")
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
                    text = "Face Verification Required",
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
                        text = "To update your legal name or ID number, please complete a biometric face scan to verify your identity.",
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
                                    text = "Matching biometric face template...",
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
                            text = "Hold device at eye level and position your face in the oval.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                SimpleButton(
                    label = if (isVerifyingFace) "Scanning..." else "Verify Face & Save",
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
                                snackbarHostState.showSnackbar("Identity verified. Full Name and ID Number updated!")
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
                    Text("Cancel", color = BrandNavy)
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
                    text = "Verify Cellphone Change",
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
                        text = "A 6-digit SMS OTP code was sent to $phone. Enter it below to confirm your phone number change.",
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
                            text = otpError!!,
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
                            text = if (countdownSeconds > 0) "Resend in ${countdownSeconds}s" else "Didn't receive code?",
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
                            Text("Resend SMS", color = if (countdownSeconds == 0) BrandNavy else MutedText)
                        }
                    }
                }
            },
            confirmButton = {
                SimpleButton(
                    label = if (isVerifyingOtp) "Verifying..." else "Verify & Save",
                    onClick = {
                        if (otpCode.length == 6) {
                            isVerifyingOtp = true
                            scope.launch {
                                delay(600) // Simulate OTP verification check
                                showOtpDialog = false
                                isVerifyingOtp = false
                                currentPhone = phone
                                snackbarHostState.showSnackbar("Cellphone number & profile updated successfully")
                                onSaveProfile(true, phone)
                            }
                        } else {
                            otpError = "Please enter the full 6-digit OTP code"
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
                    Text("Cancel", color = BrandNavy)
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
                contentDescription = "Read only",
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
