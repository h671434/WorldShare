package ankel.worlshare.gui;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;

import ankel.worlshare.dropbox.AuthorizationException;
import ankel.worlshare.dropbox.Authorizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.ErrorScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoginScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String OPEN_URL_TO_LOGIN = "Login to DropBox through URL and enter access token";
	private final WorldSelectionScreen lastScreen;
	private final Minecraft minecraft;
	private final Authorizer auth;
	private TextFieldWidget tokenInput;
	private Widget submitButton;

	public LoginScreen(WorldSelectionScreen lastScreen) {
		super(new TranslationTextComponent("Login DropBox"));
		this.lastScreen = lastScreen;
		this.minecraft = lastScreen.getMinecraft();
		this.auth = new Authorizer(minecraft.gameDirectory);
	}
	
	public void tick() {
		this.tokenInput.tick();
	}
	
	private void openUrl() {
		String url = auth.getAuthUrl();
		Util.getPlatform().openUri(URI.create(url));
		this.tokenInput.active = true;
	}
	
	private void submitToken() {
		try {
			auth.finishAuthWithToken(tokenInput.getValue());
			minecraft.setScreen(new DbxScreen(lastScreen, auth.getClient()));
		} catch (AuthorizationException e) {
			minecraft.setScreen(new ErrorScreen(
					new TranslationTextComponent("Dropbox Login Error"),
					new TranslationTextComponent("Unable to log in to dropbox")));;
		}
	}
	
	protected void init() {
		this.addButton(new Button(this.width / 2 - 100, 126 + 50, 200, 20, 
				DialogTexts.GUI_CANCEL, e -> {
					this.minecraft.setScreen(lastScreen);
		}));
		this.addButton(new Button(this.width / 2 - 100, 100 + 50, 200, 20,  
				new TranslationTextComponent("Open URL"), e -> {
					this.openUrl();
		}));
		this.submitButton = this.addButton(new Button(this.width / 2 - 100, 74 + 50, 200, 20,
				new TranslationTextComponent("Submit"), e -> {
					this.submitToken();
		}));
		this.tokenInput = new TextFieldWidget(this.font, this.width / 2 - 125, 38 + 50, 250, 20,
				new TranslationTextComponent("Enter token"));
		this.tokenInput.setResponder(e -> {
			this.submitButton.active = !e.trim().isEmpty() && tokenInput.active;
		});
		this.tokenInput.setMaxLength(50);
		this.tokenInput.active = false;
		this.children.add(this.tokenInput);
	}
	
	public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
	      this.renderDirtBackground(0);
	      this.tokenInput.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
	      drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, 15, 16777215);
	      drawCenteredString(p_230430_1_, this.font, OPEN_URL_TO_LOGIN, this.width / 2, 30, 10526880);
	      super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
	   }
	
}
