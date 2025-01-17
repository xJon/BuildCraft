package buildcraft.core.compat.module.theoneprobe;

import static buildcraft.core.compat.module.theoneprobe.BCPluginTOP.TOP_MOD_ID;

import java.util.List;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeFlowType;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.tile.TilePipeHolder;
import com.google.common.base.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import buildcraft.api.BCModules;
import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.tile.craft.IAutoCraft;

import buildcraft.core.compat.CompatUtils;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IBlockDisplayOverride;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;

@Optional.InterfaceList({
        @Optional.Interface(modid = TOP_MOD_ID, iface = "mcjty.theoneprobe.api.IBlockDisplayOverride"),
        @Optional.Interface(modid = TOP_MOD_ID, iface = "mcjty.theoneprobe.api.IProbeInfoProvider")
})
public class BCPluginTOP implements Function<ITheOneProbe, Void>, IBlockDisplayOverride, IProbeInfoProvider {
    static final String TOP_MOD_ID = "theoneprobe";

    @Override
    @Optional.Method(modid = TOP_MOD_ID)
    public Void apply(ITheOneProbe top) {
        top.registerBlockDisplayOverride(this);
        top.registerProvider(this);
        return null;
    }

    @Override
    @Optional.Method(modid = TOP_MOD_ID)
    public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        return false;
    }

    @Override
    @Optional.Method(modid = TOP_MOD_ID)
    public String getID() {
        return "buildcraftcompat.top";
    }

    @Override
    @Optional.Method(modid = TOP_MOD_ID)
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        ResourceLocation blockRegistryName = blockState.getBlock().getRegistryName();
        if ((blockRegistryName != null) && (BCModules.isBcMod(blockRegistryName.getResourceDomain()))) {
            TileEntity entity = world.getTileEntity(data.getPos());
            if (entity instanceof IAutoCraft) {
                this.addAutoCraftInfo(probeInfo, (IAutoCraft) entity);
            }
            if (entity instanceof ILaserTarget) {
                this.addLaserTargetInfo(probeInfo, (ILaserTarget) entity);
            }
            if (entity instanceof TilePipeHolder) {
                IPipe pipe = ((TilePipeHolder) entity).getPipe();
                if (pipe.getFlow() instanceof PipeFlowPower) {

                    probeInfo.horizontal().text(TextFormatting.GRAY+String.valueOf((((PipeFlowPower) pipe.getFlow()).maxPower)) + "RF/t", probeInfo.defaultTextStyle());
                } else if (pipe.getFlow() instanceof PipeFlowFluids) {
                    probeInfo.horizontal().text(TextFormatting.GRAY+String.valueOf((((PipeFlowFluids) pipe.getFlow()).fluidTransferInfo.transferPerTick)) + "mb/t", probeInfo.defaultTextStyle());
                }
            }
        }
    }

    @Optional.Method(modid = TOP_MOD_ID)
    private void addAutoCraftInfo(IProbeInfo probeInfo, IAutoCraft crafter) {
        if (!crafter.getCurrentRecipeOutput().isEmpty()) {
            IProbeInfo mainInfo = probeInfo.vertical();
            mainInfo
                    .horizontal(mainInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .text("Making: ")
                    .item(crafter.getCurrentRecipeOutput());
            IProbeInfo info = mainInfo
                    .horizontal(mainInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .text("From: ");
            List<ItemStack> stacks = CompatUtils.compactInventory(crafter.getInvBlueprint());

            for (ItemStack stack : stacks)
                info.item(stack);
        }
    }

    @Optional.Method(modid = TOP_MOD_ID)
    private void addLaserTargetInfo(IProbeInfo probeInfo, ILaserTarget laserTarget) {
        long power = laserTarget.getRequiredLaserPower();
        int rf = (int) (power * MjAPI.rfPerMj / MjAPI.MJ);
        if (power > 0) {
            probeInfo.horizontal()
                    .text(TextFormatting.WHITE + "Waiting from laser: ")
                    .text(TextFormatting.AQUA + MjAPI.formatRF(rf))
                    .text(TextFormatting.AQUA + "RF");
        }
    }
}

