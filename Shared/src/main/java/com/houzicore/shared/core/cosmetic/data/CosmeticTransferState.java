package com.houzicore.shared.core.cosmetic.data;

import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.serverdata.data.Data;

public class CosmeticTransferState implements Data {
    private String _playerName;
    private List<String> _activeGadgets;
    private String _activeMount;
    private String _activePetType;

    public CosmeticTransferState() {
        _activeGadgets = new ArrayList<>();
    }

    public CosmeticTransferState(String playerName, List<String> activeGadgets, String activeMount, String activePetType) {
        _playerName = playerName;
        _activeGadgets = activeGadgets == null ? new ArrayList<>() : new ArrayList<>(activeGadgets);
        _activeMount = activeMount;
        _activePetType = activePetType;
    }

    public String getPlayerName() {
        return _playerName;
    }

    public List<String> getActiveGadgets() {
        return _activeGadgets == null ? new ArrayList<>() : new ArrayList<>(_activeGadgets);
    }

    public String getActiveMount() {
        return _activeMount;
    }

    public String getActivePetType() {
        return _activePetType;
    }

    @Override
    public String getDataId() {
        return _playerName;
    }
}
