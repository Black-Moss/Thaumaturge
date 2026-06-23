package com.leclowndu93150.thaumcraft.server.command;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.TheorycraftManager;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCCommands {
    private TCCommands() {}

    private static final SuggestionProvider<CommandSourceStack> PARTICLE_NAMES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(ParticleDemos.DEMOS.keySet(), builder);

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
                                .executes(TCCommands::runParticle)));
        event.getDispatcher().register(tc);
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
