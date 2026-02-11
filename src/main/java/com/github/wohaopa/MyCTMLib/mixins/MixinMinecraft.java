package com.github.wohaopa.MyCTMLib.mixins;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.wohaopa.MyCTMLib.MyCTMLib;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;

/**
 * 注入 Minecraft.refreshResources 的 catch 块，在资源重载异常时用 MyCTMLib 的 Logger 打印错误，
 * 便于排查「用户资源包被自动卸载」问题（见 docs/texture-and-reload-analysis.md）。
 * <p>
 * 当异常为 ReportedException（如纹理 mipmap 生成失败）时，额外输出完整 CrashReport，
 * 包含出问题的 Sprite 名称、尺寸、帧数等信息，用于定位非标准纹理（如 128x96 连接图）。
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Redirect(
        method = "refreshResources",
        at = @At(
            value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Throwable;)V"
        ),
        require = 0)
    private void onRefreshResourcesCaughtError(Logger logger, String msg, Throwable t) {
        if (t instanceof ReportedException) {
            try {
                CrashReport report = ((ReportedException) t).getCrashReport();
                String fullReport = report.getCompleteReport();
                MyCTMLib.LOG.error(
                    "[MyCTMLib] refreshResources - ReportedException (含 Sprite/纹理 详情):\n{}",
                    fullReport);
            } catch (Throwable ex) {
                MyCTMLib.LOG.error("[MyCTMLib] refreshResources - 无法解析 CrashReport", ex);
            }
        }
        MyCTMLib.LOG.error(
            "[MyCTMLib] refreshResources caught RuntimeException - all user resource packs will be removed! " +
            "This usually indicates a texture stitching or resource loading failure. See docs/texture-and-reload-analysis.md",
            t);
        logger.info(msg, t);
    }
}
