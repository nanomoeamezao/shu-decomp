package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.FastTrig;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

public class SHUGACHAMikohimeBlessings extends BaseHullMod {
   public static final String HIMEHIME = "A hullmod tribute for the most helpful tech-miko in SS community!";
   private static final String DATA_KEY = "shu_himemiblessing_data";
   private static final Color COLORJITTER = new Color(255, 235, 40, 200);
   private static final Color COLORUNDER = new Color(255, 235, 40, 150);
   private final Color AFTER_IMAGE_COLOR = new Color(240, 230, 10, 255);
   private final IntervalUtil interval = new IntervalUtil(0.3F, 0.3F);
   private static final float BLESSINGCD = 30.0F;
   private static final float BONUS_BUFF_DURATION = 12.0F;
   public static final float BONUS_BUFF_PERCENT = 30.0F;
   public static final float FLUX_REDUC_PERCENT = 30.0F;
   public static final float SPEED_BONUS = 20.0F;
   public static final float MOVEMENT_BONUS = 20.0F;
   public static final float DAMAGE_REDUCTION = 25.0F;
   public static final float MALFUNCTION_REDUCTION = 0.5F;
   public static float DMOD_EFFECT_MULT = 0.3F;
   public static float DMOD_AVOID_CHANCE = 80.0F;
   public boolean cooldownActive = false;
   public final String ID;
   String KEYPRESS_KEY;
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   private float shu_mikohimeblessingcddata(ShipAPI ship) {
      return 30.0F;
   }

