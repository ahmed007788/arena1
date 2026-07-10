package com.arena.ai.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arena.ai.data.remote.ArenaApiService
import com.arena.ai.data.remote.AgentLeaderboardEntry
import com.arena.ai.domain.model.*
import com.arena.ai.presentation.navigation.Screen
import com.arena.ai.presentation.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

// Use real models from API
val defaultModels = ArenaApiService.realModels

data class AppState(
    val conversations: List<Conversation> = emptyList(),
    val messages: List<Message> = emptyList(),
    val selectedModelId: String = "gpt-5.5",
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
    var showSidebar by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val isChatScreen = currentDestination?.route?.startsWith("chat") == true
    
    // Load leaderboard data on start
    LaunchedEffect(Unit) {
        val result = ArenaApiService.getAgentLeaderboard()
        result.onSuccess { data ->
            appState = appState.copy(leaderboardData = data)
        }
    }
    
    Scaffold(
        containerColor = ArenaBackground,
        bottomBar = {
            if (!isChatScreen) {
                NavigationBar(containerColor = ArenaSidebarBackground) {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                if (screen.route != "chat") {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
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
        }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedVisibility(
                visible = showSidebar || !isChatScreen,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                Sidebar(
                    conversations = appState.conversations,
                    isAuthenticated = appState.isAuthenticated,
                    currentUser = appState.currentUser,
                    onConversationClick = { convId ->
                        appState = appState.copy(currentScreen = "chat")
                        navController.navigate(Screen.Chat.createRoute(convId))
                        showSidebar = false
                    },
                    onNewChat = {
                        val id = UUID.randomUUID().toString()
                        val conv = Conversation(id, "New Chat", Modality.TEXT)
                        appState = appState.copy(
                            conversations = listOf(conv) + appState.conversations,
                            currentScreen = "chat"
                        )
                        navController.navigate(Screen.Chat.createRoute(id))
                        showSidebar = false
                    },
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
                        appState = appState.copy(
                            isAuthenticated = false,
                            currentUser = null
                        )
                    },
                    onClose = { showSidebar = false },
                    modifier = Modifier.width(280.dp).fillMaxHeight()
                )
            }
            
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.weight(1f)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        models = defaultModels,
                        selectedModelId = appState.selectedModelId,
                        onSelectModel = { appState = appState.copy(selectedModelId = it) },
                        onNewChat = {
                            val id = UUID.randomUUID().toString()
                            val conv = Conversation(id, "New Chat", appState.selectedModality)
                            appState = appState.copy(
                                conversations = listOf(conv) + appState.conversations,
                                currentScreen = "chat"
                            )
                            navController.navigate(Screen.Chat.createRoute(id))
                        },
                        onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                        onNavigateToAgent = { navController.navigate(Screen.Agent.route) }
                    )
                }
                composable(Screen.Search.route) { SearchScreen(onConversationClick = { id -> navController.navigate(Screen.Chat.createRoute(id)) }) }
                composable(Screen.Leaderboard.route) { 
                    LeaderboardScreen(
                        leaderboardData = appState.leaderboardData,
                        onBack = { navController.popBackStack() }
                    ) 
                }
                composable(Screen.Agent.route) { AgentScreen(onBack = { navController.popBackStack() }) }
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
                            appState = appState.copy(
                                isAuthenticated = false,
                                currentUser = null
                            )
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
                                val userMsg = Message(UUID.randomUUID().toString(), convId, MessageRole.USER, appState.inputText)
                                appState = appState.copy(
                                    messages = appState.messages + userMsg,
                                    isStreaming = true
                                )
                                
                                // Call real API
                                scope.launch {
                                    val result = ArenaApiService.sendMessage(appState.selectedModelId, appState.inputText)
                                    result.onSuccess { response ->
                                        val assistantMsg = Message(
                                            UUID.randomUUID().toString(), 
                                            convId, 
                                            MessageRole.ASSISTANT, 
                                            response,
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
}

@Composable
fun Sidebar(
    conversations: List<Conversation>,
    isAuthenticated: Boolean,
    currentUser: String?,
    onConversationClick: (String) -> Unit,
    onNewChat: () -> Unit,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(ArenaSidebarBackground)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Arena AI", style = MaterialTheme.typography.titleLarge, color = ArenaTextPrimary, fontWeight = FontWeight.SemiBold)
            Row {
                IconButton(onClick = onNewChat) { Icon(Icons.Default.Add, "New Chat", ArenaAccentPrimary) }
                IconButton(onClick = onClose) { Icon(Icons.Default.ChevronLeft, "Close", ArenaTextSecondary) }
            }
        }
        HorizontalDivider(color = ArenaBorder, thickness = 0.5.dp)
        
        // User info if logged in
        if (isAuthenticated && currentUser != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountCircle, null, ArenaAccentPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Logged in", style = MaterialTheme.typography.bodySmall, color = ArenaAccentPrimary)
                    Text(currentUser, style = MaterialTheme.typography.bodySmall, color = ArenaTextSecondary, maxLines = 1)
                }
            }
            HorizontalDivider(color = ArenaBorder, thickness = 0.5.dp)
        }
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(conversations) { conv ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onConversationClick(conv.id) }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Chat, null, tint = ArenaAccentPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column { Text(conv.title, style = MaterialTheme.typography.bodyMedium, color = ArenaTextPrimary, maxLines = 1) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    models: List<AIModel>,
    selectedModelId: String,
    onSelectModel: (String) -> Unit,
    onNewChat: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToAgent: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("🟢 Arena AI", style = MaterialTheme.typography.displaySmall, color = ArenaTextPrimary, fontWeight = FontWeight.Bold)
        Text("The Official AI Ranking & LLM Leaderboard", style = MaterialTheme.typography.bodyMedium, color = ArenaTextSecondary)
        Text("Real-time data from arena.ai", style = MaterialTheme.typography.bodySmall, color = ArenaAccentPrimary)
        Spacer(modifier = Modifier.height(32.dp))
        Text("⚡ Quick Start", style = MaterialTheme.typography.titleMedium, color = ArenaTextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(listOf(
                "Create a landing page" to "🚀",
                "Build a dashboard" to "📊",
                "Make a game" to "🎮",
                "Design to Code" to "✨",
                "Build a fullstack app" to "🖥️",
                "Launch a storefront" to "🏪"
            )) { (title, icon) ->
                Card(
                    modifier = Modifier.width(160.dp).clickable { onNewChat() },
                    colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(ArenaBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(icon, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(title, style = MaterialTheme.typography.titleSmall, color = ArenaTextPrimary)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("🤖 Available Models (${models.size} models)", style = MaterialTheme.typography.titleMedium, color = ArenaTextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(models) { model ->
                FilterChip(
                    selected = model.id == selectedModelId,
                    onClick = { onSelectModel(model.id) },
                    label = { Text(model.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ArenaAccentPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = ArenaAccentPrimary,
                        containerColor = ArenaSurfaceElevated,
                        labelColor = ArenaTextSecondary
                    )
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onNavigateToLeaderboard, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Leaderboard, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Leaderboard")
            }
            OutlinedButton(onClick = onNavigateToAgent, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.SmartToy, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Agent Mode")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNewChat,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ArenaAccentPrimary, contentColor = ArenaBackground)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Chat", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

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
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AssistChip(
                onClick = {},
                label = { Text("Text") },
                leadingIcon = { Icon(Icons.Default.Chat, null, modifier = Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(containerColor = ArenaSurfaceElevated, labelColor = ArenaTextSecondary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = false,
                onClick = {},
                label = { Text(models.find { it.id == selectedModelId }?.displayName ?: "Select Model") },
                leadingIcon = { Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(16.dp)) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(containerColor = ArenaSurfaceElevated, labelColor = ArenaTextPrimary)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onSend) { Icon(Icons.Default.Add, "New Chat", ArenaAccentPrimary) }
        }
        HorizontalDivider(color = ArenaBorder, thickness = 0.5.dp)
        
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Chat, null, ArenaAccentPrimary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Start a conversation", style = MaterialTheme.typography.titleMedium, color = ArenaTextPrimary)
                        Text("Using ${models.find { it.id == selectedModelId }?.displayName} • Ask anything…", style = MaterialTheme.typography.bodyMedium, color = ArenaTextSecondary)
                    }
                }
            }
            items(messages) { msg ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (msg.role == MessageRole.USER) Arrangement.End else Arrangement.Start) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 400.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (msg.role == MessageRole.USER) ArenaAccentPrimary.copy(alpha = 0.15f) else ArenaSurfaceElevated)
                            .padding(12.dp)
                    ) {
                        if (msg.role == MessageRole.ASSISTANT) Text(msg.modelName ?: "", style = MaterialTheme.typography.labelSmall, color = ArenaAccentPrimary)
                        Text(msg.content, style = MaterialTheme.typography.bodyLarge, color = ArenaTextPrimary)
                    }
                }
            }
            if (isStreaming) {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ArenaAccentPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating response from arena.ai...", style = MaterialTheme.typography.bodySmall, color = ArenaTextSecondary)
                    }
                }
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = ArenaSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ArenaBorder)
        ) {
            Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.Bottom) {
                IconButton(onClick = {}) { Icon(Icons.Default.AttachFile, "Attach", ArenaTextSecondary) }
                TextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask anything…", color = ArenaTextTertiary) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ArenaSurfaceElevated,
                        unfocusedContainerColor = ArenaSurfaceElevated,
                        focusedTextColor = ArenaTextPrimary,
                        unfocusedTextColor = ArenaTextPrimary,
                        cursorColor = ArenaAccentPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 8,
                    shape = RoundedCornerShape(12.dp)
                )
                IconButton(onClick = onSend, enabled = inputText.isNotBlank()) { Icon(Icons.Default.Send, "Send", if (inputText.isNotBlank()) ArenaAccentPrimary else ArenaTextTertiary) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    leaderboardData: List<AgentLeaderboardEntry>,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", ArenaTextSecondary) }
            Text("Agent Leaderboard", style = MaterialTheme.typography.titleLarge, color = ArenaTextPrimary)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("947,000" to "Sessions", "32" to "Models", "Jul 8, 2026" to "Last Updated").forEach { (value, label) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, style = MaterialTheme.typography.titleLarge, color = ArenaTextPrimary, fontWeight = FontWeight.Bold)
                    Text(label, style = MaterialTheme.typography.bodySmall, color = ArenaTextSecondary)
                }
            }
        }
        
        if (leaderboardData.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ArenaAccentPrimary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(leaderboardData) { index, entry ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("#${entry.rank}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (index == 0) Color(0xFFFFD700) else if (index == 1) Color(0xFFC0C0C0) else if (index == 2) Color(0xFFCD7F32) else ArenaTextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.modelName, style = MaterialTheme.typography.titleMedium, color = ArenaTextPrimary)
                                Text(entry.lab, style = MaterialTheme.typography.bodySmall, color = ArenaTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    StatChip("Success", entry.confirmedSuccess)
                                    StatChip("Praise", entry.praiseVsComplaint)
                                    StatChip("Steer", entry.steerability)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(String.format("%.1f%%", entry.netImprovement), style = MaterialTheme.typography.headlineSmall, color = ArenaAccentPrimary, fontWeight = FontWeight.Bold)
                                Text("${entry.sessions} sessions", style = MaterialTheme.typography.labelSmall, color = ArenaTextTertiary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatChip(label: String, value: Double) {
    Surface(
        color = ArenaBackground,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            String.format("%.1f%%", value),
            style = MaterialTheme.typography.labelSmall,
            color = ArenaTextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun AgentScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", ArenaTextSecondary) }
            Text("Agent Mode", style = MaterialTheme.typography.titleLarge, color = ArenaTextPrimary)
        }
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SmartToy, null, ArenaAccentSecondary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("What is Agent Mode?", style = MaterialTheme.typography.titleMedium, color = ArenaTextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Agent Mode runs autonomous AI agents that can browse the web, research topics, write and execute code. Powered by arena.ai's real-time agent ranking system.", style = MaterialTheme.typography.bodyMedium, color = ArenaTextSecondary)
            }
        }
        Text("🔧 Available Tools", style = MaterialTheme.typography.titleMedium, color = ArenaTextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        listOf("Bash" to "💻", "Web Search" to "🔍", "File Write" to "📝", "Code Interpreter" to "⚡").forEach { (tool, icon) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(ArenaSurfaceElevated, RoundedCornerShape(8.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(tool, style = MaterialTheme.typography.bodyLarge, color = ArenaTextPrimary)
                Spacer(modifier = Modifier.weight(1f))
                Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = ArenaAccentPrimary, checkedTrackColor = ArenaAccentPrimary.copy(alpha = 0.3f)))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ArenaAccentSecondary, contentColor = ArenaBackground)
        ) {
            Icon(Icons.Default.SmartToy, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Agent Session", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onConversationClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { Icon(Icons.Default.ArrowBack, "Back", ArenaTextSecondary) }
            Text("Search", style = MaterialTheme.typography.titleLarge, color = ArenaTextPrimary)
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search conversations…", color = ArenaTextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, null, ArenaTextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ArenaSurfaceElevated,
                unfocusedContainerColor = ArenaSurfaceElevated,
                focusedTextColor = ArenaTextPrimary,
                unfocusedTextColor = ArenaTextPrimary,
                cursorColor = ArenaAccentPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Search across all your chat history on arena.ai", style = MaterialTheme.typography.bodyMedium, color = ArenaTextSecondary, modifier = Modifier.fillMaxWidth().padding(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var showLogin by remember { mutableStateOf(!isAuthenticated) }
    
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", ArenaTextSecondary) }
            Text("Settings", style = MaterialTheme.typography.titleLarge, color = ArenaTextPrimary)
        }
        
        if (isAuthenticated) {
            Text("Account", style = MaterialTheme.typography.titleMedium, color = ArenaTextSecondary, modifier = Modifier.padding(vertical = 8.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, ArenaAccentPrimary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Arena.ai Account", style = MaterialTheme.typography.titleMedium, color = ArenaTextPrimary)
                        Text(currentUser ?: "", style = MaterialTheme.typography.bodySmall, color = ArenaTextSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (!isAuthenticated && showLogin) {
            Text("Login to Arena.ai", style = MaterialTheme.typography.titleMedium, color = ArenaTextSecondary, modifier = Modifier.padding(vertical = 8.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = ArenaBackground,
                            unfocusedContainerColor = ArenaBackground,
                            focusedTextColor = ArenaTextPrimary,
                            unfocusedTextColor = ArenaTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = ArenaBackground,
                            unfocusedContainerColor = ArenaBackground,
                            focusedTextColor = ArenaTextPrimary,
                            unfocusedTextColor = ArenaTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onLogin(email, password) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ArenaAccentPrimary, contentColor = ArenaBackground)
                    ) {
                        Text("Login")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Appearance", style = MaterialTheme.typography.titleMedium, color = ArenaTextSecondary, modifier = Modifier.padding(vertical = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated)) {
            Column {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DarkMode, null, ArenaTextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Theme", style = MaterialTheme.typography.bodyLarge, color = ArenaTextPrimary)
                        Text("Dark mode", style = MaterialTheme.typography.bodySmall, color = ArenaTextSecondary)
                    }
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = ArenaAccentPrimary, checkedTrackColor = ArenaAccentPrimary.copy(alpha = 0.3f)))
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        if (isAuthenticated) {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaAccentError)
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}