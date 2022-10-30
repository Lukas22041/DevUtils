package data.intel.sector

import DevUtilsPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TextFieldAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import data.scripts.util.MagicSettings
import java.awt.Color
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.logging.Filter

class SectorEntitiesIntel() : BaseIntelPlugin()
{

    var remove = false
    companion object
    {
        var idField: TextFieldAPI? = null
    }

    override fun reportPlayerClickedOn() {
        super.reportPlayerClickedOn()
    }

    override fun reportRemovedIntel() {
        super.reportRemovedIntel()
    }

    override fun shouldRemoveIntel(): Boolean {
        return remove
    }

    override fun getSmallDescriptionTitle(): String {
        return "Sector Entities"
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        val c = Misc.getHighlightColor()
        info.addPara(smallDescriptionTitle, c, 1f)
    }

    override fun advance(amount: Float) {
        super.advance(amount)
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val tc = Misc.getTextColor()
        val UIColor: Color = Global.getSector().playerFaction.baseUIColor
        val UIColorDark: Color = Global.getSector().playerFaction.darkUIColor
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()

        var playerfleet = Global.getSector().playerFleet
        var sector = Global.getSector()

        var systems = Global.getSector().starSystems
        var planets: MutableList<PlanetAPI> = ArrayList()
        var entities: MutableList<SectorEntityToken> = ArrayList()
        var fleets: MutableList<CampaignFleetAPI> = ArrayList()
        var validsystems: MutableList<StarSystemAPI> = ArrayList()

        info.addPara("List of all Planets and Custom Entities in the Sector. " +
                "Shows any entity that matches with atleast one Filter.\n\n-Seperated by Comma.\n-Spaces are ignored.\n-Upper/Lowercase is ignored.", 10f)

        var idFieldText = ""
        var tagsFieldText = ""

        if (idField != null) idFieldText = idField!!.text

        info.addSectionHeading("Filter by Name/ID/Faction/Tags", Alignment.MID ,10f)
        idField = info.addTextField(width, 3f)
        idField!!.text = idFieldText


        info.addButton("Filter", "FILTER", UIColor, UIColorDark, width, 20f, 10f);
        info.addSpacer(5f)

        var entityCap = 0

        var IDfilter = idField!!.text.lowercase().filter{!it.isWhitespace()}.split(",");

        for (system in systems)
        {

            for (planet in system.planets)
            {
                if (entityCap >= DevUtilsPlugin.maxEntries) break
                var addToList = false

                if (idField!!.text != "")
                {
                    for (filter in IDfilter)
                    {
                        if (planet.id.lowercase().contains(filter)) addToList = true
                        if (planet.name.lowercase().contains(filter)) addToList = true
                        if (planet.faction.id.lowercase().contains(filter)) addToList = true
                        if (planet.faction.displayName.lowercase().contains(filter)) addToList = true

                        for (tag in planet.tags)
                        {
                            if (tag.lowercase().contains(filter)) addToList = true
                        }
                    }
                }

                if (addToList == true)
                {
                    planets.add(planet)
                    entityCap++
                }
            }

            for (entity in system.customEntities)
            {
                if (entity.customEntitySpec.id == "orbital_junk") continue
                if (entityCap >= DevUtilsPlugin.maxEntries) break
                var addToList = false

                if (idField!!.text != "")
                {
                    for (filter in IDfilter)
                    {
                        if (entity.id.lowercase().contains(filter)) addToList = true
                        if (entity.name.lowercase().contains(filter)) addToList = true
                        if (entity.faction.id.lowercase().contains(filter)) addToList = true
                        if (entity.faction.displayName.lowercase().contains(filter)) addToList = true

                        for (tag in entity.tags)
                        {
                            if (tag.lowercase().contains(filter)) addToList = true
                        }
                    }
                }

                if (addToList == true)
                {
                    entities.add(entity)
                    entityCap++
                }
            }

            for (fleet in system.fleets)
            {
                if (entityCap >= DevUtilsPlugin.maxEntries) break
                var addToList = false

                if (idField!!.text != "")
                {
                    for (filter in IDfilter)
                    {
                        if (fleet.id.lowercase().contains(filter)) addToList = true
                        if (fleet.name.lowercase().contains(filter)) addToList = true
                        if (fleet.faction.id.lowercase().contains(filter)) addToList = true
                        if (fleet.faction.displayName.lowercase().contains(filter)) addToList = true

                        for (tag in fleet.tags)
                        {
                            if (tag.lowercase().contains(filter)) addToList = true
                        }
                    }
                }

                if (addToList == true)
                {
                    fleets.add(fleet)
                    entityCap++
                }
            }
        }

        for (planet in planets)
        {
            var type = ""
            var speType = ""
            if (planet.isStar) {type = "Star"; speType = planet.typeId }
            else  {type = "Planet"; speType = planet.typeId }

            info.addSectionHeading("${planet.name} $type", Alignment.MID, 10f)
            var image = info.beginImageWithText(planet.spec.texture, 48f)

            image.addPara("Name: ${planet.name}", 10f, Misc.getHighlightColor(), "Name:")
            image.addPara("$type | Type: $speType", 10f, Misc.getHighlightColor(), type, "Type: ")
            info.addImageWithText(10f)
            info.addButton("Teleport to", planet, UIColor, UIColorDark, width, 20f, 10f);
        }

        for (entity in entities)
        {
            info.addSectionHeading("${entity.name} Entity", Alignment.MID, 10f)
            var image = info.beginImageWithText(entity.customEntitySpec.iconName, 32f)

            image.addPara("Name: ${entity.name}", 10f, Misc.getHighlightColor(), "Name:")
            image.addPara("Type: ${entity.customEntitySpec.id}", 10f, Misc.getHighlightColor(), "Type: ")
            info.addImageWithText(10f)
            info.addButton("Teleport to", entity, UIColor, UIColorDark, width, 20f, 10f);
        }

        for (fleet in fleets)
        {
            info.addSectionHeading("${fleet.name} Fleet", Alignment.MID, 10f)
            var image = info.beginImageWithText(fleet.flagship.variant.hullSpec.spriteName, 32f)

            image.addPara("Name: ${fleet.name}", 10f, Misc.getHighlightColor(), "Name:")
            image.addPara("Faction: ${fleet.faction.displayName}", 10f, fleet.faction.baseUIColor, "Faction:")
            info.addImageWithText(10f)
            info.addButton("Teleport to", fleet, UIColor, UIColorDark, width, 20f, 10f);
        }
    }

    override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean {
        return false
    }

    override fun buttonPressConfirmed(buttonId: Any, ui: IntelUIAPI)
    {
        if (buttonId is String)
        {
            if (buttonId.toString() == "FILTER")
            {
                ui.recreateIntelUI()
            }
        }
        else if (buttonId is SectorEntityToken)
        {
            Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, Global.getSector().playerFleet, JumpPointAPI.JumpDestination(buttonId, ""), 0f)
        }
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