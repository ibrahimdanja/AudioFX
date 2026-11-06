package com.bearinmind.equalizer314.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class Eq314TileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val service = EqService.instance
        if (service != null) {
            val isNowOn = qsTile.state != Tile.STATE_ACTIVE
            service.dspEngine.setPower(isNowOn)
            service.updateStatus("Quick Settings", isNowOn)
        }
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val service = EqService.instance
        val isActive = service != null
        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "AudioFX"
        tile.updateTile()
    }
}
