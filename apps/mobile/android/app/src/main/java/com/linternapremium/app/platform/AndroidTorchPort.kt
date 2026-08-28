package com.linternapremium.app.platform

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import com.linternapremium.app.model.TorchResult
import com.linternapremium.app.localization.AppLanguage
import com.linternapremium.app.localization.LinternaText
import com.linternapremium.app.localization.LinternaTextCatalog
import com.linternapremium.app.localization.TextKey
import com.linternapremium.app.ports.TorchPort
import kotlin.math.roundToInt

class AndroidTorchPort(
    context: Context,
    private val simulateWhenUnavailable: Boolean = false,
    private val text: () -> LinternaText = {
        LinternaTextCatalog.forLanguage(AppLanguage.SPANISH_ARGENTINA)
    },
) : TorchPort {
    private val cameraManager = context.getSystemService(CameraManager::class.java)
    private var activeCameraId: String? = null

    override fun turnOnAtMaximum(): TorchResult = setRelativeStrength(1f)

    override fun setRelativeStrength(relativeStrength: Float): TorchResult = runTorchAction {
        val cameraId = findTorchCameraId()
            ?: return if (simulateWhenUnavailable) {
                TorchResult.Success
            } else {
                TorchResult.Failure(text()[TextKey.TORCH_NO_FLASH])
            }
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val maximumLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
        } else {
            1
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && maximumLevel > 1) {
            val strengthLevel = (1 + (maximumLevel - 1) * relativeStrength.coerceIn(0.01f, 1f))
                .roundToInt()
                .coerceIn(1, maximumLevel)
            cameraManager.turnOnTorchWithStrengthLevel(cameraId, strengthLevel)
        } else {
            cameraManager.setTorchMode(cameraId, true)
        }
        activeCameraId = cameraId
        TorchResult.Success
    }

    override fun turnOff(): TorchResult = runTorchAction {
        val cameraId = activeCameraId ?: findTorchCameraId() ?: return TorchResult.Success
        cameraManager.setTorchMode(cameraId, false)
        activeCameraId = null
        TorchResult.Success
    }

    private fun findTorchCameraId(): String? {
        val flashCameras = cameraManager.cameraIdList.filter { cameraId ->
            cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
        return flashCameras.firstOrNull { cameraId ->
            cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        } ?: flashCameras.firstOrNull()
    }

    private inline fun runTorchAction(action: () -> TorchResult): TorchResult = try {
        action()
    } catch (_: SecurityException) {
        TorchResult.Failure(text()[TextKey.TORCH_ACCESS_ERROR])
    } catch (_: CameraAccessException) {
        TorchResult.Failure(text()[TextKey.TORCH_BUSY])
    } catch (_: IllegalArgumentException) {
        TorchResult.Failure(text()[TextKey.TORCH_UNAVAILABLE])
    }
}
