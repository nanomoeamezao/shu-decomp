package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.input.Keyboard;

public class SUFusionLampExtension extends BaseHullMod {
   private static final float DAMAGE_BONUS = 5.0F;
   private static final float FLUX_REDUC = 10.0F;
   private static final float SHIELD_DAMAGE_BONUS = 15.0F;
   private static final float RANGE_BONUS = 20.0F;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_FUSIONLAMPHMOD_EXTRA_EFFECT;
   boolean disableAdvancedOpticsIncompat = SUPlugin.DISABLE_ADVANCEDOPTICS_INCOMPATIBILITY;
   float damageBonus = SUPlugin.CM_FUSIONLAMP_DAMAGE_BONUS;
   float fluxReduction = SUPlugin.CM_FUSIONLAMP_FLUX_REDUC;
   float shieldDamageBonus = SUPlugin.CM_FUSIONLAMP_SHIELD_DAMAGE_BONUS;
   float rangeBonus = SUPlugin.CM_FUSIONLAMP_RANGE_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_fusionlampreactor_extension") ? ALL_INCOMPAT_IDS : null;
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.damageBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_DAMAGE_BONUS");
         this.fluxReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_FLUX_REDUC");
         this.shieldDamageBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_SHIELD_DAMAGE_BONUS");
         this.rangeBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_RANGE_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getBeamWeaponDamageMult().modifyPercent(id, this.damageBonus);
         stats.getEnergyWeaponDamageMult().modifyPercent(id, this.damageBonus);
         stats.getBeamWeaponFluxCostMult().modifyPercent(id, -this.fluxReduction);
         stats.getDamageToTargetShieldsMult().modifyPercent(id, this.shieldDamageBonus);
         stats.getBeamWeaponRangeBonus().modifyPercent(id, this.rangeBonus);
         stats.getEnergyWeaponRangeBonus().modifyPercent(id, this.rangeBonus);
      } else if (!this.enableCustomSM) {
         stats.getBeamWeaponDamageMult().modifyPercent(id, 5.0F);
         stats.getEnergyWeaponDamageMult().modifyPercent(id, 5.0F);
         stats.getBeamWeaponFluxCostMult().modifyPercent(id, -10.0F);
         stats.getDamageToTargetShieldsMult().modifyPercent(id, 15.0F);
         stats.getBeamWeaponRangeBonus().modifyPercent(id, 20.0F);
         stats.getEnergyWeaponRangeBonus().modifyPercent(id, 20.0F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
      } else {
         this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
      }

      if (!this.toggleGeneralIncompat) {
         for (String blockedMod : ALL_INCOMPAT_IDS) {
            if (ship.getVariant().getHullMods().contains(blockedMod)) {
               ship.getVariant().removeMod(blockedMod);
               ship.getVariant().removePermaMod(blockedMod);
            }
         }
      }

      if (SUPlugin.HASLUNALIB) {
         this.disableAdvancedOpticsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAdvancedOpticsIncompatibilityToggle");
      }

      if (!this.disableAdvancedOpticsIncompat && ship.getVariant().getHullMods().contains("advancedoptics")) {
         ship.getVariant().removeMod("advancedoptics");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableFusionLampReactorSCToggle");
      }

      if (!this.disableExtraEffect) {
         if (!ship.isAlive()) {
            return;
         }

         List<DamagingProjectileAPI> EMPdmg = new ArrayList<>();
         List<DamagingProjectileAPI> projhit = new ArrayList<>();
         List<DamagingProjectileAPI> cleaneffect = new ArrayList<>();
         if (!ship.isHulk() && !ship.isPiece() && ship.getFluxLevel() >= 0.3F) {
            ShipAPI target = AIUtils.getNearestEnemy(ship);
            if (target == null) {
               return;
            }

            for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(ship.getLocation(), 500.0F)) {
               if (ship == proj.getSource() && !EMPdmg.contains(proj) && !projhit.contains(proj)) {
                  if (Math.random() < 0.1F) {
                     EMPdmg.add(proj);
                  } else {
                     projhit.add(proj);
                  }
               }
            }

            for (DamagingProjectileAPI projx : EMPdmg) {
               if (projx.didDamage() && !projhit.contains(projx)) {
                  float empDamage = projx.getEmpAmount() * 1.2F;
                  DamageType var10000 = projx.getDamageType();
                  projx.getDamageType();
                  if (var10000 == DamageType.ENERGY) {
                     projhit.add(projx);
                     if (projx.getDamageTarget() instanceof ShipAPI && projx.getDamageAmount() != 0.0F) {
                        Global.getCombatEngine()
                           .spawnEmpArc(
                              projx.getSource(),
                              target.getLocation(),
                              target,
                              target,
                              DamageType.ENERGY,
                              projx.getDamageAmount() / 2.0F,
                              empDamage,
                              3000.0F,
                              "system_emp_emitter_impact",
                              6.0F,
                              new Color(200, 60, 10, 55),
                              new Color(240, 70, 40, 255)
                           );
                        Global.getCombatEngine()
                           .spawnEmpArcVisual(
                              projx.getLocation(), projx, target.getLocation(), target, 5.0F, new Color(200, 60, 10, 50), new Color(240, 70, 40, 255)
                           );
                     }
                  }
               }
            }

            for (DamagingProjectileAPI projxx : projhit) {
               if (!Global.getCombatEngine().isEntityInPlay(projxx)) {
                  EMPdmg.remove(projxx);
                  cleaneffect.add(projxx);
               }
            }

            for (DamagingProjectileAPI projxxx : cleaneffect) {
               projhit.remove(projxxx);
            }

            cleaneffect.clear();
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableAdvancedOpticsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAdvancedOpticsIncompatibilityToggle");
         }

         if (!this.disableAdvancedOpticsIncompat && ship.getVariant().hasHullMod("advancedoptics")) {
            return false;
         } else {
            return SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
               ? false
               : !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds());
         }
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableAdvancedOpticsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAdvancedOpticsIncompatibilityToggle");
         }

         if (!this.disableAdvancedOpticsIncompat && ship.getVariant().hasHullMod("advancedoptics")) {
            return "Incompatible with Advanced Optics";
         } else {
            return !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
                  && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
               ? super.getUnapplicableReason(ship)
               : "Only one type of special upgrade hullmod can be installed per ship";
         }
      } else {
         return "Unable to locate ship!";
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableFusionLampReactorSCToggle");
            this.damageBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_DAMAGE_BONUS");
            this.fluxReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_FLUX_REDUC");
            this.shieldDamageBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_SHIELD_DAMAGE_BONUS");
            this.rangeBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_RANGE_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases beam and energy weapon damage: %s\n• Reduces flux cost of beam weapons: %s\n• Increases damage dealt to shields: %s\n• Increases beam and energy weapon range: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.damageBonus) + "%",
                  Misc.getRoundedValue(this.fluxReduction) + "%",
                  Misc.getRoundedValue(this.shieldDamageBonus) + "%",
                  Misc.getRoundedValue(this.rangeBonus) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases beam and energy weapon damage: %s\n• Reduces flux cost of beam weapons: %s\n• Increases damage dealt to shields: %s\n• Increases beam and energy weapon range: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(5.0F) + "%", Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(15.0F) + "%", Misc.getRoundedValue(20.0F) + "%"
               }
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "supercharged_alt"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Supercharged Capacitors",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Supercharged Capacitors"}
               );
               text2.addPara(
                  "When the ship's flux level is above %s, the installed Fusion Capacitors will become supercharged. Each energy-type projectile has a %s chance to arc to weapons and engines, dealing extra damage %s of the original hit.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(10.0F) + "%", "half"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
            }

            if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addPara(
                     "Press and hold [%s] to view its passive system.",
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
            new String[]{
               "This is an extension of Variable Fusion Capacitors and it will remove itself when the main hullmod is removed from the parent module."
            }
         );
      }
   }
}
