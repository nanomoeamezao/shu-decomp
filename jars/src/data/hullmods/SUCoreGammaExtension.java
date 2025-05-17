package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SUCoreGammaExtension extends BaseHullMod {
   private static final float AUTOFIRE_BONUS = 15.0F;
   private static final float TURRET_TURN_BONUS = 30.0F;
   private static final float COST_REDUCTION_LG = 1.0F;
   private static final float COST_REDUCTION_MED = 1.0F;
   private static final float COST_REDUCTION_SM = 2.0F;
   private static final float DAMAGE_BONUS = 10.0F;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   float autoFireBonus = SUPlugin.CM_GAMMA_AUTOFIRE_BONUS;
   float turretTurnBonus = SUPlugin.CM_GAMMA_TURRET_TURN_BONUS;
   float largeOPReduc = SUPlugin.CM_GAMMA_COST_REDUCTION_LG;
   float mediumOPReduc = SUPlugin.CM_GAMMA_COST_REDUCTION_MED;
   float smallOPReduc = SUPlugin.CM_GAMMA_COST_REDUCTION_SM;
   boolean toggleGeneralIncompat;
   private static final String[] INCOMPAT_CORES = new String[]{
      "specialsphmod_alpha_core_upgrades",
      "specialsphmod_beta_core_upgrades",
      "specialsphmod_alpha_core_module_extension",
      "specialsphmod_beta_core_module_extension"
   };

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_gamma_core_module_extension") ? INCOMPAT_CORES : null;
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.autoFireBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_GAMMA_AUTOFIRE_BONUS");
         this.turretTurnBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_GAMMA_TURRET_TURN_BONUS");
         this.largeOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_GAMMA_COST_REDUCTION_LG");
         this.mediumOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_GAMMA_COST_REDUCTION_MED");
         this.smallOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_GAMMA_COST_REDUCTION_SM");
      }

      if (this.enableCustomSM) {
         stats.getAutofireAimAccuracy().modifyFlat(id, this.autoFireBonus * 0.01F);
         stats.getWeaponTurnRateBonus().modifyPercent(id, this.turretTurnBonus);
         stats.getBeamWeaponTurnRateBonus().modifyPercent(id, this.turretTurnBonus);
         stats.getDynamic().getMod("large_ballistic_mod").modifyFlat(id, -this.largeOPReduc);
         stats.getDynamic().getMod("large_energy_mod").modifyFlat(id, -this.largeOPReduc);
         stats.getDynamic().getMod("medium_ballistic_mod").modifyFlat(id, -this.mediumOPReduc);
         stats.getDynamic().getMod("medium_energy_mod").modifyFlat(id, -this.mediumOPReduc);
         stats.getDynamic().getMod("small_ballistic_mod").modifyFlat(id, -this.smallOPReduc);
         stats.getDynamic().getMod("small_energy_mod").modifyFlat(id, -this.smallOPReduc);
      } else if (!this.enableCustomSM) {
         stats.getAutofireAimAccuracy().modifyFlat(id, 0.14999999F);
         stats.getWeaponTurnRateBonus().modifyPercent(id, 30.0F);
         stats.getBeamWeaponTurnRateBonus().modifyPercent(id, 30.0F);
         stats.getDynamic().getMod("large_ballistic_mod").modifyFlat(id, -1.0F);
         stats.getDynamic().getMod("large_energy_mod").modifyFlat(id, -1.0F);
         stats.getDynamic().getMod("medium_ballistic_mod").modifyFlat(id, -1.0F);
         stats.getDynamic().getMod("medium_energy_mod").modifyFlat(id, -1.0F);
         stats.getDynamic().getMod("small_ballistic_mod").modifyFlat(id, -2.0F);
         stats.getDynamic().getMod("small_energy_mod").modifyFlat(id, -2.0F);
      }

      if (stats.getVariant().getHullMods().contains("yunru_gammacore")) {
         stats.getBallisticWeaponDamageMult().modifyPercent(id, 10.0F);
         stats.getEnergyWeaponDamageMult().modifyPercent(id, 10.0F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
      } else {
         this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
      }

      if (!this.toggleGeneralIncompat) {
         for (String blockedMod : INCOMPAT_CORES) {
            if (ship.getVariant().getHullMods().contains(blockedMod)) {
               ship.getVariant().removeMod(blockedMod);
               ship.getVariant().removePermaMod(blockedMod);
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

         return !this.toggleGeneralIncompat && SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), INCOMPAT_CORES)
            ? false
            : !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds());
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
         } else {
            this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
         }

         return this.toggleGeneralIncompat
               || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), INCOMPAT_CORES)
                  && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
            ? super.getUnapplicableReason(ship)
            : "Incompatible with installed AI core";
      } else {
         return "Unable to locate ship!";
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return index == 0 ? "Gamma-class AI" : null;
   }

   public boolean affectsOPCosts() {
      return true;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.autoFireBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_GAMMA_AUTOFIRE_BONUS");
            this.turretTurnBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_GAMMA_TURRET_TURN_BONUS");
            this.largeOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_GAMMA_COST_REDUCTION_LG");
            this.mediumOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_GAMMA_COST_REDUCTION_MED");
            this.smallOPReduc = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_GAMMA_COST_REDUCTION_SM");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Improves autofire accuracy: %s\n• Increases turret turn rate: %s\n• Reduces OP cost for non-missile weapons: %s/%s/%s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.autoFireBonus) + "%",
                  Misc.getRoundedValue(this.turretTurnBonus) + "%",
                  Misc.getRoundedValue(this.smallOPReduc),
                  Misc.getRoundedValue(this.mediumOPReduc),
                  Misc.getRoundedValue(this.largeOPReduc)
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Improves autofire accuracy: %s\n• Increases turret turn rate: %s\n• Reduces OP cost for non-missile weapons: %s/%s/%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(15.0F) + "%",
                  Misc.getRoundedValue(30.0F) + "%",
                  Misc.getRoundedValue(2.0F),
                  Misc.getRoundedValue(1.0F),
                  Misc.getRoundedValue(1.0F)
               }
            );
         }

         if (Global.getSettings().getModManager().isModEnabled("yunrucore")) {
            if (ship.getVariant().hasHullMod("specialsphmod_gamma_core_module_extension")
               && ship.getVariant().getHullMods().contains("yunru_gammacore")
               && Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Core Synchronization Effect", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI withyunrugamma = tooltip.beginImageWithText(Global.getSettings().getHullModSpec("yunru_gammacore").getSpriteName(), 36.0F);
               withyunrugamma.addPara(
                  "A ship with %s has been detected, having multiple Gamma cores installed together will unlock their latent synchronization ability. The following additional effects will be applied.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  SUStringCodex.SHU_HULLMOD_GAMMA_NAME,
                  new String[]{"Integrated Gamma Core"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• Increased energy weapon damage: %s\n• Increased ballistic weapon damage: %s",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(10.0F) + "%"}
               );
            }

            if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))
               && ship.getVariant().hasHullMod("specialsphmod_gamma_core_module_extension")
               && ship.getVariant().getHullMods().contains("yunru_gammacore")) {
               tooltip.addPara(
                     "Press and hold [%s] to view the unlocked bonus.",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     Misc.getGrayColor(),
                     Misc.getStoryBrightColor(),
                     new String[]{"F1"}
                  )
                  .setAlignment(Alignment.MID);
            }
         }

         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADMAIN,
            Misc.getGrayColor(),
            new String[]{"This is an extension of Armament Support System and it will remove itself when the main hullmod is removed from the parent module."}
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GAMMA_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GAMMA_NAME;
   }
}
