package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.HullModItemManager;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
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
import org.magiclib.util.MagicUI;

public class SUAuroranDimensionalNanoforgeUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX_COLLAB = "dimensionalnano_forge_shu_uaf_check_";
   public static final String ITEM = "uaf_dimen_nanoforge";
   public static final float FTR_REFIT_BONUS = 20.0F;
   public static final float FTR_HULL_ARMOR_BONUS = 30.0F;
   public static final float FTR_AMMO_COUNT = 50.0F;
   public static final String RD_NO_EXTRA_CRAFT = "rd_no_extra_craft";
   public static final String RD_FORCE_EXTRA_CRAFT = "rd_force_extra_craft";
   public static final float EXTRA_FIGHTER_DURATION = 30.0F;
   private static final String DATA_KEY = "specialsphmod_dimensional_reservedeployment_data";
   private static final float SUBSYSTEM_CD = 60.0F;
   private static final float SUBSYSTEM_BUFF_DURATION = 30.0F;
   public boolean cooldownActive = false;
   public final String ID;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_DIMENSIONALNANOFORGE_EXTRA_EFFECT;
   float fighterRefitTimeReduction = SUPlugin.CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_REFIT_TIME_REDUCTION;
   float fighterHullArmorBonus = SUPlugin.CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_HULL_ARMOR_BONUS;
   float fighterWeaponAmmoBonus = SUPlugin.CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_WEAPON_AMMO_BONUS;
   String KEYPRESS_KEY;
   boolean toggleGeneralIncompat;
   boolean isUAFPresent = Global.getSettings().getModManager().isModEnabled("uaf");
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private float bbplus_seraphdampercddata(ShipAPI ship) {
      return 60.0F;
   }

   public SUAuroranDimensionalNanoforgeUpgrades() {
      this.ID = "SUAuroranDimensionalNanoforgeUpgrades";
   }

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_uaf_dimensionalnanoforge_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return this.isUAFPresent ? Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("uaf_dimen_nanoforge", null), null) : null;
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_dimen_nanoforge").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_dimen_nanoforge").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_dimen_nanoforge").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.fighterRefitTimeReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_REFIT_TIME_REDUCTION");
      }

      if (this.enableCustomSM) {
         stats.getFighterRefitTimeMult().modifyMult(id, 1.0F - this.fighterRefitTimeReduction / 100.0F);
      } else if (!this.enableCustomSM) {
         stats.getFighterRefitTimeMult().modifyMult(id, 0.8F);
      }
   }

   public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.fighterHullArmorBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_HULL_ARMOR_BONUS");
         this.fighterWeaponAmmoBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_WEAPON_AMMO_BONUS");
      }

      if (this.enableCustomSM) {
         fighter.getMutableStats().getHullBonus().modifyPercent(id, this.fighterHullArmorBonus);
         fighter.getMutableStats().getArmorBonus().modifyPercent(id, this.fighterHullArmorBonus);
         fighter.getMutableStats().getBallisticAmmoBonus().modifyPercent(id, this.fighterWeaponAmmoBonus);
         fighter.getMutableStats().getEnergyAmmoBonus().modifyPercent(id, this.fighterWeaponAmmoBonus);
      } else if (!this.enableCustomSM) {
         fighter.getMutableStats().getHullBonus().modifyPercent(id, 30.0F);
         fighter.getMutableStats().getArmorBonus().modifyPercent(id, 30.0F);
         fighter.getMutableStats().getBallisticAmmoBonus().modifyPercent(id, 50.0F);
         fighter.getMutableStats().getEnergyAmmoBonus().modifyPercent(id, 50.0F);
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
               SUHullmodDisplayBlockScript.showBlocked(ship);
            }
         }
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_uaf_dimensionalnanoforge_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_uaf_dimensionalnanoforge_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_uaf_dimensionalnanoforge_upgrades");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      String subsysHotkey;
      if (SUPlugin.HASLUNALIB) {
         this.KEYPRESS_KEY = LunaSettings.getString("mayu_specialupgrades", "shu_subsystemHotkey_uaf_dimensional_nanoforge");
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableDimensionalNanoforgeFRSToggle");
         if (this.KEYPRESS_KEY.equals("LMENU")) {
            subsysHotkey = "LEFT ALT";
         } else {
            subsysHotkey = this.KEYPRESS_KEY;
         }
      } else {
         this.KEYPRESS_KEY = SUPlugin.KEYPRESS_UAF_DIMENSIONAL_NANOFORGE;
         if (this.KEYPRESS_KEY.equals("LMENU")) {
            subsysHotkey = "LEFT ALT";
         } else {
            subsysHotkey = this.KEYPRESS_KEY;
         }
      }

      if (!this.disableExtraEffect) {
         CombatEngineAPI engine = Global.getCombatEngine();
         if (engine == null || !engine.isEntityInPlay(ship) || !ship.isAlive() || ship.getParentStation() != null) {
            return;
         }

         String key = "specialsphmod_dimensional_reservedeployment_data_" + ship.getId();
         SUAuroranDimensionalNanoforgeUpgrades.specialsphmod_fighterdamper_data data = (SUAuroranDimensionalNanoforgeUpgrades.specialsphmod_fighterdamper_data)engine.getCustomData()
            .get(key);
         if (data == null) {
            data = new SUAuroranDimensionalNanoforgeUpgrades.specialsphmod_fighterdamper_data();
            engine.getCustomData().put(key, data);
         }

         if (!data.runOnce) {
            data.runOnce = true;
            data.buffId = this.getClass().getName() + "_" + ship.getId();
            data.maxcooldown = this.bbplus_seraphdampercddata(ship);
            data.maxActiveTime = 30.0F;
         }

         if (data.cooldown < data.maxcooldown && data.activeTime <= 0.0F && ship.getCurrentCR() > 0.0F && !ship.getFluxTracker().isOverloadedOrVenting()) {
            data.cooldown += amount;
         }

         if (data.activeTime > 0.0F) {
            data.activeTime -= amount;
            if (data.activeTime > 0.0F) {
            }

            for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
               if (bay.getWing() != null) {
                  bay.makeCurrentIntervalFast();
                  FighterWingSpecAPI speccy = bay.getWing().getSpec();
                  int addForWing = getAdditionalFor(speccy);
                  int maxTotal = speccy.getNumFighters() + addForWing;
                  int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
                  actualAdd = Math.min(speccy.getNumFighters(), actualAdd);
                  if (actualAdd > 0) {
                     bay.setFastReplacements(bay.getFastReplacements() + addForWing);
                     bay.setExtraDeployments(actualAdd);
                     bay.setExtraDeploymentLimit(maxTotal);
                     bay.setExtraDuration(30.0F);
                  }
               }
            }
         }

         if (engine.getPlayerShip() == ship) {
            if (data.activeTime > 0.0F) {
               MagicUI.drawHUDStatusBar(
                  ship, data.activeTime / data.maxActiveTime, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), 0.0F, "Charge", "", false
               );
               engine.maintainStatusForPlayerShip(
                  data.buffId, Global.getSettings().getSpriteName("tooltips", "fighter_autoforge"), "Fighter Autoforge", "Extra fighters deployed", false
               );
            } else {
               if (data.cooldown >= data.maxcooldown) {
                  engine.maintainStatusForPlayerShip(
                     data.buffId,
                     Global.getSettings().getSpriteName("tooltips", "fighter_autoforge"),
                     "Fighter Autoforge",
                     "Press " + subsysHotkey + " to activate",
                     true
                  );
               } else {
                  engine.maintainStatusForPlayerShip(
                     data.buffId,
                     Global.getSettings().getSpriteName("tooltips", "fighter_autoforge"),
                     "Fighter Autoforge",
                     "Charging: " + Math.round(data.cooldown / data.maxcooldown * 100.0F) + "%",
                     false
                  );
               }

               MagicUI.drawHUDStatusBar(
                  ship, data.cooldown / data.maxcooldown, Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor(), 0.0F, "Charge", "", false
               );
            }

            if (Keyboard.isKeyDown(Keyboard.getKeyIndex(this.KEYPRESS_KEY)) && data.cooldown >= data.maxcooldown) {
               data.activeTime = data.maxActiveTime;
               data.cooldown = 0.0F;
               Global.getSoundPlayer().playSound("system_reserve_wing", 1.1F, 0.4F, ship.getLocation(), ship.getVelocity());
            }

            data.holdButtonBefore = Keyboard.isKeyDown(Keyboard.getKeyIndex(this.KEYPRESS_KEY));
         }

         boolean player = false;
         player = ship == Global.getCombatEngine().getPlayerShip();
         if (ship.getAI() != null) {
            ShipwideAIFlags flags = ship.getAIFlags();
            data.tracker.advance(amount);
            if (data.tracker.intervalElapsed()
               && (!player || player)
               && data.cooldown >= data.maxcooldown
               && (flags.hasFlag(AIFlags.CARRIER_FIGHTER_TARGET) || flags.hasFlag(AIFlags.IN_ATTACK_RUN) || flags.hasFlag(AIFlags.HARASS_MOVE_IN))) {
               data.activeTime = data.maxActiveTime;
               data.cooldown = 0.0F;
               Global.getSoundPlayer().playSound("system_reserve_wing", 1.1F, 0.7F, ship.getLocation(), ship.getVelocity());
            }
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (ship.getMutableStats().getNumFighterBays().getBaseValue() <= 0.0F) {
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
         if (ship.getMutableStats().getNumFighterBays().getBaseValue() <= 0.0F) {
            return "Ship does not have standard fighter bays";
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
      if (status == 0) {
         return false;
      } else {
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasSpecialItem("uaf_dimen_nanoforge")
            ? false
            : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
      }
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      if (status == 0) {
         return "This installation is not applicable to modules";
      } else {
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasCommodity("uaf_dimen_nanoforge")
            ? "Installation requires [Auroran Dimensional Nanoforge] (1)"
            : super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableDimensionalNanoforgeFRSToggle");
            this.fighterRefitTimeReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_REFIT_TIME_REDUCTION");
            this.fighterHullArmorBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_HULL_ARMOR_BONUS");
            this.fighterWeaponAmmoBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALNANOFORGE_FIGHTER_WEAPON_AMMO_BONUS");
         }

         String subsysHotkey;
         if (SUPlugin.HASLUNALIB) {
            this.KEYPRESS_KEY = LunaSettings.getString("mayu_specialupgrades", "shu_subsystemHotkey_uaf_dimensional_nanoforge");
            if (this.KEYPRESS_KEY.equals("LMENU")) {
               subsysHotkey = "LEFT ALT";
            } else {
               subsysHotkey = this.KEYPRESS_KEY;
            }
         } else {
            this.KEYPRESS_KEY = SUPlugin.KEYPRESS_UAF_DIMENSIONAL_NANOFORGE;
            if (this.KEYPRESS_KEY.equals("LMENU")) {
               subsysHotkey = "LEFT ALT";
            } else {
               subsysHotkey = this.KEYPRESS_KEY;
            }
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Reduces fighter refit time: %s\n• Increases fighter's hull & armor rating: %s\n• Increases fighter's weapon ammo count: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.fighterRefitTimeReduction) + "%",
                  Misc.getRoundedValue(this.fighterHullArmorBonus) + "%",
                  Misc.getRoundedValue(this.fighterWeaponAmmoBonus) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Reduces fighter refit time: %s\n• Increases fighter's hull & armor rating: %s\n• Increases fighter's weapon ammo count: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(20.0F) + "%", Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(50.0F) + "%"}
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Subsystem", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "fighter_autoforge"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Fighter Reinforcements",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Fighter Reinforcements"}
               );
               text2.addPara(
                  "Deploys additional %s wing to each fighter squadron for %s seconds to provide more fire support. When the gauge is full, press [%s] to activate the Fighter Reinforcements. The subsystem has a cooldown of %s seconds.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{"one", "30", subsysHotkey, "60"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
            }

            if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addPara(
                     "Press and hold [%s] to view the subsystem.",
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
            text.addPara(
               "Not compatible with %s", SUStringCodex.SHU_TOOLTIP_PADZERO, Misc.getNegativeHighlightColor(), new String[]{"Other Special Upgrade Hullmods"}
            );
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

   public static int getAdditionalFor(FighterWingSpecAPI spec) {
      int size = spec.getNumFighters();
      if (spec.isBomber() && !spec.hasTag("rd_force_extra_craft")) {
         return 1;
      } else if (spec.hasTag("rd_no_extra_craft")) {
         return 0;
      } else {
         return size <= 3 ? 1 : 1;
      }
   }

   private static class specialsphmod_fighterdamper_data {
      String buffId = "";
      boolean runOnce = false;
      boolean holdButtonBefore = false;
      float activeTime = 0.0F;
      float maxActiveTime = 0.0F;
      float cooldown = 0.0F;
      float maxcooldown = 0.0F;
      IntervalUtil tracker = new IntervalUtil(1.0F, 1.0F);
   }
}
