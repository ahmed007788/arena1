package com.arena.ai.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arena.ai.data.remote.ArenaApiService
import com.arena.ai.data.remote.AgentLeaderboardEntry
import com.arena.ai.domain.model.*
import com.arena.ai.presentation.navigation.Screen
import com.arena.ai.presentation.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// ARENA.AI - Official Android App
// Real-time AI Leaderboard & Chat Interface
// Data from arena.ai (July 2026)
// ═══════════════════════════════════════════════════════════════════════════════

val defaultModels = ArenaApiService.realModels

data class AppState(
    val conversations: List<Conversation> = emptyList(),
    val messages: List<Message> = emptyList(),
    val selectedModelId: String = "claude-fable-5",
    val selectedModality: Modality = Modality.TEXT,
    val isStreaming: Boolean = false,
    val currentScreen: String = "home",
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: String? = null,
    val leaderboardData: List<AgentLeaderboardEntry> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaApp() {
    var appState by remember { mutableStateOf(AppState()) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val scope = rememberCoroutineScope()
    
    val isChatScreen = currentDestination?.route?.startsWith("chat") == true
    
    // Load leaderboard data
    LaunchedEffect(Unit) {
        val result = ArenaApiService.getAgentLeaderboard()
        result.onSuccess { data ->
            appState = appState.copy(leaderboardData = data)
        }
    }
    
    val navItems = listOf(
        Triple("المحادثة", "chat", Icons.Default.Chat),
        Triple("البحث", "search", Icons.Default.Search),
        Triple("التصنيف", "leaderboard", Icons.Default.Leaderboard),
        Triple("الوكيل", "agent", Icons.Default.SmartToy),
        Triple("الإعدادات", "settings", Icons.Default.Settings)
    )
    
    Scaffold(
        containerColor = ArenaBackground,
        topBar = {
            if (isChatScreen) {
                ArenaTopBar(
                    title = "محادثة جديدة",
                    onBack = { navController.popBackStack() },
                    onMenu = { }
                )
            }
        },
        bottomBar = {
            if (!isChatScreen) {
                NavigationBar(
                    containerColor = ArenaSidebarBackground,
                    contentColor = ArenaTextPrimary
                ) {
                    navItems.forEachIndexed { index, (label, route, icon) ->
                        val screenRoute = when(index) {
                            0 -> Screen.Home.route
                            1 -> Screen.Search.route
                            2 -> Screen.Leaderboard.route
                            3 -> Screen.Agent.route
                            4 -> Screen.Settings.route
                            else -> Screen.Home.route
                        }
                        val selected = currentDestination?.hierarchy?.any { it.route == screenRoute } == true
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screenRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ArenaAccentPrimary,
                                selectedTextColor = ArenaAccentPrimary,
                                unselectedIconColor = ArenaTextSecondary,
                                unselectedTextColor = ArenaTextSecondary,
                                indicatorColor = ArenaAccentPrimary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isChatScreen) {
                FloatingActionButton(
                    onClick = {
                        val id = UUID.randomUUID().toString()
                        val conv = Conversation(id, "محادثة جديدة", Modality.TEXT)
                        appState = appState.copy(
                            conversations = listOf(conv) + appState.conversations,
                            currentScreen = "chat"
                        )
                        navController.navigate(Screen.Chat.createRoute(id))
                    },
                    containerColor = ArenaAccentPrimary,
                    contentColor = ArenaBackground
                ) {
                    Icon(Icons.Default.Add, "محادثة جديدة")
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    models = defaultModels,
                    selectedModelId = appState.selectedModelId,
                    onSelectModel = { appState = appState.copy(selectedModelId = it) },
                    onNewChat = {
                        val id = UUID.randomUUID().toString()
                        val conv = Conversation(id, "محادثة جديدة", appState.selectedModality)
                        appState = appState.copy(
                            conversations = listOf(conv) + appState.conversations,
                            currentScreen = "chat"
                        )
                        navController.navigate(Screen.Chat.createRoute(id))
                    },
                    onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                    onNavigateToAgent = { navController.navigate(Screen.Agent.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onConversationClick = { id -> navController.navigate(Screen.Chat.createRoute(id)) }
                )
            }
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen(
                    leaderboardData = appState.leaderboardData,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Agent.route) {
                AgentScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    isAuthenticated = appState.isAuthenticated,
                    currentUser = appState.currentUser,
                    onBack = { navController.popBackStack() },
                    onLogin = { email, password ->
                        scope.launch {
                            appState = appState.copy(isLoading = true)
                            val result = ArenaApiService.login(email, password)
                            result.onSuccess {
                                appState = appState.copy(
                                    isAuthenticated = true,
                                    currentUser = email,
                                    isLoading = false
                                )
                            }.onFailure {
                                appState = appState.copy(isLoading = false)
                            }
                        }
                    },
                    onLogout = {
                        ArenaApiService.logout()
                        appState = appState.copy(isAuthenticated = false, currentUser = null)
                    }
                )
            }
            composable(Screen.Chat.route) { backStackEntry ->
                val convId = backStackEntry.arguments?.getString("id") ?: ""
                ChatScreen(
                    messages = appState.messages.filter { it.conversationId == convId },
                    inputText = appState.inputText,
                    onInputChange = { appState = appState.copy(inputText = it) },
                    onSend = {
                        if (appState.inputText.isNotBlank()) {
                            val userMsg = Message(
                                UUID.randomUUID().toString(), convId,
                                com.arena.ai.domain.model.MessageRole.USER, appState.inputText
                            )
                            appState = appState.copy(
                                messages = appState.messages + userMsg,
                                isStreaming = true
                            )
                            scope.launch {
                                val result = ArenaApiService.sendMessage(
                                    appState.selectedModelId, appState.inputText
                                )
                                result.onSuccess { response ->
                                    val assistantMsg = Message(
                                        UUID.randomUUID().toString(), convId,
                                        com.arena.ai.domain.model.MessageRole.ASSISTANT, response,
                                        modelName = defaultModels.find { it.id == appState.selectedModelId }?.displayName
                                    )
                                    appState = appState.copy(
                                        messages = appState.messages + assistantMsg,
                                        inputText = "",
                                        isStreaming = false
                                    )
                                }.onFailure {
                                    appState = appState.copy(isStreaming = false)
                                }
                            }
                        }
                    },
                    models = defaultModels,
                    selectedModelId = appState.selectedModelId,
                    onSelectModel = { appState = appState.copy(selectedModelId = it) },
                    isStreaming = appState.isStreaming
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TOP BAR COMPONENT
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ArenaTopBar(
    title: String,
    onBack: () -> Unit = {},
    onMenu: () -> Unit = {}
) {
    Surface(
        color = ArenaSidebarBackground,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "رجوع",
                    tint = ArenaTextPrimary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ArenaTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onMenu) {
                Icon(Icons.Default.MoreVert, "المزيد", tint = ArenaTextSecondary)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// HOME SCREEN - Main Dashboard (from arena.ai)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    models: List<AIModel>,
    selectedModelId: String,
    onSelectModel: (String) -> Unit,
    onNewChat: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToAgent: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showModelSelector by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBackground)
    ) {
        // Header with gradient effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ArenaAccentPrimary.copy(alpha = 0.1f),
                            ArenaBackground
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ArenaAccentPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🟢", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Arena AI",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArenaTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "official ranking & llm leaderboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArenaAccentPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Stats row - Real data from arena.ai
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("947K+", "Sessions")
                    StatCard("32", "Models")
                    StatCard("Real-time", "Data")
                }
            }
        }
        
        // Quick Actions Section - Exact match with arena.ai
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "⚡ بدء سريع",
                style = MaterialTheme.typography.titleMedium,
                color = ArenaTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val quickActions = listOf(
                    "إنشاء صفحة هبوط" to "🚀",
                    "بناء لوحة تحكم" to "📊",
                    "صنع لعبة" to "🎮",
                    "تحويل تصميم لكود" to "✨",
                    "بناء تطبيق كامل" to "🖥️",
                    "إطلاق متجر" to "🏪"
                )
                items(quickActions) { (title, icon) ->
                    QuickActionCard(
                        title = title,
                        icon = icon,
                        onClick = onNewChat
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Models Section - Real models from arena.ai
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🤖 النماذج المتاحة",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArenaTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${models.size} نموذج",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(models.take(8)) { model ->
                    val isSelected = model.id == selectedModelId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectModel(model.id) },
                        label = {
                            Text(
                                model.shortName,
                                style = TextStyle(fontSize = 12.sp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArenaAccentPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = ArenaAccentPrimary,
                            containerColor = ArenaSurfaceElevated,
                            labelColor = ArenaTextSecondary
                        ),
                        border = if (isSelected) BorderStroke(1.dp, ArenaAccentPrimary) else null
                    )
                }
                item {
                    AssistChip(
                        onClick = { showModelSelector = true },
                        label = { Text("المزيد...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ArrowForward,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = ArenaSurfaceElevated,
                            labelColor = ArenaTextSecondary
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Navigation Cards
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "📱 الأقسام",
                style = MaterialTheme.typography.titleMedium,
                color = ArenaTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(200.dp)
            ) {
                item {
                    NavCard(
                        title = "التصنيف",
                        subtitle = "مقارنة النماذج",
                        icon = Icons.Default.Leaderboard,
                        color = ArenaAccentPrimary,
                        onClick = onNavigateToLeaderboard
                    )
                }
                item {
                    NavCard(
                        title = "الوكيل",
                        subtitle = "وضع الوكيل",
                        icon = Icons.Default.SmartToy,
                        color = ArenaAccentSecondary,
                        onClick = onNavigateToAgent
                    )
                }
                item {
                    NavCard(
                        title = "البحث",
                        subtitle = "البحث في المحادثات",
                        icon = Icons.Default.Search,
                        color = ArenaAccentWarning,
                        onClick = onNavigateToSearch
                    )
                }
                item {
                    NavCard(
                        title = "الإعدادات",
                        subtitle = "إعدادات الحساب",
                        icon = Icons.Default.Settings,
                        color = ArenaTextSecondary,
                        onClick = onNavigateToSettings
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // New Chat Button
        Button(
            onClick = onNewChat,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ArenaAccentPrimary,
                contentColor = ArenaBackground
            )
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("محادثة جديدة", style = MaterialTheme.typography.titleMedium)
        }
    }
    
    // Model Selector Dialog
    if (showModelSelector) {
        ModelSelectorDialog(
            models = models,
            selectedModelId = selectedModelId,
            onSelectModel = {
                onSelectModel(it)
                showModelSelector = false
            },
            onDismiss = { showModelSelector = false }
        )
    }
}

@Composable
fun StatCard(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = ArenaAccentPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = ArenaTextSecondary
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ArenaBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = ArenaTextPrimary
            )
        }
    }
}

@Composable
fun NavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = ArenaTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaTextSecondary
                )
            }
        }
    }
}

@Composable
fun ModelSelectorDialog(
    models: List<AIModel>,
    selectedModelId: String,
    onSelectModel: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArenaSurfaceElevated,
        title = {
            Text(
                "اختر النموذج",
                color = ArenaTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(models) { model ->
                    val isSelected = model.id == selectedModelId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) ArenaAccentPrimary.copy(alpha = 0.1f)
                                else ArenaSurfaceTertiary
                            )
                            .clickable { onSelectModel(model.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectModel(model.id) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ArenaAccentPrimary,
                                unselectedColor = ArenaTextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                model.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArenaTextPrimary
                            )
                            Text(
                                model.provider.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                color = ArenaTextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = ArenaAccentPrimary)
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// CHAT SCREEN - Real-time Chat Interface
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ChatScreen(
    messages: List<Message>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    models: List<AIModel>,
    selectedModelId: String,
    onSelectModel: (String) -> Unit,
    isStreaming: Boolean
) {
    var showModelSelector by remember { mutableStateOf(false) }
    val selectedModel = models.find { it.id == selectedModelId }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBackground)
    ) {
        // Model selector bar
        Surface(
            color = ArenaSidebarBackground,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("نص", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.TextFields,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = ArenaSurfaceElevated,
                        labelColor = ArenaTextSecondary
                    )
                )
                
                FilterChip(
                    selected = true,
                    onClick = { showModelSelector = true },
                    label = {
                        Text(
                            selectedModel?.shortName ?: "اختر نموذج",
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = ArenaAccentPrimary
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ArenaAccentPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = ArenaAccentPrimary
                    )
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (isStreaming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = ArenaAccentPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "جارٍ الكتابة...",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaAccentPrimary
                    )
                }
            }
        }
        
        HorizontalDivider(color = ArenaBorder, thickness = 0.5.dp)
        
        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            reverseLayout = false
        ) {
            if (messages.isEmpty()) {
                item {
                    EmptyChatState(
                        modelName = selectedModel?.displayName ?: "النموذج",
                        onStartChat = {}
                    )
                }
            }
            
            items(messages) { msg ->
                MessageBubble(msg)
            }
            
            if (isStreaming) {
                item {
                    StreamingIndicator()
                }
            }
        }
        
        // Input area
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ArenaSidebarBackground
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ArenaSurfaceElevated)
                    .padding(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.AttachFile,
                        "إرفاق",
                        tint = ArenaTextSecondary
                    )
                }
                
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    textStyle = TextStyle(
                        color = ArenaTextPrimary,
                        fontSize = 14.sp
                    ),
                    placeholder = {
                        Text(
                            "اكتب رسالتك...",
                            color = ArenaTextTertiary
                        )
                    },
                    cursorBrush = SolidColor(ArenaAccentPrimary),
                    maxLines = 6
                )
                
                IconButton(
                    onClick = onSend,
                    enabled = inputText.isNotBlank() && !isStreaming
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        "إرسال",
                        tint = if (inputText.isNotBlank() && !isStreaming)
                            ArenaAccentPrimary
                        else
                            ArenaTextTertiary
                    )
                }
            }
        }
    }
    
    if (showModelSelector) {
        ModelSelectorDialog(
            models = models,
            selectedModelId = selectedModelId,
            onSelectModel = {
                onSelectModel(it)
                showModelSelector = false
            },
            onDismiss = { showModelSelector = false }
        )
    }
}

