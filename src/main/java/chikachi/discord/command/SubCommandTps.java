/*
 * Copyright (C) 2017 Chikachi
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package chikachi.discord.command;

import chikachi.discord.DiscordCommandSender;
import chikachi.discord.core.CoreUtils;
import com.google.common.base.Joiner;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.text.DecimalFormat;
import java.util.*;

public class SubCommandTps {
    private static final DecimalFormat timeFormatter = new DecimalFormat("########0.000");
    private static boolean colored = false;

    public static void execute(ICommandSender sender) {
        boolean isDiscord = sender instanceof DiscordCommandSender;
        MinecraftServer minecraftServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        List<String> tpsTimes = new ArrayList<>();

        Integer[] dimensionIds = DimensionManager.getIDs();
        HashMap<Integer, String> dimensionMap = new HashMap<>();

        for (Integer dimensionId : dimensionIds) {
            dimensionMap.put(dimensionId, DimensionManager.getProviderType(dimensionId).getName());
        }

        int maxDimensionIdLength = Math.max(CoreUtils.getMinValue(dimensionMap.keySet()).toString().length(), CoreUtils.getMaxValue(dimensionMap.keySet()).toString().length());
        int maxDimensionNameLength = Math.max(CoreUtils.getMinLength(dimensionMap.values()), CoreUtils.getMaxLength(dimensionMap.values()));

        SortedSet<Integer> sortedDimensionIds = new TreeSet<>(dimensionMap.keySet());

        for (Integer dimensionId : sortedDimensionIds) {
            String dimensionName = dimensionMap.get(dimensionId);

            double worldTickTime = CoreUtils.mean(minecraftServer.worldTickTimes.get(dimensionId)) * 1.0E-6D;
            double worldTPS = Math.min(1000.0 / worldTickTime, 20);

            tpsTimes.add(
                String.format(
                    "%s%s : Mean tick time: %s ms. Mean TPS: %s",
                    colored ? CoreUtils.tpsToColorString(worldTPS) : "",
                    String.format(
                        "Dim %s %s",
                        CoreUtils.padLeft(dimensionId + "", maxDimensionIdLength),
                        CoreUtils.padRight(dimensionName, maxDimensionNameLength)
                    ),
                    CoreUtils.padLeft(timeFormatter.format(worldTickTime), 6),
                    CoreUtils.padLeft(timeFormatter.format(worldTPS), 6)
                )
            );
        }

        double meanTickTime = CoreUtils.mean(minecraftServer.tickTimeArray) * 1.0E-6D;
        double meanTPS = Math.min(1000.0 / meanTickTime, 20);
        tpsTimes.add(
            String.format(
                "%s%s : Mean tick time: %s ms. Mean TPS: %s",
                colored ? CoreUtils.tpsToColorString(meanTPS) : "",
                CoreUtils.padRight("Overall", maxDimensionIdLength + maxDimensionNameLength + 5),
                CoreUtils.padLeft(timeFormatter.format(meanTickTime), 6),
                CoreUtils.padLeft(timeFormatter.format(meanTPS), 6)
            )
        );

        sender.sendMessage(
            new TextComponentString(
                String.format(
                    "\n```%s\n%s\n```",
                    colored ? "diff" : "lua",
                    Joiner.on("\n").join(tpsTimes)
                ).replace("\\:", ":")
            )
        );
    }
}