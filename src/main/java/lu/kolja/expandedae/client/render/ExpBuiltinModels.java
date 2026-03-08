package lu.kolja.expandedae.client.render;

import appeng.client.render.model.ColorApplicatorModel;
import appeng.hooks.BuiltInModelHooks;
import lu.kolja.expandedae.Expandedae;
import net.minecraft.client.resources.model.UnbakedModel;

import java.util.function.Supplier;

public class ExpBuiltinModels {
    public static void init() {
        addBuiltInModel("infinity_color_applicator", ColorApplicatorModel::new);
    }

    private static <T extends UnbakedModel> void addBuiltInModel(String id, Supplier<T> modelFactory) {
        // works because of the ModelBakery mixin
        BuiltInModelHooks.addBuiltInModel(Expandedae.makeId(id), modelFactory.get());
    }
}
