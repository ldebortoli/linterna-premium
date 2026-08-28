package com.linternapremium.app.platform

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import com.linternapremium.app.model.TorchResult
import com.linternapremium.app.ports.TorchPort

class AndroidTorchPort(context: Context) : TorchPort {
    private val cameraManager = context.getSystemService(CameraManager::class.java)
    private var activeCameraId: String? = null

    override fun turnOnAtMaximum(): TorchResult = runTorchAction {
        val cameraId = findTorchCameraId()
            ?: return TorchResult.Failure("Este telefono no tiene un flash disponible.")
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val maximumLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
        } else {
            1
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && maximumLevel > 1) {
            cameraManager.turnOnTorchWithStrengthLevel(cameraId, maximumLevel)
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
        TorchResult.Failure("No pudimos acceder al flash. Revisa el permiso de camara.")
    } catch (_: CameraAccessException) {
        TorchResult.Failure("La camara esta ocupada. Cerrala e intenta otra vez.")
    } catch (_: IllegalArgumentException) {
        TorchResult.Failure("El flash no esta disponible en este momento.")
    }
}

