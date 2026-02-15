/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2025 - 2026  Leander Knüttel and contributors
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

package de.the_build_craft.maplink.common;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author Leander Knüttel
 * @version 23.10.2025
 */
public class MainThreadTaskQueue {
    //Task queue for main / render thread calls needed on a background thread
    private static final Queue<QueuedTask<?>> taskQueue = new ConcurrentLinkedQueue<>();
    private static final int tasksPerFrame = 100;

    public static <T> QueuedTask<T> queueTask(Supplier<T> task) {
        QueuedTask<T> queuedTask = new QueuedTask<>(task);
        taskQueue.offer(queuedTask);
        return queuedTask;
    }

    public static QueuedTask<Void> queueTask(Runnable task) {
        return queueTask(() -> {task.run(); return null;});
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void executeQueuedTasks() {
        for (int i = 0; i < tasksPerFrame; i++) {
            if (taskQueue.isEmpty()) return;
            QueuedTask queuedTask = taskQueue.poll();
            if (queuedTask == null) continue;

            if (queuedTask.isCancelled()) {
                queuedTask.future.cancel(false);
                continue;
            }

            try {
                queuedTask.future.complete(queuedTask.task.get());
            } catch (Exception e) {
                queuedTask.future.completeExceptionally(e);
            }
        }
    }

    public static void clearQueue() {
        while (!taskQueue.isEmpty()) {
            QueuedTask<?> queuedTask = taskQueue.poll();
            if (queuedTask != null) queuedTask.cancel();
        }
    }

    public static <T> T waitForTask(Supplier<T> task) {
        QueuedTask<T> queuedTask = queueTask(task);
        try {
            return queuedTask.future.join();
        } catch (Exception e) {
            throw new RuntimeException("Error while executing Main-Thread-Task", e);
        }
    }

    public static void waitForTask(Runnable task) {
        waitForTask(() -> {task.run(); return null;});
    }

    public static class QueuedTask<T> {
        public final CompletableFuture<T> future;
        private final Supplier<T> task;
        private volatile boolean cancelled;

        public QueuedTask(Supplier<T> task) {
            this.task = task;
            this.future = new CompletableFuture<>();
            #if MC_VER > MC_1_16_5
            this.future.orTimeout(2, TimeUnit.MINUTES);
            #endif
        }

        public boolean cancel() {
            if (future.isDone()) return false;
            cancelled = true;
            future.cancel(false);
            return true;
        }

        public boolean isCancelled() {
            return cancelled || future.isCancelled();
        }
    }
}
