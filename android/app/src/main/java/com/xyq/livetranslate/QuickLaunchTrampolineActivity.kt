package com.xyq.livetranslate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * 快捷启动跳板 Activity：透明无界面，专用于 Quick Settings Tile 或通知快捷启动视频翻译。
 * 获取 MediaProjection 投屏授权后立即启动 CaptureService 并 finish()，不遮挡用户正在观看的视频内容。
 */
class QuickLaunchTrampolineActivity : ComponentActivity() {

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            startVideoTranslation(result.resultCode, result.data!!)
        } else {
            Toast.makeText(this, R.string.rt_toast_projection_permission_denied, Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
            requestProjection()
        } else {
            Toast.makeText(this, R.string.rt_toast_record_audio_permission_required, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 检查 API Key
        if (SettingsStore.apiKeyList(this).isEmpty()) {
            Toast.makeText(this, R.string.rt_toast_fill_gemini_api_key_first, Toast.LENGTH_LONG).show()
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(MainActivity.EXTRA_OPEN_SESSION_TAB, StatusBus.MODE_VIDEO)
            }
            startActivity(mainIntent)
            finish()
            return
        }

        // 2. 检查悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, R.string.rt_toast_overlay_permission_required, Toast.LENGTH_LONG).show()
            val overlayIntent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            )
            startActivity(overlayIntent)
            finish()
            return
        }

        // 3. 检查音频录制 / 通知权限
        val neededPermissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (neededPermissions.isNotEmpty()) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            requestProjection()
        }
    }

    private fun requestProjection() {
        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    private fun startVideoTranslation(resultCode: Int, data: Intent) {
        val mode = TranslationMode.VIDEO
        val plan = TranslationPlanStore.loadDraft(this, mode)
        val scene = SceneLibraryStore.resolve(this, mode, plan.scenePresetId)
        val prompt = PromptBuilder.build(scene = scene, plan = plan)

        val startIntent = Intent(this, CaptureService::class.java).apply {
            action = CaptureService.ACTION_START
            putExtra(CaptureService.EXTRA_MODE, StatusBus.MODE_VIDEO)
            putExtra(CaptureService.EXTRA_SESSION_PROMPT, prompt)
            putExtra(CaptureService.EXTRA_SOURCE_LANGUAGE, plan.sourceLanguageCode)
            putExtra(CaptureService.EXTRA_TARGET_LANGUAGE, plan.targetLanguageCode)
            putExtra(CaptureService.EXTRA_SCENE_PRESET, scene.id)
            putExtra(CaptureService.EXTRA_SCENE_LABEL, scene.label)
            putExtra(CaptureService.EXTRA_SESSION_TITLE, getString(R.string.rt_history_title_video))
            putExtra(CaptureService.EXTRA_SESSION_CONTEXT, "")
            putExtra(CaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(CaptureService.EXTRA_RESULT_DATA, data)
        }
        startForegroundService(startIntent)
    }
}
