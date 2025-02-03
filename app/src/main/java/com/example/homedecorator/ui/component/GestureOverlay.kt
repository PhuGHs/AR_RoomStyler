package com.example.homedecorator.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.homedecorator.R
import com.example.homedecorator.util.Constants
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
internal fun GestureOverlay(
    modifier: Modifier = Modifier,
    iterations: Int = Constants.DEFAULT_GESTURE_OVERLAY_ITERATIONS,
    onComplete: () -> Unit
) {
    val specs = listOf(
        R.raw.move_gesture_animation
    ).map(LottieCompositionSpec::RawRes)

    val messages = listOf(
        R.string.message_move_gesture,
        R.string.message_rotate_gesture,
        R.string.message_zoom_gesture
    ).map { stringResource(it) }

    var playedCount by rememberSaveable { mutableIntStateOf(Constants.PLAY_COUNT_ZERO) }

    val currentGestureIndex by remember(playedCount) {
        // If each gesture is already played for [iterations] times, call onComplete
        if (playedCount >= iterations * specs.size) {
            onComplete()
        }
        mutableIntStateOf(playedCount % specs.size)
    }

    Column(
        modifier = modifier
            .padding(120.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        key(currentGestureIndex) {
            Text(
                text = messages[currentGestureIndex],
                style = MaterialTheme.typography.titleLarge
            )

            LottieView(compositionSpec = specs[currentGestureIndex]) {
                playedCount++
            }
        }
    }
}

@Composable
private fun LottieView(
    modifier: Modifier = Modifier,
    compositionSpec: LottieCompositionSpec,
    onEnd: () -> Unit
) {
    val composition by rememberLottieComposition(compositionSpec)
    val progress by animateLottieCompositionAsState(composition)
    LottieAnimation(
        modifier = modifier.aspectRatio(DimenOptions.GestureAnimRatio),
        composition = composition,
        contentScale = ContentScale.FillWidth,
        progress = {
            if (progress == Constants.ANIM_COMPLETE_VALUE) {
                onEnd()
            }
            progress
        }
    )
}

object DimenOptions {
    const val GestureAnimRatio = 1F
}