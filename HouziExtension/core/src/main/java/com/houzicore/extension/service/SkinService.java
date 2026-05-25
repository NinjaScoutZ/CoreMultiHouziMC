package com.houzicore.extension.service;

import com.houzicore.extension.model.entity.FEntity;

public interface SkinService {

    String getSkin(FEntity fPlayer);

    String getAvatarUrl(FEntity entity);

    String getBodyUrl(FEntity entity);

}
