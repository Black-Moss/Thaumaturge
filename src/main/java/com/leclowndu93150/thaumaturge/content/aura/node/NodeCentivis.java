package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

final class NodeCentivis {
    private static final int CENTIVIS_PER_POINT = 100;
    private static final int ALLOWANCE_CAP_TICKS = 200;

    private final BlockEntityNode node;
    private final Object2IntMap<Holder<IAspect>> allowance = new Object2IntOpenHashMap<>();
    private final Object2IntMap<Holder<IAspect>> credit = new Object2IntOpenHashMap<>();
    private final Journal journal = new Journal();

    NodeCentivis(BlockEntityNode node) {
        this.node = node;
    }

    void clear() {
        allowance.clear();
        credit.clear();
    }

    void accrue() {
        List<AspectInstance> base = node.aspectsBase.entries();
        for (int i = 0; i < base.size(); i++) {
            AspectInstance entry = base.get(i);
            int rate = entry.amount();
            allowance.put(entry.aspect(), Math.min(rate * ALLOWANCE_CAP_TICKS, allowance.getInt(entry.aspect()) + rate));
        }
    }

    int available(Holder<IAspect> aspect) {
        return Math.min(allowance.getInt(aspect), node.held.amountOf(aspect) * CENTIVIS_PER_POINT + credit.getInt(aspect));
    }

    int drain(Holder<IAspect> aspect, int request, TransactionContext transaction) {
        if (request <= 0 || available(aspect) <= 0) {
            return 0;
        }
        int currentAllowance = allowance.getInt(aspect);
        int used = Math.min(currentAllowance, request);
        int points = node.held.amountOf(aspect);
        int stored = credit.getInt(aspect);
        journal.updateSnapshots(transaction);
        int pool = stored;
        int remainingPoints = points;
        while (pool < used && remainingPoints > 0) {
            remainingPoints--;
            pool += CENTIVIS_PER_POINT;
        }
        int taken = Math.min(used, pool);
        if (remainingPoints != points) {
            node.held = node.held.reduce(aspect, points - remainingPoints);
        }
        credit.put(aspect, pool - taken);
        allowance.put(aspect, currentAllowance - taken);
        return taken;
    }

    private record Snapshot(AspectList contained, Object2IntMap<Holder<IAspect>> allowance, Object2IntMap<Holder<IAspect>> credit) {
    }

    private final class Journal extends SnapshotJournal<Snapshot> {
        @Override
        protected Snapshot createSnapshot() {
            return new Snapshot(node.held, new Object2IntOpenHashMap<>(allowance), new Object2IntOpenHashMap<>(credit));
        }

        @Override
        protected void revertToSnapshot(Snapshot snapshot) {
            node.held = snapshot.contained();
            allowance.clear();
            allowance.putAll(snapshot.allowance());
            credit.clear();
            credit.putAll(snapshot.credit());
        }

        @Override
        protected void onRootCommit(Snapshot original) {
            if (!original.contained().equals(node.held)) {
                node.changed();
            }
        }
    }
}
