package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import org.jspecify.annotations.Nullable;

public final class BlueprintMatrix {
    private int rows;
    private int cols;
    private @Nullable BlueprintPart[][] matrix;

    public BlueprintMatrix(Blueprint blueprint, int y) {
        int xs = blueprint.xSize();
        int zs = blueprint.zSize();
        this.rows = xs;
        this.cols = zs;
        this.matrix = new BlueprintPart[xs][zs];
        for (int x = 0; x < xs; x++) {
            for (int z = 0; z < zs; z++) {
                this.matrix[x][z] = blueprint.cell(y, x, z);
            }
        }
    }

    public void rotate90DegRight(int times) {
        int t = ((times % 4) + 4) % 4;
        for (int a = 0; a < t; a++) {
            BlueprintPart[][] next = new BlueprintPart[this.cols][this.rows];
            for (int i = 0; i < this.rows; i++) {
                for (int j = 0; j < this.cols; j++) {
                    next[j][this.rows - i - 1] = this.matrix[i][j];
                }
            }
            this.matrix = next;
            int tmp = this.rows;
            this.rows = this.cols;
            this.cols = tmp;
        }
    }

    public int rows() {
        return this.rows;
    }

    public int cols() {
        return this.cols;
    }

    public @Nullable BlueprintPart get(int row, int col) {
        return this.matrix[row][col];
    }
}
