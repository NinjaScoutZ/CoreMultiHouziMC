package com.houzicore.extension.module.message.format.object.texture.mineskin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.module.integration.FIntegration;
import com.houzicore.extension.module.message.format.object.texture.model.Frame;
import com.houzicore.extension.processing.resolver.SystemVariableResolver;
import com.houzicore.extension.util.WebUtil;
import com.houzicore.extension.util.file.FileFacade;
import com.houzicore.extension.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.data.ValueAndSignature;
import org.mineskin.request.GenerateRequest;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MineskinIntegration implements FIntegration {

    private final FileFacade fileFacade;
    private final SystemVariableResolver systemVariableResolver;
    @Getter private final FLogger fLogger;

    private MineSkinClient client;

    @Override
    public String getIntegrationName() {
        return "MineSkin";
    }

    @Override
    public void hook() {
        String apiKey = systemVariableResolver.substituteEnvVars(fileFacade.message().format().object().textureTag().mineskinApiKey());
        if (StringUtils.isEmpty(apiKey)) return;

        client = MineSkinClient.builder()
                .requestHandler(JsoupRequestHandler::new)
                .userAgent(WebUtil.USER_AGENT)
                .apiKey(apiKey)
                .build();

        logHook();
    }

    @Override
    public void unhook() {
        if (client == null) return;

        client = null;

        logUnhook();
    }

    public boolean isHooked() {
        return client != null;
    }

    public CompletableFuture<Frame> loadTexture(int x, int y, BufferedImage skinImage) {
        return client.queue()
                .submit(GenerateRequest.upload(skinImage))
                .thenCompose(queueResponse -> queueResponse.getJob().waitForCompletion(client))
                .thenCompose(jobResponse -> jobResponse.getOrLoadSkin(client))
                .thenApply(skin -> {
                    ValueAndSignature valueAndSignature = skin.texture().data();
                    return new Frame(x, y, valueAndSignature.value());
                });
    }

}