@Composable
fun MessageBubble(msg: Message) {
    val isUser = msg.role == com.arena.ai.domain.model.MessageRole.USER
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) ArenaAccentPrimary.copy(alpha = 0.15f)
                    else ArenaSurfaceElevated
                )
                .padding(12.dp)
        ) {
            Column {
                if (!isUser && msg.modelName != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ArenaAccentPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            msg.modelName,
                            style = MaterialTheme.typography.labelSmall,
                            color = ArenaAccentPrimary
                        )
                    }
                }
                Text(
                    msg.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArenaTextPrimary
                )
            }
        }
    }
}

@Composable
fun EmptyChatState(modelName: String, onStartChat: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Chat,
            null,
            modifier = Modifier.size(64.dp),
            tint = ArenaAccentPrimary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "ابدأ محادثة",
            style = MaterialTheme.typography.titleMedium,
            color = ArenaTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "باستخدام $modelName",
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaTextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "اسأل أي شيء...",
            style = MaterialTheme.typography.bodySmall,
            color = ArenaTextTertiary
        )
    }
}

@Composable
fun StreamingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = ArenaAccentPrimary,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "جارٍ الحصول على رد من arena.ai...",
            style = MaterialTheme.typography.bodySmall,
            color = ArenaTextSecondary
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LEADERBOARD SCREEN - Agent Rankings (from arena.ai)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LeaderboardScreen(
    leaderboardData: List<AgentLeaderboardEntry>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBackground)
    ) {
        ArenaTopBar(
            title = "التصنيف",
            onBack = onBack
        )
        
        // Stats header
        Surface(
            color = ArenaSurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("947K+", "Sessions")
                StatCard("32", "Models")
                StatCard("Jul 10, 2026", "Last Update")
            }
        }
        
        HorizontalDivider(color = ArenaBorder, thickness = 0.5.dp)
        
        // Leaderboard list
        if (leaderboardData.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ArenaAccentPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(leaderboardData) { index, entry ->
                    LeaderboardCard(entry, index)
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(entry: AgentLeaderboardEntry, index: Int) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700)  // Gold
        2 -> Color(0xFFC0C0C0)  // Silver
        3 -> Color(0xFFCD7F32)  // Bronze
        else -> ArenaTextSecondary
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#${entry.rank}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = rankColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.modelName,
                    style = MaterialTheme.typography.titleSmall,
                    color = ArenaTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    entry.lab,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaTextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MiniStatChip("Success", entry.confirmedSuccess)
                    MiniStatChip("Praise", entry.praiseVsComplaint)
                    MiniStatChip("Steer", entry.steerability)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    String.format("%.1f%%", entry.netImprovement),
                    style = MaterialTheme.typography.headlineSmall,
                    color = ArenaAccentPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${entry.sessions} sessions",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaTextTertiary
                )
            }
        }
    }
}

