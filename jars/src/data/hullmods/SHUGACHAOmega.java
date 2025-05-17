package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;

public class SHUGACHAOmega extends BaseHullMod {
   private static final float RECOIL_REDUCTION = 70.0F;
   private static final float AUTOFIRE_BONUS = 80.0F;
   private static final float RANGE_BONUS = 20.0F;
   private static final float FLUX_BONUS = 1.2F;
   private static final float COST_REDUCTION_LG = 8.0F;
   private static final float COST_REDUCTION_MED = 6.0F;
   private static final float COST_REDUCTION_SM = 4.0F;
   private static final float TIME_MULT = 2.0F;
   boolean toggleGeneralIncompat;
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};
   private static final Map mag = new HashMap();
   private final String ID = "SHUGACHAOmega";

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_omega_lol")) {
         stats.getDynamic().getMod("large_ballistic_mod").modifyFlat(id, -8.0F);
         stats.getDynamic().getMod("large_energy_mod").modifyFlat(id, -8.0F);
         stats.getDynamic().getMod("large_missile_mod").modifyFlat(id, -8.0F);
         stats.getDynamic().getMod("medium_ballistic_mod").modifyFlat(id, -6.0F);
         stats.getDynamic().getMod("medium_energy_mod").modifyFlat(id, -6.0F);
         stats.getDynamic().getMod("medium_missile_mod").modifyFlat(id, -6.0F);
         stats.getDynamic().getMod("small_ballistic_mod").modifyFlat(id, -4.0F);
         stats.getDynamic().getMod("small_energy_mod").modifyFlat(id, -4.0F);
         stats.getDynamic().getMod("small_missile_mod").modifyFlat(id, -4.0F);
         stats.getDynamic().getMod("electronic_warfare_flat").modifyFlat(id, (Float)mag.get(hullSize));
         stats.getBallisticWeaponRangeBonus().modifyPercent(id, 20.0F);
         stats.getEnergyWeaponRangeBonus().modifyPercent(id, 20.0F);
         stats.getMaxRecoilMult().modifyMult(id, 0.3F);
         stats.getRecoilPerShotMult().modifyMult(id, 0.3F);
         stats.getRecoilDecayMult().modifyMult(id, 0.3F);
         stats.getAutofireAimAccuracy().modifyFlat(id, 0.79999995F);
         stats.getFluxCapacity().modifyMult(id, 1.2F);
         stats.getFluxDissipation().modifyMult(id, 1.2F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      } else {
         this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
      }

      if (!this.toggleGeneralIncompat) {
         for (String blockedMod : ALL_INCOMPAT_IDS) {
            if (ship.getVariant().getHullMods().contains(blockedMod)) {
               ship.getVariant().removeMod(blockedMod);
               SUHullmodDisplayBlockScript.showBlocked(ship);
            }
         }
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_omega_lol")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_omega_lol")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_omega_lol");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (ship != null) {
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_omega_lol")) {
            MutableShipStatsAPI stats = ship.getMutableStats();
            if (ship.isAlive()) {
               stats.getTimeMult().modifyMult(this.ID, 2.0F);
               stats.getMaxSpeed().modifyMult(this.ID, 0.5F);
               stats.getAcceleration().modifyMult(this.ID, 0.5F);
               stats.getDeceleration().modifyMult(this.ID, 0.5F);
               stats.getMaxTurnRate().modifyMult(this.ID, 0.5F);
               stats.getTurnAcceleration().modifyMult(this.ID, 2.0F);
               stats.getFluxDissipation().modifyMult(this.ID, 2.0F);
               stats.getShieldUpkeepMult().modifyMult(this.ID, 0.5F);
               stats.getShieldTurnRateMult().modifyMult(this.ID, 2.0F);
               stats.getShieldUnfoldRateMult().modifyMult(this.ID, 2.0F);
               stats.getPhaseCloakUpkeepCostBonus().modifyMult(this.ID, 0.5F);
               stats.getPhaseCloakCooldownBonus().modifyMult(this.ID, 0.5F);
               stats.getWeaponTurnRateBonus().modifyMult(this.ID, 2.0F);
               stats.getCRLossPerSecondPercent().modifyMult(this.ID, 0.5F);
               stats.getCombatEngineRepairTimeMult().modifyMult(this.ID, 2.0F);
               stats.getCombatWeaponRepairTimeMult().modifyMult(this.ID, 2.0F);
            }
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
         } else {
            this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
         }

         return this.toggleGeneralIncompat
            || !ship.getVariant().hasHullMod("specialsphmod_alpha_core_upgrades")
               && !ship.getVariant().hasHullMod("specialsphmod_beta_core_upgrades")
               && !ship.getVariant().hasHullMod("specialsphmod_gamma_core_upgrades");
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (SUPlugin.HASLUNALIB) {
         this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
      } else {
         this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
      }

      if (!this.toggleGeneralIncompat) {
         if (ship.getVariant().hasHullMod("specialsphmod_alpha_core_upgrades")) {
            return "Incompatible with Armament Support System (Alpha)";
         }

         if (ship.getVariant().hasHullMod("specialsphmod_beta_core_upgrades")) {
            return "Incompatible with Armament Support System (Beta)";
         }

         if (ship.getVariant().hasHullMod("specialsphmod_gamma_core_upgrades")) {
            return "Incompatible with Armament Support System (Gamma)";
         }
      }

      return super.getUnapplicableReason(ship);
   }

   public boolean affectsOPCosts() {
      return true;
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_omega_lol")) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "banner_ssr"), 368.0F, 40.0F, 5.0F);
            if (SUPlugin.HASLUNALIB) {
               this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
            }

            if (this.enableCheatModeForRetards) {
               LabelAPI retardrius = tooltip.addPara("%s", 5.0F, Misc.getBrightPlayerColor(), new String[]{"Cheat Mode: ON"});
               retardrius.setAlignment(Alignment.MID);
               retardrius.italicize();
            }

            tooltip.addPara(
               "• Improves autofire accuracy: %s\n• Reduces weapon recoil: %s\n• Increases ballistic & energy weapon range: %s\n• Increases flux capacity and dissipation: %s\n• Reduces OP cost for small/medium/large weapons: %s/%s/%s\n• Grants bonus ECM rating in combat: %s/%s/%s/%s (by hull size)\n• ???????????????????",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(80.0F) + "%",
                  Misc.getRoundedValue(70.0F) + "%",
                  Misc.getRoundedValue(200.0F) + "su",
                  Misc.getRoundedValue(20.0F) + "%",
                  Misc.getRoundedValue(4.0F),
                  Misc.getRoundedValue(6.0F),
                  Misc.getRoundedValue(8.0F),
                  Misc.getRoundedValue(10.0F),
                  Misc.getRoundedValue(15.0F),
                  Misc.getRoundedValue(20.0F),
                  Misc.getRoundedValue(30.0F)
               }
            );
         } else {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               Misc.getNegativeHighlightColor(),
               new String[]{"No bonus applied. The information will only appear if this hullmod is S-modded."}
            );
         }

         if (SUPlugin.HASLUNALIB) {
            this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
         } else {
            this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
         }

         if (!this.toggleGeneralIncompat) {
            tooltip.addSectionHeading(
               "Incompatibilities",
               SUStringCodex.SHU_HULLMOD_NEGATIVE_TEXT_COLOR,
               SUStringCodex.SHU_HULLMOD_NEGATIVE_HEADER_BG,
               Alignment.MID,
               SUStringCodex.SHU_TOOLTIP_PADMAIN
            );
            TooltipMakerAPI text = tooltip.beginImageWithText(
               Global.getSettings().getSpriteName("tooltips", "hullmod_incompatible"), SUStringCodex.SHU_TOOLTIP_IMG
            );
            text.addPara(
               "Not compatible with %s, %s, %s",
               SUStringCodex.SHU_TOOLTIP_PADZERO,
               Misc.getNegativeHighlightColor(),
               new String[]{"Armament Support System (Alpha)", "Armament Support System (Beta)", "Armament Support System (Gamma)"}
            );
            tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
         }

         tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADQUOTE,
               SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
               new String[]{"\"...Query, Query... SEARCHING... dir42 Not Found.\""}
            )
            .italicize();
         tooltip.addPara("%s", SUStringCodex.SHU_TOOLTIP_PADSIG, SUStringCodex.SHU_TOOLTIP_QUOTECOLOR, new String[]{"         — [REDACTED]"});
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SSR_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SSR_NAME;
   }

   static {
      mag.put(HullSize.FIGHTER, 0.0F);
      mag.put(HullSize.FRIGATE, 10.0F);
      mag.put(HullSize.DESTROYER, 15.0F);
      mag.put(HullSize.CRUISER, 20.0F);
      mag.put(HullSize.CAPITAL_SHIP, 30.0F);
   }
}
