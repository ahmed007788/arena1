package com.arena.ai.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arena.ai.data.remote.AgentLeaderboardEntry
import com.arena.ai.data.remote.realAgentLeaderboard
import com.arena.ai.data.remote.realModels
import com.arena.ai.domain.model.MessageRole
import com.arena.ai.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val modelName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ConversationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val messages: List<ChatMessage> = emptyList(),
    val modelId: String = "claude-fable-5",
    val createdAt: Long = System.currentTimeMillis()
)

data class AppState(
    val isLoggedIn: Boolean = false,
    val email: String = "",
    val password: String = "",
    val conversations: List<ConversationItem> = emptyList(),
    val currentConversation: ConversationItem? = null,
    val selectedModelId: String = "claude-fable-5",
    val isStreaming: Boolean = false,
    val searchQuery: String = ""
)

@Composable
fun ArenaApp() {
    val navController = rememberNavController()
    var appState by remember { mutableStateOf(AppState()) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isChatScreen = currentRoute == "chat"
    val scope = rememberCoroutineScope()
    
    Scaffold(
        containerColor = ArenaBackground,
        topBar = {
            if (isChatScreen) {
                TopBar(
                    title = appState.currentConversation?.title ?: "محادثة",
                    onBack = { navController.popBackStack() }
                )
            }
        },
        bottomBar = {
            if (!isChatScreen) {
                BottomNavBar(
                    currentRoute = currentRoute ?: "home",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isChatScreen) {
                FloatingActionButton(
                    onClick = {
                        val newConv = ConversationItem(title = "محادثة جديدة")
                        appState = appState.copy(
                            conversations = listOf(newConv) + appState.conversations,
                            currentConversation = newConv
                        )
                        navController.navigate("chat")
                    },
                    containerColor = ArenaAccentPrimary
                ) {
                    Icon(Icons.Default.Add, "محادثة جديدة", tint = ArenaBackground)
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    appState = appState,
                    onSelectModel = { appState = appState.copy(selectedModelId = it) },
                    onStartChat = { title ->
                        val conv = ConversationItem(title = title, modelId = appState.selectedModelId)
                        appState = appState.copy(
                            conversations = listOf(conv) + appState.conversations,
                            currentConversation = conv
                        )
                        navController.navigate("chat")
                    },
                    onSelectConversation = { conv ->
                        appState = appState.copy(currentConversation = conv)
                        navController.navigate("chat")
                    },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable("chat") {
                ChatScreen(
                    appState = appState,
                    onSendMessage = { content ->
                        if (content.isBlank()) return@ChatScreen
                        
                        val userMsg = ChatMessage(role = MessageRole.USER, content = content)
                        val currentConv = appState.currentConversation ?: return@ChatScreen
                        val updatedConv = currentConv.copy(
                            messages = currentConv.messages + userMsg
                        )
                        
                        appState = appState.copy(
                            currentConversation = updatedConv,
                            isStreaming = true
                        )
                        
                        scope.launch {
                            delay(1000)
                            val aiResponse = generateResponse(content, appState.selectedModelId)
                            val aiMsg = ChatMessage(
                                role = MessageRole.ASSISTANT,
                                content = aiResponse,
                                modelName = realModels.find { it.id == appState.selectedModelId }?.displayName
                            )
                            appState = appState.copy(
                                currentConversation = appState.currentConversation?.copy(
                                    messages = appState.currentConversation?.messages?.plus(aiMsg) ?: listOf(aiMsg)
                                ),
                                isStreaming = false
                            )
                        }
                    },
                    onSelectModel = { appState = appState.copy(selectedModelId = it) }
                )
            }
            composable("search") {
                SearchScreen(
                    appState = appState,
                    onSearch = { appState = appState.copy(searchQuery = it) },
                    onSelectConversation = { conv ->
                        appState = appState.copy(currentConversation = conv)
                        navController.navigate("chat")
                    }
                )
            }
            composable("leaderboard") {
                LeaderboardScreen()
            }
            composable("agent") {
                AgentScreen()
            }
            composable("settings") {
                SettingsScreen(
                    appState = appState,
                    onEmailChange = { appState = appState.copy(email = it) },
                    onPasswordChange = { appState = appState.copy(password = it) },
                    onLogin = {
                        if (appState.email == "Ai9900@bjedu.tech" && appState.password == "Ai9900@bjedu.tech") {
                            appState = appState.copy(isLoggedIn = true)
                        }
                    },
                    onLogout = { appState = appState.copy(isLoggedIn = false, email = "", password = "") }
                )
            }
        }
    }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Surface(color = ArenaSidebarBackground) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "رجوع", tint = ArenaTextPrimary)
            }
            Text(title, color = ArenaTextPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = ArenaSidebarBackground) {
        listOf(
            Triple("home", "المحادثة", Icons.Default.Chat),
            Triple("search", "البحث", Icons.Default.Search),
            Triple("leaderboard", "التصنيف", Icons.Default.Leaderboard),
            Triple("agent", "الوكيل", Icons.Default.SmartToy),
            Triple("settings", "الإعدادات", Icons.Default.Settings)
        ).forEach { (route, label, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, label) },
                label = { Text(label, fontSize = 10.sp) },
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ArenaAccentPrimary,
                    selectedTextColor = ArenaAccentPrimary,
                    unselectedIconColor = ArenaTextSecondary,
                    indicatorColor = ArenaAccentPrimary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun HomeScreen(
    appState: AppState,
    onSelectModel: (String) -> Unit,
    onStartChat: (String) -> Unit,
    onSelectConversation: (ConversationItem) -> Unit,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(ArenaBackground).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(ArenaAccentPrimary), contentAlignment = Alignment.Center) {
                    Text("🟢", fontSize = 24.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Arena AI", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ArenaTextPrimary)
                    Text("official ranking & llm leaderboard", fontSize = 12.sp, color = ArenaAccentPrimary)
                }
            }
        }
        
        item {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("947K+", "Sessions")
                StatItem("32", "Models")
                StatItem("Real-time", "Data")
            }
        }
        
        item { Text("⚡ بدء سريع", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ArenaTextPrimary) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(listOf(
                    "إنشاء صفحة هبوط" to "🚀", "بناء لوحة تحكم" to "📊", "صنع لعبة" to "🎮",
                    "تحويل تصميم لكود" to "✨", "بناء تطبيق كامل" to "🖥️", "إطلاق متجر" to "🏪"
                )) { (title, icon) ->
                    Card(Modifier.width(140.dp).clickable { onStartChat(title) }, colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(icon, fontSize = 24.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(title, color = ArenaTextPrimary)
                        }
                    }
                }
            }
        }
        
        if (appState.conversations.isNotEmpty()) {
            item { Text("📝 المحادثات الأخيرة", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ArenaTextPrimary) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(appState.conversations.take(5)) { conv ->
                        Card(Modifier.width(160.dp).clickable { onSelectConversation(conv) }, colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated)) {
                            Column(Modifier.padding(16.dp)) {
                                Icon(Icons.Default.Chat, null, tint = ArenaAccentPrimary)
                                Spacer(Modifier.height(8.dp))
                                Text(conv.title, color = ArenaTextPrimary, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
        
        item { Text("📱 الأقسام", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ArenaTextPrimary) }
        item {
            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(180.dp)) {
                item { NavCard("التصنيف", "مقارنة النماذج", Icons.Default.Leaderboard, ArenaAccentPrimary) { onNavigate("leaderboard") } }
                item { NavCard("الوكيل", "وضع الوكيل", Icons.Default.SmartToy, ArenaAccentSecondary) { onNavigate("agent") } }
                item { NavCard("البحث", "البحث في المحادثات", Icons.Default.Search, ArenaAccentWarning) { onNavigate("search") } }
                item { NavCard("الإعدادات", "إعدادات الحساب", Icons.Default.Settings, ArenaTextSecondary) { onNavigate("settings") } }
            }
        }
        
        item {
            Button(
                onClick = { onStartChat("محادثة جديدة") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ArenaAccentPrimary)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("محادثة جديدة")
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ArenaAccentPrimary)
        Text(label, fontSize = 12.sp, color = ArenaTextSecondary)
    }
}

@Composable
fun NavCard(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().height(80.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ArenaTextPrimary)
                Text(subtitle, fontSize = 12.sp, color = ArenaTextSecondary)
            }
        }
    }
}

@Composable
fun ChatScreen(
    appState: AppState,
    onSendMessage: (String) -> Unit,
    onSelectModel: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val messages = appState.currentConversation?.messages ?: emptyList()
    val selectedModel = realModels.find { it.id == appState.selectedModelId }
    
    Column(Modifier.fillMaxSize().background(ArenaBackground)) {
        Surface(color = ArenaSidebarBackground) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text("نص", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.TextFields, null, modifier = Modifier.size(16.dp)) }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text(selectedModel?.shortName ?: "اختر", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = ArenaAccentPrimary, modifier = Modifier.size(16.dp)) }
                )
                Spacer(Modifier.weight(1f))
                if (appState.isStreaming) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = ArenaAccentPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(4.dp))
                    Text("جارٍ الكتابة...", fontSize = 12.sp, color = ArenaAccentPrimary)
                }
            }
        }
        
        Divider(color = ArenaBorder, thickness = 0.5.dp)
        
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Chat, null, Modifier.size(64.dp), tint = ArenaAccentPrimary.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("ابدأ محادثة", fontSize = 18.sp, color = ArenaTextPrimary)
                        Text("باستخدام ${selectedModel?.displayName}", fontSize = 14.sp, color = ArenaTextSecondary)
                    }
                }
            }
            items(messages) { msg -> MessageBubble(msg) }
        }
        
        Surface(color = ArenaSidebarBackground, modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(12.dp).clip(RoundedCornerShape(24.dp)).background(ArenaSurfaceElevated).padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) { Icon(Icons.Default.AttachFile, "إرفاق", tint = ArenaTextSecondary) }
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                    textStyle = TextStyle(color = ArenaTextPrimary, fontSize = 14.sp),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Row {
                            if (inputText.isEmpty()) {
                                Text("اكتب رسالتك...", color = ArenaTextTertiary)
                            }
                            innerTextField()
                        }
                    }
                )
                IconButton(
                    onClick = {
                        onSendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !appState.isStreaming
                ) {
                    Icon(Icons.Default.Send, "إرسال", tint = if (inputText.isNotBlank()) ArenaAccentPrimary else ArenaTextTertiary)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.role == MessageRole.USER
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
        Box(
            Modifier.widthIn(max = 320.dp).clip(RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )).background(if (isUser) ArenaAccentPrimary.copy(alpha = 0.15f) else ArenaSurfaceElevated).padding(12.dp)
        ) {
            Column {
                if (!isUser && msg.modelName != null) {
                    Row(Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(ArenaAccentPrimary))
                        Spacer(Modifier.width(6.dp))
                        Text(msg.modelName, fontSize = 12.sp, color = ArenaAccentPrimary)
                    }
                }
                Text(msg.content, color = ArenaTextPrimary)
            }
        }
    }
}

