import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.Misc
import data.DevHotkeys
import data.DevUtilsIntelManager
import data.intel.DevUtilsOverlay
import data.intel.DevUtilsSettingsIntel
import data.intel.sector.SectorCargoIntel
import data.intel.sector.SectorEntitiesIntel
import data.intel.sector.SectorScriptsIntel
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

  /*  //Hotfix for game crashing due to it attempting to load a Textfield without a Tooltip attached
    override fun beforeGameSave() {
        SectorScriptsIntel.idField = null
        SectorCargoIntel.idField = null
        SectorEntitiesIntel.idField = null
        DevUtilsSettingsIntel.amountOfMaxEntries = null
    }*/

    override fun onNewGameAfterEconomyLoad()
    {
        Global.getSector().addScript(DevUtilsIntelManager())
        Global.getSector().addScript(DevUtilsOverlay())
    }

    override fun onGameLoad(newGame: Boolean)
    {
        // Fix for the game crashing on load due to some UI elements being loaded without their Tooltip reference existing
        SectorScriptsIntel.idField = null
        SectorCargoIntel.idField = null
        SectorEntitiesIntel.idField = null
        DevUtilsSettingsIntel.amountOfMaxEntries = null
        DevUtilsSettingsIntel.devModeIntelButton = null

        Global.getSector().memoryWithoutUpdate.set("\$DevUtils_Hud", null)
        loadDataFromJson()
        Global.getSector().addTransientScript(DevHotkeys())
    }

    override fun onApplicationLoad()
    {
    }
}