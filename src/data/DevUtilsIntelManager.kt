package data

import DevUtilsPlugin
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.util.IntervalUtil
import data.intel.*
import data.intel.sector.*
import data.intel.system.*
import data.scripts.util.MagicSettings

class DevUtilsIntelManager : EveryFrameScript
{
    var systemName: String = ""

    var settingsIntel: DevUtilsSettingsIntel? = null
    var cheatIntel: CheatIntel? = null
    var sectorMemoryIntel: SectorMemoryIntel? = null
    var sectorEntityIntel: SectorEntitiesIntel? = null
    var sectorScriptsIntel: SectorScriptsIntel? = null
    var sectorCargoIntel: SectorCargoIntel? = null

    var factionsIntel: MutableList<FactionIntel> = ArrayList()

    var systemIntel: SystemIntel? = null
    var planetIntel: MutableMap<PlanetAPI, PlanetIntel>? = HashMap()
    var customEntityIntel: MutableMap<SectorEntityToken, CustomEntityIntel>? = HashMap()
    var fleetsIntel: MutableMap<CampaignFleetAPI, FleetsIntel>? = HashMap()

    var interval = IntervalUtil(0.2f, 0.2f)

    override fun advance(amount: Float)
    {
        val playerfleet = Global.getSector().playerFleet
        interval.advance(amount)

        if (DevUtilsPlugin.devModeOnlyIntel)
        {
            if (!Global.getSettings().isDevMode)
            {
                cleanUpIntel()
                disableIntel()
                return
            }
            else if (sectorScriptsIntel == null)
            {
                enableIntel()
            }
        }
        else
        {
            enableIntel()
        }

        if (playerfleet.isInHyperspace)
        {
            cleanUpIntel()
            systemName = ""
            return
        }


        if (interval.intervalElapsed())
        {
            //Updates the Intel for Entities
            var customEntities = playerfleet.starSystem.customEntities
            for (entity in customEntities)
            {
                if (!customEntityIntel!!.contains(entity) && entity.customEntitySpec.id != "orbital_junk")
                {
                    var intel = CustomEntityIntel(entity)
                    Global.getSector().intelManager.addIntel(intel)
                    customEntityIntel!!.put(entity, intel)
                }
            }
            var entitiesToRemove: MutableList<SectorEntityToken> = ArrayList()
            for (entityMap in customEntityIntel!!)
            {
                if (!customEntities.contains(entityMap.key))
                {
                    entityMap.value.remove = true
                    entitiesToRemove.add(entityMap.key)
                }
            }
            if (entitiesToRemove.isNotEmpty()) {
                for (map in entitiesToRemove) {
                    customEntityIntel!!.remove(map)
                }
            }

            //Updates Star/System Intel
            if (systemName != playerfleet.starSystem.name)
            {
                systemName = playerfleet.starSystem.name
                cleanUpIntel()

                var sIntel = SystemIntel()
                Global.getSector().intelManager.addIntel(sIntel)
                systemIntel = sIntel
            }

            //Updates the intel for Planets
            var planets = playerfleet.starSystem.planets
            for (planet in planets)
            {
                if (!planetIntel!!.contains(planet) && !planet.isStar)
                {
                    var intel = PlanetIntel(planet)
                    Global.getSector().intelManager.addIntel(intel)
                    planetIntel!!.put(planet, intel)
                }
            }
            var planetsToRemove: MutableList<SectorEntityToken> = ArrayList()
            for (planetMap in planetIntel!!)
            {
                if (!planets.contains(planetMap.key))
                {
                    planetMap.value.remove = true
                    planetsToRemove.add(planetMap.key)
                }
            }
            if (planetsToRemove.isNotEmpty()) {
                for (map in planetsToRemove) {
                    planetIntel!!.remove(map)
                }
            }

            //Updates the intel for fleets
            var fleets = playerfleet.starSystem.fleets
            for (fleet in fleets)
            {
                if (!fleetsIntel!!.contains(fleet))
                {
                    var intel = FleetsIntel(fleet)
                    Global.getSector().intelManager.addIntel(intel)
                    fleetsIntel!!.put(fleet, intel)
                }
            }
            var fleetsToRemove: MutableList<SectorEntityToken> = ArrayList()
            for (fleetsMap in fleetsIntel!!)
            {
                if (!fleets.contains(fleetsMap.key))
                {
                    fleetsMap.value.remove = true
                    planetsToRemove.add(fleetsMap.key)
                }
            }
            if (fleetsToRemove.isNotEmpty()) {
                for (map in fleetsToRemove) {
                    fleetsIntel!!.remove(map)
                }
            }
        }

    }

    private fun cleanUpIntel()
    {
        if (systemIntel != null)
        {
            systemIntel!!.remove = true
        }
        if (planetIntel != null && !planetIntel!!.isEmpty())
        {
            for (planet in planetIntel!!)
            {
                planet.value.remove = true
            }
            planetIntel!!.clear()
        }
        if (customEntityIntel != null && !customEntityIntel!!.isEmpty())
        {
            for (entity in customEntityIntel!!)
            {
                entity.value.remove = true
            }
            customEntityIntel!!.clear()
        }
        if (fleetsIntel != null && !fleetsIntel!!.isEmpty())
        {
            for (fleet in fleetsIntel!!)
            {
                fleet.value.remove = true
            }
            fleetsIntel!!.clear()
        }
    }

    fun disableIntel()
    {
        if (sectorScriptsIntel == null) return

        sectorCargoIntel!!.remove = true
        sectorScriptsIntel!!.remove = true
        sectorMemoryIntel!!.remove = true
        sectorEntityIntel!!.remove = true
        cheatIntel!!.remove = true
        settingsIntel!!.remove = true

        sectorCargoIntel = null
        sectorScriptsIntel = null
        sectorMemoryIntel = null
        sectorEntityIntel = null
        cheatIntel = null

        for (faction in factionsIntel)
        {
            faction.remove = true
        }
        factionsIntel.clear()
    }

    fun enableIntel()
    {
        if (sectorScriptsIntel != null) return
        sectorMemoryIntel = SectorMemoryIntel()
        sectorCargoIntel = SectorCargoIntel()
        sectorScriptsIntel = SectorScriptsIntel()
        sectorEntityIntel = SectorEntitiesIntel()
        cheatIntel = CheatIntel()
        settingsIntel = DevUtilsSettingsIntel()

        Global.getSector().intelManager.addIntel(sectorCargoIntel)
        Global.getSector().intelManager.addIntel(sectorEntityIntel)
        Global.getSector().intelManager.addIntel(sectorScriptsIntel)
        Global.getSector().intelManager.addIntel(sectorMemoryIntel)
        Global.getSector().intelManager.addIntel(cheatIntel)
        Global.getSector().intelManager.addIntel(settingsIntel)

        var factions = Global.getSector().allFactions
        for (faction in factions)
        {
            var intel = FactionIntel(faction)
            Global.getSector().intelManager.addIntel(intel)
            factionsIntel.add(intel)
        }

        if (Global.getSector().playerFleet.isInHyperspace) return
        if (!Global.getSector().playerFleet.starSystem.center.isStar) return

        var sIntel = SystemIntel()
        Global.getSector().intelManager.addIntel(sIntel)
        systemIntel = sIntel
    }

    override fun isDone(): Boolean {
        return  false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

}