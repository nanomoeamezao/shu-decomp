package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
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
import java.util.EnumSet;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicUI;

public class SUAuroranInterplanetaryRiceCookerUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX_COLLAB = "ricecooker_interplanetary_shu_uaf_check_";
   public static final String ITEM = "uaf_rice_cooker";
   public static int OP_REDUCTION = 3;
   public static float DAMAGE_BONUS_PERCENT = 30.0F;
   public static final float DAMAGE_BONUS_TO_FTRMSL = 50.0F;
   private static final String DATA_KEY = "specialsphmod_ricecooker_weaponoverdrive_data";
   private static final float SUBSYSTEM_CD = 40.0F;
   private static final float SUBSYSTEM_BUFF_DURATION = 5.0F;
   public boolean cooldownActive = false;
   public final String ID;
   public static boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   public static boolean disableExtraEffect = SUPlugin.DISABLE_INTERPLANETARYRICECOOKER_EXTRA_EFFECT;
   public static float OPCostReduction = SUPlugin.CM_UAF_INTERPLANETARYRICECOOKER_SMALL_MED_PD_WEAPON_REDUCTION;
   public static float PDWeaponDamageBonus = SUPlugin.CM_UAF_INTERPLANETARYRICECOOKER_PD_WEAPON_DAMAGE_BONUS;
   public static float damageToFightersMissileBonus = SUPlugin.CM_UAF_INTERPLANETARYRICECOOKER_DAMAGE_TO_FIGHTERS_MISSILES_BONUS;
   String KEYPRESS_KEY;
   boolean toggleGeneralIncompat;
   boolean isUAFPresent = Global.getSettings().getModManager().isModEnabled("uaf");
   private static final String[] ALL_INCOMPAT_IDS = new String[]{
      "sst_cnf_log",
      "sst_pnf_log",
      "sst_cnf_car",
      "sst_cnf_mis",
      "sst_cnf_arm",
      "sst_pnf_car",
      "sst_pnf_mis",
      "sst_pnf_arm",
      "specialsphmod_corruptednanoforge_upgrades",
      "specialsphmod_pristinenanoforge_upgrades",
      "specialsphmod_hypershunt_upgrades",
      "specialsphmod_catalyticcore_upgrades",
      "specialsphmod_plasmadynamo_upgrades",
      "specialsphmod_combatdronereplicator_upgrades",
      "specialsphmod_cryoarithmeticengine_upgrades",
      "specialsphmod_synchrotoncore_upgrades",
      "specialsphmod_mantlebore_upgrades",
      "specialsphmod_fusionlampreactor_upgrades",
      "specialsphmod_biofactoryembryo_upgrades",
      "specialsphmod_dealmakerholosuite_upgrades",
      "specialsphmod_fullerenespool_upgrades",
      "specialsphmod_soilnanites_upgrades",
      "specialsphmod_sfc_phasemote_upgrades",
      "specialsphmod_sfc_aquaticstimulator_upgrades",
      "specialsphmod_uaf_interplanetaryaccessrouter_upgrades",
      "specialsphmod_uaf_garrissontransmitter_upgrades",
      "specialsphmod_uaf_modularpurifier_upgrades",
      "specialsphmod_uaf_servosyncpump_upgrades",
      "specialsphmod_uaf_dimensionalstove_upgrades",
      "specialsphmod_uaf_dimensionalnanoforge_upgrades"
   };

   private float bbplus_seraphdampercddata(ShipAPI ship) {
      return 40.0F;
   }

   public SUAuroranInterplanetaryRiceCookerUpgrades() {
      this.ID = "SUAuroranInterplanetaryRiceCookerUpgrades";
   }

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_uaf_interplanetary_ricecooker_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return this.isUAFPresent ? Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("uaf_rice_cooker", null), null) : null;
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_rice_cooker").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_rice_cooker").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_rice_cooker").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         OPCostReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYRICECOOKER_SMALL_MED_PD_WEAPON_REDUCTION");
         damageToFightersMissileBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYRICECOOKER_DAMAGE_TO_FIGHTERS_MISSILES_BONUS");
      }

      if (enableCustomSM) {
         stats.getDynamic().getMod("medium_pd_mod").modifyFlat(id, -OPCostReduction);
         stats.getDynamic().getMod("small_pd_mod").modifyFlat(id, -OPCostReduction);
         stats.getDamageToFighters().modifyPercent(id, damageToFightersMissileBonus);
         stats.getDamageToMissiles().modifyPercent(id, damageToFightersMissileBonus);
      } else if (!enableCustomSM) {
         stats.getDynamic().getMod("medium_pd_mod").modifyFlat(id, -OP_REDUCTION);
         stats.getDynamic().getMod("small_pd_mod").modifyFlat(id, -OP_REDUCTION);
         stats.getDamageToFighters().modifyPercent(id, 50.0F);
         stats.getDamageToMissiles().modifyPercent(id, 50.0F);
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

      if (ship.getVariant().getSMods().contains("specialsphmod_uaf_interplanetary_ricecooker_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_uaf_interplanetary_ricecooker_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_uaf_interplanetary_ricecooker_upgrades");
      }

      ship.addListener(new SUAuroranInterplanetaryRiceCookerUpgrades.SHURiceCookerDamageDealtMod());
   }

   public boolean affectsOPCosts() {
      return true;
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      String subsysHotkey;
      if (SUPlugin.HASLUNALIB) {
         this.KEYPRESS_KEY = LunaSettings.getString("mayu_specialupgrades", "shu_subsystemHotkey_uaf_interplanetary_ricecooker");
         disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableInterplanetaryRiceCookerWOMToggle");
         if (this.KEYPRESS_KEY.equals("LMENU")) {
            subsysHotkey = "LEFT ALT";
         } else {
            subsysHotkey = this.KEYPRESS_KEY;
         }
      } else {
         this.KEYPRESS_KEY = SUPlugin.KEYPRESS_UAF_INTERPLANETARY_RICECOOKER;
         if (this.KEYPRESS_KEY.equals("LMENU")) {
            subsysHotkey = "LEFT ALT";
         } else {
            subsysHotkey = this.KEYPRESS_KEY;
         }
      }

      if (!disableExtraEffect) {
         CombatEngineAPI engine = Global.getCombatEngine();
         if (engine == null || !engine.isEntityInPlay(ship) || !ship.isAlive() || ship.getParentStation() != null) {
            return;
         }

         String key = "specialsphmod_ricecooker_weaponoverdrive_data_" + ship.getId();
         SUAuroranInterplanetaryRiceCookerUpgrades.specialsphmod_ricecookerweaponoverdrive_data data = (SUAuroranInterplanetaryRiceCookerUpgrades.specialsphmod_ricecookerweaponoverdrive_data)engine.getCustomData()
            .get(key);
         if (data == null) {
            data = new SUAuroranInterplanetaryRiceCookerUpgrades.specialsphmod_ricecookerweaponoverdrive_data();
            engine.getCustomData().put(key, data);
         }

         if (!data.runOnce) {
            data.runOnce = true;
            data.buffId = this.getClass().getName() + "_" + ship.getId();
            data.maxcooldown = this.bbplus_seraphdampercddata(ship);
            data.maxActiveTime = 5.0F;
         }

         if (data.cooldown < data.maxcooldown && data.activeTime <= 0.0F && ship.getCurrentCR() > 0.0F && !ship.getFluxTracker().isOverloadedOrVenting()) {
            data.cooldown += amount;
         }

         if (data.activeTime > 0.0F) {
            data.activeTime -= amount;
            if (data.activeTime > 0.0F) {
               Global.getSoundPlayer().playLoop("system_high_energy_focus_loop", ship, 1.1F, 0.3F, ship.getLocation(), ship.getVelocity());
            }

            EnumSet<WeaponType> WEAPON_TYPES = EnumSet.of(
               WeaponType.BALLISTIC, WeaponType.ENERGY, WeaponType.HYBRID, WeaponType.SYNERGY, WeaponType.UNIVERSAL, WeaponType.COMPOSITE
            );
            ship.setWeaponGlow(1.0F, new Color(105, 115, 215, 255), WEAPON_TYPES);
            ship.getMutableStats().getBallisticRoFMult().modifyPercent(data.buffId, 30.0F);
            ship.getMutableStats().getEnergyRoFMult().modifyPercent(data.buffId, 30.0F);
            ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyPercent(data.buffId, -25.0F);
            ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(data.buffId, -25.0F);
         } else {
            EnumSet<WeaponType> WEAPON_TYPES = EnumSet.of(
               WeaponType.BALLISTIC, WeaponType.ENERGY, WeaponType.HYBRID, WeaponType.SYNERGY, WeaponType.UNIVERSAL, WeaponType.COMPOSITE
            );
            ship.setWeaponGlow(0.0F, new Color(105, 115, 215, 255), WEAPON_TYPES);
            ship.getMutableStats().getBallisticRoFMult().unmodifyPercent(data.buffId);
            ship.getMutableStats().getEnergyRoFMult().unmodifyPercent(data.buffId);
            ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodifyPercent(data.buffId);
            ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodifyPercent(data.buffId);
         }

         if (engine.getPlayerShip() == ship) {
            if (data.activeTime > 0.0F) {
               MagicUI.drawHUDStatusBar(
                  ship, data.activeTime / data.maxActiveTime, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), 0.0F, "Charge", "", false
               );
               MagicUI.drawInterfaceStatusBar(
                  ship,
                  data.activeTime / data.maxActiveTime,
                  Misc.getNegativeHighlightColor(),
                  Misc.getNegativeHighlightColor(),
                  data.activeTime / data.maxActiveTime,
                  "Charge",
                  0
               );
               engine.maintainStatusForPlayerShip(
                  data.buffId,
                  Global.getSettings().getSpriteName("tooltips", "weapon_overdrive_mode"),
                  "Weapon Overdrive Mode",
                  "Increased rate of fire and reduced flux cost",
                  false
               );
            } else {
               if (data.cooldown >= data.maxcooldown) {
                  engine.maintainStatusForPlayerShip(
                     data.buffId,
                     Global.getSettings().getSpriteName("tooltips", "weapon_overdrive_mode"),
                     "Weapon Overdrive Mode",
                     "Press " + subsysHotkey + " to activate",
                     true
                  );
               } else {
                  engine.maintainStatusForPlayerShip(
                     data.buffId,
                     Global.getSettings().getSpriteName("tooltips", "weapon_overdrive_mode"),
                     "Weapon Overdrive Mode",
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
               Global.getSoundPlayer().playSound("system_ammo_feeder", 1.1F, 0.4F, ship.getLocation(), ship.getVelocity());
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
               && (flags.hasFlag(AIFlags.IN_ATTACK_RUN) || flags.hasFlag(AIFlags.HARASS_MOVE_IN))) {
               data.activeTime = data.maxActiveTime;
               data.cooldown = 0.0F;
               Global.getSoundPlayer().playSound("system_ammo_feeder", 1.1F, 0.7F, ship.getLocation(), ship.getVelocity());
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

         return this.toggleGeneralIncompat || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS);
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
               || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
                  && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
            ? super.getUnapplicableReason(ship)
            : "Only one type of special upgrade hullmod can be installed per ship";
      } else {
         return "Unable to locate ship!";
      }
   }

   public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      if (status == 0) {
         return false;
      } else {
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasSpecialItem("uaf_rice_cooker")
            ? false
            : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
      }
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      if (status == 0) {
         return "This installation is not applicable to modules";
      } else {
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasCommodity("uaf_rice_cooker")
            ? "Installation requires [Auroran Interplanetary Rice Cooker] (1)"
            : super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableInterplanetaryRiceCookerWOMToggle");
            OPCostReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYRICECOOKER_SMALL_MED_PD_WEAPON_REDUCTION");
            PDWeaponDamageBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYRICECOOKER_PD_WEAPON_DAMAGE_BONUS");
            damageToFightersMissileBonus = LunaSettings.getFloat(
               "mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYRICECOOKER_DAMAGE_TO_FIGHTERS_MISSILES_BONUS"
            );
         }

         String subsysHotkey;
         if (SUPlugin.HASLUNALIB) {
            this.KEYPRESS_KEY = LunaSettings.getString("mayu_specialupgrades", "shu_subsystemHotkey_uaf_interplanetary_ricecooker");
            if (this.KEYPRESS_KEY.equals("LMENU")) {
               subsysHotkey = "LEFT ALT";
            } else {
               subsysHotkey = this.KEYPRESS_KEY;
            }
         } else {
            this.KEYPRESS_KEY = SUPlugin.KEYPRESS_UAF_INTERPLANETARY_RICECOOKER;
            if (this.KEYPRESS_KEY.equals("LMENU")) {
               subsysHotkey = "LEFT ALT";
            } else {
               subsysHotkey = this.KEYPRESS_KEY;
            }
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Reduces OP cost of small & medium PD weapons: %s\n• Increases damage output of all PD weapons: %s\n• Increases damage dealt to fighters and missiles: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(OPCostReduction),
                  Misc.getRoundedValue(PDWeaponDamageBonus) + "%",
                  Misc.getRoundedValue(damageToFightersMissileBonus) + "%"
               }
            );
         } else if (!enableCustomSM) {
            tooltip.addPara(
               "• Reduces OP cost of small & medium PD weapons: %s\n• Increases damage output of all PD weapons: %s\n• Increases damage dealt to fighters and missiles: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(3.0F), Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(50.0F) + "%"}
            );
         }

         if (!disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Subsystem", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "weapon_overdrive_mode"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Weapon Overdrive Mode",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Weapon Overdrive Mode"}
               );
               text2.addPara(
                  "When the gauge is full, press [%s] to activate the Weapon Overdrive mode. This will increase the rate of fire of weapons by %s while reducing the flux cost by %s for %s seconds. The subsystem has a cooldown of %s seconds.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{subsysHotkey, "30%", "25%", "5", "40"}
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

   public static class SHURiceCookerDamageDealtMod implements DamageDealtModifier {
      public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
         WeaponAPI weapon = null;
         if (param instanceof DamagingProjectileAPI) {
            weapon = ((DamagingProjectileAPI)param).getWeapon();
         } else if (param instanceof BeamAPI) {
            weapon = ((BeamAPI)param).getWeapon();
         } else if (param instanceof MissileAPI) {
            weapon = ((MissileAPI)param).getWeapon();
         }

         if (weapon == null) {
            return null;
         } else if (!weapon.hasAIHint(AIHints.PD)) {
            return null;
         } else {
            String id = "shu_uaf_pd_dam_mod";
            if (SUPlugin.HASLUNALIB) {
               SUAuroranInterplanetaryRiceCookerUpgrades.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
               SUAuroranInterplanetaryRiceCookerUpgrades.PDWeaponDamageBonus = LunaSettings.getFloat(
                  "mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYRICECOOKER_PD_WEAPON_DAMAGE_BONUS"
               );
            }

            if (SUAuroranInterplanetaryRiceCookerUpgrades.enableCustomSM) {
               damage.getModifier().modifyPercent(id, SUAuroranInterplanetaryRiceCookerUpgrades.PDWeaponDamageBonus);
            } else if (!SUAuroranInterplanetaryRiceCookerUpgrades.enableCustomSM) {
               damage.getModifier().modifyPercent(id, SUAuroranInterplanetaryRiceCookerUpgrades.DAMAGE_BONUS_PERCENT);
            }

            return id;
         }
      }
   }

   private static class specialsphmod_ricecookerweaponoverdrive_data {
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
