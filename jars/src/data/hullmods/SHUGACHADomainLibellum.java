package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SHUGACHADomainLibellum extends BaseHullMod {
   private final Color SHIELD_RING_COLOR = new Color(255, 255, 255, 255);
   private final Color SHIELD_INNER_COLOR = new Color(255, 100, 255, 75);
   private static final float SHIELD_ABSORP_MULT = 41.0F;
   private static final float SHIELD_UPKEEP_BONUS = 70.0F;
   private static final float HEALTH_BONUS = 100.0F;
   private static final float DAMAGE_REDUCTION_PRIMA = 0.8F;
   private static final float DAMAGE_REDUCTION_SEGUNDA = 0.8F;
   private static final String DOMAIN_LIBELLUM = "SHUGACHADomainLibellum";
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;
   private static final Map armorbonus = new HashMap();

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_domain_libellum")) {
         stats.getArmorBonus().modifyFlat(id, (Float)armorbonus.get(hullSize));
         stats.getWeaponHealthBonus().modifyPercent(id, 100.0F);
         stats.getEngineHealthBonus().modifyPercent(id, 100.0F);
         stats.getFragmentationDamageTakenMult().modifyMult(id, 0.8F);
         stats.getHighExplosiveDamageTakenMult().modifyMult(id, 0.8F);
         stats.getKineticArmorDamageTakenMult().modifyMult(id, 0.8F);
         stats.getEnergyDamageTakenMult().modifyMult(id, 0.8F);
         stats.getBeamDamageTakenMult().modifyMult(id, 0.8F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_domain_libellum")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_domain_libellum")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_domain_libellum");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (ship.isAlive()) {
         MutableShipStatsAPI stats = ship.getMutableStats();
         if (ship.getShield() != null && ship.getShield().isOn()) {
            stats.getShieldAbsorptionMult().modifyMult("SHUGACHADomainLibellum", 0.59000003F);
            stats.getShieldUpkeepMult().modifyMult("SHUGACHADomainLibellum", 0.3F);
            ship.getShield().setRingColor(this.SHIELD_RING_COLOR);
            ship.getShield().setInnerColor(this.SHIELD_INNER_COLOR);
            Global.getCombatEngine()
               .maintainStatusForPlayerShip(
                  "SHUGACHADomainLibellum",
                  "graphics/icons/hullsys/fortress_shield.png",
                  "Scutum Domanii: Active",
                  "Increased shield efficiency & reduced upkeep",
                  false
               );
            Global.getSoundPlayer().playLoop("system_fortress_shield_loop", ship, 1.0F, 1.0F, ship.getLocation(), ship.getVelocity());
         } else {
            stats.getShieldAbsorptionMult().unmodify("SHUGACHADomainLibellum");
            stats.getShieldUpkeepMult().unmodify("SHUGACHADomainLibellum");
         }
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_domain_libellum")) {
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
               "• Increases engine and weapon health: %s\n• Increases ship's armor: %s/%s/%s/%s (by hull size)\n• Reduces HE and kinetic damage taken: %s\n• Reduces beam and energy damage taken: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(100.0F) + "%",
                  Misc.getRoundedValue(200.0F),
                  Misc.getRoundedValue(300.0F),
                  Misc.getRoundedValue(400.0F),
                  Misc.getRoundedValue(500.0F),
                  Misc.getRoundedValue(20.0F) + "%",
                  Misc.getRoundedValue(20.0F) + "%"
               }
            );
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/fortress_shield.png", SUStringCodex.SHU_TOOLTIP_IMG);
               text.addPara(
                  "Scutum Domanii",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Scutum Domanii"}
               );
               text.addPara(
                  "A subsystem similar to Fortress Shield, it activates when the %s. The subsystem absorbs incoming damage and reduces the upkeep of shields.",
                  0.0F,
                  Misc.getHighlightColor(),
                  new String[]{"shields are on"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• Reduces damage taken by shields: %s\n• Reduces shield's flux upkeep cost: %s",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(30.0F) + "%"}
               );
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
               new String[]{
                  "\"We, who are about to die, salute you. May our shields retain their integrity, our armor stay firm, and our wills remain unbreakable.\""
               }
            )
            .italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{"         — Lord-Admiral Kerensky, Supreme Commander of Domain Battlegroup Zero"}
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SSR_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SSR_NAME;
   }

   static {
      armorbonus.put(HullSize.FIGHTER, 0.0F);
      armorbonus.put(HullSize.FRIGATE, 200.0F);
      armorbonus.put(HullSize.DESTROYER, 300.0F);
      armorbonus.put(HullSize.CRUISER, 400.0F);
      armorbonus.put(HullSize.CAPITAL_SHIP, 500.0F);
   }
}
