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
import lunalib.lunaSettings.LunaSettings;

public class SHUGACHAHeliacalVenator extends BaseHullMod {
   private static final float FIGHTER_RANGE_BONUS = 20.0F;
   private static final float FIGHTER_DAMAGE_BONUS = 10.0F;
   private static final float COST_REDUCTION_LPC = 3.0F;
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      stats.getDynamic().getMod("bomber_cost_mod").modifyFlat(id, -6.0F);
      stats.getDynamic().getMod("fighter_cost_mod").modifyFlat(id, -3.0F);
      stats.getDynamic().getMod("interceptor_cost_mod").modifyFlat(id, -3.0F);
      stats.getDynamic().getMod("support_cost_mod").modifyFlat(id, -3.0F);
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_orionshipyards_designs")) {
         stats.getFighterWingRange().modifyPercent(id, 20.0F);
      }
   }

   public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
      fighter.getMutableStats().getDamageToFrigates().modifyPercent(id, 10.0F);
      fighter.getMutableStats().getDamageToDestroyers().modifyPercent(id, 10.0F);
      fighter.getMutableStats().getDamageToCruisers().modifyPercent(id, 10.0F);
      fighter.getMutableStats().getDamageToCapital().modifyPercent(id, 10.0F);
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_orionshipyards_designs")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_orionshipyards_designs")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_orionshipyards_designs");
      }
   }

   public boolean affectsOPCosts() {
      return true;
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_orionshipyards_designs")) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "banner_r"), 368.0F, 40.0F, 5.0F);
            if (SUPlugin.HASLUNALIB) {
               this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
            }

            if (this.enableCheatModeForRetards) {
               LabelAPI retardrius = tooltip.addPara("%s", 5.0F, Misc.getBrightPlayerColor(), new String[]{"Cheat Mode: ON"});
               retardrius.setAlignment(Alignment.MID);
               retardrius.italicize();
            }

            tooltip.addPara(
               "• Increases fighter engagement range: %s\n• Increased fighter's damage to ships: %s\n• Reduces OP cost of fighter & bomber LPC: %s/%s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(20.0F) + "%", Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(3.0F), Misc.getRoundedValue(6.0F)}
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
               new String[]{"\"Now this... this is what I call a target-rich environment. Let's see how they like the taste of my Sabots.\""}
            )
            .italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{"         — Unnamed ace pilot aboard a Longbow fighter registered to the PLS Enterprise, c186"}
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_NAME;
   }
}
