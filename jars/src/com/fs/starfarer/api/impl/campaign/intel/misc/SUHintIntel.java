package com.fs.starfarer.api.impl.campaign.intel.misc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.Set;

public class SUHintIntel extends BaseIntelPlugin {
   private final long addedTimestamp = Global.getSector().getClock().getTimestamp();

   protected float getBaseDaysAfterEnd() {
      return 31.0F;
   }

   public void advanceImpl(float amount) {
      if (!this.isPlayerVisible() && Global.getSector().getClock().getElapsedDaysSince(this.addedTimestamp) > this.getBaseDaysAfterEnd()) {
         this.endImmediately();
      }

      if (!this.isPlayerVisible() && Global.getSector().getMemory().contains("$SpecialHMODBarOffer")) {
         this.endImmediately();
      }

      if (this.isPlayerVisible() && !this.isEnding()) {
         this.endAfterDelay();
      }

      MarketAPI market = Global.getSector().getEconomy().getMarket(SUStringCodex.PORT_TSE_FRANCHISE);
      if (market == null) {
         this.endImmediately();
      }
   }

   public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
      float pad = 10.0F;
      Color grayText = Misc.getGrayColor();
      Color textColor = Misc.getTextColor();
      Color highlightColor = Misc.getHighlightColor();
      info.addImage("graphics/illustrations/space_bar.jpg", width, 128.0F, 10.0F);
      info.addPara(
         "Sometimes people need a break, and the gamblers at %s sure know how to have a good time. Some are a little desperate, though. Maybe one of these hapless fools might be willing to make it %s your while to bankroll his addiction?",
         10.0F,
         textColor,
         highlightColor,
         new String[]{"Port Tse Franchise Station", "worth"}
      );
      info.addPara("This information will be deleted after a month.", 10.0F, grayText, grayText, new String[0]);
      this.addDeleteButton(info, width);
   }

   public String getIcon() {
      return Global.getSettings().getSpriteName("intel", "shu_hint");
   }

   public Set<String> getIntelTags(SectorMapAPI map) {
      Set<String> tags = super.getIntelTags(map);
      tags.add("Important");
      return tags;
   }

   public String getName() {
      return "Gambler's Fallacy";
   }

   public String getSmallDescriptionTitle() {
      return this.getName();
   }

   public Color getTitleColor(ListInfoMode mode) {
      boolean isUpdate = this.getListInfoParam() != null;
      return this.isEnding() && !isUpdate && mode != ListInfoMode.IN_DESC
         ? Global.getSector().getPlayerFaction().getBaseUIColor()
         : Global.getSector().getPlayerFaction().getBaseUIColor();
   }

   public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
      Color titleColor = this.getTitleColor(mode);
      info.addPara(this.getName(), titleColor, 0.0F);
      this.addBulletPoints(info, mode);
   }

   public void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
      Color highlightText = Misc.getHighlightColor();
      Color bulletColor = this.getBulletColorForMode(mode);
      this.bullet(info);
      info.addPara("Visit %s in %s.", 0.0F, bulletColor, highlightText, new String[]{"Port Tse Franchise Station", "Mayasura Star System"});
      this.unindent(info);
   }

   public SectorEntityToken getMapLocation(SectorMapAPI map) {
      MarketAPI market = null;
      market = Global.getSector().getEconomy().getMarket(SUStringCodex.PORT_TSE_FRANCHISE);
      return market == null ? null : market.getPrimaryEntity();
   }
}
