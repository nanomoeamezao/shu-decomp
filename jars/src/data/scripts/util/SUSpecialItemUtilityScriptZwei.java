package data.scripts.util;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import data.hullmods.SUHullmodUpgradeInstaller;

public class SUSpecialItemUtilityScriptZwei extends BaseHullMod {
   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      ShipVariantAPI shipVariant = ship.getVariant();
      boolean isCaptainNull = ship.getCaptain() == null;
      MutableCharacterStatsAPI currentShipStats = isCaptainNull ? null : ship.getCaptain().getStats();
      if (ship.getVariant().hasHullMod("specialsphmod_biofactoryembryo_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_biofactoryembryo_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_biofactoryembryo_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_biofactoryembryo_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_catalyticcore_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_catalyticcore_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_catalyticcore_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_catalyticcore_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_combatdronereplicator_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_combatdronereplicator_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_combatdronereplicator_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_combatdronereplicator_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_combatdronereplicator_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_cryoarithmeticengine_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_cryoarithmeticengine_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_cryoarithmeticengine_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_cryoarithmeticengine_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_cryoarithmeticengine_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_dealmakerholosuite_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_dealmakerholosuite_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_dealmakerholosuite_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_dealmakerholosuite_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_fullerenespool_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_fullerenespool_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_fullerenespool_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_fullerenespool_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_fusionlampreactor_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_fusionlampreactor_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_fusionlampreactor_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_fusionlampreactor_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_fusionlampreactor_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_hypershunt_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_hypershunt_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_hypershunt_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_hypershunt_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_hypershunt_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_mantlebore_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_mantlebore_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_mantlebore_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_mantlebore_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_mantlebore_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_corruptednanoforge_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_corruptednanoforge_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_nanoforge_corrupted_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_corruptednanoforge_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_corruptednanoforge_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_pristinenanoforge_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_pristinenanoforge_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_nanoforge_pristine_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_pristinenanoforge_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_pristinenanoforge_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_plasmadynamo_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_plasmadynamo_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_plasmadynamo_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_plasmadynamo_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_plasmadynamo_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_soilnanites_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_soilnanites_upgrades")) {
         SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_soilnanites_extension");
         ship.getVariant().getHullMods().remove("specialsphmod_soilnanites_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_soilnanites_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_synchrotoncore_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_synchrotoncore_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_synchrotoncore_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_synchrotoncore_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_sfc_aquaticstimulator_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_sfc_aquaticstimulator_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_sfc_aquaticstimulator_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_sfc_aquaticstimulator_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_sfc_phasemote_utilityscript") && !ship.getVariant().hasHullMod("specialsphmod_sfc_phasemote_upgrades")) {
         if (ship.getHullSpec().getHullId().contains("ziggurat") && ship.getVariant().hasHullMod("high_frequency_attractor")) {
            ship.getVariant().getHullMods().remove("high_frequency_attractor");
            ship.getVariant().getPermaMods().remove("high_frequency_attractor");
         }

         ship.getVariant().getHullMods().remove("specialsphmod_sfc_phasemote_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_sfc_phasemote_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_uaf_dimensionalnanoforge_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_uaf_dimensionalnanoforge_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_uaf_dimensionalnanoforge_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_uaf_dimensionalnanoforge_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_uaf_dimensionalstove_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_uaf_dimensionalstove_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_uaf_dimensionalstove_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_uaf_dimensionalstove_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_uaf_garrisontransmitter_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_uaf_garrisontransmitter_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_uaf_garrisontransmitter_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_uaf_garrisontransmitter_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_uaf_interplanetary_ricecooker_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_uaf_interplanetary_ricecooker_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_uaf_interplanetary_ricecooker_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_uaf_interplanetary_ricecooker_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_uaf_modularpurifier_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_uaf_modularpurifier_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_uaf_modularpurifier_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_uaf_modularpurifier_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_uaf_interplanetaryaccessrouter_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_uaf_interplanetaryaccessrouter_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_uaf_interplanetaryaccessrouter_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_uaf_interplanetaryaccessrouter_utilityscript");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_uaf_servosyncpump_utilityscript")
         && !ship.getVariant().hasHullMod("specialsphmod_uaf_servosyncpump_upgrades")) {
         ship.getVariant().getHullMods().remove("specialsphmod_uaf_servosyncpump_utilityscript");
         ship.getVariant().removePermaMod("specialsphmod_uaf_servosyncpump_utilityscript");
      }
   }
}
