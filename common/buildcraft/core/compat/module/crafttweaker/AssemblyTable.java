package buildcraft.core.compat.module.crafttweaker;

import buildcraft.api.recipes.AssemblyRecipe;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.api.recipes.IngredientStack;

import buildcraft.lib.recipe.AssemblyRecipeRegistry;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ModOnly;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Map;

@ZenClass("mods.buildcraft.AssemblyTable")
@ModOnly("buildcraftsilicon")
public class AssemblyTable {

    private static int ids;

    @ZenMethod
    public static void addRecipe(IItemStack output, int power, IIngredient[] ingredients) {
        addRecipe0("auto_" + ids++, output, power, ingredients);
    }

    @ZenMethod
    public static void addRecipe(String name, IItemStack output, int power, IIngredient[] ingredients) {
        addRecipe0("custom/" + name, output, power, ingredients);
    }

    private static void addRecipe0(String name, IItemStack output, int power, IIngredient[] ingredients) {
        CraftTweakerAPI.apply(new AddRecipeAction(name, output, power, ingredients));
    }

    @ZenMethod
    public static void removeByName(String name) {
        CraftTweakerAPI.apply(new RemoveRecipeByNameAction(new ResourceLocation(name)));
    }

    @ZenMethod
    public static void remove(IItemStack output) {
        CraftTweakerAPI.apply(new RemoveRecipeByOutput(output));
    }

    // ######################
    // ### Action classes ###
    // ######################

    private static class AddRecipeAction implements IAction {

        private final ItemStack output;
        private final ResourceLocation name;
        private final long requiredMj;
        private final ImmutableSet<IngredientStack> requiredStacks;

        public AddRecipeAction(String name, IItemStack output, int power, IIngredient[] ingredients) {
            this.output = CraftTweakerMC.getItemStack(output);

            Builder<IngredientStack> stacks = ImmutableSet.builder();
            for (int i = 0; i < ingredients.length; i++) {
                IIngredient ctIng = ingredients[i];
                Ingredient ingredient = CraftTweakerMC.getIngredient(ctIng);
                stacks.add(new IngredientStack(ingredient, Math.max(1, ctIng.getAmount())));
            }
            requiredStacks = stacks.build();

            this.requiredMj = (power * MjAPI.MJ) / MjAPI.rfPerMj;
            this.name = new ResourceLocation("crafttweaker", name);
        }

        @Override
        public void apply() {
            AssemblyRecipeRegistry.REGISTRY.put(name,
                new AssemblyRecipeBasic(name, requiredMj, requiredStacks, output));
        }

        @Override
        public String describe() {
            return "Adding assembly table recipe for " + output;
        }
    }

    private static class RemoveRecipeByNameAction implements IAction {
        private final ResourceLocation name;

        RemoveRecipeByNameAction(ResourceLocation name) {
            this.name = name;
        }

        @Override
        public void apply() {
            AssemblyRecipeRegistry.REGISTRY.remove(name);
        }

        @Override
        public String describe() {
            return "Removing assembly table recipe " + name;
        }
    }

    private static class RemoveRecipeByOutput implements IAction {
        private final ItemStack output;

        RemoveRecipeByOutput(IItemStack output) {
            this.output = CraftTweakerMC.getItemStack(output);
        }

        @Override
        public void apply() {
            for (Map.Entry<ResourceLocation, AssemblyRecipe> entry : AssemblyRecipeRegistry.REGISTRY.entrySet()) {
                if (entry.getValue().getOutputPreviews().stream().anyMatch(output2 -> ItemStack.areItemStacksEqual(output2, output))) {
                    AssemblyRecipeRegistry.REGISTRY.remove(entry.getKey());
                }
            }
        }

        @Override
        public String describe() {
            return "Removing assembly table recipe " + output.getDisplayName();
        }
    }
}
