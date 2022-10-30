package data.intel.system

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import data.PrintData
import java.awt.Color
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType


class SystemIntel : BaseIntelPlugin()
{

    var remove = false
    var displayingScripts: Any? = null

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
        if (Global.getSector().playerFleet.isInHyperspace) return ""
        return "System/Star: ${Global.getSector().playerFleet.starSystem.name}"
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        val c = Misc.getHighlightColor()
        info.addPara(smallDescriptionTitle, 1f, c, "System/Star")
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val tc = Misc.getTextColor()
        val UIColor: Color = Global.getSector().playerFaction.baseUIColor
        val UIColorDark: Color = Global.getSector().playerFaction.darkUIColor
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()

        var playerfleet = Global.getSector().playerFleet
        if (playerfleet.isInHyperspace) return

        var system = playerfleet.starSystem
        var star = system.star

        if (star != null)
        {
            info.addButton("Teleport to Star", "TELEPORT", UIColor, UIColorDark, width, 20f, 10f * 2f);
            info.addSpacer(20f)
        }

        info.addSectionHeading("System Tags", Alignment.MID, 3f)

        for (tag in system.tags)
        {
            info.addPara("$tag", 3f, mainColor, highlightColor, "$tag")
        }

        info.addSectionHeading("System Scripts", Alignment.MID, 3f)
        for (script in system.scripts)
        {
            info.addPara("${script.javaClass.simpleName} @${script.hashCode()}", 3f, mainColor, highlightColor, "${script.javaClass.simpleName}")

            var text = "Pin Entity Plugin to HUD"
            if (Global.getSector().memoryWithoutUpdate.get("\$DevUtils_Hud") == script) text = "Unpin Entity Plugin from HUD"
            if (script.javaClass.declaredFields.isEmpty()) text = "No Classwide Variables"

            var button = info.addButton(text, script, UIColor, UIColorDark, width, 20f, 10f);
            if (script.javaClass.declaredFields.isEmpty()) button.isEnabled = false

        }
        if (star != null)
        {

            info.addSectionHeading("Star Info", Alignment.MID, 3f)

            info.addPara("Star Name: ${star.name}", 3f, mainColor, highlightColor, "Star Name")
            info.addPara("Star Type: ${star.typeId}", 3f, mainColor, highlightColor, "Star Type")

            info.addSectionHeading("Star Tags", Alignment.MID, 3f)

            for (tag in star.tags)
            {
                info.addPara("$tag", 3f, mainColor, highlightColor, "$tag")
            }



            info.addSectionHeading("Star Memory", Alignment.MID, 3f)
            getMemory(star, info)
        }

    }

    fun getMemory(target: SectorEntityToken, info: TooltipMakerAPI)
    {
        val keys = target.memory.keys
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()

        for (key in keys)
        {
            var text = "$key: "
            val value: Any? = target.memoryWithoutUpdate.get(key)


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

        if (buttonId == "TELEPORT")
        {
            Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, Global.getSector().playerFleet, JumpPointAPI.JumpDestination(Global.getSector().playerFleet.starSystem.star, ""), 0f)
        }
        else
        {
            if (Global.getSector().memoryWithoutUpdate.get("\$DevUtils_Hud") != buttonId)
            {
                Global.getSector().memoryWithoutUpdate.set("\$DevUtils_Hud", buttonId)
                ui.updateUIForItem(this)
            }
            else
            {
                Global.getSector().memoryWithoutUpdate.set("\$DevUtils_Hud",null)
                ui.updateUIForItem(this)
            }
        }
    }

    override fun getIcon(): String? {
        var playerfleet = Global.getSector().playerFleet
        if (playerfleet.starSystem == null) return Global.getSettings().getSpriteName("intel", "red_planet")

        if (playerfleet.starSystem.center.isStar)
        {
            return playerfleet.starSystem.star.spec.texture
        }
        return Global.getSettings().getSpriteName("intel", "red_planet")
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        val tags = super.getIntelTags(map)
        tags.add("DevUtils - System (Planets)")
        return tags
    }

    override fun getCommMessageSound(): String? {
        return super.getCommMessageSound()
    }
}