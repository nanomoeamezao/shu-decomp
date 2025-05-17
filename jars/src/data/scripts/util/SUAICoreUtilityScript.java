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

public class SUAICoreUtilityScript extends BaseHullMod {
   private static final float SFX_VAL = 1.0F;
   private final IntervalUtil tracker = new IntervalUtil(0.5F, 0.7F);
   public static final ArrayList<String> DATA_PREFIXES = new ArrayList<>();
   public static final ArrayList<String> AIHULLMODS = new ArrayList<>();
   public static final HashMap<String, String> itemMap = new HashMap<>();

   public void advanceInCampaign(FleetMemberAPI member, float amount) {
      if (member != null) {
         if (member.getFleetData() != null) {
            if (member.getFleetData().getFleet() != null) {
               boolean ALPHA_COREHMOD = member.getVariant().hasHullMod("specialsphmod_alpha_core_upgrades");
               boolean BETA_COREHMOD = member.getVariant().hasHullMod("specialsphmod_beta_core_upgrades");
               boolean GAMMA_COREHMOD = member.getVariant().hasHullMod("specialsphmod_gamma_core_upgrades");
               ShipVariantAPI shipVariant = member.getVariant();
               boolean isCaptainNull = member.getCaptain() == null;
               MutableCharacterStatsAPI currentShipStats = isCaptainNull ? null : member.getCaptain().getStats();
               Map<String, Object> data = Global.getSector().getPersistentData();
               this.tracker.advance(amount);

               for (String DATA_PREFIX : DATA_PREFIXES) {
                  for (String HMODS : AIHULLMODS) {
                     String kept = DATA_PREFIX.substring(0, DATA_PREFIX.indexOf("_"));
                     String remainder = HMODS.substring(HMODS.indexOf("_") + 1, HMODS.length());
                     boolean sameType = remainder.toLowerCase().contains(kept);
                     if (sameType && data.containsKey(DATA_PREFIX + member.getId()) && !member.getVariant().hasHullMod(HMODS)) {
                        data.remove(DATA_PREFIX + member.getId());
                        if (!ALPHA_COREHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_alpha_core_module_extension");
                        }

                        if (!BETA_COREHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_beta_core_module_extension");
                        }

                        if (!GAMMA_COREHMOD) {
                           SUHullmodUpgradeInstaller.removeHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_gamma_core_module_extension");
                        }

                        member.getVariant().getHullMods().remove(SUStringCodex.AICORE_UTILITY_SCRIPT);
                        member.getVariant().removePermaMod(SUStringCodex.AICORE_UTILITY_SCRIPT);
                     }
                  }
               }
            }
         }
      }
   }

   static {
      DATA_PREFIXES.add("alpha_core_shu_check_");
      DATA_PREFIXES.add("beta_core_shu_check_");
      DATA_PREFIXES.add("gamma_core_shu_check_");
      AIHULLMODS.add("specialsphmod_alpha_core_upgrades");
      AIHULLMODS.add("specialsphmod_beta_core_upgrades");
      AIHULLMODS.add("specialsphmod_gamma_core_upgrades");

      for (String s : DATA_PREFIXES) {
         if (s.contains("alpha")) {
            itemMap.put(s, "alpha_core");
         } else if (s.contains("beta")) {
            itemMap.put(s, "beta_core");
         } else if (s.contains("gamma")) {
            itemMap.put(s, "gamma_core");
         }
      }
   }
}
