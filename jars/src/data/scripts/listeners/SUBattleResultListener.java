package data.scripts.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.hullmods.SUHullmodUpgradeInstaller;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;

public class SUBattleResultListener extends BaseCampaignEventListener {
   public SUBattleResultListener(boolean register) {
      super(register);
   }

   public void reportPlayerEngagement(EngagementResultAPI result) {
      boolean disableDestruction = SUPlugin.DISABLE_ITEMDESTRUCTION;
      if (SUPlugin.HASLUNALIB) {
         disableDestruction = LunaSettings.getBoolean("mayu_specialupgrades", "shu_itemdestructionToggle");
      }

      if (!disableDestruction) {
         EngagementResultForFleetAPI playerResult = result.didPlayerWin() ? result.getWinnerResult() : result.getLoserResult();

         for (FleetMemberAPI ship : playerResult.getDestroyed()) {
            processShip(ship);
         }

         for (FleetMemberAPI ship : playerResult.getDisabled()) {
            processShip(ship);
         }
      }
   }

   private static void processShip(FleetMemberAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         Map<String, Object> data = Global.getSector().getPersistentData();
         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(0))) {
            data.remove("biofactory_embryo_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 0);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(1))) {
            data.remove("catalytic_core_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 1);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(2))) {
            data.remove("dronereplicator_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 2);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(3))) {
            data.remove("corrupted_nanoforge_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 3);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(4))) {
            data.remove("cryoarithmetic_engine_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 4);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(5))) {
            data.remove("dealmaker_holosuite_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 5);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(6))) {
            data.remove("fullerene_spool_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 6);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(7))) {
            data.remove("fusionlamp_reactor_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 7);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(8))) {
            data.remove("hypershunt_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 8);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(9))) {
            data.remove("mantle_bore_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 9);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(10))) {
            data.remove("plasma_dynamo_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 10);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(11))) {
            data.remove("pristine_nanoforge_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 11);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(12))) {
            data.remove("soil_nanites_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 12);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(13))) {
            data.remove("synchroton_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 13);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(14))) {
            data.remove("alpha_core_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 14);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(15))) {
            data.remove("beta_core_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 15);
         }

         if (SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), SUHullmodUpgradeInstaller.getMods(16))) {
            data.remove("gamma_core_shu_check_" + ship.getId());
            SUHullmodUpgradeInstaller.removeHullmod(ship, 16);
         }

         if (ship.getVariant().hasHullMod("specialsphmod_sfc_aquaticstimulator_upgrades")) {
            data.remove("aquaticstim_shu_sfc_check_" + ship.getId());
            ship.getVariant().removeMod("specialsphmod_sfc_aquaticstimulator_upgrades");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_sfc_phasemote_upgrades")) {
            data.remove("megacondenser_shu_sfc_check_" + ship.getId());
            ship.getVariant().removeMod("specialsphmod_sfc_phasemote_upgrades");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_dimensionalnanoforge_upgrades")) {
            data.remove("dimensionalnano_forge_shu_uaf_check_" + ship.getId());
            ship.getVariant().removeMod("specialsphmod_uaf_dimensionalnanoforge_upgrades");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_dimensionalstove_upgrades")) {
            data.remove("dimensionalstove_shu_uaf_check_" + ship.getId());
            ship.getVariant().removeMod("specialsphmod_uaf_dimensionalstove_upgrades");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_garrisontransmitter_upgrades")) {
            data.remove("garrison_transmitter_shu_uaf_check_" + ship.getId());
            ship.getVariant().removeMod("specialsphmod_uaf_garrisontransmitter_upgrades");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_interplanetaryaccessrouter_upgrades")) {
            data.remove("accessrouter_shu_uaf_check_" + ship.getId());
            ship.getVariant().removeMod("specialsphmod_uaf_interplanetaryaccessrouter_upgrades");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_interplanetary_ricecooker_upgrades")) {
            data.remove("ricecooker_interplanetary_shu_uaf_check_" + ship.getId());
            ship.getVariant().removeMod("specialsphmod_uaf_interplanetary_ricecooker_upgrades");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_modularpurifier_upgrades")) {
            data.remove("modular_purifier_shu_check_" + ship.getId());
            ship.getVariant().removeMod("specialsphmod_uaf_modularpurifier_upgrades");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_servosyncpump_upgrades")) {
            data.remove("servosyncpump_shu_check_" + ship.getId());
            ship.getVariant().removeMod("specialsphmod_uaf_servosyncpump_upgrades");
         }

         if (ship.getVariant().hasHullMod(SUStringCodex.AICORE_UTILITY_SCRIPT)) {
            ship.getVariant().removePermaMod(SUStringCodex.AICORE_UTILITY_SCRIPT);
         }

         if (ship.getVariant().hasHullMod(SUStringCodex.SPECIAL_ITEM_UTILITY_SCRIPT)) {
            ship.getVariant().removePermaMod(SUStringCodex.SPECIAL_ITEM_UTILITY_SCRIPT);
         }

         if (ship.getVariant().hasHullMod(SUStringCodex.COLLAB_SPECIAL_ITEM_UTILITY_SCRIPT)) {
            ship.getVariant().removePermaMod(SUStringCodex.COLLAB_SPECIAL_ITEM_UTILITY_SCRIPT);
         }

         if (ship.getVariant().hasHullMod("specialsphmod_alphacore_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_alphacore_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_betacore_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_betacore_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_gammacore_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_gammacore_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_biofactoryembryo_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_biofactoryembryo_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_catalyticcore_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_catalyticcore_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_combatdronereplicator_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_combatdronereplicator_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_cryoarithmeticengine_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_cryoarithmeticengine_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_dealmakerholosuite_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_dealmakerholosuite_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_fullerenespool_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_fullerenespool_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_fusionlampreactor_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_fusionlampreactor_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_hypershunt_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_hypershunt_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_mantlebore_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_mantlebore_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_corruptednanoforge_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_corruptednanoforge_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_pristinenanoforge_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_pristinenanoforge_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_plasmadynamo_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_plasmadynamo_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_soilnanites_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_soilnanites_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_synchrotoncore_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_synchrotoncore_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_sfc_aquaticstimulator_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_sfc_aquaticstimulator_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_sfc_phasemote_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_sfc_phasemote_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_dimensionalnanoforge_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_uaf_dimensionalnanoforge_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_dimensionalstove_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_uaf_dimensionalstove_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_garrisontransmitter_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_uaf_garrisontransmitter_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_interplanetary_ricecooker_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_uaf_interplanetary_ricecooker_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_modularpurifier_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_uaf_modularpurifier_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_interplanetaryaccessrouter_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_uaf_interplanetaryaccessrouter_utilityscript");
         }

         if (ship.getVariant().hasHullMod("specialsphmod_uaf_servosyncpump_utilityscript")) {
            ship.getVariant().removePermaMod("specialsphmod_uaf_servosyncpump_utilityscript");
         }
      }
   }
}
