package me.sparky.testplugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

public class TestPlugin extends JavaPlugin implements Listener {

    private Random random = new Random();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) {
            return;
        }

        FileConfiguration config = this.getConfig();
        double chance = config.getDouble("spawnChance", 0.3); // Default to 30% if not set

        if (random.nextDouble() < chance) {
            spawnDiamond(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());
        }
    }

    private void spawnDiamond(World world, int chunkX, int chunkZ) {
        // Find a random surface location in the chunk
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = world.getHighestBlockYAt(x, z);

        Location location = new Location(world, x, y, z);

        // Diamond creation
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Shiny Diamond");
        meta.setLore(Arrays.asList(ChatColor.LIGHT_PURPLE + "A special gem", ChatColor.LIGHT_PURPLE + "from the depths"));
        meta.addEnchant(Enchantment.LUCK, 1, false);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        // Drop diamond at the location
        world.dropItemNaturally(location, item);

        // Clear items after 60 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                world.getEntitiesByClass(org.bukkit.entity.Item.class).stream()
                        .filter(i -> i.getItemStack().equals(item) && i.getLocation().equals(location))
                        .forEach(org.bukkit.entity.Entity::remove);
            }
        }.runTaskLater(this, 1200L); // 20 ticks * 60 seconds

        // Log the information
        Logger logger = this.getLogger();
        logger.info("Spawned a diamond at " + location + " in world " + world.getName());
    }
}

