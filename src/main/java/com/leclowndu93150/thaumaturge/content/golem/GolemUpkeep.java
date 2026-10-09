package com.leclowndu93150.thaumaturge.content.golem;

final class GolemUpkeep {
    private static final GolemDuty[] SERVER_DUTIES = {EntityThaumaturgeGolem::holdFlyerAltitude, EntityThaumaturgeGolem::forgetEndedTask, EntityThaumaturgeGolem::correctHome,
            EntityThaumaturgeGolem::validateTarget, EntityThaumaturgeGolem::tickAccessories, EntityThaumaturgeGolem::regenerate, EntityThaumaturgeGolem::followClimbable,
            EntityThaumaturgeGolem::tickAbilities};
    private static final GolemDuty[] CLIENT_DUTIES = {EntityThaumaturgeGolem::holdFlyerAltitude, EntityThaumaturgeGolem::flagRedraw, EntityThaumaturgeGolem::spinWheel,
            EntityThaumaturgeGolem::tickAbilities};

    private GolemUpkeep() {}

    static void runServer(EntityThaumaturgeGolem golem) {
        run(SERVER_DUTIES, golem);
    }

    static void runClient(EntityThaumaturgeGolem golem) {
        run(CLIENT_DUTIES, golem);
    }

    private static void run(GolemDuty[] duties, EntityThaumaturgeGolem golem) {
        for (GolemDuty duty : duties) {
            duty.perform(golem);
        }
    }
}
