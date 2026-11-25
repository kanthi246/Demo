package com.kvlabs.demo.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kvlabs.demo.domain.model.Assignment
import com.kvlabs.demo.ui.common.DateUtils
import com.kvlabs.demo.ui.common.EmptyStateView
import com.kvlabs.demo.ui.common.PriorityBadge
import com.kvlabs.demo.ui.common.StatusBadge
import com.kvlabs.demo.ui.common.SubjectBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddAssignment: () -> Unit,
    onAssignmentClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg.message,
                actionLabel = msg.actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onEvent(DashboardUiEvent.UndoDelete)
            }
            viewModel.onEvent(DashboardUiEvent.ClearMessage)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Assignments",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort Options"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.displayName,
                                            fontWeight = if (option == uiState.selectedSortOption) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.onEvent(DashboardUiEvent.SortOptionChanged(option))
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAssignment,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Assignment"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // KPI Summary Row
            SummaryKpiRow(
                stats = uiState.stats,
                selectedFilter = uiState.selectedStatusFilter,
                onFilterSelect = { filter ->
                    viewModel.onEvent(DashboardUiEvent.StatusFilterChanged(filter))
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onEvent(DashboardUiEvent.SearchQueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search by title, subject or notes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onEvent(DashboardUiEvent.SearchQueryChanged("")) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            StatusFilterRow(
                selectedFilter = uiState.selectedStatusFilter,
                onSelect = { viewModel.onEvent(DashboardUiEvent.StatusFilterChanged(it)) }
            )

            // Subject Filter Chips (shown if subjects exist)
            if (uiState.allSubjects.isNotEmpty()) {
                SubjectFilterRow(
                    subjects = uiState.allSubjects,
                    selectedSubject = uiState.selectedSubject,
                    onSelect = { viewModel.onEvent(DashboardUiEvent.SubjectFilterChanged(it)) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Area
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.assignments.isEmpty()) {
                EmptyStateView(
                    title = if (uiState.searchQuery.isNotEmpty() || uiState.selectedStatusFilter != StatusFilter.ALL) {
                        "No Matching Assignments"
                    } else {
                        "No Assignments Yet"
                    },
                    message = if (uiState.searchQuery.isNotEmpty() || uiState.selectedStatusFilter != StatusFilter.ALL) {
                        "Try adjusting your filters or search keywords."
                    } else {
                        "Tap the '+' button below to add your first assignment."
                    },
                    modifier = Modifier.padding(top = 48.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.assignments,
                        key = { it.id }
                    ) { assignment ->
                        AssignmentCard(
                            assignment = assignment,
                            onClick = { onAssignmentClick(assignment.id) },
                            onToggleComplete = {
                                viewModel.onEvent(DashboardUiEvent.ToggleCompletion(assignment))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryKpiRow(
    stats: DashboardSummaryStats,
    selectedFilter: StatusFilter,
    onFilterSelect: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiCard(
            label = "Total",
            count = stats.totalCount,
            isSelected = selectedFilter == StatusFilter.ALL,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelect(StatusFilter.ALL) }
        )
        KpiCard(
            label = "Pending",
            count = stats.pendingCount + stats.inProgressCount,
            isSelected = selectedFilter == StatusFilter.PENDING || selectedFilter == StatusFilter.IN_PROGRESS,
            color = Color(0xFFF57F17),
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelect(StatusFilter.PENDING) }
        )
        KpiCard(
            label = "Done",
            count = stats.completedCount,
            isSelected = selectedFilter == StatusFilter.COMPLETED,
            color = Color(0xFF2E7D32),
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelect(StatusFilter.COMPLETED) }
        )
        KpiCard(
            label = "Overdue",
            count = stats.overdueCount,
            isSelected = selectedFilter == StatusFilter.OVERDUE,
            color = Color(0xFFC62828),
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelect(StatusFilter.OVERDUE) }
        )
    }
}

@Composable
private fun KpiCard(
    label: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterRow(
    selectedFilter: StatusFilter,
    onSelect: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onSelect(filter) },
                label = { Text(filter.displayName, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectFilterRow(
    subjects: List<String>,
    selectedSubject: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selectedSubject == null,
            onClick = { onSelect(null) },
            label = { Text("All Subjects", fontSize = 11.sp) }
        )
        subjects.forEach { subject ->
            FilterChip(
                selected = subject == selectedSubject,
                onClick = { onSelect(if (subject == selectedSubject) null else subject) },
                label = { Text(subject, fontSize = 11.sp) }
            )
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: Assignment,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion Toggle Icon
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (assignment.isCompleted) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = if (assignment.isCompleted) "Mark Pending" else "Mark Complete",
                    tint = if (assignment.isCompleted) {
                        Color(0xFF2E7D32)
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubjectBadge(subject = assignment.subject)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PriorityBadge(priority = assignment.priority)
                        StatusBadge(
                            status = assignment.status,
                            isOverdue = assignment.isOverdue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = assignment.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (assignment.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (assignment.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (assignment.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = assignment.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (assignment.isOverdue) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = DateUtils.getRelativeDueText(assignment.dueDate, assignment.isCompleted),
                        fontSize = 12.sp,
                        fontWeight = if (assignment.isOverdue) FontWeight.Bold else FontWeight.Normal,
                        color = if (assignment.isOverdue) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
