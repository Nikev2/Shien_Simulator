package net.nikev2.nikev23.mixin;


import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerResourceMetadata;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(VillagerClothingFeatureRenderer.class)
public abstract class VillagerClothingFeatureRendererMixin <T extends LivingEntity & VillagerDataContainer, M extends EntityModel<T> & ModelWithHat>{

    @Shadow protected abstract Identifier findTexture(String keyType, Identifier keyId);


    @Shadow @Final private static Int2ObjectMap<Identifier> LEVEL_TO_ID;



    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/VillagerClothingFeatureRenderer;getContextModel()Lnet/minecraft/client/render/entity/model/EntityModel;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void renderBabyProfession(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci, VillagerData villagerData, VillagerType villagerType, VillagerProfession villagerProfession, VillagerResourceMetadata.HatType hatType, VillagerResourceMetadata.HatType hatType2){


        M entityModel = ((FeatureRenderer<T, M>)(Object)this).getContextModel();





        (entityModel).setHatVisible(hatType2 == VillagerResourceMetadata.HatType.NONE || hatType2 == VillagerResourceMetadata.HatType.PARTIAL && hatType != VillagerResourceMetadata.HatType.FULL);

        Identifier identifier = findTexture("type", Registries.VILLAGER_TYPE.getId(villagerType));
        FeatureRendererAccessor.renderModel(entityModel, identifier, matrixStack, vertexConsumerProvider, i, livingEntity, 1.0F, 1.0F, 1.0F);
        (entityModel).setHatVisible(true);

        if(livingEntity.isBaby() && villagerProfession != VillagerProfession.NONE && !(livingEntity instanceof ZombieVillagerEntity))
        {


            Identifier identifier2 = findTexture("profession", Registries.VILLAGER_PROFESSION.getId(villagerProfession));

            FeatureRendererAccessor.renderModel(entityModel, identifier2, matrixStack, vertexConsumerProvider, i, livingEntity, 1.0F, 1.0F, 1.0F);

            Identifier identifier3 = findTexture("profession_level", LEVEL_TO_ID.get(MathHelper.clamp(villagerData.getLevel(), 1, LEVEL_TO_ID.size())));

            if (!villagerProfession.equals(VillagerProfession.NITWIT)||!villagerProfession.equals(VillagerProfession.NONE)) FeatureRendererAccessor.renderModel(entityModel, identifier3, matrixStack, vertexConsumerProvider, i, livingEntity, 1.0F, 1.0F, 1.0F);

            ci.cancel();
        }



    }
}

