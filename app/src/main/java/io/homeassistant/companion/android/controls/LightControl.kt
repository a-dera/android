package io.homeassistant.companion.android.controls

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.service.controls.Control
import android.service.controls.DeviceTypes
import android.service.controls.actions.BooleanAction
import android.service.controls.actions.ControlAction
import android.service.controls.actions.FloatAction
import android.service.controls.templates.RangeTemplate
import android.service.controls.templates.ToggleRangeTemplate
import androidx.annotation.RequiresApi
import io.homeassistant.companion.android.common.data.integration.Entity
import io.homeassistant.companion.android.common.data.integration.IntegrationRepository
import io.homeassistant.companion.android.webview.WebViewActivity
import kotlinx.coroutines.runBlocking

@RequiresApi(Build.VERSION_CODES.R)
class LightControl {
    companion object : HaControl {
        override fun createControl(
            context: Context,
            entity: Entity<Map<String, Any>>
        ): Control {
            val control = Control.StatefulBuilder(
                entity.entityId,
                PendingIntent.getActivity(
                    context,
                    0,
                    WebViewActivity.newInstance(context),
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            )
            control.setTitle(entity.attributes["friendly_name"].toString())
            control.setDeviceType(DeviceTypes.TYPE_LIGHT)
            control.setStatus(Control.STATUS_OK)
            control.setControlTemplate(
                ToggleRangeTemplate(
                    entity.entityId,
                    entity.state != "off",
                    "",
                    RangeTemplate(
                        entity.entityId,
                        0f,
                        255f,
                        (entity.attributes["brightness"] as? Int)?.toFloat() ?: 0f,
                        1f,
                        ""
                    )
                )
            )
            return control.build()
        }

        override fun performAction(
            integrationRepository: IntegrationRepository,
            action: ControlAction
        ): Boolean {
            return runBlocking {
                return@runBlocking when (action) {
                    is BooleanAction -> {
                        integrationRepository.callService(
                            action.templateId.split(".")[0],
                            if (action.newState) "turn_on" else "turn_off",
                            hashMapOf(
                                "entity_id" to action.templateId
                            )
                        )
                        true
                    }
                    is FloatAction -> {
                        integrationRepository.callService(
                            action.templateId.split(".")[0],
                            "turn_on",
                            hashMapOf(
                                "entity_id" to action.templateId,
                                "brightness" to action.newValue.toInt()
                            )
                        )
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }
    }
}
