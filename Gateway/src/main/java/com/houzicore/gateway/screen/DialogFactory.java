package com.houzicore.gateway.screen;

import com.houzicore.gateway.GatewayPlugin;
import com.houzicore.gateway.auth.AuthSession.State;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;

/**
 * Factory for creating standardized, bilingual (TH/EN) Dialog UI screens
 * using Paper 1.21.11 Dialog API.
 */
@SuppressWarnings("UnstableApiUsage")
public class DialogFactory {

    private final GatewayPlugin plugin;

    public DialogFactory(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    public Dialog buildLoginDialog(String errorMsg) {
        String serverName = plugin.getGateConfig().serverDisplayName();
        
        ActionButton submitBtn = ActionButton.create(
                Component.text("✓ เข้าสู่ระบบ (Login)", NamedTextColor.GREEN, TextDecoration.BOLD),
                null,
                200,
                DialogAction.commandTemplate("/hzgate-submit $(pw)")
        );

        TextDialogInput pwInput = DialogInput.text("pw", Component.text("รหัสผ่าน (Password)", NamedTextColor.WHITE))
                .maxLength(128)
                .width(200)
                .labelVisible(true)
                .build();

        Component bodyText = Component.text("กรุณากรอกรหัสผ่านเพื่อเข้าสู่เซิร์ฟเวอร์", NamedTextColor.GRAY)
                .append(Component.newline())
                .append(Component.text("(Please enter your password to connect)", NamedTextColor.DARK_GRAY));

        if (errorMsg != null && !errorMsg.isEmpty()) {
            bodyText = bodyText.append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("⚠ ", NamedTextColor.RED))
                    .append(deserialize(errorMsg));
        }

        PlainMessageDialogBody bodySection = DialogBody.plainMessage(bodyText, 200);

        DialogBase base = DialogBase.builder(Component.text("🔑 " + serverName + " — Login", NamedTextColor.AQUA, TextDecoration.BOLD))
                .body(List.of(bodySection))
                .inputs(List.of(pwInput))
                .canCloseWithEscape(false)
                .pause(false)
                .afterAction(DialogBase.DialogAfterAction.NONE)
                .build();

        return Dialog.create(factory -> factory.empty()
                .base(base)
                .type(DialogType.multiAction(List.of(submitBtn)).build())
        );
    }

    public Dialog buildRegisterDialog(String errorMsg) {
        String serverName = plugin.getGateConfig().serverDisplayName();

        ActionButton submitBtn = ActionButton.create(
                Component.text("✓ ถัดไป (Next)", NamedTextColor.GREEN, TextDecoration.BOLD),
                null,
                200,
                DialogAction.commandTemplate("/hzgate-submit $(pw)")
        );

        TextDialogInput pwInput = DialogInput.text("pw", Component.text("รหัสผ่านใหม่ (New Password)", NamedTextColor.WHITE))
                .maxLength(128)
                .width(200)
                .labelVisible(true)
                .build();

        Component bodyText = Component.text("บัญชีนี้ยังไม่มีผู้ใช้งาน กรุณาตั้งรหัสผ่านใหม่", NamedTextColor.GRAY)
                .append(Component.newline())
                .append(Component.text("(No account found. Please create a new password)", NamedTextColor.DARK_GRAY));

        if (errorMsg != null && !errorMsg.isEmpty()) {
            bodyText = bodyText.append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("⚠ ", NamedTextColor.RED))
                    .append(deserialize(errorMsg));
        }

        PlainMessageDialogBody bodySection = DialogBody.plainMessage(bodyText, 200);

        DialogBase base = DialogBase.builder(Component.text("✦ " + serverName + " — Register", NamedTextColor.GREEN, TextDecoration.BOLD))
                .body(List.of(bodySection))
                .inputs(List.of(pwInput))
                .canCloseWithEscape(false)
                .pause(false)
                .afterAction(DialogBase.DialogAfterAction.NONE)
                .build();

