package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.HullModItemManager;
import com.fs.starfarer.api.impl.combat.RecallDeviceStats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.input.Keyboard;
import org.magiclib.util.MagicUI;

public class SUAuroranGarrisonTransmitterUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX_COLLAB = "garrison_transmitter_shu_uaf_check_";
   public static final String ITEM = "uaf_garrison_transmitter";
   private static final String DATA_KEY = "specialsphmod_fighterdamper_data";
   private static final Object KEY_JITTER = new Object();
   private static final Color JITTER_COLOR = new Color(40, 255, 100, 200);
   private static final Color JITTER_UNDER_COLOR = new Color(30, 250, 90, 150);
   public static final float FTR_SPEED = 20.0F;
   public static final float FTR_DAMAGE = 20.0F;
   public static final float FTR_DAMAGE_TO_MISSILE = 30.0F;
   private static final float DAMPERCD = 30.0F;
   private static final float BONUS_BUFF_DURATION = 8.0F;
   public static final float EMP_DAMAGE_REDUCTION_DAMPER = 50.0F;
   public static final float DAMAGE_REDUCTION_DAMPER = 50.0F;
   public boolean cooldownActive = false;
   public final String ID;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_GARRISONTRANSMITTER_EXTRA_EFFECT;
   float fighterSpeedBonus = SUPlugin.CM_UAF_GARRISONTRANSMITTER_FIGHTER_SPEED_BONUS;
   float fighterDamageToShipBonus = SUPlugin.CM_UAF_GARRISONTRANSMITTER_FIGHTER_DAMAGE_TO_SHIP_BONUS;
   float fighterDamageToMissileBonus = SUPlugin.CM_UAF_GARRISONTRANSMITTER_FIGHTER_DAMAGE_TO_MISSILE_BONUS;
   String KEYPRESS_KEY;
   boolean toggleGeneralIncompat;
   boolean isUAFPresent = Global.getSettings().getModManager().isModEnabled("uaf");
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private float bbplus_seraphdampercddata(ShipAPI ship) {
      return 30.0F;
   }

   public SUAuroranGarrisonTransmitterUpgrades() {
      this.ID = "SUAuroranGarrisonTransmitterUpgrades";
   }

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_uaf_garrisontransmitter_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return this.isUAFPresent
         ? Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("uaf_garrison_transmitter", null), null)
         : null;
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_garrison_transmitter").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_garrison_transmitter").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_garrison_transmitter").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.fighterSpeedBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_GARRISONTRANSMITTER_FIGHTER_SPEED_BONUS");
         this.fighterDamageToShipBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_GARRISONTRANSMITTER_FIGHTER_DAMAGE_TO_SHIP_BONUS");
         this.fighterDamageToMissileBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_GARRISONTRANSMITTER_FIGHTER_DAMAGE_TO_MISSILE_BONUS");
      }

      if (this.enableCustomSM) {
         fighter.getMutableStats().getMaxSpeed().modifyFlat(id, this.fighterSpeedBonus);
         fighter.getMutableStats().getAcceleration().modifyPercent(id, this.fighterSpeedBonus);
         fighter.getMutableStats().getDamageToFrigates().modifyPercent(id, this.fighterDamageToShipBonus);
         fighter.getMutableStats().getDamageToDestroyers().modifyPercent(id, this.fighterDamageToShipBonus);
         fighter.getMutableStats().getDamageToCruisers().modifyPercent(id, this.fighterDamageToShipBonus);
         fighter.getMutableStats().getDamageToCapital().modifyPercent(id, this.fighterDamageToShipBonus);
         fighter.getMutableStats().getDamageToMissiles().modifyPercent(id, this.fighterDamageToMissileBonus);
      } else if (!this.enableCustomSM) {
         fighter.getMutableStats().getMaxSpeed().modifyFlat(id, 20.0F);
         fighter.getMutableStats().getAcceleration().modifyPercent(id, 20.0F);
         fighter.getMutableStats().getDamageToFrigates().modifyPercent(id, 20.0F);
         fighter.getMutableStats().getDamageToDestroyers().modifyPercent(id, 20.0F);
         fighter.getMutableStats().getDamageToCruisers().modifyPercent(id, 20.0F);
         fighter.getMutableStats().getDamageToCapital().modifyPercent(id, 20.0F);
         fighter.getMutableStats().getDamageToMissiles().modifyPercent(id, 30.0F);
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

      if (ship.getVariant().getSMods().contains("specialsphmod_uaf_garrisontransmitter_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_uaf_garrisontransmitter_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_uaf_garrisontransmitter_upgrades");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      String subsysHotkey;
      if (SUPlugin.HASLUNALIB) {
         this.KEYPRESS_KEY = LunaSettings.getString("mayu_specialupgrades", "shu_subsystemHotkey_uaf_interplanetary_ricecooker");
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableGarrisonTransmitterFDCToggle");
         if (this.KEYPRESS_KEY.equals("LMENU")) {
            subsysHotkey = "LEFT ALT";
         } else {
            subsysHotkey = this.KEYPRESS_KEY;
         }
      } else {
         this.KEYPRESS_KEY = SUPlugin.KEYPRESS_UAF_GARRISON_TRANSMITTER;
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

         String key = "specialsphmod_fighterdamper_data_" + ship.getId();
         SUAuroranGarrisonTransmitterUpgrades.specialsphmod_fighterdamper_data data = (SUAuroranGarrisonTransmitterUpgrades.specialsphmod_fighterdamper_data)engine.getCustomData()
            .get(key);
         if (data == null) {
            data = new SUAuroranGarrisonTransmitterUpgrades.specialsphmod_fighterdamper_data();
            engine.getCustomData().put(key, data);
         }

         if (!data.runOnce) {
            data.runOnce = true;
            data.buffId = this.getClass().getName() + "_" + ship.getId();
            data.maxcooldown = this.bbplus_seraphdampercddata(ship);
            data.maxActiveTime = 8.0F;
         }

         if (data.cooldown < data.maxcooldown && data.activeTime <= 0.0F && ship.getCurrentCR() > 0.0F && !ship.getFluxTracker().isOverloadedOrVenting()) {
            data.cooldown += amount;
         }

         if (data.activeTime > 0.0F) {
            data.activeTime -= amount;
            if (data.activeTime > 0.0F) {
               Global.getSoundPlayer().playLoop("system_damper_loop", ship, 1.1F, 0.3F, ship.getLocation(), ship.getVelocity());
            }

            for (ShipAPI fighter : RecallDeviceStats.getFighters(ship)) {
               if (!fighter.isHulk()) {
                  MutableShipStatsAPI fStats = fighter.getMutableStats();
                  fStats.getHullDamageTakenMult().modifyMult(data.buffId, 0.5F);
                  fStats.getArmorDamageTakenMult().modifyMult(data.buffId, 0.5F);
                  fStats.getShieldDamageTakenMult().modifyMult(data.buffId, 0.5F);
                  fStats.getEmpDamageTakenMult().modifyMult(data.buffId, 0.5F);
                  fighter.setJitter(KEY_JITTER, JITTER_COLOR, 1.0F, 2, 0.0F, 6.0F);
                  fighter.setJitterUnder(KEY_JITTER, JITTER_UNDER_COLOR, 1.0F, 2, 0.0F, 12.0F);
                  Global.getSoundPlayer().playLoop("system_damper_loop", ship, 0.5F, 0.5F, fighter.getLocation(), fighter.getVelocity());
               }
            }
         } else {
            for (ShipAPI fighterx : RecallDeviceStats.getFighters(ship)) {
               if (!fighterx.isHulk()) {
                  MutableShipStatsAPI fStats = fighterx.getMutableStats();
                  fStats.getHullDamageTakenMult().unmodifyMult(data.buffId);
                  fStats.getArmorDamageTakenMult().unmodifyMult(data.buffId);
                  fStats.getShieldDamageTakenMult().unmodifyMult(data.buffId);
                  fStats.getEmpDamageTakenMult().unmodifyMult(data.buffId);
               }
            }
         }

         if (engine.getPlayerShip() == ship) {
            if (data.activeTime > 0.0F) {
               MagicUI.drawHUDStatusBar(
                  ship, data.activeTime / data.maxActiveTime, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), 0.0F, "Charge", "", false
               );
               engine.maintainStatusForPlayerShip(
                  data.buffId,
                  Global.getSettings().getSpriteName("tooltips", "damper_transmitter"),
                  "Fighter Damper Field",
                  "Fighters takes less damage",
                  false
               );
            } else {
               if (data.cooldown >= data.maxcooldown) {
                  engine.maintainStatusForPlayerShip(
                     data.buffId,
                     Global.getSettings().getSpriteName("tooltips", "damper_transmitter"),
                     "Fighter Damper Field",
                     "Press " + subsysHotkey + " to activate",
                     true
                  );
               } else {
                  engine.maintainStatusForPlayerShip(
                     data.buffId,
                     Global.getSettings().getSpriteName("tooltips", "damper_transmitter"),
                     "Fighter Damper Field",
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
               Global.getSoundPlayer().playSound("system_damper", 1.1F, 0.4F, ship.getLocation(), ship.getVelocity());
            }

            data.holdButtonBefore = Keyboard.isKeyDown(Keyboard.getKeyIndex(this.KEYPRESS_KEY));
         }

         boolean player = false;
         player = ship == Global.getCombatEngine().getPlayerShip();
         if (ship.getAI() != null) {
            ShipwideAIFlags flags = ship.getAIFlags();
            data.tracker.advance(amount);
            if (data.tracker.intervalElapsed() && (!player || player) && data.cooldown >= data.maxcooldown) {
               data.missiledetected = 0.0F;

               for (MissileAPI missile : CombatUtils.getMissilesWithinRange(
                  ship.getLocation(), ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(50.0F, 100.0F)
               )) {
                  if (missile.getOwner() != ship.getOwner() && !missile.isMine()) {
                     float scale = 1.0F;
                     switch (missile.getDamageType()) {
                        case FRAGMENTATION:
                           scale = 0.3F;
                           break;
                        case KINETIC:
                           scale = 0.85F;
                           break;
                        case HIGH_EXPLOSIVE:
                           scale = 1.7F;
                        case ENERGY:
                     }

                     data.missiledetected = data.missiledetected + missile.getDamageAmount() * scale;
                  }
               }

               if (flags.hasFlag(AIFlags.CARRIER_FIGHTER_TARGET) || flags.hasFlag(AIFlags.IN_ATTACK_RUN) || data.missiledetected >= 1000.0F) {
                  data.activeTime = data.maxActiveTime;
                  data.cooldown = 0.0F;
                  Global.getSoundPlayer().playSound("system_damper", 1.1F, 0.7F, ship.getLocation(), ship.getVelocity());
               }
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
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasSpecialItem("uaf_garrison_transmitter")
            ? false
            : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
      }
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      if (status == 0) {
         return "This installation is not applicable to modules";
      } else {
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasCommodity("uaf_garrison_transmitter")
            ? "Installation requires [Auroran Garrison Transmitter] (1)"
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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableGarrisonTransmitterFDCToggle");
            this.fighterSpeedBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_GARRISONTRANSMITTER_FIGHTER_SPEED_BONUS");
            this.fighterDamageToShipBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_GARRISONTRANSMITTER_FIGHTER_DAMAGE_TO_SHIP_BONUS");
            this.fighterDamageToMissileBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_GARRISONTRANSMITTER_FIGHTER_DAMAGE_TO_MISSILE_BONUS");
         }

         String subsysHotkey;
         if (SUPlugin.HASLUNALIB) {
            this.KEYPRESS_KEY = LunaSettings.getString("mayu_specialupgrades", "shu_subsystemHotkey_uaf_garrison_transmitter");
            if (this.KEYPRESS_KEY.equals("LMENU")) {
               subsysHotkey = "LEFT ALT";
            } else {
               subsysHotkey = this.KEYPRESS_KEY;
            }
         } else {
            this.KEYPRESS_KEY = SUPlugin.KEYPRESS_UAF_GARRISON_TRANSMITTER;
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
               "• Increases fighter speed: %s\n• Increases fighter's damage dealt to ships: %s\n• Increases fighter's damage dealt to missiles: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.fighterSpeedBonus),
                  Misc.getRoundedValue(this.fighterDamageToShipBonus) + "%",
                  Misc.getRoundedValue(this.fighterDamageToMissileBonus) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases fighter speed: %s\n• Increases fighter's damage dealt to ships: %s\n• Increases fighter's damage dealt to missiles: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(20.0F), Misc.getRoundedValue(20.0F) + "%", Misc.getRoundedValue(30.0F) + "%"}
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Subsystem", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "damper_transmitter"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Fighter Damper Conduit",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Fighter Damper Conduit"}
               );
               text2.addPara(
                  "Each fighter constructed by this ship's autofactory has a special built-in damper conduits that can be activated by the mothership. When the gauge is full, press [%s] to activate the fighter's damper field. It has a duration of %s seconds and has a cooldown of %s seconds.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{subsysHotkey, Misc.getRoundedValue(8.0F), Misc.getRoundedValue(30.0F)}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• Fighter's shield damage taken reduced: %s\n• Fighter's hull damage taken reduced: %s",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(50.0F) + "%"}
               );
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

   private static class specialsphmod_fighterdamper_data {
      String buffId = "";
      boolean runOnce = false;
      boolean holdButtonBefore = false;
      float activeTime = 0.0F;
      float maxActiveTime = 0.0F;
      float cooldown = 0.0F;
      float maxcooldown = 0.0F;
      float missiledetected = 0.0F;
      IntervalUtil tracker = new IntervalUtil(1.0F, 1.0F);
   }
}
