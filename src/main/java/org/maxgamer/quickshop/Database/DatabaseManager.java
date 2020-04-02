/*
 * This file is a part of project QuickShop, the name is DatabaseManager.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Database;

import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Timer;
import org.maxgamer.quickshop.Util.Util;
import org.maxgamer.quickshop.Util.WarningSender;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * Queued database manager. Use queue to solve run SQL make server lagg issue.
 */
public class DatabaseManager {

    private final Queue<DatabaseTask> sqlQueue = new LinkedBlockingQueue<>();

    private DatabaseTask lastFailedTask=null;

    @NotNull
    private final Database database;

    @NotNull
    private final QuickShop plugin;

    @NotNull
    private final WarningSender warningSender;

    @Nullable
    private BukkitTask task;

    private boolean useQueue;

    /**
     * Queued database manager. Use queue to solve run SQL make server lagg issue.
     *
     * @param plugin plugin main class
     * @param db database
     */
    public DatabaseManager(@NotNull QuickShop plugin, @NotNull Database db) {
        this.plugin = plugin;
        this.warningSender = new WarningSender(plugin, 600000);
        this.database = db;
        this.useQueue = plugin.getConfig().getBoolean("database.queue");

        if (!useQueue) {
            return;
        }
        try {
            task =
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getDatabaseManager().runTask();
                    }
                }.runTaskTimerAsynchronously(plugin, 1, plugin.getConfig().getLong("database.queue-commit-interval")*20);
        } catch (IllegalPluginAccessException e) {
            Util.debugLog("Plugin is disabled but trying create database task, move to Main Thread...");
            plugin.getDatabaseManager().runTask();
        }
    }

    /**
     * Internal method, runTasks in queue.
     */
    private void runTask() {
        DatabaseTask databaseTask=null;
        try {
            Connection connection=this.database.getConnection();
            //start our commit
            connection.setAutoCommit(false);
            while (true) {
                if (!connection.isValid(3000)) {
                    warningSender.sendWarn("Database connection may lost, we are trying reconnecting, if this message appear too many times, you should check your database file(sqlite) and internet connection(mysql).");
                    connection=database.getConnection();
                    continue; // Waiting next crycle and hope it success reconnected.
                }

                Timer timer = new Timer(true);
                if(lastFailedTask!=null){
                    Util.debugLog("Executing the SQL task which failed last time: " + lastFailedTask);
                    lastFailedTask.run(connection);
                    lastFailedTask=null;
                }else {
                    databaseTask = sqlQueue.poll();
                    if (databaseTask == null) {
                        break;
                    }
                    Util.debugLog("Executing the SQL task: " + databaseTask);
                    databaseTask.run(connection);
                }

                long tookTime = timer.endTimer();
                if (tookTime > 500) {
                    warningSender.sendWarn(
                            "Database performance warning: It took too long time ("
                                    + tookTime
                                    + "ms) to execute the task, it may cause the network connection with MySQL server or just MySQL server too slow, change to a better MySQL server or switch to a local SQLite database!");
                }
            }
            connection.commit();
            connection.close();
        } catch (SQLException sqle) {
            plugin.getSentryErrorReporter().ignoreThrow();
            lastFailedTask=databaseTask;
            this.plugin
                    .getLogger()
                    .log(Level.WARNING, "Database connection may lost, we are trying reconnecting, if this message appear too many times, you should check your database file(sqlite) and internet connection(mysql).", sqle);
        }

//        try {
//            this.database.getConnection().commit();
//        } catch (SQLException e) {
//            try {
//                this.database.getConnection().rollback();
//            } catch (SQLException ignored) {
//            }
//        }
    }

    /**
     * Add DatabaseTask to queue waiting flush to database,
     *
     * @param task The DatabaseTask you want add in queue.
     */
    public void add(DatabaseTask task) {
        if (useQueue) {
            sqlQueue.offer(task);
        } else {
            try {
                task.run();
            } catch (SQLException e) {
                e.printStackTrace();
                //retry
                add(task);
            }
        }
    }

    /**
     * Unload the DatabaseManager, run at onDisable()
     */
    public void unInit() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        plugin.getLogger().info("Please wait for the data to flush its data...");
        runTask();
    }

}