package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.id.SUStringCodex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SUSpecialCollabItemUtilityScript extends BaseHullMod {
   private static final float SFX_VAL = 1.0F;
   private final IntervalUtil tracker = new IntervalUtil(0.5F, 0.5F);
   public static final ArrayList<String> DATA_PREFIXES = new ArrayList<>();
   public static final ArrayList<String> COLLABHULLMODS = new ArrayList<>();
   public static final HashMap<String, String> itemMapCollab = new HashMap<>();

   public void advanceInCampaign(FleetMemberAPI member, float amount) {
      if (member != null) {
         if (member.getFleetData() != null) {
            if (member.getFleetData().getFleet() != null) {
               boolean MOTE_MEGACONDENSERHMOD = member.getVariant().hasHullMod("specialsphmod_sfc_phasemote_upgrades");
               boolean isSFCPresent = Global.getSettings().getModManager().isModEnabled("PAGSM");
               boolean isUAFPresent = Global.getSettings().getModManager().isModEnabled("uaf");
               Map<String, Object> data = Global.getSector().getPersistentData();
               this.tracker.advance(amount);

               for (String DATA_PREFIX : DATA_PREFIXES) {
                  for (String SPCHMODS : COLLABHULLMODS) {
                     String kept = DATA_PREFIX.substring(0, DATA_PREFIX.indexOf("_"));
                     String remainder = SPCHMODS.substring(SPCHMODS.indexOf("_") + 1, SPCHMODS.length());
                     boolean sameType = remainder.toLowerCase().contains(kept);
                     if (sameType && data.containsKey(DATA_PREFIX + member.getId()) && !member.getVariant().hasHullMod(SPCHMODS)) {
                        data.remove(DATA_PREFIX + member.getId());
                        if (!isSFCPresent && isUAFPresent) {
                        }

                        if (!MOTE_MEGACONDENSERHMOD
                           && member.getHullSpec().getHullId().contains("ziggurat")
                           && member.getVariant().hasHullMod("high_frequency_attractor")) {
                           member.getVariant().getHullMods().remove("high_frequency_attractor");
                           member.getVariant().getPermaMods().remove("high_frequency_attractor");
                        }

                        member.getVariant().getHullMods().remove(SUStringCodex.COLLAB_SPECIAL_ITEM_UTILITY_SCRIPT);
                        member.getVariant().removePermaMod(SUStringCodex.COLLAB_SPECIAL_ITEM_UTILITY_SCRIPT);
                     }
                  }
               }
            }
         }
      }
   }

   static {
      DATA_PREFIXES.add("aquaticstim_shu_sfc_check_");
      DATA_PREFIXES.add("megacondenser_shu_sfc_check_");
      DATA_PREFIXES.add("dimensionalnano_forge_shu_uaf_check_");
      DATA_PREFIXES.add("dimensionalstove_shu_uaf_check_");
      DATA_PREFIXES.add("garrison_transmitter_shu_uaf_check_");
      DATA_PREFIXES.add("modular_purifier_shu_check_");
      DATA_PREFIXES.add("accessrouter_shu_uaf_check_");
      DATA_PREFIXES.add("servosyncpump_shu_check_");
      DATA_PREFIXES.add("ricecooker_interplanetary_shu_uaf_check_");
      COLLABHULLMODS.add("specialsphmod_sfc_aquaticstimulator_upgrades");
      COLLABHULLMODS.add("specialsphmod_sfc_phasemote_upgrades");
      COLLABHULLMODS.add("specialsphmod_uaf_dimensionalnanoforge_upgrades");
      COLLABHULLMODS.add("specialsphmod_uaf_dimensionalstove_upgrades");
      COLLABHULLMODS.add("specialsphmod_uaf_garrisontransmitter_upgrades");
      COLLABHULLMODS.add("specialsphmod_uaf_modularpurifier_upgrades");
      COLLABHULLMODS.add("specialsphmod_uaf_interplanetaryaccessrouter_upgrades");
      COLLABHULLMODS.add("specialsphmod_uaf_servosyncpump_upgrades");
      COLLABHULLMODS.add("specialsphmod_uaf_interplanetary_ricecooker_upgrades");

      for (String s : DATA_PREFIXES) {
         if (s.contains("aquaticstim")) {
            itemMapCollab.put(s, "sfc_aquaticstimulator");
         } else if (s.contains("megacondenser")) {
            itemMapCollab.put(s, "sfc_motemegacondenser");
         } else if (s.contains("dimensionalnano")) {
            itemMapCollab.put(s, "uaf_dimen_nanoforge");
         } else if (s.contains("dimensionalstove")) {
            itemMapCollab.put(s, "uaf_dimen_microwave");
         } else if (s.contains("garrison")) {
            itemMapCollab.put(s, "uaf_garrison_transmitter");
         } else if (s.contains("modular")) {
            itemMapCollab.put(s, "uaf_modular_purifier");
         } else if (s.contains("accessrouter")) {
            itemMapCollab.put(s, "uaf_access_router");
         } else if (s.contains("servosyncpump")) {
            itemMapCollab.put(s, "uaf_servosync_pump");
         } else if (s.contains("ricecooker")) {
            itemMapCollab.put(s, "uaf_rice_cooker");
         }
      }
   }
}
