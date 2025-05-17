package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SUNanoforgePristineExtension extends BaseHullMod {
   protected static Object STATUSKEY = new Object();
   public static String MA_DATA_KEY = "shu_core_pristine_ext_reloader_data_key";
   private static final float WEAPON_RELOAD_TIME_BONUS = 30.0F;
   private static final float AMMO_BONUS = 50.0F;
   private static final float MISSILE_AMMO_BONUS = 100.0F;
   private static final float MIN_RELOAD_TIME = 35.0F;
   private static final float MAX_RELOAD_TIME = 35.0F;
   private static final float RELOAD_FRACTION_MIN = 1.0F;
   private static final float RELOAD_FRACTION_MAX = 3.0F;
   private static final int TEXT_SIZE = 25;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_PNANOFORGEHMOD_EXTRA_EFFECT;
   boolean disableExpandedMissileRacksIncompat = SUPlugin.DISABLE_EXPANDEDMISSILERACKS_INCOMPATIBILITY;
   boolean disableMissileAutoloaderIncompat = SUPlugin.DISABLE_MISSILEAUTOLOADER_INCOMPATIBILITY;
   float ammoRegenBonus = SUPlugin.CM_PNANOFORGE_WEAPON_RELOAD_BONUS;
   float ammoBonus = SUPlugin.CM_PNANOFORGE_AMMO_BONUS;
   float missileAmmoBonus = SUPlugin.CM_PNANOFORGE_MISSILE_AMMO_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_nanoforge_pristine_extension") ? ALL_INCOMPAT_IDS : null;
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.ammoRegenBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PNANOFORGE_WEAPON_RELOAD_BONUS");
         this.ammoBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PNANOFORGE_AMMO_BONUS");
         this.missileAmmoBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PNANOFORGE_MISSILE_AMMO_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getBallisticAmmoRegenMult().modifyPercent(id, this.ammoRegenBonus);
         stats.getEnergyAmmoRegenMult().modifyPercent(id, this.ammoRegenBonus);
         stats.getBallisticAmmoBonus().modifyPercent(id, this.ammoBonus);
         stats.getEnergyAmmoBonus().modifyPercent(id, this.ammoBonus);
         stats.getMissileAmmoBonus().modifyPercent(id, this.missileAmmoBonus);
      } else if (!this.enableCustomSM) {
         stats.getBallisticAmmoRegenMult().modifyPercent(id, 30.0F);
         stats.getEnergyAmmoRegenMult().modifyPercent(id, 30.0F);
         stats.getBallisticAmmoBonus().modifyPercent(id, 50.0F);
         stats.getEnergyAmmoBonus().modifyPercent(id, 50.0F);
         stats.getMissileAmmoBonus().modifyPercent(id, 100.0F);
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
         this.disableExpandedMissileRacksIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedMissileRacksIncompatibilityToggle");
         this.disableMissileAutoloaderIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMissileAutoloaderIncompatibilityToggle");
      }

      if (!this.disableExpandedMissileRacksIncompat && ship.getVariant().getHullMods().contains("missleracks")) {
         ship.getVariant().removeMod("missleracks");
      }

      if (!this.disableMissileAutoloaderIncompat && ship.getVariant().getHullMods().contains("missile_autoloader")) {
         ship.getVariant().removeMod("missile_autoloader");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disablePristineNanoforgeMFToggle");
      }

      if (!this.disableExtraEffect) {
         CombatEngineAPI engine = Global.getCombatEngine();
         String key = MA_DATA_KEY + "_" + ship.getId();
         if (engine.isPaused() || !ship.isAlive()) {
            return;
         }

         SUNanoforgePristineExtension.MissileAutoforger data = (SUNanoforgePristineExtension.MissileAutoforger)engine.getCustomData().get(key);
         if (data == null) {
            data = new SUNanoforgePristineExtension.MissileAutoforger();
            engine.getCustomData().put(key, data);
         }

         boolean sneeed = false;

         for (WeaponAPI w : ship.getAllWeapons()) {
            if (w.getType() == WeaponType.MISSILE && w.usesAmmo() && w.getAmmo() < w.getMaxAmmo()) {
               sneeed = true;
            }
         }

         if (sneeed) {
            data.interval.advance(amount);
            int elapsed = Math.round((float)((int)data.interval.getElapsed()));
            if (data.interval.intervalElapsed()) {
               for (WeaponAPI wx : ship.getAllWeapons()) {
                  if (wx.getType() == WeaponType.MISSILE) {
                     int currentAmmo = wx.getAmmo();
                     int maxAmmo = wx.getMaxAmmo();
                     if (wx.usesAmmo() && currentAmmo < maxAmmo) {
                        int numerator = (int)Math.max(1.0F, wx.getSpec().getMaxAmmo() / 3.0F);
                        int reloadCount = numerator;
                        int newAmmo = currentAmmo + numerator;
                        if (newAmmo + numerator >= maxAmmo) {
                           reloadCount = maxAmmo - currentAmmo;
                           wx.setAmmo(maxAmmo);
                        } else {
                           wx.setAmmo(newAmmo);
                        }

                        engine.addFloatingText(wx.getLocation(), "+" + reloadCount, 25.0F, Color.GREEN, ship, 0.0F, 0.0F);
                     }

                     Global.getSoundPlayer().playSound("system_forgevats", 1.0F, 1.0F, ship.getLocation(), ship.getVelocity());
                  }
               }
            } else if (ship == Global.getCombatEngine().getPlayerShip()) {
               Global.getCombatEngine()
                  .maintainStatusForPlayerShip(
                     STATUSKEY,
                     Global.getSettings().getSpriteName("tooltips", "nanoforge_missile_fab"),
                     "Status: Forging Missiles",
                     elapsed + " / 35(sec) until reload",
                     false
                  );
            }
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableExpandedMissileRacksIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedMissileRacksIncompatibilityToggle");
            this.disableMissileAutoloaderIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMissileAutoloaderIncompatibilityToggle");
         }

         if (!this.disableExpandedMissileRacksIncompat && ship.getVariant().getHullMods().contains("missleracks")) {
            return false;
         } else if (!this.disableMissileAutoloaderIncompat && ship.getVariant().getHullMods().contains("missile_autoloader")) {
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
            this.disableExpandedMissileRacksIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedMissileRacksIncompatibilityToggle");
            this.disableMissileAutoloaderIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMissileAutoloaderIncompatibilityToggle");
         }

         if (!this.disableExpandedMissileRacksIncompat && ship.getVariant().getHullMods().contains("missleracks")) {
            return "Incompatible with Expanded Missile Racks";
         } else if (!this.disableMissileAutoloaderIncompat && ship.getVariant().getHullMods().contains("missile_autoloader")) {
            return "Incompatible with Missile Autoloader";
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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disablePristineNanoforgeMFToggle");
            this.ammoRegenBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PNANOFORGE_WEAPON_RELOAD_BONUS");
            this.ammoBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PNANOFORGE_AMMO_BONUS");
            this.missileAmmoBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_PNANOFORGE_MISSILE_AMMO_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Decreases reload time of non-missile weapons: %s\n• Increases ammo capacity of weapons: %s\n• Increases ammo capacity of missile weapons: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.ammoRegenBonus) + "%",
                  Misc.getRoundedValue(this.ammoBonus) + "%",
                  Misc.getRoundedValue(this.missileAmmoBonus) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Decreases reload time of non-missile weapons: %s\n• Increases ammo capacity of weapons: %s\n• Increases ammo capacity of missile weapons: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(100.0F) + "%"}
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "nanoforge_missile_fab"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Missile Forging",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Missile Forging"}
               );
               text2.addPara(
                  "The nanoforge will replenish all missile weapons by %s of max ammo (min of %s) for every %s seconds in combat.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{"one-third", Misc.getRoundedValue(1.0F), Misc.getRoundedValue(35.0F)}
               );
               text2.addPara(
                  "The reload function %s include ammo increasing factors from skills and hullmods and it will only reload the base missile count of the weapon.",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"does not"}
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
               "This is an extension of Arsenal Autoforge (Enhanced) and it will remove itself when the main hullmod is removed from the parent module."
            }
         );
      }
   }

   public static class MissileAutoforger {
      final IntervalUtil interval = new IntervalUtil(35.0F, 35.0F);
   }
}
