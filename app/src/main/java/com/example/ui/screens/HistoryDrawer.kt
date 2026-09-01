package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ConversationEntity
import com.example.ui.components.RimaLogo
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.RimaCyan
import com.example.ui.theme.RimaIndigo
import com.example.ui.theme.RimaIndigoDark
import com.example.ui.theme.RimaViolet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryDrawerContent(
    conversations: List<ConversationEntity>,
    currentConversationId: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onRenameConversation: (String, String) -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onClearAll: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var conversationToRename by remember { mutableStateOf<ConversationEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header with Logo and Brand
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RimaLogo(size = 36.dp, isAnimated = false)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Rima AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Chat History",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // New Chat Button
            Button(
                onClick = {
                    onNewChat()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RimaIndigo)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Chat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search conversations...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = RimaCyan,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Conversations List
            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No conversation history yet.\nStart a new chat!" else "No matching chats found.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(conversations, key = { it.id }) { conv ->
                        ConversationItemRow(
                            conversation = conv,
                            isSelected = conv.id == currentConversationId,
                            onClick = {
                                onSelectConversation(conv.id)
                                onCloseDrawer()
                            },
                            onRename = {
                                conversationToRename = conv
                                renameText = conv.title
                            },
                            onTogglePin = { onTogglePin(conv.id, conv.isPinned) },
                            onDelete = { onDeleteConversation(conv.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Clear All Button
            if (conversations.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showClearAllConfirm = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = null,
                        tint = ErrorRed.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clear All Chats",
                        fontSize = 13.sp,
                        color = ErrorRed.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Rename Dialog
    if (conversationToRename != null) {
        AlertDialog(
            onDismissRequest = { conversationToRename = null },
            title = { Text("Rename Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    placeholder = { Text("Enter title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        conversationToRename?.let {
                            if (renameText.isNotBlank()) {
                                onRenameConversation(it.id, renameText.trim())
                            }
                        }
                        conversationToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RimaIndigo)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Clear All Chats?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Text(
                    "This will delete all saved conversations and messages. This action cannot be undone.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearAllConfirm = false
                        onCloseDrawer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ConversationItemRow(
    conversation: ConversationEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (conversation.isPinned) Icons.Default.PushPin else Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = if (isSelected) RimaCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = conversation.title,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTimestamp(conversation.updatedAt),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (conversation.isPinned) "Unpin" else "Pin to top") },
                        leadingIcon = {
                            Icon(
                                if (conversation.isPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onTogglePin()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = ErrorRed) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { time = date }

    return if (now.get(Calendar.DATE) == msgCal.get(Calendar.DATE)) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}