   public SHUGACHAMikohimeBlessings() {
      this.ID = "SHUGACHAMikohimeBlessings";
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_mikohime_blessings")) {
         stats.getWeaponMalfunctionChance().modifyMult(id, 0.5F);
         stats.getEngineMalfunctionChance().modifyMult(id, 0.5F);
         stats.getCriticalMalfunctionChance().modifyMult(id, 0.5F);
         stats.getDynamic().getStat("dmod_effect_mult").modifyMult(id, DMOD_EFFECT_MULT);
         stats.getDynamic().getMod("dmod_acquire_prob_mod").modifyMult(id, 1.0F - DMOD_AVOID_CHANCE * 0.01F);
         stats.getDynamic().getMod("individual_ship_recovery_mod").modifyFlat(id, 1000.0F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_mikohime_blessings")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_abyss_gazer")
         )) {
         ship.getVariant().removeMod("specialsphmod_mikohime_blessings");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (ship.getVariant().getSMods().contains("specialsphmod_mikohime_blessings")) {
         CombatEngineAPI engine = Global.getCombatEngine();
         String subsysHotkey;
         if (SUPlugin.HASLUNALIB) {
            this.KEYPRESS_KEY = LunaSettings.getString("mayu_specialupgrades", "shu_subsystemHotkey_mikohime_blessings");
            if (this.KEYPRESS_KEY.equals("LMENU")) {
               subsysHotkey = "LEFT ALT";
            } else {
               subsysHotkey = this.KEYPRESS_KEY;
            }
         } else {
            this.KEYPRESS_KEY = SUPlugin.KEYPRESS_HIMEMIKO;
            if (this.KEYPRESS_KEY.equals("LMENU")) {
               subsysHotkey = "LEFT ALT";
            } else {
               subsysHotkey = this.KEYPRESS_KEY;
            }
         }

         if (engine == null || !engine.isEntityInPlay(ship) || !ship.isAlive() || ship.getParentStation() != null) {
            return;
         }

         String key = "shu_himemiblessing_data_" + ship.getId();
         SHUGACHAMikohimeBlessings.shu_mikohimedata data = (SHUGACHAMikohimeBlessings.shu_mikohimedata)engine.getCustomData().get(key);
         if (data == null) {
            data = new SHUGACHAMikohimeBlessings.shu_mikohimedata();
            engine.getCustomData().put(key, data);
         }

         if (!data.runOnce) {
            data.runOnce = true;
            data.buffId = this.getClass().getName() + "_" + ship.getId();
            data.maxcooldown = this.shu_mikohimeblessingcddata(ship);
            data.maxActiveTime = 12.0F;
         }

         if (data.cooldown < data.maxcooldown && data.activeTime <= 0.0F && ship.getCurrentCR() > 0.0F && !ship.getFluxTracker().isOverloadedOrVenting()) {
            data.cooldown += amount;
         }

         if (data.activeTime > 0.0F) {
            data.activeTime -= amount;
            if (ship.getFluxTracker().isOverloadedOrVenting()) {
               data.activeTime = 0.0F;
            }

            ship.getMutableStats().getHullDamageTakenMult().modifyMult(data.buffId, 0.25F);
            ship.getMutableStats().getArmorDamageTakenMult().modifyMult(data.buffId, 0.25F);
            ship.getMutableStats().getEmpDamageTakenMult().modifyMult(data.buffId, 0.25F);
            ship.getMutableStats().getEnergyRoFMult().modifyPercent(data.buffId, 30.0F);
            ship.getMutableStats().getBallisticRoFMult().modifyPercent(data.buffId, 30.0F);
            ship.getMutableStats().getMaxSpeed().modifyFlat(data.buffId, 20.0F);
            ship.getMutableStats().getAcceleration().modifyPercent(data.buffId, 20.0F);
            ship.getMutableStats().getDeceleration().modifyPercent(data.buffId, 20.0F);
            ship.getMutableStats().getTurnAcceleration().modifyPercent(data.buffId, 20.0F);
            ship.getMutableStats().getMaxTurnRate().modifyPercent(data.buffId, 20.0F);
            ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyPercent(data.buffId, -30.0F);
            ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(data.buffId, -30.0F);
            ship.getMutableStats().getBeamWeaponFluxCostMult().modifyPercent(data.buffId, -30.0F);
            ship.getMutableStats().getMissileWeaponFluxCostMod().modifyPercent(data.buffId, -30.0F);
            ship.setJitter(ship.getId(), COLORJITTER, 1.0F, 1, 0.0F, 1.0F);
            if (ship.isPhased()) {
               ship.setDefenseDisabled(true);
            }

            this.interval.advance(engine.getElapsedInLastFrame());
            if (this.interval.intervalElapsed()) {
               SpriteAPI sprite = ship.getSpriteAPI();
               float offsetX = sprite.getWidth() / 2.0F - sprite.getCenterX();
               float offsetY = sprite.getHeight() / 2.0F - sprite.getCenterY();
               float trueOffsetX = (float)FastTrig.cos(Math.toRadians(ship.getFacing() - 90.0F)) * offsetX
                  - (float)FastTrig.sin(Math.toRadians(ship.getFacing() - 90.0F)) * offsetY;
               float trueOffsetY = (float)FastTrig.sin(Math.toRadians(ship.getFacing() - 90.0F)) * offsetX
                  + (float)FastTrig.cos(Math.toRadians(ship.getFacing() - 90.0F)) * offsetY;
               MagicRender.battlespace(
                  Global.getSettings().getSprite(ship.getHullSpec().getSpriteName()),
                  new Vector2f(ship.getLocation().getX() + trueOffsetX, ship.getLocation().getY() + trueOffsetY),
                  new Vector2f(0.0F, 0.0F),
                  new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()),
                  new Vector2f(0.0F, 0.0F),
                  ship.getFacing() - 90.0F,
                  0.0F,
                  this.AFTER_IMAGE_COLOR,
                  true,
                  0.0F,
                  0.0F,
                  0.0F,
                  0.0F,
                  0.0F,
                  0.1F,
                  0.1F,
                  1.0F,
                  CombatEngineLayers.BELOW_SHIPS_LAYER
               );
            }

            if (data.activeTime > 0.0F) {
               Global.getSoundPlayer().playLoop("sphum_asterian_blessing_loop", ship, 1.1F, 0.3F, ship.getLocation(), ship.getVelocity());
            }

            if (ship.getParentStation() == null && ship.getChildModulesCopy() != null && !ship.getChildModulesCopy().isEmpty()) {
               for (ShipAPI childModulesCopy : ship.getChildModulesCopy()) {
                  childModulesCopy.setJitterShields(false);
                  childModulesCopy.getMutableStats().getHullDamageTakenMult().modifyMult(data.buffId, 0.25F);
                  childModulesCopy.getMutableStats().getArmorDamageTakenMult().modifyMult(data.buffId, 0.25F);
                  childModulesCopy.getMutableStats().getEmpDamageTakenMult().modifyMult(data.buffId, 0.25F);
                  childModulesCopy.getMutableStats().getEnergyRoFMult().modifyPercent(data.buffId, 30.0F);
                  childModulesCopy.getMutableStats().getBallisticRoFMult().modifyPercent(data.buffId, 30.0F);
                  childModulesCopy.getMutableStats().getBallisticWeaponFluxCostMod().modifyPercent(data.buffId, -30.0F);
                  childModulesCopy.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(data.buffId, -30.0F);
                  childModulesCopy.getMutableStats().getBeamWeaponFluxCostMult().modifyPercent(data.buffId, -30.0F);
                  childModulesCopy.getMutableStats().getMissileWeaponFluxCostMod().modifyPercent(data.buffId, -30.0F);
                  childModulesCopy.setJitterUnder(childModulesCopy.getId(), COLORJITTER, 1.0F, 2, 0.0F, 2.0F);
                  childModulesCopy.setJitter(childModulesCopy.getId(), COLORUNDER, 1.0F, 1, 0.0F, 1.0F);
                  if (this.interval.intervalElapsed()) {
                     SpriteAPI spriteModule = childModulesCopy.getSpriteAPI();
                     float offsetX = spriteModule.getWidth() / 2.0F - spriteModule.getCenterX();
                     float offsetY = spriteModule.getHeight() / 2.0F - spriteModule.getCenterY();
                     float trueOffsetX = (float)FastTrig.cos(Math.toRadians(ship.getFacing() - 90.0F)) * offsetX
                        - (float)FastTrig.sin(Math.toRadians(ship.getFacing() - 90.0F)) * offsetY;
                     float trueOffsetY = (float)FastTrig.sin(Math.toRadians(ship.getFacing() - 90.0F)) * offsetX
                        + (float)FastTrig.cos(Math.toRadians(ship.getFacing() - 90.0F)) * offsetY;
                     MagicRender.battlespace(
                        Global.getSettings().getSprite(ship.getHullSpec().getSpriteName()),
                        new Vector2f(ship.getLocation().getX() + trueOffsetX, ship.getLocation().getY() + trueOffsetY),
                        new Vector2f(0.0F, 0.0F),
                        new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()),
                        new Vector2f(0.0F, 0.0F),
                        ship.getFacing() - 90.0F,
                        0.0F,
                        this.AFTER_IMAGE_COLOR,
                        true,
                        0.0F,
                        0.0F,
                        0.0F,
                        0.0F,
                        0.0F,
                        0.1F,
                        0.1F,
                        1.0F,
                        CombatEngineLayers.BELOW_SHIPS_LAYER
                     );
                  }
               }
            }
         } else {
            ship.getMutableStats().getHullDamageTakenMult().unmodifyMult(data.buffId);
            ship.getMutableStats().getArmorDamageTakenMult().unmodifyMult(data.buffId);
            ship.getMutableStats().getEmpDamageTakenMult().unmodifyMult(data.buffId);
            ship.getMutableStats().getEnergyRoFMult().unmodifyPercent(data.buffId);
            ship.getMutableStats().getBallisticRoFMult().unmodifyPercent(data.buffId);
            ship.getMutableStats().getMaxSpeed().unmodifyFlat(data.buffId);
            ship.getMutableStats().getAcceleration().unmodifyPercent(data.buffId);
            ship.getMutableStats().getDeceleration().unmodifyPercent(data.buffId);
            ship.getMutableStats().getTurnAcceleration().unmodifyPercent(data.buffId);
            ship.getMutableStats().getMaxTurnRate().unmodifyPercent(data.buffId);
            ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodifyPercent(data.buffId);
            ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodifyPercent(data.buffId);
            ship.getMutableStats().getBeamWeaponFluxCostMult().unmodifyPercent(data.buffId);
            ship.getMutableStats().getMissileWeaponFluxCostMod().unmodifyPercent(data.buffId);
            ship.setDefenseDisabled(false);
            if (ship.getParentStation() == null && ship.getChildModulesCopy() != null && !ship.getChildModulesCopy().isEmpty()) {
               for (ShipAPI childModulesCopyx : ship.getChildModulesCopy()) {
                  childModulesCopyx.setJitterShields(true);
                  childModulesCopyx.getMutableStats().getHullDamageTakenMult().unmodifyMult(data.buffId);
                  childModulesCopyx.getMutableStats().getArmorDamageTakenMult().unmodifyMult(data.buffId);
                  childModulesCopyx.getMutableStats().getEmpDamageTakenMult().unmodifyMult(data.buffId);
                  childModulesCopyx.getMutableStats().getEnergyRoFMult().unmodifyPercent(data.buffId);
                  childModulesCopyx.getMutableStats().getBallisticRoFMult().unmodifyPercent(data.buffId);
                  childModulesCopyx.getMutableStats().getBallisticWeaponFluxCostMod().unmodifyPercent(data.buffId);
                  childModulesCopyx.getMutableStats().getEnergyWeaponFluxCostMod().unmodifyPercent(data.buffId);
                  childModulesCopyx.getMutableStats().getBeamWeaponFluxCostMult().unmodifyPercent(data.buffId);
                  childModulesCopyx.getMutableStats().getMissileWeaponFluxCostMod().unmodifyPercent(data.buffId);
               }
            }
         }

         if (engine.getPlayerShip() == ship) {
            if (data.activeTime > 0.0F) {
               MagicUI.drawHUDStatusBar(
                  ship, data.activeTime / data.maxActiveTime, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), 0.0F, "Charge", "", false
               );
               engine.maintainStatusForPlayerShip(
                  data.buffId, Global.getSettings().getSpriteName("tooltips", "asterian_blessing"), "Asterian Blessings", "Combat parameters increased!", false
               );
            } else {
               if (data.cooldown >= data.maxcooldown) {
                  engine.maintainStatusForPlayerShip(
                     data.buffId,
                     Global.getSettings().getSpriteName("tooltips", "asterian_blessing"),
                     "Asterian Blessings",
                     "Press " + subsysHotkey + " to activate Asterian Blessings",
                     false
                  );
               } else {
                  engine.maintainStatusForPlayerShip(
                     data.buffId,
                     Global.getSettings().getSpriteName("tooltips", "asterian_blessing"),
                     "Asterian Blessings",
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
               Global.getSoundPlayer().playSound("sphum_asterian_blessing_activate", 1.1F, 0.4F, ship.getLocation(), ship.getVelocity());
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
               && (
                  flags.hasFlag(AIFlags.PURSUING)
                     || flags.hasFlag(AIFlags.BACK_OFF)
                     || flags.hasFlag(AIFlags.IN_ATTACK_RUN)
                     || ship.getAIFlags().hasFlag(AIFlags.HAS_INCOMING_DAMAGE)
               )) {
               data.activeTime = data.maxActiveTime;
               data.cooldown = 0.0F;
               Global.getSoundPlayer().playSound("sphum_asterian_blessing_activate", 1.1F, 0.7F, ship.getLocation(), ship.getVelocity());
            }
         }
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         String subsysHotkey;
         if (SUPlugin.HASLUNALIB) {
            this.KEYPRESS_KEY = LunaSettings.getString("mayu_specialupgrades", "shu_subsystemHotkey_mikohime_blessings");
            if (this.KEYPRESS_KEY.equals("LMENU")) {
               subsysHotkey = "LEFT ALT";
            } else {
               subsysHotkey = this.KEYPRESS_KEY;
            }
         } else {
            this.KEYPRESS_KEY = SUPlugin.KEYPRESS_HIMEMIKO;
            if (this.KEYPRESS_KEY.equals("LMENU")) {
               subsysHotkey = "LEFT ALT";
            } else {
               subsysHotkey = this.KEYPRESS_KEY;
            }
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_mikohime_blessings")) {
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
               "• Reduces chance of ship's critical malfunctions: %s\n• Reduces the negative effects of d-mod: %s\n• Reduced chance of acquiring new d-mod: %s\n• Always recoverable after being disabled or destroyed in combat.",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(70.0F) + "%", Misc.getRoundedValue(80.0F) + "%"}
            );
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Subsystem", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpriteName("tooltips", "stellar_miko"), SUStringCodex.SHU_TOOLTIP_IMG);
               text2.addPara(
                  "Asterian Blessings",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Asterian Blessings"}
               );
               text2.addPara(
                  "Bestows this vessel with blessings from the Stellar Priestess of Asteria. When the gauge is full, press [%s] to activate the blessing and gain various stat bonuses in combat (including modules). The buff will last for %s seconds and has a cooldown of %s seconds.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{subsysHotkey, Misc.getRoundedValue(12.0F), Misc.getRoundedValue(30.0F)}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• Decreases hull and armor damage taken: %s\n• Increases top speed: %s flat\n• Increases maneuverability: %s\n• Decreases EMP damage taken: %s\n• Decreases weapon flux cost: %s\n• Increases ballistic and energy RoF: %s",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{
                     Misc.getRoundedValue(20.0F) + "%",
                     Misc.getRoundedValue(20.0F),
                     Misc.getRoundedValue(20.0F) + "%",
                     Misc.getRoundedValue(25.0F) + "%",
                     Misc.getRoundedValue(30.0F) + "%",
                     Misc.getRoundedValue(30.0F) + "%"
                  }
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
         } else {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               Misc.getNegativeHighlightColor(),
               new String[]{"No bonus applied. The information will only appear if this hullmod is S-modded."}
            );
         }

         tooltip.addPara(
               "%s", SUStringCodex.SHU_TOOLTIP_PADQUOTE, SUStringCodex.SHU_TOOLTIP_QUOTECOLOR, new String[]{"\"May the stars be your guiding light...\""}
            )
            .italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{"         — Himemi, Current Stellar Priestess of Asteria"}
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SSR_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SSR_NAME;
   }

   private static class shu_mikohimedata {
      String buffId = "";
      float buffDurationRemaining = 0.0F;
      boolean runOnce = false;
      boolean holdButtonBefore = false;
      float activeTime = 0.0F;
      float maxActiveTime = 0.0F;
      float cooldown = 0.0F;
      float maxcooldown = 0.0F;
      IntervalUtil tracker = new IntervalUtil(1.0F, 1.0F);
   }
}