@Composable
fun SearchScreen(
    appState: AppState,
    onSearch: (String) -> Unit,
    onSelectConversation: (ConversationItem) -> Unit
) {
    Column(Modifier.fillMaxSize().background(ArenaBackground).padding(16.dp)) {
        Surface(color = ArenaSurfaceElevated, shape = RoundedCornerShape(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = ArenaTextSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                BasicTextField(
                    value = appState.searchQuery,
                    onValueChange = onSearch,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = ArenaTextPrimary, fontSize = 14.sp),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Row {
                            if (appState.searchQuery.isEmpty()) {
                                Text("ابحث في المحادثات...", color = ArenaTextTertiary)
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        val filtered = appState.conversations.filter { it.title.contains(appState.searchQuery, ignoreCase = true) }
        
        if (filtered.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Search, null, Modifier.size(64.dp), tint = ArenaTextTertiary.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))
                Text("ابحث في جميع محادثاتك", fontSize = 18.sp, color = ArenaTextPrimary)
                Text("اكتب للبحث", fontSize = 14.sp, color = ArenaTextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { conv ->
                    Card(Modifier.fillMaxWidth().clickable { onSelectConversation(conv) }, colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chat, null, tint = ArenaAccentPrimary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(conv.title, color = ArenaTextPrimary, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, null, tint = ArenaTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardScreen() {
    val leaderboardData = remember { realAgentLeaderboard }
    
    Column(Modifier.fillMaxSize().background(ArenaBackground)) {
        TopBar(title = "التصنيف", onBack = {})
        Surface(color = ArenaSurfaceElevated, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("947K+", "Sessions")
                StatItem("32", "Models")
                StatItem("Jul 2026", "Update")
            }
        }
        Divider(color = ArenaBorder, thickness = 0.5.dp)
        
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(leaderboardData) { entry ->
                LeaderboardCard(entry)
            }
        }
    }
}

@Composable
fun LeaderboardCard(entry: AgentLeaderboardEntry) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> ArenaTextSecondary
    }
    
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("#${entry.rank}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = rankColor, modifier = Modifier.width(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.modelName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ArenaTextPrimary)
                Text(entry.lab, fontSize = 12.sp, color = ArenaTextSecondary)
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniStat(entry.confirmedSuccess)
                    MiniStat(entry.praiseVsComplaint)
                    MiniStat(entry.steerability)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(String.format("%.1f%%", entry.netImprovement), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ArenaAccentPrimary)
                Text("${entry.sessions} sessions", fontSize = 10.sp, color = ArenaTextTertiary)
            }
        }
    }
}

@Composable
fun MiniStat(value: Double) {
    Surface(color = ArenaBackground, shape = RoundedCornerShape(4.dp)) {
        Text(String.format("%.0f%%", value), fontSize = 10.sp, color = ArenaTextSecondary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
    }
}

@Composable
fun AgentScreen() {
    Column(Modifier.fillMaxSize().background(ArenaBackground)) {
        TopBar(title = "وضع الوكيل", onBack = {})
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(ArenaAccentSecondary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SmartToy, null, tint = ArenaAccentSecondary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("ما هو وضع الوكيل؟", fontSize = 18.sp, color = ArenaTextPrimary)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("وضع الوكيل يشغل وكلاء AI autonomous الذين يمكنهم تصفح الويب، البحث، كتابة وتنفيذ الكود.", fontSize = 14.sp, color = ArenaTextSecondary)
                    }
                }
            }
            
            item { Text("🔧 الأدوات المتاحة", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ArenaTextPrimary) }
            
            items(listOf(
                Triple("Bash", "💻", "تنفيذ أوامر الطرفية"),
                Triple("Web Search", "🔍", "البحث في الإنترنت"),
                Triple("File Write", "📝", "كتابة الملفات"),
                Triple("Code Interpreter", "⚡", "تشغيل الكود")
            )) { (tool, icon, desc) ->
                Card(colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(icon, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(tool, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ArenaTextPrimary)
                            Text(desc, fontSize = 12.sp, color = ArenaTextSecondary)
                        }
                        Switch(checked = true, onCheckedChange = {})
                    }
                }
            }
            
            item {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaAccentSecondary)
                ) {
                    Icon(Icons.Default.SmartToy, null)
                    Spacer(Modifier.width(8.dp))
                    Text("بدء جلسة الوكيل")
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    appState: AppState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(ArenaBackground)) {
        TopBar(title = "الإعدادات", onBack = {})
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("👤 الحساب", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ArenaTextSecondary) }
            
            if (appState.isLoggedIn) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated), shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).clip(CircleShape).background(ArenaAccentPrimary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccountCircle, null, tint = ArenaAccentPrimary, modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("حساب Arena.ai", fontSize = 14.sp, color = ArenaTextPrimary)
                                Text(appState.email, fontSize = 12.sp, color = ArenaTextSecondary)
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaAccentError)) {
                        Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("تسجيل الخروج")
                    }
                }
            } else {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("تسجيل الدخول إلى Arena.ai", fontSize = 14.sp, color = ArenaTextPrimary)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = appState.email,
                                onValueChange = onEmailChange,
                                label = { Text("البريد الإلكتروني") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = ArenaTextSecondary) }
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = appState.password,
                                onValueChange = onPasswordChange,
                                label = { Text("كلمة المرور") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = ArenaTextSecondary) }
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onLogin,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ArenaAccentPrimary),
                                shape = RoundedCornerShape(12.dp),
                                enabled = appState.email.isNotBlank() && appState.password.isNotBlank()
                            ) {
                                Text("تسجيل الدخول")
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("بيانات تجريبية: Ai9900@bjedu.tech", fontSize = 12.sp, color = ArenaTextTertiary)
                        }
                    }
                }
            }
            
            item { Text("🎨 المظهر", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ArenaTextSecondary) }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated), shape = RoundedCornerShape(16.dp)) {
                    Column {
                        SettingsRow(Icons.Default.DarkMode, "الوضع المظلم", "تفعيل المظهر الداكن") { Switch(checked = true, onCheckedChange = {}) }
                        Divider(color = ArenaBorder, thickness = 0.5.dp)
                        SettingsRow(Icons.Default.Language, "اللغة", "العربية") { Icon(Icons.Default.ChevronRight, null, tint = ArenaTextSecondary) }
                    }
                }
            }
            
            item { Text("ℹ️ حول", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ArenaTextSecondary) }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated), shape = RoundedCornerShape(16.dp)) {
                    Column {
                        SettingsRow(Icons.Default.Info, "الإصدار", "1.0.0") {}
                        Divider(color = ArenaBorder, thickness = 0.5.dp)
                        SettingsRow(Icons.Default.Code, "مصدر البيانات", "arena.ai") {}
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, subtitle: String, trailing: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ArenaTextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = ArenaTextPrimary)
            Text(subtitle, fontSize = 12.sp, color = ArenaTextSecondary)
        }
        trailing()
    }
}

