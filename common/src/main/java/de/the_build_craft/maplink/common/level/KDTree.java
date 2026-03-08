/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *    (some parts of this file are adapted from https://github.com/mgruben/Kd-Trees/blob/master/KdTree.java)
 *    (some functionality was also inspired by https://www.geeksforgeeks.org/java/java-program-to-construct-k-d-tree/)
 *
 *    Copyright (C) 2026  Leander Knüttel and contributors
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.the_build_craft.maplink.common.level;

import java.util.Arrays;

/**
 * @author Leander Knüttel
 * @version 08.03.2026
 */
class KDTree {
    private Node root;

    static class Node {
        float[] color;
        short blockId;
        Node left;
        Node right;

        public Node(float[] color, short blockId) {
            this.color = color;
            this.blockId = blockId;
        }
    }

    public void insert(float[] color, short blockId) {
        root = insertNode(root, color, blockId, 0);
    }

    private Node insertNode(Node root, float[] color, short blockId, int depth) {
        if (root == null) return new Node(color, blockId);

        int dimension = depth % 3;
        depth++;

        if (color[dimension] < root.color[dimension]) root.left = insertNode(root.left, color, blockId, depth);
        else root.right = insertNode(root.right, color, blockId, depth);

        return root;
    }

    public short searchNearest(float[] color) {
        return searchNearestNode(root, color, 0, root).blockId;
    }

    private Node searchNearestNode(Node root, float[] color, int depth, Node currNearest) {
        if (root == null) return currNearest;
        if (Arrays.equals(root.color, color)) return root;
        if (distanceSquared(root.color, color) < distanceSquared(currNearest.color, color)) {
            currNearest = root;
        }

        int dimension = depth % 3;
        depth++;
        float dimComp = color[dimension] - root.color[dimension];

        if (dimComp < 0) {
            currNearest = searchNearestNode(root.left, color, depth, currNearest);
            if (distanceSquared(currNearest.color, color) >= dimComp * dimComp) {
                currNearest = searchNearestNode(root.right, color, depth, currNearest);
            }
        } else {
            currNearest = searchNearestNode(root.right, color, depth, currNearest);
            if (distanceSquared(currNearest.color, color) >= dimComp * dimComp) {
                currNearest = searchNearestNode(root.left, color, depth, currNearest);
            }
        }

        return currNearest;
    }

    private float distanceSquared(float[] a, float[] b) {
        float dist = 0;
        for (int i = 0; i < a.length; i++) {
            float d = a[i] - b[i];
            dist += d * d;
        }
        return dist;
    }
}