package data

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.impl.campaign.DebugFlags
import data.intel.DevUtilsSettingsIntel
import data.scripts.util.MagicSettings
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class DevHotkeys : EveryFrameScript
{

    var keyPressed = false
    var keyCooldown = 0
    var keyCooldownMax = 50

    override fun advance(amount: Float) {

        val paused = Global.getSector().campaignUI.currentCoreTab == CoreUITabId.FLEET || Global.getSector().campaignUI.currentCoreTab == null
                && !Global.getSector().campaignUI.isShowingDialog && !Global.getSector().campaignUI.isShowingMenu

        if (!paused) return



        if (keyPressed)
        {
            if (keyCooldown <= keyCooldownMax)
            {
                keyCooldown++
            }
            else
            {
                keyCooldown = 0
                keyPressed = false
            }
            return
        }

        if (Keyboard.isKeyDown((DevUtilsPlugin.PressedKey)) || Mouse.isButtonDown(DevUtilsPlugin.PressedMouseButton))
        {
            Global.getSettings().isDevMode = !Global.getSettings().isDevMode
            DebugFlags.setStandardConfig()
            keyPressed = true
        }
    }

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

}