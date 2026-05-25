package com.houzicore.shared.core.bonuses;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.plugin.PluginRegistry;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardData;
import com.houzicore.shared.core.reward.RewardRarity;
import com.houzicore.shared.core.reward.RewardType;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.treasure.TreasureManager;

public class SpinWheelPage extends ShopPageBase<BonusManager, BonusMenu> {

    private static final int HOPPER_SLOT = 4;
    private static final int CARL_SLOT = 22;
    private static final int[] LINE_NUMS = { -9, 9 };

    private TreasureManager _treasureManager;
    private BonusClientData _data;
    
    private RewardData _rewardData;
    private Reward _reward;
    private int _currentRewardIndex;
    private int _ticksThisSwap;
    private int _ticksPerSwap;
    private Reward[] _rewards;
    private boolean _stopped;
    private boolean _rewarded;
    private List<Integer> _ticks;
    private int _frame;
    private float _pitch;
    private int _stopSpinnerAt;

    public SpinWheelPage(BonusManager plugin, BonusMenu shop, CoreClientManager clientManager, DonationManager donationManager, Player player, BonusClientData data) {
        super(plugin, shop, clientManager, donationManager, "Spin the Wheel", player, 27);
        _data = data;
        
        try {
            _treasureManager = PluginRegistry.require(TreasureManager.class);
        } catch (Exception e) {}
        
        buildPage();
    }

    @Override
    protected void buildPage() {
        if (_treasureManager == null) {
            getPlayer().sendMessage(F.main("Bonus", "Treasure system is not available!"));
            getShop().openPageForPlayer(getPlayer(), new BonusPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), getPlayer()));
            return;
        }

        if (_data.Tickets <= 0) {
            getPlayer().sendMessage(F.main("Bonus", "You do not have any Spin Tickets!"));
            getShop().openPageForPlayer(getPlayer(), new BonusPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), getPlayer()));
            return;
        }

        // Consume ticket
        _data.Tickets--;
        getPlugin().getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin().getPlugin(), () -> {
            int accountId = getClientManager().Get(getPlayer()).getAccountId();
            getPlugin().getRepository().updateTickets(accountId, _data.Tickets);
        });

        // Initialize Animation state
        _ticks = new ArrayList<>();
        _frame = 0;
        _pitch = 1;
        _ticksPerSwap = 1;

        for (int i = 0; i < 40; i++) _ticks.add(1);
        for (int i = 0; i < 20; i++) _ticks.add(2);
        for (int i = 0; i < 10; i++) _ticks.add(4);
        for (int i = 0; i < 4; i++) _ticks.add(6);
        for (int i = 0; i < 3; i++) _ticks.add(8);
        if (Math.random() > 0.5) _ticks.add(12);

        _stopSpinnerAt = _ticks.size();
        _rewards = new Reward[_stopSpinnerAt + 10];
        
        for (int i = 0; i < _stopSpinnerAt + 10; i++) {
            if (i != _stopSpinnerAt + 4) {
                Reward[] fillers = _treasureManager.getRewards(getPlayer(), RewardType.SpinnerFiller);
                _rewards[i] = fillers[0];
            } else {
                Reward[] reals = _treasureManager.getRewards(getPlayer(), RewardType.SpinnerReal);
                _rewards[i] = reals[0];
                _reward = reals[0];
            }
        }

        _reward.giveReward("Spinner", getPlayer());
        _rewardData = _reward.getFakeRewardData(getPlayer());
        
        addButton(HOPPER_SLOT, new ShopItem(Material.HOPPER, C.cWhite + "Selector", new String[0], 1, false), (p, click) -> {});
        
        getPlugin().addSpin(this);
    }
    
    public boolean tick() {
        if (!getPlayer().isOnline() || !getPlayer().getOpenInventory().getTitle().equals("Spin the Wheel")) {
            if (!_rewarded) forceReward();
            return false;
        }
        
        if (_stopped) {
            checkIfDone();
            return false; // Actually keep ticking for glass animation? The old one used UpdateType.FAST for glass.
        }

        _ticksThisSwap++;

        if (_ticksThisSwap >= _ticksPerSwap) {
            _ticksThisSwap = 0;

            if (_pitch == 1) _pitch = 1.5f;
            else if (_pitch == 1.5f) _pitch = 2f;
            else if (_pitch == 2f) _pitch = 1f;

            getPlayer().playSound(getPlayer().getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, _pitch);

            _currentRewardIndex++;
            updateGui();

            _ticksPerSwap = _ticks.get(_currentRewardIndex - 1);

            if (_currentRewardIndex == _stopSpinnerAt) {
                _stopped = true;
            }
        }
        return true;
    }
    
    private void updateGui() {
        for (int i = 0; i < 9; i++) {
            int index = _currentRewardIndex + i;
            int slot = 9 + i;
            RewardData data = _rewards[index].getFakeRewardData(getPlayer());
            
            ShopItem rewardItem = new ShopItem(data.getDisplayItem().getType(), C.cWhite + data.getFriendlyName(), new String[0], 1, false);
            addButton(slot, rewardItem, (p, click) -> {});

            // Glass Panes
            for (int j = 0; j < LINE_NUMS.length; j++) {
                int paneSlot = slot + LINE_NUMS[j];
                if (paneSlot == HOPPER_SLOT) continue;
                
                ShopItem glassItem = new ShopItem(data.getDisplayItem().getType(), " ", new String[0], 1, false);
                addButton(paneSlot, glassItem, (p, click) -> {});
            }
        }
    }
    
    private void checkIfDone() {
        if (_rewarded) return;
        
        forceReward();
    }
    
    private void forceReward() {
        if (_rewarded) return;
        _rewarded = true;
        
        if (_reward.getRarity() == RewardRarity.RARE) {
            UtilServer.broadcast(F.main("Bonus", F.name(getPlayer().getName()) + " won " + C.cPurple + "Rare " + _rewardData.getFriendlyName() + C.cGray + " from Spin the Wheel."));
        } else if (_reward.getRarity() == RewardRarity.LEGENDARY) {
            UtilServer.broadcast(F.main("Bonus", F.name(getPlayer().getName()) + " won " + C.cGreen + "Legendary " + _rewardData.getFriendlyName() + C.cGray + " from Spin the Wheel."));
        } else if (_reward.getRarity() == RewardRarity.MYTHICAL) {
            UtilServer.broadcast(F.main("Bonus", F.name(getPlayer().getName()) + " won " + C.cRed + "Mythical " + _rewardData.getFriendlyName() + C.cGray + " from Spin the Wheel."));
        } else {
            getPlayer().sendMessage(F.main("Spin the Wheel", "You won " + _reward.getRarity().getColor() + _rewardData.getFriendlyName() + C.cGray + " from Spin the Wheel."));
        }
        
        getPlayer().playSound(getPlayer().getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        getPlugin().removeSpin(this);
    }
}
