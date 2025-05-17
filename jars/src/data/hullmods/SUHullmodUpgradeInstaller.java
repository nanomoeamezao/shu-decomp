package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class SUHullmodUpgradeInstaller {
   public static final int NOT_PLAYER = 0;
   public static final int IS_PLAYER = 1;
   public static final int HAS_HULLMOD = 2;
   public static final int BIOFACTORY_EMBRYO = 0;
   public static final int CATALYTIC_CORE = 1;
   public static final int COMBATDRONE_REPLICATOR = 2;
   public static final int CORRUPTED_NANOFORGE = 3;
   public static final int CRYOARITHMETHIC_ENGINE = 4;
   public static final int DEALMAKER_HOLOSUITE = 5;
   public static final int FULLERENE_SPOOL = 6;
   public static final int FUSION_LAMP = 7;
   public static final int HYPERSHUNT_TAP = 8;
   public static final int MANTLE_BORE = 9;
   public static final int PLASMA_DYNAMO = 10;
   public static final int PRISTINE_NANOFORGE = 11;
   public static final int SOIL_NANITES = 12;
   public static final int SYNCHROTON_CORE = 13;
   public static final int ALPHA_CORE = 14;
   public static final int BETA_CORE = 15;
   public static final int GAMMA_CORE = 16;
   private static final float REFUND_AMT = 1.0F;
   private static final String[] PREFIXES = new String[]{
      "biofactory_embryo_shu_check_",
      "catalytic_core_shu_check_",
      "dronereplicator_shu_check_",
      "corrupted_nanoforge_shu_check_",
      "cryoarithmetic_engine_shu_check_",
      "dealmaker_holosuite_shu_check_",
      "fullerene_spool_shu_check_",
      "fusionlamp_reactor_shu_check_",
      "hypershunt_shu_check_",
      "mantle_bore_shu_check_",
      "plasma_dynamo_shu_check_",
      "pristine_nanoforge_shu_check_",
      "soil_nanites_shu_check_",
      "synchroton_shu_check_",
      "alpha_core_shu_check_",
      "beta_core_shu_check_",
      "gamma_core_shu_check_"
   };
   private static final String[][] HULLMODS = new String[][]{
      {"specialsphmod_biofactoryembryo_upgrades"},
      {"specialsphmod_catalyticcore_upgrades"},
      {"specialsphmod_combatdronereplicator_upgrades"},
      {"specialsphmod_corruptednanoforge_upgrades"},
      {"specialsphmod_cryoarithmeticengine_upgrades"},
      {"specialsphmod_dealmakerholosuite_upgrades"},
      {"specialsphmod_fullerenespool_upgrades"},
      {"specialsphmod_fusionlampreactor_upgrades"},
      {"specialsphmod_hypershunt_upgrades"},
      {"specialsphmod_mantlebore_upgrades"},
      {"specialsphmod_plasmadynamo_upgrades"},
      {"specialsphmod_pristinenanoforge_upgrades"},
      {"specialsphmod_soilnanites_upgrades"},
      {"specialsphmod_synchrotoncore_upgrades"},
      {"specialsphmod_alpha_core_upgrades"},
      {"specialsphmod_beta_core_upgrades"},
      {"specialsphmod_gamma_core_upgrades"}
   };
   public static String copiumDosage = "Friendly reminder that modding Starsector is a mistake.";

   public static String[] getMods(int index) {
      return HULLMODS[index];
   }

   public static int isPlayerShip(ShipAPI ship, String id) {
      CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
      if (playerFleet == null) {
         return 0;
      } else {
         List<FleetMemberAPI> playerShips = playerFleet.getFleetData().getMembersListCopy();
         String memberId = ship.getFleetMemberId();
         boolean isPlayerShip = false;
         boolean hasCurrentMod = false;

         for (FleetMemberAPI playerShip : playerShips) {
            if (playerShip.getId().equals(memberId)) {
               isPlayerShip = true;
               if (playerShip.getVariant() != null && playerShip.getVariant().getHullMods().contains(id)) {
                  hasCurrentMod = true;
               }
            }
         }

         if (isPlayerShip && hasCurrentMod) {
            return 2;
         } else {
            return isPlayerShip ? 1 : 0;
         }
      }
   }

   public static void applyHullmodToModulesOfShip(ShipVariantAPI shipVariant, MutableCharacterStatsAPI stats, String id) {
      if (stats != null) {
         List<String> modules = shipVariant.getModuleSlots();
         if (!modules.isEmpty()) {
            for (String moduleID : modules) {
               ShipVariantAPI moduleVariant = shipVariant.getModuleVariant(moduleID);
               if (!moduleVariant.hasHullMod(id)) {
                  moduleVariant.addPermaMod(id);
               }
            }
         }
      }
   }

   public static void addPlayerCommodityItem(String id) {
      float amount = 1.0F;
      CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
      if (playerFleet != null) {
         CargoAPI playerFleetCargo = playerFleet.getCargo();
         playerFleetCargo.addCommodity(id, 1.0F);
      }
   }

   public static void addPlayerSpecialItem(String id) {
      float amount = 1.0F;
      CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
      if (playerFleet != null) {
         CargoAPI playerFleetCargo = playerFleet.getCargo();
         playerFleetCargo.addSpecial(new SpecialItemData(id, (String)null), 1.0F);
      }
   }

   public static void removeHullmodToModulesOfShip(ShipVariantAPI shipVariant, MutableCharacterStatsAPI stats, String id) {
      if (stats != null) {
         List<String> shipModules = shipVariant.getModuleSlots();
         if (!shipModules.isEmpty()) {
            for (String moduleID : shipModules) {
               ShipVariantAPI moduleVariant = shipVariant.getModuleVariant(moduleID);
               if (moduleVariant.hasHullMod(id)) {
                  moduleVariant.removePermaMod(id);
               }
            }
         }
      }
   }

   public static void removeHullmod(FleetMemberAPI ship, int type) {
      if (ship.getVariant() != null) {
         Map<String, Object> data = Global.getSector().getPersistentData();
         data.remove(PREFIXES[type] + ship.getId());

         for (String id : HULLMODS[type]) {
            if (ship.getVariant().getHullMods().contains(id)) {
               ship.getVariant().removeMod(id);
            }
         }
      }
   }

   public static void removePlayerSpecialItem(String id) {
      CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
      if (playerFleet != null) {
         for (CargoStackAPI cargoStack : playerFleet.getCargo().getStacksCopy()) {
            if (cargoStack.isSpecialStack() && cargoStack.getSpecialDataIfSpecial().getId().equals(id)) {
               cargoStack.subtract(1.0F);
               if (cargoStack.getSize() <= 0.0F) {
                  playerFleet.getCargo().removeStack(cargoStack);
               }

               return;
            }
         }
      }
   }

   public static void removePlayerCommodity(String id) {
      CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
      if (playerFleet != null) {
         for (CargoStackAPI cargoStack : playerFleet.getCargo().getStacksCopy()) {
            if (cargoStack.isCommodityStack() && cargoStack.getCommodityId().equals(id)) {
               cargoStack.subtract(1.0F);
               if (cargoStack.getSize() <= 0.0F) {
                  playerFleet.getCargo().removeStack(cargoStack);
               }

               return;
            }
         }
      }
   }

   public static boolean playerHasSpecialItem(String id) {
      CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
      if (playerFleet == null) {
         return false;
      } else {
         for (CargoStackAPI cargoStack : playerFleet.getCargo().getStacksCopy()) {
            if (cargoStack.isSpecialStack() && cargoStack.getSpecialDataIfSpecial().getId().equals(id) && cargoStack.getSize() > 0.0F) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean playerHasCommodity(String id) {
      CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
      if (playerFleet == null) {
         return false;
      } else {
         for (CargoStackAPI cargoStack : playerFleet.getCargo().getStacksCopy()) {
            if (cargoStack.isCommodityStack() && cargoStack.getCommodityId().equals(id) && cargoStack.getSize() > 0.0F) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean listContainsAny(Collection list, Object... objects) {
      if (objects == null) {
         return false;
      } else {
         for (Object object : objects) {
            if (list.contains(object)) {
               return true;
            }
         }

         return false;
      }
   }
}
