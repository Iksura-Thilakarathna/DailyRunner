package com.dailyrunner.drivertracker.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrunner.drivertracker.data.repository.WeeklyChequeSummary
import com.dailyrunner.drivertracker.ui.theme.OnPaidGreen
import com.dailyrunner.drivertracker.ui.theme.OnUnpaidAmber
import com.dailyrunner.drivertracker.ui.theme.PaidGreen
import com.dailyrunner.drivertracker.ui.theme.PaidGreenBg
import com.dailyrunner.drivertracker.ui.theme.UnpaidAmber
import com.dailyrunner.drivertracker.ui.theme.UnpaidAmberBg
import com.dailyrunner.drivertracker.ui.viewmodel.ChequeFilter
import com.dailyrunner.drivertracker.ui.viewmodel.WeeklyChequesViewModel
import com.dailyrunner.drivertracker.util.PayPeriodUtils
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyChequesScreen(
    viewModel: WeeklyChequesViewModel,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Confirmation dialog for marking a cheque back to UNPAID
    if (uiState.showUnpaidConfirmDialog && uiState.pendingToggleCheque != null) {
        val cheque = uiState.pendingToggleCheque!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDialog() },
            title = {
                Text(
                    text = "Mark Cheque as Unpaid?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to mark the cheque for period ${cheque.displayRange} as UNPAID?"
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmUnpaidToggle() },
                    colors = ButtonDefaults.buttonColors(containerColor = UnpaidAmber)
                ) {
                    Text("Yes, Mark Unpaid", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Weekly Cheques",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Friday – Thursday Pay Cycles",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Tabs [ All ] | [ Unpaid Cheques ] | [ Paid ]
        PrimaryTabRow(
            selectedTabIndex = uiState.selectedFilter.ordinal,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Tab(
                selected = uiState.selectedFilter == ChequeFilter.ALL,
                onClick = { viewModel.selectFilter(ChequeFilter.ALL) },
                text = { Text("All", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = uiState.selectedFilter == ChequeFilter.UNPAID,
                onClick = { viewModel.selectFilter(ChequeFilter.UNPAID) },
                text = { Text("Unpaid", fontWeight = FontWeight.Bold, color = UnpaidAmber) }
            )
            Tab(
                selected = uiState.selectedFilter == ChequeFilter.PAID,
                onClick = { viewModel.selectFilter(ChequeFilter.PAID) },
                text = { Text("Paid", fontWeight = FontWeight.Bold, color = PaidGreen) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.cheques.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Cheques Found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Log daily trips to populate Friday–Thursday pay periods.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.cheques, key = { it.weekId }) { cheque ->
                    WeeklyChequeCard(
                        summary = cheque,
                        isExpanded = uiState.expandedWeekId == cheque.weekId,
                        onToggleExpand = { viewModel.toggleCardExpansion(cheque.weekId) },
                        onTogglePayment = { viewModel.onRequestPaymentToggle(cheque) },
                        onShare = {
                            val text = viewModel.buildShareSummaryText(cheque)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Cheque Summary")
                            context.startActivity(shareIntent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyChequeCard(
    summary: WeeklyChequeSummary,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onTogglePayment: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top Row: Pay Period Range + Expansion Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = summary.displayRange,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Share Button
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onShare() },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Expand Icon
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onToggleExpand() },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Cheque Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rs. ${String.format(Locale.ENGLISH, "%,.2f", summary.totalChequeAmount)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${summary.totalDaysWorked} Days Worked",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format(Locale.ENGLISH, "%.1f", summary.totalDistanceKm)} KM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val cardHaptic = LocalHapticFeedback.current
            // Interactive Payment Status Button
            if (summary.isPaid) {
                // Paid Badge Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            cardHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePayment()
                        },
                    color = PaidGreenBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PaidGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val paidDateStr = PayPeriodUtils.formatTimestamp(summary.paidAt)
                        Text(
                            text = if (paidDateStr.isNotBlank()) "Paid on $paidDateStr ✓ (Tap to change)" else "Paid ✓ (Tap to change)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnPaidGreen
                        )
                    }
                }
            } else {
                // Unpaid Outlined Button
                OutlinedButton(
                    onClick = {
                        cardHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTogglePayment()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = UnpaidAmberBg,
                        contentColor = OnUnpaidAmber
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = UnpaidAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mark as Paid ✓",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnUnpaidAmber
                    )
                }
            }

            // Expanded Itemized Daily Breakdown
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Day-by-Day Breakdown:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    summary.trips.forEach { trip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = PayPeriodUtils.formatDateForDisplay(trip.date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (trip.isNoWork) "OFF DAY" else trip.destinations,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (trip.isNoWork) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "OFF",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Rs. ${String.format(Locale.ENGLISH, "%.0f", trip.totalEarnings)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%.1f", trip.totalKm)} KM (${trip.startKm} -> ${trip.endKm})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
