
### Added

- Added Thaumometer scans of every item in a container with Sneak+Use.
- Added JEI recipe and usage shortcuts for items and discovered aspects in the Thaumonomicon.
- Added automatic aspects for smelting and cooking recipe outputs.
- Added datapack customization for mob drops, champion rewards and mob equipment.
- Added datapack customization for infusion outcomes, altar bonuses and essentia smelter stats.
- Added sounds when item grates open and close.
- Added connected textures to the blocks that deserve it
- Added proper block set for decoration
- Added stairs on the corridors in the eldrich

### Changed

- Improved performance of machines, aura nodes, channeled spells and crystals.
- Reduced unnecessary network updates from idle devices, research pages and golem harvesting.
- Expanded translation support for commands, tooltips and screens.
- Slowed the glowing Eldritch Crust animation. (#462)

### Fixed

- foci and casters from 0.2.x keep their spells after updating
- fixed iris shader crash
- fixed taint not spreading correctly
- Fixed missing aura in chunks generated before Thaumaturge was added.
- Fixed aspect-cache crashes and stale aspect values after startup or datapack reloads.
- Fixed discovered aspects missing from JEI after login.
- Fixed void jar upgrades losing stored essentia, labels and custom names.
- Fixed hungry nodes destroying warded blocks.
- Fixed item grates accepting items while closed or blocked, scattering drops and failing to resume when cleared.
- Fixed vis relays linking through solid blocks. (#469)
- Fixed vis relay placement on walls, ceilings and partial block supports. (#470)
- Fixed the Hoe of Growth blocking offhand use when it cannot till or fertilize. (#452)
- Fixed the Sword of the Zephyr failing to trigger sculk while in use. (#453)
- Fixed Sneak+Use with the Thaumometer interacting with targets instead of scanning them. (#459)
- Fixed flux goo immobilizing players and mobs. (#472)
- Fixed flux gas repeatedly moving between nearly equal neighboring blocks.
- Fixed flux goo spreading taint despite protection or disabled taint settings.
- Fixed missing Eldritch maze rooms in chunks generated before their maze.
- Fixed obsolete Eldritch maze mobs reappearing after chunk reloads.
- Fixed boss bars losing custom names after loading.
- Fixed stale aura readings, effects and recipe displays after changing worlds.
- Fixed memory leaks from magical beam and essentia stream effects.
- Fixed Arcane Bore animations and invisible falling taint.
- Fixed lighting on the Advanced Alchemical Furnace and decorative pillars.
- Fixed wand and staff size and positioning on recharge pedestals. (#473)
- Fixed misaligned research table slots and helper icons. (#449)
- Fixed target assignment for mobs spawned by warp events. (#474)
- Fixed flickering Primal Charm tooltips and lost tooltip colors in the Thaumonomicon. (#481, #482)
- Fixed the FTB Library sidebar overlapping the Focal Manipulator. (#483)
- Fixed several Traditional Chinese death messages.
- Fixed machine and research-table actions being accepted without the matching menu open.
