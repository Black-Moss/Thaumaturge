package com.leclowndu93150.thaumaturge.content.eldritch.guardian;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;

public final class PostLedger {
    public static final Codec<PostLedger> CODEC = PostState.CODEC.listOf().xmap(PostLedger::new, ledger -> List.copyOf(ledger.posts));

    private final List<PostState> posts;

    public PostLedger() {
        this(List.of());
    }

    private PostLedger(List<PostState> posts) {
        this.posts = new ArrayList<>(posts);
    }

    List<PostState> posts() {
        return posts;
    }

    void add(PostState post) {
        posts.add(post);
    }

    void set(int slot, PostState post) {
        posts.set(slot, post);
    }

    boolean cleared(String group) {
        for (PostState post : posts) {
            if (post.guards(group) && !post.cleared()) {
                return false;
            }
        }
        return true;
    }
}
