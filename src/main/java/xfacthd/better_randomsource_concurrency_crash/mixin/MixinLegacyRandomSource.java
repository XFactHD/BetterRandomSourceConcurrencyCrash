package xfacthd.better_randomsource_concurrency_crash.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(LegacyRandomSource.class)
public class MixinLegacyRandomSource
{
    @Unique
    private static final Logger BRSCC$LOGGER = LogUtils.getLogger();

    @Unique
    private final AtomicReference<Thread> brscc$currentThread = new AtomicReference<>();

    @Inject(method = "next", at = @At("HEAD"))
    private void brscc$onNextHead(int pSize, CallbackInfoReturnable<Integer> cir)
    {
        Thread last = brscc$currentThread.getAndSet(Thread.currentThread());
        if (last != null)
        {
            throw ThreadingDetector.makeThreadingException("LegacyRandomSource (Mixin)", last);
        }
    }

    @Inject(method = "next", at = @At("TAIL"))
    private void brscc$onNextTail(int pSize, CallbackInfoReturnable<Integer> cir)
    {
        Thread last = brscc$currentThread.getAndSet(null);
        if (last != Thread.currentThread())
        {
            BRSCC$LOGGER.error("Thread changed unexpectedly", new Throwable());
        }
    }
}
