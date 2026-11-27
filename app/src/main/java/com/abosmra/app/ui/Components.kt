@file:OptIn(ExperimentalMaterial3Api::class)

package com.abosmra.app.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abosmra.app.digitsOnly
import com.abosmra.app.fmt
import com.abosmra.app.fmtDate
import com.abosmra.app.isSettled
import java.util.Calendar
import kotlin.math.abs

// ------------------------------------------------------------------
// الرأس والبطاقات
// ------------------------------------------------------------------

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(listOf(Navy, Blue)))
            .drawBehind {
                drawCircle(
                    Flame.copy(alpha = 0.20f),
                    radius = size.width * 0.28f,
                    center = Offset(size.width * 0.08f, size.height * 0.02f)
                )
                drawCircle(
                    Color.White.copy(alpha = 0.06f),
                    radius = size.width * 0.34f,
                    center = Offset(size.width * 0.96f, size.height * 0.95f)
                )
            }
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            actions()
        }
        content()
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val base = modifier
        .shadow(2.dp, shape, ambientColor = Color(0x330A2A5E), spotColor = Color(0x330A2A5E))
        .clip(shape)
        .background(Color.White)
    val m = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(m, content = content)
}

@Composable
fun SectionTitle(text: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = InkDark
        )
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                color = Blue
            )
        }
    }
}

@Composable
fun Pill(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1
    )
}

@Composable
fun Avatar(name: String, size: Dp = 46.dp) {
    val palette = listOf(Blue, Flame, Teal, Green, Color(0xFF7C3AED), Color(0xFFDB2777), Amber)
    val c = palette[(name.hashCode() and 0x7fffffff) % palette.size]
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(c.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.trim().take(1),
            color = c,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value * 0.42f).sp
        )
    }
}

@Composable
fun IconBadge(icon: ImageVector, tint: Color, bg: Color, size: Dp = 42.dp) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    modifier: Modifier = Modifier,
    sub: String? = null
) {
    AppCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            IconBadge(icon, tint, bg, 36.dp)
            Spacer(Modifier.height(10.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = InkDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(label, style = MaterialTheme.typography.bodySmall, color = InkMid, maxLines = 2)
            if (sub != null) {
                Text(sub, style = MaterialTheme.typography.labelSmall, color = tint)
            }
        }
    }
}

@Composable
fun QuickAction(
    icon: ImageVector,
    label: String,
    tint: Color,
    bg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = InkDark,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(BlueSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Blue, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = InkDark)
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = InkMid,
            textAlign = TextAlign.Center
        )
    }
}

/** عرض مبلغ الدين بلون مناسب */
@Composable
fun DebtLabel(debt: Double, cur: String, modifier: Modifier = Modifier) {
    when {
        isSettled(debt) -> Pill("مسدّد", GreenSoft, Green, modifier)
        debt > 0 -> Column(modifier, horizontalAlignment = Alignment.End) {
            Text(
                "${debt.fmt()} $cur",
                color = Red,
                style = MaterialTheme.typography.titleSmall
            )
            Text("عليه", color = InkMid, style = MaterialTheme.typography.labelSmall)
        }
        else -> Column(modifier, horizontalAlignment = Alignment.End) {
            Text(
                "${abs(debt).fmt()} $cur",
                color = Green,
                style = MaterialTheme.typography.titleSmall
            )
            Text("رصيد له", color = InkMid, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun RatioBar(label: String, ratio: Double, color: Color, valueText: String) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = InkDark
            )
            Text(valueText, style = MaterialTheme.typography.titleSmall, color = color)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(ratio.toFloat().coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = InkDark) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = InkMid
        )
        Text(value, style = MaterialTheme.typography.titleSmall, color = valueColor)
    }
}

// ------------------------------------------------------------------
// الرسوم
// ------------------------------------------------------------------