private fun generateResponse(message: String, modelId: String): String {
    val model = realModels.find { it.id == modelId }
    return when {
        message.contains("hello", ignoreCase = true) || message.contains("hi", ignoreCase = true) ->
            "Hello! I'm ${model?.displayName ?: "AI"} from arena.ai. How can I help you?"
        message.contains("who are you", ignoreCase = true) ->
            "I'm an AI assistant powered by ${model?.provider ?: "various AI models"} from arena.ai's leaderboard."
        message.contains("help", ignoreCase = true) ->
            "I can help with: Writing code, Answering questions, Research, Creative writing, Problem solving."
        message.contains("thanks", ignoreCase = true) || message.contains("thank you", ignoreCase = true) ->
            "You're welcome! Is there anything else I can help with?"
        message.contains("bye", ignoreCase = true) ->
            "Goodbye! Have a great day!"
        message.contains("leaderboard", ignoreCase = true) ->
            "Current arena.ai rankings:\n🥇 Claude Fable 5 - Anthropic - 14.10%\n🥈 Claude Opus 4.8 - 9.76%\n🥉 GPT 5.5 - OpenAI - 8.90%"
        else ->
            "I understand you're asking about: \"$message\"\n\nAs an AI from arena.ai, I can help with various tasks. Feel free to ask anything!"
    }
}