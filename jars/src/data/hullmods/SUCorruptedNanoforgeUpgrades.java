package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.HullModItemManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SUCorruptedNanoforgeUpgrades extends BaseHullMod {
   protected static Object STATUSKEY = new Object();
   public static String MA_DATA_KEY = "$core_reloader_data_key";
   public static final String DATA_PREFIX = "corrupted_nanoforge_shu_check_";
   public static final String ITEM = "corrupted_nanoforge";
   private static final float AMMO_BONUS = 20.0F;
   private static final float MISSILE_AMMO_BONUS = 30.0F;
   private static final float MIN_RELOAD_TIME = 50.0F;
   private static final float MAX_RELOAD_TIME = 50.0F;
   private static final float RELOAD_FRACTION_MIN = 1.0F;
   private static final float RELOAD_FRACTION_MAX = 4.0F;
   private static final int TEXT_SIZE = 25;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_CNANOFORGEHMOD_EXTRA_EFFECT;
   boolean disableExpandedMissileRacksIncompat = SUPlugin.DISABLE_EXPANDEDMISSILERACKS_INCOMPATIBILITY;
   boolean disableMissileAutoloaderIncompat = SUPlugin.DISABLE_MISSILEAUTOLOADER_INCOMPATIBILITY;
   float ammoBonus = SUPlugin.CM_CNANOFORGE_AMMO_BONUS;
   float missileAmmoBonus = SUPlugin.CM_CNANOFORGE_MISSILE_AMMO_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_corruptednanoforge_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("corrupted_nanoforge", null), null);
   }

   public void addRequiredItemSection(
      TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI currentVariant, MarketAPI dockedAt, float width, boolean isForModSpec
   ) {
      CargoStackAPI req = this.getRequiredItem();
      if (req != null) {
         float opad = 2.0F;
         if (isForModSpec || Global.CODEX_TOOLTIP_MODE) {
            Color color = Misc.getBasePlayerColor();
            if (isForModSpec) {
               color = Misc.getHighlightColor();
            }

            String name = req.getDisplayName();
            String aOrAn = Misc.getAOrAnFor(name);
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("corrupted_nanoforge").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("corrupted_nanoforge").getIconName(), 20.0F);
                  text2.addPara("Using item: " + req.getDisplayName(), color, opad);
                  tooltip.addImageWithText(5.0F);
               }
            } else {
               int available = HullModItemManager.getInstance().getNumAvailableMinusUnconfirmed(req, member, currentVariant, dockedAt);
               Color color = Misc.getPositiveHighlightColor();
               if (available < 1) {
                  color = Misc.getNegativeHighlightColor();
               }

               if (available < 0) {
                  available = 0;
               }

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("corrupted_nanoforge").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.ammoBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CNANOFORGE_AMMO_BONUS");
         this.missileAmmoBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CNANOFORGE_MISSILE_AMMO_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getBallisticAmmoBonus().modifyPercent(id, this.ammoBonus);
         stats.getEnergyAmmoBonus().modifyPercent(id, this.ammoBonus);
         stats.getMissileAmmoBonus().modifyPercent(id, this.missileAmmoBonus);
      } else if (!this.enableCustomSM) {
         stats.getBallisticAmmoBonus().modifyPercent(id, 20.0F);
         stats.getEnergyAmmoBonus().modifyPercent(id, 20.0F);
         stats.getMissileAmmoBonus().modifyPercent(id, 30.0F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      ShipVariantAPI shipVariant = ship.getVariant();
      MutableCharacterStatsAPI currentShipStats = ship.getCaptain() == null ? null : ship.getCaptain().getStats();
      if (SUPlugin.HASLUNALIB) {
         this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
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

      if (SUPlugin.HASLUNALIB) {
         this.disableExpandedMissileRacksIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedMissileRacksIncompatibilityToggle");
         this.disableMissileAutoloaderIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMissileAutoloaderIncompatibilityToggle");
      }

      if (!this.disableExpandedMissileRacksIncompat && ship.getVariant().getHullMods().contains("missleracks")) {
         ship.getVariant().removeMod("missleracks");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (!this.disableMissileAutoloaderIncompat && ship.getVariant().getHullMods().contains("missile_autoloader")) {
         ship.getVariant().removeMod("missile_autoloader");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_corruptednanoforge_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_corruptednanoforge_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_corruptednanoforge_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_corruptednanoforge_upgrades")) {
         ship.getVariant().addPermaMod("specialsphmod_corruptednanoforge_utilityscript");
         if (currentShipStats != null) {
            SUHullmodUpgradeInstaller.applyHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_nanoforge_corrupted_extension");
         }
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCorruptedNanoforgeMFToggle");
      }

      if (!this.disableExtraEffect) {
         CombatEngineAPI engine = Global.getCombatEngine();
         String key = MA_DATA_KEY + "_" + ship.getId();
         if (engine.isPaused() || !ship.isAlive()) {
            return;
         }

         SUCorruptedNanoforgeUpgrades.MissileAutoforger data = (SUCorruptedNanoforgeUpgrades.MissileAutoforger)engine.getCustomData().get(key);
         if (data == null) {
            data = new SUCorruptedNanoforgeUpgrades.MissileAutoforger();
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
                        int numerator = (int)Math.max(1.0F, wx.getSpec().getMaxAmmo() / 4.0F);
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
                     elapsed + " / 50(sec) until reload",
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
            if (SUPlugin.HASLUNALIB) {
               this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
            } else {
               this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
            }

            return this.toggleGeneralIncompat || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS);
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
         } else if (ship.getVariant().getHullMods().contains("missile_reload")) {
            return "Incompatible with this autoforge";
         } else {
            if (SUPlugin.HASLUNALIB) {
               this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
            } else {
               this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
            }

            return this.toggleGeneralIncompat
                  || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
                     && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
               ? super.getUnapplicableReason(ship)
               : "Only one type of special upgrade hullmod can be installed per ship";
         }
      } else {
         return "Unable to locate ship!";
      }
   }

   public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      return status == 0 ? false : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      return status == 0
         ? "This installation is not applicable to modules, please install it on the main module"
         : super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCorruptedNanoforgeMFToggle");
            this.disableExpandedMissileRacksIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedMissileRacksIncompatibilityToggle");
            this.disableMissileAutoloaderIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMissileAutoloaderIncompatibilityToggle");
            this.ammoBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CNANOFORGE_AMMO_BONUS");
            this.missileAmmoBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CNANOFORGE_MISSILE_AMMO_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases ammo capacity of weapons: %s\n• Increases ammo capacity of missile weapons: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(this.ammoBonus) + "%", Misc.getRoundedValue(this.missileAmmoBonus) + "%"}
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases ammo capacity of weapons: %s\n• Increases ammo capacity of missile weapons: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(20.0F) + "%", Misc.getRoundedValue(30.0F) + "%"}
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
                  "The Nanoforge will replenish all missile weapons by %s of max ammo (min of %s) for every %s seconds in combat.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{"one-fourth", Misc.getRoundedValue(1.0F), Misc.getRoundedValue(50.0F)}
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

         boolean disableDestruction = SUPlugin.DISABLE_ITEMDESTRUCTION;
         if (SUPlugin.HASLUNALIB) {
            this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
            disableDestruction = LunaSettings.getBoolean("mayu_specialupgrades", "shu_itemdestructionToggle");
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
            if (!this.disableExpandedMissileRacksIncompat && !this.disableMissileAutoloaderIncompat) {
               text.addPara(
                  "Not compatible with %s, %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Expanded Missile Racks", "Missile Autoloader", "Other Special Upgrade Hullmods"}
               );
            } else if (this.disableExpandedMissileRacksIncompat && !this.disableMissileAutoloaderIncompat) {
               text.addPara(
                  "Not compatible with %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Missile Autoloader", "Other Special Upgrade Hullmods"}
               );
            } else if (!this.disableExpandedMissileRacksIncompat && this.disableMissileAutoloaderIncompat) {
               text.addPara(
                  "Not compatible with %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Expanded Missile Racks", "Other Special Upgrade Hullmods"}
               );
            } else if (this.disableExpandedMissileRacksIncompat && this.disableMissileAutoloaderIncompat) {
               text.addPara(
                  "Not compatible with %s", SUStringCodex.SHU_TOOLTIP_PADZERO, Misc.getNegativeHighlightColor(), new String[]{"Other Special Upgrade Hullmods"}
               );
            }

            tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
         }

         if (!disableDestruction) {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADNOTE,
               Misc.getGrayColor(),
               new String[]{
                  "This hullmod counts as a special upgrade and it can work in conjunction with Armament Support System hullmod. Furthermore, the item is forever lost when the ship gets destroyed in combat."
               }
            );
         } else {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADNOTE,
               Misc.getGrayColor(),
               new String[]{"This hullmod counts as a special upgrade and it can work in conjunction with Armament Support System hullmod."}
            );
         }
      }
   }

   public static class MissileAutoforger {
      final IntervalUtil interval = new IntervalUtil(50.0F, 50.0F);
   }
}
