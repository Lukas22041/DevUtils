import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.Misc
import data.DevHotkeys
import data.DevUtilsIntelManager
import data.intel.DevUtilsOverlay
import org.lazywizard.lazylib.JSONUtils
import java.awt.Color

class DevUtilsPlugin : BaseModPlugin()
{

    companion object
    {
        @JvmStatic
        var data: JSONUtils.CommonDataJSONObject? = null

        var PressedKey = 0
        var PressedMouseButton = 0
        var maxEntries = 0
        var devModeOnlyIntel = true

        var OverlayPositionX = 0.7f
        var OverlayPositionY = 0.9f
        var OverlayZoom = 0.5f
        var OverlayColor = 0

        @JvmStatic
        fun saveDataToJson()
        {
            DevUtilsPlugin.data!!.put("DevmodeKeybind",  DevUtilsPlugin.PressedKey)
            DevUtilsPlugin.data!!.put("DevmodeMouseButton",  DevUtilsPlugin.PressedMouseButton)
            DevUtilsPlugin.data!!.put("devModeOnlyIntel",  DevUtilsPlugin.devModeOnlyIntel)
            DevUtilsPlugin.data!!.put("maxEntries",  DevUtilsPlugin.maxEntries)

            DevUtilsPlugin.data!!.put("overlayPosX",  DevUtilsPlugin.OverlayPositionX)
            DevUtilsPlugin.data!!.put("overlayPosY",  DevUtilsPlugin.OverlayPositionY)
            DevUtilsPlugin.data!!.put("overlayZoom",  DevUtilsPlugin.OverlayZoom)
            DevUtilsPlugin.data!!.put("overlayColor",  DevUtilsPlugin.OverlayColor)


            DevUtilsPlugin.data!!.save()
        }

        @JvmStatic
        fun loadDataFromJson()
        {
            data = JSONUtils.loadCommonJSON("devutils/DevUtilsSettings.json", "data/config/DevUtilsSettings.default");
            PressedKey = DevUtilsPlugin.data!!.getInt("DevmodeKeybind")
            PressedMouseButton = DevUtilsPlugin.data!!.getInt("DevmodeMouseButton")
            devModeOnlyIntel = DevUtilsPlugin.data!!.getBoolean("devModeOnlyIntel")
            maxEntries = DevUtilsPlugin.data!!.getInt("maxEntries")

            OverlayPositionX = DevUtilsPlugin.data!!.getDouble("overlayPosX").toFloat()
            OverlayPositionY = DevUtilsPlugin.data!!.getDouble("overlayPosY").toFloat()
            OverlayZoom = DevUtilsPlugin.data!!.getDouble("overlayZoom").toFloat()
            OverlayColor = DevUtilsPlugin.data!!.getInt("overlayColor")
        }
    }

    override fun onNewGameAfterEconomyLoad()
    {
        Global.getSector().addScript(DevUtilsIntelManager())
        Global.getSector().addScript(DevUtilsOverlay())
    }

    override fun onGameLoad(newGame: Boolean)
    {
        Global.getSector().addTransientScript(DevHotkeys())
    }

    override fun onApplicationLoad()
    {
        loadDataFromJson()
    }
}