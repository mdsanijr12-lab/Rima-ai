package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBlockBackgroundDark
import com.example.ui.theme.RimaCyan
import com.example.ui.theme.RimaIndigo
import com.example.ui.theme.RimaIndigoLight
import com.example.ui.theme.RimaViolet
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BlockQuote(val text: String) : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
}

@Composable
fun MarkdownContent(
    markdownText: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val blocks = remember(markdownText) { parseMarkdown(markdownText) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    HeadingView(block = block)
                }
                is MarkdownBlock.Paragraph -> {
                    ParagraphView(text = block.text, textColor = textColor)
                }
                is MarkdownBlock.BulletItem -> {
                    BulletItemView(text = block.text, textColor = textColor)
                }
                is MarkdownBlock.NumberedItem -> {
                    NumberedItemView(number = block.number, text = block.text, textColor = textColor)
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockView(block = block)
                }
                is MarkdownBlock.BlockQuote -> {
                    BlockQuoteView(text = block.text, textColor = textColor)
                }
                is MarkdownBlock.Table -> {
                    TableView(table = block)
                }
            }
        }
    }
}

@Composable
private fun HeadingView(block: MarkdownBlock.Heading) {
    val fontSize = when (block.level) {
        1 -> 20.sp
        2 -> 18.sp
        else -> 16.sp
    }
    val fontWeight = FontWeight.Bold

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(fontSize.value.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (block.level == 1) RimaCyan else RimaIndigo)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatInlineMarkdown(block.text, MaterialTheme.colorScheme.onSurface),
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = (fontSize.value * 1.3).sp
        )
    }
}

@Composable
private fun ParagraphView(text: String, textColor: Color) {
    Text(
        text = formatInlineMarkdown(text, textColor),
        fontSize = 15.sp,
        color = textColor,
        lineHeight = 22.sp
    )
}

@Composable
private fun BulletItemView(text: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(RimaIndigoLight)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = formatInlineMarkdown(text, textColor),
            fontSize = 15.sp,
            color = textColor,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NumberedItemView(number: String, text: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = RimaCyan,
            modifier = Modifier.width(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = formatInlineMarkdown(text, textColor),
            fontSize = 15.sp,
            color = textColor,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BlockQuoteView(text: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.FormatQuote,
            contentDescription = null,
            tint = RimaViolet,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatInlineMarkdown(text, textColor),
            fontSize = 14.5.sp,
            fontStyle = FontStyle.Italic,
            color = textColor.copy(alpha = 0.9f),
            lineHeight = 21.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CodeBlockView(block: MarkdownBlock.CodeBlock) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CodeBlockBackgroundDark)
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(10.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161B22))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = block.language.ifBlank { "code" }.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RimaCyan,
                fontFamily = FontFamily.Monospace
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("code", block.code)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isCopied) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Copied",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Code content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            Text(
                text = block.code,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE6EDF3),
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun TableView(table: MarkdownBlock.Table) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .horizontalScroll(rememberScrollState())
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                table.headers.forEach { header ->
                    Text(
                        text = header.trim(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .width(140.dp)
                            .padding(horizontal = 6.dp)
                    )
                }
            }

            // Data Rows
            table.rows.forEachIndexed { index, row ->
                val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                Row(
                    modifier = Modifier
                        .background(bg)
                        .padding(8.dp)
                ) {
                    row.forEach { cell ->
                        Text(
                            text = cell.trim(),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .width(140.dp)
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatInlineMarkdown(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length

        while (i < len) {
            // Bold **text**
            if (i + 1 < len && text[i] == '*' && text[i + 1] == '*') {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }

            // Italic *text*
            if (text[i] == '*' && (i == 0 || text[i - 1] != '*') && (i + 1 >= len || text[i + 1] != '*')) {
                val end = text.indexOf('*', i + 1)
                if (end != -1 && (end + 1 >= len || text[end + 1] != '*')) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }

            // Inline Code `code`
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            background = Color(0xFF2D333B).copy(alpha = 0.8f),
                            color = RimaCyan
                        )
                    ) {
                        append(" ${text.substring(i + 1, end)} ")
                    }
                    i = end + 1
                    continue
                }
            }

            append(text[i])
            i++
        }
    }
}

fun parseMarkdown(raw: String): List<MarkdownBlock> {
    val lines = raw.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Code Block ```lang
        if (line.trimStart().startsWith("```")) {
            val lang = line.trimStart().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(lang, codeLines.joinToString("\n")))
            i++
            continue
        }

        // Table detection (| col1 | col2 |)
        if (line.trim().startsWith("|") && line.trim().endsWith("|") && i + 1 < lines.size && lines[i + 1].contains("---")) {
            val headers = line.split("|").filter { it.isNotBlank() }
            i += 2 // skip header and delimiter
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].trim().startsWith("|")) {
                val rowCells = lines[i].split("|").filter { it.isNotBlank() }
                rows.add(rowCells)
                i++
            }
            blocks.add(MarkdownBlock.Table(headers, rows))
            continue
        }

        // Headings (#, ##, ###)
        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val headingText = line.drop(level).trim()
            blocks.add(MarkdownBlock.Heading(level.coerceIn(1, 3), headingText))
            i++
            continue
        }

        // Blockquotes (> quote)
        if (line.trimStart().startsWith(">")) {
            val quoteText = line.trimStart().removePrefix(">").trim()
            blocks.add(MarkdownBlock.BlockQuote(quoteText))
            i++
            continue
        }

        // Bullet Items (*, -, •)
        val trimmed = line.trimStart()
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("• ")) {
            val bulletText = trimmed.substring(2).trim()
            blocks.add(MarkdownBlock.BulletItem(bulletText))
            i++
            continue
        }

        // Numbered Items (1., 2.)
        val numberedRegex = Regex("^(\\d+\\.)\\s+(.*)")
        val match = numberedRegex.find(trimmed)
        if (match != null) {
            val num = match.groupValues[1]
            val text = match.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(num, text))
            i++
            continue
        }

        // Empty lines
        if (line.isBlank()) {
            i++
            continue
        }

        // Standard Paragraph
        blocks.add(MarkdownBlock.Paragraph(line.trim()))
        i++
    }

    return blocks
}
