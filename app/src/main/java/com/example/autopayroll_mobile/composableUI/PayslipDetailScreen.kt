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

data class BreakdownData(
    @SerializedName("period") val period: PeriodData? = null,
    @SerializedName("employee") val employee: EmployeeData? = null,
    @SerializedName("work_summary") val workSummary: WorkSummaryData? = null,
    @SerializedName("rates") val rates: RatesData? = null,
    @SerializedName("earnings") val earnings: EarningsData? = null,
    @SerializedName("deductions") val deductions: DeductionsData? = null,
    @SerializedName("net_taxable_income") val netTaxableIncome: Double = 0.0,
    @SerializedName("net_pay") val netPay: Double = 0.0
)

data class PeriodData(
    @SerializedName("period_label") val periodLabel: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null
)

data class EmployeeData(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("middle_name") val middleName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("employee_id") val employeeId: String? = null,
    @SerializedName("job_position") val jobPosition: String? = null
)

data class WorkSummaryData(
    @SerializedName("total_days") val totalDays: Double = 0.0,
    @SerializedName("late_minutes") val lateMinutes: Int = 0
)

data class RatesData(
    @SerializedName("daily") val daily: Double = 0.0,
    @SerializedName("monthly") val monthly: Double = 0.0,
    @SerializedName("hourly") val hourly: Double = 0.0
)

data class EarningsData(
    @SerializedName("basic_salary") val basicSalary: Double = 0.0,
    @SerializedName("overtime_pay") val overtimePay: Double = 0.0,
    @SerializedName("holiday_pay") val holidayPay: Double = 0.0,
    @SerializedName("night_differential") val nightDifferential: Double = 0.0,
    @SerializedName("gross_taxable_salary") val grossTaxableSalary: Double = 0.0
)

