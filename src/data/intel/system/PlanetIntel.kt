package data.intel.system

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color


class PlanetIntel(planet: PlanetAPI) : BaseIntelPlugin()
{

    var remove = false
    var planet: PlanetAPI

    init {
        this.planet = planet
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
        return "Planet: ${planet.fullName}"
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        val c = Misc.getHighlightColor()
        info.addPara(smallDescriptionTitle, 1f, c, "Planet")
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val tc = Misc.getTextColor()
        val UIColor: Color = Global.getSector().playerFaction.baseUIColor
        val UIColorDark: Color = Global.getSector().playerFaction.darkUIColor
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()

        var playerfleet = Global.getSector().playerFleet

        if (playerfleet.isInHyperspace) return

        info.addButton("Teleport to Planet", "TELEPORT", UIColor, UIColorDark, width, 20f, 10f * 2f);
        info.addSpacer(20f)


        info.addSectionHeading("Planet Info", Alignment.MID, 3f)

        info.addPara("Planet Name: ${planet.name}", 3f, mainColor, highlightColor, "Planet Name")
        info.addPara("Planet Type: ${planet.typeId}", 3f, mainColor, highlightColor, "Planet Type")
        info.addSpacer(5f)
        info.addPara("Market Name: ${planet.market.name}", 3f, mainColor, highlightColor, "Market Name")
        info.addSpacer(5f)
        info.addPara("Orbit Distance: ${planet.circularOrbitRadius}", 3f, mainColor, highlightColor, "Orbit Distance")
        info.addPara("Orbit Days: ${planet.circularOrbitPeriod}", 3f, mainColor, highlightColor, "Orbit Days")

        info.addSectionHeading("Planet Tags", Alignment.MID, 3f)

        for (tag in planet.tags)
        {
            info.addPara("$tag", 3f, mainColor, highlightColor, "$tag")
        }

        info.addSectionHeading("Planet Conditions", Alignment.MID, 3f)

        for (condition in planet.market.conditions)
        {
            info.addPara("${condition.name} [ID: ${condition.id}]", 3f, mainColor, highlightColor, "${condition.name}")
        }

        info.addSectionHeading("Planet Memory", Alignment.MID, 3f)
        getMemory(planet, info)
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
            Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, Global.getSector().playerFleet, JumpPointAPI.JumpDestination(planet, ""), 0f)
        }

        ui.updateUIForItem(this)
    }

    override fun getIcon(): String? {
        return planet.spec.texture
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