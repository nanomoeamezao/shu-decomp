package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.HullModItemManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class SUAuroranInterplanetaryAccessRouterUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX_COLLAB = "accessrouter_shu_uaf_check_";
   public static final String ITEM = "uaf_access_router";
   public static final float FIGHTER_RANGE_BONUS = 10.0F;
   public static final float SIGHT_BONUS = 50.0F;
   public static final float TARGET_LEADING_BONUS = 50.0F;
   private final String ID;
   private final SpriteAPI sprite = Global.getSettings().getSprite("misc", "disruptor_field");
   private ShipAPI ship;
   protected Object STATUSKEY1;
   private final List<ShipAPI> debuffed;
   private static final float ROTATION_SPEED = 13.0F;
   private static final float ROTATION_SPEED2 = 6.0F;
   private float rotation = 0.0F;
   private float opacity = 0.0F;
   private float rotation2 = 0.0F;
   private final float opacity2 = 0.0F;
   private static final Color COLOR = new Color(200, 15, 15, 200);
   private static final Color COLOR2 = new Color(255, 10, 10, 150);
   private static final Color JITTER_COLOR = new Color(200, 20, 20, 205);
   private static final float DEBUFF_RANGE = 1200.0F;
   private static final float EFFECT_RANGE = 1150.0F;
   private static final float EFFECT_RANGE2 = 1150.0F;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_INTERPLANETARYACCESSROUTER_EXTRA_EFFECT;
   float fighterEngagementRangeBonus = SUPlugin.CM_UAF_INTERPLANETARYACCESSROUTER_FIGHTER_ENGAGEMENT_RANGE_BONUS;
   float sightCombatBonus = SUPlugin.CM_UAF_INTERPLANETARYACCESSROUTER_SIGHT_COMBAT_BONUS;
   float fighterTargetLeadBonus = SUPlugin.CM_UAF_INTERPLANETARYACCESSROUTER_FIGHTER_TARGET_LEAD_BONUS;
   boolean toggleGeneralIncompat;
   boolean isUAFPresent = Global.getSettings().getModManager().isModEnabled("uaf");
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_uaf_interplanetaryaccessrouter_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public SUAuroranInterplanetaryAccessRouterUpgrades() {
      this.ID = "SUAuroranInterplanetaryAccessRouterUpgrades";
      this.STATUSKEY1 = new Object();
      this.debuffed = new ArrayList<>();
   }

   public CargoStackAPI getRequiredItem() {
      return this.isUAFPresent ? Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("uaf_access_router", null), null) : null;
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_access_router").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_access_router").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_access_router").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.fighterEngagementRangeBonus = LunaSettings.getFloat(
            "mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYACCESSROUTER_FIGHTER_ENGAGEMENT_RANGE_BONUS"
         );
         this.sightCombatBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYACCESSROUTER_SIGHT_COMBAT_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getFighterWingRange().modifyPercent(id, this.fighterEngagementRangeBonus);
         stats.getSightRadiusMod().modifyPercent(id, this.sightCombatBonus);
      } else if (!this.enableCustomSM) {
         stats.getFighterWingRange().modifyPercent(id, 10.0F);
         stats.getSightRadiusMod().modifyPercent(id, 50.0F);
      }
   }

   public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.fighterTargetLeadBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYACCESSROUTER_FIGHTER_TARGET_LEAD_BONUS");
      }

      if (this.enableCustomSM) {
         fighter.getMutableStats().getAutofireAimAccuracy().modifyFlat(id, this.fighterTargetLeadBonus * 0.01F);
      } else if (!this.enableCustomSM) {
         fighter.getMutableStats().getAutofireAimAccuracy().modifyFlat(id, 0.5F);
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

      if (ship.getVariant().getSMods().contains("specialsphmod_uaf_interplanetaryaccessrouter_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_uaf_interplanetaryaccessrouter_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_uaf_interplanetaryaccessrouter_upgrades");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableInterplanetaryAccessRouterFDFToggle");
      }

      if (!this.disableExtraEffect) {
         MutableShipStatsAPI stats = ship.getMutableStats();
         CombatEngineAPI engine = Global.getCombatEngine();
         this.ship = (ShipAPI)stats.getEntity();
         boolean visible = MagicRender.screenCheck(0.1F, this.ship.getLocation());
         List<ShipAPI> nearby = AIUtils.getNearbyEnemies(this.ship, 1200.0F);
         List<ShipAPI> previous = new ArrayList<>(this.debuffed);
         Vector2f loc = ship.getLocation();
         ViewportAPI view = Global.getCombatEngine().getViewport();
         Vector2f loc2 = ship.getLocation();
         ViewportAPI view2 = Global.getCombatEngine().getViewport();
         this.rotation += 13.0F * amount;
         if (!ship.isAlive() || ship.isHulk()) {
            return;
         }

         if (engine != null
            && Global.getCurrentState() == GameState.COMBAT
            && ship.isAlive()
            && !engine.isUIShowingDialog()
            && !engine.getCombatUI().isShowingCommandUI()) {
            if (view2.isNearViewport(loc2, 1150.0F)) {
               GL11.glPushAttrib(8192);
               GL11.glMatrixMode(5889);
               GL11.glPushMatrix();
               GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
               GL11.glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
               GL11.glEnable(3553);
               GL11.glEnable(3042);
               float scale2 = Global.getSettings().getScreenScaleMult();
               float radius2 = (1150.0F + ship.getCollisionRadius()) * 2.0F * scale2 / view2.getViewMult();
               this.sprite.setSize(radius2, radius2);
               this.sprite.setColor(COLOR2);
               this.sprite.setAdditiveBlend();
               this.sprite.setAlphaMult(0.0F);
               this.sprite.renderAtCenter(view2.convertWorldXtoScreenX(loc2.x) * scale2, view2.convertWorldYtoScreenY(loc2.y) * scale2);
               this.sprite.setAngle(this.rotation2);
               GL11.glPopMatrix();
               GL11.glPopAttrib();
            }

            if (view.isNearViewport(loc, 1150.0F)) {
               GL11.glPushAttrib(8192);
               GL11.glMatrixMode(5889);
               GL11.glPushMatrix();
               GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
               GL11.glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
               GL11.glEnable(3553);
               GL11.glEnable(3042);
               float scale = Global.getSettings().getScreenScaleMult();
               float adjustedRange = ship.getMutableStats().getSystemRangeBonus().computeEffective(1150.0F);
               float radius = (adjustedRange + ship.getCollisionRadius()) * 2.0F * scale / view.getViewMult();
               this.sprite.setSize(radius, radius);
               this.sprite.setColor(COLOR);
               this.sprite.setAdditiveBlend();
               this.sprite.setAlphaMult(0.4F * this.opacity);
               this.sprite.renderAtCenter(view.convertWorldXtoScreenX(loc.x) * scale, view.convertWorldYtoScreenY(loc.y) * scale);
               this.sprite.setAngle(this.rotation);
               GL11.glPopMatrix();
               GL11.glPopAttrib();
            }

            if (this.rotation > 360.0F) {
               this.rotation -= 360.0F;
            }

            this.rotation2 += 6.0F * amount;
            if (this.rotation2 > 360.0F) {
               this.rotation2 -= 360.0F;
            }

            Global.getCombatEngine()
               .maintainStatusForPlayerShip(
                  this.ID,
                  Global.getSettings().getSpriteName("tooltips", "flight_disruption_field"),
                  "Flight Disruption Field",
                  "-30% mobility to enemy fighters",
                  true
               );
            Global.getCombatEngine()
               .maintainStatusForPlayerShip(
                  this.ID,
                  Global.getSettings().getSpriteName("tooltips", "flight_disruption_field"),
                  "Flight Disruption Field",
                  "+40% damage taken by enemy fighters",
                  true
               );
            if (!ship.isHulk() && !ship.isPiece() && ship.isAlive()) {
               this.opacity = Math.min(1.0F, this.opacity + 4.0F * amount);
            } else {
               this.opacity = Math.max(0.0F, this.opacity - 2.0F * amount);
            }

            if (ship.isPhased()) {
               this.opacity = Math.max(0.5F, this.opacity - 2.0F * amount);
            } else {
               this.opacity = Math.min(1.0F, this.opacity + 4.0F * amount);
            }

            if (!nearby.isEmpty()) {
               for (ShipAPI affected : nearby) {
                  if (!previous.contains(affected)) {
                     if (!affected.isFighter()) {
                        continue;
                     }

                     this.applyDebuff(affected, this.ship, 5.0F, visible);
                     this.debuffed.add(affected);
                  }

                  if (previous.contains(affected) && affected.isFighter()) {
                     previous.remove(affected);
                     this.applyDebuff(affected, this.ship, 5.0F, visible);
                  }
               }

               if (!previous.isEmpty()) {
                  for (ShipAPI s : previous) {
                     if (s.isFighter()) {
                        this.debuffed.remove(s);
                        this.unapplyDebuff(s);
                     }
                  }
               }
            } else if (!this.debuffed.isEmpty()) {
               for (ShipAPI affected : this.debuffed) {
                  if (affected.isFighter()) {
                     this.unapplyDebuff(affected);
                  }
               }

               this.debuffed.clear();
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
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasSpecialItem("uaf_access_router")
            ? false
            : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
      }
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      if (status == 0) {
         return "This installation is not applicable to modules";
      } else {
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasCommodity("uaf_access_router")
            ? "Installation requires [Auroran Interplanetary Access Router] (1)"
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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableInterplanetaryAccessRouterFDFToggle");
            this.fighterEngagementRangeBonus = LunaSettings.getFloat(
               "mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYACCESSROUTER_FIGHTER_ENGAGEMENT_RANGE_BONUS"
            );
            this.sightCombatBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYACCESSROUTER_SIGHT_COMBAT_BONUS");
            this.fighterTargetLeadBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_INTERPLANETARYACCESSROUTER_FIGHTER_TARGET_LEAD_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases fighter's engagement range: %s\n• Increases ship's sight radius: %s\n• Increases fighter's target accuracy: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.fighterEngagementRangeBonus) + "%",
                  Misc.getRoundedValue(this.sightCombatBonus) + "%",
                  Misc.getRoundedValue(this.fighterTargetLeadBonus) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases fighter's engagement range: %s\n• Increases ship's sight radius: %s\n• Increases fighter's target accuracy: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(50.0F) + "%"}
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "flight_disruption_field"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Flight Disruption Field",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Flight Disruption Field"}
               );
               text2.addPara(
                  "Deploys a special ECM field that disrupts the flight coordination of all enemy fighters within %s. This reduces their mobility by %s and will receive additional %s damage taken. The effect does not stack with another Flight Disruption Field.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(1200.0F) + "su", Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(40.0F) + "%"}
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

   private void applyDebuff(ShipAPI ship, ShipAPI source, float level, boolean visible) {
      ship.setCircularJitter(visible);
      ship.setJitter(ship, JITTER_COLOR, 0.5F, 5, 5.0F);
      ship.setOverloadColor(JITTER_COLOR);
      ship.getMutableStats().getMaxSpeed().modifyFlat(ship.getId(), -30.0F);
      ship.getMutableStats().getAcceleration().modifyMult(ship.getId(), 0.70000005F);
      ship.getMutableStats().getDeceleration().modifyMult(ship.getId(), 0.70000005F);
      ship.getMutableStats().getTurnAcceleration().modifyMult(ship.getId(), 0.70000005F);
      ship.getMutableStats().getMaxTurnRate().modifyMult(ship.getId(), 0.70000005F);
      ship.getMutableStats().getShieldDamageTakenMult().modifyMult(ship.getId(), 1.4F);
      ship.getMutableStats().getHullDamageTakenMult().modifyMult(ship.getId(), 1.4F);
      ship.getMutableStats().getArmorDamageTakenMult().modifyMult(ship.getId(), 1.4F);
   }

   private void unapplyDebuff(ShipAPI ship) {
      ship.getMutableStats().getMaxSpeed().unmodify(ship.getId());
      ship.getMutableStats().getAcceleration().unmodify(ship.getId());
      ship.getMutableStats().getDeceleration().unmodify(ship.getId());
      ship.getMutableStats().getTurnAcceleration().unmodify(ship.getId());
      ship.getMutableStats().getMaxTurnRate().unmodify(ship.getId());
      ship.getMutableStats().getShieldDamageTakenMult().unmodify(ship.getId());
      ship.getMutableStats().getHullDamageTakenMult().unmodify(ship.getId());
      ship.getMutableStats().getArmorDamageTakenMult().unmodify(ship.getId());
   }
}
