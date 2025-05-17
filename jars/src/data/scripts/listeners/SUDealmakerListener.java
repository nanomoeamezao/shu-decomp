package data.scripts.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.util.Iterator;
import java.util.List;

public class SUDealmakerListener extends BaseCampaignEventListener {
   public static final String DEALMAKER_ID = "SUDealmakerListener";

   public SUDealmakerListener() {
      super(false);
   }

   public void reportPlayerClosedMarket(MarketAPI market) {
      if (Global.getSector().getCampaignUI().getCurrentInteractionDialog() != null) {
         if (!Global.getSector().getCampaignUI().getCurrentInteractionDialog().getInteractionTarget().getMarket().getOnOrAt().isEmpty()) {
            if (Global.getSector().getCampaignUI().getCurrentInteractionDialog().getInteractionTarget() != null) {
               if (Global.getSector().getCampaignUI().getCurrentInteractionDialog().getInteractionTarget().getMarket() != null) {
                  MarketAPI CHUCKSMUCKANDDUCK = Global.getSector().getCampaignUI().getCurrentInteractionDialog().getInteractionTarget().getMarket();
                  CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                  List<FleetMemberAPI> playerShips = playerFleet.getFleetData().getMembersListCopy();
                  boolean feedNSeed = false;
                  boolean forceFleetSync = false;
                  boolean isCampaign = Global.getCombatEngine().isInCampaign();
                  if (isCampaign) {
                     Iterator var8 = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().iterator();
                     if (var8.hasNext()) {
                        FleetMemberAPI member = (FleetMemberAPI)var8.next();
                        if (member.getVariant().hasHullMod("specialsphmod_dealmakerholosuite_upgrades")) {
                           Global.getSector()
                              .getCampaignUI()
                              .getCurrentInteractionDialog()
                              .getInteractionTarget()
                              .getMarket()
                              .getTariff()
                              .modifyMult("SUDealmakerListener", 0.85F);
                        } else if (!member.getVariant().hasHullMod("specialsphmod_dealmakerholosuite_upgrades")) {
                           Global.getSector()
                              .getCampaignUI()
                              .getCurrentInteractionDialog()
                              .getInteractionTarget()
                              .getMarket()
                              .getTariff()
                              .modifyMult("SUDealmakerListener", 1.0F);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
