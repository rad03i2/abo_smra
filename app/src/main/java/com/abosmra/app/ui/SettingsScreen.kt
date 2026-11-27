@file:OptIn(ExperimentalMaterial3Api::class)

package com.abosmra.app.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.abosmra.app.AppState
import com.abosmra.app.AppViewModel
import com.abosmra.app.data.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(state: AppState, vm: AppViewModel) {
    val s = state.settings
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var salePrice by remember(s) { mutableStateOf(s.salePrice.toLong().toString()) }
    var refillCost by remember(s) { mutableStateOf(s.refillCost.toLong().toString()) }
    var openingFull by remember(s) { mutableIntStateOf(s.openingFull) }
    var openingEmpty by remember(s) { mutableIntStateOf(s.openingEmpty) }
    var currency by remember(s) { mutableStateOf(s.currency) }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = try {
                    val text = vm.exportJson()
                    withContext(Dispatchers.IO) {
                        ctx.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                    }
                    true
                } catch (e: Exception) {
                    false
                }
                Toast.makeText(
                    ctx,
                    if (ok) "تم حفظ النسخة الاحتياطية" else "تعذّر حفظ النسخة الاحتياطية",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> if (uri != null) pendingImport = uri }

    pendingImport?.let { uri ->
        ConfirmDialog(
            title = "استعادة نسخة احتياطية؟",
            text = "سيتم استبدال كل البيانات الحالية بمحتوى الملف. هل تريد المتابعة؟",
            confirmText = "استعادة",
            onConfirm = {
                pendingImport = null
                scope.launch {
                    val ok = try {
                        val text = withContext(Dispatchers.IO) {
                            ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                        }
                        if (text == null) false else vm.importJson(text)
                    } catch (e: Exception) {
                        false
                    }
                    Toast.makeText(
                        ctx,
                        if (ok) "تمت استعادة البيانات بنجاح" else "الملف غير صالح",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onDismiss = { pendingImport = null }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "الإعدادات", subtitle = "الأسعار والمخزون والنسخ الاحتياطي")

        SectionTitle("الأسعار الافتراضية")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MoneyField("سعر بيع الاسطوانة", salePrice, { salePrice = it }, currency)
                MoneyField("سعر التعبئة من المعمل", refillCost, { refillCost = it }, currency)
                AppField("رمز العملة", currency, { currency = it.take(8) })
            }
        }

        SectionTitle("المخزون الافتتاحي")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "عدد الاسطوانات التي كانت معك قبل البدء بالتطبيق.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMid
                )
                Stepper("اسطوانات ممتلئة", openingFull, { openingFull = it })
                Stepper("اسطوانات فارغة", openingEmpty, { openingEmpty = it })
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                vm.saveSettings(
                    Settings(
                        salePrice = salePrice.toDoubleOrNull() ?: s.salePrice,
                        refillCost = refillCost.toDoubleOrNull() ?: s.refillCost,
                        openingFull = openingFull,
                        openingEmpty = openingEmpty,
                        currency = currency.ifBlank { "د.ع" }
                    )
                )
                Toast.makeText(ctx, "تم حفظ الإعدادات", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Flame)
        ) {
            Text("حفظ الإعدادات", style = MaterialTheme.typography.labelLarge)
        }

        SectionTitle("النسخ الاحتياطي")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "بياناتك محفوظة على هذا الهاتف فقط. احفظ نسخة احتياطية بين فترة وأخرى (في Google Drive مثلًا) حتى لا تضيع ديونك إذا تغيّر الهاتف.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMid
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            exportLauncher.launch("abosmra-backup-$stamp.json")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("حفظ نسخة") }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("استعادة نسخة") }
                }
            }
        }

        SectionTitle("حول")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("أبو سمرة", style = MaterialTheme.typography.titleMedium, color = Navy)
                Text(
                    "تطبيق حسابات وديون توزيع الغاز • الإصدار 2.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMid
                )
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}
