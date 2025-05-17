package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class SHUGACHALangelaanField extends BaseHullMod {
   private ShipAPI ship;
   protected Object STATUSKEY1;
   private final List<ShipAPI> debuffed;
   private static final String LANGELAAN_FIELD = "SHUGACHALangelaanField";
   private final SpriteAPI sprite = Global.getSettings().getSprite("misc", "langelaan_circle");
   private static final float DEBUFF_RANGE = 500.0F;
   private static final Color JITTER_COLOR = new Color(80, 20, 120, 205);
   private static final float ROTATION_SPEED = 13.0F;
   private static final float ROTATION_SPEED2 = 6.0F;
   private float rotation = 0.0F;
   private float opacity = 0.0F;
   private float rotation2 = 0.0F;
   private final float opacity2 = 0.0F;
   private static final Color COLOR = new Color(80, 15, 115, 200);
   private static final float EFFECT_RANGE = 600.0F;
   private static final float EFFECT_RANGE2 = 700.0F;
   private static final Color COLOR2 = new Color(55, 10, 95, 150);
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   public SHUGACHALangelaanField() {
      this.STATUSKEY1 = new Object();
      this.debuffed = new ArrayList<>();
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_langelaan_field")) {
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_langelaan_field")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_langelaan_field")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_langelaan_field");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (ship.getVariant().getSMods().contains("specialsphmod_gacha_langelaan_field")) {
         MutableShipStatsAPI stats = ship.getMutableStats();
         CombatEngineAPI engine = Global.getCombatEngine();
         this.ship = (ShipAPI)stats.getEntity();
         boolean visible = MagicRender.screenCheck(0.1F, this.ship.getLocation());
         List<ShipAPI> nearby = AIUtils.getNearbyEnemies(this.ship, 500.0F);
         List<ShipAPI> previous = new ArrayList<>(this.debuffed);
         Vector2f loc = ship.getLocation();
         ViewportAPI view = Global.getCombatEngine().getViewport();
         Vector2f loc2 = ship.getLocation();
         ViewportAPI view2 = Global.getCombatEngine().getViewport();
         this.rotation += 13.0F * amount;
         if (engine == null || !engine.isUIShowingHUD() || engine.isUIShowingDialog() || engine.getCombatUI().isShowingCommandUI()) {
            return;
         }

         if (Global.getCurrentState() != GameState.COMBAT || !ship.isAlive()) {
            return;
         }

         if (view2.isNearViewport(loc2, 700.0F)) {
            GL11.glPushAttrib(8192);
            GL11.glMatrixMode(5889);
            GL11.glPushMatrix();
            GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
            GL11.glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
            GL11.glEnable(3553);
            GL11.glEnable(3042);
            float scale2 = Global.getSettings().getScreenScaleMult();
            float radius2 = (700.0F + ship.getCollisionRadius()) * 2.0F * scale2 / view2.getViewMult();
            this.sprite.setSize(radius2, radius2);
            this.sprite.setColor(COLOR2);
            this.sprite.setAdditiveBlend();
            this.sprite.setAlphaMult(0.0F);
            this.sprite.renderAtCenter(view2.convertWorldXtoScreenX(loc2.x) * scale2, view2.convertWorldYtoScreenY(loc2.y) * scale2);
            this.sprite.setAngle(this.rotation2);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
         }

         if (view.isNearViewport(loc, 600.0F)) {
            GL11.glPushAttrib(8192);
            GL11.glMatrixMode(5889);
            GL11.glPushMatrix();
            GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
            GL11.glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
            GL11.glEnable(3553);
            GL11.glEnable(3042);
            float scale = Global.getSettings().getScreenScaleMult();
            float adjustedRange = ship.getMutableStats().getSystemRangeBonus().computeEffective(600.0F);
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
               "SHUGACHALangelaanField", Global.getSettings().getSpriteName("tooltips", "black_flies"), "Black Flies", "Debuffing nearby enemy ships", true
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
                  this.applyDebuff(affected, this.ship, 5.0F, visible);
                  this.debuffed.add(affected);
               }

               if (previous.contains(affected)) {
                  previous.remove(affected);
                  this.applyDebuff(affected, this.ship, 5.0F, visible);
               }
            }

            if (!previous.isEmpty()) {
               for (ShipAPI s : previous) {
                  this.debuffed.remove(s);
                  this.unapplyDebuff(s);
               }
            }
         } else if (!this.debuffed.isEmpty()) {
            for (ShipAPI affected : this.debuffed) {
               this.unapplyDebuff(affected);
            }

            this.debuffed.clear();
         }
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_langelaan_field")) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "banner_ssr"), 368.0F, 40.0F, 5.0F);
            if (SUPlugin.HASLUNALIB) {
               this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
            }

            if (this.enableCheatModeForRetards) {
               LabelAPI retardrius = tooltip.addPara("%s", 5.0F, Misc.getBrightPlayerColor(), new String[]{"Cheat Mode: ON"});
               retardrius.setAlignment(Alignment.MID);
               retardrius.italicize();
            }

            TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpriteName("tooltips", "black_flies"), SUStringCodex.SHU_TOOLTIP_IMG);
            text3.addPara(
               "Black Flies",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
               new String[]{"Black Flies"}
            );
            text3.addPara(
               "Enemy ships within %s will be plagued by cosmic pestilence and this is accompanied by the following negative effects in combat. These effects %s with another Black Flies.",
               SUStringCodex.SHU_TOOLTIP_PADZERO,
               Misc.getHighlightColor(),
               new String[]{Misc.getRoundedValue(600.0F) + "su", "will not stack"}
            );
            tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
            tooltip.addPara(
               "• Decreases max top speed: %s\n• Decreases manueverability: %s\n• Decreases weapon rate of fire: %s\n• Increases CR loss per second: %s\n• Increases damage taken by shields: %s\n• Increases overload duration: %s\n• Increases EMP damage taken: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_RED,
               new String[]{
                  Misc.getRoundedValue(20.0F),
                  Misc.getRoundedValue(20.0F) + "%",
                  Misc.getRoundedValue(30.0F) + "%",
                  Misc.getRoundedValue(20.0F) + "%",
                  Misc.getRoundedValue(30.0F) + "%",
                  Misc.getRoundedValue(30.0F) + "%",
                  Misc.getRoundedValue(50.0F) + "%"
               }
            );
         } else {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               Misc.getNegativeHighlightColor(),
               new String[]{"No bonus applied. The information will only appear if this hullmod is S-modded."}
            );
         }

         tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADQUOTE,
               SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
               new String[]{"\"In the end, I'll have you bastards know what it truly means to be inside the circle of providence.\""}
            )
            .italicize();
         tooltip.addPara("%s", SUStringCodex.SHU_TOOLTIP_PADSIG, SUStringCodex.SHU_TOOLTIP_QUOTECOLOR, new String[]{"         — Beelzebub, Kings Religion"});
      }
   }

   private void applyDebuff(ShipAPI ship, ShipAPI source, float level, boolean visible) {
      ship.setCircularJitter(visible);
      ship.setJitter(ship, JITTER_COLOR, 0.5F, 5, 5.0F);
      ship.setOverloadColor(JITTER_COLOR);
      ship.getMutableStats().getEmpDamageTakenMult().modifyMult(ship.getId(), 1.5F);
      ship.getMutableStats().getBallisticRoFMult().modifyPercent(ship.getId(), -30.0F);
      ship.getMutableStats().getEnergyRoFMult().modifyPercent(ship.getId(), -30.0F);
      ship.getMutableStats().getMissileRoFMult().modifyPercent(ship.getId(), -30.0F);
      ship.getMutableStats().getMaxSpeed().modifyFlat(ship.getId(), -20.0F);
      ship.getMutableStats().getAcceleration().modifyMult(ship.getId(), 0.8F);
      ship.getMutableStats().getDeceleration().modifyMult(ship.getId(), 0.8F);
      ship.getMutableStats().getTurnAcceleration().modifyMult(ship.getId(), 0.8F);
      ship.getMutableStats().getMaxTurnRate().modifyMult(ship.getId(), 0.8F);
      ship.getMutableStats().getShieldDamageTakenMult().modifyMult(ship.getId(), 1.3F);
      ship.getMutableStats().getShieldUpkeepMult().modifyMult(ship.getId(), 1.3F);
      ship.getMutableStats().getCRLossPerSecondPercent().modifyMult(ship.getId(), 1.2F);
      ship.getMutableStats().getOverloadTimeMod().modifyMult(ship.getId(), 1.3F);
   }

   private void unapplyDebuff(ShipAPI ship) {
      ship.getMutableStats().getEmpDamageTakenMult().unmodify(ship.getId());
      ship.getMutableStats().getBallisticRoFMult().unmodify(ship.getId());
      ship.getMutableStats().getEnergyRoFMult().unmodify(ship.getId());
      ship.getMutableStats().getMissileRoFMult().unmodify(ship.getId());
      ship.getMutableStats().getMaxSpeed().unmodify(ship.getId());
      ship.getMutableStats().getAcceleration().unmodify(ship.getId());
      ship.getMutableStats().getDeceleration().unmodify(ship.getId());
      ship.getMutableStats().getTurnAcceleration().unmodify(ship.getId());
      ship.getMutableStats().getMaxTurnRate().unmodify(ship.getId());
      ship.getMutableStats().getShieldDamageTakenMult().unmodify(ship.getId());
      ship.getMutableStats().getShieldUpkeepMult().unmodify(ship.getId());
      ship.getMutableStats().getCRLossPerSecondPercent().unmodify(ship.getId());
      ship.getMutableStats().getOverloadTimeMod().unmodify(ship.getId());
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SSR_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SSR_NAME;
   }
}
