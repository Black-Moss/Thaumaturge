package com.leclowndu93150.thaumcraft.server.command;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.api.research.IResearchEntry;
import com.leclowndu93150.thaumcraft.api.taint.TaintApi;
import com.leclowndu93150.thaumcraft.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumcraft.content.research.ResearchManager;
import com.leclowndu93150.thaumcraft.content.research.ResearchRegistration;
import com.leclowndu93150.thaumcraft.data.worldgen.feature.TCConfiguredFeatures;
import com.leclowndu93150.thaumcraft.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.TheorycraftManager;
import com.leclowndu93150.thaumcraft.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCMobEffects;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCCommands {
    private TCCommands() {}

    private static final SuggestionProvider<CommandSourceStack> PARTICLE_NAMES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(ParticleDemos.DEMOS.keySet(), builder);

    private static final DynamicCommandExceptionType ERROR_INVALID_GATE = new DynamicCommandExceptionType((value) -> Component.literal("Unknown Research Entry : " + value));

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> tc = Commands.literal("tc")
                .then(Commands.literal("theorycraft").then(Commands.literal("test")
                        .executes(TCCommands::startTestSession)))
                .then(Commands.literal("table").executes(TCCommands::giveResearchTable))
                .then(Commands.literal("book").executes(TCCommands::giveThaumonomicon))
                .then(Commands.literal("particle")
                        .then(Commands.literal("list").executes(TCCommands::listParticles))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(PARTICLE_NAMES)
                                .executes(TCCommands::runParticle)))
                .then(Commands.literal("flux_goo")
                        .then(Commands.literal("set")
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 8))
                                        .executes(TCCommands::setFluxGoo))))
                .then(Commands.literal("effect")
                        .then(Commands.literal("vis_exhaust").executes(ctx -> giveEffect(ctx, "vis_exhaust")))
                        .then(Commands.literal("infectious_vis_exhaust").executes(ctx -> giveEffect(ctx, "infectious_vis_exhaust")))
                        .then(Commands.literal("flux_taint").executes(ctx -> giveEffect(ctx, "flux_taint"))))
                .then(Commands.literal("entity")
                        .then(Commands.literal("thaumic_slime").executes(ctx -> spawnEntity(ctx, "thaumic_slime")))
                        .then(Commands.literal("taint_crawler").executes(ctx -> spawnEntity(ctx, "taint_crawler")))
                        .then(Commands.literal("taint_seed").executes(ctx -> spawnEntity(ctx, "taint_seed")))
                        .then(Commands.literal("taint_seed_prime").executes(ctx -> spawnEntity(ctx, "taint_seed_prime")))
                        .then(Commands.literal("taint_swarm").executes(ctx -> spawnEntity(ctx, "taint_swarm")))
                        .then(Commands.literal("taintacle").executes(ctx -> spawnEntity(ctx, "taintacle")))
                        .then(Commands.literal("taintacle_small").executes(ctx -> spawnEntity(ctx, "taintacle_small"))))
                .then(Commands.literal("crystal")
                        .then(Commands.argument("aspect", StringArgumentType.word())
                                .executes(TCCommands::giveCrystal)))
                .then(Commands.literal("aura").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("info").executes(TCCommands::auraInfo))
                        .then(Commands.literal("vis")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(ctx -> auraVis(ctx, false))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(ctx -> auraVis(ctx, true)))))
                        .then(Commands.literal("flux")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(ctx -> auraFlux(ctx, false))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(ctx -> auraFlux(ctx, true))))))
                .then(Commands.literal("taint").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("seed").executes(ctx -> spawnEntity(ctx, "taint_seed")))
                        .then(Commands.literal("spread").executes(TCCommands::taintSpread)))
                .then(Commands.literal("tree").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("greatwood").executes(ctx -> placeFeature(ctx, TCConfiguredFeatures.GREATWOOD_TREE)))
                        .then(Commands.literal("silverwood").executes(ctx -> placeFeature(ctx, TCConfiguredFeatures.SILVERWOOD_TREE)))
                        .then(Commands.literal("magic").executes(ctx -> placeFeature(ctx, TCConfiguredFeatures.BIG_MAGIC_TREE))))
                .then(Commands.literal("research").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("grant")
                                .then(Commands.argument("entry", ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY)).executes(TCCommands::grantGate)))
                        .then(Commands.literal("revoke")
                                .then(Commands.argument("entry", ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY)).executes(TCCommands::revokeGate))));
        event.getDispatcher().register(tc);
    }

    private static int revokeGate(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ResourceKey<IResearchEntry> key = ResourceKeyArgument.getRegistryKey(ctx, "entry", IResearchEntry.REGISTRY_KEY,ERROR_INVALID_GATE);
            PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
            if (knowledge.removeResearch(key.identifier())) {
                knowledge.sync(player);
                ctx.getSource().sendSuccess(() -> Component.literal(
                        String.format("Revoked research %s ", key.identifier())), false);
                return Command.SINGLE_SUCCESS;
            } else {
                ctx.getSource().sendFailure(Component.literal("Failed to revoke research entry"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int grantGate(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            ResourceKey<IResearchEntry> key = ResourceKeyArgument.getRegistryKey(ctx, "entry", IResearchEntry.REGISTRY_KEY,ERROR_INVALID_GATE);
            Holder<IResearchEntry> holder = level.registryAccess()
                    .lookupOrThrow(IResearchEntry.REGISTRY_KEY)
                    .getOrThrow(key);
            if (ResearchManager.complete(player,key.identifier())) {
                ResearchManager.setStage(player,key.identifier(),holder.value().stages().size());
                ctx.getSource().sendSuccess(() -> Component.literal(
                        String.format("Unlocked research %s ", key.identifier())), false);
                return Command.SINGLE_SUCCESS;
            } else {
                ctx.getSource().sendFailure(Component.literal("Failed to grant research entry"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int auraInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            float vis = AuraHelper.getVis(level, pos);
            float flux = AuraHelper.getFlux(level, pos);
            int base = AuraHelper.getAuraBase(level, pos);
            ctx.getSource().sendSuccess(() -> Component.literal(
                    String.format("Aura at %s: vis %.1f, flux %.1f, base %d", pos.toShortString(), vis, flux, base)), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int auraVis(CommandContext<CommandSourceStack> ctx, boolean remove) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            float amount = FloatArgumentType.getFloat(ctx, "amount");
            if (remove) {
                float drained = AuraHelper.drainVis(level, pos, amount, false);
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Drained %.1f vis", drained)), false);
            } else {
                AuraHelper.addVis(level, pos, amount);
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Added %.1f vis", amount)), false);
            }
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int auraFlux(CommandContext<CommandSourceStack> ctx, boolean remove) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            float amount = FloatArgumentType.getFloat(ctx, "amount");
            if (remove) {
                float drained = AuraHelper.drainFlux(level, pos, amount, false);
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Drained %.1f flux", drained)), false);
            } else {
                AuraHelper.addFlux(level, pos, amount);
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Added %.1f flux", amount)), false);
            }
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int placeFeature(CommandContext<CommandSourceStack> ctx, ResourceKey<ConfiguredFeature<?, ?>> key) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            Holder<ConfiguredFeature<?, ?>> holder = level.registryAccess()
                    .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                    .getOrThrow(key);
            boolean placed = holder.value().place(level, level.getChunkSource().getGenerator(), level.getRandom(), pos);
            if (placed) {
                ctx.getSource().sendSuccess(() -> Component.literal("Placed " + key.identifier()), false);
                return Command.SINGLE_SUCCESS;
            }
            ctx.getSource().sendFailure(Component.literal("Feature refused to place here (bad soil or no clearance)"));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int taintSpread(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            TaintApi.spreadFibres(level, pos, true);
            ctx.getSource().sendSuccess(() -> Component.literal("Forced taint spread at " + pos.toShortString()), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int setFluxGoo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int level = IntegerArgumentType.getInteger(ctx, "level");
            BlockPos pos = player.blockPosition();
            ServerLevel serverLevel = (ServerLevel) player.level();
            var state = TCBlocks.FLUX_GOO.get().defaultBlockState();
            serverLevel.setBlock(pos, state, Block.UPDATE_ALL);
            ctx.getSource().sendSuccess(() -> Component.literal("Placed flux goo at level " + level), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveEffect(CommandContext<CommandSourceStack> ctx, String key) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            Holder<MobEffect> effect = switch (key) {
                case "vis_exhaust" -> TCMobEffects.VIS_EXHAUST;
                case "infectious_vis_exhaust" -> TCMobEffects.INFECTIOUS_VIS_EXHAUST;
                case "flux_taint" -> TCMobEffects.FLUX_TAINT;
                default -> null;
            };
            if (effect == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown effect: " + key));
                return 0;
            }
            player.addEffect(new MobEffectInstance(effect, 600, 0, true, true, true));
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int spawnEntity(CommandContext<CommandSourceStack> ctx, String name) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            var type = switch (name) {
                case "thaumic_slime" -> TCEntities.THAUMIC_SLIME.get();
                case "taint_crawler" -> TCEntities.TAINT_CRAWLER.get();
                case "taint_seed" -> TCEntities.TAINT_SEED.get();
                case "taint_seed_prime" -> TCEntities.TAINT_SEED_PRIME.get();
                case "taint_swarm" -> TCEntities.TAINT_SWARM.get();
                case "taintacle" -> TCEntities.TAINTACLE.get();
                case "taintacle_small" -> TCEntities.TAINTACLE_SMALL.get();
                default -> null;
            };
            if (type == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown entity: " + name));
                return 0;
            }
            var entity = type.create(level, EntitySpawnReason.COMMAND);
            if (entity == null) {
                ctx.getSource().sendFailure(Component.literal("Failed to create " + name));
                return 0;
            }
            entity.setPos(player.getX(), player.getY(), player.getZ());
            if (entity instanceof ThaumicSlime slime) {
                slime.setSize(2, true);
            }
            level.addFreshEntity(entity);
            ctx.getSource().sendSuccess(() -> Component.literal("Spawned " + name), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveCrystal(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String tag = StringArgumentType.getString(ctx, "aspect");
            ResourceKey<IAspect> key = ResourceKey.create(IAspect.REGISTRY_KEY,
                    Identifier.fromNamespaceAndPath(TCIds.MODID, tag));
            ItemStack stack = EssentiaCrystalFactory.of(player.registryAccess(), key);
            player.getInventory().add(stack);
            ctx.getSource().sendSuccess(() -> Component.literal("Gave crystal of " + tag), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int startTestSession(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            TheorycraftManager.beginSession(player, player.level(), player.blockPosition());
            ctx.getSource().sendSuccess(() -> Component.literal("Theorycraft session started; open a Research Table."), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveResearchTable(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.getInventory().add(new ItemStack(TCItems.RESEARCH_TABLE.get()));
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveThaumonomicon(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.getInventory().add(new ItemStack(TCItems.THAUMONOMICON.get()));
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int listParticles(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("=== Thaumcraft Particle Demos ===").withStyle(ChatFormatting.GOLD), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Use /tc particle <name> — spawns 3 blocks in front of you").withStyle(ChatFormatting.GRAY), false);
        for (var entry : ParticleDemos.DEMOS.entrySet()) {
            String name = entry.getKey();
            String desc = entry.getValue().description();
            ctx.getSource().sendSuccess(() ->
                    Component.literal(name + " ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal("— " + desc).withStyle(ChatFormatting.WHITE)), false);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Total: " + ParticleDemos.DEMOS.size() + " demos").withStyle(ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int runParticle(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(ctx, "name");
            if (!ParticleDemos.DEMOS.containsKey(name)) {
                ctx.getSource().sendFailure(Component.literal("Unknown demo: " + name + " — try /tc particle list"));
                return 0;
            }
            ParticleDemos.run(player, name);
            ctx.getSource().sendSuccess(() -> Component.literal("Spawned demo: " + name).withStyle(ChatFormatting.GREEN), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }
}
