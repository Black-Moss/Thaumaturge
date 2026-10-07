package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.server.command.TTCommandRoot;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ThaumaturgeCommand {

    private ThaumaturgeCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = TTCommandRoot.root();
        List<AdminSubcommand> subcommands = List.of(new ResearchSubcommand(), new KnowledgeSubcommand(), new AspectSubcommand(), PlacementSubcommand.structures(), PlacementSubcommand.features(),
                new ShowcaseSubcommand(), new LocateSubcommand(), new BuildSubcommand(), new LabyrinthSubcommand());
        for (AdminSubcommand subcommand : subcommands) {
            LiteralArgumentBuilder<CommandSourceStack> node = subcommand.build(event.getBuildContext());
            root.then(node.requires(Commands.<CommandSourceStack>hasPermission(Commands.LEVEL_GAMEMASTERS).and(node.getRequirement())));
        }
        event.getDispatcher().register(root);
    }
}
