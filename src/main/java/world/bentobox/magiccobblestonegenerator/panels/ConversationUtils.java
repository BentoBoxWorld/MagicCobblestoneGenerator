//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.panels;


import com.sun.org.apache.bcel.internal.Const;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.eclipse.jdt.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

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
	 * This method will close opened gui and writes question in chat. After players answers on
	 * question in chat, message will trigger consumer and gui will reopen.
	 * Success and fail messages can be implemented like that, as user's chat options are disabled
	 * while it is in conversation.
	 * @param consumer Consumer that accepts player output text.
	 * @param question Message that will be displayed in chat when player triggers conversion.
	 * @param successMessage Message that will be displayed on successful operation.
	 * @param user User who is targeted with current confirmation.
	 */
	public static void createConfirmation(Consumer<Boolean> consumer,
		User user,
		@NotNull String question,
		@Nullable String successMessage)
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
						context.getForWhom().sendRawMessage(successMessage);
					}
				}
				else
				{
					// Add answer to consumer.
					consumer.accept(false);

					// Return message about failed operation.
					context.getForWhom().sendRawMessage(
						user.getTranslation(Constants.MESSAGE + "cancelled"));
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


	/**
	 * This method will close opened gui and writes question in chat. After players answers on
	 * question in chat, message will trigger consumer and gui will reopen.
	 * @param consumer Consumer that accepts player output text.
	 * @param validation Function that validates if input value is acceptable.
	 * @param question Message that will be displayed in chat when player triggers conversion.
	 * @param failTranslationLocation Message that will be displayed on failed operation.
	 * @param user User who is targeted with current confirmation.
	 */
	public static void createIDStringInput(Consumer<String> consumer,
		Function<String, Boolean> validation,
		User user,
		@NotNull String question,
		@Nullable String successMessage,
		@Nullable String failTranslationLocation)
	{
		ValidatingPrompt validatingPrompt = new ValidatingPrompt()
		{
			/**
			 * Gets the text to display to the user when
			 * this prompt is first presented.
			 *
			 * @param context Context information about the
			 * conversation.
			 * @return The text to display.
			 */
			@Override
			public String getPromptText(ConversationContext context)
			{
				// Close input GUI.
				user.closeInventory();

				// There are no editable message. Just return question.
				return question;
			}


			/**
			 * Override this method to check the validity of
			 * the player's input.
			 *
			 * @param context Context information about the
			 * conversation.
			 * @param input The player's raw console input.
			 * @return True or false depending on the
			 * validity of the input.
			 */
			@Override
			protected boolean isInputValid(ConversationContext context, String input)
			{
				return validation.apply(ConversationUtils.sanitizeInput(input));
			}


			/**
			 * Optionally override this method to
			 * display an additional message if the
			 * user enters an invalid input.
			 *
			 * @param context Context information
			 * about the conversation.
			 * @param invalidInput The invalid input
			 * provided by the user.
			 * @return A message explaining how to
			 * correct the input.
			 */
			@Override
			protected String getFailedValidationText(ConversationContext context,
				String invalidInput)
			{
				return user.getTranslation(failTranslationLocation,
					Constants.ID,
					ConversationUtils.sanitizeInput(invalidInput));
			}


			/**
			 * Override this method to accept and processes
			 * the validated input from the user. Using the
			 * input, the next Prompt in the prompt graph
			 * should be returned.
			 *
			 * @param context Context information about the
			 * conversation.
			 * @param input The validated input text from
			 * the user.
			 * @return The next Prompt in the prompt graph.
			 */
			@Override
			protected Prompt acceptValidatedInput(ConversationContext context, String input)
			{
				// Add answer to consumer.
				consumer.accept(ConversationUtils.sanitizeInput(input));
				// Send message that it is accepted.
				if (successMessage != null)
				{
					context.getForWhom().sendRawMessage(successMessage);
				}
				// End conversation
				return Prompt.END_OF_CONVERSATION;
			}
		};

		Conversation conversation = new ConversationFactory(BentoBox.getInstance()).
			withFirstPrompt(validatingPrompt).
			// On cancel conversation will be closed.
			withEscapeSequence("cancel").
			// Use null value in consumer to detect if user has abandoned conversation.
			addConversationAbandonedListener(abandonedEvent ->
			{
				if (!abandonedEvent.gracefulExit())
				{
					consumer.accept(null);
					// send cancell message
					abandonedEvent.getContext().getForWhom().sendRawMessage(
						user.getTranslation(Constants.MESSAGE + "cancelled"));
				}
			}).
			withLocalEcho(false).
			withPrefix(context -> user.getTranslation(Constants.QUESTIONS + "prefix")).
			buildConversation(user.getPlayer());

		conversation.begin();
	}


	/**
	 * This method will close opened gui and writes inputText in chat. After players answers on
	 * inputText in chat, message will trigger consumer and gui will reopen.
	 * @param consumer Consumer that accepts player output text.
	 * @param question Message that will be displayed in chat when player triggers conversion.
	 */
	public static void createNumericInput(Consumer<Number> consumer,
		@NotNull User user,
		@NotNull String question,
		Number minValue,
		Number maxValue)
	{
		// Create NumericPromt instance that will validate and process input.
		NumericPrompt numberPrompt = new NumericPrompt()
		{
			/**
			 * Override this method to perform some action
			 * with the user's integer response.
			 *
			 * @param context Context information about the
			 * conversation.
			 * @param input The user's response as a {@link
			 * Number}.
			 * @return The next {@link Prompt} in the prompt
			 * graph.
			 */
			@Override
			protected Prompt acceptValidatedInput(ConversationContext context, Number input)
			{
				// Add answer to consumer.
				consumer.accept(input);
				// End conversation
				return Prompt.END_OF_CONVERSATION;
			}


			/**
			 * Override this method to do further validation on the numeric player
			 * input after the input has been determined to actually be a number.
			 *
			 * @param context Context information about the conversation.
			 * @param input The number the player provided.
			 * @return The validity of the player's input.
			 */
			protected boolean isNumberValid(ConversationContext context, Number input)
			{
				return input.doubleValue() >= minValue.doubleValue() &&
					input.doubleValue() <= maxValue.doubleValue();
			}


			/**
			 * Optionally override this method to display an additional message if the
			 * user enters an invalid number.
			 *
			 * @param context Context information about the conversation.
			 * @param invalidInput The invalid input provided by the user.
			 * @return A message explaining how to correct the input.
			 */
			@Override
			protected String getInputNotNumericText(ConversationContext context, String invalidInput)
			{
				return user.getTranslation(Constants.ERRORS + "numeric-only", Constants.VALUE, invalidInput);
			}


			/**
			 * Optionally override this method to display an additional message if the
			 * user enters an invalid numeric input.
			 *
			 * @param context Context information about the conversation.
			 * @param invalidInput The invalid input provided by the user.
			 * @return A message explaining how to correct the input.
			 */
			@Override
			protected String getFailedValidationText(ConversationContext context, Number invalidInput)
			{
				return user.getTranslation(Constants.ERRORS + "not-valid-value",
					Constants.VALUE, invalidInput.toString(),
					Constants.MIN, Double.toString(minValue.doubleValue()),
					Constants.MAX, Double.toString(maxValue.doubleValue()));
			}


			/**
			 * @see Prompt#getPromptText(ConversationContext)
			 */
			@Override
			public String getPromptText(ConversationContext conversationContext)
			{
				// Close input GUI.
				user.closeInventory();
				// There are no editable message. Just return question.
				return question;
			}
		};

		// Init conversation api.
		Conversation conversation = new ConversationFactory(BentoBox.getInstance()).
			withFirstPrompt(numberPrompt).
			withEscapeSequence("cancel").
			// Use null value in consumer to detect if user has abandoned conversation.
			addConversationAbandonedListener(abandonedEvent ->
			{
				if (!abandonedEvent.gracefulExit())
				{
					consumer.accept(null);
					// send cancel message
					abandonedEvent.getContext().getForWhom().sendRawMessage(
						user.getTranslation(Constants.MESSAGE + "cancelled"));
				}
			}).
			withLocalEcho(false).
			withPrefix(context -> user.getTranslation(Constants.QUESTIONS + "prefix")).
			buildConversation(user.getPlayer());

		conversation.begin();
	}


	/**
	 * Sanitizes the provided input.
	 * It replaces spaces and hyphens with underscores and lower cases the input.
	 * @param input input to sanitize
	 * @return sanitized input
	 */
	public static String sanitizeInput(String input)
	{
		return input.toLowerCase(Locale.ENGLISH).replace(" ", "_").replace("-", "_");
	}
}
