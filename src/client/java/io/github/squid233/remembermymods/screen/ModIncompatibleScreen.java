package io.github.squid233.remembermymods.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.squid233.remembermymods.ModCompatibilityCheckResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ModIncompatibleScreen extends Screen {
    public static final Component TITLE = Component.translatable("screen.remember-my-mods.modIncompatible.title");
    public static final Component TEXT_RISK = Component.translatable("screen.remember-my-mods.modIncompatible.risk");
    public static final Component TEXT_REMOVED = Component.translatable("screen.remember-my-mods.modIncompatible.removed");
    private FittingMultiLineTextWidget messageWidget;
    private final FrameLayout layout;
    private final BooleanConsumer callback;
    private final Component removedMessage;

    public ModIncompatibleScreen(List<ModCompatibilityCheckResult> results, BooleanConsumer callback) {
        super(TITLE);
        this.callback = callback;
        this.layout = new FrameLayout(0, 0, width, height);

        MutableComponent removed = Component.empty();
        for (int i = 0, size = results.size(); i < size; i++) {
            ModCompatibilityCheckResult result = results.get(i);
            if (result.currentVersion() == null) {
                if (i > 0) {
                    removed.append("\n");
                }
                removed.append(Component.literal(result.modId() + " " + result.oldVersion()));
            }
        }
        this.removedMessage = removed;
    }

    @Override
    public @NonNull Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), TEXT_RISK, TEXT_REMOVED, removedMessage);
    }

    @Override
    protected void init() {
        LinearLayout linearLayout = layout.addChild(LinearLayout.vertical().spacing(8));
        linearLayout.defaultCellSetting().alignHorizontallyCenter();
        linearLayout.addChild(new StringWidget(getTitle(), font));
        linearLayout.addChild(new StringWidget(TEXT_RISK, font));

        LinearLayout linearLayoutMsg = linearLayout.addChild(LinearLayout.horizontal().spacing(10));
        LinearLayout linearLayoutRemoved = linearLayoutMsg.addChild(LinearLayout.vertical().spacing(8));
        linearLayoutRemoved.addChild(new StringWidget(TEXT_REMOVED, font));
        messageWidget = linearLayoutRemoved.addChild(
            new FittingMultiLineTextWidget(0, 0, width - 100, height - 100, removedMessage, font)
        );

        LinearLayout linearLayoutButtons = linearLayout.addChild(LinearLayout.vertical().spacing(8));
        linearLayoutButtons.defaultCellSetting().alignHorizontallyCenter();
        linearLayoutButtons.addChild(addFooterButtons());

        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    private Layout addFooterButtons() {
        LinearLayout linearLayout = LinearLayout.horizontal().spacing(8);
        linearLayout.addChild(Button.builder(CommonComponents.GUI_YES, button -> callback.accept(true)).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_NO, button -> callback.accept(false)).build());
        return linearLayout;
    }

    @Override
    protected void repositionElements() {
        if (messageWidget != null) {
            messageWidget.setWidth(width - 100);
            messageWidget.setHeight(height - 100);
            messageWidget.minimizeHeight();
        }
        layout.arrangeElements();
        FrameLayout.centerInRectangle(layout, getRectangle());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent keyEvent) {
        if (keyEvent.key() == InputConstants.KEY_ESCAPE) {
            callback.accept(false);
            return true;
        }
        return super.keyPressed(keyEvent);
    }
}
