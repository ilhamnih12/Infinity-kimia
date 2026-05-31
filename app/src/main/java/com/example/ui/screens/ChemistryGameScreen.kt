package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Substance
import com.example.data.repository.ReactionResult
import com.example.data.repository.SupabaseSyncResult
import com.example.viewmodel.ChemistryViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChemistryGameScreen(
    viewModel: ChemistryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allSubstances by viewModel.allSubstances.collectAsStateWithLifecycle()
    val discoveredSubstances by viewModel.discoveredSubstances.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("inventory") } // "inventory", "periodic", "cloud"
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Background dark space
                drawRect(Color(0xFF050505))
                
                // Nebula radial glow 1 (Top-Left, deep purple)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E0B36).copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height * 0.2f),
                        radius = size.width * 0.8f
                    ),
                    center = Offset(size.width * 0.2f, size.height * 0.2f),
                    radius = size.width * 0.8f
                )
                
                // Nebula radial glow 2 (Bottom-Right, teal/cyan)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF082626).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.8f),
                        radius = size.width * 0.7f
                    ),
                    center = Offset(size.width * 0.8f, size.height * 0.8f),
                    radius = size.width * 0.7f
                )
            },
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Brand Side
                    Column {
                        Text(
                            text = "INFINITE SYNTHESIS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF),
                            letterSpacing = 1.8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "NEO-LAB",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "v4.2",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Right Status / Info Icon Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isSupabaseConfigured = viewModel.settingsUrlInput.isNotEmpty() && viewModel.settingsKeyInput.isNotEmpty()
                        
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "CLOUD SYNC",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSupabaseConfigured) Color(0xFF4ADE80) else Color(0xFFFF6D00))
                                )
                                Text(
                                    text = if (isSupabaseConfigured) "Configured" else "Local Only",
                                    fontSize = 11.sp,
                                    color = if (isSupabaseConfigured) Color(0xFF4ADE80) else Color(0xFFFFB300),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Glass icon box for dialog
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                               .clickable { showHelpDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Show Help Guide",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // CENTRAL WORKSPACE: REACTION CHAMBER
            ReactionChamberConsole(
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
            )

            // DISCOVERY PROGRESS CARD
            val discoveredCount = discoveredSubstances.size
            val totalCount = allSubstances.size
            val progressPercent = if (totalCount > 0) (discoveredCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DISCOVERY PROGRESS",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$discoveredCount / $totalCount",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Gradient Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressPercent)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF00E5FF), Color(0xFF10B981))
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // FLOATING CAPSULE NAVIGATION
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CapsuleTabButton(
                    label = "Discovered",
                    icon = Icons.Default.Check,
                    isActive = activeTab == "inventory",
                    accentColor = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f).testTag("discovered_inventory_tab"),
                    onClick = { activeTab = "inventory" }
                )
                CapsuleTabButton(
                    label = "Catalog",
                    icon = Icons.Default.Menu,
                    isActive = activeTab == "periodic",
                    accentColor = Color(0xFFFF6D00),
                    modifier = Modifier.weight(1f).testTag("periodic_table_tab"),
                    onClick = { activeTab = "periodic" }
                )
                CapsuleTabButton(
                    label = "Cloud Sync",
                    icon = Icons.Default.Refresh,
                    isActive = activeTab == "cloud",
                    accentColor = Color(0xFFD500F9),
                    modifier = Modifier.weight(1f).testTag("sync_tab"),
                    onClick = { activeTab = "cloud" }
                )
            }

            // BOTTOM COLLAPSED DISPLAY MODULES BASED ON TAB SELECTION
            Box(
                modifier = Modifier
                    .height(290.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0C1222).copy(alpha = 0.9f))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                when (activeTab) {
                    "inventory" -> DiscoveredInventoryTab(
                        discoveredList = discoveredSubstances,
                        selectedFilter = viewModel.selectedCategoryFilter,
                        onFilterChanged = { viewModel.selectedCategoryFilter = it },
                        onSelectProduct = { viewModel.selectReactant(it) }
                    )
                    "periodic" -> PeriodicTableTab(
                        substances = allSubstances,
                        onSelectProduct = {
                            if (it.discovered) {
                                viewModel.selectReactant(it)
                            } else {
                                Toast.makeText(context, "${it.name} is currently locked! Discover it using precursors.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    "cloud" -> CloudSyncTab(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // SCI-FI INSTRUCTIONS DIALOG
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("I UNDERSTAND", color = Color(0xFF00E5FF), fontFamily = FontFamily.Monospace)
                }
            },
            title = {
                Text(
                    text = "LABORATORY DIRECTIVE",
                    color = Color(0xFF00E5FF),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Welcome to the ultimate molecular fusion engine! Here are your instructions:\n\n" +
                            "1. Click elements from the Discovered list below to place them in Slot 1 and Slot 2 of the Reaction Chamber.\n\n" +
                            "2. Tap 'REACT' to trigger fusion. Standard reactions (like H + O) occur instantly locally. Custom exotic combinations will invoke the Gemini 3.5-flash AI synthesizer to analyze physical bounds!\n\n" +
                            "3. Discovered compounds appear in your inventory and periodic sheet. Track atomic numbers to unlock locked elements!\n\n" +
                            "4. Set up Supabase free tier REST API in Cloud Sync to secure your molecular achievements permanently.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            },
            containerColor = Color(0xFF0F172A),
            textContentColor = Color.White
        )
    }
}

@Composable
fun CapsuleTabButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBackground by animateColorAsState(
        targetValue = if (isActive) accentColor else Color.Transparent,
        label = "capsule_tab_bg"
    )
    val animatedLabelColor by animateColorAsState(
        targetValue = if (isActive) Color.Black else Color.White.copy(alpha = 0.4f),
        label = "capsule_tab_text"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(animatedBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = animatedLabelColor,
            modifier = Modifier.size(18.dp)
        )
        AnimatedVisibility(
            visible = isActive,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Text(
                text = label,
                color = animatedLabelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 6.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun ReactionChamberConsole(
    viewModel: ChemistryViewModel,
    modifier: Modifier = Modifier
) {
    val r1 = viewModel.reactionSlot1
    val r2 = viewModel.reactionSlot2
    val isRunning = viewModel.isReactionRunning
    val result = viewModel.reactionResult
    val context = LocalContext.current

    // Infinite transitions for rotating orbits matching the Immersive theme
    val infiniteTransition = rememberInfiniteTransition(label = "orbits")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_spin"
    )
    val reverseRotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_spin_reverse"
    )

    // Pulse animation for the synthesize glow button when active
    val pulseTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_value"
    )

    val isReady = r1 != null && r2 != null

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("reaction_chamber"),
        contentAlignment = Alignment.Center
    ) {
        // Holographic Canvas backing grid or background spark glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth / 2, canvasHeight / 2)
            
            // Outer dashed orbit (from HTML orbit)
            this.withTransform({
                rotate(rotationAngle, center)
            }) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = 110.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(15f, 15f), 0f
                        )
                    )
                )
            }
            
            // Inner dotted orbit (reverse direction)
            this.withTransform({
                rotate(reverseRotationAngle, center)
            }) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = 80.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 1f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(4f, 10f), 0f
                        )
                    )
                )
            }

            // Draw connecting flow vectors when ingredients are loaded
            if (r1 != null) {
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                    start = Offset(canvasWidth * 0.25f, canvasHeight / 2),
                    end = center,
                    strokeWidth = 2.dp.toPx()
                )
            }
            if (r2 != null) {
                drawLine(
                    color = Color(0xFFD500F9).copy(alpha = 0.2f),
                    start = Offset(canvasWidth * 0.75f, canvasHeight / 2),
                    end = center,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // FISSION PLASMA VORTEX SIMULATOR ANIMATOR
        if (isRunning) {
            ReactionSwirlingVortex(
                color1 = Color(0xFF00E5FF),
                color2 = Color(0xFFD500F9),
                modifier = Modifier.size(160.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Chamber Slots Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SLOT 1
                ReactantSlot(
                    reactant = r1,
                    onRemove = { viewModel.removeSlot1() },
                    fallbackLabel = "REACTANT ALPHA",
                    slotColor = Color(0xFF00E5FF),
                    modifier = Modifier.testTag("slot_1_card")
                )

                // VS core plus sign inside chemical reaction
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace
                    )
                }

                // SLOT 2
                ReactantSlot(
                    reactant = r2,
                    onRemove = { viewModel.removeSlot2() },
                    fallbackLabel = "REACTANT BETA",
                    slotColor = Color(0xFFD500F9),
                    modifier = Modifier.testTag("slot_2_card")
                )
            }

            // MID OUTPUT DISPLAY LOGICAL PANEL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isRunning -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "STABILIZING CHEMICAL MATRIX...",
                                fontSize = 11.sp,
                                color = Color(0xFF00E5FF),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            LinearProgressIndicator(
                                color = Color(0xFF00E5FF),
                                trackColor = Color(0xFF00E5FF).copy(alpha = 0.1f),
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(3.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                    result != null -> {
                        when (result) {
                            is ReactionResult.Success -> {
                                DynamicDiscoveredBanner(
                                    substance = result.substance,
                                    toastMsg = result.message
                                )
                            }
                            is ReactionResult.NoReaction -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = result.reason,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = TextAlign.Center,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            is ReactionResult.Error -> {
                                Text(
                                    text = result.message,
                                    color = Color(0xFFE53935),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = if (r1 == null || r2 == null) "HOLOGRAPHIC INJECTOR: READY\nSelect or drag reactants for atomic forging."
                            else "INGESTION SUCCESSFUL\nReady to synthesize molecular structures.",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // FORGE ACTIONS BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.clearChamber() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Console",
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }

                // Synthesize button with glowing aura styling
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing aura background when ready
                    if (isReady && !isRunning) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF00E5FF).copy(alpha = 0.25f * pulseScale),
                                                Color.Transparent
                                            )
                                        ),
                                        radius = (size.minDimension / 1.5f) * pulseScale
                                    )
                                }
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerReaction() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReady) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.04f),
                            contentColor = if (isReady) Color.Black else Color.White.copy(alpha = 0.2f),
                            disabledContainerColor = Color.White.copy(alpha = 0.04f),
                            disabledContentColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("synthesize_button")
                            .border(
                                1.dp,
                                if (isReady) Color(0xFF00E5FF).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = isReady && !isRunning
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "SYNTHESIZE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReactantSlot(
    reactant: Substance?,
    onRemove: () -> Unit,
    fallbackLabel: String,
    slotColor: Color,
    modifier: Modifier = Modifier
) {
    if (reactant != null) {
        val glowingBorder = Brush.sweepGradient(
            colors = listOf(
                slotColor,
                slotColor.copy(alpha = 0.2f),
                slotColor
            )
        )
        Box(
            modifier = modifier
                .size(width = 110.dp, height = 115.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                .border(2.dp, glowingBorder, RoundedCornerShape(14.dp))
                .clickable { onRemove() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = reactant.formula,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = slotColor,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = reactant.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "TAP TO REMOVE",
                    fontSize = 7.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .size(width = 110.dp, height = 115.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF070B14).copy(alpha = 0.4f))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fallbackLabel,
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.2f),
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ReactionSwirlingVortex(
    color1: Color,
    color2: Color,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            phase += 0.05f
            delay(16) // ~60fps lightweight draw phase
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)

        // Draw multiple glowing rotating curves
        for (i in 0 until 4) {
            val offsetAngle = i * (Math.PI / 2).toFloat()
            val finalAngle = phase + offsetAngle
            val radiusX = (width / 2) * cos(finalAngle)
            val radiusY = (height / 2) * sin(finalAngle)

            drawCircle(
                color = if (i % 2 == 0) color1 else color2,
                radius = 8.dp.toPx(),
                center = Offset(center.x + radiusX, center.y + radiusY)
            )

            // Draw faint sweeping path lines in vortex
            drawCircle(
                color = Color.White.copy(alpha = 0.02f),
                radius = (width / 2) - (i * 10f),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
fun DynamicDiscoveredBanner(
    substance: Substance,
    toastMsg: String
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // glowing atom graphic
        val customColor = try {
            Color(android.graphics.Color.parseColor(substance.colorHex))
        } catch (e: Exception) {
            Color(0xFF00E5FF)
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(customColor.copy(alpha = 0.15f))
                .border(2.dp, customColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = substance.formula,
                color = customColor,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        Column(
            modifier = Modifier.weight(1.0f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "DISCOVERED!",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                Text(
                    text = substance.category.uppercase(),
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = substance.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = substance.description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
fun DiscoveredInventoryTab(
    discoveredList: List<Substance>,
    selectedFilter: String,
    onFilterChanged: (String) -> Unit,
    onSelectProduct: (Substance) -> Unit
) {
    val filters = listOf("All", "Elements", "Compounds", "Acids", "Salts", "Gases")
    
    // Perform categorizations based on labels
    val filteredList = when (selectedFilter) {
        "All" -> discoveredList
        "Elements" -> discoveredList.filter { it.isElement }
        "Compounds" -> discoveredList.filter { !it.isElement }
        "Acids" -> discoveredList.filter { it.category.contains("acid", true) }
        "Salts" -> discoveredList.filter { it.category.contains("salt", true) || it.category.contains("halide", true) }
        "Gases" -> discoveredList.filter { it.category.contains("gas", true) }
        else -> discoveredList
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Horizontal categorization filter row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filters) { category ->
                val isSelected = selectedFilter == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B))
                        .clickable { onFilterChanged(category) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No substances match this category yet.\nDiscover compounds using basic elements!",
                    color = Color.White.copy(alpha = 0.3f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(80.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredList) { substance ->
                    val colorHex = try {
                        Color(android.graphics.Color.parseColor(substance.colorHex))
                    } catch (e: Exception) {
                        Color(0xFF00E5FF)
                    }
                    Box(
                        modifier = Modifier
                            .height(78.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, colorHex.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { onSelectProduct(substance) }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = substance.formula,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorHex
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = substance.name,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodicTableTab(
    substances: List<Substance>,
    onSelectProduct: (Substance) -> Unit
) {
    // Collect active group mapping from substances list. Standard periodic catalog elements has Row and Group defined.
    // Let's filter only basic periodic elements!
    val periodicElements = substances.filter { it.isElement }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "PERIODIC CATALOG OF ELEMENTS (PERIOD 1-4)",
            fontSize = 10.sp,
            color = Color(0xFF00E5FF),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Table scroll view (sideways horizontal scroll holds accurate 1-18 columns layouts without squishing, which looks extremely clean and authentic!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .horizontalScroll(rememberScrollState())
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Return 4 periods (rows)
                for (period in 1..4) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Return 18 columns
                        for (group in 1..18) {
                            val element = periodicElements.find { it.periodicPeriod == period && it.periodicGroup == group }
                            if (element != null) {
                                val colorMapped = try {
                                    Color(android.graphics.Color.parseColor(element.colorHex))
                                } catch (e: Exception) {
                                    Color(0xFF00E5FF)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(width = 46.dp, height = 46.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (element.discovered) Color(0xFF0F172A)
                                            else Color(0xFF090D16).copy(alpha = 0.5f)
                                        )
                                        .border(
                                            1.dp,
                                            if (element.discovered) colorMapped.copy(alpha = 0.7f)
                                            else Color.White.copy(alpha = 0.05f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { onSelectProduct(element) }
                                ) {
                                    if (element.discovered) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = element.atomicNumber.toString(),
                                                fontSize = 7.sp,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.align(Alignment.Start)
                                            )
                                            Text(
                                                text = element.formula,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = colorMapped
                                            )
                                            Text(
                                                text = element.name.uppercase(),
                                                fontSize = 6.sp,
                                                color = Color.White.copy(alpha = 0.8f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Clip
                                            )
                                        }
                                    } else {
                                        // Locked element display holding question mark
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = element.atomicNumber.toString(),
                                                fontSize = 7.sp,
                                                color = Color.White.copy(alpha = 0.15f),
                                                modifier = Modifier.align(Alignment.Start)
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.1f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = element.formula,
                                                fontSize = 7.sp,
                                                color = Color.White.copy(alpha = 0.15f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Empty square in correct table classification positions
                                Box(modifier = Modifier.size(width = 46.dp, height = 46.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CloudSyncTab(
    viewModel: ChemistryViewModel
) {
    var urlInput by remember(viewModel.settingsUrlInput) { mutableStateOf(viewModel.settingsUrlInput) }
    var keyInput by remember(viewModel.settingsKeyInput) { mutableStateOf(viewModel.settingsKeyInput) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CLOUDS PROGRESS SYNCHRONIZATION",
                fontSize = 11.sp,
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = "Your unique Player ID: ${viewModel.getPlayerUuid()}",
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace
        )

        // CONFIGURATION TEXTFIELDS
        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Supabase Project URL", fontSize = 11.sp) },
            placeholder = { Text("https://your-proj.supabase.co", fontSize = 10.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedLabelColor = Color(0xFF00E5FF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("supabase_url_input"),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, color = Color.White, fontSize = 11.sp)
        )

        OutlinedTextField(
            value = keyInput,
            onValueChange = { keyInput = it },
            label = { Text("Supabase ANON KEY", fontSize = 11.sp) },
            placeholder = { Text("eyJhbGciOi...", fontSize = 10.sp) },
            singleLine = true,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedLabelColor = Color(0xFF00E5FF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("supabase_key_input"),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, color = Color.White, fontSize = 11.sp)
        )

        // SYNC ACTION ACTIONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    viewModel.saveConfigAndSync(urlInput, keyInput)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = urlInput.isNotEmpty() && keyInput.isNotEmpty() && !viewModel.isSyncing,
                modifier = Modifier
                    .weight(1f)
                    .testTag("supabase_sync_button")
            ) {
                if (viewModel.isSyncing) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                } else {
                    Text("SAVE & SYNC CLOUD", fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
            }

            if (viewModel.settingsUrlInput.isNotEmpty() && viewModel.settingsKeyInput.isNotEmpty()) {
                Button(
                    onClick = { viewModel.syncProgress() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !viewModel.isSyncing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("SYNC PROGRESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // SYNC STATE FEEDBACK LABEL
        viewModel.syncResultState?.let { state ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (state) {
                            is SupabaseSyncResult.Success -> Color(0xFF1B5E20).copy(alpha = 0.15f)
                            is SupabaseSyncResult.Failure -> Color(0xFFB71C1C).copy(alpha = 0.15f)
                            is SupabaseSyncResult.NotConfigured -> Color(0xFFE65100).copy(alpha = 0.15f)
                        }
                    )
                    .border(
                        1.dp,
                        when (state) {
                            is SupabaseSyncResult.Success -> Color(0xFF388E3C)
                            is SupabaseSyncResult.Failure -> Color(0xFFD32F2F)
                            is SupabaseSyncResult.NotConfigured -> Color(0xFFF57C00)
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = when (state) {
                        is SupabaseSyncResult.Success -> state.message
                        is SupabaseSyncResult.Failure -> state.error
                        is SupabaseSyncResult.NotConfigured -> state.prompt
                    },
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

        // SYNC INSTRUCTIONS NOTES
        Text(
            text = "SETUP INSTRUCTIONS:\n" +
                    "To set up your database progress sync:\n" +
                    "1. Head to supabase.com and provision a free database project.\n" +
                    "2. Navigate to SQL Editor and execute this exact script:\n\n" +
                    "create table discovered_substances (\n" +
                    "   id text primary key,\n" +
                    "   player_id text not null,\n" +
                    "   formula text not null,\n" +
                    "   name text not null,\n" +
                    "   discovered_at bigint not null\n" +
                    ");\n\n" +
                    "3. Open public access in Supabase policies if no auth is configured, paste your Project credentials above, and click Save & Sync!",
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.4f),
            fontFamily = FontFamily.Monospace,
            lineHeight = 12.sp
        )
    }
}
