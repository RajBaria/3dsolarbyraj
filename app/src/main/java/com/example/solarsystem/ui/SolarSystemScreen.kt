package com.example.solarsystem.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solarsystem.model.AppLanguage
import com.example.solarsystem.model.AstronomicalData
import com.example.solarsystem.model.Localization
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val GlassBackground = Color(0xDD0A0F1E)
private val GlassBorder = Color(0x334488FF)
private val AccentCyan = Color(0xFF00FFCC)
private val AccentGold = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolarSystemScreen(
    viewModel: SolarSystemViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val strings = remember(state.language) { Localization.get(state.language) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var showLangMenu by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    // Convert elapsed days to Calendar date
    val calendar = remember(state.elapsedDays) {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2000, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val msToAdd = (state.elapsedDays * 86400000.0).toLong()
        cal.timeInMillis += msToAdd
        cal
    }
    val dateString = remember(calendar) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.format(Date(calendar.timeInMillis))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = GlassBackground,
                modifier = Modifier.widthIn(max = 340.dp)
            ) {
                DrawerSettingsContent(
                    state = state,
                    strings = strings,
                    onSelectFocus = {
                        viewModel.setFocusTarget(it)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onToggle = { key ->
                        when (key) {
                            "planets" -> viewModel.togglePlanetLabels()
                            "moons" -> viewModel.toggleMoonLabels()
                            "stars" -> viewModel.toggleStarLabels()
                            "constellations" -> viewModel.toggleConstellationLines()
                            "galaxy" -> viewModel.toggleGalaxy()
                            "cometOort" -> viewModel.toggleCometOort()
                            "asteroids" -> viewModel.toggleAsteroidBelt()
                            "solarPaths" -> viewModel.toggleSolarPaths()
                            "helicalPaths" -> viewModel.toggleHelicalPaths()
                            "galOrbit" -> viewModel.toggleGalOrbit()
                        }
                    },
                    onTogglePathItem = { viewModel.togglePathItem(it) },
                    onToggleAllPaths = { viewModel.toggleAllPaths() },
                    onHelpClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.setShowHelpDialog(true)
                    },
                    onAboutClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.setShowAboutDialog(true)
                    },
                    onResetCamera = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.resetCamera()
                    },
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
            // 3D Solar Canvas
            SolarCanvas(
                state = state,
                onRotate = { dyaw, dpitch -> viewModel.rotateCamera(dyaw, dpitch) },
                onZoom = { factor -> viewModel.adjustCameraDistance(factor) },
                onSelectTarget = { viewModel.setFocusTarget(it) }
            )

            // Top Glassmorphic Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, start = 12.dp, end = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBackground)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { coroutineScope.launch { drawerState.open() } },
                    modifier = Modifier.testTag("menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Drawer Menu",
                        tint = AccentCyan
                    )
                }

                Text(
                    text = strings.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                )

                // Language Menu Button
                Box {
                    IconButton(
                        onClick = { showLangMenu = true },
                        modifier = Modifier.testTag("language_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Change Language",
                            tint = AccentGold
                        )
                    }

                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false },
                        modifier = Modifier.background(GlassBackground)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = lang.displayName,
                                        color = if (state.language == lang) AccentCyan else Color.White,
                                        fontWeight = if (state.language == lang) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.setShowAboutDialog(true) },
                    modifier = Modifier.testTag("about_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = strings.aboutApp,
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { viewModel.setShowHelpDialog(true) },
                    modifier = Modifier.testTag("help_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = strings.helpBtn,
                        tint = Color.White
                    )
                }
            }

            // Floating Zoom Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { viewModel.zoomIn() },
                    containerColor = GlassBackground,
                    contentColor = AccentCyan,
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("zoom_in_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                FloatingActionButton(
                    onClick = { viewModel.zoomOut() },
                    containerColor = GlassBackground,
                    contentColor = AccentCyan,
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("zoom_out_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }

                FloatingActionButton(
                    onClick = { viewModel.resetCamera() },
                    containerColor = GlassBackground,
                    contentColor = AccentGold,
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("reset_camera_fab")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = strings.reset)
                }
            }

            // Bottom Control Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassBackground)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // First row: Play/Pause, Speed Selector, Date display, Today button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Play / Pause Button
                    Button(
                        onClick = { viewModel.togglePlayPause() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isPlaying) Color(0xFFEF4444) else Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) strings.pause else strings.play,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (state.isPlaying) strings.pause else strings.play,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Speed Dropdown
                    Box {
                        OutlinedButton(
                            onClick = { showSpeedMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("speed_button")
                        ) {
                            val speedLabel = when (state.timeMultiplier) {
                                1.0 -> "1d/s"
                                30.0 -> "1mo/s"
                                365.25 -> "1yr/s"
                                3652.5 -> "10yr/s"
                                365250.0 -> "1kyr/s"
                                36525000.0 -> "100kyr/s"
                                3652500000.0 -> "10Myr/s"
                                else -> "${state.timeMultiplier.toInt()}x"
                            }
                            Text(text = speedLabel, color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false },
                            modifier = Modifier.background(GlassBackground)
                        ) {
                            val speeds = listOf(
                                "1 Day / sec" to 1.0,
                                "1 Month (30d) / sec" to 30.0,
                                "1 Year (365d) / sec" to 365.25,
                                "10 Years / sec" to 3652.5,
                                "1,000 Years / sec" to 365250.0,
                                "100,000 Years / sec" to 36525000.0,
                                "10M Years / sec (Galaxy)" to 3652500000.0
                            )
                            speeds.forEach { (label, mult) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            color = if (state.timeMultiplier == mult) AccentCyan else Color.White,
                                            fontWeight = if (state.timeMultiplier == mult) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.setTimeMultiplier(mult)
                                        showSpeedMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Date display & Today button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = dateString,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        IconButton(
                            onClick = { viewModel.resetToToday() },
                            modifier = Modifier.size(36.dp).testTag("today_button")
                        ) {
                            Icon(Icons.Default.Today, contentDescription = strings.today, tint = AccentCyan)
                        }
                    }
                }

                // Row 2: Galaxy 360° rotation slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${strings.galSliderText} ${state.galacticAngleDeg.toInt()}°",
                        color = Color(0xFFAABBCC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(100.dp)
                    )
                    Slider(
                        value = state.galacticAngleDeg,
                        onValueChange = { viewModel.setGalacticAngle(it) },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan,
                            inactiveTrackColor = Color(0x44FFFFFF)
                        ),
                        modifier = Modifier.weight(1f).testTag("galaxy_slider")
                    )
                }
            }

            // About App Dialog
            if (state.showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowAboutDialog(false) },
                    containerColor = GlassBackground,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AccentCyan)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.aboutApp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = strings.title,
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Developer: ${strings.developerName}",
                                color = Color.White,
                                fontSize = 13.sp
                            )

                            // Contact Actions
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22FFFFFF))
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${strings.developerPhone}"))
                                        context.startActivity(intent)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(strings.developerPhone, color = Color.White, fontSize = 13.sp)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22FFFFFF))
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${strings.developerEmail}"))
                                        context.startActivity(intent)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(strings.developerEmail, color = Color.White, fontSize = 13.sp)
                            }

                            Text(
                                text = "Instagram: ${strings.developerInstagram}",
                                color = Color(0xFFFF88AA),
                                fontSize = 13.sp
                            )

                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = strings.feedbackPrompt,
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.setShowAboutDialog(false) },
                            modifier = Modifier.testTag("about_dialog_close")
                        ) {
                            Text("OK", color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Help & Guide Dialog
            if (state.showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowHelpDialog(false) },
                    containerColor = GlassBackground,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = AccentCyan)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.helpTitle, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            strings.helpItems.forEach { item ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("✦", color = AccentCyan, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, end = 6.dp))
                                    Text(item, color = Color(0xFFDDDDDD), fontSize = 13.sp)
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.google.com/search?q=Keplerian+Helical+Solar+System+Model")
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth().testTag("google_search_button")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan)
                                Spacer(Modifier.width(6.dp))
                                Text(strings.searchGoogle, color = AccentCyan)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.setShowHelpDialog(false) },
                            modifier = Modifier.testTag("help_dialog_close")
                        ) {
                            Text("OK", color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DrawerSettingsContent(
    state: SimulationState,
    strings: com.example.solarsystem.model.LocalizedStrings,
    onSelectFocus: (String) -> Unit,
    onToggle: (String) -> Unit,
    onTogglePathItem: (String) -> Unit,
    onToggleAllPaths: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onResetCamera: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Drawer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Helical 3D", color = AccentCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Solar System", color = Color.White, fontSize = 14.sp)
            }
            IconButton(onClick = onCloseDrawer) {
                Icon(Icons.Default.Close, contentDescription = "Close Drawer", tint = Color.White)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Focus Target Selector
        Text(
            text = strings.focusTargetLabel,
            color = AccentGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))

        val focusOptions = listOf(
            "free" to strings.freeCam,
            "sun" to (strings.names["sun"] ?: "Sun"),
            "apophis" to (strings.names["apophis"] ?: "Apophis"),
            "halley" to (strings.names["halley"] ?: "Halley")
        ) + AstronomicalData.planets.map { it.key to (strings.names[it.key] ?: it.key) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            focusOptions.forEach { (key, label) ->
                val selected = state.focusTarget == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) AccentCyan else Color(0x22FFFFFF))
                        .clickable { onSelectFocus(key) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (selected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Display Toggles Section
        Text(
            text = "Visual Layers & Labels",
            color = AccentGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))

        LayerToggleItem(label = strings.planetLabels, checked = state.showPlanetLabels) { onToggle("planets") }
        LayerToggleItem(label = strings.moonLabels, checked = state.showMoonLabels) { onToggle("moons") }
        LayerToggleItem(label = strings.starLabels, checked = state.showStarLabels) { onToggle("stars") }
        LayerToggleItem(label = strings.constellationLines, checked = state.showConstellationLines) { onToggle("constellations") }
        LayerToggleItem(label = strings.galaxyView, checked = state.showGalaxy) { onToggle("galaxy") }
        LayerToggleItem(label = strings.cometOort, checked = state.showCometOort) { onToggle("cometOort") }
        LayerToggleItem(label = strings.asteroidBelt, checked = state.showAsteroidBelt) { onToggle("asteroids") }
        LayerToggleItem(label = strings.solarPaths, checked = state.showSolarPaths) { onToggle("solarPaths") }
        LayerToggleItem(label = strings.helicalPaths, checked = state.showHelicalPaths) { onToggle("helicalPaths") }
        LayerToggleItem(label = strings.galOrbit, checked = state.showGalOrbit) { onToggle("galOrbit") }

        Spacer(Modifier.height(16.dp))

        // Orbit Visibility per Body
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(strings.visibility, color = AccentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onToggleAllPaths) {
                Text(strings.toggleAll, color = AccentCyan, fontSize = 12.sp)
            }
        }

        val allBodies = listOf("sun", "apophis") + AstronomicalData.planets.map { it.key }
        allBodies.forEach { key ->
            val checked = state.pathVisibility[key] ?: true
            val label = strings.names[key] ?: key
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTogglePathItem(key) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { onTogglePathItem(key) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentCyan,
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.Black
                    )
                )
                Text(text = label, color = Color.White, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Action Buttons
        Button(
            onClick = onResetCamera,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x334488FF)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = AccentCyan)
            Spacer(Modifier.width(6.dp))
            Text(strings.reset, color = Color.White)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onHelpClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x334488FF)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = AccentCyan)
            Spacer(Modifier.width(6.dp))
            Text(strings.helpBtn, color = Color.White)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onAboutClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x334488FF)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = AccentGold)
            Spacer(Modifier.width(6.dp))
            Text(strings.aboutApp, color = Color.White)
        }
    }
}

@Composable
fun LayerToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = AccentCyan,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0x22FFFFFF)
            )
        )
    }
}
