package chaos.systems;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ItemBuilder {
    private final ItemStack stack;
    private final Registry<Enchantment> registry;

    public ItemBuilder(Item item, Registry<Enchantment> registry) {
        this.stack = new ItemStack(item);
        this.registry = registry;
    }

    public ItemBuilder name(String name, Formatting color) {
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(color));
        return this;
    }

    public ItemBuilder enchant(RegistryKey<Enchantment> enchantment, int level) {
        stack.addEnchantment(registry.getOrThrow(enchantment), level);
        return this;
    }

    public <T> ItemBuilder component(ComponentType<T> type, T value) {
        stack.set(type, value);
        return this;
    }
    public ItemStack build() {
        return this.stack;
    }
}