//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.panels;


import org.apache.commons.lang.ArrayUtils;
import org.bukkit.conversations.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


public class ConversationUtils
{
	// ---------------------------------------------------------------------
// Section: Conversation API implementation
// ---------------------------------------------------------------------


	/**
	 * This method will close opened gui and writes inputText in chat. After players answers on
	 * inputText in chat, message will trigger consumer and gui will reopen.
	 * Success and fail messages can be implemented like that, as user's chat options are disabled
	 * while it is in conversation.
	 * @param consumer Consumer that accepts player output text.
	 * @param question Message that will be displayed in chat when player triggers conversion.
	 * @param successMessage Message that will be displayed on successful operation.
	 * @param failMessage Message that will be displayed on failed operation.
	 * @param user User who is targeted with current confirmation.
	 */
	public static void createConfirmation(Consumer<Boolean> consumer,
		@NotNull String question,
		@Nullable String successMessage,
		@Nullable String failMessage,
		User user)
	{
		ValidatingPrompt confirmationPrompt = new ValidatingPrompt()
		{
			/**
			 * Is input valid boolean.
			 *
			 * @param context the context
			 * @param input the input
			 * @return the boolean
			 */
			protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input)
			{
				String[] accepted = new String[]{
					"true", "false",
					"on", "off",
					"yes", "no",
					"y", "n",
					"1", "0",
					"right", "wrong",
					"correct", "incorrect",
					"valid", "invalid",
					"confirm", "deny",
					"cancel",
					"exit"
				};
				return ArrayUtils.contains(accepted, input.toLowerCase());
			}


			/**
			 * Accept validated input prompt.
			 *
			 * @param context the context
			 * @param input the input
			 * @return the prompt
			 */
			@Nullable
			protected Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input)
			{
				if (input.equalsIgnoreCase("true") ||
					input.equalsIgnoreCase("on") ||
					input.equalsIgnoreCase("yes") ||
					input.equalsIgnoreCase("y") ||
					input.equals("1") ||
					input.equalsIgnoreCase("right") ||
					input.equalsIgnoreCase("correct") ||
					input.equalsIgnoreCase("valid") ||
					input.equalsIgnoreCase("confirm"))
				{
					// Add answer to consumer.
					consumer.accept(true);

					if (successMessage != null)
					{
						// Return message about successful operation.
						return new MessagePrompt()
						{
							@Override
							protected @Nullable Prompt getNextPrompt(@NotNull ConversationContext context)
							{
								return Prompt.END_OF_CONVERSATION;
							}


							@Override
							public @NotNull String getPromptText(@NotNull ConversationContext context)
							{
								return successMessage;
							}
						};
					}
				}
				else
				{
					// Add answer to consumer.
					consumer.accept(false);

					// Return message about failed operation.
					if (failMessage != null)
					{
						return new MessagePrompt()
						{
							@Override
							protected @Nullable Prompt getNextPrompt(@NotNull ConversationContext context)
							{
								return Prompt.END_OF_CONVERSATION;
							}


							@Override
							public @NotNull String getPromptText(@NotNull ConversationContext context)
							{
								return failMessage;
							}
						};
					}
				}

				return Prompt.END_OF_CONVERSATION;
			}


			/**
			 * @see Prompt#getPromptText(ConversationContext)
			 */
			@Override
			public @NotNull String getPromptText(@NotNull ConversationContext conversationContext)
			{
				// Close input GUI.
				user.closeInventory();
				// There are no editable message. Just return question.
				return question;
			}
		};

		Conversation conversation = new ConversationFactory(BentoBox.getInstance()).
			withFirstPrompt(confirmationPrompt).
			withLocalEcho(false).
			withPrefix(context -> user.getTranslation(Constants.QUESTIONS + "prefix")).
			buildConversation(user.getPlayer());

		conversation.begin();
	}
}
