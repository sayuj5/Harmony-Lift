package com.harmonylift.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.harmonylift.app.download.ModelDownloadManager
import com.harmonylift.app.presentation.TheoryTutorViewModel
import com.harmonylift.audio.AudioRecorderEngine
import com.harmonylift.theory.engine.TheoryEngine
import com.harmonylift.theory.domain.usecase.DetectChordUseCase
import com.harmonylift.theory.domain.usecase.DetectIntervalUseCase
import com.harmonylift.theory.domain.usecase.DetectScaleUseCase
import com.harmonylift.ui.screens.*
import com.harmonylift.ui.theme.HarmonyLiftTheme
import com.harmonylift.ui.theme.ThemePreferences
import com.harmonylift.app.download.ModelDownloadState
import com.harmonylift.app.ui.ModelDownloadScreen
import com.harmonylift.app.utils.HarmonyLiftDebug
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HarmonyLiftDebug.d("[MainActivity] onCreate() start.")

        // Log microphone permission state
        val micPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val micGranted = micPermission == PackageManager.PERMISSION_GRANTED
        HarmonyLiftDebug.d("[MainActivity] RECORD_AUDIO permission: ${if (micGranted) "GRANTED" else "DENIED"}")

        // --- Dependencies ---
        val theoryEngine = TheoryEngine()
        val detectChordUseCase = DetectChordUseCase(theoryEngine)
        val detectIntervalUseCase = DetectIntervalUseCase(theoryEngine)
        val detectScaleUseCase = DetectScaleUseCase(theoryEngine)
        val localModelManager = com.harmonylift.tutor.data.local.LocalModelManager()
        val practiceRepository = com.harmonylift.app.data.PracticeRepository(applicationContext)

        // --- ModelDownloadManager: single source of truth for model availability ---
        ModelDownloadManager.checkExistingModel(applicationContext)
        HarmonyLiftDebug.d("[MainActivity] ModelDownloadManager initialised. state=${ModelDownloadManager.state.value}")

        // --- ViewModels observe download state rather than asset extraction ---
        val viewModel = TheoryTutorViewModel(
            detectChordUseCase = detectChordUseCase,
            detectIntervalUseCase = detectIntervalUseCase,
            detectScaleUseCase = detectScaleUseCase,
            localModelManager = localModelManager,
            downloadState = ModelDownloadManager.state
        )

        val practiceSessionViewModel = com.harmonylift.app.presentation.PracticeSessionViewModel(
            detectChordUseCase = detectChordUseCase,
            localModelManager = localModelManager,
            downloadState = ModelDownloadManager.state,
            practiceRepository = practiceRepository
        )

        val audioRecorderEngine = AudioRecorderEngine()
        val audioPipeline = AudioToTheoryPipeline(audioRecorderEngine = audioRecorderEngine)
        audioPipeline.onNoteDetected = { note ->
            viewModel.onNoteDetected(note)
            practiceSessionViewModel.onNoteDetected(note)
        }

        val themePreferences = ThemePreferences(applicationContext)
        HarmonyLiftDebug.d("[MainActivity] All dependencies wired. Calling setContent().")

        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(
                initial = com.harmonylift.ui.theme.ThemeMode.SYSTEM
            )
            SideEffect {
                HarmonyLiftDebug.d("[Theme] Recomposition: current themeMode=$themeMode")
            }
            HarmonyLiftTheme(themeMode = themeMode) {
                HarmonyLiftApp(viewModel, practiceSessionViewModel, audioPipeline, practiceRepository)
            }
        }
        HarmonyLiftDebug.d("[MainActivity] onCreate() complete.")
    }
}

