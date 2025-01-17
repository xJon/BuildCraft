package buildcraft.core.compat.module.crafttweaker;

import buildcraft.energy.BCEnergyConfig;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.engine.TileEngineBase_BC8;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ModOnly;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.buildcraft.CombustionEngine")
@ModOnly("buildcraftenergy")
public class CombustionEngine {

    private static final double MAX_POWER
        = (TileEngineBase_BC8.MAX_HEAT - TileEngineBase_BC8.MIN_HEAT) / BCEnergyConfig.heatPerMj;

    @ZenMethod
    public static void addCleanFuel(ILiquidStack liquid, double powerPerTick, int timePerBucket) {
        FluidStack fluid = CraftTweakerMC.getLiquidStack(liquid);
        if (fluid == null) {
            throw new IllegalArgumentException("Fluid was null!");
        }
        //if (BuildcraftFuelRegistry.fuel.getFuel(fluid) != null) {
        //    throw new IllegalArgumentException("The fluid " + fluid + " is already registered as a fuel!");
        //}
        if (BuildcraftFuelRegistry.coolant.getCoolant(fluid) != null) {
            throw new IllegalArgumentException(
                "The fluid " + fluid
                    + " is already registered as a coolant - so it won't work very well if you add it as a fuel too!"
            );
        }
        if (powerPerTick <= 0) {
            throw new IllegalArgumentException("Power was less than or equal to 0!");
        }
        if (powerPerTick > MAX_POWER) {
            throw new IllegalArgumentException(
                "Maximum power is " + MAX_POWER
                    + ", as any values above this would instantly bring the engine to overheat."
            );
        }
        long mj = (long) (MjAPI.MJ * powerPerTick);
        CraftTweakerAPI.apply(new AddCleanFuel(fluid, mj, timePerBucket));
    }
    
    @ZenMethod
    public static void addDirtyFuel(ILiquidStack lFuel, double powerPerTick, int timePerBucket, ILiquidStack lResidue) {
        FluidStack fuel = CraftTweakerMC.getLiquidStack(lFuel);
        FluidStack residue = CraftTweakerMC.getLiquidStack(lResidue);
        if (fuel.getFluid() == null) {
            throw new IllegalArgumentException("Fuel fluid was null!");
        }
        if (residue.getFluid() == null) {
            throw new IllegalArgumentException("Residue fluid was null!");
        }
        //if (BuildcraftFuelRegistry.fuel.getFuel(fuel) != null) {
        //    throw new IllegalArgumentException("The fluid " + fuel + " is already registered as a fuel!");
        //}
        if (BuildcraftFuelRegistry.coolant.getCoolant(fuel) != null) {
            throw new IllegalArgumentException(
                "The fluid " + fuel
                    + " is already registered as a coolant - so it won't work very well if you add it as a fuel too!"
            );
        }
        if (powerPerTick <= 0) {
            throw new IllegalArgumentException("Power was less than or equal to 0!");
        }
        if (powerPerTick > MAX_POWER) {
            throw new IllegalArgumentException(
                "Maximum power is " + MAX_POWER
                    + ", as any values above this would instantly bring the engine to overheat."
            );
        }
        long mj = (long) (MjAPI.MJ * powerPerTick);
        CraftTweakerAPI.apply(new AddDirtyFuel(fuel, mj, timePerBucket, residue));
    }

    // @ZenMethod
    // public static void addLiquidCoolant(FluidStack coolant) {
    //
    // }
    //
    // @ZenMethod
    // public static void addSolidCoolant(ItemStack stack, FluidStack coolant) {
    //
    // }

    // ######################
    // ### Action classes ###
    // ######################

    static final class AddCleanFuel implements IAction {

        private final FluidStack fluid;
        private final long powerPerTick;
        private final int totalBurningTime;

        public AddCleanFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime) {
            this.fluid = fluid;
            this.powerPerTick = powerPerCycle;
            this.totalBurningTime = totalBurningTime;
        }

        @Override
        public void apply() {
            BuildcraftFuelRegistry.fuel.addFuel(fluid, powerPerTick, totalBurningTime);
        }

        @Override
        public String describe() {
            return "Adding combustion engine fuel " + fluid;
        }
    }

    static final class AddDirtyFuel implements IAction {

        private final FluidStack fuel, residue;
        private final long powerPerTick;
        private final int totalBurningTime;

        public AddDirtyFuel(FluidStack fuel, long powerPerCycle, int totalBurningTime, FluidStack residue) {
            this.fuel = fuel;
            this.powerPerTick = powerPerCycle;
            this.totalBurningTime = totalBurningTime;
            this.residue = residue;
        }

        @Override
        public void apply() {
            BuildcraftFuelRegistry.fuel.addDirtyFuel(fuel, powerPerTick, totalBurningTime, residue);
        }

        @Override
        public String describe() {
            return "Adding combustion engine fuel " + fuel;
        }
    }
}
