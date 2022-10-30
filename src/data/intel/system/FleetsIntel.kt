package data.intel.system

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType


class FleetsIntel(fleet: CampaignFleetAPI) : BaseIntelPlugin()
{

    var remove = false
    var fleet: CampaignFleetAPI
    var displayingScripts: Any? = null

    init {
        this.fleet = fleet
    }

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
        return "${fleet.fullName}"
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        val c = Misc.getHighlightColor()
        info.addPara(smallDescriptionTitle, 1f)
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val tc = Misc.getTextColor()
        val UIColor: Color = Global.getSector().playerFaction.baseUIColor
        val UIColorDark: Color = Global.getSector().playerFaction.darkUIColor
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()

        var playerfleet = Global.getSector().playerFleet

        if (playerfleet.isInHyperspace) return

        info.addButton("Teleport to Fleet", "TELEPORT", UIColor, UIColorDark, width, 20f, 10f * 2f);
        info.addButton("Add Ships to Playerfleet", "ADD", UIColor, UIColorDark, width, 20f, 10f * 2f);
        info.addSpacer(20f)


        info.addSectionHeading("Fleet Info", Alignment.MID, 3f)

        info.addPara("Fleet Name: ${fleet.name}", 3f, mainColor, highlightColor, "Fleet Name")
        var label = info.addPara("Fleet Faction: ${fleet.faction.displayName}", 3f, mainColor, highlightColor)

        label.setHighlight("Fleet Faction", "${fleet.faction.displayName}")
        label.setHighlightColors(Misc.getHighlightColor() , fleet.faction.baseUIColor)

        info.addSectionHeading("Fleet Ships", Alignment.MID, 3f)
        info.showShips(fleet.fleetData.membersListCopy, 100, true, 10f)

        info.addSectionHeading("Fleet Tags", Alignment.MID, 3f)
        for (tag in fleet.tags)
        {
            info.addPara("$tag", 3f, mainColor, highlightColor, "$tag")
        }

        info.addSectionHeading("Fleet Scripts", Alignment.MID, 3f)
        for (script in fleet.scripts)
        {
            info.addPara("${script.javaClass.simpleName} @${script.hashCode()}", 3f, mainColor, highlightColor, "${script.javaClass.simpleName}")
            var text = "Pin Entity Plugin to HUD"
            if (Global.getSector().memoryWithoutUpdate.get("\$DevUtils_Hud") == script) text = "Unpin Entity Plugin from HUD"
            if (script.javaClass.declaredFields.isEmpty()) text = "No Classwide Variables"

            var button = info.addButton(text, script, UIColor, UIColorDark, width, 20f, 10f);
            if (script.javaClass.declaredFields.isEmpty()) button.isEnabled = false
        }

        info.addSectionHeading("Fleet Memory", Alignment.MID, 3f)
        getMemory(fleet, info)
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
            Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, Global.getSector().playerFleet, JumpPointAPI.JumpDestination(fleet, ""), 0f)
        }
        else if (buttonId == "ADD")
        {
            var fleetMembers = fleet.fleetData.membersListCopy
            var playerfleet = Global.getSector().playerFleet

            for (member in fleetMembers)
            {
                playerfleet.fleetData.addFleetMember(member)
            }
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
        if (fleet.flagship == null) return ""
        return fleet.flagship.hullSpec.spriteName
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        val tags = super.getIntelTags(map)
        tags.add("DevUtils - System (Fleets)")
        return tags
    }

    override fun getCommMessageSound(): String? {
        return super.getCommMessageSound()
    }
}