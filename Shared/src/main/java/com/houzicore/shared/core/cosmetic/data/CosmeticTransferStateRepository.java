package com.houzicore.shared.core.cosmetic.data;

import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.redis.RedisDataRepository;

public class CosmeticTransferStateRepository extends RedisDataRepository<CosmeticTransferState> {
    public CosmeticTransferStateRepository() {
        super(Region.ALL, CosmeticTransferState.class, "cosmeticTransferState");
    }
}
