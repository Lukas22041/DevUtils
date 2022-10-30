package data.intel.sector

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color


class SectorMemoryIntel() : BaseIntelPlugin()
{

    var remove = false

    override fun reportPlayerClickedOn() {
        super.reportPlayerClickedOn()
    }

    override fun reportRemovedIntel() {
        super.reportRemovedIntel()
        Global.getSector().removeScript(this)
    }

    override fun shouldRemoveIntel(): Boolean {
        return remove
    }

    override fun getSmallDescriptionTitle(): String {
        return "Global Memory"
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        val c = Misc.getHighlightColor()
        info.addPara(smallDescriptionTitle, c, 1f)
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val tc = Misc.getTextColor()
        val UIColor: Color = Global.getSector().playerFaction.baseUIColor
        val UIColorDark: Color = Global.getSector().playerFaction.darkUIColor
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()

        var playerfleet = Global.getSector().playerFleet
        var sector = Global.getSector()

        getMemory(info)
    }

    fun getMemory(info: TooltipMakerAPI)
    {
        val keys = Global.getSector().memory.keys
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()

        for (key in keys)
        {
            var text = "$key: "
            val value: Any? =  Global.getSector().memoryWithoutUpdate.get(key)

            if (value is Boolean || value is String || value is Float || value is Int || value is Long )
            {
                text += value.toString()
            }
            else if (value != null)
            {
                text += value.javaClass.simpleName + "@" + value.hashCode()
            }
            else
            {
                text += "null"
            }

            info.addPara("$text", 3f, mainColor, highlightColor, "$key")
        }
    }

    override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean {
        return false
    }

    override fun buttonPressConfirmed(buttonId: Any, ui: IntelUIAPI) {

    }

    override fun getIcon(): String? {
        return Global.getSettings().getSpriteName("intel", "important")
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        val tags = super.getIntelTags(map)
        tags.add("DevUtils - Sector")
        return tags
    }

    override fun getCommMessageSound(): String? {
        return super.getCommMessageSound()
    }

}