        return Dialog.create(factory -> factory.empty()
                .base(base)
                .type(DialogType.multiAction(List.of(submitBtn)).build())
        );
    }

    public Dialog buildRegisterConfirmDialog(String errorMsg) {
        String serverName = plugin.getGateConfig().serverDisplayName();

        ActionButton submitBtn = ActionButton.create(
                Component.text("✓ ยืนยันสมัครสมาชิก (Confirm Register)", NamedTextColor.GREEN, TextDecoration.BOLD),
                null,
                200,
                DialogAction.commandTemplate("/hzgate-submit $(pw)")
        );

        TextDialogInput pwInput = DialogInput.text("pw", Component.text("ยืนยันรหัสผ่าน (Confirm Password)", NamedTextColor.WHITE))
                .maxLength(128)
                .width(200)
                .labelVisible(true)
                .build();

        Component bodyText = Component.text("กรุณากรอกรหัสผ่านอีกครั้งเพื่อยืนยันความถูกต้อง", NamedTextColor.GRAY)
                .append(Component.newline())
                .append(Component.text("(Please re-enter your password to confirm)", NamedTextColor.DARK_GRAY));

        if (errorMsg != null && !errorMsg.isEmpty()) {
            bodyText = bodyText.append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("⚠ ", NamedTextColor.RED))
                    .append(deserialize(errorMsg));
        }

        PlainMessageDialogBody bodySection = DialogBody.plainMessage(bodyText, 200);

        DialogBase base = DialogBase.builder(Component.text("✦ " + serverName + " — Confirm", NamedTextColor.GREEN, TextDecoration.BOLD))
                .body(List.of(bodySection))
                .inputs(List.of(pwInput))
                .canCloseWithEscape(false)
                .pause(false)
                .afterAction(DialogBase.DialogAfterAction.NONE)
                .build();

        return Dialog.create(factory -> factory.empty()
                .base(base)
                .type(DialogType.multiAction(List.of(submitBtn)).build())
        );
    }

    public Dialog buildTwoFaDialog(String errorMsg) {
        String serverName = plugin.getGateConfig().serverDisplayName();

        ActionButton submitBtn = ActionButton.create(
                Component.text("✓ ยืนยัน PIN (Confirm PIN)", NamedTextColor.YELLOW, TextDecoration.BOLD),
                null,
                200,
                DialogAction.commandTemplate("/hzgate-submit $(pw)")
        );

        TextDialogInput pwInput = DialogInput.text("pw", Component.text("รหัส PIN 2FA (2FA PIN)", NamedTextColor.WHITE))
                .maxLength(128)
                .width(200)
                .labelVisible(true)
                .build();

        Component bodyText = Component.text("ตรวจพบการเชื่อมต่อจาก IP ใหม่ กรุณากรอกรหัส PIN 2FA", NamedTextColor.GRAY)
                .append(Component.newline())
                .append(Component.text("(New IP detected. Please enter your 2FA PIN)", NamedTextColor.DARK_GRAY));

        if (errorMsg != null && !errorMsg.isEmpty()) {
            bodyText = bodyText.append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("⚠ ", NamedTextColor.RED))
                    .append(deserialize(errorMsg));
        }

        PlainMessageDialogBody bodySection = DialogBody.plainMessage(bodyText, 200);

        DialogBase base = DialogBase.builder(Component.text("⚿ " + serverName + " — 2FA", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .body(List.of(bodySection))
                .inputs(List.of(pwInput))
                .canCloseWithEscape(false)
                .pause(false)
                .afterAction(DialogBase.DialogAfterAction.NONE)
                .build();

        return Dialog.create(factory -> factory.empty()
                .base(base)
                .type(DialogType.multiAction(List.of(submitBtn)).build())
        );
    }

    private Component deserialize(String message) {
        String prefix = plugin.getGateConfig().prefix();
        String cleanMsg = message.replace(prefix, "").trim();
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand()
                .deserialize(cleanMsg.replace("§", "&"));
    }
}
