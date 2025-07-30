package com.github.wohaopa.MyCTMLib.mixins;

import com.github.wohaopa.MyCTMLib.CTMIconManager;
import com.github.wohaopa.MyCTMLib.MyCTMLib;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.ICTMIconManagerHolder;

import static com.github.wohaopa.MyCTMLib.Textures.ctmIconMap;

@Mixin(TextureAtlasSprite.class)
public class MixinTextureAtlasSprite implements ICTMIconManagerHolder {

    private String textureName;

    @Override
    public void setTextureName(String manager) {
        this.textureName = manager;
    }

    @Override
    public String getTextureName() {
        return this.textureName;
    }

    @Inject(method = "initSprite", at = @At("HEAD"))
    private void onInitSpriteHead(int atlasWidth, int atlasHeight, int originX, int originY, boolean rotated, CallbackInfo ci) {
        if (textureName != null) {
            Thread.dumpStack();
            MyCTMLib.LOG.warn(textureName);
            if (textureName.startsWith("gregtech:")) {
                MyCTMLib.LOG.warn(textureName);
            }
            CTMIconManager manager = ctmIconMap.get(textureName);
            if (manager != null) {
                manager.setIcon((TextureAtlasSprite) (Object) this);
            }
        }
    }
}
