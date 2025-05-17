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

public class SUMantleBoreExtension extends BaseHullMod {
   private static final float PROJ_SPEED_BONUS = 30.0F;
   private static final float BALLISTIC_RANGE_BONUS = 100.0F;
   private static final float RECOIL_BONUS = 60.0F;
   private static final float HIT_STR = 10.0F;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_MANTLEBOREHMOD_EXTRA_EFFECT;
   float ballisticRangeBonus = SUPlugin.CM_MANTLEBORE_BALLISTIC_RANGE_BONUS;
   float hitStrength = SUPlugin.CM_MANTLEBORE_HIT_STR;
   float projectileSpeedBonus = SUPlugin.CM_MANTLEBORE_PROJ_SPEED_BONUS;
   float recoilBonus = SUPlugin.CM_MANTLEBORE_RECOIL_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_mantlebore_extension") ? ALL_INCOMPAT_IDS : null;
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.ballisticRangeBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_BALLISTIC_RANGE_BONUS");
         this.hitStrength = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_HIT_STR");
         this.projectileSpeedBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_PROJ_SPEED_BONUS");
         this.recoilBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_RECOIL_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getBallisticWeaponRangeBonus().modifyPercent(id, this.ballisticRangeBonus);
         stats.getHitStrengthBonus().modifyPercent(id, this.hitStrength);
         stats.getProjectileSpeedMult().modifyPercent(id, this.projectileSpeedBonus);
         stats.getMaxRecoilMult().modifyMult(id, 1.0F - 0.01F * this.recoilBonus);
         stats.getRecoilPerShotMult().modifyMult(id, 1.0F - 0.01F * this.recoilBonus);
         stats.getRecoilDecayMult().modifyMult(id, 1.0F - 0.01F * this.recoilBonus);
      } else if (!this.enableCustomSM) {
         stats.getBallisticWeaponRangeBonus().modifyPercent(id, 100.0F);
         stats.getHitStrengthBonus().modifyPercent(id, 10.0F);
         stats.getProjectileSpeedMult().modifyPercent(id, 30.0F);
         stats.getMaxRecoilMult().modifyMult(id, 0.40000004F);
         stats.getRecoilPerShotMult().modifyMult(id, 0.40000004F);
         stats.getRecoilDecayMult().modifyMult(id, 0.40000004F);
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
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMantleBoreUVRToggle");
      }

      if (!this.disableExtraEffect) {
         if (!ship.isAlive()) {
            return;
         }

         List<DamagingProjectileAPI> criticaldmg = new ArrayList<>();
         List<DamagingProjectileAPI> projhit = new ArrayList<>();
         List<DamagingProjectileAPI> cleaneffect = new ArrayList<>();
         if (!ship.isHulk() && !ship.isPiece()) {
            ShipAPI target = AIUtils.getNearestEnemy(ship);
            if (target == null) {
               return;
            }

            for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(ship.getLocation(), 600.0F)) {
               if (ship == proj.getSource() && !criticaldmg.contains(proj) && !projhit.contains(proj)) {
                  if (Math.random() < 0.01F) {
                     criticaldmg.add(proj);
                     proj.setDamageAmount(proj.getDamageAmount() * 1.1F);
                  } else {
                     projhit.add(proj);
                  }
               }
            }

            for (DamagingProjectileAPI projx : criticaldmg) {
               if (projx.didDamage() && !projhit.contains(projx)) {
                  DamageType var10000 = projx.getDamageType();
                  projx.getDamageType();
                  if (var10000 == DamageType.KINETIC) {
                     projhit.add(projx);
                     if (projx.getDamageTarget() instanceof ShipAPI && projx.getDamageAmount() != 0.0F) {
                        Global.getCombatEngine().addFloatingText(projx.getLocation(), "Critical Hit!!!", 25.0F, Color.ORANGE, projx, 3.0F, 1.0F);
                        Global.getCombatEngine().addHitParticle(projx.getLocation(), projx.getVelocity(), 50.0F, 1.0F, 0.05F, Color.ORANGE);
                     }
                  }
               }
            }

            for (DamagingProjectileAPI projxx : projhit) {
               if (!Global.getCombatEngine().isEntityInPlay(projxx)) {
                  criticaldmg.remove(projxx);
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
         return SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
            ? false
            : !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds());
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         return !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
               && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
            ? super.getUnapplicableReason(ship)
            : "Only one type of special upgrade hullmod can be installed per ship";
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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMantleBoreUVRToggle");
            this.ballisticRangeBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_BALLISTIC_RANGE_BONUS");
            this.hitStrength = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_HIT_STR");
            this.projectileSpeedBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_PROJ_SPEED_BONUS");
            this.recoilBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_RECOIL_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases ballistic weapon range: %s\n• Increases hit strength of weapons to armor: %s\n• Increases weapon projectile speed: %s\n• Reduces weapon recoil: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.ballisticRangeBonus) + "%",
                  Misc.getRoundedValue(this.hitStrength) + "%",
                  Misc.getRoundedValue(this.projectileSpeedBonus) + "%",
                  Misc.getRoundedValue(this.recoilBonus) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases ballistic weapon range: %s\n• Increases hit strength of weapons to armor: %s\n• Increases weapon projectile speed: %s\n• Reduces weapon recoil: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(60.0F) + "%"
               }
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "hv_projectile"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Ultra-Velocity Rounds",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Ultra-Velocity Rounds"}
               );
               text2.addPara(
                  "A hacked weapon configuration that takes advantage of the throttled firing chambers. The accelerated projectile has a %s chance of dealing additional %s damage on-hit. This only applies to %s damage.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(3.0F) + "%", Misc.getRoundedValue(50.0F) + "%", "kinetic"}
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
               "This is an extension of High-Throttled Weapon Torque and it will remove itself when the main hullmod is removed from the parent module."
            }
         );
      }
   }
}
