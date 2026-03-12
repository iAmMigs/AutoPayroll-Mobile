package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.data.model.Payslip
import com.google.gson.annotations.SerializedName
import java.util.Locale

// --- DESIGN TOKENS ---
private val WebYellow = Color(0xFFFFD147)
private val WebBackground = Color(0xFFF8F9FA)
private val WebSurface = Color.White
private val WebBorder = Color(0xFFE2E8F0)
private val TableHeaderBg = Color(0xFFE2E8F0)
private val TextPrimary = Color(0xFF1E293B)
private val TextSecondary = Color(0xFF64748B)
private val TextRed = Color(0xFFDC2626)

// --- DATA CLASS FOR JSON PARSING ---
data class BreakdownData(
    @SerializedName("employee_name") val employeeName: String = "N/A",
    @SerializedName("position") val position: String = "N/A",
    @SerializedName("days_worked") val daysWorked: Int = 0,
    @SerializedName("basic_salary") val basicSalary: Double = 0.0,
    @SerializedName("daily_rate") val dailyRate: Double = 0.0,
    @SerializedName("hourly_rate") val hourlyRate: Double = 0.0,
    @SerializedName("overtime_pay") val overtimePay: Double = 0.0,
    @SerializedName("holiday_pay") val holidayPay: Double = 0.0,
    @SerializedName("night_differential") val nightDifferential: Double = 0.0,
    @SerializedName("tardiness_minutes") val tardinessMinutes: Int = 0,
    @SerializedName("tardiness_amount") val tardinessAmount: Double = 0.0,
    @SerializedName("gross_taxable_salary") val grossTaxableSalary: Double = 0.0,
    @SerializedName("sss") val sss: Double = 0.0,
    @SerializedName("philhealth") val philhealth: Double = 0.0,
    @SerializedName("pagibig") val pagibig: Double = 0.0,
    @SerializedName("total_statutory") val totalStatutory: Double = 0.0,
    @SerializedName("net_taxable_income") val netTaxableIncome: Double = 0.0,
    @SerializedName("withholding_tax") val withholdingTax: Double = 0.0,
    @SerializedName("total_deductions") val totalDeductions: Double = 0.0,
    @SerializedName("net_pay") val netPay: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipDetailScreen(
    payslip: Payslip,
    breakdown: BreakdownData,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payslip Details", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WebSurface)
            )
        },
        containerColor = WebBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NET PAY (Semi-Monthly Payslip)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = payslip.dateRange,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = WebYellow, thickness = 2.dp, modifier = Modifier.width(200.dp))
                }
            }

            // 2. Employee Info Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WebSurface),
                    border = BorderStroke(1.dp, WebBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InfoItem(label = "Employee Name:", value = breakdown.employeeName, modifier = Modifier.weight(1f))
                            InfoItem(label = "Employee ID:", value = payslip.referenceId.replace("PAY-", ""), modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InfoItem(label = "Position:", value = breakdown.position, modifier = Modifier.weight(1f))
                            InfoItem(label = "Days Worked:", value = "${breakdown.daysWorked} days", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 3. Taxable Salary Table
            item {
                SectionTable(
                    title = "TAXABLE SALARY",
                    rows = listOf(
                        "Basic Semi-Monthly Salary" to formatBreakdownCurrency(breakdown.basicSalary),
                        "Daily Rate" to formatBreakdownCurrency(breakdown.dailyRate),
                        "Hourly Rate" to formatBreakdownCurrency(breakdown.hourlyRate),
                        "Overtime Pay" to formatBreakdownCurrency(breakdown.overtimePay),
                        "Holiday Pay" to formatBreakdownCurrency(breakdown.holidayPay),
                        "Night Differentials" to (if (breakdown.nightDifferential > 0) formatBreakdownCurrency(breakdown.nightDifferential) else "-"),
                        "Tardiness (${breakdown.tardinessMinutes} mins)" to formatBreakdownCurrency(breakdown.tardinessAmount, isDeduction = true)
                    ),
                    totalLabel = "GROSS TAXABLE SALARY",
                    totalAmount = formatBreakdownCurrency(breakdown.grossTaxableSalary)
                )
            }

            // 4. Statutory Deductions Table
            item {
                SectionTable(
                    title = "STATUTORY DEDUCTIONS",
                    rows = listOf(
                        "SSS Monthly Contribution" to formatBreakdownCurrency(breakdown.sss),
                        "PhilHealth Monthly Contribution" to formatBreakdownCurrency(breakdown.philhealth),
                        "Pag-IBIG Monthly Contribution" to formatBreakdownCurrency(breakdown.pagibig)
                    ),
                    totalLabel = "TOTAL STATUTORY DEDUCTIONS",
                    totalAmount = formatBreakdownCurrency(breakdown.totalStatutory)
                )
            }

            // 5. Withholding Tax Table
            item {
                SectionTable(
                    title = "WITHHOLDING TAX",
                    rows = listOf(
                        "Net Taxable Income" to formatBreakdownCurrency(breakdown.netTaxableIncome),
                        "Tax Bracket/Range" to "-"
                    ),
                    totalLabel = "WITHHOLDING TAX",
                    totalAmount = formatBreakdownCurrency(breakdown.withholdingTax)
                )
            }

            // 6. Summary Table
            item {
                SectionTable(
                    title = "SUMMARY",
                    rows = listOf(
                        "GROSS PAY" to formatBreakdownCurrency(breakdown.grossTaxableSalary),
                        "GROSS DEDUCTIONS" to formatBreakdownCurrency(breakdown.totalDeductions)
                    )
                )
            }

            // 7. Big Yellow Net Pay Block
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(WebYellow)
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NET PAY",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatBreakdownCurrency(breakdown.netPay),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// --- REUSABLE UI COMPONENTS ---

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionTable(
    title: String,
    rows: List<Pair<String, String>>,
    totalLabel: String? = null,
    totalAmount: String? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TableHeaderBg)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "AMOUNT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            rows.forEach { (label, amount) ->
                HorizontalDivider(color = WebBorder, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                    Text(
                        text = amount,
                        fontSize = 12.sp,
                        color = if (amount.startsWith("(")) TextRed else TextPrimary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }

            if (totalLabel != null && totalAmount != null) {
                HorizontalDivider(color = WebBorder, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WebBackground)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = totalLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = totalAmount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

// RENAMED TO AVOID CONFLICTS
private fun formatBreakdownCurrency(amount: Double, isDeduction: Boolean = false): String {
    val formatted = String.format(Locale.US, "₱%,.2f", amount)
    return if (isDeduction && amount > 0) "($formatted)" else formatted
}