@Composable
fun CylinderIcon(color: Color, fill: Float, modifier: Modifier = Modifier.size(56.dp)) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val bodyLeft = w * 0.18f
        val bodyW = w * 0.64f
        val bodyTop = h * 0.24f
        val bodyH = h * 0.72f
        val radius = CornerRadius(bodyW * 0.32f, bodyW * 0.32f)

        drawRoundRect(
            color.copy(alpha = 0.16f),
            Offset(bodyLeft, bodyTop),
            Size(bodyW, bodyH),
            radius
        )
        val f = fill.coerceIn(0f, 1f)
        val clip = Path().apply {
            addRoundRect(RoundRect(bodyLeft, bodyTop, bodyLeft + bodyW, bodyTop + bodyH, radius))
        }
        clipPath(clip) {
            drawRect(
                color,
                Offset(bodyLeft, bodyTop + bodyH * (1f - f)),
                Size(bodyW, bodyH * f)
            )
        }
        drawRoundRect(color, Offset(w * 0.38f, h * 0.12f), Size(w * 0.24f, h * 0.16f), CornerRadius(w * 0.04f))
        drawRoundRect(color, Offset(w * 0.30f, h * 0.04f), Size(w * 0.40f, h * 0.09f), CornerRadius(w * 0.045f))
        drawRoundRect(
            color,
            Offset(bodyLeft, bodyTop),
            Size(bodyW, bodyH),
            radius,
            style = Stroke(width = w * 0.04f)
        )
        drawLine(
            Color.White.copy(alpha = 0.40f),
            Offset(bodyLeft + bodyW * 0.24f, bodyTop + bodyH * 0.16f),
            Offset(bodyLeft + bodyW * 0.24f, bodyTop + bodyH * 0.62f),
            strokeWidth = w * 0.05f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun BarChart(
    values: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 110.dp,
    barWidth: Dp = 22.dp
) {
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    Row(
        modifier
            .fillMaxWidth()
            .height(barHeight + 48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { i, v ->
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    if (v > 0) v.toString() else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = Navy,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                val h = if (v == 0) 4.dp else barHeight * (v.toFloat() / max)
                val bar = Modifier
                    .width(barWidth)
                    .height(h)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                Box(
                    if (v == 0) bar.background(Line)
                    else bar.background(Brush.verticalGradient(listOf(FlameLight, Flame)))
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    labels.getOrElse(i) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMid,
                    maxLines = 1
                )
            }
        }
    }
}

// ------------------------------------------------------------------
// الحقول والنماذج
// ------------------------------------------------------------------

class ThousandsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val n = raw.length
        val sb = StringBuilder()
        for (i in 0 until n) {
            sb.append(raw[i])
            val remaining = n - 1 - i
            if (remaining > 0 && remaining % 3 == 0) sb.append(',')
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                var commas = 0
                val upto = offset.coerceAtMost(n)
                for (i in 0 until upto) {
                    val remaining = n - 1 - i
                    if (remaining > 0 && remaining % 3 == 0) commas++
                }
                return upto + commas
            }

            override fun transformedToOriginal(offset: Int): Int {
                var orig = 0
                var t = 0
                for (i in 0 until n) {
                    if (t >= offset) break
                    t++
                    orig++
                    val remaining = n - 1 - i
                    if (remaining > 0 && remaining % 3 == 0) t++
                }
                return orig.coerceAtMost(n)
            }
        }
        return TransformedText(AnnotatedString(sb.toString()), mapping)
    }
}

private val fieldShape = RoundedCornerShape(14.dp)

@Composable
fun AppField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = fieldShape,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun MoneyField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    cur: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(digitsOnly(it).take(12)) },
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        suffix = { Text(cur) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = ThousandsTransformation(),
        shape = fieldShape,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun RoundBtn(icon: ImageVector, color: Color = Blue, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White)
    }
}

@Composable
fun Stepper(label: String, value: Int, onChange: (Int) -> Unit, min: Int = 0, max: Int = 9999) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PageBg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            color = InkDark
        )
        RoundBtn(Icons.Rounded.Remove, InkMid) { onChange((value - 1).coerceAtLeast(min)) }
        Text(
            value.toString(),
            Modifier.widthIn(min = 56.dp),
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Navy
        )
        RoundBtn(Icons.Rounded.Add, Blue) { onChange((value + 1).coerceAtMost(max)) }
    }
}

@Composable
fun PickerField(label: String, value: String, placeholder: String = "", onClick: () -> Unit) {
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null) },
            singleLine = true,
            shape = fieldShape,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .matchParentSize()
                .clip(fieldShape)
                .clickable(onClick = onClick)
        )
    }
}

@Composable
fun DateField(label: String = "التاريخ", millis: Long, onChange: (Long) -> Unit) {
    val ctx = LocalContext.current
    PickerField(label = label, value = fmtDate(millis)) {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        DatePickerDialog(
            ctx,
            { _, y, m, d ->
                val c = Calendar.getInstance().apply {
                    timeInMillis = millis
                    set(y, m, d)
                }
                onChange(c.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

@Composable
fun FormDialog(
    title: String,
    confirmText: String,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 660.dp)
        ) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Navy, Blue)))
                        .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        Modifier.weight(1f),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, "إغلاق", tint = Color.White)
                    }
                }
                Column(
                    Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        enabled = confirmEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Flame)
                    ) {
                        Text(confirmText, style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String = "حذف",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