@Composable
fun MiniStatChip(label: String, value: Double) {
    Surface(
        color = ArenaBackground,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            String.format("%.0f%%", value),
            style = MaterialTheme.typography.labelSmall,
            color = ArenaTextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// AGENT SCREEN - Agent Mode Interface (from arena.ai)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AgentScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBackground)
    ) {
        ArenaTopBar(title = "وضع الوكيل", onBack = onBack)
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ArenaAccentSecondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    null,
                                    tint = ArenaAccentSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "ما هو وضع الوكيل؟",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArenaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "وضع الوكيل يشغل وكلاء AI autonomous الذين يمكنهم تصفح الويب، البحث في المواضيع، كتابة وتنفيذ الكود. مدعوم من نظام ترتيب الوكلاء في arena.ai.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArenaTextSecondary
                        )
                    }
                }
            }
            
            item {
                Text(
                    "🔧 الأدوات المتاحة",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArenaTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            val tools = listOf(
                Triple("Bash", "💻", "تنفيذ أوامر الطرفية"),
                Triple("Web Search", "🔍", "البحث في الإنترنت"),
                Triple("File Write", "📝", "كتابة الملفات"),
                Triple("Code Interpreter", "⚡", "تشغيل الكود")
            )
            
            items(tools.size) { index ->
                val (tool, icon, description) = tools[index]
                ToolCard(tool, icon, description)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArenaAccentSecondary,
                        contentColor = ArenaBackground
                    )
                ) {
                    Icon(Icons.Default.SmartToy, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بدء جلسة الوكيل", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun ToolCard(tool: String, icon: String, description: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tool,
                    style = MaterialTheme.typography.titleSmall,
                    color = ArenaTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaTextSecondary
                )
            }
            Switch(
                checked = true,
                onCheckedChange = {},
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ArenaAccentPrimary,
                    checkedTrackColor = ArenaAccentPrimary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SEARCH SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onConversationClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBackground)
    ) {
        ArenaTopBar(title = "البحث", onBack = {})
        
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = ArenaSurfaceElevated,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        null,
                        tint = ArenaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            color = ArenaTextPrimary,
                            fontSize = 14.sp
                        ),
                        placeholder = {
                            Text(
                                "ابحث في المحادثات...",
                                color = ArenaTextTertiary
                            )
                        },
                        cursorBrush = SolidColor(ArenaAccentPrimary)
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                "مسح",
                                tint = ArenaTextSecondary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Search,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = ArenaTextTertiary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "ابحث في جميع محادثاتك",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArenaTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "اكتب للبحث في سجل المحادثات على arena.ai",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArenaTextSecondary
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SETTINGS SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SettingsScreen(
    isAuthenticated: Boolean,
    currentUser: String?,
    onBack: () -> Unit,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBackground)
    ) {
        ArenaTopBar(title = "الإعدادات", onBack = onBack)
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "👤 الحساب",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArenaTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            if (isAuthenticated && currentUser != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ArenaAccentPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    null,
                                    tint = ArenaAccentPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "حساب Arena.ai",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ArenaTextPrimary
                                )
                                Text(
                                    currentUser,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArenaTextSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "تسجيل الدخول إلى Arena.ai",
                                style = MaterialTheme.typography.titleSmall,
                                color = ArenaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("البريد الإلكتروني") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ArenaAccentPrimary,
                                    unfocusedBorderColor = ArenaBorder,
                                    focusedLabelColor = ArenaAccentPrimary,
                                    unfocusedLabelColor = ArenaTextSecondary,
                                    focusedTextColor = ArenaTextPrimary,
                                    unfocusedTextColor = ArenaTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("كلمة المرور") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ArenaAccentPrimary,
                                    unfocusedBorderColor = ArenaBorder,
                                    focusedLabelColor = ArenaAccentPrimary,
                                    unfocusedLabelColor = ArenaTextSecondary,
                                    focusedTextColor = ArenaTextPrimary,
                                    unfocusedTextColor = ArenaTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation()
                            )
                            
                            if (loginError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    loginError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArenaAccentError
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Button(
                                onClick = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        onLogin(email, password)
                                    } else {
                                        loginError = "يرجى إدخال البريد وكلمة المرور"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ArenaAccentPrimary,
                                    contentColor = ArenaBackground
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("تسجيل الدخول")
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "🎨 المظهر",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArenaTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.DarkMode,
                            title = "الوضع المظلم",
                            subtitle = "تفعيل المظهر الداكن"
                        ) {
                            Switch(
                                checked = true,
                                onCheckedChange = {},
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ArenaAccentPrimary,
                                    checkedTrackColor = ArenaAccentPrimary.copy(alpha = 0.3f)
                                )
                            )
                        }
                        HorizontalDivider(color = ArenaBorder, thickness = 0.5.dp)
                        SettingsRow(
                            icon = Icons.Default.Language,
                            title = "اللغة",
                            subtitle = "العربية"
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                null,
                                tint = ArenaTextSecondary
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "ℹ️ حول",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArenaTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "الإصدار",
                            subtitle = "1.0.0"
                        ) {}
                        HorizontalDivider(color = ArenaBorder, thickness = 0.5.dp)
                        SettingsRow(
                            icon = Icons.Default.Code,
                            title = "مصدر البيانات",
                            subtitle = "arena.ai"
                        ) {}
                    }
                }
            }
            
            if (isAuthenticated) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ArenaAccentError
                        ),
                        border = BorderStroke(1.dp, ArenaAccentError)
                    ) {
                        Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل الخروج")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = ArenaTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = ArenaTextPrimary
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaTextSecondary
            )
        }
        trailing()
    }
}