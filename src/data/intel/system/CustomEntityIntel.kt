package data.intel.system

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import java.awt.Color

class CustomEntityIntel(entity: SectorEntityToken) : BaseIntelPlugin()
{

    var remove = false
    var entity: SectorEntityToken

    init {
        this.entity = entity
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
        return "${entity.fullName}"
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

        info.addButton("Teleport to Entity", "TELEPORT", UIColor, UIColorDark, width, 20f, 10f * 2f);

        var text = "Pin Entity Plugin to HUD"
        if (Global.getSector().memoryWithoutUpdate.get("\$DevUtils_Hud") == entity.customPlugin) text = "Unpin Entity Plugin from HUD"
        if (entity.customPlugin == null || entity.javaClass.declaredFields.isEmpty()) text = "No Plugin/No Classwide Variables"

        var button = info.addButton(text, "PIN", UIColor, UIColorDark, width, 20f, 10f);
        if (entity.customPlugin == null || entity.javaClass.declaredFields.isEmpty()) button.isEnabled = false

        info.addSpacer(20f)

        info.addSectionHeading("Entity Info", Alignment.MID, 3f)

        info.addPara("Entity Name: ${entity.name}", 3f, mainColor, highlightColor, "Entity Name")
        info.addPara("Entity Type: ${entity.customEntitySpec.id}", 3f, mainColor, highlightColor, "Entity Type")

        info.addSpacer(5f)
        info.addPara("Orbit Distance: ${entity.circularOrbitRadius}", 3f, mainColor, highlightColor, "Orbit Distance")
        info.addPara("Orbit Days: ${entity.circularOrbitPeriod}", 3f, mainColor, highlightColor, "Orbit Days")

        info.addSectionHeading("Entity Tags", Alignment.MID, 3f)

        for (tag in entity.tags)
        {
            info.addPara("$tag", 3f, mainColor, highlightColor, "$tag")
        }

        if (entity.market != null)
        {
            info.addSectionHeading("Entity Conditions", Alignment.MID, 3f)
            for (condition in entity.market.conditions)
            {
                info.addPara("${condition.name} [ID: ${condition.id}]", 3f, mainColor, highlightColor, "${condition.name}")
            }
        }

        info.addSectionHeading("Entity Memory", Alignment.MID, 3f)
        getMemory(entity, info)

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
            Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, Global.getSector().playerFleet, JumpPointAPI.JumpDestination(entity, ""), 0f)
        }
        if (buttonId == "PIN")
        {

            if (Global.getSector().memoryWithoutUpdate.get("\$DevUtils_Hud") != entity.customPlugin)
            {
                Global.getSector().memoryWithoutUpdate.set("\$DevUtils_Hud", entity.customPlugin)
            }
            else
            {
                Global.getSector().memoryWithoutUpdate.set("\$DevUtils_Hud",null)
            }
        }
        ui.updateUIForItem(this)
    }

    override fun getIcon(): String? {
        return entity.customEntitySpec.iconName
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        val tags = super.getIntelTags(map)
        tags.add("DevUtils - System (Entities)")
        return tags
    }

    override fun getCommMessageSound(): String? {
        return super.getCommMessageSound()
    }

}