data class DeductionsData(
    @SerializedName("late_deductions") val lateDeductions: Double = 0.0,
    @SerializedName("sss") val sss: Double = 0.0,
    @SerializedName("philhealth") val philhealth: Double = 0.0,
    @SerializedName("pagibig") val pagibig: Double = 0.0,
    @SerializedName("total_statutory") val totalStatutory: Double = 0.0,
    @SerializedName("withholding_tax") val withholdingTax: Double = 0.0,
    @SerializedName("total_deductions") val totalDeductions: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipDetailScreen(
    payslip: Payslip,
    breakdown: BreakdownData,
    onBack: () -> Unit
) {
    val emp = breakdown.employee
    val empName = listOfNotNull(emp?.firstName, emp?.middleName?.takeIf { it.isNotBlank() }, emp?.lastName).joinToString(" ").ifBlank { "N/A" }
    val daysWorked = breakdown.workSummary?.totalDays?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "0"

    val rates = breakdown.rates
    val earnings = breakdown.earnings
    val deductions = breakdown.deductions
    val summary = breakdown.workSummary

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
                        text = breakdown.period?.periodLabel ?: payslip.dateRange,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val periodDates = if (breakdown.period?.startDate != null && breakdown.period.endDate != null) {
                        "Pay Period: ${breakdown.period.startDate} - ${breakdown.period.endDate}"
                    } else ""
                    if (periodDates.isNotBlank()) {
                        Text(
                            text = periodDates,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = WebYellow, thickness = 2.dp, modifier = Modifier.width(200.dp))
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WebSurface),
                    border = BorderStroke(1.dp, WebBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PayslipDetailInfoItem(label = "Employee Name:", value = empName, modifier = Modifier.weight(1f))
                            // FIXED FALLBACK: Strictly uses the real Employee ID from the main payload
                            PayslipDetailInfoItem(label = "Employee ID:", value = emp?.employeeId ?: payslip.employeeId, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PayslipDetailInfoItem(label = "Position:", value = emp?.jobPosition ?: "N/A", modifier = Modifier.weight(1f))
                            PayslipDetailInfoItem(label = "Days Worked:", value = "$daysWorked days", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                val dailyRate = rates?.daily ?: 0.0
                val rowsList = mutableListOf(
                    "Basic Semi-Monthly Salary ($daysWorked days × ${formatBreakdownCurrency(dailyRate)})" to formatBreakdownCurrency(earnings?.basicSalary ?: 0.0),
                    "Daily Rate (${formatBreakdownCurrency(dailyRate)} × 22 days)" to formatBreakdownCurrency(rates?.monthly ?: 0.0),
                    "Hourly Rate" to formatBreakdownCurrency(rates?.hourly ?: 0.0),
                    "Overtime Pay" to formatBreakdownCurrency(earnings?.overtimePay ?: 0.0),
                    "Holiday Pay" to (if ((earnings?.holidayPay ?: 0.0) > 0) formatBreakdownCurrency(earnings!!.holidayPay) else "-"),
                    "Night Differentials" to (if ((earnings?.nightDifferential ?: 0.0) > 0) formatBreakdownCurrency(earnings!!.nightDifferential) else "-")
                )

                if ((summary?.lateMinutes ?: 0) > 0) {
                    rowsList.add("Tardiness (${summary!!.lateMinutes} mins)" to formatBreakdownCurrency(deductions?.lateDeductions ?: 0.0, isDeduction = true))
                }

                SectionTable(
                    title = "TAXABLE SALARY",
                    rows = rowsList,
                    totalLabel = "GROSS TAXABLE SALARY",
                    totalAmount = formatBreakdownCurrency(earnings?.grossTaxableSalary ?: 0.0)
                )
            }

            item {
                SectionTable(
                    title = "STATUTORY DEDUCTIONS",
                    rows = listOf(
                        "SSS Monthly Contribution" to formatBreakdownCurrency(deductions?.sss ?: 0.0),
                        "PhilHealth Monthly Contribution" to formatBreakdownCurrency(deductions?.philhealth ?: 0.0),
                        "Pag-IBIG Monthly Contribution" to formatBreakdownCurrency(deductions?.pagibig ?: 0.0)
                    ),
                    totalLabel = "TOTAL STATUTORY DEDUCTIONS",
                    totalAmount = formatBreakdownCurrency(deductions?.totalStatutory ?: 0.0)
                )
            }

            item {
                SectionTable(
                    title = "WITHHOLDING TAX",
                    rows = listOf(
                        "Net Taxable Income" to formatBreakdownCurrency(breakdown.netTaxableIncome),
                        "Tax Bracket/Range" to "-"
                    ),
                    totalLabel = "WITHHOLDING TAX",
                    totalAmount = formatBreakdownCurrency(deductions?.withholdingTax ?: 0.0)
                )
            }

            item {
                SectionTable(
                    title = "SUMMARY",
                    rows = listOf(
                        "GROSS PAY" to formatBreakdownCurrency(earnings?.grossTaxableSalary ?: 0.0),
                        "GROSS DEDUCTIONS" to formatBreakdownCurrency(deductions?.totalDeductions ?: 0.0)
                    )
                )
            }

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
                        Text(text = "NET PAY", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = formatBreakdownCurrency(breakdown.netPay), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = 1.sp)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PayslipDetailInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionTable(title: String, rows: List<Pair<String, String>>, totalLabel: String? = null, totalAmount: String? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = WebSurface), border = BorderStroke(1.dp, WebBorder), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().background(TableHeaderBg).padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "AMOUNT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            rows.forEach { (label, amount) ->
                HorizontalDivider(color = WebBorder, thickness = 1.dp)
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                    Text(text = amount, fontSize = 12.sp, color = if (amount.startsWith("(")) TextRed else TextPrimary, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.weight(0.5f))
                }
            }
            if (totalLabel != null && totalAmount != null) {
                HorizontalDivider(color = WebBorder, thickness = 1.dp)
                Row(modifier = Modifier.fillMaxWidth().background(WebBackground).padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = totalLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = totalAmount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

private fun formatBreakdownCurrency(amount: Double, isDeduction: Boolean = false): String {
    val formatted = String.format(Locale.US, "₱%,.2f", amount)
    return if (isDeduction && amount > 0) "($formatted)" else formatted
}