package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.hullmods.SUHullmodUpgradeInstaller;
import data.scripts.util.id.SUStringCodex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SUSpecialItemUtilityScript extends BaseHullMod {
   private static final float SFX_VAL = 1.0F;
   private final IntervalUtil tracker = new IntervalUtil(0.5F, 0.6F);
   public static final ArrayList<String> DATA_PREFIXES = new ArrayList<>();
   public static final ArrayList<String> COLHULLMODS = new ArrayList<>();
   public static final HashMap<String, String> itemMap = new HashMap<>();

   public void advanceInCampaign(FleetMemberAPI member, float amount) {
      if (member != null) {
         if (member.getFleetData() != null) {
            if (member.getFleetData().getFleet() != null) {
               boolean DRONE_REPLICATORHMOD = member.getVariant().hasHullMod("specialsphmod_combatdronereplicator_upgrades");
               boolean CORRUPTED_NANOFORGEHMOD = member.getVariant().hasHullMod("specialsphmod_corruptednanoforge_upgrades");
               boolean CRYOARITHMETIC_ENGINEHMOD = member.getVariant().hasHullMod("specialsphmod_cryoarithmeticengine_upgrades");
               boolean FUSION_LAMPHMOD = member.getVariant().hasHullMod("specialsphmod_fusionlampreactor_upgrades");
               boolean HYPERSHUNT_TAPHMOD = member.getVariant().hasHullMod("specialsphmod_hypershunt_upgrades");
               boolean MANTLE_BOREHMOD = member.getVariant().hasHullMod("specialsphmod_mantlebore_upgrades");
               boolean PLASMA_DYNAMOHMOD = member.getVariant().hasHullMod("specialsphmod_plasmadynamo_upgrades");
               boolean PRISTINE_NANOFORGEHMOD = member.getVariant().hasHullMod("specialsphmod_pristinenanoforge_upgrades");
               boolean SOIL_NANITEHMOD = member.getVariant().hasHullMod("specialsphmod_soilnanites_upgrades");
               ShipVariantAPI shipVariant = member.getVariant();
               boolean isCaptainNull = member.getCaptain() == null;
               MutableCharacterStatsAPI currentShipStats = isCaptainNull ? null : member.getCaptain().getStats();
               Map<String, Object> data = Global.getSector().getPersistentData();
               this.tracker.advance(amount);

               for (String DATA_PREFIX : DATA_PREFIXES) {
                  for (String SPCHMODS : COLHULLMODS) {
                     String kept = DATA_PREFIX.substring(0, DATA_PREFIX.indexOf("_"));
                     String remainder = SPCHMODS.substring(SPCHMODS.indexOf("_") + 1, SPCHMODS.length());
                     boolean sameType = remainder.toLowerCase().contains(kept);
                     if (sameType && data.containsKey(DATA_PREFIX + member.getId()) && !member.getVariant().hasHullMod(SPCHMODS)) {
                        data.remove(DATA_PREFIX + member.getId());
                        if (!DRONE_REPLICATORHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(
                              shipVariant, currentShipStats, "specialsphmod_combatdronereplicator_extension"
                           );
                        }

                        if (!CORRUPTED_NANOFORGEHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_nanoforge_corrupted_extension");
                        }

                        if (!CRYOARITHMETIC_ENGINEHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_cryoarithmeticengine_extension");
                        }

                        if (!FUSION_LAMPHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_fusionlampreactor_extension");
                        }

                        if (!HYPERSHUNT_TAPHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_hypershunt_extension");
                        }

                        if (!MANTLE_BOREHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_mantlebore_extension");
                        }

                        if (!PLASMA_DYNAMOHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_plasmadynamo_extension");
                        }

                        if (!PRISTINE_NANOFORGEHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_nanoforge_pristine_extension");
                        }

                        if (!SOIL_NANITEHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_soilnanites_extension");
                        }

                        member.getVariant().getHullMods().remove(SUStringCodex.SPECIAL_ITEM_UTILITY_SCRIPT);
                        member.getVariant().removePermaMod(SUStringCodex.SPECIAL_ITEM_UTILITY_SCRIPT);
                     }
                  }
               }
            }
         }
      }
   }

   static {
      DATA_PREFIXES.add("biofactory_embryo_shu_check_");
      DATA_PREFIXES.add("catalytic_core_shu_check_");
      DATA_PREFIXES.add("dronereplicator_shu_check_");
      DATA_PREFIXES.add("corrupted_nanoforge_shu_check_");
      DATA_PREFIXES.add("cryoarithmetic_engine_shu_check_");
      DATA_PREFIXES.add("dealmaker_holosuite_shu_check_");
      DATA_PREFIXES.add("fullerene_spool_shu_check_");
      DATA_PREFIXES.add("fusionlamp_reactor_shu_check_");
      DATA_PREFIXES.add("hypershunt_shu_check_");
      DATA_PREFIXES.add("mantle_bore_shu_check_");
      DATA_PREFIXES.add("plasma_dynamo_shu_check_");
      DATA_PREFIXES.add("pristine_nanoforge_shu_check_");
      DATA_PREFIXES.add("soil_nanites_shu_check_");
      DATA_PREFIXES.add("synchroton_shu_check_");
      COLHULLMODS.add("specialsphmod_biofactoryembryo_upgrades");
      COLHULLMODS.add("specialsphmod_catalyticcore_upgrades");
      COLHULLMODS.add("specialsphmod_combatdronereplicator_upgrades");
      COLHULLMODS.add("specialsphmod_corruptednanoforge_upgrades");
      COLHULLMODS.add("specialsphmod_cryoarithmeticengine_upgrades");
      COLHULLMODS.add("specialsphmod_dealmakerholosuite_upgrades");
      COLHULLMODS.add("specialsphmod_fullerenespool_upgrades");
      COLHULLMODS.add("specialsphmod_fusionlampreactor_upgrades");
      COLHULLMODS.add("specialsphmod_hypershunt_upgrades");
      COLHULLMODS.add("specialsphmod_mantlebore_upgrades");
      COLHULLMODS.add("specialsphmod_plasmadynamo_upgrades");
      COLHULLMODS.add("specialsphmod_pristinenanoforge_upgrades");
      COLHULLMODS.add("specialsphmod_soilnanites_upgrades");
      COLHULLMODS.add("specialsphmod_synchrotoncore_upgrades");

      for (String s : DATA_PREFIXES) {
         if (s.contains("biofactory")) {
            itemMap.put(s, "biofactory_embryo");
         } else if (s.contains("catalytic")) {
            itemMap.put(s, "catalytic_core");
         } else if (s.contains("dronereplicator")) {
            itemMap.put(s, "drone_replicator");
         } else if (s.contains("corrupted")) {
            itemMap.put(s, "corrupted_nanoforge");
         } else if (s.contains("cryoarithmetic")) {
            itemMap.put(s, "cryoarithmetic_engine");
         } else if (s.contains("dealmaker")) {
            itemMap.put(s, "dealmaker_holosuite");
         } else if (s.contains("fullerene")) {
            itemMap.put(s, "fullerene_spool");
         } else if (s.contains("fusionlamp")) {
            itemMap.put(s, "orbital_fusion_lamp");
         } else if (s.contains("hypershunt")) {
            itemMap.put(s, "coronal_portal");
         } else if (s.contains("mantle")) {
            itemMap.put(s, "mantle_bore");
         } else if (s.contains("plasma")) {
            itemMap.put(s, "plasma_dynamo");
         } else if (s.contains("pristine")) {
            itemMap.put(s, "pristine_nanoforge");
         } else if (s.contains("soil")) {
            itemMap.put(s, "soil_nanites");
         } else if (s.contains("synchroton")) {
            itemMap.put(s, "synchrotron");
         }
      }
   }
}
