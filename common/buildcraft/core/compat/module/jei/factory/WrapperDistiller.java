package buildcraft.core.compat.module.jei.factory;

import java.awt.Color;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IRefineryRecipeManager;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

public class WrapperDistiller implements IRecipeWrapper {
    public final IRefineryRecipeManager.IDistillationRecipe recipe;
    private final ImmutableList<FluidStack> in, out;
    private final IDrawableAnimated animated;

    WrapperDistiller(IGuiHelper guiHelper, IRefineryRecipeManager.IDistillationRecipe recipe) {
        this.recipe = recipe;
        this.in = ImmutableList.of(recipe.in());
        this.out = ImmutableList.of(recipe.outLiquid());

        IDrawableStatic overComplete = guiHelper.createDrawable(CategoryDistiller.distillerBackground, 212, 0, 36, 57);
        this.animated = guiHelper.createAnimatedDrawable(overComplete, /*recipe.ticks() * 20*/ 40, IDrawableAnimated.StartDirection.LEFT, false);
    }
    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(FluidStack.class, this.in);
        ingredients.setOutputs(FluidStack.class, this.out);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        this.animated.draw(minecraft, 18, 4);
        minecraft.fontRenderer.drawString(MjAPI.formatRFFromMj(recipe.powerRequired()) + " RF", 21, 18, Color.GRAY.getRGB());
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return Lists.newArrayList();
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
