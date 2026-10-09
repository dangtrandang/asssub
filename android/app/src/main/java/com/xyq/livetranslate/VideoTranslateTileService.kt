package com.xyq.livetranslate

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * 快捷设置磁贴 (Quick Settings Tile)：
 * 允许用户在观看任意视频或使用其他 App 时，从系统下拉菜单一键开启或停止视频翻译悬浮字幕。
 */
class VideoTranslateTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        if (StatusBus.serviceRunning) {
            // 正在运行翻译 -> 一键停止
            val stopIntent = Intent(this, CaptureService::class.java).apply {
                action = CaptureService.ACTION_STOP
            }
            startService(stopIntent)
            qsTile?.apply {
                state = Tile.STATE_INACTIVE
                subtitle = getString(R.string.rt_tile_idle)
                updateTile()
            }
        } else {
            // 未运行 -> 启动透明跳板请求授权并开跑
            val trampolineIntent = Intent(this, QuickLaunchTrampolineActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (Build.VERSION.SDK_INT >= 34) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    trampolineIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(trampolineIntent)
            }
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isRunning = StatusBus.serviceRunning
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.subtitle = if (isRunning) {
            getString(R.string.rt_tile_running)
        } else {
            getString(R.string.rt_tile_idle)
        }
        tile.updateTile()
    }
}
