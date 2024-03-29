//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.panels.admin;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;


/**
 * This class manages settings for Magic Cobblestone Generator Addon.
 */
public class SettingsPanel extends CommonPanel
{
    /**
     * This is default constructor for all classes that extends CommonPanel.
     *
     * @param parentPanel Parent panel of current panel.
     */
    protected SettingsPanel(CommonPanel parentPanel)
    {
        super(parentPanel);
        this.settings = this.addon.getSettings();
    }


    /**
     * This method allows to build panel.
     */
    @Override
    public void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "settings"));

        PanelUtils.fillBorder(panelBuilder, 4, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(10, this.createButton(Action.OFFLINE_GENERATION));
        panelBuilder.item(11, this.createButton(Action.WORKING_RANGE));
        panelBuilder.item(20, this.createButton(Action.ACTIVE_GENERATORS));

        panelBuilder.item(12, this.createButton(Action.USE_PHYSIC));

        if (this.addon.isBankProvided())
        {
            panelBuilder.item(14, this.createButton(Action.USE_BANK));
        }

        panelBuilder.item(16, this.createButton(Action.UNLOCK_NOTIFY));
        panelBuilder.item(25, this.createButton(Action.DISABLE_ON_ACTIVATE));

        panelBuilder.item(35, this.createButton(Action.RETURN));
        panelBuilder.build();
    }


    /**
     * Create button panel item with a given button type.
     *
     * @param button the button
     * @return the panel item
     */
    private PanelItem createButton(Action button)
    {
        String reference = Constants.BUTTON + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");
        List<String> description = new ArrayList<>(2);
        description.add(this.user.getTranslationOrNothing(reference + ".description"));

        Material material;
        PanelItem.ClickHandler clickHandler;
        boolean glow = false;
        int count = 1;

        switch (button)
        {
            case OFFLINE_GENERATION:
            {
                clickHandler = (panel, user, clickType, i) -> {
                    this.settings.setOfflineGeneration(!this.settings.isOfflineGeneration());
                    this.saveSettings();
                    // Update button in panel
                    this.build();

                    return true;
                };

                glow = this.settings.isOfflineGeneration();

                if (glow)
                {
                    description.add(this.user.getTranslation(reference + ".enabled"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".disabled"));
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));

                material = Material.REDSTONE_TORCH;

                break;
            }
            case USE_PHYSIC:
            {
                clickHandler = (panel, user, clickType, i) -> {
                    this.settings.setUsePhysics(!this.settings.isUsePhysics());
                    this.saveSettings();
                    // Update button in panel
                    this.build();

                    return true;
                };

                glow = this.settings.isUsePhysics();

                if (glow)
                {
                    description.add(this.user.getTranslation(reference + ".enabled"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".disabled"));
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));

                material = Material.PISTON;

                break;
            }
            case WORKING_RANGE:
            {
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.settings.setDefaultWorkingRange(number.intValue());
                            this.saveSettings();
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        2000);

                    return true;
                };

                description.add(this.user.getTranslation(reference + ".value",
                    Constants.NUMBER, String.valueOf(this.settings.getDefaultWorkingRange())));

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                material = Material.OAK_TRAPDOOR;
                count = this.settings.getDefaultWorkingRange();

                break;
            }
            case ACTIVE_GENERATORS:
            {
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.settings.setDefaultActiveGeneratorCount(number.intValue());
                            this.saveSettings();
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        1,
                        2000);

                    return true;
                };

                description.add(this.user.getTranslation(reference + ".value",
                    Constants.NUMBER, String.valueOf(this.settings.getDefaultActiveGeneratorCount())));

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                material = Material.BOOK;
                count = this.settings.getDefaultActiveGeneratorCount();

                break;
            }
            case UNLOCK_NOTIFY:
            {
                clickHandler = (panel, user, clickType, i) -> {
                    this.settings.setNotifyUnlockedGenerators(!this.settings.isNotifyUnlockedGenerators());
                    this.saveSettings();
                    // Update button in panel
                    this.build();

                    return true;
                };

                glow = this.settings.isNotifyUnlockedGenerators();

                if (glow)
                {
                    description.add(this.user.getTranslation(reference + ".enabled"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".disabled"));
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));

                material = Material.OAK_SIGN;

                break;
            }
            case DISABLE_ON_ACTIVATE:
            {
                clickHandler = (panel, user, clickType, i) -> {
                    this.settings.setOverwriteOnActive(!this.settings.isOverwriteOnActive());
                    this.saveSettings();
                    // Update button in panel
                    this.build();

                    return true;
                };

                glow = this.settings.isOverwriteOnActive();

                if (glow)
                {
                    description.add(this.user.getTranslation(reference + ".enabled"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".disabled"));
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));

                material = Material.SADDLE;

                break;
            }
            case RETURN:
            {
                description.add("");
                if (this.parentPanel != null)
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-return"));
                }
                else
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-quit"));
                }

                clickHandler = (panel, user, clickType, i) -> {
                    if (this.parentPanel != null)
                    {
                        this.parentPanel.reopen();
                    }
                    else
                    {
                        user.closeInventory();
                    }

                    return true;
                };

                material = Material.OAK_DOOR;

                break;
            }
            case USE_BANK:
            {
                clickHandler = (panel, user, clickType, i) -> {
                    this.settings.setUseBankAccount(!this.settings.isUseBankAccount());
                    this.saveSettings();
                    // Update button in panel
                    this.build();

                    return true;
                };

                glow = this.settings.isUseBankAccount();

                if (glow)
                {
                    description.add(this.user.getTranslation(reference + ".enabled"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".disabled"));
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));

                material = Material.GOLD_INGOT;

                break;
            }
            default:
                return PanelItem.empty();
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(new ItemStack(material, count == 0 ? 1 : count)).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method saves current settings.
     */
    private void saveSettings()
    {
        new Config<>(this.addon, Settings.class).saveConfigObject(this.settings);
    }


    /**
     * This method build settings panel from parent panel.
     *
     * @param panel ParentPanel.
     */
    public static void open(CommonPanel panel)
    {
        new SettingsPanel(panel).build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This enum holds all possible actions in current GUI.
     */
    private enum Action
    {
        /**
         * Process Offline Generation Action.
         */
        OFFLINE_GENERATION,
        /**
         * Process Use Physic Action.
         */
        USE_PHYSIC,
        /**
         * Process Use Bank Action.
         */
        USE_BANK,
        /**
         * Process Working Range Action.
         */
        WORKING_RANGE,
        /**
         * Process Default Active Generators Action.
         */
        ACTIVE_GENERATORS,
        /**
         * Process unlock notification action.
         */
        UNLOCK_NOTIFY,
        /**
         * Process disable on activate action.
         */
        DISABLE_ON_ACTIVATE,
        /**
         * Process Return Action.
         */
        RETURN
    }


    /**
     * Settings object to allow to change settings.
     */
    private final Settings settings;
}
