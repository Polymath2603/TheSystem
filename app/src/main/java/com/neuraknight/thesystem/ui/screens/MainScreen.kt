package com.neuraknight.thesystem.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuraknight.thesystem.R
import com.neuraknight.thesystem.data.models.User
import com.neuraknight.thesystem.ui.screens.tabs.*
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import java.io.File
import kotlin.math.roundToInt

data class TabItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val appData = viewModel.appData
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = remember(appData.settings.showHabits, appData.settings.showPrayers) {
        buildList {
            add(TabItem("Workout", Icons.Default.FitnessCenter))
            if (appData.settings.showPrayers) add(TabItem("Prayers", Icons.Default.Mosque))
            add(TabItem("Habits", Icons.Default.History))
            add(TabItem("Profile", Icons.Default.Person))
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            UserInfoHeader(user = appData.user)
            Spacer(modifier = Modifier.height(16.dp))
            XPBar(progress = appData.user.xpProgress, needed = appData.user.xpNeeded)
            Spacer(modifier = Modifier.height(24.dp))

            val correctedIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex.coerceAtLeast(0))
            
            // Calculate tab width accounting for padding
            val tabPadding = 8.dp
            val totalPadding = tabPadding * (tabs.size + 1)
            
            ScrollableTabRow(
                selectedTabIndex = correctedIndex,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty() && correctedIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[correctedIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                edgePadding = tabPadding,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, tabItem ->
                    Tab(
                        selected = correctedIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = { 
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = tabItem.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = tabItem.title.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (correctedIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = tabs.getOrNull(correctedIndex)?.title,
                transitionSpec = { fadeIn() with fadeOut() }
            ) { targetTabTitle ->
                when (targetTabTitle) {
                    "Workout" -> WorkoutTab(viewModel)
                    "Prayers" -> PrayerTab(viewModel)
                    "Habits" -> HabitsTab(viewModel)
                    "Profile" -> ProfileTab(viewModel)
                }
            }
        }
    }
}

@Composable
fun UserInfoHeader(user: User) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .shadow(elevation = 10.dp, shape = CircleShape, spotColor = MaterialTheme.colorScheme.primary)
        ) {
            if (user.profileImg.isNotEmpty() && File(user.profileImg).exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(user.profileImg)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "User Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    ProfilePlaceholder()
                }
            } else {
                ProfilePlaceholder()
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = user.name.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LVL ${user.level}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = " [${user.rank}] ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "${user.characterClass} | ${user.currentTitle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "🎫 PASSES: ${user.passcards}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ProfilePlaceholder() {
    Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = "User Profile Placeholder",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun XPBar(progress: Double, needed: Double) {
    val progressFraction = if (needed > 0) (progress / needed).toFloat() else 0f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "XP PROGRESS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "${progress.roundToInt()} / ${needed.roundToInt()}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
        }
    }
}