@Composable
fun HarmonyLiftApp(
    viewModel: TheoryTutorViewModel,
    practiceSessionViewModel: com.harmonylift.app.presentation.PracticeSessionViewModel,
    audioPipeline: AudioToTheoryPipeline,
    practiceRepository: com.harmonylift.app.data.PracticeRepository
) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val downloadState by ModelDownloadManager.state.collectAsState()
    
    NavHost(
        navController = navController, 
        startDestination = "splash",
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            ) + fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            ) + fadeOut(animationSpec = tween(500))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            ) + fadeIn(animationSpec = tween(500))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            ) + fadeOut(animationSpec = tween(500))
        }
    ) {
        composable("splash") {
            SplashScreen(onSplashComplete = {
                navController.navigate("welcome") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        
        composable("welcome") {
            WelcomeScreen(
                onStartLearning = { navController.navigate("mic_permission") },
                onAlreadyPlay = { navController.navigate("instrument_selection") }
            )
        }
        
        composable("mic_permission") {
            MicrophonePermissionScreen(
                onPermissionGranted = { 
                    navController.navigate("instrument_selection") {
                        popUpTo("mic_permission") { inclusive = true }
                    }
                },
                onSkip = { navController.navigate("instrument_selection") }
            )
        }
        
        composable("instrument_selection") {
            InstrumentSelectionScreen(
                onContinue = { instrument ->
                    navController.navigate("dashboard/$instrument") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard/{instrument}") { backStackEntry ->
            val instrument = backStackEntry.arguments?.getString("instrument") ?: "Piano"
            DashboardScreen(
                instrument = instrument,
                onNavigateToLiveListening = { navController.navigate("live_listening") },
                onNavigateToPracticeCoach = {
                    if (downloadState is ModelDownloadState.Ready) navController.navigate("practice_coach")
                    else navController.navigate("model_download")
                },
                onNavigateToAiTutor = {
                    if (downloadState is ModelDownloadState.Ready) navController.navigate("ai_tutor_intro")
                    else navController.navigate("model_download")
                },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        
        composable("live_listening") {
            val state by viewModel.uiState.collectAsState()
            
            val previousEntry = navController.previousBackStackEntry
            val instrument = previousEntry?.arguments?.getString("instrument") ?: "Piano"
            
            val scope = rememberCoroutineScope()
            androidx.compose.runtime.LaunchedEffect(Unit) {
                audioPipeline.start(scope)
            }
            
            androidx.compose.runtime.DisposableEffect(Unit) {
                onDispose {
                    audioPipeline.stop()
                    viewModel.resetSession()
                }
            }

            LiveListeningScreen(
                state = state,
                waveformFlow = audioPipeline.rawWaveform,
                instrument = instrument,
                onStopListening = { navController.navigate("session_summary/$instrument") }
            )
        }
        
        composable("session_summary/{instrument}") { backStackEntry ->
            val instrument = backStackEntry.arguments?.getString("instrument") ?: "Piano"
            val recentSessions by practiceRepository.allSessions.collectAsState(initial = emptyList())
            val session = recentSessions.firstOrNull()
            SessionSummaryScreen(
                durationMs = session?.durationMs ?: 0L,
                notesDetected = session?.notesDetected ?: 0,
                pitchStability = session?.pitchStability ?: 100f,
                onReturnHome = {
                    navController.popBackStack("dashboard/$instrument", inclusive = false)
                }
            )
        }
        
        composable("practice_coach") {
            val totalXp by practiceRepository.totalXp.collectAsState(initial = 0)
            PracticeCoachScreen(
                totalXp = totalXp,
                onStartSession = { mode ->
                    practiceSessionViewModel.startSession(mode)
                    navController.navigate("practice_session")
                },
                onViewAnalytics = { navController.navigate("progress_analytics") },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("practice_session") {
            val state by practiceSessionViewModel.uiState.collectAsState()
            
            val scope = rememberCoroutineScope()
            androidx.compose.runtime.LaunchedEffect(Unit) {
                audioPipeline.start(scope)
            }
            
            androidx.compose.runtime.DisposableEffect(Unit) {
                onDispose {
                    audioPipeline.stop()
                    practiceSessionViewModel.resetSession()
                }
            }

            PracticeSessionScreen(
                state = state,
                onEndSession = { 
                    practiceSessionViewModel.endSession()
                    navController.popBackStack() 
                }
            )
        }
        
        composable("progress_analytics") {
            val sessions by practiceRepository.allSessions.collectAsState(initial = emptyList())
            val totalTime by practiceRepository.totalPracticeTimeMs.collectAsState(initial = 0L)
            val overallAccuracy = if (sessions.isNotEmpty()) sessions.map { it.accuracy }.average().toFloat() else 0f
            ProgressAnalyticsScreen(
                totalSessions = sessions.size,
                overallAccuracy = overallAccuracy,
                totalPracticeTimeMs = totalTime,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("ai_tutor_intro") {
            AiTutorIntroScreen(
                onStartTutor = { navController.navigate("ai_tutor_mentor") }
            )
        }
        
        composable("ai_tutor_mentor") {
            val state by viewModel.uiState.collectAsState()
            
            // Trigger AI tutor on launch
            androidx.compose.runtime.LaunchedEffect(Unit) {
                viewModel.askTutor()
            }

            AiTutorMentorScreen(
                state = state,
                onAskQuestion = { query -> viewModel.askTutor(query) },
                onEndSession = { 
                    viewModel.clearHistory()
                    navController.popBackStack("ai_tutor_intro", inclusive = true) 
                }
            )
        }
        
        composable("model_download") {
            val dlState by ModelDownloadManager.state.collectAsState()
            ModelDownloadScreen(
                state = dlState,
                onStartDownload = { ModelDownloadManager.startDownload(context) },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("settings") {
            val scope = rememberCoroutineScope()
            val totalXp by practiceRepository.totalXp.collectAsState(initial = 0)
            val totalTime by practiceRepository.totalPracticeTimeMs.collectAsState(initial = 0L)
            val sessions by practiceRepository.allSessions.collectAsState(initial = emptyList())
            val accuracy = if (sessions.isNotEmpty()) sessions.map { it.accuracy }.average().toFloat() else 0f
            
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigatePrivacyPolicy = { navController.navigate("privacy_policy") },
                onExportData = { format ->
                    scope.launch {
                        val minutes = totalTime / 60000
                        val accuracyInt = (accuracy * 100).toInt()
                        val currentLevel = (totalXp / 500) + 1
                        
                        val realData = mapOf(
                            "Total Practice Time" to "$minutes minutes",
                            "Current Level" to "$currentLevel",
                            "XP" to "$totalXp",
                            "Accuracy" to "$accuracyInt%",
                            "Total Sessions" to "${sessions.size}",
                            "App Version" to "5.1.0"
                        )
                        val result = if (format == "PDF") {
                            com.harmonylift.app.export.DataExporter.exportAsPdf(context, realData)
                        } else {
                            com.harmonylift.app.export.DataExporter.exportAsTxt(context, realData)
                        }
                        result.onSuccess { file ->
                            com.harmonylift.app.export.DataExporter.shareFile(context, file)
                        }
                    }
                }
            )
        }
        
        composable("privacy_policy") {
            PrivacyPolicyScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
