package com.raf.doubletaptolock.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raf.doubletaptolock.R
import com.raf.doubletaptolock.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.checkPermission(context)
    }

    // Haptic Feedback
    LaunchedEffect(state.height, state.touchArea, state.isLandscapeEnabled) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_launcher_monochrome),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(150.dp)
        )
        if (state.isServiceEnabled) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.service_is_active))
                    }
                    append("\n")
                    append(stringResource(R.string.service_is_active_desc))
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )

            /**
             * Settings
             */
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                HorizontalDivider()

                // Touch Area
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = stringResource(R.string.touch_area), modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = viewModel::previousTouchArea) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_keyboard_arrow_left),
                                contentDescription = stringResource(R.string.previous)
                            )
                        }
                        Text(
                            text = state.touchArea.name,
                            maxLines = 1,
                            modifier = Modifier.animateContentSize()
                        )
                        IconButton(onClick = viewModel::nextTouchArea) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_keyboard_arrow_right),
                                contentDescription = stringResource(R.string.next)
                            )
                        }
                    }
                }

                // Height
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.touch_height),
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = state.height != 0,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            OutlinedIconButton(
                                onClick = viewModel::resetHeight
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_reset_settings),
                                    contentDescription = stringResource(R.string.reset_height)
                                )
                            }
                            VerticalDivider(modifier = Modifier.height(IntrinsicSize.Max))
                        }
                        IconButton(onClick = viewModel::decreaseHeight) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_remove),
                                contentDescription = stringResource(R.string.decrease_height)
                            )
                        }
                        Text(
                            text = if (state.height == 0) stringResource(R.string.default_height)
                            else state.height.toString(),
                            maxLines = 1,
                            modifier = Modifier.animateContentSize()
                        )
                        IconButton(onClick = viewModel::increaseHeight) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                                contentDescription = stringResource(R.string.increase_height)
                            )
                        }
                    }
                }

                // Landscape Settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.enable_in_landscape),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.isLandscapeEnabled,
                        onCheckedChange = viewModel::toggleLandscape
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.accessibility_service_required),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.accessibility_service_required_desc),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            ) {
                Text(stringResource(R.string.open_accessibility_settings))
            }
        }
    }
}