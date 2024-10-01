package com.afi.gemichat

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.afi.gemichat.App.Companion.applicationScope
import com.afi.gemichat.ui.enums.ThemeMode
import com.afi.gemichat.ui.model.ThemeModel
import com.afi.gemichat.ui.theme.GemiChatTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mainViewModel by viewModels<MainViewModel>()
        val themeModel: ThemeModel by viewModels()

        setContent {
            GemiChatTheme(
                when (themeModel.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.DARK -> true
                    else -> false
                }
            ) {

                val isDarkTheme = themeModel.themeMode == ThemeMode.DARK
                val view = LocalView.current

                val themeShifter = {
                    applicationScope.launch(Dispatchers.IO) {
                        val newThemeMode = if (isDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK
                        themeModel.updateThemeMode(newThemeMode)
                    }
                }

                var promptText by remember {
                    mutableStateOf("")
                }

                val conversations = mainViewModel.conversations
                val isGenerating by mainViewModel.isGenerating

                val keyboardController = LocalSoftwareKeyboardController.current

                val imageBitmaps: SnapshotStateList<Bitmap> = remember {
                    mutableStateListOf()
                }

                val context = LocalContext.current

                val photoPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
                        uris.forEach { uri ->
                            @Suppress("DEPRECATION") imageBitmaps.add(
                                when {
                                    Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                                        context.contentResolver, uri
                                    )

                                    else -> {
                                        val source =
                                            ImageDecoder.createSource(context.contentResolver, uri)
                                        ImageDecoder.decodeBitmap(source)
                                    }
                                }
                            )

                        }
                    }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text(text = stringResource(R.string.app_name)) },
                                actions = {
                                    IconButton(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                            themeShifter()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                            contentDescription = stringResource(R.string.theme_change)
                                        )
                                    }
                                },
                            )
                        },
                        bottomBar = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                                    .wrapContentHeight()
                            ) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(imageBitmaps.size) { index ->
                                        val imageBitmap = imageBitmaps[index]
                                        Modifier.height(100.dp)
                                        Image(bitmap = imageBitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .animateItem(
                                                    fadeInSpec = null,
                                                    fadeOutSpec = null,
                                                    placementSpec = spring(
                                                        stiffness = Spring.StiffnessMediumLow,
                                                        visibilityThreshold = IntOffset.VisibilityThreshold,
                                                    )
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                )
                                                .clickable {
                                                    imageBitmaps.remove(imageBitmap)
                                                }
                                        )
                                    }
                                }

                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    shape = RoundedCornerShape(180.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp, horizontal = 12.dp)
                                            .wrapContentHeight(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BasicTextField(
                                            value = promptText,
                                            onValueChange = { promptText = it },
                                            textStyle = TextStyle(
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            decorationBox = { innerTextField ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {

                                                    IconButton(onClick = {
                                                        photoPicker.launch(
                                                            PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                                                        )
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Add,
                                                            contentDescription = null
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(width = 8.dp))

                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .wrapContentHeight()
                                                    ) {
                                                        if (promptText.isEmpty()) {
                                                            Text(
                                                                text = stringResource(R.string.message),
                                                                fontSize = 18.sp,
                                                            )
                                                        }
                                                        innerTextField()
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .navigationBarsPadding()
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = {
                                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                if (promptText.isNotBlank() && isGenerating.not()) {
                                                    mainViewModel.sendText(promptText, imageBitmaps)
                                                    promptText = ""
                                                    imageBitmaps.clear()
                                                    keyboardController?.hide()
                                                } else if (promptText.isBlank()) {
                                                    Toast.makeText(
                                                        context,
                                                        "Please enter a message",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier,
                                        ) {
                                            AnimatedContent(
                                                targetState = isGenerating,
                                                label = "",
                                            ) { generating ->
                                                if (generating) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(
                                                            20.dp
                                                        ), strokeWidth = 3.dp
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                                        contentDescription = null,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }


                            }
                        }) {
                        ConversationScreen(
                            conversations = conversations,
                            modifier = Modifier.padding(it),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationScreen(
    conversations: SnapshotStateList<Triple<String, String, List<Bitmap>?>>,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(conversations.size) { index ->
            val conversation = conversations[index]
            val isInComingBoolean = conversation.first == "received"

            Modifier
                .fillMaxWidth()
            MessageItem(
                isInComing = isInComingBoolean,
                images = conversation.third ?: emptyList(),
                content = conversation.second,
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                    placementSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold,
                    )
                )
            )
        }
    }
}


@Composable
fun MessageItem(
    isInComing: Boolean,
    images: List<Bitmap>,
    content: String,
    modifier: Modifier = Modifier,
) {

    val cardShape by remember {
        derivedStateOf {
            RoundedCornerShape(
                18.dp
            )
        }
    }

    val cardPadding by remember {
        derivedStateOf {
            if (isInComing) {
                PaddingValues(end = 24.dp)
            } else {
                PaddingValues(start = 24.dp)
            }
        }
    }

    val horizontalAlignment = if (isInComing) Alignment.Start else Alignment.End

    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
    ) {
        if (images.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
            ) {
                items(images.size) { index ->
                    val image = images[index]
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .height(60.dp)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            )
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .wrapContentHeight()
                .wrapContentWidth()
                .padding(cardPadding),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = if (isInComing) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.animateContentSize(
                        animationSpec = spring(),
                    )
                )
            }
        }
    }
}